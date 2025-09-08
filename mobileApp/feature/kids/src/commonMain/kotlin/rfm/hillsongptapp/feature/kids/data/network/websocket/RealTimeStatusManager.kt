package rfm.hillsongptapp.feature.kids.data.network.websocket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import co.touchlab.kermit.Logger
import kotlinx.datetime.Clock
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource

/**
 * Manages real-time status updates and WebSocket connection for kids management
 */
class RealTimeStatusManager(
    private val remoteDataSource: KidsRemoteDataSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    private val logger = Logger.withTag("RealTimeStatusManager")
    
    // Connection status
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    // Status update flows
    private val _childStatusUpdates = MutableSharedFlow<ChildStatusUpdate>()
    val childStatusUpdates: SharedFlow<ChildStatusUpdate> = _childStatusUpdates.asSharedFlow()
    
    private val _serviceStatusUpdates = MutableSharedFlow<ServiceStatusUpdate>()
    val serviceStatusUpdates: SharedFlow<ServiceStatusUpdate> = _serviceStatusUpdates.asSharedFlow()
    
    private val _checkInUpdates = MutableSharedFlow<CheckInStatusUpdate>()
    val checkInUpdates: SharedFlow<CheckInStatusUpdate> = _checkInUpdates.asSharedFlow()
    
    // Notification flows
    private val _notifications = MutableSharedFlow<StatusNotification>()
    val notifications: SharedFlow<StatusNotification> = _notifications.asSharedFlow()
    
    // Connection management
    private var connectionJob: Job? = null
    private var messageProcessingJob: Job? = null
    private var heartbeatJob: Job? = null
    
    // Retry configuration
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val baseRetryDelay = 1000L // 1 second
    
    // Subscriptions tracking
    private val childSubscriptions = mutableSetOf<String>()
    private val serviceSubscriptions = mutableSetOf<String>()
    
    init {
        startConnectionMonitoring()
    }
    
    /**
     * Connect to the WebSocket and start real-time updates
     */
    suspend fun connect(): Result<Unit> {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
            return Result.success(Unit)
        }
        
        _connectionStatus.value = ConnectionStatus.CONNECTING
        
        return try {
            val result = remoteDataSource.connectWebSocket()
            
            if (result.isSuccess) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                reconnectAttempts = 0
                
                startMessageProcessing()
                startHeartbeat()
                resubscribeToUpdates()
                
                logger.i { "WebSocket connected successfully" }
                
                // Send connection notification
                _notifications.emit(
                    StatusNotification(
                        type = NotificationType.CONNECTION_ESTABLISHED,
                        title = "Real-time Updates Connected",
                        message = "You'll now receive live status updates",
                        timestamp = System.currentTimeMillis()
                    )
                )
                
                Result.success(Unit)
            } else {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
                logger.e { "Failed to connect WebSocket: ${result.exceptionOrNull()?.message}" }
                Result.failure(result.exceptionOrNull() ?: Exception("Connection failed"))
            }
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            logger.e(e) { "Error connecting WebSocket" }
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from the WebSocket
     */
    suspend fun disconnect() {
        logger.i { "Disconnecting WebSocket" }
        
        _connectionStatus.value = ConnectionStatus.DISCONNECTING
        
        // Cancel all jobs
        connectionJob?.cancel()
        messageProcessingJob?.cancel()
        heartbeatJob?.cancel()
        
        // Disconnect from remote
        remoteDataSource.disconnectWebSocket()
        
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        
        // Clear subscriptions
        childSubscriptions.clear()
        serviceSubscriptions.clear()
        
        // Send disconnection notification
        _notifications.emit(
            StatusNotification(
                type = NotificationType.CONNECTION_LOST,
                title = "Real-time Updates Disconnected",
                message = "You may not receive live status updates",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Subscribe to real-time updates for a specific child
     */
    suspend fun subscribeToChild(childId: String): Result<Unit> {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED) {
            // Store subscription for when we reconnect
            childSubscriptions.add(childId)
            return Result.success(Unit)
        }
        
        return try {
            val result = remoteDataSource.subscribeToChildUpdates(childId)
            if (result.isSuccess) {
                childSubscriptions.add(childId)
                logger.d { "Subscribed to child updates: $childId" }
            }
            result
        } catch (e: Exception) {
            logger.e(e) { "Error subscribing to child updates: $childId" }
            Result.failure(e)
        }
    }
    
    /**
     * Subscribe to real-time updates for a specific service
     */
    suspend fun subscribeToService(serviceId: String): Result<Unit> {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED) {
            // Store subscription for when we reconnect
            serviceSubscriptions.add(serviceId)
            return Result.success(Unit)
        }
        
        return try {
            val result = remoteDataSource.subscribeToServiceUpdates(serviceId)
            if (result.isSuccess) {
                serviceSubscriptions.add(serviceId)
                logger.d { "Subscribed to service updates: $serviceId" }
            }
            result
        } catch (e: Exception) {
            logger.e(e) { "Error subscribing to service updates: $serviceId" }
            Result.failure(e)
        }
    }
    
    /**
     * Unsubscribe from all real-time updates
     */
    suspend fun unsubscribeFromAll(): Result<Unit> {
        return try {
            val result = remoteDataSource.unsubscribeFromUpdates()
            if (result.isSuccess) {
                childSubscriptions.clear()
                serviceSubscriptions.clear()
                logger.d { "Unsubscribed from all updates" }
            }
            result
        } catch (e: Exception) {
            logger.e(e) { "Error unsubscribing from updates" }
            Result.failure(e)
        }
    }
    
    /**
     * Check if currently connected to WebSocket
     */
    fun isConnected(): Boolean {
        return _connectionStatus.value == ConnectionStatus.CONNECTED
    }
    
    /**
     * Get fallback mechanism status
     */
    fun hasFallbackMechanism(): Boolean {
        // In a real implementation, this could check for alternative update mechanisms
        // like periodic polling when WebSocket is unavailable
        return true
    }
    
    private fun startConnectionMonitoring() {
        connectionJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // Check connection status
                    val isConnected = remoteDataSource.isWebSocketConnected()
                    
                    if (!isConnected && _connectionStatus.value == ConnectionStatus.CONNECTED) {
                        logger.w { "WebSocket connection lost, attempting to reconnect" }
                        _connectionStatus.value = ConnectionStatus.RECONNECTING
                        
                        // Send connection lost notification
                        _notifications.emit(
                            StatusNotification(
                                type = NotificationType.CONNECTION_LOST,
                                title = "Connection Lost",
                                message = "Attempting to reconnect...",
                                timestamp = 0L
                            )
                        )
                        
                        attemptReconnect()
                    }
                    
                    delay(5000) // Check every 5 seconds
                } catch (e: Exception) {
                    logger.e(e) { "Error in connection monitoring" }
                    delay(5000)
                }
            }
        }
    }
    
    private suspend fun attemptReconnect() {
        while (reconnectAttempts < maxReconnectAttempts) {
            try {
                reconnectAttempts++
                val delay = baseRetryDelay * (1 shl (reconnectAttempts - 1)) // Exponential backoff
                
                logger.i { "Reconnection attempt $reconnectAttempts/$maxReconnectAttempts" }
                
                delay(delay)
                
                val result = connect()
                if (result.isSuccess) {
                    logger.i { "Reconnection successful" }
                    return
                }
            } catch (e: Exception) {
                logger.e(e) { "Reconnection attempt $reconnectAttempts failed" }
            }
        }
        
        if (reconnectAttempts >= maxReconnectAttempts) {
            logger.e { "Max reconnection attempts reached, giving up" }
            _connectionStatus.value = ConnectionStatus.FAILED
            
            // Send final failure notification
            _notifications.emit(
                StatusNotification(
                    type = NotificationType.CONNECTION_FAILED,
                    title = "Connection Failed",
                    message = "Unable to establish real-time connection. Using offline mode.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
    
    private fun startMessageProcessing() {
        messageProcessingJob = coroutineScope.launch {
            remoteDataSource.getWebSocketMessages()
                .catch { e -> 
                    logger.e(e) { "Error in WebSocket message flow" }
                    _connectionStatus.value = ConnectionStatus.DISCONNECTED
                }
                .collect { message ->
                    processWebSocketMessage(message)
                }
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob = coroutineScope.launch {
            while (isActive && _connectionStatus.value == ConnectionStatus.CONNECTED) {
                try {
                    delay(30000) // Send heartbeat every 30 seconds
                    
                    // In a real implementation, you might send a heartbeat message
                    // For now, we'll just check if the connection is still active
                    if (!remoteDataSource.isWebSocketConnected()) {
                        logger.w { "Heartbeat failed, connection appears to be lost" }
                        _connectionStatus.value = ConnectionStatus.DISCONNECTED
                        break
                    }
                } catch (e: Exception) {
                    logger.e(e) { "Error in heartbeat" }
                    break
                }
            }
        }
    }
    
    private suspend fun resubscribeToUpdates() {
        // Resubscribe to all previously subscribed children and services
        childSubscriptions.forEach { childId ->
            remoteDataSource.subscribeToChildUpdates(childId)
        }
        
        serviceSubscriptions.forEach { serviceId ->
            remoteDataSource.subscribeToServiceUpdates(serviceId)
        }
    }
    
    private suspend fun processWebSocketMessage(message: WebSocketMessage) {
        try {
            when (message) {
                is ChildStatusUpdateMessage -> {
                    val update = ChildStatusUpdate(
                        childId = message.child.id,
                        previousStatus = message.previousStatus,
                        newStatus = message.newStatus,
                        serviceId = message.serviceId,
                        timestamp = message.timestamp.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    
                    _childStatusUpdates.emit(update)
                    
                    // Create notification for status change
                    val notification = StatusNotification(
                        type = NotificationType.CHILD_STATUS_CHANGED,
                        title = "Child Status Updated",
                        message = "${message.child.name} is now ${message.newStatus.lowercase()}",
                        childId = message.child.id,
                        serviceId = message.serviceId,
                        timestamp = update.timestamp
                    )
                    _notifications.emit(notification)
                }
                
                is ServiceCapacityUpdateMessage -> {
                    val update = ServiceStatusUpdate(
                        serviceId = message.service.id,
                        serviceName = message.service.name,
                        previousCapacity = message.previousCapacity,
                        newCapacity = message.newCapacity,
                        maxCapacity = message.service.maxCapacity,
                        timestamp = message.timestamp.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    
                    _serviceStatusUpdates.emit(update)
                    
                    // Create notification for capacity changes
                    if (message.newCapacity >= message.service.maxCapacity) {
                        val notification = StatusNotification(
                            type = NotificationType.SERVICE_FULL,
                            title = "Service Full",
                            message = "${message.service.name} is now at full capacity",
                            serviceId = message.service.id,
                            timestamp = update.timestamp
                        )
                        _notifications.emit(notification)
                    }
                }
                
                is CheckInUpdateMessage -> {
                    val update = CheckInStatusUpdate(
                        recordId = message.record.id,
                        childId = message.record.childId,
                        childName = message.child.name,
                        serviceId = message.record.serviceId,
                        serviceName = message.service.name,
                        action = CheckInAction.CHECK_IN,
                        timestamp = message.timestamp.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    
                    _checkInUpdates.emit(update)
                    
                    // Create notification for check-in
                    val notification = StatusNotification(
                        type = NotificationType.CHILD_CHECKED_IN,
                        title = "Child Checked In",
                        message = "${message.child.name} checked into ${message.service.name}",
                        childId = message.child.id,
                        serviceId = message.service.id,
                        timestamp = update.timestamp
                    )
                    _notifications.emit(notification)
                }
                
                is CheckOutUpdateMessage -> {
                    val update = CheckInStatusUpdate(
                        recordId = message.record.id,
                        childId = message.record.childId,
                        childName = message.child.name,
                        serviceId = message.record.serviceId,
                        serviceName = message.service.name,
                        action = CheckInAction.CHECK_OUT,
                        timestamp = message.timestamp.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    
                    _checkInUpdates.emit(update)
                    
                    // Create notification for check-out
                    val notification = StatusNotification(
                        type = NotificationType.CHILD_CHECKED_OUT,
                        title = "Child Checked Out",
                        message = "${message.child.name} checked out of ${message.service.name}",
                        childId = message.child.id,
                        serviceId = message.service.id,
                        timestamp = update.timestamp
                    )
                    _notifications.emit(notification)
                }
                
                is ConnectionEstablishedMessage -> {
                    logger.i { "WebSocket connection established with session: ${message.sessionId}" }
                }
                
                is HeartbeatMessage -> {
                    // Respond to heartbeat
                    // In a real implementation, you might send a heartbeat response
                    logger.d { "Received heartbeat" }
                }
                
                is ErrorMessage -> {
                    logger.e { "WebSocket error: ${message.error}" }
                    
                    val notification = StatusNotification(
                        type = NotificationType.ERROR,
                        title = "Connection Error",
                        message = message.error,
                        timestamp = message.timestamp.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    _notifications.emit(notification)
                }
                
                else -> {
                    logger.d { "Received unhandled WebSocket message: ${message.type}" }
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error processing WebSocket message: ${message.type}" }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        coroutineScope.launch {
            disconnect()
        }
        coroutineScope.cancel()
    }
}

/**
 * Connection status enum
 */
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING,
    FAILED
}

/**
 * Data classes for status updates
 */
data class ChildStatusUpdate(
    val childId: String,
    val previousStatus: String,
    val newStatus: String,
    val serviceId: String?,
    val timestamp: Long
)

data class ServiceStatusUpdate(
    val serviceId: String,
    val serviceName: String,
    val previousCapacity: Int,
    val newCapacity: Int,
    val maxCapacity: Int,
    val timestamp: Long
)

data class CheckInStatusUpdate(
    val recordId: String,
    val childId: String,
    val childName: String,
    val serviceId: String,
    val serviceName: String,
    val action: CheckInAction,
    val timestamp: Long
)

enum class CheckInAction {
    CHECK_IN,
    CHECK_OUT
}

/**
 * Notification system for status changes
 */
data class StatusNotification(
    val type: NotificationType,
    val title: String,
    val message: String,
    val childId: String? = null,
    val serviceId: String? = null,
    val timestamp: Long
)

enum class NotificationType {
    CONNECTION_ESTABLISHED,
    CONNECTION_LOST,
    CONNECTION_FAILED,
    CHILD_STATUS_CHANGED,
    CHILD_CHECKED_IN,
    CHILD_CHECKED_OUT,
    SERVICE_FULL,
    SERVICE_AVAILABLE,
    ERROR
}