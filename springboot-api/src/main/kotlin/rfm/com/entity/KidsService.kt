package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "kids_service")
data class KidsService(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    val dayOfWeek: DayOfWeek,

    @Column(name = "service_date", nullable = false)
    val serviceDate: LocalDate,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,

    @Column(name = "location", nullable = false, length = 255)
    val location: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = true)
    val leader: UserProfile? = null,

    @Column(name = "max_capacity", nullable = false)
    val maxCapacity: Int,

    @Column(name = "min_age", nullable = false)
    val minAge: Int,

    @Column(name = "max_age", nullable = false)
    val maxAge: Int,

    @ElementCollection(targetClass = AgeGroup::class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "kids_service_age_groups",
        joinColumns = [JoinColumn(name = "kids_service_id")]
    )
    @Column(name = "age_group", length = 20)
    val ageGroups: MutableSet<AgeGroup> = mutableSetOf(),

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "requires_pre_registration", nullable = false)
    val requiresPreRegistration: Boolean = false,

    @Column(name = "check_in_starts_minutes_before")
    val checkInStartsMinutesBefore: Int = 30,

    @Column(name = "check_in_ends_minutes_after")
    val checkInEndsMinutesAfter: Int = 15,

    @Column(name = "volunteer_to_child_ratio")
    val volunteerToChildRatio: String? = null, // e.g., "1:5" for 1 volunteer per 5 children

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    val specialRequirements: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "kidsService", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val attendanceRecords: MutableSet<Attendance> = mutableSetOf(),

    @OneToMany(mappedBy = "kidsService", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val kidAttendanceRecords: MutableSet<KidAttendance> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "kids_service_enrollment",
        joinColumns = [JoinColumn(name = "kids_service_id")],
        inverseJoinColumns = [JoinColumn(name = "kid_id")]
    )
    val enrolledKids: MutableSet<Kid> = mutableSetOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "kids_service_volunteers",
        joinColumns = [JoinColumn(name = "kids_service_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val volunteers: MutableSet<User> = mutableSetOf()
) {
    init {
        require(minAge >= 0) { "Minimum age must be non-negative" }
        require(maxAge >= minAge) { "Maximum age must be greater than or equal to minimum age" }
        require(maxCapacity > 0) { "Maximum capacity must be positive" }
        require(checkInStartsMinutesBefore >= 0) { "Check-in start time must be non-negative" }
        require(checkInEndsMinutesAfter >= 0) { "Check-in end time must be non-negative" }
    }
    
    val duration: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
    
    val currentAttendeeCount: Int
        get() = kidAttendanceRecords.count { 
            it.status == AttendanceStatus.CHECKED_IN && 
            it.checkInTime.toLocalDate() == LocalDateTime.now().toLocalDate() 
        }
    
    val isAtCapacity: Boolean
        get() = currentAttendeeCount >= maxCapacity
    
    val availableSpots: Int
        get() = maxOf(0, maxCapacity - currentAttendeeCount)
    
    val requiredVolunteers: Int?
        get() = volunteerToChildRatio?.let { ratio ->
            val parts = ratio.split(":")
            if (parts.size == 2) {
                val volunteerCount = parts[0].toIntOrNull() ?: 1
                val childCount = parts[1].toIntOrNull() ?: 1
                kotlin.math.ceil(currentAttendeeCount.toDouble() / childCount * volunteerCount).toInt()
            } else null
        }
    
    val currentVolunteerCount: Int
        get() = volunteers.size
    
    val hasAdequateVolunteers: Boolean
        get() = requiredVolunteers?.let { required -> currentVolunteerCount >= required } ?: true
    
    fun canEnroll(kid: Kid): Boolean {
        if (!isActive) return false
        if (isAtCapacity) return false
        if (enrolledKids.contains(kid)) return false
        
        return kid.isEligibleForService(this)
    }
    
    fun enroll(kid: Kid): Boolean {
        return if (canEnroll(kid)) {
            enrolledKids.add(kid)
            true
        } else {
            false
        }
    }
    
    fun unenroll(kid: Kid): Boolean {
        return enrolledKids.remove(kid)
    }
    
    fun addVolunteer(user: User): Boolean {
        return if (!volunteers.contains(user)) {
            volunteers.add(user)
            true
        } else {
            false
        }
    }
    
    fun removeVolunteer(user: User): Boolean {
        return volunteers.remove(user)
    }
    
    fun isCheckInOpen(): Boolean {
        val now = LocalDateTime.now()
        val serviceDateTime = getNextServiceDateTime()
        val checkInStart = serviceDateTime.minusMinutes(checkInStartsMinutesBefore.toLong())
        val checkInEnd = serviceDateTime.plusMinutes(checkInEndsMinutesAfter.toLong())
        
        return now.isAfter(checkInStart) && now.isBefore(checkInEnd)
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
        other as KidsService
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "KidsService(id=$id, name='$name', ageRange=$minAge-$maxAge, capacity=$maxCapacity, dayOfWeek=$dayOfWeek)"
}