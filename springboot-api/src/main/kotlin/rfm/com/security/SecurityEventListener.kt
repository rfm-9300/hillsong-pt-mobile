package rfm.com.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.*
import org.springframework.security.authorization.event.AuthorizationDeniedEvent
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import rfm.com.config.SecurityLogger
import rfm.com.security.jwt.UserPrincipal

/**
 * Listener for Spring Security events to log security-related activities
 */
@Component
class SecurityEventListener {

    @EventListener
    fun onAuthenticationSuccess(event: AuthenticationSuccessEvent) {
        val authentication = event.authentication
        val userPrincipal = authentication.principal as? UserPrincipal
        val request = getCurrentRequest()
        
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.LOGIN_SUCCESS,
            userId = userPrincipal?.id,
            email = userPrincipal?.getEmail(),
            ipAddress = request?.let { getClientIpAddress(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = mapOf(
                "authenticationMethod" to authentication.javaClass.simpleName
            )
        )
    }

    @EventListener
    fun onAuthenticationFailure(event: AbstractAuthenticationFailureEvent) {
        val request = getCurrentRequest()
        val attemptedUsername = event.authentication?.name
        
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.LOGIN_FAILURE,
            email = attemptedUsername,
            ipAddress = request?.let { getClientIpAddress(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = mapOf(
                "failureReason" to event.exception.javaClass.simpleName,
                "failureMessage" to (event.exception.message ?: "Authentication failed")
            )
        )
    }

    @EventListener
    fun onAuthorizationDenied(event: AuthorizationDeniedEvent<*>) {
        val authentication = event.authentication.get()
        val userPrincipal = authentication?.principal as? UserPrincipal
        val request = getCurrentRequest()
        
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.ACCESS_DENIED,
            userId = userPrincipal?.id,
            email = userPrincipal?.getEmail(),
            ipAddress = request?.let { getClientIpAddress(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = mapOf(
                "resource" to (event.`object`?.toString() ?: "unknown"),
                "authorities" to (authentication?.authorities?.map { it.authority } ?: emptyList())
            )
        )
    }

    @EventListener
    fun onInteractiveAuthenticationSuccess(event: InteractiveAuthenticationSuccessEvent) {
        val authentication = event.authentication
        val userPrincipal = authentication.principal as? UserPrincipal
        val request = getCurrentRequest()
        
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_SUCCESS,
            userId = userPrincipal?.id,
            email = userPrincipal?.getEmail(),
            ipAddress = request?.let { getClientIpAddress(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = mapOf(
                "interactiveAuth" to true
            )
        )
    }

    /**
     * Get the current HTTP request from the request context
     */
    private fun getCurrentRequest(): HttpServletRequest? {
        return try {
            val requestAttributes = RequestContextHolder.currentRequestAttributes() as? ServletRequestAttributes
            requestAttributes?.request
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extract client IP address from request, considering proxy headers
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }
        
        return request.remoteAddr
    }
}