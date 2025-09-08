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

/**
 * Integration test for the complete services functionality
 * Tests the interaction between ViewModel, UI State, and filtering logic
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServicesIntegrationTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var viewModel: ServicesViewModel
    
    // Test data representing different service scenarios
    private val toddlerService = KidsService(
        id = "toddler-service",
        name = "Toddler Time",
        description = "Fun activities for toddlers",
        minAge = 2,
        maxAge = 4,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Toddler Room",
        maxCapacity = 12,
        currentCapacity = 8,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1", "staff-2"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val kidsService = KidsService(
        id = "kids-service",
        name = "Kids Church",
        description = "Sunday school for elementary kids",
        minAge = 5,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Kids Room",
        maxCapacity = 25,
        currentCapacity = 25, // Full capacity
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-3", "staff-4", "staff-5"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val youthService = KidsService(
        id = "youth-service",
        name = "Youth Group",
        description = "Activities for teenagers",
        minAge = 13,
        maxAge = 17,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Youth Room",
        maxCapacity = 30,
        currentCapacity = 15,
        isAcceptingCheckIns = false, // Not accepting check-ins
        staffMembers = listOf("staff-6"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val specialNeedsService = KidsService(
        id = "special-needs-service",
        name = "Special Needs Ministry",
        description = "Specialized care for children with special needs",
        minAge = 3,
        maxAge = 18,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Special Care Room",
        maxCapacity = 8,
        currentCapacity = 3,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-7", "staff-8"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    // Test children with different ages
    private val toddlerChild = Child(
        id = "toddler-child",
        parentId = "parent-1",
        name = "Little Toddler",
        dateOfBirth = "2022-06-01", // 3 years old
        emergencyContact = EmergencyContact("Parent", "+1234567890", "Parent"),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    private val schoolAgeChild = Child(
        id = "school-child",
        parentId = "parent-1",
        name = "School Kid",
        dateOfBirth = "2017-03-15", // 8 years old
        emergencyContact = EmergencyContact("Parent", "+1234567890", "Parent"),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    private val teenChild = Child(
        id = "teen-child",
        parentId = "parent-1",
        name = "Teenager",
        dateOfBirth = "2010-12-01", // 15 years old
        emergencyContact = EmergencyContact("Parent", "+1234567890", "Parent"),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        mockRepository.servicesResult = Result.success(
            listOf(toddlerService, kidsService, youthService, specialNeedsService)
        )
        viewModel = ServicesViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `complete services workflow - load, filter, and select for toddler`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        var state = viewModel.uiState.first()
        
        // Verify all services loaded
        assertEquals(4, state.services.size)
        assertEquals(4, state.filteredServices.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        // Set toddler child (3 years old)
        viewModel.setSelectedChild(toddlerChild)
        state = viewModel.uiState.first()
        
        // Verify child is set and services are prioritized
        assertEquals(toddlerChild, state.selectedChild)
        
        // Get eligible services for toddler (age 3)
        val eligibleServices = viewModel.getServicesForChild(toddlerChild)
        
        // Toddler should be eligible for:
        // - toddlerService (2-4 years, available) ✓
        // - specialNeedsService (3-18 years, available) ✓
        // Not eligible for:
        // - kidsService (5-12 years) ✗
        // - youthService (13-17 years) ✗
        assertEquals(2, eligibleServices.size)
        assertTrue(eligibleServices.any { it.id == "toddler-service" })
        assertTrue(eligibleServices.any { it.id == "special-needs-service" })
    }
    
    @Test
    fun `complete services workflow - load, filter, and select for school age child`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Set school age child (8 years old)
        viewModel.setSelectedChild(schoolAgeChild)
        val state = viewModel.uiState.first()
        
        assertEquals(schoolAgeChild, state.selectedChild)
        
        // Get eligible services for school age child (age 8)
        val eligibleServices = viewModel.getServicesForChild(schoolAgeChild)
        
        // School age child should be eligible for:
        // - specialNeedsService (3-18 years, available) ✓
        // Not eligible for:
        // - toddlerService (2-4 years) ✗
        // - kidsService (5-12 years, but full) ✗ (full capacity)
        // - youthService (13-17 years) ✗
        assertEquals(1, eligibleServices.size)
        assertEquals("special-needs-service", eligibleServices[0].id)
    }
    
    @Test
    fun `complete services workflow - load, filter, and select for teenager`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Set teenager (15 years old)
        viewModel.setSelectedChild(teenChild)
        val state = viewModel.uiState.first()
        
        assertEquals(teenChild, state.selectedChild)
        
        // Get eligible services for teenager (age 15)
        val eligibleServices = viewModel.getServicesForChild(teenChild)
        
        // Teenager should be eligible for:
        // - specialNeedsService (3-18 years, available) ✓
        // Not eligible for:
        // - toddlerService (2-4 years) ✗
        // - kidsService (5-12 years) ✗
        // - youthService (13-17 years, but not accepting) ✗ (not accepting check-ins)
        assertEquals(1, eligibleServices.size)
        assertEquals("special-needs-service", eligibleServices[0].id)
    }
    
    @Test
    fun `filter workflow - availability filters work correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Test AVAILABLE_ONLY filter
        viewModel.updateFilters(ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY))
        var state = viewModel.uiState.first()
        
        // Should exclude kidsService (full) and youthService (not accepting)
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.all { it.hasAvailableSpots() })
        
        // Test ACCEPTING_CHECKINS filter
        viewModel.updateFilters(ServiceFilters(availability = ServiceFilters.Availability.ACCEPTING_CHECKINS))
        state = viewModel.uiState.first()
        
        // Should exclude youthService (not accepting check-ins)
        assertEquals(3, state.filteredServices.size)
        assertTrue(state.filteredServices.all { it.isAcceptingCheckIns })
        
        // Test ALL filter (default)
        viewModel.updateFilters(ServiceFilters(availability = ServiceFilters.Availability.ALL))
        state = viewModel.uiState.first()
        
        // Should include all services
        assertEquals(4, state.filteredServices.size)
    }
    
    @Test
    fun `filter workflow - age range filters work correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Filter for toddler age range (2-4 years)
        viewModel.updateFilters(ServiceFilters(minAge = 2, maxAge = 4))
        var state = viewModel.uiState.first()
        
        // Should include services that overlap with 2-4 age range
        // - toddlerService (2-4) ✓
        // - specialNeedsService (3-18) ✓
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.any { it.id == "toddler-service" })
        assertTrue(state.filteredServices.any { it.id == "special-needs-service" })
        
        // Filter for school age range (5-12 years)
        viewModel.updateFilters(ServiceFilters(minAge = 5, maxAge = 12))
        state = viewModel.uiState.first()
        
        // Should include services that overlap with 5-12 age range
        // - kidsService (5-12) ✓
        // - specialNeedsService (3-18) ✓
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.any { it.id == "kids-service" })
        assertTrue(state.filteredServices.any { it.id == "special-needs-service" })
        
        // Filter for teen age range (13-17 years)
        viewModel.updateFilters(ServiceFilters(minAge = 13, maxAge = 17))
        state = viewModel.uiState.first()
        
        // Should include services that overlap with 13-17 age range
        // - youthService (13-17) ✓
        // - specialNeedsService (3-18) ✓
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.any { it.id == "youth-service" })
        assertTrue(state.filteredServices.any { it.id == "special-needs-service" })
    }
    
    @Test
    fun `filter workflow - capacity filter works correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Filter to hide full services
        viewModel.updateFilters(ServiceFilters(showFullServices = false))
        val state = viewModel.uiState.first()
        
        // Should exclude kidsService (at capacity)
        assertEquals(3, state.filteredServices.size)
        assertFalse(state.filteredServices.any { it.isAtCapacity() })
        assertTrue(state.filteredServices.all { it.hasAvailableSpots() })
    }
    
    @Test
    fun `filter workflow - complex filter combination works correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Apply complex filter: available services for ages 3-10, excluding full services
        viewModel.updateFilters(ServiceFilters(
            availability = ServiceFilters.Availability.AVAILABLE_ONLY,
            minAge = 3,
            maxAge = 10,
            showFullServices = false
        ))
        val state = viewModel.uiState.first()
        
        // Should only include services that:
        // - Have available spots
        // - Accept ages 3-10
        // - Are not at capacity
        // Expected: toddlerService (2-4, available) and specialNeedsService (3-18, available)
        assertEquals(2, state.filteredServices.size)
        assertTrue(state.filteredServices.all { it.hasAvailableSpots() })
        assertTrue(state.filteredServices.all { !it.isAtCapacity() })
    }
    
    @Test
    fun `error handling workflow - network error recovery`() = runTest {
        // Simulate network error
        mockRepository.servicesResult = Result.failure(Exception("Network timeout"))
        
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        var state = viewModel.uiState.first()
        
        // Verify error state
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network timeout"))
        assertTrue(state.services.isEmpty())
        
        // Simulate network recovery
        mockRepository.servicesResult = Result.success(listOf(toddlerService, kidsService))
        
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.uiState.first()
        
        // Verify recovery
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.services.size)
    }
    
    @Test
    fun `refresh workflow - data updates correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        var state = viewModel.uiState.first()
        assertEquals(4, state.services.size)
        
        // Update mock data (simulate capacity change)
        val updatedKidsService = kidsService.copy(currentCapacity = 20) // No longer full
        mockRepository.servicesResult = Result.success(
            listOf(toddlerService, updatedKidsService, youthService, specialNeedsService)
        )
        
        viewModel.refreshServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.uiState.first()
        
        // Verify data was updated
        assertFalse(state.isRefreshing)
        val updatedService = state.services.find { it.id == "kids-service" }
        assertNotNull(updatedService)
        assertEquals(20, updatedService.currentCapacity)
        assertTrue(updatedService.hasAvailableSpots())
    }
    
    @Test
    fun `ui state properties work correctly`() = runTest {
        // Initial load
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Set child and filters
        viewModel.setSelectedChild(schoolAgeChild)
        viewModel.updateFilters(ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY))
        
        val state = viewModel.uiState.first()
        
        // Test UI state computed properties
        assertTrue(state.hasActiveFilters)
        assertEquals(2, state.eligibleServices.size) // Services eligible for 8-year-old
        assertEquals(2, state.availableServices.size) // Services with available spots
        assertEquals(1, state.fullServices.size) // Only kidsService is full
        assertFalse(state.isOperationInProgress)
    }
    
    // Mock repository implementation for testing
    private class MockKidsRepository : KidsRepository {
        var servicesResult: Result<List<KidsService>> = Result.success(emptyList())
        
        override suspend fun getAvailableServices(): Result<List<KidsService>> = servicesResult
        override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> = servicesResult
        override suspend fun getServiceById(serviceId: String): Result<KidsService> = 
            Result.failure(NotImplementedError())
        override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> = servicesResult
        
        // All other methods return not implemented for this test
        override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = 
            Result.failure(NotImplementedError())
        override suspend fun registerChild(child: Child): Result<Child> = 
            Result.failure(NotImplementedError())
        override suspend fun updateChild(child: Child): Result<Child> = 
            Result.failure(NotImplementedError())
        override suspend fun deleteChild(childId: String): Result<Unit> = 
            Result.failure(NotImplementedError())
        override suspend fun getChildById(childId: String): Result<Child> = 
            Result.failure(NotImplementedError())
        override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
            Result.failure(NotImplementedError())
        override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
            Result.failure(NotImplementedError())
        override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
        override suspend fun getCurrentCheckIns(serviceId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
        override suspend fun getAllCurrentCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
            Result.failure(NotImplementedError())
        override suspend fun getCheckInRecord(recordId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
            Result.failure(NotImplementedError())
        override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.ServiceReport> = 
            Result.failure(NotImplementedError())
        override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport> = 
            Result.failure(NotImplementedError())
        override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {}
        override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) {}
        override suspend fun unsubscribeFromUpdates() {}
    }
}