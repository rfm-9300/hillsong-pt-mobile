package rfm.com.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import jakarta.mail.internet.MimeMessage
import jakarta.mail.Session
import java.util.*

class EmailServiceTest {
    
    private lateinit var javaMailSender: JavaMailSender
    private lateinit var emailService: EmailService
    private lateinit var mimeMessage: MimeMessage
    
    private val fromEmail = "test@example.com"
    private val baseUrl = "http://localhost:8080"
    
    @BeforeEach
    fun setUp() {
        javaMailSender = mockk()
        mimeMessage = MimeMessage(Session.getDefaultInstance(Properties()))
        
        every { javaMailSender.createMimeMessage() } returns mimeMessage
        every { javaMailSender.send(any<MimeMessage>()) } returns Unit
        every { javaMailSender.send(any<SimpleMailMessage>()) } returns Unit
    }
    
    @Test
    fun `should send verification email in production mode`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        val toEmail = "user@example.com"
        val verificationToken = "test-token-123"
        
        // When
        emailService.sendVerificationEmail(toEmail, verificationToken)
        
        // Then
        verify { javaMailSender.send(any<MimeMessage>()) }
    }
    
    @Test
    fun `should not send verification email in development mode`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, false, baseUrl)
        val toEmail = "user@example.com"
        val verificationToken = "test-token-123"
        
        // When
        emailService.sendVerificationEmail(toEmail, verificationToken)
        
        // Then
        verify(exactly = 0) { javaMailSender.send(any<MimeMessage>()) }
    }
    
    @Test
    fun `should send password reset email in production mode`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        val toEmail = "user@example.com"
        val resetToken = "reset-token-123"
        
        // When
        emailService.sendPasswordResetEmail(toEmail, resetToken, baseUrl)
        
        // Then
        verify { javaMailSender.send(any<MimeMessage>()) }
    }
    
    @Test
    fun `should not send password reset email in development mode`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, false, baseUrl)
        val toEmail = "user@example.com"
        val resetToken = "reset-token-123"
        
        // When
        emailService.sendPasswordResetEmail(toEmail, resetToken, baseUrl)
        
        // Then
        verify(exactly = 0) { javaMailSender.send(any<MimeMessage>()) }
    }
    
    @Test
    fun `should send simple email in production mode`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        val toEmail = "user@example.com"
        val subject = "Test Subject"
        val body = "Test Body"
        
        // When
        emailService.sendEmail(toEmail, subject, body)
        
        // Then
        verify { javaMailSender.send(any<SimpleMailMessage>()) }
    }
    
    @Test
    fun `should throw EmailSendingException when JavaMailSender fails`() {
        // Given
        every { javaMailSender.send(any<MimeMessage>()) } throws RuntimeException("Mail server error")
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        
        // When & Then
        assertThrows<EmailSendingException> {
            emailService.sendVerificationEmail("user@example.com", "token")
        }
    }
    
    @Test
    fun `should test email connection successfully`() {
        // Given
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        
        // When
        val result = emailService.testEmailConnection()
        
        // Then
        assert(result)
        verify { javaMailSender.createMimeMessage() }
    }
    
    @Test
    fun `should return false when email connection test fails`() {
        // Given
        every { javaMailSender.createMimeMessage() } throws RuntimeException("Connection failed")
        emailService = EmailService(javaMailSender, fromEmail, true, baseUrl)
        
        // When
        val result = emailService.testEmailConnection()
        
        // Then
        assert(!result)
    }
}