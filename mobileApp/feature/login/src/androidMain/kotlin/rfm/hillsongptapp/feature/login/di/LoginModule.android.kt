package rfm.hillsongptapp.feature.login.di

import androidx.credentials.CredentialManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.java.KoinAndroidApplication.create
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rfm.hillsongptapp.feature.login.GoogleAuthProvider
import kotlin.reflect.KClass

actual val koinPlatformModule: Module =
    module {
        factory { CredentialManager.create(androidContext()) }
        factoryOf(::GoogleAuthProvider) bind GoogleAuthProvider::class
    }