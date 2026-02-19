package rfm.com.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDateTime

data class AuthRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
)

data class SignUpRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "Confirm password is required")
    val confirmPassword: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 50, message = "First name must not exceed 50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 50, message = "Last name must not exceed 50 characters")
    val lastName: String
)

data class VerificationRequest(
    @field:NotBlank(message = "Verification token is required")
    val token: String
)

data class PasswordResetRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    val token: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

data class GoogleAuthRequest(
    @field:NotBlank(message = "Google ID token is required")
    val idToken: String
)

data class FacebookAuthRequest(
    @field:NotBlank(message = "Facebook access token is required")
    val accessToken: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: Instant = Instant.now()
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val verified: Boolean,
    val createdAt: Instant,
    val authProvider: String
)

data class UserProfileResponse(
    val id: String,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val imagePath: String?,
    val isAdmin: Boolean,
    val joinedAt: Instant,
    val fullName: String
)
