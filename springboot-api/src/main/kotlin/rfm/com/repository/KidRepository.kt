package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.AgeGroup
import rfm.com.entity.Gender
import rfm.com.entity.Kid
import rfm.com.entity.KidsService
import rfm.com.entity.UserProfile
import java.time.LocalDate

@Repository
interface KidRepository : JpaRepository<Kid, Long> {
    
    /**
     * Find kid by ID with primary parent eagerly loaded
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.id = :id")
    fun findByIdWithPrimaryParent(@Param("id") id: Long): Kid?
    
    /**
     * Find kid by ID with both parents eagerly loaded
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent LEFT JOIN FETCH k.secondaryParent WHERE k.id = :id")
    fun findByIdWithParents(@Param("id") id: Long): Kid?
    
    /**
     * Find kid by ID with kids services eagerly loaded
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.kidsServices WHERE k.id = :id")
    fun findByIdWithKidsServices(@Param("id") id: Long): Kid?
    
    /**
     * Find kid by ID with attendance records eagerly loaded
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.attendanceRecords WHERE k.id = :id")
    fun findByIdWithAttendanceRecords(@Param("id") id: Long): Kid?
    
    /**
     * Find all active kids
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.isActive = true ORDER BY k.firstName, k.lastName")
    fun findActiveKids(): List<Kid>
    
    /**
     * Find all inactive kids
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.isActive = false ORDER BY k.firstName, k.lastName")
    fun findInactiveKids(): List<Kid>
    
    /**
     * Find kids by primary parent
     */
    @Query("SELECT k FROM Kid k WHERE k.primaryParent = :parent ORDER BY k.firstName, k.lastName")
    fun findByPrimaryParent(@Param("parent") parent: UserProfile): List<Kid>
    
    /**
     * Find kids by secondary parent
     */
    @Query("SELECT k FROM Kid k WHERE k.secondaryParent = :parent ORDER BY k.firstName, k.lastName")
    fun findBySecondaryParent(@Param("parent") parent: UserProfile): List<Kid>
    
    /**
     * Find kids by either parent (primary or secondary)
     */
    @Query("SELECT k FROM Kid k WHERE k.primaryParent = :parent OR k.secondaryParent = :parent ORDER BY k.firstName, k.lastName")
    fun findByEitherParent(@Param("parent") parent: UserProfile): List<Kid>
    
    /**
     * Find kids by first name (case-insensitive)
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE LOWER(k.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) ORDER BY k.firstName, k.lastName")
    fun findByFirstNameContainingIgnoreCase(@Param("firstName") firstName: String): List<Kid>
    
    /**
     * Find kids by last name (case-insensitive)
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE LOWER(k.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) ORDER BY k.firstName, k.lastName")
    fun findByLastNameContainingIgnoreCase(@Param("lastName") lastName: String): List<Kid>
    
    /**
     * Find kids by full name (case-insensitive)
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE LOWER(CONCAT(k.firstName, ' ', k.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%')) ORDER BY k.firstName, k.lastName")
    fun findByFullNameContainingIgnoreCase(@Param("fullName") fullName: String): List<Kid>
    
    /**
     * Find kids by gender
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.gender = :gender ORDER BY k.firstName, k.lastName")
    fun findByGender(@Param("gender") gender: Gender): List<Kid>
    
    /**
     * Find kids by age range (calculated from date of birth)
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.dateOfBirth BETWEEN :maxAgeDate AND :minAgeDate ORDER BY k.firstName, k.lastName")
    fun findByAgeRange(@Param("minAgeDate") minAgeDate: LocalDate, @Param("maxAgeDate") maxAgeDate: LocalDate): List<Kid>
    
    /**
     * Find kids by date of birth range
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.dateOfBirth BETWEEN :startDate AND :endDate ORDER BY k.dateOfBirth DESC")
    fun findByDateOfBirthBetween(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<Kid>
    
    /**
     * Find kids enrolled in a specific kids service
     */
    @Query("SELECT k FROM Kid k JOIN k.kidsServices ks WHERE ks = :kidsService ORDER BY k.firstName, k.lastName")
    fun findByKidsService(@Param("kidsService") kidsService: KidsService): List<Kid>
    
