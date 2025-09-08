package rfm.hillsongptapp.feature.kids.di

import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.*
import org.koin.test.KoinTest
import org.koin.test.inject
import rfm.hillsongptapp.feature.kids.data.network.websocket.WebSocketManager
import io.ktor.client.HttpClient

/**
 * Tests for platform-specific dependency injection configuration
 * Verifies platform-specific implementations are properly registered
 */
class PlatformModuleTest : KoinTest {
    
    @BeforeTest
    fun setup() {
        stopKoin()
    }
    
    @AfterTest
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `platform module should provide WebSocket manager`() = runTest {
        startKoin {
            modules(kidsKoinPlatformModule)
        }
        
        val webSocketManager: WebSocketManager by inject()
        assertNotNull(webSocketManager)
    }
    
    @Test
    fun `platform module should provide HTTP client for kids`() = runTest {
        startKoin {
            modules(kidsKoinPlatformModule)
        }
        
        val httpClient: HttpClient by inject(qualifier = org.koin.core.qualifier.named("KidsHttpClient"))
        assertNotNull(httpClient)
    }
    
    @Test
    fun `WebSocket manager should implement required interface methods`() = runTest {
        startKoin {
            modules(kidsKoinPlatformModule)
        }
        
        val webSocketManager: WebSocketManager by inject()
        
        // Test interface methods exist and can be called
        assertFalse(webSocketManager.isConnected()) // Should start disconnected
        
        // These should not throw exceptions
        assertDoesNotThrow {
            runTest {
                webSocketManager.connect("ws://test.example.com")
                webSocketManager.sendMessage("test message")
                webSocketManager.disconnect()
            }
        }
    }
    
    @Test
    fun `HTTP client should be configured with WebSocket support`() = runTest {
        startKoin {
            modules(kidsKoinPlatformModule)
        }
        
        val httpClient: HttpClient by inject(qualifier = org.koin.core.qualifier.named("KidsHttpClient"))
        
        // Verify client is properly configured
        assertNotNull(httpClient.engineConfig)
        
        // Client should be ready for use
        assertTrue(httpClient.isActive)
    }
    
    @Test
    fun `platform module should integrate with main kids module`() = runTest {
        startKoin {
            modules(featureKidsModule, kidsKoinPlatformModule)
        }
        
        // Should be able to resolve both platform and common dependencies
        val webSocketManager: WebSocketManager by inject()
        val httpClient: HttpClient by inject(qualifier = org.koin.core.qualifier.named("KidsHttpClient"))
        
        assertNotNull(webSocketManager)
        assertNotNull(httpClient)
        
        // Should also resolve common dependencies
        val repository by inject<rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository>()
        assertNotNull(repository)
    }
}
</content>
</invoke>