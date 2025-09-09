package rfm.hillsongptapp.core.network.auth

/**
 * Interface for providing authentication tokens to network requests
 * This abstraction breaks the circular dependency between core.network and core.data
 */
interface AuthTokenProvider {
    /**
     * Get the current authentication token for API requests
     * @return The bearer token or null if no user is authenticated
     */
    suspend fun getAuthToken(): String?
    
    /**
     * Check if a user is currently authenticated
     * @return true if a valid token is available
     */
    suspend fun isAuthenticated(): Boolean
}

/**
 * Default implementation that returns no token (for unauthenticated requests)
 */
class NoAuthTokenProvider : AuthTokenProvider {
    override suspend fun getAuthToken(): String? = null
    override suspend fun isAuthenticated(): Boolean = false
}