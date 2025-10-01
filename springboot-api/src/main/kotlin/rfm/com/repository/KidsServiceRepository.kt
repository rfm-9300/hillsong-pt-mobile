package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.AgeGroup
import rfm.com.entity.Kid
import rfm.com.entity.KidsService
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface KidsServiceRepository : JpaRepository<KidsService, Long> {
    
    /**
     * Find kids service by ID with leader eagerly loaded
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.id = :id")
    fun findByIdWithLeader(@Param("id") id: Long): KidsService?
    
    /**
     * Find kids service by ID with enrolled kids eagerly loaded
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.enrolledKids WHERE ks.id = :id")
    fun findByIdWithEnrolledKids(@Param("id") id: Long): KidsService?
    
    /**
     * Find kids service by ID with volunteers eagerly loaded
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.volunteers WHERE ks.id = :id")
    fun findByIdWithVolunteers(@Param("id") id: Long): KidsService?
    
    /**
     * Find kids service by ID with attendance records eagerly loaded
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.kidAttendanceRecords WHERE ks.id = :id")
    fun findByIdWithAttendanceRecords(@Param("id") id: Long): KidsService?
    
    /**
     * Find kids service by ID with all relationships eagerly loaded
     */
    @Query("SELECT DISTINCT ks FROM KidsService ks " +
           "LEFT JOIN FETCH ks.leader " +
           "LEFT JOIN FETCH ks.enrolledKids " +
           "LEFT JOIN FETCH ks.volunteers " +
           "WHERE ks.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): KidsService?
    
    /**
     * Find all active kids services
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findActiveKidsServices(): List<KidsService>
    
    /**
     * Find all inactive kids services
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.isActive = false ORDER BY ks.dayOfWeek, ks.startTime")
    fun findInactiveKidsServices(): List<KidsService>
    
    /**
     * Find kids services by day of week
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.dayOfWeek = :dayOfWeek ORDER BY ks.startTime")
    fun findByDayOfWeek(@Param("dayOfWeek") dayOfWeek: DayOfWeek): List<KidsService>
    
    /**
     * Find kids services by leader
     */
    @Query("SELECT ks FROM KidsService ks WHERE ks.leader = :leader ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByLeader(@Param("leader") leader: UserProfile): List<KidsService>
    
    /**
     * Find kids services by location (case-insensitive)
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE LOWER(ks.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByLocationContainingIgnoreCase(@Param("location") location: String): List<KidsService>
    
    /**
     * Find kids services by name (case-insensitive)
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE LOWER(ks.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<KidsService>
    
    /**
     * Find kids services by age group
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE :ageGroup MEMBER OF ks.ageGroups ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByAgeGroup(@Param("ageGroup") ageGroup: AgeGroup): List<KidsService>
    
    /**
     * Find kids services by age range
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.minAge <= :age AND ks.maxAge >= :age ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByAgeRange(@Param("age") age: Int): List<KidsService>
    
    /**
     * Find kids services that require pre-registration
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.requiresPreRegistration = true AND ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesThatRequirePreRegistration(): List<KidsService>
    
    /**
     * Find kids services that don't require pre-registration
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.requiresPreRegistration = false AND ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesThatDontRequirePreRegistration(): List<KidsService>
    
    /**
     * Find kids services by time range
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.startTime >= :startTime AND ks.endTime <= :endTime ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesByTimeRange(@Param("startTime") startTime: LocalTime, @Param("endTime") endTime: LocalTime): List<KidsService>
    
    /**
     * Find kids services that a kid is enrolled in
     */
    @Query("SELECT ks FROM KidsService ks JOIN ks.enrolledKids k WHERE k = :kid ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesEnrolledByKid(@Param("kid") kid: Kid): List<KidsService>
    
    /**
     * Find kids services where a user is volunteering
     */
    @Query("SELECT ks FROM KidsService ks JOIN ks.volunteers v WHERE v = :user ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesVolunteeredByUser(@Param("user") user: User): List<KidsService>
    
    /**
     * Find kids services with available spots
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE SIZE(ks.enrolledKids) < ks.maxCapacity AND ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesWithAvailableSpots(): List<KidsService>
    
    /**
     * Find kids services at capacity
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE SIZE(ks.enrolledKids) >= ks.maxCapacity ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesAtCapacity(): List<KidsService>
    
    /**
     * Find kids services that need more volunteers (based on ratio)
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.volunteerToChildRatio IS NOT NULL AND ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findServicesWithVolunteerRatio(): List<KidsService>
    
    /**
     * Find kids services by capacity range
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.maxCapacity BETWEEN :minCapacity AND :maxCapacity ORDER BY ks.dayOfWeek, ks.startTime")
    fun findByCapacityRange(@Param("minCapacity") minCapacity: Int, @Param("maxCapacity") maxCapacity: Int): List<KidsService>
    
    /**
     * Find kids services suitable for a specific kid (based on age and age groups)
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.minAge <= :kidAge AND ks.maxAge >= :kidAge AND :ageGroup MEMBER OF ks.ageGroups AND ks.isActive = true ORDER BY ks.dayOfWeek, ks.startTime")
    fun findSuitableServicesForKid(@Param("kidAge") kidAge: Int, @Param("ageGroup") ageGroup: AgeGroup): List<KidsService>
    
    /**
     * Count kids services by day of week
     */
    @Query("SELECT COUNT(ks) FROM KidsService ks WHERE ks.dayOfWeek = :dayOfWeek")
    fun countByDayOfWeek(@Param("dayOfWeek") dayOfWeek: DayOfWeek): Long
    
    /**
     * Count active kids services
     */
    @Query("SELECT COUNT(ks) FROM KidsService ks WHERE ks.isActive = true")
    fun countActiveKidsServices(): Long
    
    /**
     * Count kids services by age group
     */
    @Query("SELECT COUNT(ks) FROM KidsService ks WHERE :ageGroup MEMBER OF ks.ageGroups")
    fun countByAgeGroup(@Param("ageGroup") ageGroup: AgeGroup): Long
    
    /**
     * Count kids services led by a specific leader
     */
    @Query("SELECT COUNT(ks) FROM KidsService ks WHERE ks.leader = :leader")
    fun countByLeader(@Param("leader") leader: UserProfile): Long
    
    /**
     * Count enrolled kids in a service
     */
    @Query("SELECT COUNT(k) FROM KidsService ks JOIN ks.enrolledKids k WHERE ks.id = :serviceId")
    fun countEnrolledKidsInService(@Param("serviceId") serviceId: Long): Long
    
    /**
     * Count volunteers in a service
     */
    @Query("SELECT COUNT(v) FROM KidsService ks JOIN ks.volunteers v WHERE ks.id = :serviceId")
    fun countVolunteersInService(@Param("serviceId") serviceId: Long): Long
    
    /**
     * Find all kids services with pagination
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader")
    fun findAllWithLeader(pageable: Pageable): Page<KidsService>
    
    /**
     * Find active kids services with pagination
     */
    @Query("SELECT ks FROM KidsService ks LEFT JOIN FETCH ks.leader WHERE ks.isActive = true")
    fun findActiveKidsServicesWithPagination(pageable: Pageable): Page<KidsService>
    
    /**
     * Check if kids service exists by name and day/time (for duplicate prevention)
     */
    @Query("SELECT COUNT(ks) > 0 FROM KidsService ks WHERE ks.name = :name AND ks.dayOfWeek = :dayOfWeek AND ks.startTime = :startTime AND (:id IS NULL OR ks.id != :id)")
    fun existsByNameAndDayOfWeekAndStartTimeAndIdNot(@Param("name") name: String, @Param("dayOfWeek") dayOfWeek: DayOfWeek, @Param("startTime") startTime: LocalTime, @Param("id") id: Long?): Boolean
}