package rfm.hillsongptapp.core.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Manages authentication tokens with automatic refresh functionality
 * Follows modern Android security best practices
 */
class AuthTokenManager(
    private val userDao: UserDao,
    private val apiClient: HillsongApiClient
) {
    private val refreshMutex = Mutex()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: Flow<AuthState> = _authState.asStateFlow()
    
    private var currentUser: User? = null
    

    
    /**
     * Initialize authentication state by checking stored tokens
     */
    suspend fun initialize() {
        try {
            // Get the most recent authenticated user
            val user = userDao.getAuthenticatedUser()
            
            if (user?.token != null) {
                currentUser = user
                
                if (isTokenValid(user)) {
                    _authState.value = AuthState.Authenticated(user)
                } else if (isTokenExpiredButRefreshable(user)) {
                    refreshTokenIfNeeded()
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Save authentication data after successful login
     */
    suspend fun saveAuthData(email: String, token: String, expiryTimeMillis: Long? = null) {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val expiryTime = expiryTimeMillis ?: (currentTimeMillis + DEFAULT_TOKEN_VALIDITY_MS)
        
        val user = User(
            id = 1, // Single user app, or generate proper ID
            email = email,
            token = token,
            expiryAt = expiryTime,
            refreshToken = null,
            lastLoginAt = currentTimeMillis
        )
        
        userDao.insertUser(user)
        currentUser = user
        _authState.value = AuthState.Authenticated(user)
    }
    
    /**
     * Get current valid token, refreshing if necessary
     */
    suspend fun getValidToken(): String? {
        val user = currentUser ?: return null
        
        return if (isTokenValid(user)) {
            user.token
        } else if (isTokenExpiredButRefreshable(user)) {
            refreshTokenIfNeeded()
            currentUser?.token
        } else {
            null
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }
    
    /**
     * Clear authentication data (logout)
     */
    suspend fun clearAuthData() {
        currentUser?.let { user ->
            userDao.deleteUser(user)
        }
        
        currentUser = null
        _authState.value = AuthState.Unauthenticated
    }
    
    /**
     * Refresh token if needed (with mutex to prevent concurrent calls)
     */
    private suspend fun refreshTokenIfNeeded(): Boolean {
        return refreshMutex.withLock {
            val user = currentUser ?: return@withLock false
            
            if (isTokenValid(user)) {
                return@withLock true // Token was refreshed by another call
            }
            
            when (val result = apiClient.auth.refreshToken()) {
                is NetworkResult.Success -> {
                    val response = result.data
                    val authData = response.data
                    if (response.success && authData != null && authData.token.isNotEmpty()) {
                        val newToken = authData.token
                        val newExpiryTime = Clock.System.now().toEpochMilliseconds() + DEFAULT_TOKEN_VALIDITY_MS
                        
                        val updatedUser = user.copy(
                            token = newToken,
                            expiryAt = newExpiryTime
                        )
                        
                        userDao.updateUser(updatedUser)
                        currentUser = updatedUser
                        _authState.value = AuthState.Authenticated(updatedUser)
                        
                        true
                    } else {
                        _authState.value = AuthState.Unauthenticated
                        false
                    }
                }
                is NetworkResult.Error -> {
                    _authState.value = AuthState.Unauthenticated
                    false
                }
                is NetworkResult.Loading -> false
            }
        }
    }
    
    /**
     * Check if token is still valid (not expired)
     */
    private fun isTokenValid(user: User): Boolean {
        val token = user.token ?: return false
        val expiryTime = user.expiryAt ?: return false
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Add buffer time (5 minutes) to refresh before actual expiry
        val bufferTime = 5 * 60 * 1000L // 5 minutes in milliseconds
        return currentTime < (expiryTime - bufferTime)
    }
    
    /**
     * Check if token is expired but still within refresh window
     */
    private fun isTokenExpiredButRefreshable(user: User): Boolean {
        val token = user.token ?: return false
        val expiryTime = user.expiryAt ?: return false
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Allow refresh within 7 days of expiry
        val refreshWindow = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        return currentTime < (expiryTime + refreshWindow)
    }
    
    companion object {
        // Default token validity: 24 hours
        private const val DEFAULT_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L
    }
}

/**
 * Sealed class representing authentication states
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
}