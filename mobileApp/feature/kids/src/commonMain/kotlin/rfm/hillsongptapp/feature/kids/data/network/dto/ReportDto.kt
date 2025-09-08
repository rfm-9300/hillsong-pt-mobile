package rfm.hillsongptapp.feature.kids.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Service Report API responses
 */
@Serializable
data class ServiceReportDto(
    val serviceId: String,
    val serviceName: String,
    val totalCapacity: Int,
    val currentCheckIns: Int,
    val availableSpots: Int,
    val checkedInChildren: List<ChildDto>,
    val staffMembers: List<String>,
    val generatedAt: String // ISO 8601 format
)

/**
 * Data Transfer Object for Attendance Report API responses
 */
@Serializable
data class AttendanceReportDto(
    val startDate: String,
    val endDate: String,
    val totalCheckIns: Int,
    val uniqueChildren: Int,
    val serviceBreakdown: Map<String, Int>,
    val dailyBreakdown: Map<String, Int>,
    val generatedAt: String // ISO 8601 format
)

/**
 * Response DTO for service report operations
 */
@Serializable
data class ServiceReportResponse(
    val success: Boolean,
    val report: ServiceReportDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for attendance report operations
 */
@Serializable
data class AttendanceReportResponse(
    val success: Boolean,
    val report: AttendanceReportDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Request DTO for attendance report generation
 */
@Serializable
data class AttendanceReportRequest(
    val startDate: String, // ISO 8601 format
    val endDate: String, // ISO 8601 format
    val serviceIds: List<String>? = null, // Optional filter by specific services
    val includeDetails: Boolean = false // Whether to include detailed breakdown
)