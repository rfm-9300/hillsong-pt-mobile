package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import rfm.com.entity.Gender
import java.time.LocalDateTime

/**
 * Request DTO for creating a check-in request
 * Used by parents to initiate a QR code-based check-in
 */
data class CreateCheckInRequestDto(
    @field:NotNull(message = "Child ID is required")
    val childId: Long,
    
    @field:NotNull(message = "Service ID is required")
    val serviceId: Long,
    
    val notes: String? = null
)

/**
 * Request DTO for approving a check-in request
 * Used by staff when scanning and approving a QR code
 */
data class ApproveCheckInDto(
    val notes: String? = null
)

/**
 * Request DTO for rejecting a check-in request
 * Used by staff when denying a check-in with a reason
 */
data class RejectCheckInDto(
    @field:NotBlank(message = "Rejection reason is required")
    val reason: String
)

/**
 * Response DTO for check-in request with QR code data
 * Returned when a parent creates a check-in request
 */
data class CheckInRequestResponse(
    val id: Long,
    val token: String,
    val qrCodeData: String, // Token string to be encoded in QR code
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val requestedBy: ParentResponse,
    val status: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val expiresAt: LocalDateTime,
    
    val expiresInSeconds: Long,
    val isExpired: Boolean
)

/**
 * Detailed response DTO for check-in request with medical information
 * Returned when staff scans a QR code to view request details
 */
data class CheckInRequestDetailsResponse(
    val id: Long,
    val child: ChildDetailedResponse, // Includes medical info
    val service: KidsServiceResponse,
    val requestedBy: ParentResponse,
    val status: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val expiresAt: LocalDateTime,
    
    val notes: String?,
    val isExpired: Boolean,
    val canBeProcessed: Boolean,
    
    // Medical alerts highlighted for staff
    val hasMedicalAlerts: Boolean,
    val hasAllergies: Boolean,
    val hasSpecialNeeds: Boolean
)

/**
 * Detailed child response with medical information for staff
 * Used in check-in verification to show all relevant child details
 */
data class ChildDetailedResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val ageGroup: String,
    val gender: Gender?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val medicalNotes: String?,
    val allergies: String?,
    val specialNeeds: String?,
    val pickupAuthorization: String?
)

/**
 * Response DTO after successful check-in approval
 * Returned when staff approves a check-in request
 */
data class CheckInApprovalResponse(
    val requestId: Long,
    val attendanceId: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime,
    
    val approvedBy: String,
    val message: String
)

/**
 * Response DTO after check-in rejection
 * Returned when staff rejects a check-in request
 */
data class CheckInRejectionResponse(
    val requestId: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val rejectedBy: String,
    val reason: String,
    val message: String
)

/**
 * WebSocket notification DTO for real-time check-in status updates
 * Sent to parents when their check-in request status changes
 */
data class CheckInStatusNotification(
    val requestId: Long,
    val childId: Long,
    val serviceId: Long,
    val status: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: LocalDateTime,
    
    val message: String? = null,
    val rejectionReason: String? = null,
    val approvedBy: String? = null,
    val attendanceId: Long? = null
)
