package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Event
import rfm.com.entity.User
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.EventRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

/**
 * Service for managing events
 */
@Service
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(EventService::class.java)
    
    /**
     * Create a new event
     */
    suspend fun createEvent(
        createEventRequest: CreateEventRequest,
        organizerId: String,
        headerImage: MultipartFile? = null
    ): EventResponse = withContext(Dispatchers.IO) {
        logger.info("Creating event: ${createEventRequest.title} for organizer: $organizerId")
        
        userRepository.findById(organizerId).orElse(null)
            ?: throw EntityNotFoundException("User", organizerId)
        
        val headerImagePath = headerImage?.let { image ->
            try {
                fileStorageService.storeEventImage(image)
            } catch (ex: Exception) {
                logger.error("Failed to store event image", ex)
                throw RuntimeException("Failed to upload event image: ${ex.message}")
            }
        } ?: ""
        
        val event = Event(
            title = createEventRequest.title,
            description = createEventRequest.description,
            date = createEventRequest.date,
            location = createEventRequest.location,
            organizerId = organizerId,
            headerImagePath = headerImagePath,
            maxAttendees = createEventRequest.maxAttendees,
            needsApproval = createEventRequest.needsApproval
        )
        
        val savedEvent = eventRepository.save(event)
        logger.info("Event created successfully with id: ${savedEvent.id}")
        
        mapToEventResponse(savedEvent)
    }
    
    /**
     * Get all events with pagination
     */
    suspend fun getAllEvents(pageable: Pageable): Page<EventSummaryResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching all events with pagination")
        try {
            val events = eventRepository.findAll(pageable)
            logger.debug("Found ${events.totalElements} events")
            events.map { event ->
                logger.debug("Mapping event: ${event.id} - ${event.title}")
                mapToEventSummaryResponse(event)
            }
        } catch (e: Exception) {
            logger.error("Error fetching events", e)
            throw e
        }
    }
    
    /**
     * Get all upcoming events
     */
    suspend fun getUpcomingEvents(): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching upcoming events")
        val now = LocalDateTime.now()
        eventRepository.findUpcomingEvents(now)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Get event by ID with full details
     */
    suspend fun getEventById(eventId: String, includeAttendees: Boolean = false): EventResponse = withContext(Dispatchers.IO) {
        logger.debug("Fetching event by id: $eventId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        mapToEventResponse(event, includeAttendees)
    }
    
    /**
     * Update an existing event
     */
    suspend fun updateEvent(
        eventId: String,
        updateRequest: UpdateEventRequest,
        userId: String,
        headerImage: MultipartFile? = null
    ): EventResponse = withContext(Dispatchers.IO) {
        logger.info("Updating event: $eventId by user: $userId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        if (event.organizerId != userId) {
            throw AccessDeniedException("Only the event organizer can update this event")
        }
        
        val newImagePath = headerImage?.let { image ->
            try {
                if (event.headerImagePath.isNotBlank()) {
                    fileStorageService.deleteFile(event.headerImagePath)
                }
                fileStorageService.storeEventImage(image)
            } catch (ex: Exception) {
                logger.error("Failed to store updated event image", ex)
                throw RuntimeException("Failed to upload event image: ${ex.message}")
            }
        }
        
        val updatedEvent = event.copy(
            title = updateRequest.title ?: event.title,
            description = updateRequest.description ?: event.description,
            date = updateRequest.date ?: event.date,
            location = updateRequest.location ?: event.location,
            maxAttendees = updateRequest.maxAttendees ?: event.maxAttendees,
            needsApproval = updateRequest.needsApproval ?: event.needsApproval,
            headerImagePath = newImagePath ?: event.headerImagePath
        )
        
        val savedEvent = eventRepository.save(updatedEvent)
        logger.info("Event updated successfully: ${savedEvent.id}")
        
        mapToEventResponse(savedEvent)
    }
    
    /**
     * Delete an event
     */
    suspend fun deleteEvent(eventId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        logger.info("Deleting event: $eventId by user: $userId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        if (event.organizerId != userId) {
            throw AccessDeniedException("Only the event organizer can delete this event")
        }
        
        if (event.headerImagePath.isNotBlank()) {
            fileStorageService.deleteFile(event.headerImagePath)
        }
        
        eventRepository.delete(event)
        logger.info("Event deleted successfully: $eventId")
        true
    }
    
    /**
     * Join an event
     */
    suspend fun joinEvent(eventId: String, userId: String): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("User $userId attempting to join event $eventId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        userRepository.findById(userId).orElse(null)
            ?: throw EntityNotFoundException("User", userId)
        
        // Check if user is already an attendee
        if (event.attendeeIds.contains(userId)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is already attending this event",
                eventId = eventId,
                userId = userId,
                currentStatus = EventUserStatus.ATTENDEE
            )
        }
        
        // Check if user is already on waiting list
        if (event.waitingListIds.contains(userId)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is already on the waiting list for this event",
                eventId = eventId,
                userId = userId,
                currentStatus = EventUserStatus.WAITING_LIST
            )
        }
        
        val result = when {
            event.needsApproval -> {
                event.addToWaitingList(userId)
                eventRepository.save(event)
                EventActionResponse(
                    success = true,
                    message = "Added to waiting list. Awaiting organizer approval.",
                    eventId = eventId,
                    userId = userId,
                    currentStatus = EventUserStatus.PENDING_APPROVAL
                )
            }
            event.isAtCapacity -> {
                event.addToWaitingList(userId)
                eventRepository.save(event)
                EventActionResponse(
                    success = true,
                    message = "Event is at capacity. Added to waiting list.",
                    eventId = eventId,
                    userId = userId,
                    currentStatus = EventUserStatus.WAITING_LIST
                )
            }
            else -> {
                event.addAttendee(userId)
                eventRepository.save(event)
                EventActionResponse(
                    success = true,
                    message = "Successfully joined the event",
                    eventId = eventId,
                    userId = userId,
                    currentStatus = EventUserStatus.ATTENDEE
                )
            }
        }
        
        logger.info("User $userId join result for event $eventId: ${result.message}")
        result
    }
    
    /**
     * Leave an event
     */
    suspend fun leaveEvent(eventId: String, userId: String): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("User $userId attempting to leave event $eventId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        userRepository.findById(userId).orElse(null)
            ?: throw EntityNotFoundException("User", userId)
        
        val wasAttendee = event.removeAttendee(userId)
        val wasOnWaitingList = event.removeFromWaitingList(userId)
        
        if (!wasAttendee && !wasOnWaitingList) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is not associated with this event",
                eventId = eventId,
                userId = userId,
                currentStatus = EventUserStatus.NOT_JOINED
            )
        }
        
        // If someone left and there's space, promote from waiting list
        if (wasAttendee && !event.isAtCapacity && event.waitingListIds.isNotEmpty()) {
            val nextUserId = event.waitingListIds.first()
            event.removeFromWaitingList(nextUserId)
            event.addAttendee(nextUserId)
            logger.info("Promoted user $nextUserId from waiting list to attendee for event $eventId")
        }
        
        eventRepository.save(event)
        
        val message = if (wasAttendee) "Successfully left the event" else "Removed from waiting list"
        logger.info("User $userId left event $eventId: $message")
        
        EventActionResponse(
            success = true,
            message = message,
            eventId = eventId,
            userId = userId,
            currentStatus = EventUserStatus.NOT_JOINED
        )
    }
    
    /**
     * Approve a user for an event (organizer only)
     */
    suspend fun approveUserForEvent(eventId: String, userIdToApprove: String, organizerId: String): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("Organizer $organizerId approving user $userIdToApprove for event $eventId")
        
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        if (event.organizerId != organizerId) {
            throw AccessDeniedException("Only the event organizer can approve users")
        }
        
        userRepository.findById(userIdToApprove).orElse(null)
            ?: throw EntityNotFoundException("User", userIdToApprove)
        
        if (!event.waitingListIds.contains(userIdToApprove)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is not on the waiting list for this event",
                eventId = eventId,
                userId = userIdToApprove,
                currentStatus = if (event.attendeeIds.contains(userIdToApprove)) EventUserStatus.ATTENDEE else EventUserStatus.NOT_JOINED
            )
        }
        
        if (event.isAtCapacity) {
            return@withContext EventActionResponse(
                success = false,
                message = "Event is at capacity. Cannot approve more users.",
                eventId = eventId,
                userId = userIdToApprove,
                currentStatus = EventUserStatus.WAITING_LIST
            )
        }
        
        event.removeFromWaitingList(userIdToApprove)
        event.addAttendee(userIdToApprove)
        eventRepository.save(event)
        
        logger.info("User $userIdToApprove approved for event $eventId by organizer $organizerId")
        
        EventActionResponse(
            success = true,
            message = "User approved and added to event",
            eventId = eventId,
            userId = userIdToApprove,
            currentStatus = EventUserStatus.ATTENDEE
        )
    }
    
    /**
     * Get user's status for a specific event
     */
    suspend fun getUserEventStatus(eventId: String, userId: String): UserEventStatusResponse = withContext(Dispatchers.IO) {
        val event = eventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("Event", eventId)
        
        userRepository.findById(userId).orElse(null)
            ?: throw EntityNotFoundException("User", userId)
        
        val status = when {
            event.attendeeIds.contains(userId) -> EventUserStatus.ATTENDEE
            event.waitingListIds.contains(userId) -> {
                if (event.needsApproval) EventUserStatus.PENDING_APPROVAL else EventUserStatus.WAITING_LIST
            }
            else -> EventUserStatus.NOT_JOINED
        }
        
        val canJoin = status == EventUserStatus.NOT_JOINED
        val canLeave = status in listOf(EventUserStatus.ATTENDEE, EventUserStatus.WAITING_LIST, EventUserStatus.PENDING_APPROVAL)
        
        UserEventStatusResponse(
            eventId = eventId,
            userId = userId,
            status = status,
            canJoin = canJoin,
            canLeave = canLeave
        )
    }
    
    /**
     * Get events organized by a user
     */
    suspend fun getEventsByOrganizer(organizerId: String, pageable: Pageable): Page<EventSummaryResponse> = withContext(Dispatchers.IO) {
        userRepository.findById(organizerId).orElse(null)
            ?: throw EntityNotFoundException("User", organizerId)
        
        eventRepository.findByOrganizerId(organizerId, pageable)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Get events a user is attending
     */
    suspend fun getEventsByAttendee(userId: String): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        userRepository.findById(userId).orElse(null)
            ?: throw EntityNotFoundException("User", userId)
        
        eventRepository.findEventsByAttendee(userId)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Get events a user is on the waiting list for
     */
    suspend fun getEventsByWaitingListUser(userId: String): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        userRepository.findById(userId).orElse(null)
            ?: throw EntityNotFoundException("User", userId)
        
        eventRepository.findEventsByWaitingListUser(userId)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Search events by title or location
     */
    suspend fun searchEvents(query: String): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        val titleResults = eventRepository.findEventsByTitleContainingIgnoreCase(query)
        val locationResults = eventRepository.findEventsByLocationContainingIgnoreCase(query)
        
        (titleResults + locationResults)
            .distinctBy { it.id }
            .map { mapToEventSummaryResponse(it) }
    }
    
    // Helper methods for mapping entities to DTOs
    
    private fun mapToEventResponse(event: Event, includeAttendees: Boolean = false): EventResponse {
        val organizer = event.organizerId.let { userRepository.findById(it).orElse(null) }
        
        val attendeeUsers = if (includeAttendees) {
            event.attendeeIds.mapNotNull { id -> userRepository.findById(id).orElse(null) }
                .map { mapToUserResponse(it) }
        } else emptyList()
        
        val waitingListUsers = if (includeAttendees) {
            event.waitingListIds.mapNotNull { id -> userRepository.findById(id).orElse(null) }
                .map { mapToUserResponse(it) }
        } else emptyList()
        
        return EventResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = organizer?.fullName ?: "Unknown",
            organizerId = event.organizerId,
            attendeeCount = event.attendeeCount,
            maxAttendees = event.maxAttendees,
            availableSpots = event.availableSpots,
            headerImagePath = event.headerImagePath.takeIf { it.isNotBlank() },
            needsApproval = event.needsApproval,
            isAtCapacity = event.isAtCapacity,
            createdAt = event.createdAt,
            attendees = attendeeUsers,
            waitingListUsers = waitingListUsers
        )
    }
    
    private fun mapToEventSummaryResponse(event: Event): EventSummaryResponse {
        val organizer = event.organizerId.let { userRepository.findById(it).orElse(null) }
        logger.debug("Mapping event ${event.id}: maxAttendees=${event.maxAttendees}, needsApproval=${event.needsApproval}")
        return EventSummaryResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = organizer?.fullName ?: "Unknown",
            organizerId = event.organizerId,
            attendeeCount = event.attendeeCount,
            maxAttendees = event.maxAttendees,
            availableSpots = event.availableSpots,
            headerImagePath = event.headerImagePath.takeIf { it.isNotBlank() },
            needsApproval = event.needsApproval,
            isAtCapacity = event.isAtCapacity,
            createdAt = event.createdAt
        )
    }
    
    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            createdAt = user.joinedAt
        )
    }
}