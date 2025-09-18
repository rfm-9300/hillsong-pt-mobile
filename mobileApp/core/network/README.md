# Core Network Module

A modern, feature-based network layer for the Hillsong PT mobile app built with Kotlin Multiplatform and Ktor.

## Architecture Overview

This module follows modern Android architecture patterns with:

- **Feature-based API services** - Separate services for each app feature
- **Result-based error handling** - Using sealed classes for type-safe error handling
- **Reactive programming** - Flow support for real-time data streams
- **Dependency injection** - Koin-based DI with proper scoping
- **Comprehensive error handling** - Specific error types for better UX

## Structure

```
core/network/
├── api/                    # Feature-specific API services
│   ├── AuthApiService.kt
│   ├── PostsApiService.kt
│   ├── ProfileApiService.kt
│   ├── EventsApiService.kt
│   ├── GroupsApiService.kt
│   └── PrayerApiService.kt
├── auth/                   # Authentication providers
│   └── AuthTokenProvider.kt
├── base/                   # Base classes and utilities
│   └── BaseApiService.kt
├── di/                     # Dependency injection
│   └── NetworkModule.kt
├── ktor/                   # Legacy Ktor implementation
│   ├── ApiService.kt       # (Deprecated)
│   ├── requests/
│   └── responses/
├── provider/               # Platform-specific providers
│   └── HttpClientEngineProvider.kt
├── result/                 # Result types and error handling
│   └── NetworkResult.kt
├── util/                   # Extension functions and utilities
│   └── NetworkExtensions.kt
├── example/                # Usage examples
│   └── ExampleRepository.kt
├── HillsongApiClient.kt    # Main API client
└── MIGRATION_GUIDE.md      # Migration documentation
```

## Key Features

### 1. Feature-Based API Services

Each app feature has its own dedicated API service:

```kotlin
// Authentication
val loginResult = apiClient.auth.login(LoginRequest(email, password))

// Posts
val postsResult = apiClient.posts.getPosts()

// Profile
val profileResult = apiClient.profile.getProfile()

// Events
val eventsResult = apiClient.events.getUpcomingEvents()
```

### 2. Type-Safe Error Handling

Using sealed classes for comprehensive error handling:

```kotlin
when (val result = apiClient.auth.login(request)) {
    is NetworkResult.Success -> {
        // Handle successful response
        val token = result.data.data?.token
    }
    is NetworkResult.Error -> {
        when (result.exception) {
            is NetworkException.NoInternetConnection -> showNoInternetMessage()
            is NetworkException.Unauthorized -> redirectToLogin()
            is NetworkException.Timeout -> showRetryOption()
            else -> showGenericError()
        }
    }
    is NetworkResult.Loading -> showLoadingIndicator()
}
```

### 3. Reactive Programming Support

Flow-based streams for real-time data:

```kotlin
apiClient.posts.getPostsStream()
    .collect { result ->
        when (result) {
            is NetworkResult.Success -> updateUI(result.data)
            is NetworkResult.Error -> showError(result.exception)
            is NetworkResult.Loading -> showLoading()
        }
    }
```

### 4. Extension Functions for Cleaner Code

Utility extensions for common operations:

```kotlin
apiClient.auth.login(request)
    .onSuccess { response -> saveToken(response.data?.token) }
    .onError { exception -> logError(exception) }
    .mapSuccess { response -> response.data?.token }
```

## Usage Examples

### Basic Repository Implementation

```kotlin
class AuthRepository(private val apiClient: HillsongApiClient) {
    
    suspend fun login(email: String, password: String): NetworkResult<String> {
        return apiClient.auth.login(LoginRequest(email, password))
            .mapSuccess { response -> 
                response.data?.token ?: throw IllegalStateException("No token")
            }
    }
    
    suspend fun signUp(
        email: String, 
        password: String, 
        firstName: String, 
        lastName: String
    ): NetworkResult<Unit> {
        val request = SignUpRequest(email, password, password, firstName, lastName)
        return apiClient.auth.signUp(request)
            .mapSuccess { Unit }
    }
}
```

### ViewModel Integration

```kotlin
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    _loginState.value = LoginState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _loginState.value = LoginState.Error(result.exception.message)
                }
                is NetworkResult.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }
}
```

## Configuration

### Dependency Injection Setup

The network module is configured through Koin:

```kotlin
// In your app module
startKoin {
    modules(networkModule)
    properties(mapOf("API_BASE_URL" to "https://your-api-url.com"))
}
```

### Custom Base URL

You can override the base URL:

```kotlin
// Through properties
val koinApp = startKoin {
    properties(mapOf("API_BASE_URL" to "https://staging-api.com"))
}

// Or through environment variables
System.setProperty("API_BASE_URL", "https://production-api.com")
```

## Testing

### Unit Testing API Services

```kotlin
class AuthApiServiceTest {
    
    @Test
    fun `login should return success when credentials are valid`() = runTest {
        // Given
        val mockHttpClient = mockHttpClient {
            onPost("/api/auth/login") respond {
                LoginResponse(success = true, message = "Success", data = AuthResponse("token"))
            }
        }
        
        val authService = AuthApiServiceImpl(mockHttpClient, "https://test.com")
        
        // When
        val result = authService.login(LoginRequest("test@test.com", "password"))
        
        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("token", (result as NetworkResult.Success).data.data?.token)
    }
}
```

## Migration from Legacy ApiService

See [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for detailed migration instructions.

## Best Practices

1. **Use the aggregated client** (`HillsongApiClient`) in repositories
2. **Handle all NetworkResult states** (Success, Error, Loading)
3. **Use specific error types** for better user experience
4. **Leverage extension functions** for cleaner code
5. **Use Flow streams** for reactive UI updates
6. **Implement proper logging** for debugging
7. **Cache responses** when appropriate
8. **Handle authentication** centrally through AuthTokenProvider

## Contributing

When adding new API endpoints:

1. Add the endpoint to the appropriate feature service interface
2. Implement the endpoint in the service implementation
3. Add proper error handling
4. Update tests
5. Update documentation

## Dependencies

- **Ktor Client** - HTTP client for multiplatform
- **Kotlinx Serialization** - JSON serialization
- **Koin** - Dependency injection
- **Kotlinx Coroutines** - Async programming
- **Kotlinx DateTime** - Date/time handling (if needed)

This network module provides a solid foundation for scalable, maintainable network operations in your Kotlin Multiplatform mobile app.