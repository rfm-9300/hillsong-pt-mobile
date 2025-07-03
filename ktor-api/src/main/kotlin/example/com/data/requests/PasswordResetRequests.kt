package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
) 