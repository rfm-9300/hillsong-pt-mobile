# Real-Time Status Updates Implementation

This document describes the implementation of real-time status updates for the Kids Management feature, which provides live notifications and automatic UI refresh when child status changes occur.

## Overview

The real-time updates system enables parents and staff to receive immediate notifications when:
- Children are checked in or out of services
- Service capacity changes
- Connection status changes
- Errors occur

## Architecture

### Components

1. **RealTimeStatusManager**: Core component that manages WebSocket connections and processes real-time messages
2. **ConnectionStatusIndicator**: UI component that shows the current connection status
3. **StatusNotificationSystem**: UI components for displaying notifications
4. **Enhanced ViewModels**: Updated to integrate with real-time updates

### Data Flow

```
WebSocket Server → KidsRemoteDataSource → RealTimeStatusManager → ViewModel → UI Components
```

## Key Features

### 1. WebSocket Integration

- **Connection Management**: Automatic connection, reconnection with exponential backoff
- **Message Processing**: Handles various message types (child status, service capacity, check-in/out updates)
- **Error Handling**: Graceful handling of connection failures and message parsing errors

### 2. Status Change Notifications

- **Child Status Changes**: Notifications when children are checked in/out
- **Service Capacity Updates**: Alerts when services reach capacity
- **Connection Status**: Visual indicators for connection state

### 3. Automatic UI Refresh

- **Real-time Updates**: UI automatically refreshes when status changes are received
- **Optimistic Updates**: Local changes are applied immediately, then synchronized with server
- **Fallback Mechanisms**: Manual refresh available when real-time connection is unavailable

### 4. Connection Status Indicators

- **Visual Indicators**: Color-coded dots and text showing connection status
- **Status Banner**: Non-intrusive banner for connection issues
- **Retry Functionality**: Easy retry for failed connections

## Implementation Details

### RealTimeStatusManager

```kotlin
class RealTimeStatusManager(
    private val remoteDataSource: KidsRemoteDataSource,
    private val coroutineScope: CoroutineScope
) {
    // Connection status flow
    val connectionStatus: StateFlow<ConnectionStatus>
    
    // Update flows
    val childStatusUpdates: SharedFlow<ChildStatusUpdate>
    val serviceStatusUpdates: SharedFlow<ServiceStatusUpdate>
    val checkInUpdates: SharedFlow<CheckInStatusUpdate>
    val notifications: SharedFlow<StatusNotification>
    
    // Connection management
    suspend fun connect(): Result<Unit>
    suspend fun disconnect()
    
    // Subscription management
    suspend fun subscribeToChild(childId: String): Result<Unit>
    suspend fun subscribeToService(serviceId: String): Result<Unit>
}
```

### Connection States

- **DISCONNECTED**: No connection to real-time updates
- **CONNECTING**: Attempting to establish connection
- **CONNECTED**: Successfully connected and receiving updates
- **RECONNECTING**: Connection lost, attempting to reconnect
- **DISCONNECTING**: Gracefully disconnecting
- **FAILED**: Connection failed after maximum retry attempts

### Message Types

1. **ChildStatusUpdateMessage**: Child check-in/out status changes
2. **ServiceCapacityUpdateMessage**: Service capacity updates
3. **CheckInUpdateMessage**: Check-in events
4. **CheckOutUpdateMessage**: Check-out events
5. **ConnectionEstablishedMessage**: Connection confirmation
6. **HeartbeatMessage**: Connection health check
7. **ErrorMessage**: Error notifications

### Notification Types

- **CONNECTION_ESTABLISHED**: Real-time connection active
- **CONNECTION_LOST**: Connection lost
- **CONNECTION_FAILED**: Connection failed
- **CHILD_STATUS_CHANGED**: Child status updated
- **CHILD_CHECKED_IN**: Child checked into service
- **CHILD_CHECKED_OUT**: Child checked out of service
- **SERVICE_FULL**: Service reached capacity
- **SERVICE_AVAILABLE**: Service has available spots
- **ERROR**: General error occurred

## UI Components

### ConnectionStatusIndicator

Shows current connection status with:
- Color-coded status dot (green=connected, orange=connecting, red=failed, gray=offline)
- Status text
- Pulsing animation for transitional states

### ConnectionStatusBanner

Non-intrusive banner that appears for non-connected states:
- Shows connection status and description
- Retry button for failed connections
- Dismissible (optional)

### StatusNotificationSystem

Manages notification display:
- **FloatingNotificationOverlay**: Slides down from top of screen
- **NotificationToast**: Compact notification at bottom
- **StatusNotificationCard**: Full notification with details
- **NotificationBadge**: Shows unread notification count

