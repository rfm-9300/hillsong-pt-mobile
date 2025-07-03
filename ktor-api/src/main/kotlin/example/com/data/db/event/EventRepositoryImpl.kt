package example.com.data.db.event

import example.com.data.db.user.*
import example.com.plugins.Logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class EventRepositoryImpl: EventRepository {
    override suspend fun addEvent(event: Event): Int? = suspendTransaction {
        val organizer = UserProfileDao.findById(event.organizerId) ?: return@suspendTransaction null

        try {
            val eventId = EventTable.insertAndGetId {
                it[title] = event.title
                it[description] = event.description
                it[date] = event.date
                it[location] = event.location
                it[organizerId] = organizer.id.value
                it[headerImagePath] = event.headerImagePath
                it[maxAttendees] = event.maxAttendees
                it[needsApproval] = event.needsApproval
            }.value
            eventId // Return the generated event ID
        } catch (e: Exception) {
            Logger.d("Detailed event creation error: ${e.message}")
            e.printStackTrace() // Add this to get full stack trace
            null
        }
    }

    override suspend fun getAllEvents(): List<Event> = suspendTransaction {
        val eventsList = mutableListOf<Event>()
        Logger.d("List: $eventsList")
        val events = EventTable.selectAll().map {
            Event(
                id = it[EventTable.id].value,
                title = it[EventTable.title],
                description = it[EventTable.description],
                date = it[EventTable.date],
                location = it[EventTable.location],
                headerImagePath = it[EventTable.headerImagePath],
                attendees = emptyList(),
                organizerId = it[EventTable.organizerId].value,
                organizerName = "Rodrigo",
                maxAttendees = it[EventTable.maxAttendees]
            )
        }
        Logger.d("Events: $events")
        events.forEach { event ->
            // Fetch attendees for each event
            val attendeesQuery = EventAttendeeTable.select { EventAttendeeTable.eventId eq event.id }
            Logger.d("Attendees Query: $attendeesQuery")
            attendeesQuery.forEach { attendeeRow ->
                val userId = attendeeRow[EventAttendeeTable.userId].value
                val userProfile = UserProfilesTable.select { UserProfilesTable.userId eq userId }
                    .firstOrNull()?.toUserProfile()
                Logger.d("User: $userProfile")
                userProfile?.let { user ->
                    // Reassign the list with the new user added using the + operator
                    event.attendees = event.attendees + user
                }
            }
            eventsList.add(event)
            Logger.d("Event: $event")
        }

        eventsList
    }

    override suspend fun getEvent(eventId: Int): Event? = suspendTransaction {
        val event = EventTable.select { EventTable.id eq eventId }.firstOrNull()?.toEvent()
        val attendeesQuery = EventAttendeeTable.select { EventAttendeeTable.eventId eq eventId }
        val waitingListQuery = EventWaitingListTable.select { EventWaitingListTable.eventId eq eventId }

        attendeesQuery.forEach { attendeeRow ->
            val userId = attendeeRow[EventAttendeeTable.userId].value
            val userProfile = UserProfilesTable.select { UserProfilesTable.userId eq userId }.firstOrNull()?.toUserProfile()
            userProfile?.let { user -> if (event != null) { event.attendees += user } }
        }

        waitingListQuery.forEach { waitingListRow ->
            val userId = waitingListRow[EventWaitingListTable.userId].value
            val userProfile = UserProfilesTable.select { UserProfilesTable.userId eq userId }.firstOrNull()?.toUserProfile()
            userProfile?.let { user -> if (event != null) {
                val waitingList = waitingListRow.toEventWaitingList(user)
                event.waitingList += waitingList
            } }
        }

        event
    }

    override suspend fun deleteEvent(eventId: Int): Boolean = suspendTransaction {
        EventTable.deleteWhere { id eq eventId } > 0
    }

    override suspend fun updateEvent(event: Event): Boolean = suspendTransaction {
        try {
            EventTable.update({ EventTable.id eq event.id }) {
                it[title] = event.title
                it[description] = event.description
                it[date] = event.date
                it[location] = event.location
                it[headerImagePath] = event.headerImagePath
            } > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun joinEvent(eventId: Int, userId: Int): Boolean = suspendTransaction {
        try {
            EventAttendeeTable.insert {
                it[this.eventId] = eventId
                it[this.userId] = userId
                it[joinedAt] = LocalDateTime.now()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getEventAttendees(eventId: Int): List<UserProfile> = suspendTransaction {
        val attendees = mutableListOf<UserProfile>()
        try {
            Logger.d("Event ID: $eventId")
            val attendeesQuery = EventAttendeeTable.select { EventAttendeeTable.eventId eq eventId }
            Logger.d("Attendees Query: $attendeesQuery")
            attendeesQuery.forEach { attendeeRow ->
                val userId = attendeeRow[EventAttendeeTable.userId].value
                val userProfile = UserProfilesTable.select { UserProfilesTable.userId eq userId }
                    .firstOrNull()?.toUserProfile()
                userProfile?.let { user ->
                    attendees.add(user)
                }
            }
        } catch (e: Exception) {
            Logger.d("Error getting event attendees: ${e}")
        }
        attendees
    }

    override suspend fun deleteEventAttendees(eventId: Int): Int = suspendTransaction {
        EventAttendeeTable.deleteWhere { EventAttendeeTable.eventId eq eventId }
    }

    override suspend fun getUpcomingEvents(): List<Event> {
        val upcomingEvents = getAllEvents().filter { event ->
            event.date.isAfter(LocalDateTime.now())
        }
        return upcomingEvents
    }

    override suspend fun joinEventWaitingList(eventId: Int, userId: Int): Boolean = suspendTransaction {
        try {
            EventWaitingListTable.insert {
                it[this.eventId] = eventId
                it[this.userId] = userId
                it[joinedAt] = LocalDateTime.now()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun approveUser(eventId: Int, userId: Int): Boolean = suspendTransaction {
        // First, check if the user is in the waiting list
        val waitingListEntry = EventWaitingListTable.select {
            (EventWaitingListTable.eventId eq eventId) and (EventWaitingListTable.userId eq userId)
        }.firstOrNull() ?: return@suspendTransaction false

        // Delete from waiting list
        EventWaitingListTable.deleteWhere {
            (EventWaitingListTable.eventId eq eventId) and (EventWaitingListTable.userId eq userId)
        }

        // Add to attendees
        EventAttendeeTable.insert {
            it[EventAttendeeTable.eventId] = eventId
            it[EventAttendeeTable.userId] = userId
            it[joinedAt] = LocalDateTime.now()
        }

        true
    }

    override suspend fun removeUserFromEvent(eventId: Int, userId: Int): Boolean = suspendTransaction {
        try {
            // Delete the user from attendees
            val deletedRows = EventAttendeeTable.deleteWhere {
                (EventAttendeeTable.eventId eq eventId) and (EventAttendeeTable.userId eq userId)
            }
            
            // Return true if at least one row was deleted
            deletedRows > 0
        } catch (e: Exception) {
            Logger.d("Error removing user from event: ${e.message}")
            false
        }
    }
}