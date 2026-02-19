package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Service
import rfm.com.entity.ServiceType
import java.time.DayOfWeek
import java.time.LocalTime

@Repository
interface ServiceRepository : MongoRepository<Service, String> {
    
    fun findByIsActiveTrue(): List<Service>
    
    fun findByIsActiveFalse(): List<Service>
    
    fun findByServiceType(serviceType: ServiceType): List<Service>
    
    fun findByDayOfWeek(dayOfWeek: DayOfWeek): List<Service>
    
    fun findByLeaderId(leaderId: String): List<Service>
    
    @Query("{'location': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByLocationContainingIgnoreCase(location: String): List<Service>
    
    @Query("{'name': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByNameContainingIgnoreCase(name: String): List<Service>
    
    fun findByRequiresRegistrationTrueAndIsActiveTrue(): List<Service>
    
    fun findByRequiresRegistrationFalseAndIsActiveTrue(): List<Service>
    
    @Query("{'registeredUserIds': ?0}")
    fun findServicesRegisteredByUser(userId: String): List<Service>
    
    fun findByServiceTypeAndDayOfWeek(serviceType: ServiceType, dayOfWeek: DayOfWeek): List<Service>
    
    fun findByServiceTypeAndIsActive(serviceType: ServiceType, isActive: Boolean): List<Service>
    
    fun countByServiceType(serviceType: ServiceType): Long
    
    fun countByIsActiveTrue(): Long
    
    fun countByDayOfWeek(dayOfWeek: DayOfWeek): Long
    
    fun countByLeaderId(leaderId: String): Long
    
    fun findByIsActiveTrue(pageable: Pageable): Page<Service>

    fun existsByNameAndDayOfWeekAndStartTime(name: String, dayOfWeek: DayOfWeek, startTime: LocalTime): Boolean
}