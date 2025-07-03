package rfm.hillsongptapp.core.data.repository.ktor.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class FacebookAuthRequest(
    val accessToken: String
)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class VerificationRequest(
    val token: String
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
) 