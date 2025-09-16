package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.*
import java.time.LocalDateTime

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Long> {
    
    /**
     * Find attendance by ID with user eagerly loaded
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.id = :id")
    fun findByIdWithUser(@Param("id") id: Long): Attendance?
    
    /**
     * Find attendance by ID with all relationships eagerly loaded
     */
    @Query("SELECT DISTINCT a FROM Attendance a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.event " +
           "LEFT JOIN FETCH a.service " +
           "LEFT JOIN FETCH a.kidsService " +
           "WHERE a.id = :id")
    fun findByIdWithAllRelationships(@Param("id") id: Long): Attendance?
    
    /**
     * Find all attendance records for a specific user
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user ORDER BY a.checkInTime DESC")
    fun findByUser(@Param("user") user: User): List<Attendance>
    
    /**
     * Find all attendance records for a specific user with pagination
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user ORDER BY a.checkInTime DESC")
    fun findByUser(@Param("user") user: User, pageable: Pageable): Page<Attendance>
    
    /**
     * Find all attendance records for a specific event
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.event = :event ORDER BY a.checkInTime DESC")
    fun findByEvent(@Param("event") event: Event): List<Attendance>
    
    /**
     * Find all attendance records for a specific service
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.service = :service ORDER BY a.checkInTime DESC")
    fun findByService(@Param("service") service: Service): List<Attendance>
    
    /**
     * Find all attendance records for a specific kids service
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.kidsService = :kidsService ORDER BY a.checkInTime DESC")
    fun findByKidsService(@Param("kidsService") kidsService: KidsService): List<Attendance>
    
    /**
     * Find attendance records by type
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.attendanceType = :type ORDER BY a.checkInTime DESC")
    fun findByAttendanceType(@Param("type") type: AttendanceType): List<Attendance>
    
    /**
     * Find attendance records by status
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.status = :status ORDER BY a.checkInTime DESC")
    fun findByStatus(@Param("status") status: AttendanceStatus): List<Attendance>
    
    /**
     * Find attendance records by type and status
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.attendanceType = :type AND a.status = :status ORDER BY a.checkInTime DESC")
    fun findByAttendanceTypeAndStatus(@Param("type") type: AttendanceType, @Param("status") status: AttendanceStatus): List<Attendance>
    
    /**
     * Find attendance records for a user and event
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.event = :event ORDER BY a.checkInTime DESC")
    fun findByUserAndEvent(@Param("user") user: User, @Param("event") event: Event): List<Attendance>
    
    /**
     * Find attendance records for a user and service
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.service = :service ORDER BY a.checkInTime DESC")
    fun findByUserAndService(@Param("user") user: User, @Param("service") service: Service): List<Attendance>
    
    /**
     * Find attendance records for a user and kids service
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.kidsService = :kidsService ORDER BY a.checkInTime DESC")
    fun findByUserAndKidsService(@Param("user") user: User, @Param("kidsService") kidsService: KidsService): List<Attendance>
    
    /**
     * Find attendance records within a date range
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.checkInTime BETWEEN :startDate AND :endDate ORDER BY a.checkInTime DESC")
    fun findByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Attendance>
    
    /**
     * Find attendance records within a date range with pagination
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.checkInTime BETWEEN :startDate AND :endDate ORDER BY a.checkInTime DESC")
    fun findByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<Attendance>
    
    /**
     * Find currently checked-in users (status = CHECKED_IN)
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.status = 'CHECKED_IN' ORDER BY a.checkInTime DESC")
    fun findCurrentlyCheckedIn(): List<Attendance>
    
    /**
     * Find users who haven't checked out (checkOutTime is null)
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.checkOutTime IS NULL ORDER BY a.checkInTime DESC")
    fun findUsersNotCheckedOut(): List<Attendance>
    
    /**
     * Find attendance records for a specific user within a date range
     */
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.checkInTime BETWEEN :startDate AND :endDate ORDER BY a.checkInTime DESC")
    fun findByUserAndCheckInTimeBetween(@Param("user") user: User, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Attendance>
    
    /**
     * Find attendance records for a specific event within a date range
     */
    @Query("SELECT a FROM Attendance a LEFT JOIN FETCH a.user WHERE a.event = :event AND a.checkInTime BETWEEN :startDate AND :endDate ORDER BY a.checkInTime DESC")
    fun findByEventAndCheckInTimeBetween(@Param("event") event: Event, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Attendance>
    
    /**
     * Count attendance records for a specific user
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user")
    fun countByUser(@Param("user") user: User): Long
    
    /**
     * Count attendance records for a specific event
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event = :event")
    fun countByEvent(@Param("event") event: Event): Long
    
    /**
     * Count attendance records for a specific service
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.service = :service")
    fun countByService(@Param("service") service: Service): Long
    
    /**
     * Count attendance records for a specific kids service
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.kidsService = :kidsService")
    fun countByKidsService(@Param("kidsService") kidsService: KidsService): Long
    
    /**
     * Count attendance records by type
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.attendanceType = :type")
    fun countByAttendanceType(@Param("type") type: AttendanceType): Long
    
    /**
     * Count attendance records by status
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.status = :status")
    fun countByStatus(@Param("status") status: AttendanceStatus): Long
    
    /**
     * Count attendance records within a date range
     */
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :startDate AND :endDate")
    fun countByCheckInTimeBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Find attendance statistics by type for a date range
     */
    @Query("SELECT a.attendanceType, COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :startDate AND :endDate GROUP BY a.attendanceType")
    fun findAttendanceStatsByType(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Array<Any>>
    
    /**
     * Find attendance statistics by status for a date range
     */
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :startDate AND :endDate GROUP BY a.status")
    fun findAttendanceStatsByStatus(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Array<Any>>
    
    /**
     * Find most frequent attendees (users with most attendance records)
     */
    @Query("SELECT a.user, COUNT(a) as attendanceCount FROM Attendance a GROUP BY a.user ORDER BY attendanceCount DESC")
    fun findMostFrequentAttendees(pageable: Pageable): Page<Array<Any>>
}