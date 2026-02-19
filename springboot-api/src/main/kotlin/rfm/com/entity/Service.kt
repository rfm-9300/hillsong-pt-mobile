package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Document(collection = "services")
data class Service(
    @Id
    val id: String? = null,

    val name: String,

    val description: String? = null,

    val serviceType: ServiceType,

    val dayOfWeek: DayOfWeek,

    val startTime: LocalTime,

    val endTime: LocalTime,

    val location: String,

    val leaderId: String? = null,

    val maxCapacity: Int? = null,

    val isActive: Boolean = true,

    val requiresRegistration: Boolean = false,

    val registrationDeadlineHours: Int? = null,

    val notes: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null,

    val registeredUserIds: MutableList<String> = mutableListOf()
) {
    val duration: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()

    val isAtCapacity: Boolean
        get() = maxCapacity?.let { registeredUserIds.size >= it } ?: false

    val availableSpots: Int?
        get() = maxCapacity?.let { maxOf(0, it - registeredUserIds.size) }

    fun canRegister(userId: String): Boolean {
        if (!requiresRegistration) return true
        if (!isActive) return false
        if (isAtCapacity) return false

        registrationDeadlineHours?.let { deadlineHours ->
            val nextServiceDateTime = getNextServiceDateTime()
            val registrationDeadline = nextServiceDateTime.minusHours(deadlineHours.toLong())
            if (LocalDateTime.now().isAfter(registrationDeadline)) return false
        }

        return !registeredUserIds.contains(userId)
    }

    fun register(userId: String): Boolean {
        return if (canRegister(userId)) {
            registeredUserIds.add(userId)
            true
        } else {
            false
        }
    }

    fun unregister(userId: String): Boolean {
        return registeredUserIds.remove(userId)
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