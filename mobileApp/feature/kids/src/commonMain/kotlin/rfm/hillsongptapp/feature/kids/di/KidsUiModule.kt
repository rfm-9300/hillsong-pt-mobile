package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInRequestViewModel
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutViewModel
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationViewModel
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsViewModel
import rfm.hillsongptapp.feature.kids.ui.services.ServicesViewModel
import rfm.hillsongptapp.feature.kids.ui.staff.StaffCheckInViewModel
import rfm.hillsongptapp.feature.kids.ui.staff.StaffDashboardViewModel

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
            authRepository = get(),
            checkInRequestApiService = get()
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
    
    viewModel<CheckInRequestViewModel> {
        CheckInRequestViewModel(
            checkInRequestApiService = get(),
            authRepository = get(),
            webSocketClient = get()
        )
    }
    
    viewModel<StaffCheckInViewModel> {
        StaffCheckInViewModel(
            checkInRequestApiService = get()
        )
    }
    
    viewModel<StaffDashboardViewModel> {
        StaffDashboardViewModel(
            kidsApiService = get(),
            checkInRequestApiService = get()
        )
    }
}