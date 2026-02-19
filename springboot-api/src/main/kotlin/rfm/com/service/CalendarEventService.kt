package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.CalendarEvent
import rfm.com.entity.CalendarEventType
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.CalendarEventRepository
import rfm.com.repository.UserRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class CalendarEventService(
    private val calendarEventRepository: CalendarEventRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {
    private val logger = LoggerFactory.getLogger(CalendarEventService::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Create a new calendar event
     */
    suspend fun createCalendarEvent(
        request: CreateCalendarEventRequest,
        creatorId: String,
        image: MultipartFile? = null
    ): CalendarEventResponse = withContext(Dispatchers.IO) {
        logger.info("Creating calendar event: ${request.title} for user: $creatorId")

        userRepository.findById(creatorId).orElse(null)
            ?: throw EntityNotFoundException("User", creatorId)

        val imagePath = image?.let { img ->
            try {
                fileStorageService.storeFile(img, "calendar")
            } catch (ex: Exception) {
                logger.error("Failed to store calendar event image", ex)
                throw RuntimeException("Failed to upload calendar event image: ${ex.message}")
            }
        }

        val calendarEvent = CalendarEvent(
            title = request.title,
            description = request.description,
            eventDate = request.eventDate,
            startTime = request.startTime,
            endTime = request.endTime,
            location = request.location,
            imagePath = imagePath,
            isAllDay = request.isAllDay,
            eventType = request.eventType,
            createdById = creatorId
        )

        val savedEvent = calendarEventRepository.save(calendarEvent)
        logger.info("Calendar event created successfully with id: ${savedEvent.id}")

        mapToCalendarEventResponse(savedEvent)
    }

    /**
     * Get events for a specific month
     */
    suspend fun getEventsForMonth(month: Int, year: Int): List<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching calendar events for month: $month, year: $year")

        if (month < 1 || month > 12) {
            throw IllegalArgumentException("Month must be between 1 and 12")
        }

        val yearMonth = YearMonth.of(year, month)
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        calendarEventRepository.findByDateRange(startDate, endDate)
            .map { mapToCalendarEventResponse(it) }
    }

    /**
     * Get event by ID
     */
    suspend fun getEventById(eventId: String): CalendarEventResponse = withContext(Dispatchers.IO) {
        logger.debug("Fetching calendar event by id: $eventId")

        val event = calendarEventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("CalendarEvent", eventId)

        mapToCalendarEventResponse(event)
    }

    /**
     * Get events for a date range
     */
    suspend fun getEventsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching calendar events from $startDate to $endDate")

        if (startDate.isAfter(endDate)) {
            throw IllegalArgumentException("Start date must be before or equal to end date")
        }

        calendarEventRepository.findByDateRange(startDate, endDate)
            .map { mapToCalendarEventResponse(it) }
    }

    /**
     * Get events for a specific date
     */
    suspend fun getEventsForDate(date: LocalDate): List<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching calendar events for date: $date")

        calendarEventRepository.findByEventDate(date)
            .map { mapToCalendarEventResponse(it) }
    }

    /**
     * Get upcoming events
     */
    suspend fun getUpcomingEvents(pageable: Pageable): Page<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching upcoming calendar events")

        calendarEventRepository.findUpcomingEventsPaged(LocalDate.now(), pageable)
            .map { mapToCalendarEventResponse(it) }
    }

    /**
     * Get events by type
     */
    suspend fun getEventsByType(eventType: CalendarEventType): List<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching calendar events by type: $eventType")

        calendarEventRepository.findByEventType(eventType)
            .map { mapToCalendarEventResponse(it) }
    }

    /**
     * Update a calendar event
     */
    suspend fun updateCalendarEvent(
        eventId: String,
        request: UpdateCalendarEventRequest,
        userId: String,
        image: MultipartFile? = null
    ): CalendarEventResponse = withContext(Dispatchers.IO) {
        logger.info("Updating calendar event: $eventId by user: $userId")

        val event = calendarEventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("CalendarEvent", eventId)

        // Check if user is the creator
        event.createdById?.let { creatorId ->
            if (creatorId != userId) {
                throw AccessDeniedException("Only the event creator can update this event")
            }
        }

        val newImagePath = image?.let { img ->
            try {
                event.imagePath?.let { oldPath ->
                    fileStorageService.deleteFile(oldPath)
                }
                fileStorageService.storeFile(img, "calendar")
            } catch (ex: Exception) {
                logger.error("Failed to store updated calendar event image", ex)
                throw RuntimeException("Failed to upload calendar event image: ${ex.message}")
            }
        }

        val updatedEvent = event.copy(
            title = request.title ?: event.title,
            description = request.description ?: event.description,
            eventDate = request.eventDate ?: event.eventDate,
            startTime = request.startTime ?: event.startTime,
            endTime = request.endTime ?: event.endTime,
            location = request.location ?: event.location,
            imagePath = newImagePath ?: event.imagePath,
            isAllDay = request.isAllDay ?: event.isAllDay,
            eventType = request.eventType ?: event.eventType
        )

        val savedEvent = calendarEventRepository.save(updatedEvent)
        logger.info("Calendar event updated successfully: ${savedEvent.id}")

        mapToCalendarEventResponse(savedEvent)
    }

    /**
     * Delete a calendar event
     */
    suspend fun deleteCalendarEvent(eventId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        logger.info("Deleting calendar event: $eventId by user: $userId")

        val event = calendarEventRepository.findById(eventId).orElse(null)
            ?: throw EntityNotFoundException("CalendarEvent", eventId)

        // Check if user is the creator
        event.createdById?.let { creatorId ->
            if (creatorId != userId) {
                throw AccessDeniedException("Only the event creator can delete this event")
            }
        }

        event.imagePath?.let { imagePath ->
            fileStorageService.deleteFile(imagePath)
        }

        calendarEventRepository.delete(event)
        logger.info("Calendar event deleted successfully: $eventId")
        true
    }

    /**
     * Search events by title
     */
    suspend fun searchEvents(query: String): List<CalendarEventResponse> = withContext(Dispatchers.IO) {
        logger.debug("Searching calendar events with query: $query")

        calendarEventRepository.findByTitleContainingIgnoreCase(query)
            .map { mapToCalendarEventResponse(it) }
    }

    // Helper method for mapping entity to DTO
    private fun mapToCalendarEventResponse(event: CalendarEvent): CalendarEventResponse {
        return CalendarEventResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.eventDate.format(dateFormatter),
            startTime = event.startTime?.format(timeFormatter),
            endTime = event.endTime?.format(timeFormatter),
            location = event.location,
            imageUrl = event.imagePath,
            isAllDay = event.isAllDay,
            eventType = event.eventType,
            createdAt = event.createdAt
        )
    }
}
