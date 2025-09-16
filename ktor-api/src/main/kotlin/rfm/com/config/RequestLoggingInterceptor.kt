package rfm.com.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import rfm.com.security.jwt.UserPrincipal
import java.util.*

/**
 * Interceptor for logging HTTP requests and responses
 */
@Component
class RequestLoggingInterceptor : HandlerInterceptor {

    companion object {
        private const val REQUEST_ID_ATTRIBUTE = "requestId"
        private const val START_TIME_ATTRIBUTE = "startTime"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val requestId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        
        // Store request metadata
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId)
        request.setAttribute(START_TIME_ATTRIBUTE, startTime)
        
        // Get current user if authenticated
        val userId = getCurrentUserId()
        
        // Set logging context
        LoggingUtils.setRequestContext(
            requestId = requestId,
            ipAddress = getClientIpAddress(request),
            userAgent = request.getHeader("User-Agent"),
            endpoint = request.requestURI,
            httpMethod = request.method
        )
        
        userId?.let { LoggingUtils.setUserContext(it) }
        
        // Log the request
        AccessLogger.logRequest(request, requestId, userId)
        
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        try {
            val requestId = request.getAttribute(REQUEST_ID_ATTRIBUTE) as? String ?: "unknown"
            val startTime = request.getAttribute(START_TIME_ATTRIBUTE) as? Long ?: System.currentTimeMillis()
            val processingTime = System.currentTimeMillis() - startTime
            
            // Log the response
            AccessLogger.logResponse(response, requestId, processingTime)
            
            // Log any exceptions
            ex?.let { exception ->
                SecurityLogger.logSecurityEvent(
                    event = SecurityLogger.SecurityEvent.SUSPICIOUS_ACTIVITY,
                    userId = getCurrentUserId(),
                    ipAddress = getClientIpAddress(request),
                    userAgent = request.getHeader("User-Agent"),
                    details = mapOf(
                        "exception" to exception.javaClass.simpleName,
                        "message" to (exception.message ?: "No message"),
                        "endpoint" to request.requestURI,
                        "method" to request.method
                    )
                )
            }
            
        } finally {
            // Clear logging context
            LoggingUtils.clearContext()
        }
    }

    /**
     * Get the current authenticated user ID
     */
    private fun getCurrentUserId(): Long? {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication?.isAuthenticated == true && authentication.principal is UserPrincipal) {
                (authentication.principal as UserPrincipal).id
            } else null
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
        
        val xForwardedProto = request.getHeader("X-Forwarded-Proto")
        if (!xForwardedProto.isNullOrBlank()) {
            return request.remoteAddr
        }
        
        return request.remoteAddr
    }
}