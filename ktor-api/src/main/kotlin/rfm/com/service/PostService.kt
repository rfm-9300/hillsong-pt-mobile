package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Post
import rfm.com.entity.PostComment
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.repository.PostCommentRepository
import rfm.com.repository.PostRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

/**
 * Service for managing posts and post-related operations
 */
@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val postCommentRepository: PostCommentRepository,
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(PostService::class.java)
    
    /**
     * Create a new post
     */
    fun createPost(createPostRequest: CreatePostRequest, authorId: Long, headerImage: MultipartFile?): PostResponse {
        logger.info("Creating new post with title: '${createPostRequest.title}' by user ID: $authorId")
        
        val author = userRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("User not found with ID: $authorId") }
        
        // Handle header image upload
        val headerImagePath = headerImage?.let { image ->
            if (!image.isEmpty) {
                fileStorageService.storePostImage(image)
            } else null
        } ?: "default-header.jpg"
        
        val post = Post(
            title = createPostRequest.title,
            content = createPostRequest.content,
            author = author,
            headerImagePath = headerImagePath
        )
        
        val savedPost = postRepository.save(post)
        logger.info("Post created successfully with ID: ${savedPost.id}")
        
        return mapToPostResponse(savedPost, null)
    }
    
    /**
     * Get all posts with pagination
     */
    @Transactional(readOnly = true)
    fun getAllPosts(page: Int = 0, size: Int = 20, sortBy: String = "date", sortDirection: String = "desc", currentUserId: Long? = null): PostPageResponse {
        logger.debug("Fetching posts - page: $page, size: $size, sortBy: $sortBy, sortDirection: $sortDirection")
        
        val sort = if (sortDirection.lowercase() == "asc") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }
        
        val pageable = PageRequest.of(page, size, sort)
        val postsPage = postRepository.findAllWithAuthor(pageable)
        
        val currentUser = currentUserId?.let { userRepository.findById(it).orElse(null) }
        
        val postResponses = postsPage.content.map { post ->
            mapToPostResponse(post, currentUser)
        }
        
        return PostPageResponse(
            posts = postResponses,
            currentPage = postsPage.number,
            totalPages = postsPage.totalPages,
            totalElements = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
            hasPrevious = postsPage.hasPrevious()
        )
    }
    
    /**
     * Get a post by ID
     */
    @Transactional(readOnly = true)
    fun getPostById(postId: Long, currentUserId: Long? = null): PostResponse {
        logger.debug("Fetching post with ID: $postId")
        
        val post = postRepository.findByIdWithAllRelationships(postId)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val currentUser = currentUserId?.let { userRepository.findById(it).orElse(null) }
        
        return mapToPostResponse(post, currentUser)
    }
    
    /**
     * Update an existing post
     */
    fun updatePost(postId: Long, updatePostRequest: UpdatePostRequest, userId: Long, headerImage: MultipartFile?): PostResponse {
        logger.info("Updating post with ID: $postId by user ID: $userId")
        
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        // Check if the user is the author or an admin
        val userProfile = userProfileRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User profile not found with ID: $userId") }
        
        if (post.author.id != userId && !userProfile.isAdmin) {
            throw SecurityException("User is not authorized to update this post")
        }
        
        // Handle header image update
        val newHeaderImagePath = headerImage?.let { image ->
            if (!image.isEmpty) {
                // Delete old image if it's not the default
                if (post.headerImagePath != "default-header.jpg") {
                    fileStorageService.deleteFile(post.headerImagePath)
                }
                fileStorageService.storePostImage(image)
            } else {
                post.headerImagePath
            }
        } ?: post.headerImagePath
        
        val updatedPost = post.copy(
            title = updatePostRequest.title,
            content = updatePostRequest.content,
            headerImagePath = newHeaderImagePath
        )
        
        val savedPost = postRepository.save(updatedPost)
        logger.info("Post updated successfully with ID: ${savedPost.id}")
        
        val currentUser = userRepository.findById(userProfile.user.id!!).orElse(null)
        return mapToPostResponse(savedPost, currentUser)
    }
    
    /**
     * Delete a post
     */
    fun deletePost(postId: Long, userId: Long): Boolean {
        logger.info("Deleting post with ID: $postId by user ID: $userId")
        
        val post = postRepository.findByIdWithAuthor(postId)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        // Check if the user is the author or an admin
        val userProfile = userProfileRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User profile not found with ID: $userId") }
        
        if (post.author.id != userId && !userProfile.isAdmin) {
            throw SecurityException("User is not authorized to delete this post")
        }
        
        // Delete header image if it's not the default
        if (post.headerImagePath != "default-header.jpg") {
            fileStorageService.deleteFile(post.headerImagePath)
        }
        
        postRepository.delete(post)
        logger.info("Post deleted successfully with ID: $postId")
        
        return true
    }
    
    /**
     * Like or unlike a post
     */
    fun togglePostLike(postId: Long, userId: Long): PostResponse {
        logger.debug("Toggling like for post ID: $postId by user ID: $userId")
        
        val post = postRepository.findByIdWithLikes(postId)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: $userId") }
        
        val isCurrentlyLiked = post.isLikedBy(user)
        
        if (isCurrentlyLiked) {
            post.removeLike(user)
            logger.debug("User $userId unliked post $postId")
        } else {
            post.addLike(user)
            logger.debug("User $userId liked post $postId")
        }
        
        val savedPost = postRepository.save(post)
        return mapToPostResponse(savedPost, user)
    }
    
    /**
     * Get posts by author
     */
    @Transactional(readOnly = true)
    fun getPostsByAuthor(authorId: Long, page: Int = 0, size: Int = 20, currentUserId: Long? = null): PostPageResponse {
        logger.debug("Fetching posts by author ID: $authorId")
        
        val author = userProfileRepository.findById(authorId)
            .orElseThrow { IllegalArgumentException("User profile not found with ID: $authorId") }
        
        val pageable = PageRequest.of(page, size, Sort.by("date").descending())
        val postsPage = postRepository.findByAuthor(author, pageable)
        
        val currentUser = currentUserId?.let { userRepository.findById(it).orElse(null) }
        
        val postResponses = postsPage.content.map { post ->
            mapToPostResponse(post, currentUser)
        }
        
        return PostPageResponse(
            posts = postResponses,
            currentPage = postsPage.number,
            totalPages = postsPage.totalPages,
            totalElements = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
            hasPrevious = postsPage.hasPrevious()
        )
    }
    
    /**
     * Search posts
     */
    @Transactional(readOnly = true)
    fun searchPosts(searchRequest: PostSearchRequest, currentUserId: Long? = null): PostPageResponse {
        logger.debug("Searching posts with criteria: $searchRequest")
        
        val sort = if (searchRequest.sortDirection.lowercase() == "asc") {
            Sort.by(searchRequest.sortBy).ascending()
        } else {
            Sort.by(searchRequest.sortBy).descending()
        }
        
        val pageable = PageRequest.of(searchRequest.page, searchRequest.size, sort)
        
        // For now, implement basic title search. More complex search can be added later
        val postsPage = if (!searchRequest.title.isNullOrBlank()) {
            postRepository.findByTitleContainingIgnoreCase(searchRequest.title, pageable)
        } else {
            postRepository.findAllWithAuthor(pageable)
        }
        
        val currentUser = currentUserId?.let { userRepository.findById(it).orElse(null) }
        
        val postResponses = postsPage.content.map { post ->
            mapToPostResponse(post, currentUser)
        }
        
        return PostPageResponse(
            posts = postResponses,
            currentPage = postsPage.number,
            totalPages = postsPage.totalPages,
            totalElements = postsPage.totalElements,
            hasNext = postsPage.hasNext(),
            hasPrevious = postsPage.hasPrevious()
        )
    }
    
    /**
     * Add a comment to a post
     */
    fun addComment(postId: Long, createCommentRequest: CreateCommentRequest, userId: Long): CommentResponse {
        logger.info("Adding comment to post ID: $postId by user ID: $userId")
        
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found with ID: $postId") }
        
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with ID: $userId") }
        
        val comment = PostComment(
            post = post,
            user = user,
            content = createCommentRequest.content
        )
        
        val savedComment = postCommentRepository.save(comment)
        logger.info("Comment added successfully with ID: ${savedComment.id}")
        
        return mapToCommentResponse(savedComment)
    }
    
    /**
     * Get comments for a post
     */
    @Transactional(readOnly = true)
    fun getPostComments(postId: Long, page: Int = 0, size: Int = 20): CommentPageResponse {
        logger.debug("Fetching comments for post ID: $postId")
        
        val post = postRepository.findById(postId)
            .orElseThrow { IllegalArgumentException("Post not found with ID: $postId") }
        
        val pageable = PageRequest.of(page, size, Sort.by("date").ascending())
        val commentsPage = postCommentRepository.findByPost(post, pageable)
        
        val commentResponses = commentsPage.content.map { comment ->
            mapToCommentResponse(comment)
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = commentsPage.number,
            totalPages = commentsPage.totalPages,
            totalElements = commentsPage.totalElements,
            hasNext = commentsPage.hasNext(),
            hasPrevious = commentsPage.hasPrevious()
        )
    }
    
    /**
     * Delete a comment
     */
    fun deleteComment(commentId: Long, userId: Long): Boolean {
        logger.info("Deleting comment with ID: $commentId by user ID: $userId")
        
        val comment = postCommentRepository.findByIdWithUser(commentId)
            ?: throw IllegalArgumentException("Comment not found with ID: $commentId")
        
        // Check if the user is the comment author or an admin
        val userProfile = userProfileRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User profile not found with ID: $userId") }
        
        if (comment.user.id != userProfile.user.id && !userProfile.isAdmin) {
            throw SecurityException("User is not authorized to delete this comment")
        }
        
        postCommentRepository.delete(comment)
        logger.info("Comment deleted successfully with ID: $commentId")
        
        return true
    }
    
    /**
     * Get post statistics
     */
    @Transactional(readOnly = true)
    fun getPostStats(): PostStatsResponse {
        logger.debug("Fetching post statistics")
        
        val totalPosts = postRepository.count()
        
        // Get most liked and most commented posts
        val mostLikedPostPage = postRepository.findMostLikedPosts(PageRequest.of(0, 1))
        val mostCommentedPostPage = postRepository.findMostCommentedPosts(PageRequest.of(0, 1))
        
        val mostLikedPost = if (mostLikedPostPage.hasContent()) {
            mapToPostResponse(mostLikedPostPage.content.first(), null)
        } else null
        
        val mostCommentedPost = if (mostCommentedPostPage.hasContent()) {
            mapToPostResponse(mostCommentedPostPage.content.first(), null)
        } else null
        
        // Calculate total likes and comments
        val allPosts = postRepository.findAll()
        val totalLikes = allPosts.sumOf { it.likeCount }.toLong()
        val totalComments = allPosts.sumOf { it.commentCount }.toLong()
        
        return PostStatsResponse(
            totalPosts = totalPosts,
            totalLikes = totalLikes,
            totalComments = totalComments,
            mostLikedPost = mostLikedPost,
            mostCommentedPost = mostCommentedPost
        )
    }
    
    /**
     * Map Post entity to PostResponse DTO
     */
    private fun mapToPostResponse(post: Post, currentUser: User?): PostResponse {
        val isLikedByCurrentUser = currentUser?.let { post.isLikedBy(it) } ?: false
        
        return PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            date = post.date,
            headerImagePath = post.headerImagePath,
            author = AuthorResponse(
                id = post.author.id!!,
                fullName = post.author.profile?.fullName ?: "${post.author.email}",
                email = post.author.email,
                imagePath = post.author.profile?.imagePath?.takeIf { it.isNotBlank() }
            ),
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            isLikedByCurrentUser = isLikedByCurrentUser
        )
    }
    
    /**
     * Map PostComment entity to CommentResponse DTO
     */
    private fun mapToCommentResponse(comment: PostComment): CommentResponse {
        return CommentResponse(
            id = comment.id!!,
            content = comment.content,
            date = comment.date,
            author = AuthorResponse(
                id = comment.user.id!!,
                fullName = comment.user.profile?.fullName ?: "${comment.user.email}",
                email = comment.user.email,
                imagePath = comment.user.profile?.imagePath?.takeIf { it.isNotBlank() }
            )
        )
    }
}