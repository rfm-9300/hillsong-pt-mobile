package rfm.com.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import rfm.com.service.EmailService

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceIntegrationTest {
    
    @Autowired
    private lateinit var emailService: EmailService
    
    @Test
    fun `should inject EmailService successfully`() {
        // This test verifies that the EmailService is properly configured and can be injected
        assert(::emailService.isInitialized)
    }
    
    @Test
    fun `should test email connection in development mode`() {
        // This test verifies that the email connection test works
        val result = emailService.testEmailConnection()
        // In test mode, this should return true as it just creates a MimeMessage
        assert(result)
    }
    
    @Test
    fun `should send verification email in development mode`() {
        // This test verifies that verification email can be sent without errors in development mode
        val toEmail = "test@example.com"
        val verificationToken = "test-token-123"
        
        // Should not throw any exceptions in development mode
        emailService.sendVerificationEmail(toEmail, verificationToken)
    }
    
    @Test
    fun `should send password reset email in development mode`() {
        // This test verifies that password reset email can be sent without errors in development mode
        val toEmail = "test@example.com"
        val resetToken = "reset-token-123"
        val baseUrl = "http://localhost:8080"
        
        // Should not throw any exceptions in development mode
        emailService.sendPasswordResetEmail(toEmail, resetToken, baseUrl)
    }
    
    @Test
    fun `should send simple email in development mode`() {
        // This test verifies that simple email can be sent without errors in development mode
        val toEmail = "test@example.com"
        val subject = "Test Subject"
        val body = "Test Body"
        
        // Should not throw any exceptions in development mode
        emailService.sendEmail(toEmail, subject, body)
    }
}