    /**
     * Find kids with medical notes
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.medicalNotes IS NOT NULL AND k.medicalNotes != '' ORDER BY k.firstName, k.lastName")
    fun findKidsWithMedicalNotes(): List<Kid>
    
    /**
     * Find kids with allergies
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.allergies IS NOT NULL AND k.allergies != '' ORDER BY k.firstName, k.lastName")
    fun findKidsWithAllergies(): List<Kid>
    
    /**
     * Find kids with special needs
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.specialNeeds IS NOT NULL AND k.specialNeeds != '' ORDER BY k.firstName, k.lastName")
    fun findKidsWithSpecialNeeds(): List<Kid>
    
    /**
     * Find kids with emergency contact information
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.emergencyContactName IS NOT NULL AND k.emergencyContactPhone IS NOT NULL ORDER BY k.firstName, k.lastName")
    fun findKidsWithEmergencyContact(): List<Kid>
    
    /**
     * Find kids without emergency contact information
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.emergencyContactName IS NULL OR k.emergencyContactPhone IS NULL ORDER BY k.firstName, k.lastName")
    fun findKidsWithoutEmergencyContact(): List<Kid>
    
    /**
     * Find kids eligible for a specific age group
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.dateOfBirth BETWEEN :maxAgeDate AND :minAgeDate AND k.isActive = true ORDER BY k.firstName, k.lastName")
    fun findKidsEligibleForAgeGroup(@Param("minAgeDate") minAgeDate: LocalDate, @Param("maxAgeDate") maxAgeDate: LocalDate): List<Kid>
    
    /**
     * Find kids by specific age (calculated)
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE YEAR(CURRENT_DATE) - YEAR(k.dateOfBirth) = :age ORDER BY k.firstName, k.lastName")
    fun findByAge(@Param("age") age: Int): List<Kid>
    
    /**
     * Find kids born in a specific year
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE YEAR(k.dateOfBirth) = :year ORDER BY k.dateOfBirth, k.firstName, k.lastName")
    fun findByBirthYear(@Param("year") year: Int): List<Kid>
    
    /**
     * Count kids by primary parent
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.primaryParent = :parent")
    fun countByPrimaryParent(@Param("parent") parent: UserProfile): Long
    
    /**
     * Count kids by either parent
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.primaryParent = :parent OR k.secondaryParent = :parent")
    fun countByEitherParent(@Param("parent") parent: UserProfile): Long
    
    /**
     * Count active kids
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.isActive = true")
    fun countActiveKids(): Long
    
    /**
     * Count kids by gender
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.gender = :gender")
    fun countByGender(@Param("gender") gender: Gender): Long
    
    /**
     * Count kids in age range
     */
    @Query("SELECT COUNT(k) FROM Kid k WHERE k.dateOfBirth BETWEEN :maxAgeDate AND :minAgeDate")
    fun countByAgeRange(@Param("minAgeDate") minAgeDate: LocalDate, @Param("maxAgeDate") maxAgeDate: LocalDate): Long
    
    /**
     * Count kids enrolled in a specific kids service
     */
    @Query("SELECT COUNT(k) FROM Kid k JOIN k.kidsServices ks WHERE ks = :kidsService")
    fun countByKidsService(@Param("kidsService") kidsService: KidsService): Long
    
    /**
     * Find all kids with pagination
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent")
    fun findAllWithPrimaryParent(pageable: Pageable): Page<Kid>
    
    /**
     * Find active kids with pagination
     */
    @Query("SELECT k FROM Kid k LEFT JOIN FETCH k.primaryParent WHERE k.isActive = true")
    fun findActiveKidsWithPagination(pageable: Pageable): Page<Kid>
    
    /**
     * Find kids by parent with pagination
     */
    @Query("SELECT k FROM Kid k WHERE k.primaryParent = :parent OR k.secondaryParent = :parent")
    fun findByEitherParent(@Param("parent") parent: UserProfile, pageable: Pageable): Page<Kid>
    
    /**
     * Check if kid exists by name and date of birth (for duplicate prevention)
     */
    @Query("SELECT COUNT(k) > 0 FROM Kid k WHERE k.firstName = :firstName AND k.lastName = :lastName AND k.dateOfBirth = :dateOfBirth AND (:id IS NULL OR k.id != :id)")
    fun existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(@Param("firstName") firstName: String, @Param("lastName") lastName: String, @Param("dateOfBirth") dateOfBirth: LocalDate, @Param("id") id: Long?): Boolean
}