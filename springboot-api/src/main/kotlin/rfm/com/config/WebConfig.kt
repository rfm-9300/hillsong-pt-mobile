package rfm.com.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val requestLoggingInterceptor: RequestLoggingInterceptor
) : WebMvcConfigurer {
    
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")  // Allow all origins for development
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)  // Must be false when using wildcard origins
            .maxAge(3600) // Cache preflight for 1 hour
    }
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLoggingInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/health", "/actuator/**")
    }
}