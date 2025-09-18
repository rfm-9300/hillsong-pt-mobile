package rfm.hillsongptapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.util.onError
import rfm.hillsongptapp.core.network.util.onSuccess
import rfm.hillsongptapp.core.network.ktor.responses.Post as NetworkPost

/**
 * Repository for post-related operations using the new HillsongApiClient
 * Handles both network operations and provides clean abstractions for the UI layer
 */
class PostRepository(
    private val apiClient: HillsongApiClient
) {
    
    /**
     * Get all posts with pagination
     */
    suspend fun getPosts(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "date",
        sortDirection: String = "desc"
    ): PostResult<PostPageResponse> {
        return when (val result = apiClient.posts.getPosts()) {
            is NetworkResult.Success -> {
                val response = result.data
                val postsData = response.data
                rfm.hillsongptapp.logging.LoggerHelper.logDebug("API Response: success=${response.success}, data=${postsData}", "PostRepo")
                if (response.success && postsData != null) {
                    rfm.hillsongptapp.logging.LoggerHelper.logDebug("Posts received: ${postsData.posts.size} posts", "PostRepo")
                    // Convert network posts to repository posts
                    val convertedPosts = postsData.posts.map { it.toPostResponse() }
                    rfm.hillsongptapp.logging.LoggerHelper.logDebug("Converted posts: ${convertedPosts.size} posts", "PostRepo")
                    val pageResponse = PostPageResponse(
                        posts = convertedPosts,
                        currentPage = page,
                        totalPages = 1, // We don't have pagination info from API
                        totalElements = convertedPosts.size.toLong(),
                        hasNext = false,
                        hasPrevious = false
                    )
                    PostResult.Success(pageResponse)
                } else {
                    rfm.hillsongptapp.logging.LoggerHelper.logDebug("API Response failed: ${response.message}", "PostRepo")
                    PostResult.Error(response.message ?: "Failed to fetch posts")
                }
            }
            is NetworkResult.Error -> {
                rfm.hillsongptapp.logging.LoggerHelper.logDebug("Network error: ${result.exception.message}", "PostRepo")
                PostResult.NetworkError(result.exception.message ?: "Network error occurred")
            }
            is NetworkResult.Loading -> {
                PostResult.Loading
            }
        }
    }
    
    /**
     * Get a specific post by ID
     */
    suspend fun getPostById(postId: Int): PostResult<PostResponse> {
        return when (val result = apiClient.posts.getPost(postId)) {
            is NetworkResult.Success -> {
                PostResult.Success(result.data.toPostResponse())
            }
            is NetworkResult.Error -> {
                PostResult.NetworkError(result.exception.message ?: "Network error occurred")
            }
            is NetworkResult.Loading -> {
                PostResult.Loading
            }
        }
    }
    
    /**
     * Like or unlike a post
     */
    suspend fun togglePostLike(postId: Int): PostResult<Unit> {
        return when (val result = apiClient.posts.likePost(postId)) {
            is NetworkResult.Success -> {
                PostResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                PostResult.NetworkError(result.exception.message ?: "Failed to toggle like")
            }
            is NetworkResult.Loading -> {
                PostResult.Loading
            }
        }
    }
    
    /**
     * Create a new post
     */
    suspend fun createPost(
        title: String,
        content: String,
        headerImagePath: String? = null
    ): PostResult<PostResponse> {
        return when (val result = apiClient.posts.createPost(title, content, headerImagePath)) {
            is NetworkResult.Success -> {
                PostResult.Success(result.data.toPostResponse())
            }
            is NetworkResult.Error -> {
                PostResult.NetworkError(result.exception.message ?: "Failed to create post")
            }
            is NetworkResult.Loading -> {
                PostResult.Loading
            }
        }
    }
    
    /**
     * Delete a post
     */
    suspend fun deletePost(postId: Int): PostResult<Unit> {
        return when (val result = apiClient.posts.deletePost(postId)) {
            is NetworkResult.Success -> {
                PostResult.Success(Unit)
            }
            is NetworkResult.Error -> {
                PostResult.NetworkError(result.exception.message ?: "Failed to delete post")
            }
            is NetworkResult.Loading -> {
                PostResult.Loading
            }
        }
    }
    
    /**
     * Get posts as a reactive stream for real-time updates
     */
    fun getPostsStream(): Flow<PostResult<PostPageResponse>> = flow {
        emit(PostResult.Loading)
        emit(getPosts())
    }
    
    /**
     * Search posts
     */
    suspend fun searchPosts(query: String): PostResult<List<PostResponse>> {
        // This would use the search endpoint when implemented in the API service
        // For now, we'll filter the existing posts
        return when (val result = getPosts()) {
            is PostResult.Success -> {
                val filteredPosts = result.data.posts.filter { post ->
                    post.title.contains(query, ignoreCase = true) ||
                    post.content.contains(query, ignoreCase = true)
                }
                PostResult.Success(filteredPosts)
            }
            is PostResult.Error -> PostResult.Error(result.message)
            is PostResult.NetworkError -> PostResult.NetworkError(result.message)
            is PostResult.Loading -> PostResult.Loading
        }
    }
}

/**
 * Sealed class for repository-level post results
 * Provides a clean abstraction over NetworkResult for the UI layer
 */
sealed class PostResult<out T> {
    data class Success<T>(val data: T) : PostResult<T>()
    data class Error(val message: String) : PostResult<Nothing>()
    data class NetworkError(val message: String) : PostResult<Nothing>()
    data object Loading : PostResult<Nothing>()
}

/**
 * Data classes matching the API structure
 */
@kotlinx.serialization.Serializable
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val date: String, // ISO date string
    val headerImagePath: String?,
    val author: AuthorResponse,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean = false
)

@kotlinx.serialization.Serializable
data class AuthorResponse(
    val id: Long,
    val fullName: String,
    val email: String,
    val imagePath: String?
)

@kotlinx.serialization.Serializable
data class PostPageResponse(
    val posts: List<PostResponse>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@kotlinx.serialization.Serializable
data class CommentResponse(
    val id: Long,
    val content: String,
    val date: String,
    val author: AuthorResponse
)

/**
 * Extension function to convert network Post to repository PostResponse
 */
private fun NetworkPost.toPostResponse(): PostResponse {
    return PostResponse(
        id = this.id,
        title = this.title,
        content = this.content,
        date = this.date,
        headerImagePath = this.headerImagePath,
        author = AuthorResponse(
            id = this.author.id,
            fullName = this.author.fullName,
            email = this.author.email,
            imagePath = this.author.imagePath
        ),
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        isLikedByCurrentUser = this.isLikedByCurrentUser
    )
}
