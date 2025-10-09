package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Entity
@Table(name = "kid")
data class Kid(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "first_name", nullable = false, length = 100)
    val firstName: String,
    
    @Column(name = "last_name", nullable = false, length = 100)
    val lastName: String,
    
    @Column(name = "date_of_birth", nullable = false)
    val dateOfBirth: LocalDate,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    val gender: Gender? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_parent_id", nullable = false)
    val primaryParent: UserProfile,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_parent_id", nullable = true)
    val secondaryParent: UserProfile? = null,
    
    @Column(name = "emergency_contact_name", length = 255)
    val emergencyContactName: String? = null,
    
    @Column(name = "emergency_contact_phone", length = 20)
    val emergencyContactPhone: String? = null,
    
    @Column(name = "medical_notes", columnDefinition = "TEXT")
    val medicalNotes: String? = null,
    
    @Column(name = "allergies", columnDefinition = "TEXT")
    val allergies: String? = null,
    
    @Column(name = "special_needs", columnDefinition = "TEXT")
    val specialNeeds: String? = null,
    
    @Column(name = "pickup_authorization", columnDefinition = "TEXT")
    val pickupAuthorization: String? = null,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "kid", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val attendanceRecords: MutableSet<KidAttendance> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "enrolledKids", fetch = FetchType.LAZY)
    val kidsServices: MutableSet<KidsService> = mutableSetOf()
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    val age: Int
        get() = Period.between(dateOfBirth, LocalDate.now()).years
    
    val ageGroup: AgeGroup
        get() = when (age) {
            in 0..2 -> AgeGroup.NURSERY
            in 3..5 -> AgeGroup.PRESCHOOL
            in 6..8 -> AgeGroup.ELEMENTARY_LOWER
            in 9..11 -> AgeGroup.ELEMENTARY_UPPER
            in 12..14 -> AgeGroup.MIDDLE_SCHOOL
            in 15..17 -> AgeGroup.HIGH_SCHOOL
            else -> AgeGroup.ADULT
        }
    
    fun isEligibleForService(kidsService: KidsService): Boolean {
        return true
    }
    
    fun hasParent(userProfile: UserProfile): Boolean {
        return primaryParent.id == userProfile.id || secondaryParent?.id == userProfile.id
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Kid
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "Kid(id=$id, fullName='$fullName', age=$age, ageGroup=$ageGroup)"
}

@Entity
@Table(name = "kid_attendance")
data class KidAttendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kid_id", nullable = false)
    val kid: Kid,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kids_service_id", nullable = false)
    val kidsService: KidsService,
    
    @CreationTimestamp
    @Column(name = "check_in_time", nullable = false)
    val checkInTime: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "check_out_time")
    val checkOutTime: LocalDateTime? = null,
    
    @Column(name = "checked_in_by", nullable = false, length = 255)
    val checkedInBy: String,
    
    @Column(name = "checked_out_by", length = 255)
    val checkedOutBy: String? = null,
    
    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_request_id")
    val checkInRequest: CheckInRequest? = null,
    
    @Column(name = "approved_by_staff", length = 255)
    val approvedByStaff: String? = null
) {
    val isCheckedOut: Boolean
        get() = checkOutTime != null && status == AttendanceStatus.CHECKED_OUT
    
    val duration: Long?
        get() = if (checkOutTime != null) {
            java.time.Duration.between(checkInTime, checkOutTime).toMinutes()
        } else null
    
    fun checkOut(checkOutTime: LocalDateTime = LocalDateTime.now(), checkedOutBy: String): KidAttendance {
        return this.copy(
            checkOutTime = checkOutTime,
            status = AttendanceStatus.CHECKED_OUT,
            checkedOutBy = checkedOutBy
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KidAttendance
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "KidAttendance(id=$id, kid=${kid.fullName}, status=$status, checkInTime=$checkInTime)"
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

enum class AgeGroup {
    NURSERY,        // 0-2 years
    PRESCHOOL,      // 3-5 years
    ELEMENTARY_LOWER, // 6-8 years
    ELEMENTARY_UPPER, // 9-11 years
    MIDDLE_SCHOOL,   // 12-14 years
    HIGH_SCHOOL,     // 15-17 years
    ADULT           // 18+ years
}