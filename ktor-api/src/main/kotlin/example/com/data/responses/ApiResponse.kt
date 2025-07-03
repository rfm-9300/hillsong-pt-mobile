package example.com.data.responses

import example.com.data.db.post.Post
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success : Boolean,
    val message : String,
    val data: ApiResponseData? = null
)

@Serializable
sealed class ApiResponseData {
    @Serializable
    data class AuthResponse (val token: String) : ApiResponseData()
    @Serializable
    data class PostListResponse(val postList: List<Post>) : ApiResponseData()
}