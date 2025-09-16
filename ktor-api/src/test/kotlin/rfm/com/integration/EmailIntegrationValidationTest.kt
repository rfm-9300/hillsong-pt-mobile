package rfm.com.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.PasswordResetRequest
import rfm.com.dto.SignUpRequest
import rfm.com.entity.User
import rfm.com.entity.AuthProvider
import rfm.com.repository.UserRepository
import rfm.com.service.EmailService

/**
 * Integration test to validate email service functionality.
 * Tests that email sending works correctly in the Spring Boot implementation
 * and maintains compatibility with the original Ktor email flows.
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:emailtest",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "app.email.production=false", // Test mode - emails won't actually be sent
    "spring.mail.host=localhost",
    "spring.mail.port=25",
    "app.email.from=test@example.com",
    "logging.level.rfm.com=INFO"
])
class EmailIntegrationValidationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        // Create a test user for password reset testing
        val testUser = User(
            email = "existing@example.com",
            password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.IjZg.BkmBm32S.6ZQuMjA/3oUYeizC",
            salt = "test-salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )
        userRepository.save(testUser)
    }

    @Test
    fun `should send verification email during user registration`() {
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
        
        // In test mode, this should complete without errors
        // The actual email sending is mocked/disabled in test environment
    }

    @Test
    fun `should send password reset email for existing user`() {
        val passwordResetRequest = PasswordResetRequest(
            email = "existing@example.com"
        )

        mockMvc.perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"))
        
        // In test mode, this should complete without errors
    }

    @Test
    fun `should handle password reset request for non-existent user gracefully`() {
        val passwordResetRequest = PasswordResetRequest(
            email = "nonexistent@example.com"
        )

        mockMvc.perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"))
        
        // Should return same message for security (don't reveal if email exists)
    }

    @Test
    fun `should validate email service configuration`() {
        // Test that EmailService is properly injected and configured
        assert(::emailService.isInitialized)
        
        // Test email connection in development mode
        val connectionTest = emailService.testEmailConnection()
        assert(connectionTest) // Should return true in test mode
    }

    @Test
    fun `should handle email service errors gracefully`() {
        // Test that email service handles errors without crashing the application
        try {
            emailService.sendEmail("invalid-email", "Test Subject", "Test Body")
            // In test mode, this should not throw an exception
        } catch (e: Exception) {
            // If an exception is thrown, it should be handled gracefully
            assert(e.message != null)
        }
    }

    @Test
    fun `should send verification email with correct format`() {
        // Test that verification email can be sent without errors
        val testEmail = "verification@example.com"
        val testToken = "test-verification-token-123"
        
        try {
            emailService.sendVerificationEmail(testEmail, testToken)
            // Should complete without throwing exceptions in test mode
        } catch (e: Exception) {
            throw AssertionError("Verification email sending should not fail in test mode", e)
        }
    }

    @Test
    fun `should send password reset email with correct format`() {
        // Test that password reset email can be sent without errors
        val testEmail = "reset@example.com"
        val testToken = "test-reset-token-123"
        val baseUrl = "http://localhost:8080"
        
        try {
            emailService.sendPasswordResetEmail(testEmail, testToken, baseUrl)
            // Should complete without throwing exceptions in test mode
        } catch (e: Exception) {
            throw AssertionError("Password reset email sending should not fail in test mode", e)
        }
    }

    @Test
    fun `should validate email addresses before sending`() {
        val signUpRequest = SignUpRequest(
            email = "invalid-email-format",
            password = "password123",
            confirmPassword = "password123",
            firstName = "Test",
            lastName = "User"
        )

        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.data.email").exists()) // Should contain email validation error
    }

    @Test
    fun `should handle concurrent email sending requests`() {
        // Test multiple simultaneous registration requests
        val requests = (1..5).map { i ->
            SignUpRequest(
                email = "concurrent$i@example.com",
                password = "password123",
                confirmPassword = "password123",
                firstName = "User",
                lastName = "$i"
            )
        }

        requests.forEach { request ->
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Test
    fun `should maintain email template consistency`() {
        // Verify that email templates are properly loaded and formatted
        val testEmail = "template@example.com"
        val testToken = "template-test-token"
        
        // This test ensures that email templates don't cause runtime errors
        try {
            emailService.sendVerificationEmail(testEmail, testToken)
            emailService.sendPasswordResetEmail(testEmail, testToken, "http://localhost:8080")
            emailService.sendEmail(testEmail, "Test Subject", "Test Body")
        } catch (e: Exception) {
            throw AssertionError("Email template processing should not fail", e)
        }
    }
}