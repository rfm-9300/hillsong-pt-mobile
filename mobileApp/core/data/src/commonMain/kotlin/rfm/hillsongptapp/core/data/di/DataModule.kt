package rfm.hillsongptapp.core.data.di

import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.core.data.auth.AuthTokenManager
import rfm.hillsongptapp.core.data.auth.AuthTokenProviderImpl
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.repository.PostRepository
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.di.networkModule
import rfm.hillsongptapp.core.network.ktor.ApiService


val dataModule =
    module {
        // Include network module
        includes(networkModule)
        
        // Database DAOs
        single<UserDao> {
            databaseInstance().userDao()
        }
        single<UserProfileDao> {
            databaseInstance().userProfileDao()
        }

        // Auth Token Manager - handles token storage and refresh
        single<AuthTokenManager> {
            AuthTokenManager(
                userDao = get(),
                apiClient = get()
            )
        }

        // Auth Token Provider - override the default with real implementation
        single<AuthTokenProvider> { 
            AuthTokenProviderImpl(authTokenManager = get()) 
        } bind AuthTokenProvider::class

        // Repositories
        single {
            AuthRepository(
                userDao = get<UserDao>(),
                userProfileDao = get<UserProfileDao>(),
                apiClient = get<HillsongApiClient>(),
                authTokenManager = get<AuthTokenManager>(),
            )
        }

        single {
            PostRepository(
                api = get<ApiService>(), // Specify ApiService type explicitly
            )
        }
    }