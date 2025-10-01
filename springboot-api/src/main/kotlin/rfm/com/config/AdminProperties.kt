package rfm.com.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.admin")
data class AdminProperties(
    var token: String = "admin-secret-token-12345"
)