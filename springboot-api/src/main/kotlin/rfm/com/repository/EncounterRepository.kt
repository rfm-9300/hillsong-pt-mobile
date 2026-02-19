package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Encounter
import java.time.LocalDateTime

@Repository
interface EncounterRepository : MongoRepository<Encounter, String> {
    
    @Query("{'date': {'\$gte': ?0}}", sort = "{'date': 1}")
    fun findUpcomingEncounters(fromDate: LocalDateTime): List<Encounter>
    
    fun findByOrganizerId(organizerId: String, pageable: Pageable): Page<Encounter>
    
    @Query("{'date': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'date': 1}")
    fun findEncountersByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Encounter>
    
    @Query("{'location': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': 1}")
    fun findEncountersByLocationContainingIgnoreCase(location: String): List<Encounter>
    
    @Query("{'title': {'\$regex': ?0, '\$options': 'i'}}", sort = "{'date': 1}")
    fun findEncountersByTitleContainingIgnoreCase(title: String): List<Encounter>
    
    fun countByOrganizerId(organizerId: String): Long
}
