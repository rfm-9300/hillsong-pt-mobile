package rfm.com.controller

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.service.EventService
import rfm.com.util.getCurrentUserId

/**
 * REST controller for event management operations
 */
@RestController
@RequestMapping("/api/events")
@PreAuthorize("hasRole('USER')")
class EventController(
    private val eventService: EventService
) {
    
    private val logger = LoggerFactory.getLogger(EventController::class.java)
    
    /**
     * Get all events with pagination and sorting
     */
    @GetMapping
    fun getAllEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> = runBlocking {
        logger.debug("Fetching all events - page: $page, size: $size, sortBy: $sortBy, sortDir: $sortDir")
        
        val sort = if (sortDir.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val events = eventService.getAllEvents(pageable)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Events retrieved successfully",
                data = events
            )
        )
    }
    
    /**
     * Get upcoming events
     */
    @GetMapping("/upcoming")
    fun getUpcomingEvents(): ResponseEntity<ApiResponse<List<EventSummaryResponse>>> = runBlocking {
        logger.debug("Fetching upcoming events")
        
        val events = eventService.getUpcomingEvents()
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Upcoming events retrieved successfully",
                data = events
            )
        )
    }
    
    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    fun getEventById(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "false") includeAttendees: Boolean
    ): ResponseEntity<ApiResponse<EventResponse>> = runBlocking {
        logger.debug("Fetching event by id: $id, includeAttendees: $includeAttendees")
        
        val event = eventService.getEventById(id, includeAttendees)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Event retrieved successfully",
                data = event
            )
        )
    }
    
    /**
     * Create a new event
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createEvent(
        @Valid @RequestPart("event") createEventRequest: CreateEventRequest,
        @RequestPart("image", required = false) headerImage: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Creating event: ${createEventRequest.title} for user: $userId")
        
        val event = eventService.createEvent(createEventRequest, userId, headerImage)
        
        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Event created successfully",
                data = event
            )
        )
    }
    
    /**
     * Update an existing event
     */
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateEvent(
        @PathVariable id: Long,
        @Valid @RequestPart("event") updateEventRequest: UpdateEventRequest,
        @RequestPart("image", required = false) headerImage: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Updating event: $id by user: $userId")
        
        val event = eventService.updateEvent(id, updateEventRequest, userId, headerImage)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Event updated successfully",
                data = event
            )
        )
    }
    
    /**
     * Delete an event
     */
    @DeleteMapping("/{id}")
    fun deleteEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Deleting event: $id by user: $userId")
        
        eventService.deleteEvent(id, userId)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Event deleted successfully",
                data = "Event with id $id has been deleted"
            )
        )
    }
    
    /**
     * Join an event
     */
    @PostMapping("/{id}/join")
    fun joinEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EventActionResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("User $userId joining event $id")
        
        val result = eventService.joinEvent(id, userId)
        
        val status = if (result.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        
        ResponseEntity.status(status).body(
            ApiResponse(
                success = result.success,
                message = result.message,
                data = result
            )
        )
    }
    
    /**
     * Leave an event
     */
    @PostMapping("/{id}/leave")
    fun leaveEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EventActionResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("User $userId leaving event $id")
        
        val result = eventService.leaveEvent(id, userId)
        
        val status = if (result.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        
        ResponseEntity.status(status).body(
            ApiResponse(
                success = result.success,
                message = result.message,
                data = result
            )
        )
    }
    
    /**
     * Approve a user for an event (organizer only)
     */
    @PostMapping("/{eventId}/approve/{userId}")
    fun approveUserForEvent(
        @PathVariable eventId: Long,
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EventActionResponse>> = runBlocking {
        val organizerId = authentication.getCurrentUserId()
        logger.info("Organizer $organizerId approving user $userId for event $eventId")
        
        val result = eventService.approveUserForEvent(eventId, userId, organizerId)
        
        val status = if (result.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        
        ResponseEntity.status(status).body(
            ApiResponse(
                success = result.success,
                message = result.message,
                data = result
            )
        )
    }
    
    /**
     * Get user's status for a specific event
     */
    @GetMapping("/{id}/status")
    fun getUserEventStatus(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<UserEventStatusResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.debug("Getting event status for user $userId and event $id")
        
        val status = eventService.getUserEventStatus(id, userId)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Event status retrieved successfully",
                data = status
            )
        )
    }
    
    /**
     * Get events organized by the current user
     */
    @GetMapping("/my-events")
    fun getMyEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Page<EventSummaryResponse>>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.debug("Fetching events organized by user: $userId")
        
        val sort = if (sortDir.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val events = eventService.getEventsByOrganizer(userId, pageable)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Your events retrieved successfully",
                data = events
            )
        )
    }
    
    /**
     * Get events the current user is attending
     */
    @GetMapping("/attending")
    fun getEventsAttending(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<EventSummaryResponse>>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.debug("Fetching events user $userId is attending")
        
        val events = eventService.getEventsByAttendee(userId)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Events you're attending retrieved successfully",
                data = events
            )
        )
    }
    
    /**
     * Get events the current user is on the waiting list for
     */
    @GetMapping("/waiting-list")
    fun getEventsOnWaitingList(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<EventSummaryResponse>>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.debug("Fetching events user $userId is on waiting list for")
        
        val events = eventService.getEventsByWaitingListUser(userId)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Events you're on waiting list for retrieved successfully",
                data = events
            )
        )
    }
    
    /**
     * Search events by title or location
     */
    @GetMapping("/search")
    fun searchEvents(
        @RequestParam query: String
    ): ResponseEntity<ApiResponse<List<EventSummaryResponse>>> = runBlocking {
        logger.debug("Searching events with query: $query")
        
        if (query.isBlank()) {
            return@runBlocking ResponseEntity.badRequest().body(
                ApiResponse<List<EventSummaryResponse>>(
                    success = false,
                    message = "Search query cannot be empty"
                )
            )
        }
        
        val events = eventService.searchEvents(query.trim())
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Search completed successfully",
                data = events
            )
        )
    }
}