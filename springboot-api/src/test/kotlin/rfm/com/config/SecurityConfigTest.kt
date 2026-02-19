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
        assertNotNull(passwordEncoder)
        assertNotNull(jwtTokenProvider)
        assertNotNull(customUserDetailsService)
    }

    @Test
    fun `password encoder should work correctly`() {
        val rawPassword = "testPassword123"
        val encodedPassword = passwordEncoder.encode(rawPassword)
        assertNotNull(encodedPassword)
        assertNotEquals(rawPassword, encodedPassword)
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword))
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword))
    }
}