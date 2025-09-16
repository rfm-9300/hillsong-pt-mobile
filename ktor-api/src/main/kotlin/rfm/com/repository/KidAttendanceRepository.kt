package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.Kid
import rfm.com.entity.KidAttendance
import rfm.com.entity.KidsService
import java.time.LocalDateTime

@Repository
interface KidAttendanceRepository : JpaRepository<KidAttendance, Long> {
    
    /**
     * Find kid attendance by ID with kid eagerly loaded
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid WHERE ka.id = :id")
    fun findByIdWithKid(@Param("id") id: Long): KidAttendance?
    
    /**
     * Find kid attendance by ID with kids service eagerly loaded
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kidsService WHERE ka.id = :id")
    fun findByIdWithKidsService(@Param("id") id: Long): KidAttendance?
    
    /**
     * Find kid attendance by ID with all relationships eagerly loaded
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): KidAttendance?
    
    /**
     * Find all attendance records for a specific kid
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kidsService WHERE ka.kid = :kid ORDER BY ka.checkInTime DESC")
    fun findByKid(@Param("kid") kid: Kid): List<KidAttendance>
    
    /**
     * Find all attendance records for a specific kid with pagination
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kidsService WHERE ka.kid = :kid ORDER BY ka.checkInTime DESC")
    fun findByKid(@Param("kid") kid: Kid, pageable: Pageable): Page<KidAttendance>
    
    /**
     * Find all attendance records for a specific kids service
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid WHERE ka.kidsService = :kidsService ORDER BY ka.checkInTime DESC")
    fun findByKidsService(@Param("kidsService") kidsService: KidsService): List<KidAttendance>
    
    /**
     * Find all attendance records for a specific kids service with pagination
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid WHERE ka.kidsService = :kidsService ORDER BY ka.checkInTime DESC")
    fun findByKidsService(@Param("kidsService") kidsService: KidsService, pageable: Pageable): Page<KidAttendance>
    
    /**
     * Find attendance records by status
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.status = :status ORDER BY ka.checkInTime DESC")
    fun findByStatus(@Param("status") status: AttendanceStatus): List<KidAttendance>
    
    /**
     * Find attendance records for a kid and kids service
     */
    @Query("SELECT ka FROM KidAttendance ka WHERE ka.kid = :kid AND ka.kidsService = :kidsService ORDER BY ka.checkInTime DESC")
    fun findByKidAndKidsService(@Param("kid") kid: Kid, @Param("kidsService") kidsService: KidsService): List<KidAttendance>
    
    /**
     * Find attendance records within a date range
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.checkInTime BETWEEN :startDate AND :endDate ORDER BY ka.checkInTime DESC")
    fun findByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<KidAttendance>
    
    /**
     * Find attendance records within a date range with pagination
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.checkInTime BETWEEN :startDate AND :endDate ORDER BY ka.checkInTime DESC")
    fun findByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<KidAttendance>
    
    /**
     * Find currently checked-in kids (status = CHECKED_IN)
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.status = 'CHECKED_IN' ORDER BY ka.checkInTime DESC")
    fun findCurrentlyCheckedInKids(): List<KidAttendance>
    
    /**
     * Find kids who haven't checked out (checkOutTime is null)
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.checkOutTime IS NULL ORDER BY ka.checkInTime DESC")
    fun findKidsNotCheckedOut(): List<KidAttendance>
    
    /**
     * Find attendance records for a specific kid within a date range
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kidsService WHERE ka.kid = :kid AND ka.checkInTime BETWEEN :startDate AND :endDate ORDER BY ka.checkInTime DESC")
    fun findByKidAndCheckInTimeBetween(@Param("kid") kid: Kid, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<KidAttendance>
    
    /**
     * Find attendance records for a specific kids service within a date range
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid WHERE ka.kidsService = :kidsService AND ka.checkInTime BETWEEN :startDate AND :endDate ORDER BY ka.checkInTime DESC")
    fun findByKidsServiceAndCheckInTimeBetween(@Param("kidsService") kidsService: KidsService, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<KidAttendance>
    
    /**
     * Find attendance records by who checked them in
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.checkedInBy = :checkedInBy ORDER BY ka.checkInTime DESC")
    fun findByCheckedInBy(@Param("checkedInBy") checkedInBy: String): List<KidAttendance>
    
    /**
     * Find attendance records by who checked them out
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.checkedOutBy = :checkedOutBy ORDER BY ka.checkInTime DESC")
    fun findByCheckedOutBy(@Param("checkedOutBy") checkedOutBy: String): List<KidAttendance>
    
    /**
     * Find attendance records with notes
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid LEFT JOIN FETCH ka.kidsService WHERE ka.notes IS NOT NULL AND ka.notes != '' ORDER BY ka.checkInTime DESC")
    fun findAttendanceWithNotes(): List<KidAttendance>
    
    /**
     * Find most recent attendance record for a kid in a specific service
     */
    @Query("SELECT ka FROM KidAttendance ka WHERE ka.kid = :kid AND ka.kidsService = :kidsService ORDER BY ka.checkInTime DESC LIMIT 1")
    fun findMostRecentByKidAndKidsService(@Param("kid") kid: Kid, @Param("kidsService") kidsService: KidsService): KidAttendance?
    
