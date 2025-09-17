package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import jakarta.mail.internet.MimeMessage

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    @Value("\${app.email.from}") private val fromEmail: String,
    @Value("\${app.email.production:false}") private val isProduction: Boolean,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    init {
        logger.info("EmailService initialized with fromEmail: $fromEmail, isProduction: $isProduction")
    }
    
    /**
     * Send verification email to user
     */
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
            throw EmailSendingException("Failed to send verification email", e)
        }
    }
    
    /**
     * Send password reset email to user
     */
    fun sendPasswordResetEmail(toEmail: String, resetToken: String, baseUrl: String) {
        if (!isProduction) {
            logger.info("Development mode: Would send password reset email to $toEmail")
            logger.info("Reset link: $baseUrl/reset-password/$resetToken")
            return
        }
        
        try {
            val resetLink = "$baseUrl/reset-password/$resetToken"
            val subject = "Password Reset Request"
            val body = createPasswordResetEmailTemplate(resetLink)
            
            sendHtmlEmail(toEmail, subject, body)
            logger.info("Password reset email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send password reset email to $toEmail: ${e.message}", e)
            throw EmailSendingException("Failed to send password reset email", e)
        }
    }
    
    /**
     * Send a simple text email
     */
    fun sendEmail(toEmail: String, subject: String, body: String) {
        if (!isProduction) {
            logger.info("Development mode: Would send email to $toEmail")
            logger.info("Subject: $subject")
            logger.info("Body: $body")
            return
        }
        
        try {
            val message = SimpleMailMessage().apply {
                setFrom(fromEmail)
                setTo(toEmail)
                setSubject(subject)
                setText(body)
            }
            
            javaMailSender.send(message)
            logger.info("Email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send email to $toEmail: ${e.message}", e)
            throw EmailSendingException("Failed to send email", e)
        }
    }
    
    /**
     * Send HTML email
     */
    private fun sendHtmlEmail(toEmail: String, subject: String, htmlBody: String) {
        try {
            val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
            
            helper.setFrom(fromEmail)
            helper.setTo(toEmail)
            helper.setSubject(subject)
            helper.setText(htmlBody, true) // true indicates HTML content
            
            javaMailSender.send(mimeMessage)
            logger.debug("HTML email sent to $toEmail")
        } catch (e: Exception) {
            logger.error("Failed to send HTML email to $toEmail: ${e.message}", e)
            throw EmailSendingException("Failed to send HTML email", e)
        }
    }
    
    /**
     * Create verification email template
     */
    private fun createVerificationEmailTemplate(verificationLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Your Email Address</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background-color: #4CAF50;
                        color: white;
                        padding: 20px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        background-color: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 5px 5px;
                    }
                    .button {
                        display: inline-block;
                        background-color: #4CAF50;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Welcome to Active Hive!</h1>
                </div>
                <div class="content">
                    <h2>Verify Your Email Address</h2>
                    <p>Thank you for registering with our church management system. To complete your registration, please verify your email address by clicking the button below:</p>
                    
                    <div style="text-align: center;">
                        <a href="$verificationLink" class="button">Verify Email Address</a>
                    </div>
                    
                    <p>If the button doesn't work, you can also copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; background-color: #f0f0f0; padding: 10px; border-radius: 3px;">
                        $verificationLink
                    </p>
                    
                    <p><strong>Important:</strong> This verification link will expire in 24 hours for security reasons.</p>
                    
                    <p>If you didn't create an account with us, please ignore this email.</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from Active Hive Church Management System.</p>
                    <p>Please do not reply to this email.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Create password reset email template
     */
    private fun createPasswordResetEmailTemplate(resetLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset Request</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background-color: #FF6B35;
                        color: white;
                        padding: 20px;
                        text-align: center;
                        border-radius: 5px 5px 0 0;
                    }
                    .content {
                        background-color: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 5px 5px;
                    }
                    .button {
                        display: inline-block;
                        background-color: #FF6B35;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border: 1px solid #ffeaa7;
                        color: #856404;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        font-size: 12px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Password Reset Request</h1>
                </div>
                <div class="content">
                    <h2>Reset Your Password</h2>
                    <p>We received a request to reset your password for your Active Hive account. If you made this request, click the button below to reset your password:</p>
                    
                    <div style="text-align: center;">
                        <a href="$resetLink" class="button">Reset Password</a>
                    </div>
                    
                    <p>If the button doesn't work, you can also copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; background-color: #f0f0f0; padding: 10px; border-radius: 3px;">
                        $resetLink
                    </p>
                    
                    <div class="warning">
                        <strong>Security Notice:</strong>
                        <ul>
                            <li>This password reset link will expire in 1 hour for security reasons</li>
                            <li>If you didn't request a password reset, please ignore this email</li>
                            <li>Your password will remain unchanged until you create a new one</li>
                        </ul>
                    </div>
                    
                    <p>If you continue to have problems, please contact our support team.</p>
                </div>
                <div class="footer">
                    <p>This is an automated message from Active Hive Church Management System.</p>
                    <p>Please do not reply to this email.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Test email connectivity
     */
    fun testEmailConnection(): Boolean {
        return try {
            javaMailSender.createMimeMessage()
            logger.info("Email service connection test successful")
            true
        } catch (e: Exception) {
            logger.error("Email service connection test failed: ${e.message}", e)
            false
        }
    }
}

/**
 * Custom exception for email sending failures
 */
class EmailSendingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)