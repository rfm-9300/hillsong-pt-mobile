package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.*
import java.time.LocalDateTime
import java.time.LocalTime

class AttendanceRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var attendanceRepository: AttendanceRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var testUser: User
    private lateinit var testEvent: Event
    private lateinit var testService: Service
    private lateinit var testKidsService: KidsService
    private lateinit var organizer: UserProfile
    private lateinit var eventAttendance: Attendance
    private lateinit var serviceAttendance: Attendance
    
    @BeforeEach
    fun setUp() {
        // Create test user
        testUser = User(
            email = "attendee@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        entityManager.persistAndFlush(testUser)
        
        // Create organizer user and profile
        val organizerUser = User(
            email = "organizer@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedOrganizerUser = entityManager.persistAndFlush(organizerUser)
        
        organizer = UserProfile(
            user = savedOrganizerUser,
            firstName = "Event",
            lastName = "Organizer",
            email = "organizer@example.com",
            phone = "1234567890",
            isAdmin = true
        )
        entityManager.persistAndFlush(organizer)
        
        // Create test event
        testEvent = Event(
            title = "Test Event",
            description = "Test event for attendance",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizer = organizer,
            maxAttendees = 50
        )
        entityManager.persistAndFlush(testEvent)
        
        // Create test service
        testService = Service(
            name = "Sunday Service",
            description = "Weekly Sunday service",
            dayOfWeek = 0, // Sunday
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            isActive = true
        )
        entityManager.persistAndFlush(testService)
        
        // Create test kids service
        testKidsService = KidsService(
            name = "Kids Church",
            description = "Children's service",
            ageGroup = AgeGroup.ELEMENTARY,
            dayOfWeek = 0,
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 0),
            maxCapacity = 20,
            isActive = true
        )
        entityManager.persistAndFlush(testKidsService)
        
        // Create attendance records
        eventAttendance = Attendance(
            user = testUser,
            event = testEvent,
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now().minusHours(2)
        )
        entityManager.persistAndFlush(eventAttendance)
        
        serviceAttendance = Attendance(
            user = testUser,
            service = testService,
            attendanceType = AttendanceType.SERVICE,
            status = AttendanceStatus.CHECKED_OUT,
            checkInTime = LocalDateTime.now().minusHours(3),
            checkOutTime = LocalDateTime.now().minusHours(1)
        )
        entityManager.persistAndFlush(serviceAttendance)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByIdWithUser should eagerly load user`() {
        // When
        val foundAttendance = attendanceRepository.findByIdWithUser(eventAttendance.id!!)
        
        // Then
        assertNotNull(foundAttendance)
        assertNotNull(foundAttendance?.user)
        assertEquals("attendee@example.com", foundAttendance?.user?.email)
    }
    
    @Test
    fun `findByIdWithAllRelationships should eagerly load all relationships`() {
        // When
        val foundAttendance = attendanceRepository.findByIdWithAllRelationships(eventAttendance.id!!)
        
        // Then
        assertNotNull(foundAttendance)
        assertNotNull(foundAttendance?.user)
        assertNotNull(foundAttendance?.event)
        assertEquals("attendee@example.com", foundAttendance?.user?.email)
        assertEquals("Test Event", foundAttendance?.event?.title)
    }
    
    @Test
    fun `findByUser should return all attendance records for user`() {
        // When
        val userAttendances = attendanceRepository.findByUser(testUser)
        
        // Then
        assertEquals(2, userAttendances.size)
        assertTrue(userAttendances.all { it.user.id == testUser.id })
        assertTrue(userAttendances.any { it.attendanceType == AttendanceType.EVENT })
        assertTrue(userAttendances.any { it.attendanceType == AttendanceType.SERVICE })
    }
    
    @Test
    fun `findByUser with pagination should return paginated results`() {
        // When
        val page = attendanceRepository.findByUser(testUser, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
        assertEquals(2, page.totalPages)
        assertTrue(page.content.all { it.user.id == testUser.id })
    }
    
    @Test
    fun `findByEvent should return attendance records for specific event`() {
        // When
        val eventAttendances = attendanceRepository.findByEvent(testEvent)
        
        // Then
        assertTrue(eventAttendances.isNotEmpty())
        assertTrue(eventAttendances.all { it.event?.id == testEvent.id })
        assertTrue(eventAttendances.any { it.user.email == "attendee@example.com" })
    }
    
    @Test
    fun `findByService should return attendance records for specific service`() {
        // When
        val serviceAttendances = attendanceRepository.findByService(testService)
        
        // Then
        assertTrue(serviceAttendances.isNotEmpty())
        assertTrue(serviceAttendances.all { it.service?.id == testService.id })
        assertTrue(serviceAttendances.any { it.user.email == "attendee@example.com" })
    }
    
    @Test
    fun `findByKidsService should return attendance records for specific kids service`() {
        // Given - create kids service attendance
        val kidsAttendance = Attendance(
            user = testUser,
            kidsService = testKidsService,
            attendanceType = AttendanceType.KIDS_SERVICE,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now().minusMinutes(30)
        )
        entityManager.persistAndFlush(kidsAttendance)
        entityManager.clear()
        
        // When
        val kidsAttendances = attendanceRepository.findByKidsService(testKidsService)
        
        // Then
        assertTrue(kidsAttendances.isNotEmpty())
        assertTrue(kidsAttendances.all { it.kidsService?.id == testKidsService.id })
        assertTrue(kidsAttendances.any { it.user.email == "attendee@example.com" })
    }
    
    @Test
    fun `findByAttendanceType should return records of specific type`() {
        // When
        val eventAttendances = attendanceRepository.findByAttendanceType(AttendanceType.EVENT)
        val serviceAttendances = attendanceRepository.findByAttendanceType(AttendanceType.SERVICE)
        
        // Then
        assertTrue(eventAttendances.isNotEmpty())
        assertTrue(eventAttendances.all { it.attendanceType == AttendanceType.EVENT })
        
        assertTrue(serviceAttendances.isNotEmpty())
        assertTrue(serviceAttendances.all { it.attendanceType == AttendanceType.SERVICE })
    }
    
    @Test
    fun `findByStatus should return records with specific status`() {
        // When
        val checkedInAttendances = attendanceRepository.findByStatus(AttendanceStatus.CHECKED_IN)
        val checkedOutAttendances = attendanceRepository.findByStatus(AttendanceStatus.CHECKED_OUT)
        
        // Then
        assertTrue(checkedInAttendances.isNotEmpty())
        assertTrue(checkedInAttendances.all { it.status == AttendanceStatus.CHECKED_IN })
        assertTrue(checkedInAttendances.any { it.attendanceType == AttendanceType.EVENT })
        
        assertTrue(checkedOutAttendances.isNotEmpty())
        assertTrue(checkedOutAttendances.all { it.status == AttendanceStatus.CHECKED_OUT })
        assertTrue(checkedOutAttendances.any { it.attendanceType == AttendanceType.SERVICE })
    }
    
    @Test
    fun `findByAttendanceTypeAndStatus should return records matching both criteria`() {
        // When
        val eventCheckedIn = attendanceRepository.findByAttendanceTypeAndStatus(
            AttendanceType.EVENT, 
            AttendanceStatus.CHECKED_IN
        )
        val serviceCheckedOut = attendanceRepository.findByAttendanceTypeAndStatus(
            AttendanceType.SERVICE, 
            AttendanceStatus.CHECKED_OUT
        )
        
        // Then
        assertTrue(eventCheckedIn.isNotEmpty())
        assertTrue(eventCheckedIn.all { 
            it.attendanceType == AttendanceType.EVENT && it.status == AttendanceStatus.CHECKED_IN 
        })
        
        assertTrue(serviceCheckedOut.isNotEmpty())
        assertTrue(serviceCheckedOut.all { 
            it.attendanceType == AttendanceType.SERVICE && it.status == AttendanceStatus.CHECKED_OUT 
        })
    }
    
    @Test
    fun `findByUserAndEvent should return attendance for specific user and event`() {
        // When
        val userEventAttendances = attendanceRepository.findByUserAndEvent(testUser, testEvent)
        
        // Then
        assertTrue(userEventAttendances.isNotEmpty())
        assertTrue(userEventAttendances.all { 
            it.user.id == testUser.id && it.event?.id == testEvent.id 
        })
    }
    
    @Test
    fun `findByUserAndService should return attendance for specific user and service`() {
        // When
        val userServiceAttendances = attendanceRepository.findByUserAndService(testUser, testService)
        
        // Then
        assertTrue(userServiceAttendances.isNotEmpty())
        assertTrue(userServiceAttendances.all { 
            it.user.id == testUser.id && it.service?.id == testService.id 
        })
    }
    
    @Test
    fun `findByCheckInTimeBetween should return records within date range`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().minusHours(1)
        
        // When
        val attendancesInRange = attendanceRepository.findByCheckInTimeBetween(startDate, endDate)
        
        // Then
        assertTrue(attendancesInRange.isNotEmpty())
        assertTrue(attendancesInRange.all { 
            it.checkInTime.isAfter(startDate) && it.checkInTime.isBefore(endDate) 
        })
        assertEquals(2, attendancesInRange.size) // Both our test records
    }
    
    @Test
    fun `findByCheckInTimeBetween with pagination should return paginated results`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().minusHours(1)
        
        // When
        val page = attendanceRepository.findByCheckInTimeBetween(startDate, endDate, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
    }
    
    @Test
    fun `findCurrentlyCheckedIn should return users still checked in`() {
        // When
        val currentlyCheckedIn = attendanceRepository.findCurrentlyCheckedIn()
        
        // Then
        assertTrue(currentlyCheckedIn.isNotEmpty())
        assertTrue(currentlyCheckedIn.all { it.status == AttendanceStatus.CHECKED_IN })
        assertTrue(currentlyCheckedIn.any { it.attendanceType == AttendanceType.EVENT })
    }
    
    @Test
    fun `findUsersNotCheckedOut should return users without checkout time`() {
        // When
        val notCheckedOut = attendanceRepository.findUsersNotCheckedOut()
        
        // Then
        assertTrue(notCheckedOut.isNotEmpty())
        assertTrue(notCheckedOut.all { it.checkOutTime == null })
        assertTrue(notCheckedOut.any { it.attendanceType == AttendanceType.EVENT })
    }
    
    @Test
    fun `findByUserAndCheckInTimeBetween should return user records in date range`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().minusHours(1)
        
        // When
        val userAttendancesInRange = attendanceRepository.findByUserAndCheckInTimeBetween(
            testUser, startDate, endDate
        )
        
        // Then
        assertEquals(2, userAttendancesInRange.size)
        assertTrue(userAttendancesInRange.all { it.user.id == testUser.id })
        assertTrue(userAttendancesInRange.all { 
            it.checkInTime.isAfter(startDate) && it.checkInTime.isBefore(endDate) 
        })
    }
    
    @Test
    fun `countByUser should return correct count`() {
        // When
        val userAttendanceCount = attendanceRepository.countByUser(testUser)
        
        // Then
        assertEquals(2, userAttendanceCount)
    }
    
    @Test
    fun `countByEvent should return correct count`() {
        // When
        val eventAttendanceCount = attendanceRepository.countByEvent(testEvent)
        
        // Then
        assertEquals(1, eventAttendanceCount)
    }
    
    @Test
    fun `countByService should return correct count`() {
        // When
        val serviceAttendanceCount = attendanceRepository.countByService(testService)
        
        // Then
        assertEquals(1, serviceAttendanceCount)
    }
    
    @Test
    fun `countByAttendanceType should return correct count`() {
        // When
        val eventCount = attendanceRepository.countByAttendanceType(AttendanceType.EVENT)
        val serviceCount = attendanceRepository.countByAttendanceType(AttendanceType.SERVICE)
        
        // Then
        assertEquals(1, eventCount)
        assertEquals(1, serviceCount)
    }
    
    @Test
    fun `countByStatus should return correct count`() {
        // When
        val checkedInCount = attendanceRepository.countByStatus(AttendanceStatus.CHECKED_IN)
        val checkedOutCount = attendanceRepository.countByStatus(AttendanceStatus.CHECKED_OUT)
        
        // Then
        assertEquals(1, checkedInCount)
        assertEquals(1, checkedOutCount)
    }
    
    @Test
    fun `countByCheckInTimeBetween should return correct count`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().minusHours(1)
        
        // When
        val countInRange = attendanceRepository.countByCheckInTimeBetween(startDate, endDate)
        
        // Then
        assertEquals(2, countInRange)
    }
    
    @Test
    fun `findAttendanceStatsByType should return statistics grouped by type`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val stats = attendanceRepository.findAttendanceStatsByType(startDate, endDate)
        
        // Then
        assertTrue(stats.isNotEmpty())
        assertEquals(2, stats.size) // EVENT and SERVICE types
        
        val eventStat = stats.find { it[0] == AttendanceType.EVENT }
        val serviceStat = stats.find { it[0] == AttendanceType.SERVICE }
        
        assertNotNull(eventStat)
        assertNotNull(serviceStat)
        assertEquals(1L, eventStat!![1])
        assertEquals(1L, serviceStat!![1])
    }
    
    @Test
    fun `findAttendanceStatsByStatus should return statistics grouped by status`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(4)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val stats = attendanceRepository.findAttendanceStatsByStatus(startDate, endDate)
        
        // Then
        assertTrue(stats.isNotEmpty())
        assertEquals(2, stats.size) // CHECKED_IN and CHECKED_OUT statuses
        
        val checkedInStat = stats.find { it[0] == AttendanceStatus.CHECKED_IN }
        val checkedOutStat = stats.find { it[0] == AttendanceStatus.CHECKED_OUT }
        
        assertNotNull(checkedInStat)
        assertNotNull(checkedOutStat)
        assertEquals(1L, checkedInStat!![1])
        assertEquals(1L, checkedOutStat!![1])
    }
    
    @Test
    fun `findMostFrequentAttendees should return users ordered by attendance count`() {
        // When
        val page = attendanceRepository.findMostFrequentAttendees(PageRequest.of(0, 10))
        
        // Then
        assertTrue(page.content.isNotEmpty())
        val topAttendee = page.content.first()
        assertEquals(testUser.id, (topAttendee[0] as User).id)
        assertEquals(2L, topAttendee[1]) // 2 attendance records
    }
    
    @Test
    fun `save should persist attendance with all fields`() {
        // Given
        val newAttendance = Attendance(
            user = testUser,
            event = testEvent,
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            notes = "Test attendance record"
        )
        
        // When
        val savedAttendance = attendanceRepository.save(newAttendance)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedAttendance.id)
        
        val foundAttendance = attendanceRepository.findById(savedAttendance.id!!)
        assertTrue(foundAttendance.isPresent)
        assertEquals(AttendanceType.EVENT, foundAttendance.get().attendanceType)
        assertEquals(AttendanceStatus.CHECKED_IN, foundAttendance.get().status)
        assertEquals("Test attendance record", foundAttendance.get().notes)
    }
    
    @Test
    fun `attendance entity should handle equals and hashCode correctly`() {
        // Given
        val attendance1 = Attendance(
            id = 1L,
            user = testUser,
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now()
        )
        val attendance2 = Attendance(
            id = 1L,
            user = testUser,
            attendanceType = AttendanceType.SERVICE,
            status = AttendanceStatus.CHECKED_OUT,
            checkInTime = LocalDateTime.now()
        )
        val attendance3 = Attendance(
            id = 2L,
            user = testUser,
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now()
        )
        
        // Then
        assertEquals(attendance1, attendance2) // Same ID
        assertNotEquals(attendance1, attendance3) // Different ID
        assertEquals(attendance1.hashCode(), attendance2.hashCode()) // Same ID should have same hash
    }
}