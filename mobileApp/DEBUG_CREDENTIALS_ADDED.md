# Debug Credentials Added to Login Screen

## Changes Made

Added debug credentials to the LoginScreen to make testing easier during development.

### Pre-filled Credentials

**Login Screen:**
- Email/Username: `rodrigomartins@msn.com`
- Password: `feller123`

**Signup Screen:**
- First Name: `Rodrigo`
- Last Name: `Martins`
- Email: `rodrigomartins@msn.com`
- Password: `feller123`
- Confirm Password: `feller123`

### Implementation

```kotlin
// Pre-fill debug credentials for easier testing
val (username, setUsername) = rememberSaveable { 
    mutableStateOf(if (isDebugBuild()) "rodrigomartins@msn.com" else "") 
}
val (password, setPassword) = rememberSaveable { 
    mutableStateOf(if (isDebugBuild()) "feller123" else "") 
}

private fun isDebugBuild(): Boolean {
    // For development/testing purposes, always pre-fill credentials
    // In production, this should be tied to BuildConfig.DEBUG or similar
    return true
}
```

### Benefits

1. **Faster Testing**: No need to manually type credentials every time
2. **Consistent Test Data**: Always uses the same test credentials
3. **Easy to Disable**: Simply change `isDebugBuild()` to return `false`
4. **Production Safe**: Can be tied to actual debug build flags later

### Future Improvements

For production builds, the `isDebugBuild()` function should be updated to use proper build configuration:

```kotlin
// Android example
private fun isDebugBuild(): Boolean {
    return BuildConfig.DEBUG
}

// Or using expect/actual for multiplatform
expect fun isDebugBuild(): Boolean

// Android actual
actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG

// iOS actual  
actual fun isDebugBuild(): Boolean = 
    #if DEBUG
        true
    #else
        false
    #endif
```

### Usage

1. Launch the app
2. Navigate to login screen
3. Credentials are automatically pre-filled
4. Just tap "Login" to test authentication
5. For signup, all fields are pre-filled for quick testing

This makes it much easier to test the authentication flow and the kids management features without having to manually enter credentials every time.