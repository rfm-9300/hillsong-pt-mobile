package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Post
import rfm.com.entity.PostComment
import rfm.com.entity.User
import rfm.com.repository.PostRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

/**
 * Service for managing posts and post-related operations
 */
@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(PostService::class.java)
    
    /**
     * Create a new post
     */
    fun createPost(createPostRequest: CreatePostRequest, authorId: String, headerImage: MultipartFile?): PostResponse {
        logger.info("Creating new post with title: '${createPostRequest.title}' by user ID: $authorId")
        
        val author = userRepository.findById(authorId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $authorId")
        
        val headerImagePath = headerImage?.let { image ->
            if (!image.isEmpty) {
                fileStorageService.storePostImage(image)
            } else null
        } ?: "default-header.jpg"
        
        val post = Post(
            title = createPostRequest.title,
            content = createPostRequest.content,
            authorId = authorId,
            headerImagePath = headerImagePath
        )
        
        val savedPost = postRepository.save(post)
        logger.info("Post created successfully with ID: ${savedPost.id}")
        
        return mapToPostResponse(savedPost, null)
    }
    
    /**
     * Get all posts with pagination
     */
    fun getAllPosts(page: Int = 0, size: Int = 20, sortBy: String = "date", sortDirection: String = "desc", currentUserId: String? = null): PostPageResponse {
        logger.debug("Fetching posts - page: $page, size: $size, sortBy: $sortBy, sortDirection: $sortDirection")
        
        val sort = if (sortDirection.lowercase() == "asc") {
            Sort.by(sortBy).ascending()
        } else {
            Sort.by(sortBy).descending()
        }
        
        val pageable = PageRequest.of(page, size, sort)
        val postsPage = postRepository.findAllByOrderByDateDesc(pageable)
        
        val postResponses = postsPage.content.map { post ->
            mapToPostResponse(post, currentUserId)
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
    fun getPostById(postId: String, currentUserId: String? = null): PostResponse {
        logger.debug("Fetching post with ID: $postId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        return mapToPostResponse(post, currentUserId)
    }
    
    /**
     * Update an existing post
     */
    fun updatePost(postId: String, updatePostRequest: UpdatePostRequest, userId: String, headerImage: MultipartFile?): PostResponse {
        logger.info("Updating post with ID: $postId by user ID: $userId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $userId")
        
        if (post.authorId != userId && !user.isAdmin) {
            throw SecurityException("User is not authorized to update this post")
        }
        
        val newHeaderImagePath = headerImage?.let { image ->
            if (!image.isEmpty) {
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
        
        return mapToPostResponse(savedPost, userId)
    }
    
    /**
     * Delete a post
     */
    fun deletePost(postId: String, userId: String): Boolean {
        logger.info("Deleting post with ID: $postId by user ID: $userId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $userId")
        
        if (post.authorId != userId && !user.isAdmin) {
            throw SecurityException("User is not authorized to delete this post")
        }
        
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
    fun togglePostLike(postId: String, userId: String): PostResponse {
        logger.debug("Toggling like for post ID: $postId by user ID: $userId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $userId")
        
        val isCurrentlyLiked = post.isLikedBy(userId)
        
        if (isCurrentlyLiked) {
            post.removeLike(userId)
            logger.debug("User $userId unliked post $postId")
        } else {
            post.addLike(userId)
            logger.debug("User $userId liked post $postId")
        }
        
        val savedPost = postRepository.save(post)
        return mapToPostResponse(savedPost, userId)
    }
    
    /**
     * Get posts by author
     */
    fun getPostsByAuthor(authorId: String, page: Int = 0, size: Int = 20, currentUserId: String? = null): PostPageResponse {
        logger.debug("Fetching posts by author ID: $authorId")
        
        userRepository.findById(authorId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $authorId")
        
        val pageable = PageRequest.of(page, size, Sort.by("date").descending())
        val postsPage = postRepository.findByAuthorId(authorId, pageable)
        
        val postResponses = postsPage.content.map { post ->
            mapToPostResponse(post, currentUserId)
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
    fun searchPosts(searchRequest: PostSearchRequest, currentUserId: String? = null): PostPageResponse {
        logger.debug("Searching posts with criteria: $searchRequest")
        
        val sort = if (searchRequest.sortDirection.lowercase() == "asc") {
            Sort.by(searchRequest.sortBy).ascending()
        } else {
            Sort.by(searchRequest.sortBy).descending()
        }
        
        val pageable = PageRequest.of(searchRequest.page, searchRequest.size, sort)
        
        // Use title search if title is provided, otherwise return all posts
        val posts = if (!searchRequest.title.isNullOrBlank()) {
            postRepository.findByTitleContainingIgnoreCase(searchRequest.title)
        } else {
            postRepository.findAllByOrderByDateDesc(pageable).content
        }
        
        // Apply additional in-memory filters
        val filtered = posts
            .let { list -> searchRequest.authorId?.let { authorId -> list.filter { it.authorId == authorId } } ?: list }
            .let { list -> searchRequest.minLikes?.let { min -> list.filter { it.likeCount >= min } } ?: list }
            .let { list -> searchRequest.minComments?.let { min -> list.filter { it.commentCount >= min } } ?: list }
        
        val totalElements = filtered.size.toLong()
        val pagedPosts = filtered.drop(searchRequest.page * searchRequest.size).take(searchRequest.size)
        val totalPages = if (searchRequest.size > 0) ((totalElements + searchRequest.size - 1) / searchRequest.size).toInt() else 0
        
        val postResponses = pagedPosts.map { post ->
            mapToPostResponse(post, currentUserId)
        }
        
        return PostPageResponse(
            posts = postResponses,
            currentPage = searchRequest.page,
            totalPages = totalPages,
            totalElements = totalElements,
            hasNext = searchRequest.page < totalPages - 1,
            hasPrevious = searchRequest.page > 0
        )
    }
    
    /**
     * Add a comment to a post (comments are now embedded in the Post document)
     */
    fun addComment(postId: String, createCommentRequest: CreateCommentRequest, userId: String): CommentResponse {
        logger.info("Adding comment to post ID: $postId by user ID: $userId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $userId")
        
        val comment = PostComment(
            id = org.bson.types.ObjectId().toString(),
            userId = userId,
            content = createCommentRequest.content,
            date = LocalDateTime.now()
        )
        
        post.addComment(comment)
        val savedPost = postRepository.save(post)
        
        val savedComment = savedPost.comments.last()
        logger.info("Comment added successfully with ID: ${savedComment.id}")
        
        return mapToCommentResponse(savedComment)
    }
    
    /**
     * Get comments for a post (comments are embedded in the Post document)
     */
    fun getPostComments(postId: String, page: Int = 0, size: Int = 20): CommentPageResponse {
        logger.debug("Fetching comments for post ID: $postId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val allComments = post.comments.sortedBy { it.date }
        val totalElements = allComments.size.toLong()
        val totalPages = if (size > 0) ((totalElements + size - 1) / size).toInt() else 0
        val pagedComments = allComments.drop(page * size).take(size)
        
        val commentResponses = pagedComments.map { comment ->
            mapToCommentResponse(comment)
        }
        
        return CommentPageResponse(
            comments = commentResponses,
            currentPage = page,
            totalPages = totalPages,
            totalElements = totalElements,
            hasNext = page < totalPages - 1,
            hasPrevious = page > 0
        )
    }
    
    /**
     * Delete a comment (embedded in the Post document)
     */
    fun deleteComment(postId: String, commentId: String, userId: String): Boolean {
        logger.info("Deleting comment with ID: $commentId from post $postId by user ID: $userId")
        
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("Post not found with ID: $postId")
        
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException("User not found with ID: $userId")
        
        val comment = post.comments.find { it.id == commentId }
            ?: throw IllegalArgumentException("Comment not found with ID: $commentId")
        
        if (comment.userId != userId && !user.isAdmin) {
            throw SecurityException("User is not authorized to delete this comment")
        }
        
        post.comments.removeIf { it.id == commentId }
        postRepository.save(post)
        logger.info("Comment deleted successfully with ID: $commentId")
        
        return true
    }
    
    /**
     * Get post statistics
     */
    fun getPostStats(): PostStatsResponse {
        logger.debug("Fetching post statistics")
        
        val totalPosts = postRepository.count()
        val allPosts = postRepository.findAll()
        val totalLikes = allPosts.sumOf { it.likeCount }.toLong()
        val totalComments = allPosts.sumOf { it.commentCount }.toLong()
        
        val mostLikedPost = allPosts.maxByOrNull { it.likeCount }?.let { mapToPostResponse(it, null) }
        val mostCommentedPost = allPosts.maxByOrNull { it.commentCount }?.let { mapToPostResponse(it, null) }
        
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
    private fun mapToPostResponse(post: Post, currentUserId: String?): PostResponse {
        val isLikedByCurrentUser = currentUserId?.let { post.isLikedBy(it) } ?: false
        val author = userRepository.findById(post.authorId).orElse(null)
        
        return PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            date = post.date,
            headerImagePath = post.headerImagePath,
            author = AuthorResponse(
                id = post.authorId,
                fullName = author?.fullName ?: "Unknown",
                email = author?.email ?: "",
                imagePath = author?.imagePath?.takeIf { it.isNotBlank() }
            ),
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            isLikedByCurrentUser = isLikedByCurrentUser
        )
    }
    
    /**
     * Map embedded PostComment to CommentResponse DTO
     */
    private fun mapToCommentResponse(comment: PostComment): CommentResponse {
        val user = userRepository.findById(comment.userId).orElse(null)
        
        return CommentResponse(
            id = comment.id ?: "",
            content = comment.content,
            date = comment.date,
            author = AuthorResponse(
                id = comment.userId,
                fullName = user?.fullName ?: "Unknown",
                email = user?.email ?: "",
                imagePath = user?.imagePath?.takeIf { it.isNotBlank() }
            )
        )
    }
}