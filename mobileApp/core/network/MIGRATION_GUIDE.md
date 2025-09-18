# Network Module Migration Guide

This guide helps you migrate from the old monolithic `ApiService` to the new feature-based API services.

## What Changed

### Before (Old Structure)
```kotlin
class ApiService(private val baseUrl: String, private val httpClient: HttpClient) {
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun signUp(request: SignUpRequest): SignUpResponse
    suspend fun getPosts(): ApiResponse<PostListResponse>
    // All API methods mixed together
}
```

### After (New Structure)
```kotlin
// Feature-specific services
interface AuthApiService {
    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse>
    suspend fun signUp(request: SignUpRequest): NetworkResult<SignUpResponse>
}

interface PostsApiService {
    suspend fun getPosts(): NetworkResult<ApiResponse<PostListResponse>>
}

// Main client aggregating all services
class HillsongApiClient(
    val auth: AuthApiService,
    val posts: PostsApiService,
    // ... other services
)
```

## Key Improvements

1. **Better Error Handling**: Uses `NetworkResult<T>` sealed class instead of throwing exceptions
2. **Feature Separation**: Each feature has its own API service
3. **Modern Patterns**: Follows latest Android architecture guidelines
4. **Reactive Streams**: Support for Flow-based reactive programming
5. **Type Safety**: Better compile-time safety with sealed classes

## Migration Steps

### 1. Update Dependency Injection

**Before:**
```kotlin
class MyRepository(private val apiService: ApiService)
```

**After:**
```kotlin
class MyRepository(private val apiClient: HillsongApiClient)
// or inject specific services
class AuthRepository(private val authApiService: AuthApiService)
```

### 2. Update API Calls

**Before:**
```kotlin
try {
    val response = apiService.login(loginRequest)
    if (response.success) {
        // Handle success
    } else {
        // Handle error
    }
} catch (e: Exception) {
    // Handle network error
}
```

**After:**
```kotlin
when (val result = apiClient.auth.login(loginRequest)) {
    is NetworkResult.Success -> {
        val response = result.data
        if (response.success) {
            // Handle success
        } else {
            // Handle API error
        }
    }
    is NetworkResult.Error -> {
        // Handle network error
        when (result.exception) {
            is NetworkException.NoInternetConnection -> // Handle no internet
            is NetworkException.Unauthorized -> // Handle auth error
            else -> // Handle other errors
        }
    }
    is NetworkResult.Loading -> {
        // Handle loading state
    }
}
```

### 3. Use Extension Functions for Cleaner Code

```kotlin
apiClient.auth.login(loginRequest)
    .onSuccess { response ->
        // Handle success
    }
    .onError { exception ->
        // Handle error
    }
```

### 4. Leverage Reactive Streams

```kotlin
// For real-time updates
apiClient.posts.getPostsStream()
    .collect { result ->
        when (result) {
            is NetworkResult.Success -> updateUI(result.data)
            is NetworkResult.Error -> showError(result.exception)
            is NetworkResult.Loading -> showLoading()
        }
    }
```

## Available API Services

- **AuthApiService**: Authentication operations (login, signup, password reset)
- **PostsApiService**: Post management (CRUD operations, likes)
- **ProfileApiService**: User profile management
- **EventsApiService**: Event management and registration
- **GroupsApiService**: Group management and membership
- **PrayerApiService**: Prayer request management

## Best Practices

1. **Use the aggregated client** (`HillsongApiClient`) in repositories for convenience
2. **Handle all NetworkResult states** (Success, Error, Loading)
3. **Use extension functions** for cleaner code
4. **Leverage Flow streams** for reactive UI updates
5. **Check for specific error types** for better UX

## Backward Compatibility

The old `ApiService` is still available but marked as deprecated. It will be removed in a future version.

## Example Repository Implementation

```kotlin
class AuthRepository(private val apiClient: HillsongApiClient) {
    
    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return apiClient.auth.login(LoginRequest(email, password))
            .mapSuccess { it.data }
            .onError { exception ->
                // Log error or perform additional error handling
                logError("Login failed", exception)
            }
    }
    
    fun isUserAuthenticated(): Flow<Boolean> {
        return apiClient.auth.getProfile()
            .asFlow()
            .map { result ->
                result is NetworkResult.Success
            }
    }
}
```

This new structure provides better maintainability, testability, and follows modern Android development best practices.