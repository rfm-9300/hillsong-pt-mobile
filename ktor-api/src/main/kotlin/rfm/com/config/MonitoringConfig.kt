package rfm.com.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Configuration for application monitoring and metrics
 */
@Configuration
class MonitoringConfig {

    /**
     * Custom metrics for security events
     */
    @Bean
    fun securityMetrics(meterRegistry: MeterRegistry): SecurityMetrics {
        return SecurityMetrics(meterRegistry)
    }

    /**
     * Custom metrics for API performance
     */
    @Bean
    fun apiMetrics(meterRegistry: MeterRegistry): ApiMetrics {
        return ApiMetrics(meterRegistry)
    }
}

/**
 * Security-related metrics
 */
class SecurityMetrics(private val meterRegistry: MeterRegistry) {
    
    private val loginAttempts = Counter.builder("security.login.attempts")
        .description("Number of login attempts")
        .tag("status", "total")
        .register(meterRegistry)
    
    private val loginSuccesses = Counter.builder("security.login.successes")
        .description("Number of successful logins")
        .register(meterRegistry)
    
    private val loginFailures = Counter.builder("security.login.failures")
        .description("Number of failed logins")
        .register(meterRegistry)
    
    private val tokenValidations = Counter.builder("security.token.validations")
        .description("Number of token validations")
        .tag("status", "total")
        .register(meterRegistry)
    
    private val tokenValidationFailures = Counter.builder("security.token.validation.failures")
        .description("Number of failed token validations")
        .register(meterRegistry)
    
    private val accessDenied = Counter.builder("security.access.denied")
        .description("Number of access denied events")
        .register(meterRegistry)
    
    fun incrementLoginAttempts() = loginAttempts.increment()
    fun incrementLoginSuccesses() = loginSuccesses.increment()
    fun incrementLoginFailures() = loginFailures.increment()
    fun incrementTokenValidations() = tokenValidations.increment()
    fun incrementTokenValidationFailures() = tokenValidationFailures.increment()
    fun incrementAccessDenied() = accessDenied.increment()
}

/**
 * API performance metrics
 */
class ApiMetrics(private val meterRegistry: MeterRegistry) {
    
    private val requestTimer = Timer.builder("api.request.duration")
        .description("API request processing time")
        .register(meterRegistry)
    
    private val requestCounter = Counter.builder("api.requests")
        .description("Number of API requests")
        .register(meterRegistry)
    
    private val errorCounter = Counter.builder("api.errors")
        .description("Number of API errors")
        .register(meterRegistry)
    
    fun recordRequestTime(duration: Duration) = requestTimer.record(duration)
    fun incrementRequests() = requestCounter.increment()
    fun incrementErrors() = errorCounter.increment()
    
    fun startTimer(): Timer.Sample = Timer.start(meterRegistry)
}

