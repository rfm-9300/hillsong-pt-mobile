package rfm.com.service

import org.springframework.stereotype.Service
import rfm.com.config.AdminProperties

@Service
class AdminAuthService(
    private val adminProperties: AdminProperties
) {
    
    /**
     * Check if the provided token is a valid admin token
     */
    fun isValidAdminToken(token: String?): Boolean {
        if (token.isNullOrBlank()) return false
        
        // Remove "Bearer " prefix if present
        val cleanToken = if (token.startsWith("Bearer ")) {
            token.substring(7)
        } else {
            token
        }
        
        return cleanToken == adminProperties.token
    }
    
    /**
     * Get the configured admin token
     */
    fun getAdminToken(): String {
        return adminProperties.token
    }
}