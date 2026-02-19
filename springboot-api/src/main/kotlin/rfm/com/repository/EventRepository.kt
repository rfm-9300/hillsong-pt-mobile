package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Event
import java.time.LocalDateTime

@Repository
interface EventRepository : MongoRepository<Event, String> {
    
    @Query("{'date': {'\$gte': ?0}}", sort = "{'date': 1}")
    fun findUpcomingEvents(fromDate: LocalDateTime): List<Event>
    
    @Query("{'date': {'\$lt': ?0}}", sort = "{'date': -1}")
    fun findPastEvents(toDate: LocalDateTime): List<Event>
    
    fun findByOrganizerId(organizerId: String): List<Event>

    fun findByOrganizerId(organizerId: String, pageable: Pageable): Page<Event>
    
    @Query("{'attendeeIds': ?0}", sort = "{'date': 1}")
    fun findEventsByAttendee(userId: String): List<Event>
    
    @Query("{'waitingListIds': ?0}", sort = "{'date': 1}")
    fun findEventsByWaitingListUser(userId: String): List<Event>
    
    @Query("{'date': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'date': 1}")
    fun findEventsByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Event>
    
    @Query("{'location': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': 1}")
    fun findEventsByLocationContainingIgnoreCase(location: String): List<Event>
    
    @Query("{'title': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': 1}")
    fun findEventsByTitleContainingIgnoreCase(title: String): List<Event>
    
    fun findByNeedsApprovalTrue(): List<Event>

    @Query("{'date': {'\$gte': ?0}}")
    fun countUpcomingEvents(fromDate: LocalDateTime): Long
    
    @Query("{'date': {'\$lt': ?0}}")
    fun countPastEvents(toDate: LocalDateTime): Long
    
    @Query("{'date': {'\$gte': ?0}}")
    fun findUpcomingEventsWithPagination(fromDate: LocalDateTime, pageable: Pageable): Page<Event>
}