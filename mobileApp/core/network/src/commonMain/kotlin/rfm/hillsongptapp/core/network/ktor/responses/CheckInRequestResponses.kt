package rfm.hillsongptapp.core.network.ktor.responses

import kotlinx.serialization.Serializable

/**
 * Network response DTOs for Check-In Request API
 */

@Serializable
data class ChildSummaryResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val ageGroup: String
)

@Serializable
data class ChildDetailedResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val ageGroup: String,
    val gender: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null
)

@Serializable
data class KidsServiceResponse(
    val id: Long,
    val name: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String
)

@Serializable
data class ParentSummaryResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String? = null
)


@Serializable
data class CheckInRequestResponse(
    val id: Long,
    val token: String,
    val qrCodeData: String,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val requestedBy: ParentSummaryResponse,
    val status: String,
    val createdAt: String,
    val expiresAt: String,
    val expiresInSeconds: Long,
    val isExpired: Boolean
)

@Serializable
data class CheckInRequestDetailsResponse(
    val id: Long,
    val child: ChildDetailedResponse,
    val service: KidsServiceResponse,
    val requestedBy: ParentSummaryResponse,
    val status: String,
    val createdAt: String,
    val expiresAt: String,
    val notes: String? = null,
    val isExpired: Boolean,
    val canBeProcessed: Boolean,
    val hasMedicalAlerts: Boolean,
    val hasAllergies: Boolean,
    val hasSpecialNeeds: Boolean
)

@Serializable
data class CheckInApprovalResponse(
    val requestId: Long,
    val attendanceId: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val checkInTime: String,
    val approvedBy: String,
    val message: String
)

@Serializable
data class CheckInRejectionResponse(
    val requestId: Long,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val rejectedBy: String,
    val reason: String,
    val message: String
)

@Serializable
data class CheckInStatusNotification(
    val requestId: Long,
    val childId: Long,
    val serviceId: Long,
    val status: String,
    val timestamp: String
)


// API Response wrappers
@Serializable
data class CheckInRequestApiResponse(
    val success: Boolean,
    val data: CheckInRequestResponse?,
    val message: String
)

@Serializable
data class CheckInRequestDetailsApiResponse(
    val success: Boolean,
    val data: CheckInRequestDetailsResponse?,
    val message: String
)

@Serializable
data class CheckInApprovalApiResponse(
    val success: Boolean,
    val data: CheckInApprovalResponse?,
    val message: String
)

@Serializable
data class CheckInRejectionApiResponse(
    val success: Boolean,
    val data: CheckInRejectionResponse?,
    val message: String
)

@Serializable
data class ActiveCheckInRequestsApiResponse(
    val success: Boolean,
    val data: List<CheckInRequestResponse>? = null,
    val message: String
)
