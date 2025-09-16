package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.config.SecurityConfig
import rfm.com.dto.*
import rfm.com.service.FileStorageService
import rfm.com.service.UserService
import java.time.LocalDateTime

@WebMvcTest(ProfileController::class)
@Import(SecurityConfig::class)
class ProfileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var fileStorageService: FileStorageService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var sampleUserProfile: UserProfileResponse

    @BeforeEach
    fun setUp() {
        sampleUserProfile = UserProfileResponse(
            id = 1L,
            userId = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+1234567890",
            imagePath = "/profiles/profile.jpg",
            isAdmin = false,
            joinedAt = LocalDateTime.now(),
            fullName = "John Doe"
        )
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `getCurrentUserProfile should return user profile successfully`() {
        // Given
        val apiResponse = ApiResponse(
            success = true,
            message = "Profile retrieved successfully",
            data = sampleUserProfile
        )
        every { userService.getUserProfile(1L) } returns apiResponse

        // When & Then
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("John"))
            .andExpect(jsonPath("$.data.lastName").value("Doe"))

        verify { userService.getUserProfile(1L) }
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `updateCurrentUserProfile should update profile successfully`() {
        // Given
        val updateRequest = UpdateProfileRequest(
            firstName = "Jane",
            lastName = "Smith",
            phone = "+0987654321"
        )
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "Profile updated successfully"
        )
        every { userService.updateUserProfile(1L, "Jane", "Smith", "+0987654321") } returns apiResponse

        // When & Then
        mockMvc.perform(
            put("/api/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Profile updated successfully"))

        verify { userService.updateUserProfile(1L, "Jane", "Smith", "+0987654321") }
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `getAllUserProfiles should return all profiles for admin`() {
        // Given
        val profiles = listOf(sampleUserProfile)
        val apiResponse = ApiResponse(
            success = true,
            message = "User profiles retrieved successfully",
            data = profiles
        )
        every { userService.getAllUserProfiles() } returns apiResponse

        // When & Then
        mockMvc.perform(get("/api/profile/all"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].firstName").value("John"))

        verify { userService.getAllUserProfiles() }
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `getAllUserProfiles should return forbidden for non-admin`() {
        // When & Then
        mockMvc.perform(get("/api/profile/all"))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `updateUserAdminStatus should update admin status successfully`() {
        // Given
        val updateRequest = UpdateAdminStatusRequest(isAdmin = true)
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "Admin status updated successfully"
        )
        every { userService.updateUserAdminStatus(2L, true) } returns apiResponse

        // When & Then
        mockMvc.perform(
            put("/api/profile/2/admin-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))

        verify { userService.updateUserAdminStatus(2L, true) }
    }

    @Test
    @WithMockUser(username = "1", roles = ["ADMIN"])
    fun `updateUserAdminStatus should prevent self-modification`() {
        // Given
        val updateRequest = UpdateAdminStatusRequest(isAdmin = false)

        // When & Then
        mockMvc.perform(
            put("/api/profile/1/admin-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Cannot change your own admin status"))
    }

    @Test
    fun `unauthenticated requests should return unauthorized`() {
        // When & Then
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isUnauthorized)
    }
}