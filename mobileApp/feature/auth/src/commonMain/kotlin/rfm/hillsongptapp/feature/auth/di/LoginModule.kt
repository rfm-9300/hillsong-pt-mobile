package rfm.hillsongptapp.feature.auth.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.auth.AuthViewModel

val featureLoginModule = lazyModule {
    viewModel<AuthViewModel> {
        AuthViewModel(
            authRepository = get(),
        )
    }
}

expect val koinPlatformModule: Module