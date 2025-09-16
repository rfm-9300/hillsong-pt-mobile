package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "attendance")
data class Attendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = true)
    val event: Event? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = true)
    val service: Service? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kids_service_id", nullable = true)
    val kidsService: KidsService? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_type", nullable = false, length = 20)
    val attendanceType: AttendanceType,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,
    
    @CreationTimestamp
    @Column(name = "check_in_time", nullable = false)
    val checkInTime: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "check_out_time")
    val checkOutTime: LocalDateTime? = null,
    
    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Column(name = "checked_in_by", length = 255)
    val checkedInBy: String? = null,
    
    @Column(name = "checked_out_by", length = 255)
    val checkedOutBy: String? = null
) {
    init {
        // Ensure exactly one of event, service, or kidsService is set
        val nonNullCount = listOfNotNull(event, service, kidsService).size
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