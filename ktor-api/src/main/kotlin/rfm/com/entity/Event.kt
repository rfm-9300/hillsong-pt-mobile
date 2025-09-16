package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "event")
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val title: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val description: String,
    
    @Column(nullable = false)
    val date: LocalDateTime,
    
    @Column(nullable = false, length = 255)
    val location: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    val organizer: UserProfile,
    
    @Column(name = "header_image_path", length = 255, nullable = false)
    val headerImagePath: String = "",
    
    @Column(name = "max_attendees", nullable = false)
    val maxAttendees: Int,
    
    @Column(name = "needs_approval", nullable = false)
    val needsApproval: Boolean = false,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_attendee",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val attendees: MutableSet<User> = mutableSetOf(),
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_waiting_list",
        joinColumns = [JoinColumn(name = "event_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val waitingListUsers: MutableSet<User> = mutableSetOf()
) {
    val attendeeCount: Int
        get() = attendees.size
    
    val isAtCapacity: Boolean
        get() = attendeeCount >= maxAttendees
    
    val availableSpots: Int
        get() = maxOf(0, maxAttendees - attendeeCount)
    
    fun addAttendee(user: User): Boolean {
        return if (!isAtCapacity && !attendees.contains(user)) {
            attendees.add(user)
            waitingListUsers.remove(user) // Remove from waiting list if they were there
            true
        } else {
            false
        }
    }
    
    fun addToWaitingList(user: User): Boolean {
        return if (!attendees.contains(user) && !waitingListUsers.contains(user)) {
            waitingListUsers.add(user)
            true
        } else {
            false
        }
    }
    
    fun removeAttendee(user: User): Boolean {
        return attendees.remove(user)
    }
    
    fun removeFromWaitingList(user: User): Boolean {
        return waitingListUsers.remove(user)
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