package rfm.hillsongptapp.feature.home.di
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.home.ui.screens.HomeViewModel

val featureHomeModule = lazyModule {
    viewModel<HomeViewModel>{
        HomeViewModel(get(), get())
    }
}