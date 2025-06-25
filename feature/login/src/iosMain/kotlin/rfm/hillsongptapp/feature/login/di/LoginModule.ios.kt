package rfm.hillsongptapp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.feature.login.GoogleAuthProvider

actual val koinPlatformModule: Module =
    module {
        factoryOf(::GoogleAuthProvider) bind GoogleAuthProvider::class
    }