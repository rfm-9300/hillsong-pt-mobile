package rfm.com.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import rfm.com.security.jwt.JwtTokenProvider
import rfm.com.service.CustomUserDetailsService
import org.junit.jupiter.api.Assertions.*

@SpringBootTest
@TestPropertySource(properties = [
    "app.jwt.secret=testSecretKey",
    "app.jwt.expiration=86400000",
    "app.jwt.issuer=test-issuer",
    "app.jwt.audience=test-audience"
])
class SecurityConfigTest {

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun `should load security beans correctly`() {
        // Verify that all security beans are loaded
        assertNotNull(passwordEncoder)
        assertNotNull(jwtTokenProvider)
        assertNotNull(customUserDetailsService)
    }

    @Test
    fun `password encoder should work correctly`() {
        // Given
        val rawPassword = "testPassword123"

        // When
        val encodedPassword = passwordEncoder.encode(rawPassword)

        // Then
        assertNotNull(encodedPassword)
        assertNotEquals(rawPassword, encodedPassword)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword))
    }

    @Test
    fun `JWT token provider should generate valid tokens`() {
        // Given
        val userId = 1L
        val email = "test@example.com"

        // When
        val token = jwtTokenProvider.generateTokenFromUserId(userId, email)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
        assertTrue(jwtTokenProvider.validateToken(token))
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token))
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token))
    }
}