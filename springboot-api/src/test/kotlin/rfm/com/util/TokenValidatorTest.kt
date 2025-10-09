package rfm.com.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TokenValidatorTest {

    @Test
    fun `isTokenExpired should return false for future expiration time`() {
        // Given
        val futureTime = LocalDateTime.now().plusMinutes(15)

        // When
        val result = TokenValidator.isTokenExpired(futureTime)

        // Then
        assertFalse(result, "Token with future expiration should not be expired")
    }

    @Test
    fun `isTokenExpired should return true for past expiration time`() {
        // Given
        val pastTime = LocalDateTime.now().minusMinutes(15)

        // When
        val result = TokenValidator.isTokenExpired(pastTime)

        // Then
        assertTrue(result, "Token with past expiration should be expired")
    }

    @Test
    fun `isTokenExpired should return true for current time`() {
        // Given
        val currentTime = LocalDateTime.now()

        // When
        val result = TokenValidator.isTokenExpired(currentTime)

        // Then
        // Note: This might be flaky due to timing, but LocalDateTime.now().isAfter(currentTime)
        // should generally be true due to execution time
        assertTrue(result, "Token expiring at current time should be considered expired")
    }

    @Test
    fun `validateTokenFormat should return true for valid token format`() {
        // Given
        val validToken = TokenGenerator.generateSecureToken()

        // When
        val result = TokenValidator.validateTokenFormat(validToken)

        // Then
        assertTrue(result, "Valid token format should pass validation")
    }

    @Test
    fun `validateTokenFormat should return false for invalid token format`() {
        // Given
        val invalidToken = "invalid-token"

        // When
        val result = TokenValidator.validateTokenFormat(invalidToken)

        // Then
        assertFalse(result, "Invalid token format should fail validation")
    }

    @Test
    fun `validateToken should return true for valid non-expired token`() {
        // Given
        val validToken = TokenGenerator.generateSecureToken()
        val futureExpiration = LocalDateTime.now().plusMinutes(15)

        // When
        val result = TokenValidator.validateToken(validToken, futureExpiration)

        // Then
        assertTrue(result, "Valid non-expired token should pass validation")
    }

    @Test
    fun `validateToken should return false for valid but expired token`() {
        // Given
        val validToken = TokenGenerator.generateSecureToken()
        val pastExpiration = LocalDateTime.now().minusMinutes(15)

        // When
        val result = TokenValidator.validateToken(validToken, pastExpiration)

        // Then
        assertFalse(result, "Valid but expired token should fail validation")
    }

    @Test
    fun `validateToken should return false for invalid format non-expired token`() {
        // Given
        val invalidToken = "short"
        val futureExpiration = LocalDateTime.now().plusMinutes(15)

        // When
        val result = TokenValidator.validateToken(invalidToken, futureExpiration)

        // Then
        assertFalse(result, "Invalid format token should fail validation even if not expired")
    }

    @Test
    fun `validateToken should return false for invalid format expired token`() {
        // Given
        val invalidToken = "short"
        val pastExpiration = LocalDateTime.now().minusMinutes(15)

        // When
        val result = TokenValidator.validateToken(invalidToken, pastExpiration)

        // Then
        assertFalse(result, "Invalid format expired token should fail validation")
    }

    @Test
    fun `validateToken should handle edge case of expiration exactly at boundary`() {
        // Given
        val validToken = TokenGenerator.generateSecureToken()
        val expirationTime = LocalDateTime.now().plusSeconds(1)

        // When
        val result = TokenValidator.validateToken(validToken, expirationTime)

        // Then
        assertTrue(result, "Token expiring in 1 second should still be valid")
    }

    @Test
    fun `validateTokenFormat should reject token with special characters`() {
        // Given
        val tokenWithSpecialChars = "a".repeat(63) + "@"

        // When
        val result = TokenValidator.validateTokenFormat(tokenWithSpecialChars)

        // Then
        assertFalse(result, "Token with special characters should fail format validation")
    }

    @Test
    fun `validateTokenFormat should reject empty token`() {
        // Given
        val emptyToken = ""

        // When
        val result = TokenValidator.validateTokenFormat(emptyToken)

        // Then
        assertFalse(result, "Empty token should fail format validation")
    }

    @Test
    fun `validateTokenFormat should reject token with wrong length`() {
        // Given
        val shortToken = "abc123"
        val longToken = "a".repeat(100)

        // When & Then
        assertFalse(
            TokenValidator.validateTokenFormat(shortToken),
            "Short token should fail format validation"
        )
        assertFalse(
            TokenValidator.validateTokenFormat(longToken),
            "Long token should fail format validation"
        )
    }

    @Test
    fun `isTokenExpired should handle far future dates`() {
        // Given
        val farFuture = LocalDateTime.now().plusYears(10)

        // When
        val result = TokenValidator.isTokenExpired(farFuture)

        // Then
        assertFalse(result, "Token expiring in far future should not be expired")
    }

    @Test
    fun `isTokenExpired should handle far past dates`() {
        // Given
        val farPast = LocalDateTime.now().minusYears(10)

        // When
        val result = TokenValidator.isTokenExpired(farPast)

        // Then
        assertTrue(result, "Token expired long ago should be expired")
    }
}
