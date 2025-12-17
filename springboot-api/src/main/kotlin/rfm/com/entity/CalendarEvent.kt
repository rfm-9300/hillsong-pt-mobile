package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "calendar_event")
data class CalendarEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "event_date", nullable = false)
    val eventDate: LocalDate,

    @Column(name = "start_time")
    val startTime: LocalTime? = null,

    @Column(name = "end_time")
    val endTime: LocalTime? = null,

    @Column(length = 255)
    val location: String? = null,

    @Column(name = "image_path", length = 500)
    val imagePath: String? = null,

    @Column(name = "is_all_day", nullable = false)
    val isAllDay: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 50, nullable = false)
    val eventType: CalendarEventType = CalendarEventType.GENERAL,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    val createdBy: UserProfile? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
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
