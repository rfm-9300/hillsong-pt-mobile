package rfm.com.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    var upload: UploadProperties = UploadProperties(),
    var email: EmailProperties = EmailProperties()
)

data class UploadProperties(
    var path: String = "./uploads"
)

data class EmailProperties(
    var from: String = "noreply@church.com",
    var production: Boolean = false
)