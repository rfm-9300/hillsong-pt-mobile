package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Document(collection = "calendar_events")
data class CalendarEvent(
    @Id
    val id: String? = null,

    val title: String,

    val description: String? = null,

    val eventDate: LocalDate,

    val startTime: LocalTime? = null,

    val endTime: LocalTime? = null,

    val location: String? = null,

    val imagePath: String? = null,

    val isAllDay: Boolean = false,

    val eventType: CalendarEventType = CalendarEventType.GENERAL,

    val createdById: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CalendarEvent
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "CalendarEvent(id=$id, title='$title', date=$eventDate)"
}

enum class CalendarEventType {
    GENERAL,
    WORSHIP_SERVICE,
    BIBLE_STUDY,
    YOUTH_EVENT,
    KIDS_EVENT,
    PRAYER_MEETING,
    SMALL_GROUP,
    CONFERENCE,
    OUTREACH,
    SPECIAL_EVENT
}
