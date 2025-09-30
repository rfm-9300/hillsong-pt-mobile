package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import rfm.com.entity.Gender
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Request DTO for registering a new child
 */
data class ChildRegistrationRequest(
    @field:NotBlank(message = "First name is required")
    @field:Size(max = 100, message = "First name must not exceed 100 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(max = 100, message = "Last name must not exceed 100 characters")
    val lastName: String,
    
    @field:NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    
    val gender: Gender? = null,
    
    val secondaryParentId: Long? = null,
    
    @field:Size(max = 255, message = "Emergency contact name must not exceed 255 characters")
    val emergencyContactName: String? = null,
    
    @field:Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    val emergencyContactPhone: String? = null,
    
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null
)

/**
 * Request DTO for updating a child
 */
data class ChildUpdateRequest(
    @field:Size(max = 100, message = "First name must not exceed 100 characters")
    val firstName: String? = null,
    
    @field:Size(max = 100, message = "Last name must not exceed 100 characters")
    val lastName: String? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate? = null,
    
    val gender: Gender? = null,
    
    val secondaryParentId: Long? = null,
    
    @field:Size(max = 255, message = "Emergency contact name must not exceed 255 characters")
    val emergencyContactName: String? = null,
    
    @field:Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    val emergencyContactPhone: String? = null,
    
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null,
    val isActive: Boolean? = null
)

/**
 * Response DTO for child information
 */
data class ChildResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    
    val age: Int,
    val ageGroup: String,
    val gender: Gender? = null,
    
    val primaryParent: ParentResponse,
    val secondaryParent: ParentResponse? = null,
    
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null,
    
    val isActive: Boolean,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime? = null,
    
    val currentCheckInStatus: CheckInStatusResponse? = null
)

/**
 * Response DTO for parent information (simplified)
 */
data class ParentResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null
)

/**
 * Response DTO for check-in status
 */
data class CheckInStatusResponse(
    val isCheckedIn: Boolean,
    val serviceName: String? = null,
    val serviceId: Long? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime? = null,
    
    val checkedInBy: String? = null
)

/**
 * Request DTO for kids check-in
 */
data class KidsCheckInRequest(
    @field:NotNull(message = "Child ID is required")
    val childId: Long,
    
    @field:NotNull(message = "Service ID is required")
    val serviceId: Long,
    
    val notes: String? = null
)

/**
 * Request DTO for kids check-out
 */
data class KidsCheckOutRequest(
    @field:NotNull(message = "Child ID is required")
    val childId: Long,
    
    val notes: String? = null
)

/**
 * Response DTO for kids check-in operations
 */
data class KidsCheckInResponse(
    val id: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkOutTime: LocalDateTime? = null,
    
    val checkedInBy: String,
    val checkedOutBy: String? = null,
    val notes: String? = null,
    val status: String,
    val isCheckedOut: Boolean,
    val duration: Long? = null
)

/**
 * Response DTO for kids check-out operations
 */
data class KidsCheckOutResponse(
    val id: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkOutTime: LocalDateTime,
    
    val checkedInBy: String,
    val checkedOutBy: String,
    val notes: String? = null,
    val duration: Long
)

/**
 * Simplified child response for list views
 */
data class ChildSummaryResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val ageGroup: String,
    val isActive: Boolean
)

/**
 * Request DTO for service filtering
 */
data class ServiceFilterRequest(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val acceptingCheckIns: Boolean? = null,
    val location: String? = null,
    val dayOfWeek: String? = null
)

/**
 * Request DTO for pagination
 */
data class PaginationRequest(
    @field:Min(0, message = "Page must be non-negative")
    val page: Int = 0,
    
    @field:Min(1, message = "Page size must be positive")
    @field:Max(100, message = "Page size must not exceed 100")
    val pageSize: Int = 20
)

/**
 * Response DTO for children list API
 */
data class ChildrenApiResponse(
    val success: Boolean,
    val children: List<ChildResponse>,
    val message: String
)