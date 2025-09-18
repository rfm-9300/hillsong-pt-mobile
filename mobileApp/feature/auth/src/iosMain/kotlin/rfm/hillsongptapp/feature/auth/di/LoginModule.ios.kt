package rfm.hillsongptapp.feature.auth.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.feature.auth.GoogleAuthProvider

actual val koinPlatformModule: Module =
    module {
        factoryOf(::GoogleAuthProvider) bind GoogleAuthProvider::class
    }