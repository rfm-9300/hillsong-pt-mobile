package rfm.com.data.db.attendance

import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import rfm.com.data.db.kid.KidTable
import rfm.com.data.db.user.UserTable
import rfm.com.data.utils.LocalDateTimeSerializer

@Serializable
data class Attendance(
        val id: Int? = null,
        val eventType: EventType,
        val eventId: Int, // References the specific event/service/kids_service
        val userId: Int? = null, // For adult attendees
        val kidId: Int? = null, // For kids attendees
        val checkedInBy: Int, // User who performed the check-in
        @Serializable(with = LocalDateTimeSerializer::class)
        val checkInTime: LocalDateTime = LocalDateTime.now(),
        @Serializable(with = LocalDateTimeSerializer::class)
        val checkOutTime: LocalDateTime? = null,
        val checkedOutBy: Int? = null, // User who performed the check-out
        val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,
        val notes: String = "",
        @Serializable(with = LocalDateTimeSerializer::class)
        val createdAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class AttendanceWithDetails(
        val attendance: Attendance,
        val attendeeName: String,
        val eventName: String,
        val checkedInByName: String,
        val checkedOutByName: String? = null
)

enum class EventType {
    EVENT, // General events
    SERVICE, // Regular church services
    KIDS_SERVICE // Kids services
}

enum class AttendanceStatus {
    CHECKED_IN,
    CHECKED_OUT,
    EMERGENCY,
    NO_SHOW
}

object AttendanceTable : IntIdTable("attendance") {
    val eventType = varchar("event_type", 20)
    val eventId = integer("event_id") // Generic reference to event/service/kids_service
    val userId = reference("user_id", UserTable).nullable()
    val kidId = reference("kid_id", KidTable).nullable()
    val checkedInBy = reference("checked_in_by", UserTable)
    val checkInTime = datetime("check_in_time").default(LocalDateTime.now())
    val checkOutTime = datetime("check_out_time").nullable()
    val checkedOutBy = reference("checked_out_by", UserTable).nullable()
    val status = varchar("status", 20).default(AttendanceStatus.CHECKED_IN.name)
    val notes = text("notes")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

fun ResultRow.toAttendance() =
        Attendance(
                id = this[AttendanceTable.id].value,
                eventType = EventType.valueOf(this[AttendanceTable.eventType]),
                eventId = this[AttendanceTable.eventId],
                userId = this[AttendanceTable.userId]?.value,
                kidId = this[AttendanceTable.kidId]?.value,
                checkedInBy = this[AttendanceTable.checkedInBy].value,
                checkInTime = this[AttendanceTable.checkInTime],
                checkOutTime = this[AttendanceTable.checkOutTime],
                checkedOutBy = this[AttendanceTable.checkedOutBy]?.value,
                status = AttendanceStatus.valueOf(this[AttendanceTable.status]),
                notes = this[AttendanceTable.notes],
                createdAt = this[AttendanceTable.createdAt]
        )
