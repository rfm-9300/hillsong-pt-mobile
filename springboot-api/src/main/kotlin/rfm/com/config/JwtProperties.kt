package rfm.com.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var secret: String = "mySecretKey",
    var expiration: Long = 86400000, // 24 hours
    var issuer: String = "church-management-api",
    var audience: String = "church-management-client"
)