    /**
     * Find attendance records for today for a specific kids service
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kid WHERE ka.kidsService = :kidsService AND CAST(ka.checkInTime AS date) = CURRENT_DATE ORDER BY ka.checkInTime DESC")
    fun findTodayAttendanceForKidsService(@Param("kidsService") kidsService: KidsService): List<KidAttendance>
    
    /**
     * Find attendance records for today for a specific kid
     */
    @Query("SELECT ka FROM KidAttendance ka LEFT JOIN FETCH ka.kidsService WHERE ka.kid = :kid AND CAST(ka.checkInTime AS date) = CURRENT_DATE ORDER BY ka.checkInTime DESC")
    fun findTodayAttendanceForKid(@Param("kid") kid: Kid): List<KidAttendance>
    
    /**
     * Count attendance records for a specific kid
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.kid = :kid")
    fun countByKid(@Param("kid") kid: Kid): Long
    
    /**
     * Count attendance records for a specific kids service
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.kidsService = :kidsService")
    fun countByKidsService(@Param("kidsService") kidsService: KidsService): Long
    
    /**
     * Count attendance records by status
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.status = :status")
    fun countByStatus(@Param("status") status: AttendanceStatus): Long
    
    /**
     * Count attendance records within a date range
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.checkInTime BETWEEN :startDate AND :endDate")
    fun countByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count attendance records for a kid within a date range
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.kid = :kid AND ka.checkInTime BETWEEN :startDate AND :endDate")
    fun countByKidAndCheckInTimeBetween(@Param("kid") kid: Kid, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count attendance records for a kids service within a date range
     */
    @Query("SELECT COUNT(ka) FROM KidAttendance ka WHERE ka.kidsService = :kidsService AND ka.checkInTime BETWEEN :startDate AND :endDate")
    fun countByKidsServiceAndCheckInTimeBetween(@Param("kidsService") kidsService: KidsService, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Find attendance statistics by status for a date range
     */
    @Query("SELECT ka.status, COUNT(ka) FROM KidAttendance ka WHERE ka.checkInTime BETWEEN :startDate AND :endDate GROUP BY ka.status")
    fun findAttendanceStatsByStatus(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Array<Any>>
    
    /**
     * Find most frequent kid attendees (kids with most attendance records)
     */
    @Query("SELECT ka.kid, COUNT(ka) as attendanceCount FROM KidAttendance ka GROUP BY ka.kid ORDER BY attendanceCount DESC")
    fun findMostFrequentKidAttendees(pageable: Pageable): Page<Array<Any>>
    
    /**
     * Find average attendance duration for a kids service
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, ka.checkInTime, ka.checkOutTime)) FROM KidAttendance ka WHERE ka.kidsService = :kidsService AND ka.checkOutTime IS NOT NULL")
    fun findAverageAttendanceDurationForKidsService(@Param("kidsService") kidsService: KidsService): Double?
    
    /**
     * Check if kid is currently checked in to any service
     */
    @Query("SELECT COUNT(ka) > 0 FROM KidAttendance ka WHERE ka.kid = :kid AND ka.status = 'CHECKED_IN' AND ka.checkOutTime IS NULL")
    fun isKidCurrentlyCheckedIn(@Param("kid") kid: Kid): Boolean
    
    /**
     * Check if kid is currently checked in to a specific service
     */
    @Query("SELECT COUNT(ka) > 0 FROM KidAttendance ka WHERE ka.kid = :kid AND ka.kidsService = :kidsService AND ka.status = 'CHECKED_IN' AND ka.checkOutTime IS NULL")
    fun isKidCurrentlyCheckedInToService(@Param("kid") kid: Kid, @Param("kidsService") kidsService: KidsService): Boolean
}