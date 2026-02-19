package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.AgeGroup
import rfm.com.entity.KidsService
import java.time.DayOfWeek
import java.time.LocalTime

@Repository
interface KidsServiceRepository : MongoRepository<KidsService, String> {
    
    fun findByIsActiveTrue(): List<KidsService>
    
    fun findByIsActiveFalse(): List<KidsService>
    
    fun findByDayOfWeek(dayOfWeek: DayOfWeek): List<KidsService>
    
    fun findByLeaderId(leaderId: String): List<KidsService>
    
    @Query("{'location': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByLocationContainingIgnoreCase(location: String): List<KidsService>
    
    @Query("{'name': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByNameContainingIgnoreCase(name: String): List<KidsService>
    
    @Query("{'ageGroups': ?0}")
    fun findByAgeGroup(ageGroup: AgeGroup): List<KidsService>
    
    @Query("{'minAge': {'\$lte': ?0}, 'maxAge': {'\$gte': ?0}}")
    fun findByAgeRange(age: Int): List<KidsService>
    
    fun findByRequiresPreRegistrationTrueAndIsActiveTrue(): List<KidsService>
    
    fun findByRequiresPreRegistrationFalseAndIsActiveTrue(): List<KidsService>
    
    @Query("{'enrolledKidIds': ?0}")
    fun findServicesEnrolledByKid(kidId: String): List<KidsService>
    
    @Query("{'volunteerIds': ?0}")
    fun findServicesVolunteeredByUser(userId: String): List<KidsService>

    @Query("{'minAge': {'\$lte': ?0}, 'maxAge': {'\$gte': ?0}, 'ageGroups': ?1, 'isActive': true}")
    fun findSuitableServicesForKid(kidAge: Int, ageGroup: AgeGroup): List<KidsService>
    
    fun countByDayOfWeek(dayOfWeek: DayOfWeek): Long
    
    fun countByIsActiveTrue(): Long
    
    fun countByLeaderId(leaderId: String): Long
    
    fun findByIsActiveTrue(pageable: Pageable): Page<KidsService>

    fun existsByNameAndDayOfWeekAndStartTime(name: String, dayOfWeek: DayOfWeek, startTime: LocalTime): Boolean
}