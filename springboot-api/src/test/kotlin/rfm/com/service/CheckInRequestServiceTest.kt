package rfm.com.service

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rfm.com.dto.CreateCheckInRequestDto
import rfm.com.entity.*
import rfm.com.repository.*
import rfm.com.util.TokenGenerator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Unit tests for CheckInRequestService
 * 
 * Tests cover:
 * - Creating check-in requests with valid and invalid inputs
 * - Duplicate request prevention
 * - Approval flow
 * - Rejection flow
 * - Expiration logic
 * - Cancellation
 */
class CheckInRequestServiceTest {

    private lateinit var checkInRequestRepository: CheckInRequestRepository
    private lateinit var kidRepository: KidRepository
    private lateinit var kidsServiceRepository: KidsServiceRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var kidAttendanceRepository: KidAttendanceRepository
    private lateinit var checkInRequestService: CheckInRequestService

    private lateinit var testParent: UserProfile
    private lateinit var testStaff: UserProfile
    private lateinit var testChild: Kid
    private lateinit var testService: rfm.com.entity.KidsService
    private lateinit var testCheckInRequest: rfm.com.entity.CheckInRequest

    @BeforeEach
    fun setUp() {
        checkInRequestRepository = mockk()
        kidRepository = mockk()
        kidsServiceRepository = mockk()
        userProfileRepository = mockk()
        kidAttendanceRepository = mockk()
        
        checkInRequestService = CheckInRequestService(
            checkInRequestRepository,
            kidRepository,
            kidsServiceRepository,
            userProfileRepository,
            kidAttendanceRepository
        )

        // Create test data
        testParent = UserProfile(
            id = 1L,
            user = mockk(relaxed = true),
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "1234567890"
        )

        testStaff = UserProfile(
            id = 2L,
            user = mockk(relaxed = true),
            firstName = "Jane",
            lastName = "Staff",
            email = "jane.staff@example.com",
            phone = "0987654321"
        )

        testChild = Kid(
            id = 1L,
            firstName = "Tommy",
            lastName = "Doe",
            dateOfBirth = LocalDate.now().minusYears(5),
            primaryParent = testParent,
            medicalNotes = "No known allergies",
            allergies = null,
            specialNeeds = null
        )

        testService = rfm.com.entity.KidsService(
            id = 1L,
            name = "Sunday Kids Service",
            dayOfWeek = DayOfWeek.SUNDAY,
            serviceDate = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            location = "Kids Room",
            maxCapacity = 20,
            minAge = 3,
            maxAge = 10,
            ageGroups = mutableSetOf(AgeGroup.PRESCHOOL, AgeGroup.ELEMENTARY_LOWER),
            isActive = true,
            checkInStartsMinutesBefore = 30,
            checkInEndsMinutesAfter = 15
        )

        testCheckInRequest = rfm.com.entity.CheckInRequest(
            id = 1L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "test-token-123",
            expiresAt = LocalDateTime.now().plusMinutes(15),
            status = CheckInRequestStatus.PENDING
        )
    }

    // Test createCheckInRequest with valid inputs
    @Test
    fun `createCheckInRequest should create request successfully with valid inputs`() {
        // Given
        val request = CreateCheckInRequestDto(
            childId = 1L,
            serviceId = 1L,
            notes = "Test notes"
        )

        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { kidRepository.findById(1L) } returns Optional.of(testChild)
        every { kidsServiceRepository.findById(1L) } returns Optional.of(testService)
        every { checkInRequestRepository.findByKidAndKidsServiceAndStatus(testChild, testService, CheckInRequestStatus.PENDING) } returns null
        every { checkInRequestRepository.save(any<CheckInRequest>()) } returns testCheckInRequest

        // When
        val result = checkInRequestService.createCheckInRequest(1L, request)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("test-token-123", result.token)
        assertEquals("Tommy Doe", result.child.fullName)
        assertEquals("Sunday Kids Service", result.service.name)
        verify(exactly = 1) { checkInRequestRepository.save(any<CheckInRequest>()) }
    }

    // Test createCheckInRequest with parent not owning child
    @Test
    fun `createCheckInRequest should throw SecurityException when parent does not own child`() {
        // Given
        val otherParent = testParent.copy(id = 99L)
        val request = CreateCheckInRequestDto(childId = 1L, serviceId = 1L)

        every { userProfileRepository.findById(99L) } returns Optional.of(otherParent)
        every { kidRepository.findById(1L) } returns Optional.of(testChild)

        // When/Then
        val exception = assertThrows<SecurityException> {
            checkInRequestService.createCheckInRequest(99L, request)
        }
        assertTrue(exception.message!!.contains("not authorized"))
    }

