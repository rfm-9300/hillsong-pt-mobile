package rfm.hillsongptapp.core.data.repository.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

@Serializable
data class AuthResponse(
    val token: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: AuthResponse
)

@Serializable
data class SignUpResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class VerificationResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class PasswordResetResponse(
    val success: Boolean,
    val message: String
) 