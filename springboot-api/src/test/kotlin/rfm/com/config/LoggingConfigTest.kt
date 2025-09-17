package rfm.com.config

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.slf4j.MDC
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LoggingConfigTest {

    @BeforeEach
    fun setUp() {
        MDC.clear()
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `should set and clear user context in MDC`() {
        // Given
        val userId = 123L
        val sessionId = "session-123"

        // When
        LoggingUtils.setUserContext(userId, sessionId)

        // Then
        assert(MDC.get("userId") == userId.toString())
        assert(MDC.get("sessionId") == sessionId)

        // When
        LoggingUtils.clearUserContext()

        // Then
        assert(MDC.get("userId") == null)
        assert(MDC.get("sessionId") == null)
    }

    @Test
    fun `should set and clear request context in MDC`() {
        // Given
        val requestId = "req-123"
        val ipAddress = "192.168.1.1"
        val userAgent = "Mozilla/5.0"
        val endpoint = "/api/users"
        val httpMethod = "GET"

        // When
        LoggingUtils.setRequestContext(requestId, ipAddress, userAgent, endpoint, httpMethod)

        // Then
        assert(MDC.get("requestId") == requestId)
        assert(MDC.get("ipAddress") == ipAddress)
        assert(MDC.get("userAgent") == userAgent)
        assert(MDC.get("endpoint") == endpoint)
        assert(MDC.get("httpMethod") == httpMethod)

        // When
        LoggingUtils.clearRequestContext()

        // Then
        assert(MDC.get("requestId") == null)
        assert(MDC.get("ipAddress") == null)
        assert(MDC.get("userAgent") == null)
        assert(MDC.get("endpoint") == null)
        assert(MDC.get("httpMethod") == null)
    }

    @Test
    fun `should clear all MDC context`() {
        // Given
        LoggingUtils.setUserContext(123L, "session-123")
        LoggingUtils.setRequestContext("req-123", "192.168.1.1", "Mozilla/5.0", "/api/users", "GET")

        // When
        LoggingUtils.clearContext()

        // Then
        assert(MDC.getCopyOfContextMap()?.isEmpty() ?: true)
    }

    @Test
    fun `should log security events without throwing exceptions`() {
        // Given
        val securityMetrics = mockk<SecurityMetrics>(relaxed = true)
        SecurityLogger.setSecurityMetrics(securityMetrics)

        // When - should not throw any exceptions
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.LOGIN_SUCCESS,
            userId = 123L,
            email = "test@example.com",
            ipAddress = "192.168.1.1",
            userAgent = "Mozilla/5.0",
            details = mapOf("authMethod" to "password")
        )

        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.LOGIN_FAILURE,
            email = "test@example.com",
            ipAddress = "192.168.1.1",
            details = mapOf("reason" to "invalid_credentials")
        )

        // Then
        verify { securityMetrics.incrementLoginSuccesses() }
        verify { securityMetrics.incrementLoginAttempts() }
        verify { securityMetrics.incrementLoginFailures() }
    }

    @Test
    fun `should handle null values gracefully in security logging`() {
        // When - should not throw any exceptions with null values
        SecurityLogger.logSecurityEvent(
            event = SecurityLogger.SecurityEvent.TOKEN_VALIDATION_FAILURE,
            userId = null,
            email = null,
            ipAddress = null,
            userAgent = null,
            details = emptyMap()
        )

        // Then - no exception should be thrown
        // Test passes if no exception is thrown
    }
}