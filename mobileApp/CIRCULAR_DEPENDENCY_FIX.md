# Circular Dependency Fix: core.data ↔ core.network

## Problem Description

The app had a circular dependency between `core.data` and `core.network` modules:

1. **core.data** depends on **core.network** (to make API calls via `ApiService`)
2. **core.network** depends on **core.data** (to get user tokens via `UserRepository`)

This circular dependency prevented proper module initialization and could cause runtime issues.

## Root Cause

In `NetworkModule.kt`, the HTTP client was directly injecting `UserRepository` to get authentication tokens:

```kotlin
// PROBLEMATIC CODE
defaultRequest {
    val userRepository = get<UserRepository>() // ❌ Creates circular dependency
    val user = runBlocking { userRepository.getUserById(1) }
    user?.token?.let { token ->
        header("Authorization", "Bearer $token")
    }
}
```

## Solution: Dependency Inversion with AuthTokenProvider

I implemented the **Dependency Inversion Principle** to break the circular dependency by creating an abstraction layer.

### 1. Created AuthTokenProvider Interface

```kotlin
// core/network/src/.../auth/AuthTokenProvider.kt
interface AuthTokenProvider {
    suspend fun getAuthToken(): String?
    suspend fun isAuthenticated(): Boolean
}
```

### 2. Updated NetworkModule

The network module now depends on the abstraction, not the concrete implementation:

```kotlin
// core/network/di/NetworkModule.kt
val networkModule = module {
    // Default implementation (no auth)
    single<AuthTokenProvider> { NoAuthTokenProvider() }
    
    single {
        HttpClient(engine = httpClientEngine()) {
            // ... other config
            
            defaultRequest {
                val authTokenProvider = get<AuthTokenProvider>() // ✅ Uses abstraction
                val token = runBlocking { authTokenProvider.getAuthToken() }
                token?.let {
                    header("Authorization", "Bearer $it")
                }
            }
        }
    }
}
```

### 3. Implemented Real AuthTokenProvider in Data Module

```kotlin
// core/data/src/.../auth/UserAuthTokenProvider.kt
class UserAuthTokenProvider(
    private val userDao: UserDao
) : AuthTokenProvider {
    
    override suspend fun getAuthToken(): String? {
        return try {
            val user = userDao.getUserById(1)
            user?.token
        } catch (e: Exception) {
            null // Graceful degradation
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
```

### 4. Updated DataModule to Override AuthTokenProvider

```kotlin
// core/data/di/DataModule.kt
val dataModule = module {
    includes(networkModule) // Include network module
    
    // Database DAOs
    single<UserDao> { databaseInstance().userDao() }
    single<UserProfileDao> { databaseInstance().userProfileDao() }

    // Override the default AuthTokenProvider with real implementation
    single<AuthTokenProvider> { 
        UserAuthTokenProvider(userDao = get()) 
    } bind AuthTokenProvider::class

    // Repositories
    single {
        UserRepository(
            userDao = get<UserDao>(),
            api = get<ApiService>(), // ✅ Now uses ApiService directly
            userProfileDao = get<UserProfileDao>(),
        )
    }

    single {
        PostRepository(api = get<ApiService>())
    }
}
```

### 5. Updated App Module Loading

```kotlin
// composeApp/di/AppModules.kt
val coreModules = listOf(
    dataModule, // This now includes networkModule
    koinPlatformModule,
    kidsKoinPlatformModule
)
```

## Dependency Flow (After Fix)

```
┌─────────────────┐
│   core.network  │
│                 │
│ AuthTokenProvider ←─────┐
│ (interface)     │       │
│                 │       │
│ NoAuthTokenProvider     │
│ (default impl)  │       │
└─────────────────┘       │
                          │
┌─────────────────┐       │
│   core.data     │       │
│                 │       │
│ includes        │       │
│ core.network    │       │
│                 │       │
│ UserAuthTokenProvider ──┘
│ (real impl)     │
│                 │
│ UserRepository  │
│ PostRepository  │
└─────────────────┘
```

## Benefits

1. **No Circular Dependency**: Clean dependency graph with proper separation of concerns
2. **Testability**: Easy to mock `AuthTokenProvider` for testing
3. **Flexibility**: Can easily switch authentication strategies
4. **Graceful Degradation**: Network requests work even if auth fails
5. **Single Responsibility**: Each module has a clear, focused responsibility

## Key Principles Applied

1. **Dependency Inversion Principle**: High-level modules don't depend on low-level modules; both depend on abstractions
2. **Interface Segregation**: Small, focused interface for auth token provision
3. **Single Responsibility**: Each class has one reason to change
4. **Open/Closed Principle**: Easy to extend with new auth providers without modifying existing code

## Testing Strategy

The fix enables better testing:

```kotlin
// Easy to test with mock auth provider
val testModule = module {
    single<AuthTokenProvider> { MockAuthTokenProvider("test-token") }
}
```

## Migration Notes

- No breaking changes to existing API
- All existing functionality preserved
- Better error handling for auth failures
- Improved separation of concerns

## Additional Fixes Applied

### Import Path Corrections
Fixed incorrect import paths in repository classes:

```kotlin
// BEFORE (incorrect paths)
import rfm.hillsongptapp.core.data.repository.ktor.ApiService
import rfm.hillsongptapp.core.data.repository.ktor.requests.*
import rfm.hillsongptapp.core.data.repository.ktor.responses.*

// AFTER (correct paths)
import rfm.hillsongptapp.core.network.ktor.ApiService
import rfm.hillsongptapp.core.network.ktor.requests.*
import rfm.hillsongptapp.core.network.ktor.responses.*
```

### Koin Type Inference Fix
Specified explicit types for Koin dependency injection:

```kotlin
// BEFORE (type inference error)
api = get(), // Cannot infer type

// AFTER (explicit type)
api = get<ApiService>(), // ✅ Clear type specification
```

This fix resolves the circular dependency while maintaining all existing functionality and improving the overall architecture.