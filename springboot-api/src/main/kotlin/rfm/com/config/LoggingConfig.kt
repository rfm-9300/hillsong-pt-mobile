package rfm.com.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.CommonsRequestLoggingFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Configuration for application logging and monitoring
 */
@Configuration
class LoggingConfig {

    /**
     * Configure request logging filter for debugging and monitoring
     */
    @Bean
    fun requestLoggingFilter(): CommonsRequestLoggingFilter {
        val loggingFilter = CommonsRequestLoggingFilter()
        loggingFilter.setIncludeClientInfo(true)
        loggingFilter.setIncludeQueryString(true)
        loggingFilter.setIncludePayload(true)
        loggingFilter.setMaxPayloadLength(10000)
        loggingFilter.setIncludeHeaders(false)
        loggingFilter.setAfterMessagePrefix("REQUEST DATA: ")
        return loggingFilter
    }
}

/**
 * Utility class for structured logging with MDC (Mapped Diagnostic Context)
 */
object LoggingUtils {
    
    private const val USER_ID_KEY = "userId"
    private const val REQUEST_ID_KEY = "requestId"
    private const val SESSION_ID_KEY = "sessionId"
    private const val IP_ADDRESS_KEY = "ipAddress"
    private const val USER_AGENT_KEY = "userAgent"
    private const val ENDPOINT_KEY = "endpoint"
    private const val HTTP_METHOD_KEY = "httpMethod"
    
    /**
     * Set user context in MDC for logging
     */
    fun setUserContext(userId: Long?, sessionId: String? = null) {
        userId?.let { MDC.put(USER_ID_KEY, it.toString()) }
        sessionId?.let { MDC.put(SESSION_ID_KEY, it) }
    }
    
    /**
     * Set request context in MDC for logging
     */
    fun setRequestContext(
        requestId: String,
        ipAddress: String?,
        userAgent: String?,
        endpoint: String,
        httpMethod: String
    ) {
        MDC.put(REQUEST_ID_KEY, requestId)
        ipAddress?.let { MDC.put(IP_ADDRESS_KEY, it) }
        userAgent?.let { MDC.put(USER_AGENT_KEY, it) }
        MDC.put(ENDPOINT_KEY, endpoint)
        MDC.put(HTTP_METHOD_KEY, httpMethod)
    }
    
    /**
     * Clear all MDC context
     */
    fun clearContext() {
        MDC.clear()
    }
    
    /**
     * Clear user context from MDC
     */
    fun clearUserContext() {
        MDC.remove(USER_ID_KEY)
        MDC.remove(SESSION_ID_KEY)
    }
    
    /**
     * Clear request context from MDC
     */
    fun clearRequestContext() {
        MDC.remove(REQUEST_ID_KEY)
        MDC.remove(IP_ADDRESS_KEY)
        MDC.remove(USER_AGENT_KEY)
        MDC.remove(ENDPOINT_KEY)
        MDC.remove(HTTP_METHOD_KEY)
    }
}

/**
 * Security event logging utility
 */
object SecurityLogger {
    
    private val logger: Logger = LoggerFactory.getLogger("SECURITY")
    private var securityMetrics: SecurityMetrics? = null
    
    /**
     * Set the security metrics instance for recording metrics
     */
    fun setSecurityMetrics(metrics: SecurityMetrics) {
        securityMetrics = metrics
    }
    
    enum class SecurityEvent {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        TOKEN_VALIDATION_SUCCESS,
        TOKEN_VALIDATION_FAILURE,
        ACCESS_DENIED,
        ACCOUNT_LOCKED,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_SUCCESS,
        EMAIL_VERIFICATION_SUCCESS,
        EMAIL_VERIFICATION_FAILURE,
        OAUTH_LOGIN_SUCCESS,
        OAUTH_LOGIN_FAILURE,
        UNAUTHORIZED_ACCESS_ATTEMPT,
        SUSPICIOUS_ACTIVITY
    }
    
