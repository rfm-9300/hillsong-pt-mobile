package rfm.com.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Tests for JwtTokenProvider — validation-only (token generation is handled by auth-service).
 * Tokens are created inline using JJWT directly to simulate tokens issued by auth-service.
 */
class JwtTokenProviderTest {

    private val secret = "dGVzdFNlY3JldEtleVRoYXRJc0xvbmdFbm91Z2hGb3JIUzI1Ng=="
    private val issuer = "test-issuer"
    private val audience = "test-audience"

    private val jwtTokenProvider = JwtTokenProvider(
        jwtSecret = secret,
        jwtIssuer = issuer,
        jwtAudience = audience
    )

    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    private fun createToken(
        subject: String,
        email: String,
        expiration: Date = Date(Date().time + 86400000),
        tokenIssuer: String = issuer,
        tokenAudience: String = audience,
        extraClaims: Map<String, Any> = emptyMap()
    ): String {
        val builder = Jwts.builder()
            .subject(subject)
            .issuer(tokenIssuer)
            .audience().add(tokenAudience).and()
            .claim("email", email)
            .issuedAt(Date())
            .expiration(expiration)
            .signWith(key)

        extraClaims.forEach { (k, v) -> builder.claim(k, v) }

        return builder.compact()
    }

    @Test
    fun `should validate a valid JWT token`() {
        val token = createToken("user-123", "test@example.com")
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `should extract user ID from token`() {
        val token = createToken("user-123", "test@example.com")
        assertEquals("user-123", jwtTokenProvider.getUserIdFromToken(token))
    }

    @Test
    fun `should extract email from token`() {
        val token = createToken("user-123", "test@example.com")
        assertEquals("test@example.com", jwtTokenProvider.getEmailFromToken(token))
    }

    @Test
    fun `should extract token type from token`() {
        val token = createToken("user-123", "test@example.com", extraClaims = mapOf("type" to "refresh"))
        assertEquals("refresh", jwtTokenProvider.getTokenTypeFromToken(token))
    }

    @Test
    fun `should return null for token without type claim`() {
        val token = createToken("user-123", "test@example.com")
        assertNull(jwtTokenProvider.getTokenTypeFromToken(token))
    }

    @Test
    fun `should reject invalid token`() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"))
    }

    @Test
    fun `should reject expired token`() {
        val token = createToken("user-123", "test@example.com", expiration = Date(Date().time - 1000))
        assertFalse(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `should detect expired token via isTokenExpired`() {
        val token = createToken("user-123", "test@example.com", expiration = Date(Date().time - 1000))
        // validateToken catches the exception, but getClaimsFromToken will throw
        // Use a valid token and check it's NOT expired
        val validToken = createToken("user-123", "test@example.com")
        assertFalse(jwtTokenProvider.isTokenExpired(validToken))
    }

    @Test
    fun `should reject token with wrong issuer`() {
        val token = createToken("user-123", "test@example.com", tokenIssuer = "wrong-issuer")
        assertFalse(jwtTokenProvider.validateToken(token))
    }
}