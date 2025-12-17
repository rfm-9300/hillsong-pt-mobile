package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.CalendarEvent
import rfm.com.entity.CalendarEventType
import java.time.LocalDate

@Repository
interface CalendarEventRepository : JpaRepository<CalendarEvent, Long> {

    // Find by ID with creator loaded
    @Query("SELECT ce FROM CalendarEvent ce LEFT JOIN FETCH ce.createdBy WHERE ce.id = :id")
    fun findByIdWithCreator(@Param("id") id: Long): CalendarEvent?

    // Find events for a specific month
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE YEAR(ce.eventDate) = :year AND MONTH(ce.eventDate) = :month
        ORDER BY ce.eventDate ASC, ce.startTime ASC
    """)
    fun findByMonth(@Param("month") month: Int, @Param("year") year: Int): List<CalendarEvent>

    // Find events for a date range
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventDate BETWEEN :startDate AND :endDate
        ORDER BY ce.eventDate ASC, ce.startTime ASC
    """)
    fun findByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<CalendarEvent>

    // Find events for a specific date
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventDate = :date
        ORDER BY ce.startTime ASC
    """)
    fun findByDate(@Param("date") date: LocalDate): List<CalendarEvent>

    // Find upcoming events from today
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventDate >= :fromDate
        ORDER BY ce.eventDate ASC, ce.startTime ASC
    """)
    fun findUpcomingEvents(@Param("fromDate") fromDate: LocalDate): List<CalendarEvent>

    // Find upcoming events with pagination
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventDate >= :fromDate
    """)
    fun findUpcomingEventsPaged(
        @Param("fromDate") fromDate: LocalDate,
        pageable: Pageable
    ): Page<CalendarEvent>

    // Find events by type
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventType = :eventType
        ORDER BY ce.eventDate ASC, ce.startTime ASC
    """)
    fun findByEventType(@Param("eventType") eventType: CalendarEventType): List<CalendarEvent>

    // Find events by type for a specific month
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE ce.eventType = :eventType
        AND YEAR(ce.eventDate) = :year AND MONTH(ce.eventDate) = :month
        ORDER BY ce.eventDate ASC, ce.startTime ASC
    """)
    fun findByEventTypeAndMonth(
        @Param("eventType") eventType: CalendarEventType,
        @Param("month") month: Int,
        @Param("year") year: Int
    ): List<CalendarEvent>

    // Search by title
    @Query("""
        SELECT ce FROM CalendarEvent ce
        LEFT JOIN FETCH ce.createdBy
        WHERE LOWER(ce.title) LIKE LOWER(CONCAT('%', :title, '%'))
        ORDER BY ce.eventDate ASC
    """)
    fun findByTitleContainingIgnoreCase(@Param("title") title: String): List<CalendarEvent>

    // Count events for a month
    @Query("SELECT COUNT(ce) FROM CalendarEvent ce WHERE YEAR(ce.eventDate) = :year AND MONTH(ce.eventDate) = :month")
    fun countByMonth(@Param("month") month: Int, @Param("year") year: Int): Long

    // Check if there are any events on a specific date
    fun existsByEventDate(eventDate: LocalDate): Boolean
}
