package rfm.hillsongptapp.feature.login.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.login.LoginViewModel

val featureLoginModule = lazyModule {
    viewModel<LoginViewModel> {
        LoginViewModel(

        )
    }
}