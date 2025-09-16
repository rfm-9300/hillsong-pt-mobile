package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.*
import rfm.com.entity.AttendanceStatus
import rfm.com.entity.AttendanceType
import rfm.com.service.AttendanceService
import java.time.LocalDateTime

@WebMvcTest(AttendanceController::class)
@ContextConfiguration(classes = [AttendanceControllerTest.TestConfig::class])
class AttendanceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var attendanceService: AttendanceService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun attendanceService(): AttendanceService = mockk()
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `POST check-in should return success response`() {
        // Given
        val checkInRequest = CheckInRequest(
            attendanceType = AttendanceType.EVENT,
            eventId = 1L,
            notes = "Test check-in"
        )
        
        val attendanceResponse = AttendanceResponse(
            id = 1L,
            user = UserResponse(
                id = 1L,
                email = "test@example.com",
                firstName = "Test",
                lastName = "User",
                verified = true,
                createdAt = LocalDateTime.now(),
                authProvider = "LOCAL"
            ),
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            isCheckedOut = false
        )

        coEvery { attendanceService.checkIn(1L, checkInRequest) } returns attendanceResponse

        // When & Then
        mockMvc.perform(
            post("/api/attendance/check-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Successfully checked in"))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.attendanceType").value("EVENT"))
            .andExpect(jsonPath("$.data.status").value("CHECKED_IN"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET my-attendance should return paginated attendance records`() {
        // Given
        val attendanceSummary = AttendanceSummaryResponse(
            id = 1L,
            userId = 1L,
            userName = "Test User",
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            isCheckedOut = false,
            entityName = "Test Event",
            entityId = 1L
        )
        
        val page = PageImpl(listOf(attendanceSummary), PageRequest.of(0, 20), 1)
        
        coEvery { 
            attendanceService.getUserAttendance(1L, any()) 
        } returns page

        // When & Then
        mockMvc.perform(
            get("/api/attendance/my-attendance")
                .param("page", "0")
                .param("size", "20")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Attendance records retrieved successfully"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content[0].id").value(1))
            .andExpect(jsonPath("$.data.content[0].attendanceType").value("EVENT"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET currently-checked-in should return list of checked-in users`() {
        // Given
        val attendanceSummary = AttendanceSummaryResponse(
            id = 1L,
            userId = 1L,
            userName = "Test User",
            attendanceType = AttendanceType.EVENT,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            isCheckedOut = false,
            entityName = "Test Event",
            entityId = 1L
        )
        
        coEvery { attendanceService.getCurrentlyCheckedIn() } returns listOf(attendanceSummary)

        // When & Then
        mockMvc.perform(get("/api/attendance/currently-checked-in"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Currently checked-in users retrieved successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].status").value("CHECKED_IN"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `POST check-in with invalid request should return bad request`() {
        // Given - Invalid request with no entity ID
        val invalidRequest = """
            {
                "attendanceType": "EVENT",
                "notes": "Test check-in"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/attendance/check-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `GET frequent-attendees should return paginated frequent attendees for admin`() {
        // Given
        val frequentAttendee = FrequentAttendeesResponse(
            user = UserResponse(
                id = 1L,
                email = "test@example.com",
                firstName = "Test",
                lastName = "User",
                verified = true,
                createdAt = LocalDateTime.now(),
                authProvider = "LOCAL"
            ),
            attendanceCount = 10L,
            lastAttendance = LocalDateTime.now()
        )
        
        val page = PageImpl(listOf(frequentAttendee), PageRequest.of(0, 20), 1)
        
        coEvery { attendanceService.getMostFrequentAttendees(any()) } returns page

        // When & Then
        mockMvc.perform(get("/api/attendance/frequent-attendees"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Most frequent attendees retrieved successfully"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content[0].attendanceCount").value(10))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET frequent-attendees should return forbidden for non-admin user`() {
        // When & Then
        mockMvc.perform(get("/api/attendance/frequent-attendees"))
            .andExpect(status().isForbidden)
    }
}