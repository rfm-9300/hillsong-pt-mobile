# Kids Management Feature - Dependency Injection Setup

## Overview

The Kids Management feature uses Koin for dependency injection with a comprehensive module structure that supports both common and platform-specific dependencies. The setup includes proper lifecycle management for WebSocket connections and follows the established app architecture patterns.

## Module Structure

### Main Module: `featureKidsModule`

Located in `feature/kids/src/commonMain/kotlin/rfm/hillsongptapp/feature/kids/di/KidsModule.kt`

This is the primary module that should be used for the Kids Management feature. It includes:

- **Database**: KidsDatabase singleton instance
- **Data Sources**: Local and Remote data sources with singleton scope
- **Real-time Manager**: RealTimeStatusManager for WebSocket connections
- **Repository**: KidsRepository implementation with singleton scope
- **Use Cases**: CheckIn and CheckOut use cases with factory scope
- **ViewModels**: All screen ViewModels with proper ViewModel scope

### Platform-Specific Modules

#### Android: `kidsKoinPlatformModule`
Located in `feature/kids/src/androidMain/kotlin/rfm/hillsongptapp/feature/kids/di/KidsModule.android.kt`

Provides:
- Android-specific HTTP Client with OkHttp engine
- AndroidWebSocketManager with lifecycle awareness
- Android Context for platform-specific operations

#### iOS: `kidsKoinPlatformModule`
Located in `feature/kids/src/iosMain/kotlin/rfm/hillsongptapp/feature/kids/di/KidsModule.ios.kt`

Provides:
- iOS-specific HTTP Client with Darwin engine
- IosWebSocketManager with app state awareness
- iOS-specific configurations

## Dependency Scopes

### Singleton Scope
- `KidsDatabase`: Single instance across the app
- `KidsLocalDataSource`: Single instance for local data operations
- `KidsRemoteDataSource`: Single instance for network operations
- `RealTimeStatusManager`: Single instance for WebSocket management
- `KidsRepository`: Single instance as the main data access point

### Factory Scope
- `CheckInChildUseCase`: New instance per injection
- `CheckOutChildUseCase`: New instance per injection

### ViewModel Scope
- All ViewModels use Koin's `viewModelOf` for proper lifecycle management

## Integration with Main App

The Kids feature is integrated into the main app through `composeApp/src/commonMain/kotlin/rfm/hillsongptapp/di/AppModules.kt`:

```kotlin
val featureModules = listOf(
    featureHomeModule,
    featureLoginModule,
    featureFeedModule,
    featureKidsModule  // Added
)

val coreModules = listOf(
    dataModule,
    koinPlatformModule,
    kidsKoinPlatformModule  // Added
)
```

## WebSocket Lifecycle Management

### Real-time Connection Management
- `RealTimeStatusManager` handles WebSocket connections for real-time updates
- Platform-specific `WebSocketManager` implementations handle lifecycle events
- Automatic reconnection and connection state management
- Proper cleanup on app backgrounding/foregrounding

### Platform-Specific Lifecycle Handling

#### Android
- `AndroidWebSocketManager` handles app lifecycle events
- Manages connections during background/foreground transitions
- Uses Android Context for lifecycle awareness

#### iOS
- `IosWebSocketManager` handles iOS app state changes
- Manages connections during app state transitions
- Integrates with iOS app lifecycle notifications

## Database Configuration

### Room Database Setup
- Platform-specific database instances through `kidsDatabaseInstance()`
- Proper migration handling through `KidsDatabaseMigrations`
- SQLite driver configuration for each platform

### DAO Registration
- All DAOs are automatically available through the KidsDatabase instance
- Proper transaction handling and connection pooling

## Network Configuration

### HTTP Client Setup
- Named qualifier "KidsHttpClient" for kids-specific network operations
- WebSocket support enabled for real-time features
- Platform-optimized engines (OkHttp for Android, Darwin for iOS)
- Proper timeout and retry configurations

### API Integration
- `KidsRemoteDataSource` uses the configured HTTP client
- Automatic JSON serialization/deserialization
- Error handling and network state management

## Testing Configuration

### Test Modules
Comprehensive test suite includes:
- `KidsModuleTest`: Basic module verification and dependency resolution
- `PlatformModuleTest`: Platform-specific dependency testing
- `WebSocketLifecycleTest`: WebSocket connection lifecycle testing
- `KidsModuleIntegrationTest`: End-to-end integration testing

### Test Coverage
- All dependencies can be resolved correctly
- Proper scoping (singleton vs factory) verification
- Platform-specific implementations work correctly
- WebSocket lifecycle management functions properly
- Integration with main app modules

## Migration from Legacy Modules

### Deprecated Modules
The following modules are now deprecated in favor of `featureKidsModule`:
- `kidsDataModule` - Use `featureKidsModule` instead
- `kidsUseCaseModule` - Use `featureKidsModule` instead  
- `kidsUiModule` - Use `featureKidsModule` instead

### Backward Compatibility
Legacy modules are still functional for backward compatibility but should be migrated to use the new `featureKidsModule`.

## Usage Examples

### Injecting Dependencies in ViewModels
```kotlin
class KidsManagementViewModel(
    private val repository: KidsRepository,
    private val checkInUseCase: CheckInChildUseCase
) : ViewModel()
```

### Injecting Dependencies in Use Cases
```kotlin
class CheckInChildUseCase(
    private val kidsRepository: KidsRepository
)
```

### Platform-Specific Dependencies
```kotlin
// In platform-specific code
val httpClient: HttpClient by inject(named("KidsHttpClient"))
val webSocketManager: WebSocketManager by inject()
```

## Best Practices

1. **Use the main `featureKidsModule`** instead of individual modules
2. **Include platform module** alongside the main module
3. **Proper lifecycle management** for WebSocket connections
4. **Test dependency resolution** in unit tests
5. **Follow established scoping patterns** (singleton for stateful, factory for stateless)

## Troubleshooting

### Common Issues
1. **Missing platform module**: Ensure `kidsKoinPlatformModule` is included
2. **WebSocket connection issues**: Check platform-specific WebSocket manager configuration
3. **Database access errors**: Verify `kidsDatabaseInstance()` is properly configured
4. **Circular dependencies**: Use factory scope for use cases to avoid circular references

### Debug Tips
1. Use Koin's `verify()` method to check module configuration
2. Enable Koin logging to trace dependency resolution
3. Check platform-specific implementations are properly registered
4. Verify all required dependencies are available in the module