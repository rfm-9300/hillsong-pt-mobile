package rfm.com.util

import java.time.LocalDateTime

/**
 * Utility class for validating check-in request tokens
 */
object TokenValidator {
    
    /**
     * Validates that a token has the correct format and is not expired
     * 
     * @param token The token string to validate
     * @param expiresAt The expiration timestamp
     * @return true if the token is valid and not expired, false otherwise
     */
    fun validateToken(token: String, expiresAt: LocalDateTime): Boolean {
        return TokenGenerator.isValidTokenFormat(token) && !isTokenExpired(expiresAt)
    }
    
    /**
     * Checks if a token has expired based on its expiration timestamp
     * 
     * @param expiresAt The expiration timestamp
     * @return true if the token has expired, false otherwise
     */
    fun isTokenExpired(expiresAt: LocalDateTime): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    /**
     * Validates only the token format without checking expiration
     * 
     * @param token The token string to validate
     * @return true if the token has valid format, false otherwise
     */
    fun validateTokenFormat(token: String): Boolean {
        return TokenGenerator.isValidTokenFormat(token)
    }
}
