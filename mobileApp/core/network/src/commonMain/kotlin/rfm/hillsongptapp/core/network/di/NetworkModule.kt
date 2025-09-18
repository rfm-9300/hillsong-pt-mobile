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
import rfm.hillsongptapp.core.network.api.AuthApiService
import rfm.hillsongptapp.core.network.api.AuthApiServiceImpl
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.api.EventsApiServiceImpl
import rfm.hillsongptapp.core.network.api.GroupsApiService
import rfm.hillsongptapp.core.network.api.GroupsApiServiceImpl
import rfm.hillsongptapp.core.network.api.PostsApiService
import rfm.hillsongptapp.core.network.api.PostsApiServiceImpl
import rfm.hillsongptapp.core.network.api.PrayerApiService
import rfm.hillsongptapp.core.network.api.PrayerApiServiceImpl
import rfm.hillsongptapp.core.network.api.ProfileApiService
import rfm.hillsongptapp.core.network.api.ProfileApiServiceImpl
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.auth.NoAuthTokenProvider
import rfm.hillsongptapp.core.network.ktor.ApiService
import rfm.hillsongptapp.core.network.provider.httpClientEngine

val networkModule =
    module {
        // Auth Token Provider - default implementation (will be overridden by data module)
        single<AuthTokenProvider> { NoAuthTokenProvider() }
        
        // Base URL configuration
        single<String>(qualifier = org.koin.core.qualifier.named("baseUrl")) {
            getProperty("API_BASE_URL", "https://activehive.pt:443")
        }
        
        // HTTP Client configuration
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
                                prettyPrint = true
                                encodeDefaults = false
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
        
        // Feature-specific API Services
        single<AuthApiService> {
            AuthApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<PostsApiService> {
            PostsApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<ProfileApiService> {
            ProfileApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<EventsApiService> {
            EventsApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<GroupsApiService> {
            GroupsApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<PrayerApiService> {
            PrayerApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        // Main API Client aggregating all services
        single {
            rfm.hillsongptapp.core.network.HillsongApiClient(
                auth = get(),
                posts = get(),
                profile = get(),
                events = get(),
                groups = get(),
                prayer = get()
            )
        }
        
        // Legacy ApiService for backward compatibility (deprecated)
        single {
            ApiService(
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl")),
                httpClient = get(),
            )
        }
    }