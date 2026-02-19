package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.CalendarEvent
import rfm.com.entity.CalendarEventType
import java.time.LocalDate

@Repository
interface CalendarEventRepository : MongoRepository<CalendarEvent, String> {

    fun findByEventDate(date: LocalDate): List<CalendarEvent>

    @Query("{'eventDate': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'eventDate': 1, 'startTime': 1}")
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent>

    @Query("{'eventDate': {'\$gte': ?0}}", sort = "{'eventDate': 1, 'startTime': 1}")
    fun findUpcomingEvents(fromDate: LocalDate): List<CalendarEvent>

    @Query("{'eventDate': {'\$gte': ?0}}")
    fun findUpcomingEventsPaged(fromDate: LocalDate, pageable: Pageable): Page<CalendarEvent>

    fun findByEventType(eventType: CalendarEventType): List<CalendarEvent>

    @Query("{'title': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'eventDate': 1}")
    fun findByTitleContainingIgnoreCase(title: String): List<CalendarEvent>

    fun existsByEventDate(eventDate: LocalDate): Boolean
}
