package rfm.hillsongptapp.core.network.ktor.responses

import kotlinx.serialization.Serializable

/**
 * Network response DTOs for Kids API
 */

@Serializable
data class EmergencyContactResponse(
    val name: String,
    val phoneNumber: String,
    val relationship: String
)

@Serializable
data class ChildResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val dateOfBirth: String, // Serialized as string from LocalDate (YYYY-MM-DD)
    val age: Int,
    val ageGroup: String,
    val gender: String? = null,
    val primaryParent: ParentResponse,
    val secondaryParent: ParentResponse? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val medicalNotes: String? = null,
    val allergies: String? = null,
    val specialNeeds: String? = null,
    val pickupAuthorization: String? = null,
    val isActive: Boolean,
    val createdAt: String, // Serialized as string from LocalDateTime (ISO format)
    val updatedAt: String? = null,
    val currentCheckInStatus: CheckInStatusResponse? = null
)

@Serializable
data class ParentResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class CheckInStatusResponse(
    val isCheckedIn: Boolean,
    val serviceName: String? = null,
    val serviceId: Long? = null,
    val checkInTime: String? = null, // ISO format string
    val checkedInBy: String? = null
)

@Serializable
data class ServiceResponse(
    val id: Long,
    val name: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val leaderName: String? = null,
    val maxCapacity: Int,
    val minAge: Int,
    val maxAge: Int,
    val ageGroups: List<String>,
    val isActive: Boolean
)

@Serializable
data class CheckInRecordResponse(
    val id: String,
    val childId: String,
    val serviceId: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val checkedInBy: String,
    val checkedOutBy: String?,
    val notes: String?,
    val status: String
)

// API Response wrappers
@Serializable
data class ChildApiResponse(
    val success: Boolean,
    val data: ChildResponse?,
    val message: String
) {
    // Convenience property to match the expected interface
    val child: ChildResponse?
        get() = data
}

@Serializable
data class ChildrenApiResponse(
    val success: Boolean,
    val data: List<ChildResponse>? = null,
    val message: String
) {
    // Convenience property to match the expected interface
    val children: List<ChildResponse>
        get() = data ?: emptyList()
}

@Serializable
data class ServiceApiResponse(
    val success: Boolean,
    val data: ServiceResponse?,
    val message: String
) {
    // Convenience property to match the expected interface
    val service: ServiceResponse?
        get() = data
}

@Serializable
data class ServicesApiResponse(
    val success: Boolean,
    val data: List<ServiceResponse>? = null,
    val message: String,
    val timestamp: String? = null
) {
    // Convenience property to match the expected interface
    val services: List<ServiceResponse>
        get() = data ?: emptyList()
}

@Serializable
data class CheckInApiResponse(
    val success: Boolean,
    val data: CheckInRecordResponse?,
    val message: String
) {
    // Convenience properties to match the expected interface
    val record: CheckInRecordResponse?
        get() = data
}

@Serializable
data class CheckOutApiResponse(
    val success: Boolean,
    val data: CheckInRecordResponse?,
    val message: String
) {
    // Convenience properties to match the expected interface
    val record: CheckInRecordResponse?
        get() = data
}

@Serializable
data class CurrentCheckInsApiResponse(
    val success: Boolean,
    val data: List<CheckInRecordResponse>? = null,
    val message: String
) {
    // Convenience property to match the expected interface
    val records: List<CheckInRecordResponse>
        get() = data ?: emptyList()
}

@Serializable
data class CheckInHistoryApiResponse(
    val success: Boolean,
    val data: List<CheckInRecordResponse>? = null,
    val totalCount: Int? = null,
    val message: String
) {
    // Convenience property to match the expected interface
    val records: List<CheckInRecordResponse>
        get() = data ?: emptyList()
}

@Serializable
data class AttendanceReportApiResponse(
    val success: Boolean,
    val data: AttendanceReportResponse?,
    val message: String
) {
    // Convenience property to match the expected interface
    val report: AttendanceReportResponse?
        get() = data
}

@Serializable
data class AttendanceReportResponse(
    val startDate: String,
    val endDate: String,
    val totalCheckIns: Int,
    val uniqueChildren: Int,
    val serviceBreakdown: Map<String, Int>,
    val dailyBreakdown: Map<String, Int>,
    val generatedAt: String
)