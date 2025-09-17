package rfm.hillsongptapp.core.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.auth.NoAuthTokenProvider
import rfm.hillsongptapp.core.network.ktor.ApiService
import rfm.hillsongptapp.core.network.provider.httpClientEngine

val networkModule =
    module {
        // Auth Token Provider - default implementation (will be overridden by data module)
        single<AuthTokenProvider> { NoAuthTokenProvider() }
        
        // Api - base URL will be provided during Koin initialization
        single {
            ApiService(
                baseUrl = getProperty("API_BASE_URL", "https://activehive.pt:443"),
                httpClient = get(),
            )
        }

        // http client
        single {
            HttpClient(
                engine = httpClientEngine(),
            ) {
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = LogLevel.HEADERS
                }

                install(ContentNegotiation) {
                    json(
                        json =
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                            },
                    )
                }

                // Add auth interceptor using AuthTokenProvider
                defaultRequest {
                    val authTokenProvider = get<AuthTokenProvider>()
                    val token = runBlocking { authTokenProvider.getAuthToken() }
                    token?.let {
                        header("Authorization", "Bearer $it")
                    }
                }
            }
        }
    }