package rfm.com.util

import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails

/**
 * Utility functions for authentication and security
 */

/**
 * Get the current user ID from the authentication object
 */
fun Authentication.getCurrentUserId(): String {
    return when (val principal = this.principal) {
        is rfm.com.security.jwt.UserPrincipal -> {
            // Extract user ID from UserPrincipal
            principal.id
        }
        is UserDetails -> {
            // Use username as user ID (fallback)
            principal.username
        }
        is String -> {
            // Direct string principal (user ID)
            principal
        }
        else -> {
            throw IllegalStateException("Unsupported principal type: ${principal?.javaClass?.simpleName}")
        }
    }
}

/**
 * Get the current username from the authentication object
 */
fun Authentication.getCurrentUsername(): String {
    return when (val principal = this.principal) {
        is UserDetails -> principal.username
        is String -> principal
        else -> throw IllegalStateException("Unsupported principal type: ${principal?.javaClass?.simpleName}")
    }
}

/**
 * Check if the current user has a specific role
 */
fun Authentication.hasRole(role: String): Boolean {
    return this.authorities?.any { 
        it.authority == "ROLE_$role" || it.authority == role 
    } ?: false
}

/**
 * Check if the current user has any of the specified roles
 */
fun Authentication.hasAnyRole(vararg roles: String): Boolean {
    return roles.any { hasRole(it) }
}

/**
 * Check if the current user has a specific authority
 */
fun Authentication.hasAuthority(authority: String): Boolean {
    return this.authorities?.any { it.authority == authority } ?: false
}