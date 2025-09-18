package rfm.hillsongptapp.feature.auth.di

import androidx.credentials.CredentialManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.feature.auth.GoogleAuthProvider

actual val koinPlatformModule: Module =
    module {
        factory { CredentialManager.create(androidContext()) }
        factoryOf(::GoogleAuthProvider) bind GoogleAuthProvider::class
    }