package rfm.hillsongptapp.core.data.di

import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.core.data.auth.AuthTokenManager
import rfm.hillsongptapp.core.data.auth.AuthTokenProviderImpl
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.repository.PostRepository
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsRepositoryImpl
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.network.HillsongApiClient
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.di.networkModule
import rfm.hillsongptapp.core.network.ktor.ApiService


val dataModule =
    module {
        // Database DAOs
        single<UserDao> {
            databaseInstance().userDao()
        }
        single<UserProfileDao> {
            databaseInstance().userProfileDao()
        }
        single<ChildDao> {
            databaseInstance().childDao()
        }
        single<CheckInRecordDao> {
            databaseInstance().checkInRecordDao()
        }
        single<KidsServiceDao> {
            databaseInstance().kidsServiceDao()
        }

        // Auth Token Manager - handles token storage and refresh (without apiClient dependency for now)
        single<AuthTokenManager> {
            AuthTokenManager(
                userDao = get(),
                apiClient = get() // This will be resolved after HillsongApiClient is created
            )
        }

        // Auth Token Provider - MUST be defined before HTTP client
        single<AuthTokenProvider> { 
            AuthTokenProviderImpl(authTokenManager = get()) 
        }
        
        // Now include network module AFTER AuthTokenProvider is defined
        includes(networkModule)

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
                apiClient = get<HillsongApiClient>()
            )
        }

        single<KidsRepository> {
            KidsRepositoryImpl(
                childDao = get<ChildDao>(),
                checkInRecordDao = get<CheckInRecordDao>(),
                kidsServiceDao = get<KidsServiceDao>(),
                kidsApiService = get<KidsApiService>()
            )
        }
    }