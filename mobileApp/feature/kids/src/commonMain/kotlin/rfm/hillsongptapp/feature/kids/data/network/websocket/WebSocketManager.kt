package rfm.hillsongptapp.feature.kids.data.network.websocket

/**
 * Platform-agnostic WebSocket manager interface
 * Provides abstraction for real-time connection management across platforms
 */
interface WebSocketManager {
    
    /**
     * Establishes WebSocket connection to the specified URL
     * @param url The WebSocket endpoint URL
     */
    suspend fun connect(url: String)
    
    /**
     * Closes the WebSocket connection gracefully
     */
    suspend fun disconnect()
    
    /**
     * Sends a message through the WebSocket connection
     * @param message The message to send
     */
    suspend fun sendMessage(message: String)
    
    /**
     * Checks if the WebSocket connection is currently active
     * @return true if connected, false otherwise
     */
    fun isConnected(): Boolean
}
