package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class SingUpRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
)