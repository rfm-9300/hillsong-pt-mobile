package rfm.hillsongptapp.core.data.di

import org.koin.dsl.module
import rfm.hillsongptapp.core.data.providers.databaseInstance
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.UserDao

val dataModule =
    module {

        // Db
        single<UserDao> {
            databaseInstance().userDao()
        }
        single {
            UserRepository(
                userDao = get<UserDao>()
            )
        }
    }