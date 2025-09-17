package rfm.com.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.service.PostService
import rfm.com.util.getCurrentUserId

/**
 * REST controller for post management operations
 */
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    
    private val logger = LoggerFactory.getLogger(PostController::class.java)
    
    /**
     * Create a new post
     */
    @PostMapping
    fun createPost(
        @Valid @RequestPart("post") createPostRequest: CreatePostRequest,
        @RequestPart("image", required = false) headerImage: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<PostResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.info("Creating post for user ID: $userId")
            
            val postResponse = postService.createPost(createPostRequest, userId, headerImage)
            
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    success = true,
                    message = "Post created successfully",
                    data = postResponse
                )
            )
        } catch (ex: Exception) {
            logger.error("Error creating post", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Failed to create post"
                )
            )
        }
    }
    
    /**
     * Get all posts with pagination
     */
    @GetMapping
    fun getAllPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String,
        authentication: Authentication?
    ): ResponseEntity<ApiResponse<PostPageResponse>> {
        return try {
            val currentUserId = authentication?.getCurrentUserId()
            logger.debug("Fetching posts - page: $page, size: $size")
            
            val postsPage = postService.getAllPosts(page, size, sortBy, sortDirection, currentUserId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Posts retrieved successfully",
                    data = postsPage
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching posts", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostPageResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch posts"
                )
            )
        }
    }
    
    /**
     * Get a specific post by ID
     */
    @GetMapping("/{id}")
    fun getPostById(
        @PathVariable id: Long,
        authentication: Authentication?
    ): ResponseEntity<ApiResponse<PostResponse>> {
        return try {
            val currentUserId = authentication?.getCurrentUserId()
            logger.debug("Fetching post with ID: $id")
            
            val postResponse = postService.getPostById(id, currentUserId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Post retrieved successfully",
                    data = postResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch post"
                )
            )
        }
    }
    
    /**
     * Update an existing post
     */
    @PutMapping("/{id}")
    fun updatePost(
        @PathVariable id: Long,
        @Valid @RequestPart("post") updatePostRequest: UpdatePostRequest,
        @RequestPart("image", required = false) headerImage: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<PostResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.info("Updating post ID: $id by user ID: $userId")
            
            val postResponse = postService.updatePost(id, updatePostRequest, userId, headerImage)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Post updated successfully",
                    data = postResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found for update with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: SecurityException) {
            logger.warn("Unauthorized attempt to update post ID: $id")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Not authorized to update this post"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error updating post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Failed to update post"
                )
            )
        }
    }
    
    /**
     * Delete a post
     */
    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.info("Deleting post ID: $id by user ID: $userId")
            
            postService.deletePost(id, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Post deleted successfully",
                    data = "Post with ID $id has been deleted"
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found for deletion with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: SecurityException) {
            logger.warn("Unauthorized attempt to delete post ID: $id")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Not authorized to delete this post"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error deleting post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Failed to delete post"
                )
            )
        }
    }
    
    /**
     * Like or unlike a post
     */
    @PostMapping("/{id}/like")
    fun togglePostLike(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<PostResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.debug("Toggling like for post ID: $id by user ID: $userId")
            
            val postResponse = postService.togglePostLike(id, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Post like toggled successfully",
                    data = postResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found for like toggle with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error toggling like for post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostResponse>(
                    success = false,
                    message = ex.message ?: "Failed to toggle post like"
                )
            )
        }
    }
    
    /**
     * Get posts by author
     */
    @GetMapping("/author/{authorId}")
    fun getPostsByAuthor(
        @PathVariable authorId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        authentication: Authentication?
    ): ResponseEntity<ApiResponse<PostPageResponse>> {
        return try {
            val currentUserId = authentication?.getCurrentUserId()
            logger.debug("Fetching posts by author ID: $authorId")
            
            val postsPage = postService.getPostsByAuthor(authorId, page, size, currentUserId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Posts by author retrieved successfully",
                    data = postsPage
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Author not found with ID: $authorId")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<PostPageResponse>(
                    success = false,
                    message = ex.message ?: "Author not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching posts by author ID: $authorId", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostPageResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch posts by author"
                )
            )
        }
    }
    
    /**
     * Search posts
     */
    @PostMapping("/search")
    fun searchPosts(
        @Valid @RequestBody searchRequest: PostSearchRequest,
        authentication: Authentication?
    ): ResponseEntity<ApiResponse<PostPageResponse>> {
        return try {
            val currentUserId = authentication?.getCurrentUserId()
            logger.debug("Searching posts with criteria: $searchRequest")
            
            val postsPage = postService.searchPosts(searchRequest, currentUserId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Posts search completed successfully",
                    data = postsPage
                )
            )
        } catch (ex: Exception) {
            logger.error("Error searching posts", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostPageResponse>(
                    success = false,
                    message = ex.message ?: "Failed to search posts"
                )
            )
        }
    }
    
    /**
     * Add a comment to a post
     */
    @PostMapping("/{id}/comments")
    fun addComment(
        @PathVariable id: Long,
        @Valid @RequestBody createCommentRequest: CreateCommentRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CommentResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.info("Adding comment to post ID: $id by user ID: $userId")
            
            val commentResponse = postService.addComment(id, createCommentRequest, userId)
            
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    success = true,
                    message = "Comment added successfully",
                    data = commentResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found for comment with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<CommentResponse>(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error adding comment to post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<CommentResponse>(
                    success = false,
                    message = ex.message ?: "Failed to add comment"
                )
            )
        }
    }
    
    /**
     * Get comments for a post
     */
    @GetMapping("/{id}/comments")
    fun getPostComments(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<CommentPageResponse>> {
        return try {
            logger.debug("Fetching comments for post ID: $id")
            
            val commentsPage = postService.getPostComments(id, page, size)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Comments retrieved successfully",
                    data = commentsPage
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Post not found for comments with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<CommentPageResponse>(
                    success = false,
                    message = ex.message ?: "Post not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching comments for post with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<CommentPageResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch comments"
                )
            )
        }
    }
    
    /**
     * Delete a comment
     */
    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authentication.getCurrentUserId()
            logger.info("Deleting comment ID: $commentId by user ID: $userId")
            
            postService.deleteComment(commentId, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Comment deleted successfully",
                    data = "Comment with ID $commentId has been deleted"
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("Comment not found for deletion with ID: $commentId")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Comment not found"
                )
            )
        } catch (ex: SecurityException) {
            logger.warn("Unauthorized attempt to delete comment ID: $commentId")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Not authorized to delete this comment"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error deleting comment with ID: $commentId", ex)
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Failed to delete comment"
                )
            )
        }
    }
    
    /**
     * Get post statistics
     */
    @GetMapping("/stats")
    fun getPostStats(): ResponseEntity<ApiResponse<PostStatsResponse>> {
        return try {
            logger.debug("Fetching post statistics")
            
            val stats = postService.getPostStats()
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Post statistics retrieved successfully",
                    data = stats
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching post statistics", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<PostStatsResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch post statistics"
                )
            )
        }
    }
}