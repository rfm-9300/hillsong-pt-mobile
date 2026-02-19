package rfm.com.integration

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import rfm.com.security.jwt.JwtTokenProvider
import java.util.*
import org.junit.jupiter.api.Assertions.*

/**
 * Test to ensure JWT token validation works correctly for tokens issued by auth-service.
 * Tokens are created inline to simulate auth-service-issued tokens.
 */
@SpringBootTest
@TestPropertySource(properties = [
    "app.jwt.secret=myTestSecretKeyThatIsLongEnoughForHS256Algorithm",
    "app.jwt.issuer=http://localhost:8080",
    "app.jwt.audience=users"
])
class JwtCompatibilityTest {

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.issuer}")
    private lateinit var jwtIssuer: String

    @Value("\${app.jwt.audience}")
    private lateinit var jwtAudience: String

    private fun createTestToken(
        userId: String,
        email: String,
        expiration: Date = Date(Date().time + 86400000)
    ): String {
        val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
        return Jwts.builder()
            .subject(userId)
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .claim("email", email)
            .issuedAt(Date())
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    @Test
    fun `should validate auth-service token with correct structure`() {
        val token = createTestToken("123", "test@example.com")
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        val tokenParts = token.split(".")
        assertEquals(3, tokenParts.size, "JWT token should have 3 parts (header.payload.signature)")
    }

    @Test
    fun `should validate auth-service token successfully`() {
        val token = createTestToken("123", "test@example.com")
        assertTrue(jwtTokenProvider.validateToken(token), "Token should be valid")
    }

    @Test
    fun `should extract correct user ID from auth-service token`() {
        val token = createTestToken("123", "test@example.com")
        assertEquals("123", jwtTokenProvider.getUserIdFromToken(token))
    }

    @Test
    fun `should extract correct email from auth-service token`() {
        val token = createTestToken("123", "test@example.com")
        assertEquals("test@example.com", jwtTokenProvider.getEmailFromToken(token))
    }

    @Test
    fun `should include required claims in auth-service token`() {
        val token = createTestToken("123", "test@example.com")
        val claims = jwtTokenProvider.getClaimsFromToken(token)

        assertEquals("123", claims.subject)
        assertEquals("test@example.com", claims["email"])
        assertEquals(jwtIssuer, claims.issuer)
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
        assertTrue(claims.expiration.after(Date()))
    }

    @Test
    fun `should reject expired auth-service token`() {
        val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
        val expiredToken = Jwts.builder()
            .subject("123")
            .claim("email", "test@example.com")
            .issuer(jwtIssuer)
            .audience().add(jwtAudience).and()
            .issuedAt(Date(Date().time - 2000))
            .expiration(Date(Date().time - 1000))
            .signWith(key)
            .compact()

        assertFalse(jwtTokenProvider.validateToken(expiredToken))
    }

    @Test
    fun `should reject token with invalid signature`() {
        val validToken = createTestToken("123", "test@example.com")
        val tamperedToken = validToken.dropLast(1) + "X"
        assertFalse(jwtTokenProvider.validateToken(tamperedToken))
    }

    @Test
    fun `should handle malformed token gracefully`() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt.token"))
    }

    @Test
    fun `should validate different user tokens correctly`() {
        val token1 = createTestToken("123", "user1@example.com")
        val token2 = createTestToken("456", "user2@example.com")

        assertTrue(token1 != token2)
        assertEquals("123", jwtTokenProvider.getUserIdFromToken(token1))
        assertEquals("user1@example.com", jwtTokenProvider.getEmailFromToken(token1))
        assertEquals("456", jwtTokenProvider.getUserIdFromToken(token2))
        assertEquals("user2@example.com", jwtTokenProvider.getEmailFromToken(token2))
    }
}