package rfm.com.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.*
import rfm.com.entity.AuthProvider
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.repository.UserProfileRepository
import rfm.com.repository.UserRepository
import rfm.com.security.jwt.JwtTokenProvider
import rfm.com.service.EmailService
import java.time.LocalDateTime

/**
 * Final validation test for the Ktor to Spring Boot migration.
 * This test validates the key requirements from task 22:
 * - API endpoints with same request/response format as Ktor version
 * - JWT token compatibility and authentication flows
 * - File upload and serving functionality
 * - Email sending integration
 * - OAuth2 integration setup
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:finaltest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.email.production=false",
    "logging.level.rfm.com=INFO"
])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FinalValidationTest {

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

    @Autowired
    private lateinit var emailService: EmailService

    private lateinit var testUser: User
    private lateinit var jwtToken: String

    @BeforeEach
    fun setUp() {
        // Create test user for authentication
        testUser = User(
            email = "validation@example.com",
            password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.IjZg.BkmBm32S.6ZQuMjA/3oUYeizC",
            salt = "test-salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )
        testUser = userRepository.save(testUser)

        val testUserProfile = UserProfile(
            user = testUser,
            firstName = "Validation",
            lastName = "User",
            email = testUser.email,
            phone = "1234567890"
        )
        userProfileRepository.save(testUserProfile)

        jwtToken = jwtTokenProvider.generateTokenFromUserId(testUser.id!!, testUser.email)
    }

    @AfterEach
    fun tearDown() {
        userProfileRepository.deleteAll()
        userRepository.deleteAll()
    }

    // ========== Requirement 1.3: API endpoints with same request/response format ==========

    @Test
    @Order(1)
    fun `should maintain consistent API response format across endpoints`() {
        // Test authentication endpoint
        val loginRequest = AuthRequest(
            email = "validation@example.com",
            password = "password"
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.data.token").exists())
        .andExpect(jsonPath("$.data.user").exists())
    }

    @Test
    @Order(2)
    fun `should handle validation errors with consistent format`() {
        val invalidRequest = AuthRequest(
            email = "invalid-email",
            password = ""
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    // ========== Requirement 2.4: JWT token compatibility ==========

    @Test
    @Order(3)
    fun `should generate and validate JWT tokens correctly`() {
        val userId = 123L
        val email = "jwt@example.com"
        
        // Generate token
        val token = jwtTokenProvider.generateTokenFromUserId(userId, email)
        
        // Validate token structure
        val tokenParts = token.split(".")
        assertEquals(3, tokenParts.size, "JWT should have 3 parts")
        
        // Validate token
        assertTrue(jwtTokenProvider.validateToken(token), "Token should be valid")
        
        // Extract claims
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token))
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token))
    }

    @Test
    @Order(4)
    fun `should authenticate with JWT token`() {
        mockMvc.perform(
            get("/api/profile/me")
                .header("Authorization", "Bearer $jwtToken")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.email").value("validation@example.com"))
    }

    @Test
    @Order(5)
    fun `should reject invalid JWT token`() {
        mockMvc.perform(
            get("/api/profile/me")
                .header("Authorization", "Bearer invalid-token")
        )
        .andExpect(status().isUnauthorized)
    }

    // ========== Requirement 4.4: File upload and serving functionality ==========

    @Test
    @Order(6)
    fun `should upload image file successfully`() {
        val imageFile = MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            createTestImageBytes()
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
    @Order(7)
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
    @Order(8)
    fun `should serve uploaded files`() {
        // First upload a file
        val imageFile = MockMultipartFile(
            "file",
            "serve-test.jpg",
            "image/jpeg",
            createTestImageBytes()
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

        // Then serve the file
        mockMvc.perform(get("/api/files/$fileName"))
        .andExpect(status().isOk)
        .andExpect(header().string("Content-Type", "image/jpeg"))
    }

    // ========== Requirement 8.6: Email sending integration ==========

    @Test
    @Order(9)
    fun `should send verification email during registration`() {
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

    @Test
    @Order(10)
    fun `should handle password reset email request`() {
        val passwordResetRequest = PasswordResetRequest(
            email = "validation@example.com"
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

    @Test
    @Order(11)
    fun `should validate email service configuration`() {
        // Test that EmailService is properly configured
        assertTrue(::emailService.isInitialized, "EmailService should be initialized")
        
        // Test email connection in development mode
        val connectionTest = emailService.testEmailConnection()
        assertTrue(connectionTest, "Email connection test should pass in development mode")
    }

    // ========== OAuth2 Integration Setup ==========

    @Test
    @Order(12)
    fun `should handle Google OAuth2 login endpoint`() {
        val googleAuthRequest = GoogleAuthRequest(
            idToken = "mock-google-token"
        )

        // This will fail with invalid token, but validates endpoint structure
        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @Order(13)
    fun `should handle Facebook OAuth2 login endpoint`() {
        val facebookAuthRequest = FacebookAuthRequest(
            accessToken = "mock-facebook-token"
        )

        // This will fail with invalid token, but validates endpoint structure
        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facebookAuthRequest))
        )
        .andExpect(status().isBadRequest) // Expected due to mock token
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
    }

    // ========== Additional Validation Tests ==========

    @Test
    @Order(14)
    fun `should handle CORS properly`() {
        mockMvc.perform(
            options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type")
        )
        .andExpect(status().isOk)
        .andExpect(header().exists("Access-Control-Allow-Origin"))
    }

    @Test
    @Order(15)
    fun `should access health endpoints`() {
        mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    @Order(16)
    fun `should handle malformed JSON gracefully`() {
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    private fun createTestImageBytes(): ByteArray {
        // Create a minimal valid JPEG header for testing
        return byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), // JPEG SOI marker
            0xFF.toByte(), 0xE0.toByte(), // JFIF marker
            0x00, 0x10, // Length
            0x4A, 0x46, 0x49, 0x46, 0x00, // "JFIF\0"
            0x01, 0x01, // Version
            0x01, // Units
            0x00, 0x48, 0x00, 0x48, // X and Y density
            0x00, 0x00, // Thumbnail dimensions
            0xFF.toByte(), 0xD9.toByte() // JPEG EOI marker
        )
    }

    private fun assertEquals(expected: Any?, actual: Any?, message: String = "") {
        if (expected != actual) {
            throw AssertionError("$message: Expected $expected but was $actual")
        }
    }

    private fun assertTrue(condition: Boolean, message: String = "") {
        if (!condition) {
            throw AssertionError(message)
        }
    }
}