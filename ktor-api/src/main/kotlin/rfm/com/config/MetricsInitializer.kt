package rfm.com.config

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * Initializes metrics integration with logging utilities
 */
@Component
class MetricsInitializer(
    private val securityMetrics: SecurityMetrics
) {
    
    @PostConstruct
    fun initializeMetrics() {
        // Wire up security metrics with the SecurityLogger
        SecurityLogger.setSecurityMetrics(securityMetrics)
    }
}