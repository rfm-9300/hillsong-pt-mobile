package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.Encounter
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

@Repository
interface EncounterRepository : JpaRepository<Encounter, Long> {
    
    /**
     * Find encounter by ID with organizer eagerly loaded
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE e.id = :id")
    fun findByIdWithOrganizer(@Param("id") id: Long): Encounter?
    
    /**
     * Find all upcoming encounters (encounters with date >= current time)
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE e.date >= :fromDate ORDER BY e.date ASC")
    fun findUpcomingEncounters(@Param("fromDate") fromDate: LocalDateTime): List<Encounter>
    
    /**
     * Find encounters organized by a specific user
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE e.organizer = :organizer ORDER BY e.date DESC")
    fun findByOrganizer(@Param("organizer") organizer: UserProfile, pageable: Pageable): Page<Encounter>
    
    /**
     * Find encounters by date range
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE e.date BETWEEN :startDate AND :endDate ORDER BY e.date ASC")
    fun findEncountersByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Encounter>
    
    /**
     * Find encounters by location (case-insensitive)
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) ORDER BY e.date ASC")
    fun findEncountersByLocationContainingIgnoreCase(@Param("location") location: String): List<Encounter>
    
    /**
     * Find encounters by title (case-insensitive)
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY e.date ASC")
    fun findEncountersByTitleContainingIgnoreCase(@Param("title") title: String): List<Encounter>
    
    /**
     * Find encounters with pagination and sorting
     */
    @Query("SELECT e FROM Encounter e LEFT JOIN FETCH e.organizer")
    fun findAllWithOrganizer(pageable: Pageable): Page<Encounter>
    
    /**
     * Count encounters organized by a user
     */
    @Query("SELECT COUNT(e) FROM Encounter e WHERE e.organizer = :organizer")
    fun countEncountersByOrganizer(@Param("organizer") organizer: UserProfile): Long
}
