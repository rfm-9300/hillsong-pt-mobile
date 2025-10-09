package rfm.com.util

import java.security.SecureRandom

/**
 * Utility class for generating secure tokens for check-in requests
 */
object TokenGenerator {
    
    private const val TOKEN_LENGTH = 64
    private const val ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private val secureRandom = SecureRandom()
    
    /**
     * Generates a cryptographically secure random token
     * 
     * @return A 64-character alphanumeric token
     */
    fun generateSecureToken(): String {
        return (1..TOKEN_LENGTH)
            .map { ALPHANUMERIC_CHARS[secureRandom.nextInt(ALPHANUMERIC_CHARS.length)] }
            .joinToString("")
    }
    
    /**
     * Validates that a token matches the expected format
     * 
     * @param token The token to validate
     * @return true if the token is 64 characters and alphanumeric, false otherwise
     */
    fun isValidTokenFormat(token: String): Boolean {
        return token.length == TOKEN_LENGTH && token.all { it in ALPHANUMERIC_CHARS }
    }
}
