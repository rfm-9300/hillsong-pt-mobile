package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.AttendanceType
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Request DTO for checking in to an event, service, or kids service
 */
data class CheckInRequest(
    @field:NotNull(message = "Attendance type is required")
    val attendanceType: AttendanceType,
    
    val eventId: Long? = null,
    val serviceId: Long? = null,
    val kidsServiceId: Long? = null,
    
    val notes: String? = null,
    val checkedInBy: String? = null
) {
    init {
        val nonNullCount = listOfNotNull(eventId, serviceId, kidsServiceId).size
        require(nonNullCount == 1) { 
            "Exactly one of eventId, serviceId, or kidsServiceId must be provided" 
        }
        
        when (attendanceType) {
            AttendanceType.EVENT -> require(eventId != null) { "eventId is required for EVENT attendance type" }
            AttendanceType.SERVICE -> require(serviceId != null) { "serviceId is required for SERVICE attendance type" }
            AttendanceType.KIDS_SERVICE -> require(kidsServiceId != null) { "kidsServiceId is required for KIDS_SERVICE attendance type" }
        }
    }
}

/**
 * Request DTO for checking out from an attendance record
 */
data class CheckOutRequest(
    @field:NotNull(message = "Attendance ID is required")
    val attendanceId: Long,
    
    val notes: String? = null,
    val checkedOutBy: String? = null
)

/**
 * Response DTO for attendance records
 */
data class AttendanceResponse(
    val id: Long,
    val user: UserResponse,
    val attendanceType: AttendanceType,
    val status: AttendanceStatus,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkOutTime: LocalDateTime? = null,
    
    val notes: String? = null,
    val checkedInBy: String? = null,
    val checkedOutBy: String? = null,
    val duration: Long? = null,
    val isCheckedOut: Boolean,
    
    // Related entity information
    val event: EventSummaryResponse? = null,
    val service: ServiceResponse? = null,
    val kidsService: KidsServiceResponse? = null
)

/**
 * Simplified attendance response for list views
 */
data class AttendanceSummaryResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val attendanceType: AttendanceType,
    val status: AttendanceStatus,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkInTime: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val checkOutTime: LocalDateTime? = null,
    
    val duration: Long? = null,
    val isCheckedOut: Boolean,
    
    val entityName: String, // Name of the event, service, or kids service
    val entityId: Long
)

/**
 * Request DTO for attendance reporting and analytics
 */
data class AttendanceReportRequest(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime,
    
    val attendanceType: AttendanceType? = null,
    val status: AttendanceStatus? = null,
    val eventId: Long? = null,
    val serviceId: Long? = null,
    val kidsServiceId: Long? = null,
    val userId: Long? = null
) {
    init {
        require(startDate.isBefore(endDate)) { "Start date must be before end date" }
    }
}

/**
 * Response DTO for attendance statistics
 */
data class AttendanceStatsResponse(
    val totalAttendance: Long,
    val checkedInCount: Long,
    val checkedOutCount: Long,
    val noShowCount: Long,
    val cancelledCount: Long,
    val averageDuration: Double? = null,
    val attendanceByType: Map<AttendanceType, Long>,
    val attendanceByStatus: Map<AttendanceStatus, Long>,
    val dateRange: DateRangeResponse
)

/**
 * Response DTO for date range information
 */
data class DateRangeResponse(
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val startDate: LocalDateTime,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val endDate: LocalDateTime
)

/**
 * Response DTO for most frequent attendees
 */
data class FrequentAttendeesResponse(
    val user: UserResponse,
    val attendanceCount: Long,
    val lastAttendance: LocalDateTime? = null
)

/**
 * Response DTO for service information (simplified)
 */
data class ServiceResponse(
    val id: Long,
    val name: String,
    val serviceType: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val leaderName: String? = null,
    val maxCapacity: Int? = null,
    val isActive: Boolean
)

/**
 * Response DTO for kids service information (simplified)
 */
data class KidsServiceResponse(
    val id: Long,
    val name: String,
    val dayOfWeek: String,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val serviceDate: LocalDate,
    
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

/**
 * Request DTO for bulk check-in operations
 */
data class BulkCheckInRequest(
    @field:NotEmpty(message = "User IDs list cannot be empty")
    val userIds: List<Long>,
    
    @field:NotNull(message = "Attendance type is required")
    val attendanceType: AttendanceType,
    
    val eventId: Long? = null,
    val serviceId: Long? = null,
    val kidsServiceId: Long? = null,
    
    val notes: String? = null,
    val checkedInBy: String? = null
) {
    init {
        val nonNullCount = listOfNotNull(eventId, serviceId, kidsServiceId).size
        require(nonNullCount == 1) { 
            "Exactly one of eventId, serviceId, or kidsServiceId must be provided" 
        }
        
        when (attendanceType) {
            AttendanceType.EVENT -> require(eventId != null) { "eventId is required for EVENT attendance type" }
            AttendanceType.SERVICE -> require(serviceId != null) { "serviceId is required for SERVICE attendance type" }
            AttendanceType.KIDS_SERVICE -> require(kidsServiceId != null) { "kidsServiceId is required for KIDS_SERVICE attendance type" }
        }
    }
}

/**
 * Response DTO for bulk operations
 */
data class BulkAttendanceResponse(
    val successful: List<AttendanceResponse>,
    val failed: List<BulkAttendanceError>,
    val totalProcessed: Int,
    val successCount: Int,
    val failureCount: Int
)

/**
 * Error information for bulk operations
 */
data class BulkAttendanceError(
    val userId: Long,
    val error: String
)