package rfm.hillsongptapp.feature.kids.ui.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var viewModel: ServicesViewModel
    
    private val mockService1 = KidsService(
        id = "service-1",
        name = "Toddler Time",
        description = "Service for toddlers",
        minAge = 2,
        maxAge = 4,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room A",
        maxCapacity = 15,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val mockService2 = KidsService(
        id = "service-2",
        name = "Kids Church",
        description = "Service for school age kids",
        minAge = 5,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room B",
        maxCapacity = 20,
        currentCapacity = 20, // Full capacity
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-2", "staff-3"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val mockService3 = KidsService(
        id = "service-3",
        name = "Youth Group",
        description = "Service for teenagers",
        minAge = 13,
        maxAge = 17,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room C",
        maxCapacity = 25,
        currentCapacity = 5,
        isAcceptingCheckIns = false, // Not accepting check-ins
        staffMembers = listOf("staff-4"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val mockChild = Child(
        id = "child-123",
        parentId = "parent-123",
        name = "Test Child",
        dateOfBirth = "2018-01-01", // 7 years old
        emergencyContact = EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        mockRepository.servicesResult = Result.success(listOf(mockService1, mockService2, mockService3))
        viewModel = ServicesViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state loads services successfully`() = runTest {
        // Wait for initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(3, state.services.size)
        assertEquals(3, state.filteredServices.size)
        assertEquals("Toddler Time", state.services[0].name)
        assertEquals("Kids Church", state.services[1].name)
        assertEquals("Youth Group", state.services[2].name)
    }
    
    @Test
    fun `loadServices handles error correctly`() = runTest {
        // Setup error
        mockRepository.servicesResult = Result.failure(Exception("Network error"))
        
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
        assertTrue(state.services.isEmpty())
    }
    
    @Test
    fun `refreshServices updates data`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Update mock data
        val updatedService = mockService1.copy(currentCapacity = 12)
        mockRepository.servicesResult = Result.success(listOf(updatedService, mockService2, mockService3))
        
        viewModel.refreshServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        
        assertFalse(state.isRefreshing)
        assertEquals(12, state.services[0].currentCapacity)
    }
    
    @Test
    fun `setSelectedChild filters services by age eligibility`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Set child (7 years old - eligible for Kids Church only)
        viewModel.setSelectedChild(mockChild)
        
        val state = viewModel.uiState.first()
        
        assertEquals(mockChild, state.selectedChild)
        // Services should be sorted with eligible ones first
        // Kids Church (5-12) should be first, then others
        assertTrue(state.filteredServices.any { it.id == "service-2" })
    }
    
    @Test
    fun `updateFilters applies availability filter correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply filter for available services only
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        viewModel.updateFilters(filters)
        
        val state = viewModel.uiState.first()
        
        assertEquals(filters, state.filters)
        // Should exclude service-2 (full capacity) and service-3 (not accepting)
        assertEquals(1, state.filteredServices.size)
        assertEquals("service-1", state.filteredServices[0].id)
    }
    
    @Test
    fun `updateFilters applies accepting check-ins filter correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply filter for services accepting check-ins
        val filters = ServiceFilters(availability = ServiceFilters.Availability.ACCEPTING_CHECKINS)
        viewModel.updateFilters(filters)
        
        val state = viewModel.uiState.first()
        
        // Should exclude service-3 (not accepting check-ins) but include service-2 (full but accepting)
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.all { it.isAcceptingCheckIns })
    }
    
    @Test
    fun `updateFilters applies age range filter correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply age filter for 5-10 year olds
        val filters = ServiceFilters(minAge = 5, maxAge = 10)
        viewModel.updateFilters(filters)
        
        val state = viewModel.uiState.first()
        
        // Should include Kids Church (5-12) but exclude Toddler Time (2-4) and Youth Group (13-17)
        assertEquals(1, state.filteredServices.size)
        assertEquals("service-2", state.filteredServices[0].id)
    }
    
    @Test
    fun `updateFilters applies show full services filter correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply filter to hide full services
        val filters = ServiceFilters(showFullServices = false)
        viewModel.updateFilters(filters)
        
        val state = viewModel.uiState.first()
        
        // Should exclude service-2 (at capacity)
        assertEquals(2, state.filteredServices.size)
        assertFalse(state.filteredServices.any { it.isAtCapacity() })
    }
    
    @Test
    fun `clearFilters resets to default state`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply some filters
        val filters = ServiceFilters(
            availability = ServiceFilters.Availability.AVAILABLE_ONLY,
            minAge = 5,
            showFullServices = false
        )
        viewModel.updateFilters(filters)
        
        // Clear filters
        viewModel.clearFilters()
        
        val state = viewModel.uiState.first()
        
        assertEquals(ServiceFilters(), state.filters)
        assertEquals(3, state.filteredServices.size) // All services should be shown
    }
    
    @Test
    fun `getServicesForChild returns eligible and available services only`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        val eligibleServices = viewModel.getServicesForChild(mockChild)
        
        // Child is 7 years old, so only Kids Church (5-12) is age-eligible
        // But Kids Church is full, so no services should be returned
        assertEquals(0, eligibleServices.size)
    }
    
    @Test
    fun `getServicesForChild returns available services for eligible child`() = runTest {
        // Setup service with available spots
        val availableService = mockService2.copy(currentCapacity = 15) // Not full
        mockRepository.servicesResult = Result.success(listOf(mockService1, availableService, mockService3))
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val eligibleServices = viewModel.getServicesForChild(mockChild)
        
        // Child is 7 years old, Kids Church (5-12) is age-eligible and has spots
        assertEquals(1, eligibleServices.size)
        assertEquals("service-2", eligibleServices[0].id)
    }
    
    @Test
    fun `clearError removes error message`() = runTest {
        // Setup error state
        mockRepository.servicesResult = Result.failure(Exception("Test error"))
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error exists
        var state = viewModel.uiState.first()
        assertNotNull(state.error)
        
        // Clear error
        viewModel.clearError()
        
        state = viewModel.uiState.first()
        assertNull(state.error)
    }
    
    // Mock repository implementation
    private class MockKidsRepository : KidsRepository {
        var servicesResult: Result<List<KidsService>> = Result.success(emptyList())
        var childrenResult: Result<List<Child>> = Result.success(emptyList())
        
        override suspend fun getAvailableServices(): Result<List<KidsService>> = servicesResult
        override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> = servicesResult
        override suspend fun getServiceById(serviceId: String): Result<KidsService> = 
            Result.failure(NotImplementedError())
        override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> = servicesResult
        
        override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = childrenResult
        override suspend fun registerChild(child: Child): Result<Child> = Result.failure(NotImplementedError())
        override suspend fun updateChild(child: Child): Result<Child> = Result.failure(NotImplementedError())
        override suspend fun deleteChild(childId: String): Result<Unit> = Result.failure(NotImplementedError())
        
        override suspend fun checkInChild(childId: String, serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
            Result.failure(NotImplementedError())
        override suspend fun checkOutChild(childId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
            Result.failure(NotImplementedError())
        override suspend fun getCheckInHistory(childId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
        override suspend fun getCurrentCheckIns(serviceId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
        override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.ServiceReport> = 
            Result.failure(NotImplementedError())
        override suspend fun getAllCurrentCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
    }
}