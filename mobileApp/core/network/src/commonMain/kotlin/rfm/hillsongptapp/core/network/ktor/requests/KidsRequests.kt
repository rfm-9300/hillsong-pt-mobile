package rfm.hillsongptapp.core.network.ktor.requests

import kotlinx.serialization.Serializable

/**
 * Network request DTOs for Kids API
 */



@Serializable
data class ChildRegistrationRequest(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String, // Will be parsed as LocalDate on backend
    val gender: String? = null,
    val secondaryParentId: Long? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null
)

@Serializable
data class ChildUpdateRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: String? = null, // Will be parsed as LocalDate on backend
    val gender: String? = null,
    val secondaryParentId: Long? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null,
    val isActive: Boolean? = null
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