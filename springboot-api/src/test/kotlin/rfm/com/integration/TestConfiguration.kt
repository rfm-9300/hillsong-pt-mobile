package rfm.com.integration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import rfm.com.service.EmailService
import java.util.*

@TestConfiguration
@Profile("test")
class IntegrationTestConfiguration {

    @Bean
    @Primary
    fun testJavaMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "localhost"
        mailSender.port = 25
        
        val props = Properties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = false
        props["mail.smtp.starttls.enable"] = false
        props["mail.debug"] = false
        
        mailSender.javaMailProperties = props
        return mailSender
    }

    @Bean
    @Primary
    fun testEmailService(javaMailSender: JavaMailSender): EmailService {
        return EmailService(
            javaMailSender = javaMailSender,
            fromEmail = "test@example.com",
            isProduction = false,
            baseUrl = "http://localhost:8080"
        )
    }
}