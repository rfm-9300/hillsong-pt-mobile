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
    val id: String,
    val parentId: String,
    val name: String,
    val dateOfBirth: String,
    val medicalInfo: String?,
    val dietaryRestrictions: String?,
    val emergencyContact: EmergencyContactResponse,
    val status: String,
    val currentServiceId: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ServiceResponse(
    val id: String,
    val name: String,
    val description: String,
    val minAge: Int,
    val maxAge: Int,
    val startTime: String,
    val endTime: String,
    val location: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val isAcceptingCheckIns: Boolean,
    val staffMembers: List<String>,
    val createdAt: String
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
    val child: ChildResponse?,
    val message: String
)

@Serializable
data class ChildrenApiResponse(
    val success: Boolean,
    val children: List<ChildResponse>,
    val message: String
)

@Serializable
data class ServiceApiResponse(
    val success: Boolean,
    val service: ServiceResponse?,
    val message: String
)

@Serializable
data class ServicesApiResponse(
    val success: Boolean,
    val services: List<ServiceResponse>,
    val message: String
)

@Serializable
data class CheckInApiResponse(
    val success: Boolean,
    val record: CheckInRecordResponse?,
    val updatedChild: ChildResponse?,
    val updatedService: ServiceResponse?,
    val message: String
)

@Serializable
data class CheckOutApiResponse(
    val success: Boolean,
    val record: CheckInRecordResponse?,
    val updatedChild: ChildResponse?,
    val updatedService: ServiceResponse?,
    val message: String
)

@Serializable
data class CurrentCheckInsApiResponse(
    val success: Boolean,
    val records: List<CheckInRecordResponse>,
    val message: String
)

@Serializable
data class CheckInHistoryApiResponse(
    val success: Boolean,
    val records: List<CheckInRecordResponse>,
    val totalCount: Int,
    val message: String
)

@Serializable
data class AttendanceReportApiResponse(
    val success: Boolean,
    val report: AttendanceReportResponse?,
    val message: String
)

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