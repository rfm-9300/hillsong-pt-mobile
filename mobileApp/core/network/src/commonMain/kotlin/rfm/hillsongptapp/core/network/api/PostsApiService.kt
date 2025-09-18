package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.ktor.responses.ApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.Post
import rfm.hillsongptapp.core.network.ktor.responses.PostsDataResponse
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Posts API service handling all post-related network operations
 * Supports both single requests and reactive streams using Flow
 */
interface PostsApiService {
    suspend fun getPosts(): NetworkResult<ApiResponse<PostsDataResponse>>
    suspend fun getPost(postId: Int): NetworkResult<Post>
    suspend fun createPost(title: String, content: String, headerImagePath: String?): NetworkResult<Post>
    suspend fun updatePost(postId: Int, title: String, content: String): NetworkResult<Post>
    suspend fun deletePost(postId: Int): NetworkResult<Unit>
    suspend fun likePost(postId: Int): NetworkResult<Unit>
    suspend fun unlikePost(postId: Int): NetworkResult<Unit>
    
    // Reactive streams for real-time updates
    fun getPostsStream(): Flow<NetworkResult<ApiResponse<PostsDataResponse>>>
}

/**
 * Implementation of PostsApiService using Ktor HTTP client
 */
class PostsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), PostsApiService {
    
    override suspend fun getPosts(): NetworkResult<ApiResponse<PostsDataResponse>> {
        return safeGet("api/posts")
    }
    
    override suspend fun getPost(postId: Int): NetworkResult<Post> {
        return safeGet("api/posts/$postId")
    }
    
    override suspend fun createPost(
        title: String, 
        content: String, 
        headerImagePath: String?
    ): NetworkResult<Post> {
        val requestBody = mapOf(
            "title" to title,
            "content" to content,
            "headerImagePath" to headerImagePath
        )
        return safePost("api/posts", requestBody)
    }
    
    override suspend fun updatePost(
        postId: Int, 
        title: String, 
        content: String
    ): NetworkResult<Post> {
        val requestBody = mapOf(
            "title" to title,
            "content" to content
        )
        return safePut("api/posts/$postId", requestBody)
    }
    
    override suspend fun deletePost(postId: Int): NetworkResult<Unit> {
        return safeDelete("api/posts/$postId")
    }
    
    override suspend fun likePost(postId: Int): NetworkResult<Unit> {
        return safePost("api/posts/$postId/like")
    }
    
    override suspend fun unlikePost(postId: Int): NetworkResult<Unit> {
        return safeDelete("api/posts/$postId/like")
    }
    
    override fun getPostsStream(): Flow<NetworkResult<ApiResponse<PostsDataResponse>>> = flow {
        emit(NetworkResult.Loading)
        emit(getPosts())
    }
}