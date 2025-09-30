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
import org.koin.dsl.module

/**
 * iOS-specific Koin module for Kids Management feature
 * Provides iOS-specific implementations for real-time connections and platform dependencies
 */
val kidsKoinPlatformModule: Module = module {
    
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
                // iOS WebSocket configuration
            }
        }
    }
}
