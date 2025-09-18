package rfm.hillsongptapp.core.data.auth

import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * Implementation of AuthTokenProvider that uses AuthTokenManager
 * This breaks the circular dependency between network and data modules
 */
class AuthTokenProviderImpl(
    private val authTokenManager: AuthTokenManager
) : AuthTokenProvider {
    
    override suspend fun getAuthToken(): String? {
        LoggerHelper.logDebug("getAuthToken() called on AuthTokenProviderImpl", "AuthTokenProvider")
        LoggerHelper.logDebug("AuthTokenManager instance: ${authTokenManager.hashCode()}", "AuthTokenProvider")
        val token = authTokenManager.getValidToken()
        LoggerHelper.logDebug("getAuthToken() result: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}", "AuthTokenProvider")
        return token
    }
    
    override suspend fun isAuthenticated(): Boolean {
        val isAuth = authTokenManager.isAuthenticated()
        LoggerHelper.logDebug("isAuthenticated(): $isAuth", "AuthTokenProvider")
        return isAuth
    }
}