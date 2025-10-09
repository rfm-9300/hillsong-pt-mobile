package rfm.hillsongptapp.core.network.websocket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import rfm.hillsongptapp.core.network.ktor.responses.CheckInStatusNotification
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * WebSocket client for real-time check-in status updates
 * Connects to the backend WebSocket endpoint and listens for status notifications
 * 
 * Features:
 * - Automatic reconnection on connection loss
 * - Connection state management
 * - Error handling with exponential backoff
 * - Thread-safe operations
 * 
 * Usage:
 * ```kotlin
 * val client = CheckInWebSocketClient(httpClient, baseUrl, authToken)
 * client.connect(userId) { notification ->
 *     // Handle status update
 * }
 * // Later...
 * client.disconnect()
 * ```
 */
class CheckInWebSocketClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val authToken: String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private var connectionJob: Job? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var onStatusUpdateCallback: ((CheckInStatusNotification) -> Unit)? = null
    private var currentUserId: Long? = null
    
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val baseReconnectDelay = 1000L // 1 second
    
    /**
     * Connect to the WebSocket server and subscribe to check-in status updates
     * 
     * @param userId The user ID to subscribe to notifications for
     * @param onStatusUpdate Callback invoked when a status update is received
     */
    fun connect(userId: Long, onStatusUpdate: (CheckInStatusNotification) -> Unit) {
        LoggerHelper.logDebug("Connecting to WebSocket for user $userId", "CheckInWebSocket")
        
        currentUserId = userId
        onStatusUpdateCallback = onStatusUpdate
        
        // Cancel any existing connection
        disconnect()
        
        // Start connection
        connectionJob = scope.launch {
            connectInternal(userId)
        }
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        LoggerHelper.logDebug("Disconnecting from WebSocket", "CheckInWebSocket")
        
        reconnectJob?.cancel()
        reconnectJob = null
        
        connectionJob?.cancel()
        connectionJob = null
        
        _connectionState.value = ConnectionState.Disconnected
        reconnectAttempts = 0
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        disconnect()
        scope.cancel()
    }
    
    private suspend fun connectInternal(userId: Long) {
        try {
            _connectionState.value = ConnectionState.Connecting
            
            // Convert HTTP(S) URL to WS(S) URL
            val wsUrl = baseUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            
            val fullUrl = "$wsUrl/ws"
            
            LoggerHelper.logDebug("Connecting to WebSocket at: $fullUrl", "CheckInWebSocket")
            
            httpClient.webSocket(
                urlString = fullUrl,
                request = {
                    // Add auth token to headers
                    headers.append("Authorization", "Bearer $authToken")
                }
            ) {
                _connectionState.value = ConnectionState.Connected
                reconnectAttempts = 0 // Reset reconnect attempts on successful connection
                
                LoggerHelper.logDebug("WebSocket connected successfully", "CheckInWebSocket")
                
                // Send STOMP CONNECT frame
                val connectFrame = """
                    CONNECT
                    accept-version:1.2
                    heart-beat:10000,10000
                    Authorization:Bearer $authToken
                    
                    
                """.trimIndent()
                
                send(Frame.Text(connectFrame))
                
                // Subscribe to user-specific queue
                val subscribeFrame = """
                    SUBSCRIBE
                    id:sub-0
                    destination:/user/$userId/queue/checkin-status
                    
                    
                """.trimIndent()
                
                send(Frame.Text(subscribeFrame))
                
                LoggerHelper.logDebug("Subscribed to /user/$userId/queue/checkin-status", "CheckInWebSocket")
                
                // Listen for incoming messages
                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                handleIncomingMessage(text)
                            }
                            is Frame.Close -> {
                                LoggerHelper.logDebug("WebSocket closed by server", "CheckInWebSocket")
                                break
                            }
                            else -> {
                                // Ignore other frame types
                            }
                        }
                    }
                } catch (e: Exception) {
                    LoggerHelper.logError("Error reading WebSocket frames: ${e.message}", tag = "CheckInWebSocket")
                    throw e
                }
            }
            
            // Connection closed normally
            LoggerHelper.logDebug("WebSocket connection closed", "CheckInWebSocket")
            _connectionState.value = ConnectionState.Disconnected
            
        } catch (e: Exception) {
            LoggerHelper.logError("WebSocket connection error: ${e.message}", tag = "CheckInWebSocket")
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            
            // Attempt reconnection if we haven't exceeded max attempts
            if (reconnectAttempts < maxReconnectAttempts && scope.isActive) {
                scheduleReconnect(userId)
            }
        }
    }
    
    private fun handleIncomingMessage(message: String) {
        try {
            LoggerHelper.logDebug("Received WebSocket message: $message", "CheckInWebSocket")
            
            // Parse STOMP frame
            if (message.startsWith("MESSAGE")) {
                // Extract message body from STOMP frame
                val lines = message.split("\n")
                val bodyStartIndex = lines.indexOfFirst { it.isEmpty() } + 1
                
                if (bodyStartIndex > 0 && bodyStartIndex < lines.size) {
                    val body = lines.subList(bodyStartIndex, lines.size).joinToString("\n").trim()
                    
                    if (body.isNotEmpty()) {
                        // Parse JSON notification
                        val notification = json.decodeFromString<CheckInStatusNotification>(body)
                        LoggerHelper.logDebug("Parsed notification: $notification", "CheckInWebSocket")
                        
                        // Invoke callback
                        onStatusUpdateCallback?.invoke(notification)
                    }
                }
            } else if (message.startsWith("CONNECTED")) {
                LoggerHelper.logDebug("STOMP CONNECTED frame received", "CheckInWebSocket")
            } else if (message.startsWith("ERROR")) {
                LoggerHelper.logError("STOMP ERROR frame received: $message", tag = "CheckInWebSocket")
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error parsing WebSocket message: ${e.message}", tag = "CheckInWebSocket")
        }
    }
    
    private fun scheduleReconnect(userId: Long) {
        reconnectAttempts++
        val delay = calculateReconnectDelay()
        
        LoggerHelper.logDebug(
            "Scheduling reconnect attempt $reconnectAttempts/$maxReconnectAttempts in ${delay}ms",
            "CheckInWebSocket"
        )
        
        _connectionState.value = ConnectionState.Reconnecting(reconnectAttempts, maxReconnectAttempts)
        
        reconnectJob = scope.launch {
            delay(delay)
            if (scope.isActive) {
                connectInternal(userId)
            }
        }
    }
    
    private fun calculateReconnectDelay(): Long {
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s
        return baseReconnectDelay * (1 shl (reconnectAttempts - 1).coerceAtMost(4))
    }
    
    /**
     * Connection state sealed class
     */
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Reconnecting(val attempt: Int, val maxAttempts: Int) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}
