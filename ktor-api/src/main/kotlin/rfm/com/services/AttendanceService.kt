package rfm.com.services

import rfm.com.data.db.attendance.*
import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.service.ServiceRepository
import rfm.com.data.db.kidsservice.KidsServiceRepository
import rfm.com.data.db.user.UserRepository
import rfm.com.data.db.kid.KidRepository
import rfm.com.plugins.Logger
import java.time.LocalDateTime

class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val serviceRepository: ServiceRepository,
    private val kidsServiceRepository: KidsServiceRepository,
    private val userRepository: UserRepository,
    private val kidRepository: KidRepository
) {
    // User check-in methods
    suspend fun checkInUserToEvent(eventId: Int, userId: Int, checkedInBy: Int, notes: String = ""): Attendance? {
        val event = eventRepository.getEventById(eventId) ?: return null
        return attendanceRepository.checkInUser(EventType.EVENT, eventId, userId, checkedInBy, notes)
    }
    
    suspend fun checkInUserToService(serviceId: Int, userId: Int, checkedInBy: Int, notes: String = ""): Attendance? {
        val service = serviceRepository.getServiceById(serviceId) ?: return null
        return attendanceRepository.checkInUser(EventType.SERVICE, serviceId, userId, checkedInBy, notes)
    }
    
    // Kid check-in methods
    suspend fun checkInKidToKidsService(kidsServiceId: Int, kidId: Int, checkedInBy: Int, notes: String = ""): Attendance? {
        val kidsService = kidsServiceRepository.getKidsServiceById(kidsServiceId) ?: return null
        return attendanceRepository.checkInKid(EventType.KIDS_SERVICE, kidsServiceId, kidId, checkedInBy, notes)
    }
    
    // Check-out methods
    suspend fun checkOutUser(attendanceId: Int, checkedOutBy: Int, notes: String = ""): Boolean {
        return attendanceRepository.checkOutUser(attendanceId, checkedOutBy, notes)
    }
    
    suspend fun checkOutKid(attendanceId: Int, checkedOutBy: Int, notes: String = ""): Boolean {
        return attendanceRepository.checkOutKid(attendanceId, checkedOutBy, notes)
    }
    
    // Query methods
    suspend fun getEventAttendance(eventId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getAttendanceByEvent(EventType.EVENT, eventId)
    }
    
    suspend fun getServiceAttendance(serviceId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getAttendanceByEvent(EventType.SERVICE, serviceId)
    }
    
    suspend fun getKidsServiceAttendance(kidsServiceId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getAttendanceByEvent(EventType.KIDS_SERVICE, kidsServiceId)
    }
    
    suspend fun getUserAttendanceHistory(
        userId: Int, 
        startDate: LocalDateTime? = null, 
        endDate: LocalDateTime? = null
    ): List<AttendanceWithDetails> {
        return attendanceRepository.getAttendanceByUser(userId, startDate, endDate)
    }
    
    suspend fun getKidAttendanceHistory(
        kidId: Int, 
        startDate: LocalDateTime? = null, 
        endDate: LocalDateTime? = null
    ): List<AttendanceWithDetails> {
        return attendanceRepository.getAttendanceByKid(kidId, startDate, endDate)
    }
    
    suspend fun getCurrentlyCheckedInToEvent(eventId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getCurrentlyCheckedIn(EventType.EVENT, eventId)
    }
    
    suspend fun getCurrentlyCheckedInToService(serviceId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getCurrentlyCheckedIn(EventType.SERVICE, serviceId)
    }
    
    suspend fun getCurrentlyCheckedInToKidsService(kidsServiceId: Int): List<AttendanceWithDetails> {
        return attendanceRepository.getCurrentlyCheckedIn(EventType.KIDS_SERVICE, kidsServiceId)
    }
    
    suspend fun getEventAttendanceStats(eventId: Int): AttendanceStats {
        return attendanceRepository.getAttendanceStats(EventType.EVENT, eventId)
    }
    
    suspend fun getServiceAttendanceStats(serviceId: Int): AttendanceStats {
        return attendanceRepository.getAttendanceStats(EventType.SERVICE, serviceId)
    }
    
    suspend fun getKidsServiceAttendanceStats(kidsServiceId: Int): AttendanceStats {
        return attendanceRepository.getAttendanceStats(EventType.KIDS_SERVICE, kidsServiceId)
    }
    
    suspend fun isUserCheckedInToEvent(eventId: Int, userId: Int): Boolean {
        return attendanceRepository.isUserCheckedIn(EventType.EVENT, eventId, userId)
    }
    
    suspend fun isUserCheckedInToService(serviceId: Int, userId: Int): Boolean {
        return attendanceRepository.isUserCheckedIn(EventType.SERVICE, serviceId, userId)
    }
    
    suspend fun isKidCheckedInToKidsService(kidsServiceId: Int, kidId: Int): Boolean {
        return attendanceRepository.isKidCheckedIn(EventType.KIDS_SERVICE, kidsServiceId, kidId)
    }
    
    suspend fun updateAttendanceStatus(attendanceId: Int, status: AttendanceStatus, notes: String = ""): Boolean {
        return attendanceRepository.updateAttendanceStatus(attendanceId, status, notes)
    }
    
    suspend fun updateAttendanceNotes(attendanceId: Int, notes: String): Boolean {
        return attendanceRepository.updateAttendanceNotes(attendanceId, notes)
    }
}