package rfm.hillsongptapp.core.network.ktor.requests

import kotlinx.serialization.Serializable

/**
 * Network request DTOs for Kids API
 */

@Serializable
data class EmergencyContactRequest(
    val name: String,
    val phoneNumber: String,
    val relationship: String
)

@Serializable
data class ChildRegistrationRequest(
    val parentId: String,
    val name: String,
    val dateOfBirth: String,
    val medicalInfo: String?,
    val dietaryRestrictions: String?,
    val emergencyContact: EmergencyContactRequest
)

@Serializable
data class ChildUpdateRequest(
    val name: String,
    val dateOfBirth: String,
    val medicalInfo: String?,
    val dietaryRestrictions: String?,
    val emergencyContact: EmergencyContactRequest
)

@Serializable
data class CheckInRequest(
    val childId: String,
    val serviceId: String,
    val checkedInBy: String,
    val notes: String?
)

@Serializable
data class CheckOutRequest(
    val childId: String,
    val checkedOutBy: String,
    val notes: String?
)

@Serializable
data class ServiceFilterRequest(
    val minAge: Int?,
    val maxAge: Int?,
    val acceptingCheckIns: Boolean?,
    val location: String?
)

@Serializable
data class PaginationRequest(
    val page: Int,
    val pageSize: Int
)

@Serializable
data class AttendanceReportRequest(
    val startDate: String,
    val endDate: String,
    val serviceIds: List<String>?
)