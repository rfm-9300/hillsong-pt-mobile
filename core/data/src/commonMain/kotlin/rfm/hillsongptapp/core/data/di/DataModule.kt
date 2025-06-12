package rfm.hillsongptapp.core.data.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.providers.httpClientEngine
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.ktor.ApiService

val dataModule =
    module {
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

            }
        }

        // Api
        single {
            ApiService(
                baseUrl = "https://activehive.pt:8080",
                httpClient = get(),
            )
        }
        // Db
        single<UserDao> {
            databaseInstance().userDao()
        }
        single {
            UserRepository(
                userDao = get<UserDao>(),
                api = get(),
            )
        }
    }