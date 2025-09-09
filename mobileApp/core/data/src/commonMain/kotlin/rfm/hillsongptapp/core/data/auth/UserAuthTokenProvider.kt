package rfm.hillsongptapp.core.data.auth

import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.data.repository.database.UserDao

/**
 * Implementation of AuthTokenProvider that uses UserRepository to get authentication tokens
 * This breaks the circular dependency by implementing the interface in the data layer
 */
class UserAuthTokenProvider(
    private val userDao: UserDao
) : AuthTokenProvider {
    
    override suspend fun getAuthToken(): String? {
        return try {
            // Get the current user (assuming user ID 1 is the logged-in user)
            // In a real app, you'd have a proper session management system
            val user = userDao.getUserById(1)
            user?.token
        } catch (e: Exception) {
            // Log error and return null for graceful degradation
            null
        }
    }
    
    override suspend fun isAuthenticated(): Boolean {
        return try {
            val user = userDao.getUserById(1)
            user?.token != null && user.expiryAt?.let { it > System.currentTimeMillis() } ?: true
        } catch (e: Exception) {
            false
        }
    }
}