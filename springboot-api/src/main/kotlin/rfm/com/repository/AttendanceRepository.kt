package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Attendance
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.AttendanceType
import java.time.LocalDateTime

@Repository
interface AttendanceRepository : MongoRepository<Attendance, String> {
    
    fun findByUserId(userId: String): List<Attendance>
    
    fun findByUserId(userId: String, pageable: Pageable): Page<Attendance>
    
    fun findByEventId(eventId: String): List<Attendance>
    
    fun findByServiceId(serviceId: String): List<Attendance>
    
    fun findByKidsServiceId(kidsServiceId: String): List<Attendance>
    
    fun findByAttendanceType(type: AttendanceType): List<Attendance>
    
    fun findByStatus(status: AttendanceStatus): List<Attendance>
    
    fun findByAttendanceTypeAndStatus(type: AttendanceType, status: AttendanceStatus): List<Attendance>
    
    fun findByUserIdAndEventId(userId: String, eventId: String): List<Attendance>
    
    fun findByUserIdAndServiceId(userId: String, serviceId: String): List<Attendance>
    
    fun findByUserIdAndKidsServiceId(userId: String, kidsServiceId: String): List<Attendance>
    
    @Query("{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}", sort = "{'checkInTime': -1}")
    fun findByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Attendance>
    
    @Query("{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}")
    fun findByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<Attendance>
    
    fun findByStatusOrderByCheckInTimeDesc(status: AttendanceStatus): List<Attendance>
    
    fun findByCheckOutTimeIsNullOrderByCheckInTimeDesc(): List<Attendance>
    
    fun countByUserId(userId: String): Long
    
    fun countByEventId(eventId: String): Long
    
    fun countByServiceId(serviceId: String): Long
    
    fun countByKidsServiceId(kidsServiceId: String): Long
    
    fun countByAttendanceType(type: AttendanceType): Long
    
    fun countByStatus(status: AttendanceStatus): Long
    
    @Query(value = "{'checkInTime': {'\$gte': ?0, '\$lte': ?1}}", count = true)
    fun countByCheckInTimeBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
}