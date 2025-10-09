package rfm.com.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.RepeatedTest

class TokenGeneratorTest {

    @Test
    fun `generateSecureToken should return 64 character token`() {
        // When
        val token = TokenGenerator.generateSecureToken()

        // Then
        assertEquals(64, token.length, "Token should be exactly 64 characters long")
    }

    @Test
    fun `generateSecureToken should return alphanumeric token`() {
        // When
        val token = TokenGenerator.generateSecureToken()

        // Then
        assertTrue(
            token.all { it.isLetterOrDigit() },
            "Token should contain only alphanumeric characters"
        )
    }

    @RepeatedTest(100)
    fun `generateSecureToken should generate unique tokens`() {
        // Given
        val tokens = mutableSetOf<String>()

        // When
        val token = TokenGenerator.generateSecureToken()

        // Then
        assertTrue(
            tokens.add(token),
            "Generated token should be unique"
        )
    }

    @Test
    fun `generateSecureToken should use all character types`() {
        // Given
        val tokens = (1..1000).map { TokenGenerator.generateSecureToken() }
        val allChars = tokens.joinToString("").toSet()

        // Then
        assertTrue(allChars.any { it.isUpperCase() }, "Should contain uppercase letters")
        assertTrue(allChars.any { it.isLowerCase() }, "Should contain lowercase letters")
        assertTrue(allChars.any { it.isDigit() }, "Should contain digits")
    }

    @Test
    fun `isValidTokenFormat should return true for valid 64 character alphanumeric token`() {
        // Given
        val validToken = "a".repeat(32) + "1".repeat(32)

        // When
        val result = TokenGenerator.isValidTokenFormat(validToken)

        // Then
        assertTrue(result, "Should validate correct format token")
    }

    @Test
    fun `isValidTokenFormat should return false for token with invalid length`() {
        // Given
        val shortToken = "abc123"
        val longToken = "a".repeat(65)

        // When & Then
        assertFalse(
            TokenGenerator.isValidTokenFormat(shortToken),
            "Should reject token shorter than 64 characters"
        )
        assertFalse(
            TokenGenerator.isValidTokenFormat(longToken),
            "Should reject token longer than 64 characters"
        )
    }

    @Test
    fun `isValidTokenFormat should return false for token with special characters`() {
        // Given
        val tokenWithSpecialChars = "a".repeat(63) + "!"

        // When
        val result = TokenGenerator.isValidTokenFormat(tokenWithSpecialChars)

        // Then
        assertFalse(result, "Should reject token with special characters")
    }

    @Test
    fun `isValidTokenFormat should return false for token with spaces`() {
        // Given
        val tokenWithSpaces = "a".repeat(63) + " "

        // When
        val result = TokenGenerator.isValidTokenFormat(tokenWithSpaces)

        // Then
        assertFalse(result, "Should reject token with spaces")
    }

    @Test
    fun `isValidTokenFormat should return false for empty token`() {
        // Given
        val emptyToken = ""

        // When
        val result = TokenGenerator.isValidTokenFormat(emptyToken)

        // Then
        assertFalse(result, "Should reject empty token")
    }

    @Test
    fun `generated tokens should pass format validation`() {
        // Given
        val tokens = (1..100).map { TokenGenerator.generateSecureToken() }

        // When & Then
        tokens.forEach { token ->
            assertTrue(
                TokenGenerator.isValidTokenFormat(token),
                "Generated token should pass format validation"
            )
        }
    }

    @Test
    fun `generateSecureToken should not contain predictable patterns`() {
        // Given
        val tokens = (1..100).map { TokenGenerator.generateSecureToken() }

        // Then - Check that tokens don't start with the same character
        val firstChars = tokens.map { it.first() }.toSet()
        assertTrue(
            firstChars.size > 10,
            "First characters should be diverse (found ${firstChars.size} unique)"
        )

        // Check that tokens don't end with the same character
        val lastChars = tokens.map { it.last() }.toSet()
        assertTrue(
            lastChars.size > 10,
            "Last characters should be diverse (found ${lastChars.size} unique)"
        )
    }
}
