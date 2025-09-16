package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, Long> {
    
    /**
     * Find user profile by user
     */
    fun findByUser(user: User): UserProfile?
    
    /**
     * Find user profile by user ID
     */
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId")
    fun findByUserId(@Param("userId") userId: Long): UserProfile?
    
    /**
     * Find user profile by email
     */
    fun findByEmail(email: String): UserProfile?
    
    /**
     * Find user profile by phone
     */
    fun findByPhone(phone: String): UserProfile?
    
    /**
     * Find user profile by ID with user eagerly loaded
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.id = :id")
    fun findByIdWithUser(@Param("id") id: Long): UserProfile?
    
    /**
     * Find user profile by ID with organized events eagerly loaded
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.organizedEvents WHERE up.id = :id")
    fun findByIdWithOrganizedEvents(@Param("id") id: Long): UserProfile?
    
    /**
     * Find user profile by ID with authored posts eagerly loaded
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user u LEFT JOIN FETCH Post p ON p.author = u WHERE up.id = :id")
    fun findByIdWithAuthoredPosts(@Param("id") id: Long): UserProfile?
    
    /**
     * Find user profile by first name (case-insensitive)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE LOWER(up.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) ORDER BY up.firstName, up.lastName")
    fun findByFirstNameContainingIgnoreCase(@Param("firstName") firstName: String): List<UserProfile>
    
    /**
     * Find user profile by last name (case-insensitive)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE LOWER(up.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')) ORDER BY up.firstName, up.lastName")
    fun findByLastNameContainingIgnoreCase(@Param("lastName") lastName: String): List<UserProfile>
    
    /**
     * Find user profile by full name (case-insensitive)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE LOWER(CONCAT(up.firstName, ' ', up.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%')) ORDER BY up.firstName, up.lastName")
    fun findByFullNameContainingIgnoreCase(@Param("fullName") fullName: String): List<UserProfile>
    
    /**
     * Find all admin user profiles
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.isAdmin = true ORDER BY up.firstName, up.lastName")
    fun findAdminProfiles(): List<UserProfile>
    
    /**
     * Find all non-admin user profiles
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.isAdmin = false ORDER BY up.firstName, up.lastName")
    fun findNonAdminProfiles(): List<UserProfile>
    
    /**
     * Find user profiles joined within a date range
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.joinedAt BETWEEN :startDate AND :endDate ORDER BY up.joinedAt DESC")
    fun findByJoinedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<UserProfile>
    
    /**
     * Find user profiles joined within a date range with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.joinedAt BETWEEN :startDate AND :endDate ORDER BY up.joinedAt DESC")
    fun findByJoinedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<UserProfile>
    
    /**
     * Find recently joined user profiles (joined within the last N days)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.joinedAt >= :fromDate ORDER BY up.joinedAt DESC")
    fun findRecentlyJoinedProfiles(@Param("fromDate") fromDate: LocalDateTime): List<UserProfile>
    
    /**
     * Find recently joined user profiles with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.joinedAt >= :fromDate ORDER BY up.joinedAt DESC")
    fun findRecentlyJoinedProfiles(@Param("fromDate") fromDate: LocalDateTime, pageable: Pageable): Page<UserProfile>
    
    /**
     * Find user profiles with profile images
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.imagePath IS NOT NULL AND up.imagePath != '' ORDER BY up.firstName, up.lastName")
    fun findProfilesWithImages(): List<UserProfile>
    
    /**
     * Find user profiles without profile images
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.imagePath IS NULL OR up.imagePath = '' ORDER BY up.firstName, up.lastName")
    fun findProfilesWithoutImages(): List<UserProfile>
    
    /**
     * Find user profiles by phone pattern (partial match)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.phone LIKE CONCAT('%', :phonePattern, '%') ORDER BY up.firstName, up.lastName")
    fun findByPhoneContaining(@Param("phonePattern") phonePattern: String): List<UserProfile>
    
    /**
     * Find user profiles that have organized events
     */
    @Query("SELECT DISTINCT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE SIZE(up.organizedEvents) > 0 ORDER BY up.firstName, up.lastName")
    fun findProfilesWithOrganizedEvents(): List<UserProfile>
    
    /**
     * Find user profiles that have authored posts
     */
    @Query("SELECT DISTINCT up FROM UserProfile up LEFT JOIN FETCH up.user u LEFT JOIN Post p ON p.author = u WHERE p.id IS NOT NULL ORDER BY up.firstName, up.lastName")
    fun findProfilesWithAuthoredPosts(): List<UserProfile>
    
    /**
     * Find user profiles ordered by join date (newest first)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user ORDER BY up.joinedAt DESC")
    fun findAllOrderByJoinedAtDesc(): List<UserProfile>
    
    /**
     * Find user profiles ordered by name
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user ORDER BY up.firstName, up.lastName")
    fun findAllOrderByName(): List<UserProfile>
    
    /**
     * Count admin user profiles
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isAdmin = true")
    fun countAdminProfiles(): Long
    
    /**
     * Count non-admin user profiles
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.isAdmin = false")
    fun countNonAdminProfiles(): Long
    
    /**
     * Count user profiles joined within a date range
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.joinedAt BETWEEN :startDate AND :endDate")
    fun countByJoinedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count recently joined user profiles
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.joinedAt >= :fromDate")
    fun countRecentlyJoinedProfiles(@Param("fromDate") fromDate: LocalDateTime): Long
    
    /**
     * Count user profiles with profile images
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.imagePath IS NOT NULL AND up.imagePath != ''")
    fun countProfilesWithImages(): Long
    
    /**
     * Count user profiles without profile images
     */
    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.imagePath IS NULL OR up.imagePath = ''")
    fun countProfilesWithoutImages(): Long
    
    /**
     * Count user profiles that have organized events
     */
    @Query("SELECT COUNT(DISTINCT up) FROM UserProfile up WHERE SIZE(up.organizedEvents) > 0")
    fun countProfilesWithOrganizedEvents(): Long
    
    /**
     * Count user profiles that have authored posts
     */
    @Query("SELECT COUNT(DISTINCT up) FROM UserProfile up LEFT JOIN up.user u LEFT JOIN Post p ON p.author = u WHERE p.id IS NOT NULL")
    fun countProfilesWithAuthoredPosts(): Long
    
    /**
     * Count organized events by user profile
     */
    @Query("SELECT COUNT(e) FROM UserProfile up JOIN up.organizedEvents e WHERE up = :userProfile")
    fun countOrganizedEventsByProfile(@Param("userProfile") userProfile: UserProfile): Long
    
    /**
     * Count authored posts by user profile
     */
    @Query("SELECT COUNT(p) FROM UserProfile up LEFT JOIN up.user u LEFT JOIN Post p ON p.author = u WHERE up = :userProfile")
    fun countAuthoredPostsByProfile(@Param("userProfile") userProfile: UserProfile): Long
    
    /**
     * Check if email exists (excluding specific profile ID)
     */
    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.email = :email AND (:id IS NULL OR up.id != :id)")
    fun existsByEmailAndIdNot(@Param("email") email: String, @Param("id") id: Long?): Boolean
    
    /**
     * Check if phone exists (excluding specific profile ID)
     */
    @Query("SELECT COUNT(up) > 0 FROM UserProfile up WHERE up.phone = :phone AND (:id IS NULL OR up.id != :id)")
    fun existsByPhoneAndIdNot(@Param("phone") phone: String, @Param("id") id: Long?): Boolean
    
    /**
     * Find all user profiles with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user")
    fun findAllWithUser(pageable: Pageable): Page<UserProfile>
    
    /**
     * Find admin profiles with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.isAdmin = true")
    fun findAdminProfilesWithPagination(pageable: Pageable): Page<UserProfile>
    
    /**
     * Find non-admin profiles with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.isAdmin = false")
    fun findNonAdminProfilesWithPagination(pageable: Pageable): Page<UserProfile>
    
    /**
     * Search user profiles by name or email (case-insensitive)
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE " +
           "LOWER(up.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(up.firstName, ' ', up.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY up.firstName, up.lastName")
    fun searchProfiles(@Param("searchTerm") searchTerm: String): List<UserProfile>
    
    /**
     * Search user profiles with pagination
     */
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE " +
           "LOWER(up.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(up.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(up.firstName, ' ', up.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY up.firstName, up.lastName")
    fun searchProfiles(@Param("searchTerm") searchTerm: String, pageable: Pageable): Page<UserProfile>
}