    // Test createCheckInRequest with service not accepting check-ins
    @Test
    fun `createCheckInRequest should throw IllegalStateException when service not accepting check-ins`() {
        // Given
        val futureService = rfm.com.entity.KidsService(
            id = 1L,
            name = "Sunday Kids Service",
            dayOfWeek = DayOfWeek.SUNDAY,
            serviceDate = LocalDate.now(),
            startTime = LocalTime.now().plusHours(2),
            endTime = LocalTime.now().plusHours(3),
            location = "Kids Room",
            maxCapacity = 20,
            minAge = 3,
            maxAge = 10,
            ageGroups = mutableSetOf(AgeGroup.PRESCHOOL, AgeGroup.ELEMENTARY_LOWER),
            isActive = true,
            checkInStartsMinutesBefore = 30,
            checkInEndsMinutesAfter = 15
        )
        val request = CreateCheckInRequestDto(childId = 1L, serviceId = 1L)

        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { kidRepository.findById(1L) } returns Optional.of(testChild)
        every { kidsServiceRepository.findById(1L) } returns Optional.of(futureService)

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            checkInRequestService.createCheckInRequest(1L, request)
        }
        assertTrue(exception.message!!.contains("not currently open"))
    }

    // Test createCheckInRequest with child not eligible for service
    @Test
    fun `createCheckInRequest should throw IllegalArgumentException when child not eligible`() {
        // Given
        val tooYoungChild = Kid(
            id = 1L,
            firstName = "Tommy",
            lastName = "Doe",
            dateOfBirth = LocalDate.now().minusYears(2),
            primaryParent = testParent,
            medicalNotes = "No known allergies"
        )
        val request = CreateCheckInRequestDto(childId = 1L, serviceId = 1L)

        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { kidRepository.findById(1L) } returns Optional.of(tooYoungChild)
        every { kidsServiceRepository.findById(1L) } returns Optional.of(testService)

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            checkInRequestService.createCheckInRequest(1L, request)
        }
        assertTrue(exception.message!!.contains("age requirements"))
    }

    // Test duplicate request prevention
    @Test
    fun `createCheckInRequest should return existing request when duplicate found`() {
        // Given
        val request = CreateCheckInRequestDto(childId = 1L, serviceId = 1L)

        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { kidRepository.findById(1L) } returns Optional.of(testChild)
        every { kidsServiceRepository.findById(1L) } returns Optional.of(testService)
        every { checkInRequestRepository.findByKidAndKidsServiceAndStatus(testChild, testService, CheckInRequestStatus.PENDING) } returns testCheckInRequest

        // When
        val result = checkInRequestService.createCheckInRequest(1L, request)

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        verify(exactly = 0) { checkInRequestRepository.save(any<CheckInRequest>()) }
    }

    // Test getCheckInRequestByToken with valid token
    @Test
    fun `getCheckInRequestByToken should return request details with valid token`() {
        // Given
        every { checkInRequestRepository.findByToken("test-token-123") } returns testCheckInRequest

        // When
        val result = checkInRequestService.getCheckInRequestByToken("test-token-123")

        // Then
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals("Tommy Doe", result.child.fullName)
        assertTrue(result.hasMedicalAlerts)
        assertFalse(result.hasAllergies)
    }

    // Test getCheckInRequestByToken with invalid token
    @Test
    fun `getCheckInRequestByToken should throw IllegalArgumentException with invalid token`() {
        // Given
        every { checkInRequestRepository.findByToken("invalid-token") } returns null

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            checkInRequestService.getCheckInRequestByToken("invalid-token")
        }
        assertTrue(exception.message!!.contains("Invalid"))
    }

    // Test getCheckInRequestByToken with expired token
    @Test
    fun `getCheckInRequestByToken should throw IllegalStateException with expired token`() {
        // Given
        val expiredRequest = rfm.com.entity.CheckInRequest(
            id = 1L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "expired-token",
            expiresAt = LocalDateTime.now().minusMinutes(1),
            status = CheckInRequestStatus.PENDING
        )
        every { checkInRequestRepository.findByToken("expired-token") } returns expiredRequest

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            checkInRequestService.getCheckInRequestByToken("expired-token")
        }
        assertTrue(exception.message!!.contains("expired"))
    }

    // Test approval flow
    @Test
    fun `approveCheckInRequest should create attendance record and update request status`() {
        // Given
        val attendance = KidAttendance(
            id = 1L,
            kid = testChild,
            kidsService = testService,
            checkedInBy = testParent.fullName,
            status = AttendanceStatus.CHECKED_IN,
            checkInRequest = testCheckInRequest,
            approvedByStaff = testStaff.fullName
        )

        every { checkInRequestRepository.findByToken("test-token-123") } returns testCheckInRequest
        every { userProfileRepository.findById(2L) } returns Optional.of(testStaff)
        every { kidAttendanceRepository.findByKidAndCheckInTimeBetween(any(), any(), any()) } returns emptyList()
        every { kidAttendanceRepository.save(any<KidAttendance>()) } returns attendance
        every { checkInRequestRepository.save(any<CheckInRequest>()) } returns testCheckInRequest.copy(status = CheckInRequestStatus.APPROVED)

        // When
        val result = checkInRequestService.approveCheckInRequest("test-token-123", 2L, "Approved")

        // Then
        assertNotNull(result)
        assertEquals(1L, result.requestId)
        assertEquals(1L, result.attendanceId)
        assertEquals("Jane Staff", result.approvedBy)
        verify(exactly = 1) { kidAttendanceRepository.save(any<KidAttendance>()) }
        verify(exactly = 1) { checkInRequestRepository.save(match { it.status == CheckInRequestStatus.APPROVED }) }
    }

    // Test approval with service at capacity
    @Test
    fun `approveCheckInRequest should throw IllegalStateException when service at capacity`() {
        // Given
        val fullService = rfm.com.entity.KidsService(
            id = 1L,
            name = "Sunday Kids Service",
            dayOfWeek = DayOfWeek.SUNDAY,
            serviceDate = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            location = "Kids Room",
            maxCapacity = 0,
            minAge = 3,
            maxAge = 10,
            ageGroups = mutableSetOf(AgeGroup.PRESCHOOL, AgeGroup.ELEMENTARY_LOWER),
            isActive = true
        )
        val requestWithFullService = rfm.com.entity.CheckInRequest(
            id = 1L,
            kid = testChild,
            kidsService = fullService,
            requestedBy = testParent,
            token = "test-token-123",
            expiresAt = LocalDateTime.now().plusMinutes(15),
            status = CheckInRequestStatus.PENDING
        )

        every { checkInRequestRepository.findByToken("test-token-123") } returns requestWithFullService
        every { userProfileRepository.findById(2L) } returns Optional.of(testStaff)

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            checkInRequestService.approveCheckInRequest("test-token-123", 2L, null)
        }
        assertTrue(exception.message!!.contains("capacity"))
    }

    // Test approval with duplicate check-in
    @Test
    fun `approveCheckInRequest should throw IllegalStateException when child already checked in`() {
        // Given
        val existingAttendance = KidAttendance(
            id = 2L,
            kid = testChild,
            kidsService = testService,
            checkedInBy = testParent.fullName,
            status = AttendanceStatus.CHECKED_IN
        )

        every { checkInRequestRepository.findByToken("test-token-123") } returns testCheckInRequest
        every { userProfileRepository.findById(2L) } returns Optional.of(testStaff)
        every { kidAttendanceRepository.findByKidAndCheckInTimeBetween(any(), any(), any()) } returns listOf(existingAttendance)

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            checkInRequestService.approveCheckInRequest("test-token-123", 2L, null)
        }
        assertTrue(exception.message!!.contains("already checked in"))
    }

    // Test rejection flow
    @Test
    fun `rejectCheckInRequest should update request status with reason`() {
        // Given
        every { checkInRequestRepository.findByToken("test-token-123") } returns testCheckInRequest
        every { userProfileRepository.findById(2L) } returns Optional.of(testStaff)
        every { checkInRequestRepository.save(any<rfm.com.entity.CheckInRequest>()) } returns testCheckInRequest

        // When
        val result = checkInRequestService.rejectCheckInRequest("test-token-123", 2L, "Child seems unwell")

        // Then
        assertNotNull(result)
        assertEquals(1L, result.requestId)
        assertEquals("Jane Staff", result.rejectedBy)
        assertEquals("Child seems unwell", result.reason)
        verify(exactly = 1) { checkInRequestRepository.save(match { 
            it.status == CheckInRequestStatus.REJECTED && 
            it.rejectionReason == "Child seems unwell" 
        }) }
    }

    // Test cancellation
    @Test
    fun `cancelCheckInRequest should update request status to CANCELLED`() {
        // Given
        every { checkInRequestRepository.findById(1L) } returns Optional.of(testCheckInRequest)
        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { checkInRequestRepository.save(any<rfm.com.entity.CheckInRequest>()) } returns testCheckInRequest

        // When
        checkInRequestService.cancelCheckInRequest(1L, 1L)

        // Then
        verify(exactly = 1) { checkInRequestRepository.save(match { it.status == CheckInRequestStatus.CANCELLED }) }
    }

    // Test cancellation with wrong parent
    @Test
    fun `cancelCheckInRequest should throw SecurityException when parent does not own child`() {
        // Given
        val otherParent = testParent.copy(id = 99L)

        every { checkInRequestRepository.findById(1L) } returns Optional.of(testCheckInRequest)
        every { userProfileRepository.findById(99L) } returns Optional.of(otherParent)

        // When/Then
        val exception = assertThrows<SecurityException> {
            checkInRequestService.cancelCheckInRequest(1L, 99L)
        }
        assertTrue(exception.message!!.contains("not authorized"))
    }

    // Test cancellation with non-pending request
    @Test
    fun `cancelCheckInRequest should throw IllegalStateException when request not pending`() {
        // Given
        val approvedRequest = rfm.com.entity.CheckInRequest(
            id = 1L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "test-token-123",
            expiresAt = LocalDateTime.now().plusMinutes(15),
            status = CheckInRequestStatus.APPROVED
        )

        every { checkInRequestRepository.findById(1L) } returns Optional.of(approvedRequest)
        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)

        // When/Then
        val exception = assertThrows<IllegalStateException> {
            checkInRequestService.cancelCheckInRequest(1L, 1L)
        }
        assertTrue(exception.message!!.contains("Cannot cancel"))
    }

    // Test getActiveRequestsForParent
    @Test
    fun `getActiveRequestsForParent should return all pending requests`() {
        // Given
        val request2 = rfm.com.entity.CheckInRequest(
            id = 2L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "token-2",
            expiresAt = LocalDateTime.now().plusMinutes(15),
            status = CheckInRequestStatus.PENDING
        )
        
        every { userProfileRepository.findById(1L) } returns Optional.of(testParent)
        every { checkInRequestRepository.findByRequestedByAndStatusIn(testParent, listOf(CheckInRequestStatus.PENDING)) } returns listOf(testCheckInRequest, request2)

        // When
        val result = checkInRequestService.getActiveRequestsForParent(1L)

        // Then
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("test-token-123", result[0].token)
        assertEquals("token-2", result[1].token)
    }

    // Test expireOldRequests
    @Test
    fun `expireOldRequests should mark expired requests as EXPIRED`() {
        // Given
        val expiredRequest1 = rfm.com.entity.CheckInRequest(
            id = 1L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "expired-1",
            expiresAt = LocalDateTime.now().minusMinutes(5),
            status = CheckInRequestStatus.PENDING
        )
        val expiredRequest2 = rfm.com.entity.CheckInRequest(
            id = 2L,
            kid = testChild,
            kidsService = testService,
            requestedBy = testParent,
            token = "expired-2",
            expiresAt = LocalDateTime.now().minusMinutes(10),
            status = CheckInRequestStatus.PENDING
        )
        
        every { checkInRequestRepository.findByStatusAndExpiresAtBefore(CheckInRequestStatus.PENDING, any()) } returns listOf(expiredRequest1, expiredRequest2)
        every { checkInRequestRepository.saveAll(any<List<rfm.com.entity.CheckInRequest>>()) } returns listOf(expiredRequest1, expiredRequest2)

        // When
        checkInRequestService.expireOldRequests()

        // Then
        verify(exactly = 1) { checkInRequestRepository.saveAll(match<List<rfm.com.entity.CheckInRequest>> { 
            it.size == 2 && it.all { req -> req.status == CheckInRequestStatus.EXPIRED }
        }) }
    }
}
