package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.KidAttendance
import java.time.LocalDateTime

@Repository
interface KidAttendanceRepository : MongoRepository<KidAttendance, String> {

    fun findByKidId(kidId: String): List<KidAttendance>
    
    fun findByKidId(kidId: String, pageable: Pageable): Page<KidAttendance>
    
    fun findByKidsServiceId(kidsServiceId: String): List<KidAttendance>
    
    fun findByKidsServiceId(kidsServiceId: String, pageable: Pageable): Page<KidAttendance>
    
    fun findByStatus(status: AttendanceStatus): List<KidAttendance>
    
    fun findByKidIdAndKidsServiceId(kidId: String, kidsServiceId: String): List<KidAttendance>
    
    @Query("{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'checkInTime': -1}")
    fun findByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<KidAttendance>
    
    @Query("{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}")
    fun findByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<KidAttendance>
    
    @Query("{'status': 'CHECKED_IN'}", sort = "{'checkInTime': -1}")
    fun findCurrentlyCheckedInKids(): List<KidAttendance>
    
    fun findByCheckOutTimeIsNullOrderByCheckInTimeDesc(): List<KidAttendance>
    
    fun findByCheckedInBy(checkedInBy: String): List<KidAttendance>
    
    fun findByCheckedOutBy(checkedOutBy: String): List<KidAttendance>
    
    @Query("{'notes': {'\$ne': null, '\$ne': ''}}", sort = "{'checkInTime': -1}")
    fun findAttendanceWithNotes(): List<KidAttendance>
    
    fun countByKidId(kidId: String): Long
    
    fun countByKidsServiceId(kidsServiceId: String): Long
    
    fun countByStatus(status: AttendanceStatus): Long
    
    @Query(value = "{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}", count = true)
    fun countByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long

    @Query("{'kidId': ?0, 'status': 'CHECKED_IN', 'checkOutTime': null}", exists = true)
    fun isKidCurrentlyCheckedIn(kidId: String): Boolean

    @Query("{'kidId': ?0, 'kidsServiceId': ?1, 'status': 'CHECKED_IN', 'checkOutTime': null}", exists = true)
    fun isKidCurrentlyCheckedInToService(kidId: String, kidsServiceId: String): Boolean
}
