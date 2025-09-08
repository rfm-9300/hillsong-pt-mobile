package rfm.hillsongptapp.feature.kids.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import rfm.hillsongptapp.feature.kids.data.network.websocket.IosWebSocketManager
import rfm.hillsongptapp.feature.kids.data.network.websocket.WebSocketManager

/**
 * iOS-specific Koin module for Kids Management feature
 * Provides iOS-specific implementations for real-time connections and platform dependencies
 */
actual val kidsKoinPlatformModule: Module = module {
    
    // iOS-specific HTTP Client for Kids API with WebSocket support
    single<HttpClient>(qualifier = org.koin.core.qualifier.named("KidsHttpClient")) {
        HttpClient(Darwin) {
            engine {
                configureRequest {
                    setAllowsCellularAccess(true)
                    setTimeoutInterval(30.0)
                }
            }
            
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                })
            }
            
            install(Logging) {
                level = LogLevel.INFO
            }
            
            install(WebSockets) {
                pingInterval = 20_000
                maxFrameSize = Long.MAX_VALUE
            }
        }
    }
    
    // iOS-specific WebSocket Manager with app state awareness
    singleOf(::IosWebSocketManager) bind WebSocketManager::class
}

/**
 * iOS-specific WebSocket Manager implementation
 * Handles iOS app lifecycle events and background/foreground state changes
 */
class IosWebSocketManager(
    private val httpClient: HttpClient
) : WebSocketManager {
    
    private var isInBackground = false
    
    override suspend fun connect(url: String) {
        // iOS-specific connection logic with app state awareness
        if (!isInBackground) {
            // Implement connection logic
        }
    }
    
    override suspend fun disconnect() {
        // iOS-specific disconnection logic
    }
    
    override suspend fun sendMessage(message: String) {
        // iOS-specific message sending
    }
    
    override fun isConnected(): Boolean {
        // iOS-specific connection status
        return false // Placeholder
    }
    
    fun onAppDidEnterBackground() {
        isInBackground = true
        // Handle app entering background
    }
    
    fun onAppWillEnterForeground() {
        isInBackground = false
        // Handle app entering foreground
    }
}
</content>
</invoke>