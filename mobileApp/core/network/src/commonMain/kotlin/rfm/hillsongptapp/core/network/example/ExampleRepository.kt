package rfm.hillsongptapp.core.network.example

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.network.ktor.responses.LoginResponse
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.util.mapSuccess
import rfm.hillsongptapp.core.network.util.onError
import rfm.hillsongptapp.core.network.util.onSuccess

/**
 * Example repository showing how to use the new API services
 * This demonstrates best practices for repository implementation
 */
class ExampleAuthRepository(
    private val apiClient: HillsongApiClient
) {
    
    /**
     * Example: Simple login with error handling
     */
    suspend fun login(email: String, password: String): NetworkResult<String> {
        return apiClient.auth.login(LoginRequest(email, password))
            .mapSuccess { response ->
                // Extract token from successful response
                response.data?.token ?: throw IllegalStateException("No token in response")
            }
            .onSuccess { token ->
                // Could save token to local storage here
                println("Login successful, token: $token")
            }
            .onError { exception ->
                // Log error for debugging
                println("Login failed: ${exception.message}")
            }
    }
    
    /**
     * Example: Using reactive streams for real-time data
     */
    fun getPostsStream(): Flow<NetworkResult<List<rfm.hillsongptapp.core.network.ktor.responses.Post>>> {
        return flow {
            emit(NetworkResult.Loading)
            
            // Get posts from API
            val result = apiClient.posts.getPosts()
            
            when (result) {
                is NetworkResult.Success -> {
                    val posts = result.data.data?.postList ?: emptyList()
                    emit(NetworkResult.Success(posts))
                }
                is NetworkResult.Error -> {
                    emit(NetworkResult.Error(result.exception))
                }
                is NetworkResult.Loading -> {
                    emit(NetworkResult.Loading)
                }
            }
        }
    }
    
    /**
     * Example: Combining multiple API calls
     */
    suspend fun getUserDashboardData(): NetworkResult<DashboardData> {
        return try {
            // Get user profile
            val profileResult = apiClient.profile.getProfile()
            if (profileResult is NetworkResult.Error) {
                return NetworkResult.Error(profileResult.exception)
            }
            
            // Get user's events
            val eventsResult = apiClient.events.getUpcomingEvents()
            if (eventsResult is NetworkResult.Error) {
                return NetworkResult.Error(eventsResult.exception)
            }
            
            // Get user's groups
            val groupsResult = apiClient.groups.getMyGroups()
            if (groupsResult is NetworkResult.Error) {
                return NetworkResult.Error(groupsResult.exception)
            }
            
            // Combine all data
            val profile = (profileResult as NetworkResult.Success).data
            val events = (eventsResult as NetworkResult.Success).data
            val groups = (groupsResult as NetworkResult.Success).data
            
            NetworkResult.Success(
                DashboardData(
                    profile = profile,
                    upcomingEvents = events,
                    myGroups = groups
                )
            )
            
        } catch (e: Exception) {
            NetworkResult.Error(e.toNetworkException())
        }
    }
    
    /**
     * Example: Error handling with specific error types
     */
    suspend fun loginWithDetailedErrorHandling(email: String, password: String): LoginResult {
        return when (val result = apiClient.auth.login(LoginRequest(email, password))) {
            is NetworkResult.Success -> {
                val response = result.data
                if (response.success && response.data?.token != null) {
                    LoginResult.Success(response.data.token)
                } else {
                    LoginResult.InvalidCredentials(response.message)
                }
            }
            is NetworkResult.Error -> {
                when (result.exception) {
                    is rfm.hillsongptapp.core.network.result.NetworkException.NoInternetConnection -> 
                        LoginResult.NoInternet
                    is rfm.hillsongptapp.core.network.result.NetworkException.Timeout -> 
                        LoginResult.Timeout
                    is rfm.hillsongptapp.core.network.result.NetworkException.Unauthorized -> 
                        LoginResult.InvalidCredentials("Invalid email or password")
                    else -> 
                        LoginResult.UnknownError(result.exception.message!!)
                }
            }
            is NetworkResult.Loading -> {
                LoginResult.Loading
            }
        }
    }
}

/**
 * Example data classes for repository responses
 */
data class DashboardData(
    val profile: rfm.hillsongptapp.core.network.api.UserProfile,
    val upcomingEvents: List<rfm.hillsongptapp.core.network.api.Event>,
    val myGroups: List<rfm.hillsongptapp.core.network.api.Group>
)

/**
 * Example sealed class for specific login results
 */
sealed class LoginResult {
    data class Success(val token: String) : LoginResult()
    data class InvalidCredentials(val message: String) : LoginResult()
    data class UnknownError(val message: String) : LoginResult()
    data object NoInternet : LoginResult()
    data object Timeout : LoginResult()
    data object Loading : LoginResult()
}

// Extension function to convert exceptions
private fun Exception.toNetworkException(): rfm.hillsongptapp.core.network.result.NetworkException {
    return rfm.hillsongptapp.core.network.result.NetworkException.UnknownError(
        this.message ?: "Unknown error occurred"
    )
}