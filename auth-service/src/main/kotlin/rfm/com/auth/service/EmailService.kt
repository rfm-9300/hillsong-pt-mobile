package rfm.com.auth.service

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    @Value("\${app.email.from:noreply@church.com}") private val fromEmail: String,
    @Value("\${app.email.production:false}") private val isProduction: Boolean,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    fun sendVerificationEmail(toEmail: String, verificationToken: String) {
        if (!isProduction) {
            logger.info("Development mode: Would send verification email to $toEmail")
            logger.info("Verification link: $baseUrl/api/auth/verify?token=$verificationToken")
            return
        }
        
        try {
            val verificationLink = "$baseUrl/api/auth/verify?token=$verificationToken"
            val subject = "Verify Your Email Address"
            val body = createVerificationEmailTemplate(verificationLink)
            
            sendHtmlEmail(toEmail, subject, body)
            logger.info("Verification email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send verification email to $toEmail: ${e.message}", e)
        }
    }
    
    fun sendPasswordResetEmail(toEmail: String, resetToken: String) {
         if (!isProduction) {
            logger.info("Development mode: Would send password reset email to $toEmail")
            logger.info("Reset link: $baseUrl/api/auth/reset-password?token=$resetToken")
            return
        }

        try {
            val resetLink = "$baseUrl/api/auth/reset-password?token=$resetToken"
            val subject = "Password Reset Request"
            val body = createPasswordResetEmailTemplate(resetLink)

            sendHtmlEmail(toEmail, subject, body)
            logger.info("Password reset email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send password reset email to $toEmail", e)
        }
    }
    
    private fun sendHtmlEmail(toEmail: String, subject: String, htmlBody: String) {
        val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
        
        helper.setFrom(fromEmail)
        helper.setTo(toEmail)
        helper.setSubject(subject)
        helper.setText(htmlBody, true)
        
        javaMailSender.send(mimeMessage)
    }
    
    private fun createVerificationEmailTemplate(verificationLink: String): String {
        return """
            <html><body>
                <h2>Verify Your Email</h2>
                <p>Click <a href="$verificationLink">here</a> to verify your email.</p>
            </body></html>
        """.trimIndent()
    }

    private fun createPasswordResetEmailTemplate(resetLink: String): String {
        return """
            <html><body>
                <h2>Reset Password</h2>
                <p>Click <a href="$resetLink">here</a> to reset your password.</p>
            </body></html>
        """.trimIndent()
    }
}
