package rfm.com.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

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
 * Simplified user response DTO (used in event/attendance responses)
 */
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: LocalDateTime
)

/**
 * User profile response DTO
 */
data class UserProfileResponse(
    val id: String,
    val userId: String,
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
 * Note: Credentials (password) are managed by auth-service.
 * This only creates a profile record in the API.
 */
data class CreateUserRequest(
    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
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