    /**
     * Log security events with structured data
     */
    fun logSecurityEvent(
        event: SecurityEvent,
        userId: Long? = null,
        email: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        details: Map<String, Any> = emptyMap()
    ) {
        try {
            // Set MDC context for this security event
            userId?.let { MDC.put("securityUserId", it.toString()) }
            email?.let { MDC.put("securityEmail", it) }
            ipAddress?.let { MDC.put("securityIpAddress", it) }
            userAgent?.let { MDC.put("securityUserAgent", it) }
            MDC.put("securityEvent", event.name)
            
            // Add additional details to MDC
            details.forEach { (key, value) ->
                MDC.put("security_$key", value.toString())
            }
            
            val message = buildString {
                append("Security Event: ${event.name}")
                userId?.let { append(" | User ID: $it") }
                email?.let { append(" | Email: $it") }
                ipAddress?.let { append(" | IP: $it") }
                if (details.isNotEmpty()) {
                    append(" | Details: $details")
                }
            }
            
            // Record metrics
            securityMetrics?.let { metrics ->
                when (event) {
                    SecurityEvent.LOGIN_SUCCESS -> {
                        metrics.incrementLoginSuccesses()
                        metrics.incrementLoginAttempts()
                    }
                    SecurityEvent.LOGIN_FAILURE -> {
                        metrics.incrementLoginFailures()
                        metrics.incrementLoginAttempts()
                    }
                    SecurityEvent.TOKEN_VALIDATION_SUCCESS -> metrics.incrementTokenValidations()
                    SecurityEvent.TOKEN_VALIDATION_FAILURE -> {
                        metrics.incrementTokenValidationFailures()
                        metrics.incrementTokenValidations()
                    }
                    SecurityEvent.ACCESS_DENIED -> metrics.incrementAccessDenied()
                    else -> { /* No specific metric for this event */ }
                }
            }
            
            when (event) {
                SecurityEvent.LOGIN_FAILURE,
                SecurityEvent.TOKEN_VALIDATION_FAILURE,
                SecurityEvent.ACCESS_DENIED,
                SecurityEvent.ACCOUNT_LOCKED,
                SecurityEvent.EMAIL_VERIFICATION_FAILURE,
                SecurityEvent.OAUTH_LOGIN_FAILURE,
                SecurityEvent.UNAUTHORIZED_ACCESS_ATTEMPT,
                SecurityEvent.SUSPICIOUS_ACTIVITY -> logger.warn(message)
                
                else -> logger.info(message)
            }
            
        } finally {
            // Clean up security-specific MDC keys
            MDC.remove("securityUserId")
            MDC.remove("securityEmail")
            MDC.remove("securityIpAddress")
            MDC.remove("securityUserAgent")
            MDC.remove("securityEvent")
            
            // Remove additional details from MDC
            details.keys.forEach { key ->
                MDC.remove("security_$key")
            }
        }
    }
}

/**
 * Access logging utility for HTTP requests and responses
 */
object AccessLogger {
    
    private val logger: Logger = LoggerFactory.getLogger("ACCESS")
    
    /**
     * Log HTTP request access
     */
    fun logRequest(
        request: HttpServletRequest,
        requestId: String,
        userId: Long? = null
    ) {
        try {
            MDC.put("accessRequestId", requestId)
            MDC.put("accessMethod", request.method)
            MDC.put("accessUri", request.requestURI)
            MDC.put("accessQueryString", request.queryString ?: "")
            MDC.put("accessRemoteAddr", request.remoteAddr)
            MDC.put("accessUserAgent", request.getHeader("User-Agent") ?: "")
            userId?.let { MDC.put("accessUserId", it.toString()) }
            
            logger.info("HTTP Request: ${request.method} ${request.requestURI}")
            
        } finally {
            // Keep MDC for response logging
        }
    }
    
    /**
     * Log HTTP response access
     */
    fun logResponse(
        response: HttpServletResponse,
        requestId: String,
        processingTimeMs: Long
    ) {
        try {
            MDC.put("accessResponseStatus", response.status.toString())
            MDC.put("accessProcessingTime", processingTimeMs.toString())
            
            logger.info("HTTP Response: ${response.status} | Processing time: ${processingTimeMs}ms")
            
        } finally {
            // Clean up access-specific MDC keys
            MDC.remove("accessRequestId")
            MDC.remove("accessMethod")
            MDC.remove("accessUri")
            MDC.remove("accessQueryString")
            MDC.remove("accessRemoteAddr")
            MDC.remove("accessUserAgent")
            MDC.remove("accessUserId")
            MDC.remove("accessResponseStatus")
            MDC.remove("accessProcessingTime")
        }
    }
}