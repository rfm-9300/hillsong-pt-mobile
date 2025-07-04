package rfm.com.data.responses

import rfm.com.data.db.post.Post
import rfm.com.data.db.user.User
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
    data class AuthResponse(val token: String) : ApiResponseData()

    @Serializable
    data class PostListResponse(val postList: List<Post>) : ApiResponseData()

    @Serializable
    data class UserListResponse(val users: List<User>) : ApiResponseData()
}