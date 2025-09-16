package rfm.com.security.jwt

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Utility class for JWT operations
 */
object JwtUtils {
    
    /**
     * Get current authenticated user principal
     */
    fun getCurrentUserPrincipal(): UserPrincipal? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication?.principal is UserPrincipal) {
            authentication.principal as UserPrincipal
        } else null
    }
    
    /**
     * Get current authenticated user ID
     */
    fun getCurrentUserId(): Long? {
        return getCurrentUserPrincipal()?.id
    }
    
    /**
     * Get current authenticated user email
     */
    fun getCurrentUserEmail(): String? {
        return getCurrentUserPrincipal()?.username
    }
    
    /**
     * Check if current user is admin
     */
    fun isCurrentUserAdmin(): Boolean {
        val userPrincipal = getCurrentUserPrincipal()
        return userPrincipal?.authorities?.any { it.authority == "ROLE_ADMIN" } ?: false
    }
    
    /**
     * Check if current user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated && authentication.principal is UserPrincipal
    }
}