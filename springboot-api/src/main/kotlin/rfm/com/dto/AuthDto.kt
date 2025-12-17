package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Authentication request DTO
 */
data class AuthRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String
)

/**
 * Sign up request DTO
 */
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

/**
 * Email verification request DTO
 */
data class VerificationRequest(
    @field:NotBlank(message = "Verification token is required")
    val token: String
)

/**
 * Password reset request DTO
 */
data class PasswordResetRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String
)

/**
 * Reset password request DTO
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "Reset token is required")
    val token: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

/**
 * Google OAuth request DTO
 */
data class GoogleAuthRequest(
    @field:NotBlank(message = "Google ID token is required")
    val idToken: String
)

/**
 * Facebook OAuth request DTO
 */
data class FacebookAuthRequest(
    @field:NotBlank(message = "Facebook access token is required")
    val accessToken: String
)

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Authentication response DTO
 */
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

/**
 * User response DTO
 */
data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val verified: Boolean,
    val createdAt: LocalDateTime,
    val authProvider: String
)

/**
 * User profile response DTO
 */
data class UserProfileResponse(
    val id: Long,
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val imagePath: String,
    val isAdmin: Boolean,
    val joinedAt: LocalDateTime,
    val fullName: String
)

/**
 * Update profile request DTO
 */
data class UpdateProfileRequest(
    @field:Size(max = 50, message = "First name must not exceed 50 characters")
    val firstName: String?,
    
    @field:Size(max = 50, message = "Last name must not exceed 50 characters")
    val lastName: String?,
    
    @field:Size(max = 18, message = "Phone number must not exceed 18 characters")
    val phone: String?
)

/**
 * Update admin status request DTO
 */
data class UpdateAdminStatusRequest(
    val isAdmin: Boolean
)

/**
 * Admin create user request DTO
 */
data class CreateUserRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 50, message = "First name must not exceed 50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 50, message = "Last name must not exceed 50 characters")
    val lastName: String,
    
    val phone: String?,
    
    val isAdmin: Boolean = false
)

/**
 * Admin update user request DTO
 */
data class AdminUpdateUserRequest(
    @field:Size(max = 50, message = "First name must not exceed 50 characters")
    val firstName: String?,
    
    @field:Size(max = 50, message = "Last name must not exceed 50 characters")
    val lastName: String?,
    
    @field:Size(max = 18, message = "Phone number must not exceed 18 characters")
    val phone: String?,
    
    val isAdmin: Boolean?
)