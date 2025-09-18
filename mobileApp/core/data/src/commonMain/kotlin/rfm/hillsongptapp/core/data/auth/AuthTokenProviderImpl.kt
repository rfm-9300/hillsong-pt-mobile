package rfm.hillsongptapp.core.data.auth

import rfm.hillsongptapp.core.network.auth.AuthTokenProvider

/**
 * Implementation of AuthTokenProvider that uses AuthTokenManager
 * This breaks the circular dependency between network and data modules
 */
class AuthTokenProviderImpl(
    private val authTokenManager: AuthTokenManager
) : AuthTokenProvider {
    
    override suspend fun getAuthToken(): String? {
        return authTokenManager.getValidToken()
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return authTokenManager.isAuthenticated()
    }
}