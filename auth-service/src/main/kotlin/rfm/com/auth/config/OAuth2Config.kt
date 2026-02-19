package rfm.com.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class OAuth2Config {
    
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
