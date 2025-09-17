package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "service")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "name", nullable = false, length = 255)
    val name: String,
    
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    val serviceType: ServiceType,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    val dayOfWeek: DayOfWeek,
    
    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,
    
    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,
    
    @Column(name = "location", nullable = false, length = 255)
    val location: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = true)
    val leader: UserProfile? = null,
    
    @Column(name = "max_capacity")
    val maxCapacity: Int? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "requires_registration", nullable = false)
    val requiresRegistration: Boolean = false,
    
    @Column(name = "registration_deadline_hours")
    val registrationDeadlineHours: Int? = null,
    
    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "service", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val attendanceRecords: MutableSet<Attendance> = mutableSetOf(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "service_registration",
        joinColumns = [JoinColumn(name = "service_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val registeredUsers: MutableSet<User> = mutableSetOf()
) {
    val duration: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
    
    val currentAttendeeCount: Int
        get() = attendanceRecords.count { 
            it.status == AttendanceStatus.CHECKED_IN && 
            it.checkInTime.toLocalDate() == LocalDateTime.now().toLocalDate() 
        }
    
    val isAtCapacity: Boolean
        get() = maxCapacity?.let { currentAttendeeCount >= it } ?: false
    
    val availableSpots: Int?
        get() = maxCapacity?.let { maxOf(0, it - currentAttendeeCount) }
    
    fun canRegister(user: User): Boolean {
        if (!requiresRegistration) return true
        if (!isActive) return false
        if (isAtCapacity) return false
        
        registrationDeadlineHours?.let { deadlineHours ->
            val nextServiceDateTime = getNextServiceDateTime()
            val registrationDeadline = nextServiceDateTime.minusHours(deadlineHours.toLong())
            if (LocalDateTime.now().isAfter(registrationDeadline)) return false
        }
        
        return !registeredUsers.contains(user)
    }
    
    fun register(user: User): Boolean {
        return if (canRegister(user)) {
            registeredUsers.add(user)
            true
        } else {
            false
        }
    }
    
    fun unregister(user: User): Boolean {
        return registeredUsers.remove(user)
    }
    
    private fun getNextServiceDateTime(): LocalDateTime {
        val now = LocalDateTime.now()
        val today = now.dayOfWeek
        val daysUntilService = if (dayOfWeek.value >= today.value) {
            dayOfWeek.value - today.value
        } else {
            7 - (today.value - dayOfWeek.value)
        }
        
        return now.plusDays(daysUntilService.toLong())
            .withHour(startTime.hour)
            .withMinute(startTime.minute)
            .withSecond(0)
            .withNano(0)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Service
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "Service(id=$id, name='$name', serviceType=$serviceType, dayOfWeek=$dayOfWeek, startTime=$startTime)"
}

enum class ServiceType {
    SUNDAY_WORSHIP,
    BIBLE_STUDY,
    PRAYER_MEETING,
    YOUTH_SERVICE,
    SMALL_GROUP,
    SPECIAL_EVENT,
    COMMUNITY_OUTREACH,
    DISCIPLESHIP,
    WORSHIP_PRACTICE,
    LEADERSHIP_MEETING,
    OTHER
}