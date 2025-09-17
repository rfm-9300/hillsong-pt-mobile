package rfm.com.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.*
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.entity.AuthProvider
import rfm.com.repository.UserRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.security.jwt.JwtTokenProvider
import java.time.LocalDateTime

/**
 * Comprehensive integration test that validates the complete migration from Ktor to Spring Boot.
 * This test covers:
 * - API endpoint compatibility with same request/response format as Ktor version
 * - JWT token compatibility and authentication flows
 * - File upload and serving functionality
 * - Email sending integration (in test mode)
 * - OAuth2 integration setup
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.email.production=false",
    "logging.level.rfm.com=INFO"
])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ComprehensiveIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var testUserProfile: UserProfile
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        // Create test user and profile for authentication tests
        testUser = User(
            email = "test@example.com",
            password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.IjZg.BkmBm32S.6ZQuMjA/3oUYeizC", // "password"
            salt = "test-salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )
        testUser = userRepository.save(testUser)

        testUserProfile = UserProfile(
            user = testUser,
            firstName = "Test",
            lastName = "User",
            email = testUser.email,
            phone = "1234567890"
        )
        testUserProfile = userProfileRepository.save(testUserProfile)

        // Generate JWT token for authenticated requests
        jwtToken = jwtTokenProvider.generateTokenFromUserId(testUser.id!!, testUser.email)
    }

    @AfterEach
    fun tearDown() {
        userProfileRepository.deleteAll()
        userRepository.deleteAll()
    }

    // ========== Authentication Flow Tests ==========

    @Test
    @Order(1)
    fun `should authenticate user with valid credentials and return JWT token`() {
        val loginRequest = AuthRequest(
            email = "test@example.com",
            password = "password"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Authentication successful"))
        .andExpect(jsonPath("$.data.token").exists())
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.data.user.firstName").value("Test"))
        .andExpect(jsonPath("$.data.user.lastName").value("User"))
    }

    @Test
    @Order(2)
    fun `should reject authentication with invalid credentials`() {
        val loginRequest = AuthRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
        .andExpect(status().isUnauthorized)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    @Order(3)
    fun `should register new user and send verification email`() {
        val signUpRequest = SignUpRequest(
            email = "newuser@example.com",
            password = "password123",
            confirmPassword = "password123",
            firstName = "New",
            lastName = "User"
        )

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."))
    }

    // ========== JWT Token Validation Tests ==========

    @Test
    @Order(4)
    fun `should access protected endpoint with valid JWT token`() {
        mockMvc.perform(
            get("/api/profile/me")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    @Order(5)
    fun `should reject access to protected endpoint without JWT token`() {
        mockMvc.perform(get("/api/profile/me"))
        .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(6)
    fun `should reject access with invalid JWT token`() {
        mockMvc.perform(
            get("/api/profile/me")
                .header("Authorization", "Bearer invalid-token")
        )
        .andExpect(status().isUnauthorized)
    }

    // ========== Event Management API Tests ==========

    @Test
    @Order(7)
    fun `should create event with authenticated user`() {
        val createEventRequest = CreateEventRequest(
            title = "Test Event",
            description = "Test Description",
            date = LocalDateTime.now().plusDays(7),
            location = "Test Location",
            maxAttendees = 50,
            needsApproval = false
        )

        mockMvc.perform(
            post("/api/events")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEventRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Event created successfully"))
        .andExpect(jsonPath("$.data.title").value("Test Event"))
        .andExpect(jsonPath("$.data.organizerName").value("Test User"))
    }

    @Test
    @Order(8)
    fun `should retrieve all events with pagination`() {
        mockMvc.perform(
            get("/api/events")
                .header("Authorization", "Bearer $jwtToken")
                .param("page", "0")
                .param("size", "20")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
        .andExpect(jsonPath("$.data.content").isArray)
        .andExpect(jsonPath("$.data.pageable").exists())
    }

    @Test
    @Order(9)
    fun `should retrieve upcoming events`() {
        mockMvc.perform(
            get("/api/events/upcoming")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Upcoming events retrieved successfully"))
        .andExpect(jsonPath("$.data").isArray)
    }

    // ========== Post Management API Tests ==========

    @Test
    @Order(10)
    fun `should create post with authenticated user`() {
        val createPostRequest = CreatePostRequest(
            title = "Test Post",
            content = "Test post content for integration testing"
        )

        mockMvc.perform(
            post("/api/posts")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Post created successfully"))
        .andExpect(jsonPath("$.data.title").value("Test Post"))
        .andExpect(jsonPath("$.data.authorName").value("Test User"))
    }

    @Test
    @Order(11)
    fun `should retrieve all posts with pagination`() {
        mockMvc.perform(
            get("/api/posts")
                .header("Authorization", "Bearer $jwtToken")
                .param("page", "0")
                .param("size", "20")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Posts retrieved successfully"))
        .andExpect(jsonPath("$.data.content").isArray)
    }

    // ========== File Upload and Serving Tests ==========

    @Test
    @Order(12)
    fun `should upload image file successfully`() {
        val imageFile = MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "fake image content".toByteArray()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("File uploaded successfully"))
        .andExpect(jsonPath("$.data.fileName").exists())
        .andExpect(jsonPath("$.data.filePath").exists())
    }

    @Test
    @Order(13)
    fun `should reject non-image file upload`() {
        val textFile = MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "text content".toByteArray()
        )

        mockMvc.perform(
            multipart("/api/files/upload")
                .file(textFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Only image files are allowed"))
    }

    @Test
    @Order(14)
    fun `should serve uploaded files`() {
        // First upload a file
        val imageFile = MockMultipartFile(
            "file",
            "serve-test.jpg",
            "image/jpeg",
            "fake image content for serving".toByteArray()
        )

        val uploadResult = mockMvc.perform(
            multipart("/api/files/upload")
                .file(imageFile)
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andReturn()

        val uploadResponse = objectMapper.readTree(uploadResult.response.contentAsString)
        val fileName = uploadResponse.get("data").get("fileName").asText()

        // Then try to serve it
        mockMvc.perform(get("/api/files/$fileName"))
        .andExpect(status().isOk)
        .andExpect(header().string("Content-Type", "image/jpeg"))
    }

    // ========== Attendance Management Tests ==========

    @Test
    @Order(15)
    @WithMockUser(username = "1", roles = ["USER"])
    fun `should access attendance endpoints with proper authentication`() {
        mockMvc.perform(
            get("/api/attendance/currently-checked-in")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    @Order(16)
    @WithMockUser(username = "1", roles = ["USER"])
    fun `should get user attendance history`() {
        mockMvc.perform(
            get("/api/attendance/my-attendance")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray)
    }

    // ========== Profile Management Tests ==========

    @Test
    @Order(17)
    fun `should update user profile`() {
        val updateProfileRequest = UpdateProfileRequest(
            firstName = "Updated",
            lastName = "Name",
            phone = "9876543210"
        )

        mockMvc.perform(
            put("/api/profile/me")
                .header("Authorization", "Bearer $jwtToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Profile updated successfully"))
        .andExpect(jsonPath("$.data.firstName").value("Updated"))
        .andExpect(jsonPath("$.data.lastName").value("Name"))
    }

    // ========== Password Reset Flow Tests ==========

    @Test
    @Order(18)
    fun `should handle password reset request`() {
        val passwordResetRequest = PasswordResetRequest(
            email = "test@example.com"
        )

        mockMvc.perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"))
    }

    // ========== OAuth2 Integration Tests ==========

    @Test
    @Order(19)
    fun `should handle Google OAuth2 login request`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = "mock-google-token"
        )

        // This will fail with invalid token, but tests the endpoint structure
        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @Order(20)
    fun `should handle Facebook OAuth2 login request`() {
        val facebookAuthRequest = FacebookAuthRequest(
            accessToken = "mock-facebook-token"
        )

        // This will fail with invalid token, but tests the endpoint structure
        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facebookAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
    }

    // ========== Error Handling Tests ==========

    @Test
    @Order(21)
    fun `should handle validation errors properly`() {
        val invalidSignUpRequest = SignUpRequest(
            email = "invalid-email",
            password = "123", // Too short
            confirmPassword = "456", // Doesn't match
            firstName = "",
            lastName = ""
        )

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidSignUpRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.data").exists()) // Validation errors
    }

    @Test
    @Order(22)
    fun `should handle not found errors properly`() {
        mockMvc.perform(
            get("/api/events/99999")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Event not found"))
    }

    // ========== Health and Monitoring Tests ==========

    @Test
    @Order(23)
    fun `should access health endpoint`() {
        mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    @Order(24)
    fun `should access application info endpoint`() {
        mockMvc.perform(get("/actuator/info"))
        .andExpect(status().isOk)
    }

    // ========== API Response Format Compatibility Tests ==========

    @Test
    @Order(25)
    fun `should maintain consistent API response format across all endpoints`() {
        // Test that all successful responses follow the ApiResponse<T> format
        mockMvc.perform(
            get("/api/events/upcoming")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.timestamp").exists())

        // Test error responses also follow the format
        mockMvc.perform(get("/api/events/upcoming"))
        .andExpect(status().isUnauthorized)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @Order(26)
    fun `should handle CORS properly for web client compatibility`() {
        mockMvc.perform(
            options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
        )
        .andExpect(status().isOk)
        .andExpect(header().exists("Access-Control-Allow-Origin"))
        .andExpect(header().exists("Access-Control-Allow-Methods"))
    }
}