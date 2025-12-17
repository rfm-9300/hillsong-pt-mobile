package rfm.hillsongptapp.core.network.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import rfm.hillsongptapp.core.network.api.AuthApiService
import rfm.hillsongptapp.core.network.api.AuthApiServiceImpl
import rfm.hillsongptapp.core.network.api.CalendarApiService
import rfm.hillsongptapp.core.network.api.CalendarApiServiceImpl
import rfm.hillsongptapp.core.network.api.CheckInRequestApiService
import rfm.hillsongptapp.core.network.api.CheckInRequestApiServiceImpl
import rfm.hillsongptapp.core.network.api.EncountersApiService
import rfm.hillsongptapp.core.network.api.EncountersApiServiceImpl
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.api.EventsApiServiceImpl
import rfm.hillsongptapp.core.network.api.GroupsApiService
import rfm.hillsongptapp.core.network.api.GroupsApiServiceImpl
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.api.KidsApiServiceImpl
import rfm.hillsongptapp.logging.LoggerHelper
import rfm.hillsongptapp.core.network.api.PostsApiService
import rfm.hillsongptapp.core.network.api.PostsApiServiceImpl
import rfm.hillsongptapp.core.network.api.PrayerApiService
import rfm.hillsongptapp.core.network.api.PrayerApiServiceImpl
import rfm.hillsongptapp.core.network.api.ProfileApiService
import rfm.hillsongptapp.core.network.api.ProfileApiServiceImpl
import rfm.hillsongptapp.core.network.api.YouTubeVideosApiService
import rfm.hillsongptapp.core.network.api.YouTubeVideosApiServiceImpl
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.auth.NoAuthTokenProvider
import rfm.hillsongptapp.core.network.ktor.ApiService
import rfm.hillsongptapp.core.network.provider.httpClientEngine
import rfm.hillsongptapp.core.network.websocket.CheckInWebSocketClient

val networkModule =
    module {

        
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
                
                install(WebSockets) {
                    pingIntervalMillis = 20_000 // 20 seconds
                }

                // Add auth interceptor using AuthTokenProvider
                defaultRequest {
                    try {
                        LoggerHelper.logDebug("Adding auth header to request", "NetworkAuth")
                        val authTokenProvider = get<AuthTokenProvider>()
                        LoggerHelper.logDebug("Got AuthTokenProvider instance: ${authTokenProvider::class.simpleName}", "NetworkAuth")
                        val token = runBlocking { 
                            LoggerHelper.logDebug("Calling authTokenProvider.getAuthToken()", "NetworkAuth")
                            authTokenProvider.getAuthToken()
                        }
                        LoggerHelper.logDebug("Token retrieved: ${if (token != null) "Present (${token.take(10)}...)" else "NULL"}", "NetworkAuth")
                        token?.let {
                            header("Authorization", "Bearer $it")
                            LoggerHelper.logDebug("Authorization header added", "NetworkAuth")
                        }
                    } catch (e: Exception) {
                        LoggerHelper.logDebug("Error adding auth header: ${e.message}", "NetworkAuth")
                        e.printStackTrace()
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
        
        single<EncountersApiService> {
            EncountersApiServiceImpl(
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
        
        single<KidsApiService> {
            KidsApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<CheckInRequestApiService> {
            CheckInRequestApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }
        
        single<YouTubeVideosApiService> {
            YouTubeVideosApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }

        single<CalendarApiService> {
            CalendarApiServiceImpl(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
            )
        }

        // WebSocket Client for real-time check-in updates
        factory {
            val authTokenProvider = get<AuthTokenProvider>()
            val token = runBlocking { authTokenProvider.getAuthToken() } ?: ""
            CheckInWebSocketClient(
                httpClient = get(),
                baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl")),
                authToken = token
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
                prayer = get(),
                kids = get()
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