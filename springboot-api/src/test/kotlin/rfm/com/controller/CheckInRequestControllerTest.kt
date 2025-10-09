package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.*
import rfm.com.entity.Gender
import rfm.com.service.CheckInRequestService
import java.time.LocalDateTime

/**
 * Integration tests for CheckInRequestController.
 * 
 * Tests cover:
 * - Complete check-in flow from request to approval
 * - Rejection flow
 * - Expiration handling
 * - Authorization for different roles
 */
@WebMvcTest(CheckInRequestController::class)
@ContextConfiguration(classes = [CheckInRequestController::class])
class CheckInRequestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var checkInRequestService: CheckInRequestService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var childSummary: ChildSummaryResponse
    private lateinit var serviceResponse: KidsServiceResponse
    private lateinit var parentResponse: ParentResponse
    private lateinit var checkInRequestResponse: CheckInRequestResponse

    @BeforeEach
    fun setup() {
        childSummary = ChildSummaryResponse(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            fullName = "John Doe",
            age = 5,
            ageGroup = "PRESCHOOL",
            isActive = true
        )

        serviceResponse = KidsServiceResponse(
            id = 1L,
            name = "Sunday Kids Service",
            dayOfWeek = "SUNDAY",
            serviceDate = LocalDateTime.now().toLocalDate(),
            startTime = "10:00:00",
            endTime = "11:30:00",
            location = "Kids Building",
            leaderName = "Jane Smith",
            maxCapacity = 50,
            minAge = 3,
            maxAge = 10,
            ageGroups = listOf("PRESCHOOL", "ELEMENTARY"),
            isActive = true
        )

        parentResponse = ParentResponse(
            id = 1L,
            firstName = "Parent",
            lastName = "User",
            fullName = "Parent User",
            email = "parent@example.com",
            phone = "555-1234"
        )

        checkInRequestResponse = CheckInRequestResponse(
            id = 1L,
            token = "test-token-123",
            qrCodeData = "test-token-123",
            child = childSummary,
            service = serviceResponse,
            requestedBy = parentResponse,
            status = "PENDING",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            expiresInSeconds = 900,
            isExpired = false
        )
    }

    // Test 7.1: POST /api/kids/checkin-requests
    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `createCheckInRequest should return success when request is valid`() {
        // Given
        val request = CreateCheckInRequestDto(
            childId = 1L,
            serviceId = 1L,
            notes = "Test notes"
        )

        given(checkInRequestService.createCheckInRequest(eq(1L), any()))
            .willReturn(checkInRequestResponse)

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Check-in request created successfully"))
            .andExpect(jsonPath("$.data.token").value("test-token-123"))
            .andExpect(jsonPath("$.data.child.firstName").value("John"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `createCheckInRequest should return forbidden when parent does not own child`() {
        // Given
        val request = CreateCheckInRequestDto(
            childId = 1L,
            serviceId = 1L
        )

        given(checkInRequestService.createCheckInRequest(eq(1L), any()))
            .willThrow(SecurityException("Access denied: You are not authorized to create check-in requests for this child"))

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `createCheckInRequest should return bad request when service not accepting check-ins`() {
        // Given
        val request = CreateCheckInRequestDto(
            childId = 1L,
            serviceId = 1L
        )

        given(checkInRequestService.createCheckInRequest(eq(1L), any()))
            .willThrow(IllegalStateException("Check-in is not currently open for this service"))

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    // Test 7.2: GET /api/kids/checkin-requests/token/{token}
    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `getRequestByToken should return request details for staff`() {
        // Given
        val detailsResponse = CheckInRequestDetailsResponse(
            id = 1L,
            child = ChildDetailedResponse(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                fullName = "John Doe",
                age = 5,
                ageGroup = "PRESCHOOL",
                gender = Gender.MALE,
                emergencyContactName = "Emergency Contact",
                emergencyContactPhone = "555-9999",
                medicalNotes = "Allergic to peanuts",
                allergies = "Peanuts",
                specialNeeds = null,
                pickupAuthorization = "Parent only"
            ),
            service = serviceResponse,
            requestedBy = parentResponse,
            status = "PENDING",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            notes = null,
            isExpired = false,
            canBeProcessed = true,
            hasMedicalAlerts = true,
            hasAllergies = true,
            hasSpecialNeeds = false
        )

        given(checkInRequestService.getCheckInRequestByToken("test-token-123"))
            .willReturn(detailsResponse)

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/token/test-token-123")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.child.medicalNotes").value("Allergic to peanuts"))
            .andExpect(jsonPath("$.data.hasMedicalAlerts").value(true))
            .andExpect(jsonPath("$.data.hasAllergies").value(true))
    }

    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `getRequestByToken should return 404 when token not found`() {
        // Given
        given(checkInRequestService.getCheckInRequestByToken("invalid-token"))
            .willThrow(IllegalArgumentException("Invalid check-in request token"))

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/token/invalid-token")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `getRequestByToken should return 410 when token expired`() {
        // Given
        given(checkInRequestService.getCheckInRequestByToken("expired-token"))
            .willThrow(IllegalStateException("Check-in request has expired. Please generate a new QR code."))

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/token/expired-token")
        )
            .andExpect(status().isGone)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `getRequestByToken should be forbidden for regular users`() {
        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/token/test-token-123")
        )
            .andExpect(status().isForbidden)
    }

    // Test 7.3: POST /api/kids/checkin-requests/token/{token}/approve
    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `approveCheckIn should return success when approval is valid`() {
        // Given
        val approveRequest = ApproveCheckInDto(notes = "Verified identity")
        val approvalResponse = CheckInApprovalResponse(
            requestId = 1L,
            attendanceId = 1L,
            child = childSummary,
            service = serviceResponse,
            checkInTime = LocalDateTime.now(),
            approvedBy = "Staff Member",
            message = "Check-in approved successfully"
        )

        given(checkInRequestService.approveCheckInRequest(
            token = "test-token-123",
            staffUserId = 2L,
            notes = "Verified identity"
        )).willReturn(approvalResponse)

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attendanceId").value(1))
            .andExpect(jsonPath("$.data.approvedBy").value("Staff Member"))
    }

    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `approveCheckIn should return 409 when service at capacity`() {
        // Given
        val approveRequest = ApproveCheckInDto()

        given(checkInRequestService.approveCheckInRequest(
            token = "test-token-123",
            staffUserId = 2L,
            notes = null
        )).willThrow(IllegalStateException("Service is at capacity. Cannot complete check-in."))

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `approveCheckIn should return 409 when child already checked in`() {
        // Given
        val approveRequest = ApproveCheckInDto()

        given(checkInRequestService.approveCheckInRequest(
            token = "test-token-123",
            staffUserId = 2L,
            notes = null
        )).willThrow(IllegalStateException("Child is already checked in to this service today"))

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `approveCheckIn should be forbidden for regular users`() {
        // Given
        val approveRequest = ApproveCheckInDto()

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest))
        )
            .andExpect(status().isForbidden)
    }

    // Test 7.4: POST /api/kids/checkin-requests/token/{token}/reject
    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `rejectCheckIn should return success when rejection is valid`() {
        // Given
        val rejectRequest = RejectCheckInDto(reason = "Child appears unwell")
        val rejectionResponse = CheckInRejectionResponse(
            requestId = 1L,
            child = childSummary,
            service = serviceResponse,
            rejectedBy = "Staff Member",
            reason = "Child appears unwell",
            message = "Check-in request has been rejected"
        )

        given(checkInRequestService.rejectCheckInRequest(
            token = "test-token-123",
            staffUserId = 2L,
            reason = "Child appears unwell"
        )).willReturn(rejectionResponse)

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.reason").value("Child appears unwell"))
            .andExpect(jsonPath("$.data.rejectedBy").value("Staff Member"))
    }

    @Test
    @WithMockUser(username = "2", roles = ["STAFF"])
    fun `rejectCheckIn should return 410 when token expired`() {
        // Given
        val rejectRequest = RejectCheckInDto(reason = "Token expired")

        given(checkInRequestService.rejectCheckInRequest(
            token = "expired-token",
            staffUserId = 2L,
            reason = "Token expired"
        )).willThrow(IllegalStateException("Check-in request has expired. Please generate a new QR code."))

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/expired-token/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest))
        )
            .andExpect(status().isGone)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `rejectCheckIn should be forbidden for regular users`() {
        // Given
        val rejectRequest = RejectCheckInDto(reason = "Test reason")

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest))
        )
            .andExpect(status().isForbidden)
    }

    // Test 7.5: DELETE /api/kids/checkin-requests/{requestId}
    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `cancelRequest should return success when cancellation is valid`() {
        // Given
        willDoNothing().given(checkInRequestService).cancelCheckInRequest(1L, 1L)

        // When & Then
        mockMvc.perform(
            delete("/api/kids/checkin-requests/1")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Check-in request cancelled successfully"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `cancelRequest should return forbidden when user does not own child`() {
        // Given
        willThrow(SecurityException("Access denied: You are not authorized to cancel this check-in request"))
            .given(checkInRequestService).cancelCheckInRequest(1L, 1L)

        // When & Then
        mockMvc.perform(
            delete("/api/kids/checkin-requests/1")
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `cancelRequest should return bad request when request not pending`() {
        // Given
        willThrow(IllegalStateException("Cannot cancel check-in request. Current status: APPROVED"))
            .given(checkInRequestService).cancelCheckInRequest(1L, 1L)

        // When & Then
        mockMvc.perform(
            delete("/api/kids/checkin-requests/1")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
    }

    // Test 7.6: GET /api/kids/checkin-requests/active
    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `getActiveRequests should return list of active requests`() {
        // Given
        val activeRequests = listOf(checkInRequestResponse)

        given(checkInRequestService.getActiveRequestsForParent(1L))
            .willReturn(activeRequests)

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/active")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].token").value("test-token-123"))
            .andExpect(jsonPath("$.data[0].status").value("PENDING"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `getActiveRequests should return empty list when no active requests`() {
        // Given
        given(checkInRequestService.getActiveRequestsForParent(1L))
            .willReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/active")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)
    }

    // Test authorization for ADMIN role
    @Test
    @WithMockUser(username = "3", roles = ["ADMIN"])
    fun `admin should be able to scan QR codes`() {
        // Given
        val detailsResponse = CheckInRequestDetailsResponse(
            id = 1L,
            child = ChildDetailedResponse(
                id = 1L,
                firstName = "John",
                lastName = "Doe",
                fullName = "John Doe",
                age = 5,
                ageGroup = "PRESCHOOL",
                gender = Gender.MALE,
                emergencyContactName = null,
                emergencyContactPhone = null,
                medicalNotes = null,
                allergies = null,
                specialNeeds = null,
                pickupAuthorization = null
            ),
            service = serviceResponse,
            requestedBy = parentResponse,
            status = "PENDING",
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            notes = null,
            isExpired = false,
            canBeProcessed = true,
            hasMedicalAlerts = false,
            hasAllergies = false,
            hasSpecialNeeds = false
        )

        given(checkInRequestService.getCheckInRequestByToken("test-token-123"))
            .willReturn(detailsResponse)

        // When & Then
        mockMvc.perform(
            get("/api/kids/checkin-requests/token/test-token-123")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @WithMockUser(username = "3", roles = ["ADMIN"])
    fun `admin should be able to approve check-ins`() {
        // Given
        val approveRequest = ApproveCheckInDto()
        val approvalResponse = CheckInApprovalResponse(
            requestId = 1L,
            attendanceId = 1L,
            child = childSummary,
            service = serviceResponse,
            checkInTime = LocalDateTime.now(),
            approvedBy = "Admin User",
            message = "Check-in approved successfully"
        )

        given(checkInRequestService.approveCheckInRequest(
            token = "test-token-123",
            staffUserId = 3L,
            notes = null
        )).willReturn(approvalResponse)

        // When & Then
        mockMvc.perform(
            post("/api/kids/checkin-requests/token/test-token-123/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }
}
