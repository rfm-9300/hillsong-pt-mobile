package rfm.com.data.db.attendance

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

interface AttendanceRepository {
    
    // Check-in operations
    suspend fun checkInUser(
        eventType: EventType,
        eventId: Int,
        userId: Int,
        checkedInBy: Int,
        notes: String = ""
    ): Attendance?
    
    suspend fun checkInKid(
        eventType: EventType,
        eventId: Int,
        kidId: Int,
        checkedInBy: Int,
        notes: String = ""
    ): Attendance?
    
    // Check-out operations
    suspend fun checkOutUser(
        attendanceId: Int,
        checkedOutBy: Int,
        notes: String = ""
    ): Boolean
    
    suspend fun checkOutKid(
        attendanceId: Int,
        checkedOutBy: Int,
        notes: String = ""
    ): Boolean
    
    // Query operations
    suspend fun getAttendanceByEvent(
        eventType: EventType,
        eventId: Int
    ): List<AttendanceWithDetails>
    
    suspend fun getAttendanceByUser(
        userId: Int,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): List<AttendanceWithDetails>
    
    suspend fun getAttendanceByKid(
        kidId: Int,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): List<AttendanceWithDetails>
    
    suspend fun getCurrentlyCheckedIn(
        eventType: EventType,
        eventId: Int
    ): List<AttendanceWithDetails>
    
    suspend fun getAttendanceStats(
        eventType: EventType,
        eventId: Int
    ): AttendanceStats
    
    suspend fun isUserCheckedIn(
        eventType: EventType,
        eventId: Int,
        userId: Int
    ): Boolean
    
    suspend fun isKidCheckedIn(
        eventType: EventType,
        eventId: Int,
        kidId: Int
    ): Boolean
    
    // Update operations
    suspend fun updateAttendanceStatus(
        attendanceId: Int,
        status: AttendanceStatus,
        notes: String = ""
    ): Boolean
    
    suspend fun updateAttendanceNotes(
        attendanceId: Int,
        notes: String
    ): Boolean
}

@Serializable
data class AttendanceStats(
    val totalAttendees: Int,
    val currentlyCheckedIn: Int,
    val checkedOut: Int,
    val noShows: Int,
    val emergencies: Int
)