## Error Handling

### Connection Errors

- **Network Failures**: Automatic retry with exponential backoff
- **Authentication Errors**: Clear error messages with retry options
- **Server Errors**: Fallback to manual refresh mode

### Message Processing Errors

- **Invalid Messages**: Logged and ignored, connection remains active
- **Parsing Errors**: Graceful handling with unknown message type
- **Processing Errors**: Error notifications to user

### Fallback Mechanisms

When real-time connection is unavailable:
- Manual refresh functionality remains available
- Pull-to-refresh for data updates
- Visual indicators show offline status
- Cached data is used until connection is restored

## Testing

### Unit Tests

- **RealTimeStatusManagerTest**: Connection management and message processing
- **ConnectionStatusIndicatorTest**: UI component behavior
- **StatusNotificationSystemTest**: Notification system functionality
- **KidsManagementViewModelTest**: ViewModel integration with real-time updates

### Integration Tests

- **RealTimeUpdatesIntegrationTest**: End-to-end real-time update flow
- Connection failure and recovery scenarios
- Multiple notification handling
- Fallback mechanism testing

## Configuration

### Dependency Injection

```kotlin
val kidsDataModule = module {
    // Real-time Status Manager
    singleOf(::RealTimeStatusManager)
    
    // Other dependencies...
}
```

### ViewModel Integration

```kotlin
class KidsManagementViewModel(
    private val kidsRepository: KidsRepository,
    private val realTimeStatusManager: RealTimeStatusManager
) : ViewModel() {
    
    val connectionStatus: StateFlow<ConnectionStatus> = realTimeStatusManager.connectionStatus
    val activeNotifications: StateFlow<List<StatusNotification>>
    
    // Real-time update handlers
    private fun setupRealTimeUpdates()
    private fun handleChildStatusUpdate(update: ChildStatusUpdate)
    private fun handleServiceStatusUpdate(update: ServiceStatusUpdate)
}
```

## Performance Considerations

### Connection Management

- **Connection Pooling**: Reuse WebSocket connections
- **Heartbeat Monitoring**: Regular connection health checks
- **Automatic Cleanup**: Proper resource cleanup on disconnect

### Message Processing

- **Efficient Parsing**: Fast JSON parsing for real-time messages
- **Background Processing**: Message processing on background threads
- **Memory Management**: Proper cleanup of message flows

### UI Updates

- **Minimal Recomposition**: Efficient Compose UI updates
- **Debounced Updates**: Prevent excessive UI refreshes
- **Selective Updates**: Only update affected UI components

## Security Considerations

### Authentication

- **Secure WebSocket**: WSS protocol for encrypted communication
- **Token-based Auth**: JWT tokens for WebSocket authentication
- **Session Management**: Proper session handling and renewal

### Data Protection

- **Message Validation**: Validate all incoming messages
- **Access Control**: Ensure users only receive updates for their children
- **Audit Logging**: Log all real-time events for security monitoring

## Monitoring and Debugging

### Logging

- **Connection Events**: Log all connection state changes
- **Message Processing**: Log message types and processing results
- **Error Tracking**: Comprehensive error logging with context

### Metrics

- **Connection Uptime**: Track connection stability
- **Message Latency**: Monitor real-time update delays
- **Error Rates**: Track connection and processing errors

### Debug Tools

- **Connection Status**: Visual indicators for debugging
- **Message Inspector**: Development tools for message inspection
- **Performance Monitoring**: Track real-time update performance

## Future Enhancements

### Planned Features

1. **Push Notifications**: Native push notifications when app is backgrounded
2. **Offline Sync**: Queue updates when offline and sync when reconnected
3. **Advanced Filtering**: More granular subscription options
4. **Analytics Integration**: Track real-time update usage and performance

### Scalability Improvements

1. **Connection Pooling**: Share connections across features
2. **Message Batching**: Batch multiple updates for efficiency
3. **Selective Subscriptions**: More targeted update subscriptions
4. **Caching Strategy**: Intelligent caching of real-time data

## Troubleshooting

### Common Issues

1. **Connection Failures**: Check network connectivity and server status
2. **Missing Updates**: Verify subscription setup and message processing
3. **Performance Issues**: Monitor message frequency and processing time
4. **UI Not Updating**: Check ViewModel integration and state flows

### Debug Steps

1. Check connection status indicator
2. Verify WebSocket connection in network logs
3. Monitor message processing in application logs
4. Test fallback mechanisms with manual refresh
5. Validate subscription setup for specific children/services