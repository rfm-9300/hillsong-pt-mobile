package rfm.hillsongptapp.core.data.di

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
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.providers.httpClientEngine
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.ktor.ApiService

val dataModule =
    module {
        // Db
        single<UserDao> {
            databaseInstance().userDao()
        }

        // Api
        single {
            ApiService(
                baseUrl = "https://activehive.pt:443",
                httpClient = get(),
            )
        }

        // Repository
        single {
            UserRepository(
                userDao = get<UserDao>(),
                api = get(),
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

                // Add auth interceptor
                defaultRequest {
                    val userRepository = get<UserRepository>()
                    val user = runBlocking { userRepository.getUserById(1) } // Assuming user ID 1 is the logged-in user
                    user?.token?.let { token ->
                        header("Authorization", "Bearer $token")
                    }
                }
            }
        }
    }