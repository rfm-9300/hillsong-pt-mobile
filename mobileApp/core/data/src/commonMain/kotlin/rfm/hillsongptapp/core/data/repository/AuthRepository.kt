package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfile
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.data.auth.AuthTokenManager
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.ktor.requests.*
import rfm.hillsongptapp.core.network.ktor.responses.*
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.util.getOrNull
import rfm.hillsongptapp.core.network.util.onError
import rfm.hillsongptapp.core.network.util.onSuccess

/**
 * Repository for authentication operations using the new HillsongApiClient
 * Handles both network operations and local database storage
 */
class AuthRepository(
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao,
    private val apiClient: HillsongApiClient,
    private val authTokenManager: AuthTokenManager,
) {
    
    // Database operations for User
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    // Authentication operations using new API client
    suspend fun login(email: String, password: String): AuthResult<LoginResponse> {
        val apiRequest = LoginRequest(
            email = email,
            password = password
        )
        
        return when (val result = apiClient.auth.login(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                val authData = response.data
                if (response.success && authData != null && authData.token.isNotEmpty()) {
                    // Save auth data using AuthTokenManager
                    authTokenManager.saveAuthData(
                        email = email,
                        token = authData.token,
                        expiryTimeMillis = null // Let AuthTokenManager set default expiry
                    )
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Login network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun googleLogin(idToken: String): AuthResult<LoginResponse> {
        val apiRequest = GoogleAuthRequest(idToken = idToken)
        
        return when (val result = apiClient.auth.googleLogin(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                val authData = response.data
                if (response.success && authData != null && authData.token.isNotEmpty()) {
                    // Extract email from Google account or use a placeholder
                    // You might need to get this from the Google account info
                    authTokenManager.saveAuthData(
                        email = "google_user@example.com", // Replace with actual email from Google
                        token = authData.token,
                        expiryTimeMillis = null
                    )
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Google login network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun facebookLogin(accessToken: String): AuthResult<LoginResponse> {
        val apiRequest = FacebookAuthRequest(accessToken = accessToken)
        
        return when (val result = apiClient.auth.facebookLogin(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success && response.data?.token != null) {
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Facebook login network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun signUp(
        email: String, 
        password: String, 
        confirmPassword: String, 
        firstName: String, 
        lastName: String
    ): AuthResult<SignUpResponse> {
        val apiRequest = SignUpRequest(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            firstName = firstName,
            lastName = lastName
        )
        
        return when (val result = apiClient.auth.signUp(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success) {
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Sign up network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun verifyEmail(token: String): AuthResult<VerificationResponse> {
        val apiRequest = VerificationRequest(token = token)
        
        return when (val result = apiClient.auth.verifyEmail(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success) {
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Email verification network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun requestPasswordReset(email: String): AuthResult<PasswordResetResponse> {
        val apiRequest = PasswordResetRequest(email = email)
        
        return when (val result = apiClient.auth.requestPasswordReset(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success) {
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Password reset request network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): AuthResult<PasswordResetResponse> {
        val apiRequest = ResetPasswordRequest(token = token, newPassword = newPassword)
        
        return when (val result = apiClient.auth.resetPassword(apiRequest)) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success) {
                    AuthResult.Success(response)
                } else {
                    AuthResult.Error(response.message)
                }
            }
            is NetworkResult.Error -> {
                AuthResult.NetworkError(result.exception.message ?: "Password reset network error")
            }
            is NetworkResult.Loading -> {
                AuthResult.Loading
            }
        }
    }



    // UserProfile management
    suspend fun insertUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun getUserProfileByUserId(userId: Long): UserProfile? {
        return userProfileDao.getUserProfileByUserId(userId)
    }

    suspend fun deleteUserProfile(profile: UserProfile) {
        userProfileDao.deleteUserProfile(profile)
    }

    // Authentication state management
    suspend fun initializeAuthState() {
        authTokenManager.initialize()
    }
    
    suspend fun isUserAuthenticated(): Boolean {
        return authTokenManager.isAuthenticated()
    }
    
    suspend fun getCurrentUser(): User? {
        return userDao.getAuthenticatedUser()
    }
    
    suspend fun logout(): AuthResult<Unit> {
        return try {
            // Call logout API
            when (val result = apiClient.auth.logout()) {
                is NetworkResult.Success -> {
                    // Clear local auth data
                    authTokenManager.clearAuthData()
                    AuthResult.Success(Unit)
                }
                is NetworkResult.Error -> {
                    // Even if API call fails, clear local data
                    authTokenManager.clearAuthData()
                    AuthResult.Success(Unit)
                }
                is NetworkResult.Loading -> {
                    AuthResult.Loading
                }
            }
        } catch (e: Exception) {
            // Ensure local data is cleared even on error
            authTokenManager.clearAuthData()
            AuthResult.Success(Unit)
        }
    }
    
    // Get auth state flow for reactive UI updates
    fun getAuthStateFlow() = authTokenManager.authState
    
    // Helper methods using extension functions for cleaner code
    suspend fun loginWithExtensions(email: String, password: String): LoginResponse? {
        val apiRequest = LoginRequest(email = email, password = password)
        
        return apiClient.auth.login(apiRequest)
            .onSuccess { response ->
                // Handle successful login (save token, update UI, etc.)
                println("Login successful: ${response.message}")
            }
            .onError { exception ->
                // Handle error (show error message, log, etc.)
                println("Login failed: ${exception.message}")
            }
            .getOrNull() // Returns the data if successful, null if error/loading
    }
}

/**
 * Sealed class for repository-level authentication results
 * Provides a clean abstraction over NetworkResult for the UI layer
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    data class NetworkError(val message: String) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}