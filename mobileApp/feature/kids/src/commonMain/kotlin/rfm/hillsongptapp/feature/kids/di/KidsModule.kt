package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import rfm.hillsongptapp.feature.kids.data.database.KidsDatabase
import rfm.hillsongptapp.feature.kids.data.database.kidsDatabaseInstance
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSourceImpl
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSourceImpl
import rfm.hillsongptapp.feature.kids.data.network.websocket.RealTimeStatusManager
import rfm.hillsongptapp.feature.kids.data.repository.KidsRepositoryImpl
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutChildUseCase
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInViewModel
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutViewModel
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditViewModel
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsViewModel
import rfm.hillsongptapp.feature.kids.ui.services.ServicesViewModel
import rfm.hillsongptapp.core.data.repository.UserRepository

/**
 * Main Koin module for Kids Management feature
 * Combines all data, domain, and UI layer dependencies with proper scoping
 */
val featureKidsModule = lazyModule {
    
    // Database
    single<KidsDatabase> { kidsDatabaseInstance() }
    
    // Data Sources
    singleOf(::KidsLocalDataSourceImpl) bind KidsLocalDataSource::class
    singleOf(::KidsRemoteDataSourceImpl) bind KidsRemoteDataSource::class
    
    // Real-time Status Manager with proper lifecycle management
    singleOf(::RealTimeStatusManager)
    
    // Repository
    singleOf(::KidsRepositoryImpl) bind KidsRepository::class
    
    // Use Cases
    factoryOf(::CheckInChildUseCase)
    factoryOf(::CheckOutChildUseCase)
    
    // ViewModels
    viewModelOf(::KidsManagementViewModel)
    viewModelOf(::CheckInViewModel)
    viewModelOf(::CheckOutViewModel)
    viewModelOf(::ChildRegistrationViewModel)
    viewModelOf(::ChildEditViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::ServicesViewModel)
}


