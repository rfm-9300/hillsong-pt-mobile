package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInViewModel
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutViewModel
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationViewModel
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsViewModel
import rfm.hillsongptapp.feature.kids.ui.services.ServicesViewModel

/**
 * Koin module for Kids Management UI layer dependencies
 * Note: This module is now included in the main featureKidsModule
 * @deprecated Use featureKidsModule instead
 */
@Deprecated("Use featureKidsModule instead", ReplaceWith("featureKidsModule"))
val kidsUiModule = module {
    
    // ViewModels
    viewModelOf(::KidsManagementViewModel)
    viewModelOf(::CheckInViewModel)
    viewModelOf(::CheckOutViewModel)
    viewModelOf(::ChildRegistrationViewModel)
    viewModelOf(::ChildEditViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::ServicesViewModel)
}