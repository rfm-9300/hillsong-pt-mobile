package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.Service
import rfm.com.entity.ServiceType
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.DayOfWeek
import java.time.LocalTime

@Repository
interface ServiceRepository : JpaRepository<Service, Long> {
    
    /**
     * Find service by ID with leader eagerly loaded
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.id = :id")
    fun findByIdWithLeader(@Param("id") id: Long): Service?
    
    /**
     * Find service by ID with registered users eagerly loaded
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.registeredUsers WHERE s.id = :id")
    fun findByIdWithRegisteredUsers(@Param("id") id: Long): Service?
    
    /**
     * Find service by ID with attendance records eagerly loaded
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.attendanceRecords WHERE s.id = :id")
    fun findByIdWithAttendanceRecords(@Param("id") id: Long): Service?
    
    /**
     * Find all active services
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.isActive = true ORDER BY s.dayOfWeek, s.startTime")
    fun findActiveServices(): List<Service>
    
    /**
     * Find all inactive services
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.isActive = false ORDER BY s.dayOfWeek, s.startTime")
    fun findInactiveServices(): List<Service>
    
    /**
     * Find services by type
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.serviceType = :serviceType ORDER BY s.dayOfWeek, s.startTime")
    fun findByServiceType(@Param("serviceType") serviceType: ServiceType): List<Service>
    
    /**
     * Find services by day of week
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.dayOfWeek = :dayOfWeek ORDER BY s.startTime")
    fun findByDayOfWeek(@Param("dayOfWeek") dayOfWeek: DayOfWeek): List<Service>
    
    /**
     * Find services by leader
     */
    @Query("SELECT s FROM Service s WHERE s.leader = :leader ORDER BY s.dayOfWeek, s.startTime")
    fun findByLeader(@Param("leader") leader: UserProfile): List<Service>
    
    /**
     * Find services by location (case-insensitive)
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY s.dayOfWeek, s.startTime")
    fun findByLocationContainingIgnoreCase(@Param("location") location: String): List<Service>
    
    /**
     * Find services by name (case-insensitive)
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY s.dayOfWeek, s.startTime")
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<Service>
    
    /**
     * Find services that require registration
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.requiresRegistration = true AND s.isActive = true ORDER BY s.dayOfWeek, s.startTime")
    fun findServicesThatRequireRegistration(): List<Service>
    
    /**
     * Find services that don't require registration
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.requiresRegistration = false AND s.isActive = true ORDER BY s.dayOfWeek, s.startTime")
    fun findServicesThatDontRequireRegistration(): List<Service>
    
    /**
     * Find services with capacity limits
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.maxCapacity IS NOT NULL AND s.isActive = true ORDER BY s.dayOfWeek, s.startTime")
    fun findServicesWithCapacityLimits(): List<Service>
    
    /**
     * Find services by time range
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.startTime >= :startTime AND s.endTime <= :endTime ORDER BY s.dayOfWeek, s.startTime")
    fun findServicesByTimeRange(@Param("startTime") startTime: LocalTime, @Param("endTime") endTime: LocalTime): List<Service>
    
    /**
     * Find services that a user is registered for
     */
    @Query("SELECT s FROM Service s JOIN s.registeredUsers r WHERE r = :user ORDER BY s.dayOfWeek, s.startTime")
    fun findServicesRegisteredByUser(@Param("user") user: User): List<Service>
    
    /**
     * Find services by type and day of week
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.serviceType = :serviceType AND s.dayOfWeek = :dayOfWeek ORDER BY s.startTime")
    fun findByServiceTypeAndDayOfWeek(@Param("serviceType") serviceType: ServiceType, @Param("dayOfWeek") dayOfWeek: DayOfWeek): List<Service>
    
    /**
     * Find services by type and active status
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.serviceType = :serviceType AND s.isActive = :isActive ORDER BY s.dayOfWeek, s.startTime")
    fun findByServiceTypeAndIsActive(@Param("serviceType") serviceType: ServiceType, @Param("isActive") isActive: Boolean): List<Service>
    
    /**
     * Count services by type
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.serviceType = :serviceType")
    fun countByServiceType(@Param("serviceType") serviceType: ServiceType): Long
    
    /**
     * Count active services
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.isActive = true")
    fun countActiveServices(): Long
    
    /**
     * Count services by day of week
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.dayOfWeek = :dayOfWeek")
    fun countByDayOfWeek(@Param("dayOfWeek") dayOfWeek: DayOfWeek): Long
    
    /**
     * Count services led by a specific leader
     */
    @Query("SELECT COUNT(s) FROM Service s WHERE s.leader = :leader")
    fun countByLeader(@Param("leader") leader: UserProfile): Long
    
    /**
     * Find all services with pagination
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader")
    fun findAllWithLeader(pageable: Pageable): Page<Service>
    
    /**
     * Find active services with pagination
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.leader WHERE s.isActive = true")
    fun findActiveServicesWithPagination(pageable: Pageable): Page<Service>
    
    /**
     * Check if service exists by name and day/time (for duplicate prevention)
     */
    @Query("SELECT COUNT(s) > 0 FROM Service s WHERE s.name = :name AND s.dayOfWeek = :dayOfWeek AND s.startTime = :startTime AND (:id IS NULL OR s.id != :id)")
    fun existsByNameAndDayOfWeekAndStartTimeAndIdNot(@Param("name") name: String, @Param("dayOfWeek") dayOfWeek: DayOfWeek, @Param("startTime") startTime: LocalTime, @Param("id") id: Long?): Boolean
}