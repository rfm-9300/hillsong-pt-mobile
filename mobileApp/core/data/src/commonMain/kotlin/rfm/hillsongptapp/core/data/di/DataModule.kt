package rfm.hillsongptapp.core.data.di

import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.core.data.auth.UserAuthTokenProvider
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.repository.PostRepository
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.network.auth.AuthTokenProvider
import rfm.hillsongptapp.core.network.di.networkModule
import rfm.hillsongptapp.core.network.ktor.ApiService


val dataModule =
    module {
        // Include network module
        includes(networkModule)
        
        // Db
        single<UserDao> {
            databaseInstance().userDao()
        }
        single<UserProfileDao> {
            databaseInstance().userProfileDao()
        }

        // Auth Token Provider - override the default with real implementation
        single<AuthTokenProvider> { 
            UserAuthTokenProvider(userDao = get()) 
        } bind AuthTokenProvider::class

        // Repository
        single {
            UserRepository(
                userDao = get<UserDao>(),
                api = get<ApiService>(), // Specify ApiService type explicitly
                userProfileDao = get<UserProfileDao>(),
            )
        }

        single {
            PostRepository(
                api = get<ApiService>(), // Specify ApiService type explicitly
            )
        }
    }