package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Document(collection = "kids")
data class Kid(
    @Id
    val id: String? = null,

    val firstName: String,

    val lastName: String,

    val dateOfBirth: LocalDate,

    val gender: Gender? = null,

    val primaryParentId: String,

    val secondaryParentId: String? = null,

    val emergencyContactName: String? = null,

    val emergencyContactPhone: String? = null,

    val medicalNotes: String? = null,

    val allergies: String? = null,

    val specialNeeds: String? = null,

    val pickupAuthorization: String? = null,

    val isActive: Boolean = true,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null
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

    fun hasParent(userId: String): Boolean {
        return primaryParentId == userId || secondaryParentId == userId
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

@Document(collection = "kid_attendance")
data class KidAttendance(
    @Id
    val id: String? = null,

    val kidId: String,

    val kidsServiceId: String,

    @CreatedDate
    val checkInTime: LocalDateTime = LocalDateTime.now(),

    val checkOutTime: LocalDateTime? = null,

    val checkedInBy: String,

    val checkedOutBy: String? = null,

    val notes: String? = null,

    val status: AttendanceStatus = AttendanceStatus.CHECKED_IN,

    val checkInRequestId: String? = null,

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

    override fun toString(): String = "KidAttendance(id=$id, kidId=$kidId, status=$status, checkInTime=$checkInTime)"
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