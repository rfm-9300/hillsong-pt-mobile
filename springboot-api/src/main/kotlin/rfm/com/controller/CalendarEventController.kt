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
import rfm.com.entity.CalendarEventType
import rfm.com.service.CalendarEventService
import rfm.com.util.getCurrentUserId
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/calendar")
@PreAuthorize("hasRole('USER')")
class CalendarEventController(
    private val calendarEventService: CalendarEventService
) {
    private val logger = LoggerFactory.getLogger(CalendarEventController::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Get events for a specific month
     * GET /api/calendar/month?month=12&year=2024
     */
    @GetMapping("/month")
    fun getEventsForMonth(
        @RequestParam month: Int,
        @RequestParam year: Int
    ): ResponseEntity<ApiResponse<List<CalendarEventResponse>>> = runBlocking {
        logger.debug("Fetching calendar events for month: $month, year: $year")

        val events = calendarEventService.getEventsForMonth(month, year)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar events retrieved successfully",
                data = events
            )
        )
    }

    /**
     * Get a single event by ID
     * GET /api/calendar/{id}
     */
    @GetMapping("/{id}")
    fun getEventById(
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<CalendarEventResponse>> = runBlocking {
        logger.debug("Fetching calendar event by id: $id")

        val event = calendarEventService.getEventById(id)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar event retrieved successfully",
                data = event
            )
        )
    }

    /**
     * Get events for a date range
     * GET /api/calendar/range?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/range")
    fun getEventsForDateRange(
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<ApiResponse<List<CalendarEventResponse>>> = runBlocking {
        logger.debug("Fetching calendar events from $startDate to $endDate")

        val start = LocalDate.parse(startDate, dateFormatter)
        val end = LocalDate.parse(endDate, dateFormatter)

        val events = calendarEventService.getEventsForDateRange(start, end)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar events retrieved successfully",
                data = events
            )
        )
    }

    /**
     * Get events for a specific date
     * GET /api/calendar/date?date=2024-12-25
     */
    @GetMapping("/date")
    fun getEventsForDate(
        @RequestParam date: String
    ): ResponseEntity<ApiResponse<List<CalendarEventResponse>>> = runBlocking {
        logger.debug("Fetching calendar events for date: $date")

        val localDate = LocalDate.parse(date, dateFormatter)
        val events = calendarEventService.getEventsForDate(localDate)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar events retrieved successfully",
                data = events
            )
        )
    }

    /**
     * Get upcoming events with pagination
     * GET /api/calendar/upcoming?page=0&size=20
     */
    @GetMapping("/upcoming")
    fun getUpcomingEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<CalendarEventResponse>>> = runBlocking {
        logger.debug("Fetching upcoming calendar events - page: $page, size: $size")

        val pageable: Pageable = PageRequest.of(page, size, Sort.by("eventDate", "startTime").ascending())
        val events = calendarEventService.getUpcomingEvents(pageable)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Upcoming calendar events retrieved successfully",
                data = events
            )
        )
    }

    /**
     * Get events by type
     * GET /api/calendar/type/{eventType}
     */
    @GetMapping("/type/{eventType}")
    fun getEventsByType(
        @PathVariable eventType: CalendarEventType
    ): ResponseEntity<ApiResponse<List<CalendarEventResponse>>> = runBlocking {
        logger.debug("Fetching calendar events by type: $eventType")

        val events = calendarEventService.getEventsByType(eventType)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar events retrieved successfully",
                data = events
            )
        )
    }

    /**
     * Search events by title
     * GET /api/calendar/search?query=worship
     */
    @GetMapping("/search")
    fun searchEvents(
        @RequestParam query: String
    ): ResponseEntity<ApiResponse<List<CalendarEventResponse>>> = runBlocking {
        logger.debug("Searching calendar events with query: $query")

        val events = calendarEventService.searchEvents(query)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar events search completed",
                data = events
            )
        )
    }

    /**
     * Create a new calendar event
     * POST /api/calendar
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun createCalendarEvent(
        @Valid @RequestPart("event") request: CreateCalendarEventRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CalendarEventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Creating calendar event: ${request.title} by user: $userId")

        val event = calendarEventService.createCalendarEvent(request, userId, image)

        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Calendar event created successfully",
                data = event
            )
        )
    }

    /**
     * Create a new calendar event (JSON only, no image)
     * POST /api/calendar/json
     */
    @PostMapping("/json", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun createCalendarEventJson(
        @Valid @RequestBody request: CreateCalendarEventRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CalendarEventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Creating calendar event (JSON): ${request.title} by user: $userId")

        val event = calendarEventService.createCalendarEvent(request, userId, null)

        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Calendar event created successfully",
                data = event
            )
        )
    }

    /**
     * Update an existing calendar event
     * PUT /api/calendar/{id}
     */
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateCalendarEvent(
        @PathVariable id: Long,
        @Valid @RequestPart("event") request: UpdateCalendarEventRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CalendarEventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Updating calendar event: $id by user: $userId")

        val event = calendarEventService.updateCalendarEvent(id, request, userId, image)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar event updated successfully",
                data = event
            )
        )
    }

    /**
     * Update an existing calendar event (JSON only, no image)
     * PUT /api/calendar/{id}/json
     */
    @PutMapping("/{id}/json", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN')")
    fun updateCalendarEventJson(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCalendarEventRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CalendarEventResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Updating calendar event (JSON): $id by user: $userId")

        val event = calendarEventService.updateCalendarEvent(id, request, userId, null)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar event updated successfully",
                data = event
            )
        )
    }

    /**
     * Delete a calendar event
     * DELETE /api/calendar/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteCalendarEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Deleting calendar event: $id by user: $userId")

        calendarEventService.deleteCalendarEvent(id, userId)

        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Calendar event deleted successfully",
                data = "Calendar event with id $id has been deleted"
            )
        )
    }
}
