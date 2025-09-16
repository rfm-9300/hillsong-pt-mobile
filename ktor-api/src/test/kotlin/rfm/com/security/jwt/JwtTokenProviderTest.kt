package rfm.com.security.jwt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "app.jwt.secret=testSecretKey",
    "app.jwt.expiration=86400000",
    "app.jwt.issuer=test-issuer",
    "app.jwt.audience=test-audience"
])
class JwtTokenProviderTest {

    private val jwtTokenProvider = JwtTokenProvider(
        jwtSecret = "testSecretKey",
        jwtExpiration = 86400000,
        jwtIssuer = "test-issuer",
        jwtAudience = "test-audience"
    )

    @Test
    fun `should generate valid JWT token`() {
        // Given
        val userId = 1L
        val email = "test@example.com"

        // When
        val token = jwtTokenProvider.generateTokenFromUserId(userId, email)

        // Then
        assertTrue(token.isNotEmpty())
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `should extract user ID from token`() {
        // Given
        val userId = 123L
        val email = "test@example.com"
        val token = jwtTokenProvider.generateTokenFromUserId(userId, email)

        // When
        val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }

    @Test
    fun `should extract email from token`() {
        // Given
        val userId = 1L
        val email = "test@example.com"
        val token = jwtTokenProvider.generateTokenFromUserId(userId, email)

        // When
        val extractedEmail = jwtTokenProvider.getEmailFromToken(token)

        // Then
        assertEquals(email, extractedEmail)
    }

    @Test
    fun `should generate verification token with correct type`() {
        // Given
        val userId = 1L
        val email = "test@example.com"

        // When
        val token = jwtTokenProvider.generateVerificationToken(userId, email)

        // Then
        assertTrue(jwtTokenProvider.validateToken(token))
        assertEquals("verification", jwtTokenProvider.getTokenTypeFromToken(token))
    }

    @Test
    fun `should generate refresh token with correct type`() {
        // Given
        val userId = 1L
        val email = "test@example.com"

        // When
        val token = jwtTokenProvider.generateRefreshToken(userId, email)

        // Then
        assertTrue(jwtTokenProvider.validateToken(token))
        assertEquals("refresh", jwtTokenProvider.getTokenTypeFromToken(token))
    }

    @Test
    fun `should reject invalid token`() {
        // Given
        val invalidToken = "invalid.jwt.token"

        // When & Then
        assertFalse(jwtTokenProvider.validateToken(invalidToken))
    }

    @Test
    fun `should reject expired token`() {
        // Given - create a token provider with very short expiration
        val shortExpirationProvider = JwtTokenProvider(
            jwtSecret = "testSecretKey",
            jwtExpiration = 1, // 1 millisecond
            jwtIssuer = "test-issuer",
            jwtAudience = "test-audience"
        )
        
        val token = shortExpirationProvider.generateTokenFromUserId(1L, "test@example.com")
        
        // Wait for token to expire
        Thread.sleep(10)

        // When & Then
        assertFalse(shortExpirationProvider.validateToken(token))
    }
}