package rfm.hillsongptapp.feature.home.di
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.home.ui.screens.HomeViewModel

val featureHomeModule = lazyModule {
    viewModel<HomeViewModel>{
        HomeViewModel(
            authRepository = get(),
            encountersApiService = get(),
            baseUrl = get(qualifier = org.koin.core.qualifier.named("baseUrl"))
        )
    }
}