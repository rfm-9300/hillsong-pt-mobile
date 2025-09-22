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
import rfm.hillsongptapp.logging.LoggerHelper

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
    private var isInitialized = false
    
    init {
        LoggerHelper.setTag("AuthTokenManager")
    }
    

    
    /**
     * Initialize authentication state by checking stored tokens
     */
    suspend fun initialize() {
        if (isInitialized) {
            LoggerHelper.logDebug("Already initialized, skipping", "AuthTokenManager")
            return
        }
        
        LoggerHelper.logDebug("Initializing...", "AuthTokenManager")
        try {
            // Get the most recent authenticated user
            val user = userDao.getAuthenticatedUser()
            LoggerHelper.logDebug("Found user: ${user?.email}, token present: ${user?.token != null}", "AuthTokenManager")
            
            if (user?.token != null) {
                currentUser = user
                
                if (isTokenValid(user)) {
                    LoggerHelper.logDebug("Token is valid, setting authenticated state", "AuthTokenManager")
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    LoggerHelper.logDebug("Token is expired or invalid, setting unauthenticated state", "AuthTokenManager")
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                LoggerHelper.logDebug("No user or token found", "AuthTokenManager")
                _authState.value = AuthState.Unauthenticated
            }
            isInitialized = true
            LoggerHelper.logDebug("Initialization complete, state: ${_authState.value}", "AuthTokenManager")
        } catch (e: Exception) {
            LoggerHelper.logDebug("Initialization failed: ${e.message}", "AuthTokenManager")
            _authState.value = AuthState.Unauthenticated
            isInitialized = true
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
     * Get current valid token (non-blocking version)
     * Returns null if token is expired and needs refresh
     */
    suspend fun getValidToken(): String? {
        LoggerHelper.logDebug("getValidToken() called on instance ${this.hashCode()}, isInitialized: $isInitialized", "AuthTokenManager")
        
        if (!isInitialized) {
            LoggerHelper.logDebug("Not initialized, initializing now...", "AuthTokenManager")
            initialize()
        }
        
        // Get user from AuthState instead of currentUser field to ensure consistency
        val authState = _authState.value
        LoggerHelper.logDebug("Current auth state: $authState", "AuthTokenManager")
        
        val user = when (authState) {
            is AuthState.Authenticated -> {
                LoggerHelper.logDebug("User from auth state: ${authState.user.email}, token present: ${authState.user.token != null}", "AuthTokenManager")
                authState.user
            }
            else -> {
                LoggerHelper.logDebug("User not authenticated in auth state", "AuthTokenManager")
                return null
            }
        }
        
        // Also check currentUser for debugging
        LoggerHelper.logDebug("currentUser field: ${currentUser?.email}, same as auth state: ${currentUser?.email == user.email}", "AuthTokenManager")
        
        return if (isTokenValid(user)) {
            LoggerHelper.logDebug("Token is valid, returning token", "AuthTokenManager")
            user.token
        } else {
            LoggerHelper.logDebug("Token is expired or invalid, returning null", "AuthTokenManager")
            // Don't refresh here - let the app handle re-authentication
            _authState.value = AuthState.Unauthenticated
            null
        }
    }
    
    /**
     * Proactively refresh token if it's about to expire
     * This should be called from background threads or during app initialization
     */
    suspend fun refreshTokenIfExpired(): Boolean {
        if (!isInitialized) {
            initialize()
        }
        
        val user = currentUser ?: return false
        
        return if (isTokenExpiredButRefreshable(user)) {
            LoggerHelper.logDebug("Token expired but refreshable, attempting refresh", "AuthTokenManager")
            refreshTokenIfNeeded()
        } else {
            true // Token is still valid or not refreshable
        }
    }
    
    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        val isAuth = _authState.value is AuthState.Authenticated
        LoggerHelper.logDebug("isAuthenticated() called, result: $isAuth, state: ${_authState.value}", "AuthTokenManager")
        return isAuth
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
        val token = user.token ?: run {
            LoggerHelper.logDebug("Token is null", "AuthTokenManager")
            return false
        }
        val expiryTime = user.expiryAt ?: run {
            LoggerHelper.logDebug("No expiry time, assuming token is valid", "AuthTokenManager")
            return true // If no expiry time is set, assume token is valid
        }
        val currentTime = Clock.System.now().toEpochMilliseconds()
        
        // Add buffer time (5 minutes) to refresh before actual expiry
        val bufferTime = 5 * 60 * 1000L // 5 minutes in milliseconds
        val isValid = currentTime < (expiryTime - bufferTime)
        
        LoggerHelper.logDebug("Token validation: current=$currentTime, expiry=$expiryTime, valid=$isValid", "AuthTokenManager")
        return isValid
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