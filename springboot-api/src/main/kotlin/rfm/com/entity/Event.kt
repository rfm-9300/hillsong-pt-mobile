package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "events")
data class Event(
    @Id
    val id: String? = null,

    val title: String,

    val description: String,

    val date: LocalDateTime,

    val location: String,

    val organizerId: String,

    val headerImagePath: String = "",

    val maxAttendees: Int,

    val needsApproval: Boolean = false,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val attendeeIds: MutableList<String> = mutableListOf(),

    val waitingListIds: MutableList<String> = mutableListOf()
) {
    val attendeeCount: Int
        get() = attendeeIds.size

    val isAtCapacity: Boolean
        get() = attendeeCount >= maxAttendees

    val availableSpots: Int
        get() = maxOf(0, maxAttendees - attendeeCount)

    fun addAttendee(userId: String): Boolean {
        return if (!isAtCapacity && !attendeeIds.contains(userId)) {
            attendeeIds.add(userId)
            waitingListIds.remove(userId)
            true
        } else {
            false
        }
    }

    fun addToWaitingList(userId: String): Boolean {
        return if (!attendeeIds.contains(userId) && !waitingListIds.contains(userId)) {
            waitingListIds.add(userId)
            true
        } else {
            false
        }
    }

    fun removeAttendee(userId: String): Boolean {
        return attendeeIds.remove(userId)
    }

    fun removeFromWaitingList(userId: String): Boolean {
        return waitingListIds.remove(userId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Event
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Event(id=$id, title='$title', date=$date, location='$location')"
}