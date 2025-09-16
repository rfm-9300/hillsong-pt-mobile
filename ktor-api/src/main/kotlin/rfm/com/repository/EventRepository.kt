package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.Event
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    
    /**
     * Find event by ID with organizer eagerly loaded
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.id = :id")
    fun findByIdWithOrganizer(@Param("id") id: Long): Event?
    
    /**
     * Find event by ID with attendees eagerly loaded
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.attendees WHERE e.id = :id")
    fun findByIdWithAttendees(@Param("id") id: Long): Event?
    
    /**
     * Find event by ID with waiting list users eagerly loaded
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.waitingListUsers WHERE e.id = :id")
    fun findByIdWithWaitingList(@Param("id") id: Long): Event?
    
    /**
     * Find event by ID with all relationships eagerly loaded
     */
    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN FETCH e.organizer " +
           "LEFT JOIN FETCH e.attendees " +
           "LEFT JOIN FETCH e.waitingListUsers " +
           "WHERE e.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): Event?
    
    /**
     * Find all upcoming events (events with date >= current time)
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.date >= :fromDate ORDER BY e.date ASC")
    fun findUpcomingEvents(@Param("fromDate") fromDate: LocalDateTime): List<Event>
    
    /**
     * Find all past events (events with date < current time)
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.date < :toDate ORDER BY e.date DESC")
    fun findPastEvents(@Param("toDate") toDate: LocalDateTime): List<Event>
    
    /**
     * Find events organized by a specific user
     */
    @Query("SELECT e FROM Event e WHERE e.organizer = :organizer ORDER BY e.date DESC")
    fun findByOrganizer(@Param("organizer") organizer: UserProfile): List<Event>
    
    /**
     * Find events organized by a specific user with pagination
     */
    @Query("SELECT e FROM Event e WHERE e.organizer = :organizer ORDER BY e.date DESC")
    fun findByOrganizer(@Param("organizer") organizer: UserProfile, pageable: Pageable): Page<Event>
    
    /**
     * Find events that a user is attending
     */
    @Query("SELECT e FROM Event e JOIN e.attendees a WHERE a = :user ORDER BY e.date ASC")
    fun findEventsByAttendee(@Param("user") user: User): List<Event>
    
    /**
     * Find events that a user is on the waiting list for
     */
    @Query("SELECT e FROM Event e JOIN e.waitingListUsers w WHERE w = :user ORDER BY e.date ASC")
    fun findEventsByWaitingListUser(@Param("user") user: User): List<Event>
    
    /**
     * Find events by date range
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.date BETWEEN :startDate AND :endDate ORDER BY e.date ASC")
    fun findEventsByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Event>
    
    /**
     * Find events by location (case-insensitive)
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY e.date ASC")
    fun findEventsByLocationContainingIgnoreCase(@Param("location") location: String): List<Event>
    
    /**
     * Find events by title (case-insensitive)
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY e.date ASC")
    fun findEventsByTitleContainingIgnoreCase(@Param("title") title: String): List<Event>
    
    /**
     * Find events that need approval
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.needsApproval = true ORDER BY e.date ASC")
    fun findEventsThatNeedApproval(): List<Event>
    
    /**
     * Find events that are at capacity
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE SIZE(e.attendees) >= e.maxAttendees ORDER BY e.date ASC")
    fun findEventsAtCapacity(): List<Event>
    
    /**
     * Find events with available spots
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE SIZE(e.attendees) < e.maxAttendees ORDER BY e.date ASC")
    fun findEventsWithAvailableSpots(): List<Event>
    
    /**
     * Count events organized by a user
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.organizer = :organizer")
    fun countEventsByOrganizer(@Param("organizer") organizer: UserProfile): Long
    
    /**
     * Count upcoming events
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.date >= :fromDate")
    fun countUpcomingEvents(@Param("fromDate") fromDate: LocalDateTime): Long
    
    /**
     * Count past events
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.date < :toDate")
    fun countPastEvents(@Param("toDate") toDate: LocalDateTime): Long
    
    /**
     * Find events with pagination and sorting
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer")
    fun findAllWithOrganizer(pageable: Pageable): Page<Event>
    
    /**
     * Find upcoming events with pagination
     */
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.date >= :fromDate")
    fun findUpcomingEventsWithPagination(@Param("fromDate") fromDate: LocalDateTime, pageable: Pageable): Page<Event>
}