package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInViewModel
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutViewModel
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationViewModel
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsViewModel
import rfm.hillsongptapp.feature.kids.ui.services.ServicesViewModel

/**
 * Simplified Koin module for Kids Management UI layer dependencies
 * Only contains ViewModels that depend on core repositories
 * Follows the same pattern as feed and auth features
 */
val featureKidsModule = lazyModule {
    
    // ViewModels - using core repositories from DI
    viewModel<KidsManagementViewModel> {
        KidsManagementViewModel(
            kidsRepository = get(),
            authRepository = get()
        )
    }
    
    viewModel<CheckInViewModel> {
        CheckInViewModel(
            kidsRepository = get(),
            authRepository = get()
        )
    }
    
    viewModel<CheckOutViewModel> {
        CheckOutViewModel(
            kidsRepository = get(),
            authRepository = get()
        )
    }
    
    viewModel<ChildRegistrationViewModel> {
        ChildRegistrationViewModel(
            kidsRepository = get(),
            authRepository = get()
        )
    }
    
    viewModel<ChildEditViewModel> {
        ChildEditViewModel(
            kidsRepository = get(),
            authRepository = get()
        )
    }
    
    viewModel<ReportsViewModel> {
        ReportsViewModel(
            kidsRepository = get()
        )
    }
    
    viewModel<ServicesViewModel> {
        ServicesViewModel(
            kidsRepository = get()
        )
    }
}