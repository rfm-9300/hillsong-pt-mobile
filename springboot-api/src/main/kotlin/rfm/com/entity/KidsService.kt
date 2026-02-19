package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Document(collection = "kids_services")
data class KidsService(
    @Id
    val id: String? = null,

    val name: String,

    val description: String? = null,

    val dayOfWeek: DayOfWeek,

    val serviceDate: LocalDate,

    val startTime: LocalTime,

    val endTime: LocalTime,

    val location: String,

    val leaderId: String? = null,

    val maxCapacity: Int,

    val minAge: Int,

    val maxAge: Int,

    val ageGroups: MutableSet<AgeGroup> = mutableSetOf(),

    val isActive: Boolean = true,

    val requiresPreRegistration: Boolean = false,

    val checkInStartsMinutesBefore: Int = 30,

    val checkInEndsMinutesAfter: Int = 15,

    val volunteerToChildRatio: String? = null,

    val specialRequirements: String? = null,

    val notes: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null,

    val enrolledKidIds: MutableList<String> = mutableListOf(),

    val volunteerIds: MutableList<String> = mutableListOf()
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

    val enrolledCount: Int
        get() = enrolledKidIds.size

    val isAtCapacity: Boolean
        get() = enrolledCount >= maxCapacity

    val availableSpots: Int
        get() = maxOf(0, maxCapacity - enrolledCount)

    val requiredVolunteers: Int?
        get() = volunteerToChildRatio?.let { ratio ->
            val parts = ratio.split(":")
            if (parts.size == 2) {
                val volunteerCount = parts[0].toIntOrNull() ?: 1
                val childCount = parts[1].toIntOrNull() ?: 1
                kotlin.math.ceil(enrolledCount.toDouble() / childCount * volunteerCount).toInt()
            } else null
        }

    val currentVolunteerCount: Int
        get() = volunteerIds.size

    val hasAdequateVolunteers: Boolean
        get() = requiredVolunteers?.let { required -> currentVolunteerCount >= required } ?: true

    fun canEnroll(kidId: String): Boolean {
        if (!isActive) return false
        if (isAtCapacity) return false
        if (enrolledKidIds.contains(kidId)) return false
        return true
    }

    fun enroll(kidId: String): Boolean {
        return if (canEnroll(kidId)) {
            enrolledKidIds.add(kidId)
            true
        } else {
            false
        }
    }

    fun unenroll(kidId: String): Boolean {
        return enrolledKidIds.remove(kidId)
    }

    fun addVolunteer(userId: String): Boolean {
        return if (!volunteerIds.contains(userId)) {
            volunteerIds.add(userId)
            true
        } else {
            false
        }
    }

    fun removeVolunteer(userId: String): Boolean {
        return volunteerIds.remove(userId)
    }

    fun isCheckInOpen(): Boolean {
        //val now = LocalDateTime.now()
        //val serviceDateTime = LocalDateTime.of(serviceDate, startTime)
        //val checkInStart = serviceDateTime.minusMinutes(checkInStartsMinutesBefore.toLong())
        //val checkInEnd = serviceDateTime.plusMinutes(checkInEndsMinutesAfter.toLong())
        //return now.isAfter(checkInStart) && now.isBefore(checkInEnd)
        return true
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