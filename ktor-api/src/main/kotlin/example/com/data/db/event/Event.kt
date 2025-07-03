package example.com.data.db.event

import example.com.data.db.user.*
import example.com.data.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

@Serializable
data class Event(
    val id: Int? = null,
    val title: String,
    val headerImagePath: String = "",
    val description: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime,
    val location: String,
    var attendees: List<UserProfile> = emptyList(),
    val organizerId: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    val organizerName: String = "",
    val maxAttendees: Int,
    val needsApproval: Boolean = false,
    var waitingList: List<EventWaitingList> = emptyList()
)

@Serializable
data class EventAttendee(
    val event: Int,
    val user: UserProfile,
    @Serializable(with = LocalDateTimeSerializer::class)
    val joinedAt: LocalDateTime
)

@Serializable
data class EventWaitingList(
    val eventId: Int,
    val user: UserProfile,
    @Serializable(with = LocalDateTimeSerializer::class)
    val joinedAt: LocalDateTime
)

object EventTable : IntIdTable("event")  {
    val title = varchar("title", 255)
    val description = text("description")
    val date = datetime("date")
    val location = varchar("location", 255)
    val organizerId = reference("organizer_id", UserProfilesTable)
    val headerImagePath = varchar("header_image_path", 255)
    val maxAttendees = integer("max_attendees")
    val needsApproval = bool("needs_approval").default(false)
}

object EventAttendeeTable : Table("event_attendee") {
    val eventId = reference("event_id", EventTable)
    val userId = reference("user_id", UserTable)
    val joinedAt = datetime("joined_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(eventId, userId)
}

object EventWaitingListTable : Table("event_waiting_list") {
    val eventId = reference("event_id", EventTable)
    val userId = reference("user_id", UserTable)
    val joinedAt = datetime("joined_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(eventId, userId)
}

fun ResultRow.toEvent() = Event(
    id = this[EventTable.id].value,
    title = this[EventTable.title],
    description = this[EventTable.description],
    date = this[EventTable.date],
    location = this[EventTable.location],
    headerImagePath = this[EventTable.headerImagePath],
    attendees = emptyList(),
    organizerId = this[EventTable.organizerId].value,
    organizerName = "Rodrigo",
    maxAttendees = this[EventTable.maxAttendees],
    createdAt = this[EventTable.date],
    needsApproval = this[EventTable.needsApproval]
)

fun ResultRow.toEventAttendee() = EventAttendee(
    event = this[EventAttendeeTable.eventId].value,
    user = this.toUserProfile(),
    joinedAt = this[EventAttendeeTable.joinedAt]
)

fun ResultRow.toEventWaitingList(user: UserProfile) = EventWaitingList(
    eventId = this[EventWaitingListTable.eventId].value,
    user = user,
    joinedAt = this[EventWaitingListTable.joinedAt]
)
