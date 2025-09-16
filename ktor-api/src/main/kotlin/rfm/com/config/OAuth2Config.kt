package rfm.com.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class OAuth2Config {
    
    /**
     * RestTemplate bean for making HTTP requests to OAuth2 providers
     */
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}