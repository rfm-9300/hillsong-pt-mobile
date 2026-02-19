package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "attendance")
data class Attendance(
    @Id
    val id: String? = null,

    val userId: String,

    val eventId: String? = null,

    val serviceId: String? = null,

    val kidsServiceId: String? = null,

    val attendanceType: AttendanceType,

    val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,

    @CreatedDate
    val checkInTime: LocalDateTime = LocalDateTime.now(),

    val checkOutTime: LocalDateTime? = null,

    val notes: String? = null,

    val checkedInBy: String? = null,

    val checkedOutBy: String? = null
) {
    init {
        val nonNullCount = listOfNotNull(eventId, serviceId, kidsServiceId).size
        require(nonNullCount == 1) {
            "Attendance must be associated with exactly one of: event, service, or kidsService"
        }
    }

    val isCheckedOut: Boolean
        get() = checkOutTime != null && status == AttendanceStatus.CHECKED_OUT

    val duration: Long?
        get() = if (checkOutTime != null) {
            java.time.Duration.between(checkInTime, checkOutTime).toMinutes()
        } else null

    fun checkOut(checkOutTime: LocalDateTime = LocalDateTime.now(), checkedOutBy: String? = null): Attendance {
        return this.copy(
            checkOutTime = checkOutTime,
            status = AttendanceStatus.CHECKED_OUT,
            checkedOutBy = checkedOutBy
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Attendance
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Attendance(id=$id, attendanceType=$attendanceType, status=$status, checkInTime=$checkInTime)"
}

enum class AttendanceType {
    EVENT,
    SERVICE,
    KIDS_SERVICE
}

enum class AttendanceStatus {
    CHECKED_IN,
    CHECKED_OUT,
    NO_SHOW,
    CANCELLED
}