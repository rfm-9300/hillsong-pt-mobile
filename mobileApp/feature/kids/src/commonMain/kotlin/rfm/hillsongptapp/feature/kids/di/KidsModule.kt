package rfm.hillsongptapp.feature.kids.di

import org.koin.core.module.dsl.viewModel
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

/**
 * Main Koin module for Kids Management feature
 * Combines all data, domain, and UI layer dependencies with proper scoping
 */
val featureKidsModule = lazyModule {
    
    // Database
    single { kidsDatabaseInstance() }
    
    // Data Sources
    single<KidsLocalDataSource> { 
        KidsLocalDataSourceImpl(
            database = get()
        )
    }
    
    single<KidsRemoteDataSource> { 
        KidsRemoteDataSourceImpl(
            httpClient = get(),
            baseUrl = "https://api.hillsong.pt", // TODO: Move to config
            json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
    
    // Real-time Status Manager
    single<RealTimeStatusManager> { 
        RealTimeStatusManager(
            remoteDataSource = get()
        )
    }
    
    // Repository
    single<KidsRepository> { 
        KidsRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get()
        )
    }
    
    // Error Handling
    single { 
        rfm.hillsongptapp.feature.kids.domain.error.ErrorHandler(
            logger = co.touchlab.kermit.Logger.withTag("ErrorHandler")
        )
    }
    
    single { 
        rfm.hillsongptapp.feature.kids.domain.offline.OfflineHandler(
            logger = co.touchlab.kermit.Logger.withTag("OfflineHandler")
        )
    }
    
    single { 
        rfm.hillsongptapp.feature.kids.domain.error.ErrorRecoveryManager(
            errorHandler = get(),
            offlineHandler = get(),
            logger = co.touchlab.kermit.Logger.withTag("ErrorRecoveryManager")
        )
    }
    
    // Use Cases
    factory<CheckInChildUseCase> { 
        CheckInChildUseCase(
            kidsRepository = get()
        )
    }
    
    factory<CheckOutChildUseCase> { 
        CheckOutChildUseCase(
            kidsRepository = get()
        )
    }
    
    // ViewModels
    viewModel<KidsManagementViewModel> {
        KidsManagementViewModel(
            kidsRepository = get(),
            realTimeStatusManager = get(),
            userRepository = get()
        )
    }
    
    viewModel<CheckInViewModel> {
        CheckInViewModel(
            checkInChildUseCase = get(),
            errorHandler = get(),
            errorRecoveryManager = get(),
            offlineHandler = get()
        )
    }
    
    viewModel<CheckOutViewModel> {
        CheckOutViewModel(
            kidsRepository = get(),
            checkOutChildUseCase = get()
        )
    }
    
    viewModel<ChildRegistrationViewModel> {
        ChildRegistrationViewModel(
            kidsRepository = get()
        )
    }
    
    viewModel<ChildEditViewModel> {
        ChildEditViewModel(
            kidsRepository = get()
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


