package rfm.com.service

import jakarta.persistence.EntityNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Event
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.repository.EventRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

/**
 * Service for managing events
 */
@Service
@Transactional
class EventService(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(EventService::class.java)
    
    /**
     * Create a new event
     */
    suspend fun createEvent(
        createEventRequest: CreateEventRequest,
        organizerId: Long,
        headerImage: MultipartFile? = null
    ): EventResponse = withContext(Dispatchers.IO) {
        logger.info("Creating event: ${createEventRequest.title} for organizer: $organizerId")
        
        val organizer = userProfileRepository.findById(organizerId)
            .orElseThrow { EntityNotFoundException("User profile not found with id: $organizerId") }
        
        // Handle image upload if provided
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
            organizer = organizer,
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
        eventRepository.findAllWithOrganizer(pageable)
            .map { mapToEventSummaryResponse(it) }
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
    suspend fun getEventById(eventId: Long, includeAttendees: Boolean = false): EventResponse = withContext(Dispatchers.IO) {
        logger.debug("Fetching event by id: $eventId")
        
        val event = if (includeAttendees) {
            eventRepository.findByIdWithAllRelationships(eventId)
        } else {
            eventRepository.findByIdWithOrganizer(eventId)
        } ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        mapToEventResponse(event, includeAttendees)
    }
    
    /**
     * Update an existing event
     */
    suspend fun updateEvent(
        eventId: Long,
        updateRequest: UpdateEventRequest,
        userId: Long,
        headerImage: MultipartFile? = null
    ): EventResponse = withContext(Dispatchers.IO) {
        logger.info("Updating event: $eventId by user: $userId")
        
        val event = eventRepository.findByIdWithOrganizer(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        // Check if user is the organizer
        if (event.organizer.id != userId) {
            throw AccessDeniedException("Only the event organizer can update this event")
        }
        
        // Handle image upload if provided
        val newImagePath = headerImage?.let { image ->
            try {
                // Delete old image if it exists
                if (event.headerImagePath.isNotBlank()) {
                    fileStorageService.deleteFile(event.headerImagePath)
                }
                fileStorageService.storeEventImage(image)
            } catch (ex: Exception) {
                logger.error("Failed to store updated event image", ex)
                throw RuntimeException("Failed to upload event image: ${ex.message}")
            }
        }
        
        // Create updated event
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
    suspend fun deleteEvent(eventId: Long, userId: Long): Boolean = withContext(Dispatchers.IO) {
        logger.info("Deleting event: $eventId by user: $userId")
        
        val event = eventRepository.findByIdWithOrganizer(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        // Check if user is the organizer
        if (event.organizer.id != userId) {
            throw AccessDeniedException("Only the event organizer can delete this event")
        }
        
        // Delete associated image if it exists
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
    suspend fun joinEvent(eventId: Long, userId: Long): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("User $userId attempting to join event $eventId")
        
        val event = eventRepository.findByIdWithAllRelationships(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        // Check if user is already an attendee
        if (event.attendees.contains(user)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is already attending this event",
                eventId = eventId,
                userId = userId,
                currentStatus = EventUserStatus.ATTENDEE
            )
        }
        
        // Check if user is already on waiting list
        if (event.waitingListUsers.contains(user)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is already on the waiting list for this event",
                eventId = eventId,
                userId = userId,
                currentStatus = EventUserStatus.WAITING_LIST
            )
        }
        
        val result = when {
            // If event needs approval, add to waiting list
            event.needsApproval -> {
                event.addToWaitingList(user)
                eventRepository.save(event)
                EventActionResponse(
                    success = true,
                    message = "Added to waiting list. Awaiting organizer approval.",
                    eventId = eventId,
                    userId = userId,
                    currentStatus = EventUserStatus.PENDING_APPROVAL
                )
            }
            // If event is at capacity, add to waiting list
            event.isAtCapacity -> {
                event.addToWaitingList(user)
                eventRepository.save(event)
                EventActionResponse(
                    success = true,
                    message = "Event is at capacity. Added to waiting list.",
                    eventId = eventId,
                    userId = userId,
                    currentStatus = EventUserStatus.WAITING_LIST
                )
            }
            // Otherwise, add as attendee
            else -> {
                event.addAttendee(user)
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
    suspend fun leaveEvent(eventId: Long, userId: Long): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("User $userId attempting to leave event $eventId")
        
        val event = eventRepository.findByIdWithAllRelationships(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        val wasAttendee = event.removeAttendee(user)
        val wasOnWaitingList = event.removeFromWaitingList(user)
        
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
        if (wasAttendee && !event.isAtCapacity && event.waitingListUsers.isNotEmpty()) {
            val nextUser = event.waitingListUsers.first()
            event.removeFromWaitingList(nextUser)
            event.addAttendee(nextUser)
            logger.info("Promoted user ${nextUser.id} from waiting list to attendee for event $eventId")
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
    suspend fun approveUserForEvent(eventId: Long, userIdToApprove: Long, organizerId: Long): EventActionResponse = withContext(Dispatchers.IO) {
        logger.info("Organizer $organizerId approving user $userIdToApprove for event $eventId")
        
        val event = eventRepository.findByIdWithAllRelationships(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        // Check if user is the organizer
        if (event.organizer.id != organizerId) {
            throw AccessDeniedException("Only the event organizer can approve users")
        }
        
        val userToApprove = userRepository.findById(userIdToApprove)
            .orElseThrow { EntityNotFoundException("User not found with id: $userIdToApprove") }
        
        // Check if user is on waiting list
        if (!event.waitingListUsers.contains(userToApprove)) {
            return@withContext EventActionResponse(
                success = false,
                message = "User is not on the waiting list for this event",
                eventId = eventId,
                userId = userIdToApprove,
                currentStatus = if (event.attendees.contains(userToApprove)) EventUserStatus.ATTENDEE else EventUserStatus.NOT_JOINED
            )
        }
        
        // Check if event has capacity
        if (event.isAtCapacity) {
            return@withContext EventActionResponse(
                success = false,
                message = "Event is at capacity. Cannot approve more users.",
                eventId = eventId,
                userId = userIdToApprove,
                currentStatus = EventUserStatus.WAITING_LIST
            )
        }
        
        // Move user from waiting list to attendees
        event.removeFromWaitingList(userToApprove)
        event.addAttendee(userToApprove)
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
    suspend fun getUserEventStatus(eventId: Long, userId: Long): UserEventStatusResponse = withContext(Dispatchers.IO) {
        val event = eventRepository.findByIdWithAllRelationships(eventId)
            ?: throw EntityNotFoundException("Event not found with id: $eventId")
        
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        val status = when {
            event.attendees.contains(user) -> EventUserStatus.ATTENDEE
            event.waitingListUsers.contains(user) -> {
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
    suspend fun getEventsByOrganizer(organizerId: Long, pageable: Pageable): Page<EventSummaryResponse> = withContext(Dispatchers.IO) {
        val organizer = userProfileRepository.findById(organizerId)
            .orElseThrow { EntityNotFoundException("User profile not found with id: $organizerId") }
        
        eventRepository.findByOrganizer(organizer, pageable)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Get events a user is attending
     */
    suspend fun getEventsByAttendee(userId: Long): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        eventRepository.findEventsByAttendee(user)
            .map { mapToEventSummaryResponse(it) }
    }
    
    /**
     * Get events a user is on the waiting list for
     */
    suspend fun getEventsByWaitingListUser(userId: Long): List<EventSummaryResponse> = withContext(Dispatchers.IO) {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found with id: $userId") }
        
        eventRepository.findEventsByWaitingListUser(user)
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
        return EventResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = "${event.organizer.firstName} ${event.organizer.lastName}",
            organizerId = event.organizer.id!!,
            attendeeCount = event.attendeeCount,
            maxAttendees = event.maxAttendees,
            availableSpots = event.availableSpots,
            headerImagePath = event.headerImagePath.takeIf { it.isNotBlank() },
            needsApproval = event.needsApproval,
            isAtCapacity = event.isAtCapacity,
            createdAt = event.createdAt,
            attendees = if (includeAttendees) event.attendees.map { mapToUserResponse(it) } else emptyList(),
            waitingListUsers = if (includeAttendees) event.waitingListUsers.map { mapToUserResponse(it) } else emptyList()
        )
    }
    
    private fun mapToEventSummaryResponse(event: Event): EventSummaryResponse {
        return EventSummaryResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = "${event.organizer.firstName} ${event.organizer.lastName}",
            organizerId = event.organizer.id!!,
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
            firstName = user.profile?.firstName ?: "",
            lastName = user.profile?.lastName ?: "",
            verified = user.verified,
            createdAt = user.createdAt,
            authProvider = user.authProvider.name
        )
    }
}