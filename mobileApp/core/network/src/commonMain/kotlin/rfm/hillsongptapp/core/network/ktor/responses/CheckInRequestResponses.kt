package rfm.hillsongptapp.core.network.ktor.responses

import kotlinx.serialization.Serializable

/**
 * Network response DTOs for Check-In Request API
 */

@Serializable
data class ChildSummaryResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val age: Int,
    val ageGroup: String
)

@Serializable
data class ChildDetailedResponse(
    val id: String,
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
    val id: String,
    val name: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String
)

@Serializable
data class ParentSummaryResponse(
    val id: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String? = null
)


@Serializable
data class CheckInRequestResponse(
    val id: String,
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
    val id: String,
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
    val requestId: String,
    val attendanceId: String,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val checkInTime: String,
    val approvedBy: String,
    val message: String
)

@Serializable
data class CheckInRejectionResponse(
    val requestId: String,
    val child: ChildSummaryResponse,
    val service: KidsServiceResponse,
    val rejectedBy: String,
    val reason: String,
    val message: String
)

@Serializable
data class CheckInStatusNotification(
    val requestId: String,
    val childId: String,
    val serviceId: String,
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
