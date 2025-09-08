package rfm.hillsongptapp.feature.kids.di

import kotlinx.coroutines.test.runTest
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.verify.verify
import kotlin.test.*
import org.koin.test.KoinTest
import org.koin.test.inject
import rfm.hillsongptapp.feature.kids.data.database.KidsDatabase
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.websocket.RealTimeStatusManager
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
 * Comprehensive tests for Kids Management dependency injection configuration
 * Verifies all dependencies are properly registered and can be resolved
 */
@OptIn(KoinExperimentalAPI::class)
class KidsModuleTest : KoinTest {
    
    @BeforeTest
    fun setup() {
        stopKoin()
    }
    
    @AfterTest
    fun tearDown() {
        stopKoin()
    }
    
    @Test
    fun `featureKidsModule should verify successfully`() {
        // Verify module configuration is correct
        featureKidsModule.verify()
    }
    
    @Test
    fun `should resolve all data layer dependencies`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        // Database
        val database: KidsDatabase by inject()
        assertNotNull(database)
        
        // Data Sources
        val localDataSource: KidsLocalDataSource by inject()
        assertNotNull(localDataSource)
        
        val remoteDataSource: KidsRemoteDataSource by inject()
        assertNotNull(remoteDataSource)
        
        // Real-time Manager
        val realTimeManager: RealTimeStatusManager by inject()
        assertNotNull(realTimeManager)
        
        // Repository
        val repository: KidsRepository by inject()
        assertNotNull(repository)
    }
    
    @Test
    fun `should resolve all use case dependencies`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        val checkInUseCase: CheckInChildUseCase by inject()
        assertNotNull(checkInUseCase)
        
        val checkOutUseCase: CheckOutChildUseCase by inject()
        assertNotNull(checkOutUseCase)
    }
    
    @Test
    fun `should resolve all ViewModel dependencies`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        val kidsManagementViewModel: KidsManagementViewModel by inject()
        assertNotNull(kidsManagementViewModel)
        
        val checkInViewModel: CheckInViewModel by inject()
        assertNotNull(checkInViewModel)
        
        val checkOutViewModel: CheckOutViewModel by inject()
        assertNotNull(checkOutViewModel)
        
        val childRegistrationViewModel: ChildRegistrationViewModel by inject()
        assertNotNull(childRegistrationViewModel)
        
        val childEditViewModel: ChildEditViewModel by inject()
        assertNotNull(childEditViewModel)
        
        val reportsViewModel: ReportsViewModel by inject()
        assertNotNull(reportsViewModel)
        
        val servicesViewModel: ServicesViewModel by inject()
        assertNotNull(servicesViewModel)
    }
    
    @Test
    fun `should maintain singleton scope for appropriate dependencies`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        // Database should be singleton
        val database1: KidsDatabase by inject()
        val database2: KidsDatabase by inject()
        assertSame(database1, database2)
        
        // Repository should be singleton
        val repository1: KidsRepository by inject()
        val repository2: KidsRepository by inject()
        assertSame(repository1, repository2)
        
        // Real-time manager should be singleton
        val manager1: RealTimeStatusManager by inject()
        val manager2: RealTimeStatusManager by inject()
        assertSame(manager1, manager2)
    }
    
    @Test
    fun `should create new instances for factory scoped dependencies`() = runTest {
        startKoin {
            modules(featureKidsModule)
        }
        
        // Use cases should be factory scoped (new instances)
        val useCase1: CheckInChildUseCase by inject()
        val useCase2: CheckInChildUseCase by inject()
        assertNotSame(useCase1, useCase2)
    }
    
    @Test
    fun `should handle circular dependencies correctly`() = runTest {
        // This test ensures there are no circular dependency issues
        assertDoesNotThrow {
            startKoin {
                modules(featureKidsModule)
            }
            
            // Try to resolve all major dependencies
            val repository: KidsRepository by inject()
            val checkInUseCase: CheckInChildUseCase by inject()
            val viewModel: KidsManagementViewModel by inject()
            
            assertNotNull(repository)
            assertNotNull(checkInUseCase)
            assertNotNull(viewModel)
        }
    }
    
    @Test
    fun `deprecated modules should still work for backward compatibility`() = runTest {
        // Test that deprecated modules still function
        assertDoesNotThrow {
            startKoin {
                modules(kidsDataModule, kidsUseCaseModule, kidsUiModule)
            }
            
            val repository: KidsRepository by inject()
            val useCase: CheckInChildUseCase by inject()
            val viewModel: KidsManagementViewModel by inject()
            
            assertNotNull(repository)
            assertNotNull(useCase)
            assertNotNull(viewModel)
        }
    }
}
</content>
</invoke>