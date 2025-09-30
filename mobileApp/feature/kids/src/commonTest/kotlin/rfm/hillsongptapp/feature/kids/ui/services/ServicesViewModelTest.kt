package rfm.hillsongptapp.feature.kids.ui.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.feature.kids.ui.MockKidsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for ServicesViewModel
 * Tests service loading, filtering, and UI state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val mockKidsRepository = MockKidsRepository()
    
    private lateinit var viewModel: ServicesViewModel
    
    // Test data
    private val testEmergencyContact = EmergencyContact(
        name = "Jane Doe",
        phoneNumber = "+1234567890",
        relationship = "Mother"
    )
    
    private val testChild = Child(
        id = "child-1",
        parentId = "parent-1",
        name = "John Doe",
        dateOfBirth = "2015-05-15", // 8-9 years old
        medicalInfo = "No allergies",
        dietaryRestrictions = "None",
        emergencyContact = testEmergencyContact,
        status = CheckInStatus.CHECKED_OUT,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2024-01-01T10:00:00Z",
        updatedAt = "2024-01-01T10:00:00Z"
    )
    
    private val testServices = listOf(
        KidsService(
            id = "service-1",
            name = "Kids Church",
            description = "Sunday kids service",
            minAge = 5,
            maxAge = 12,
            startTime = "2024-01-07T10:00:00Z",
            endTime = "2024-01-07T11:30:00Z",
            location = "Kids Room A",
            maxCapacity = 20,
            currentCapacity = 5,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-1", "staff-2"),
            createdAt = "2024-01-01T09:00:00Z"
        ),
        KidsService(
            id = "service-2",
            name = "Toddler Time",
            description = "Service for toddlers",
            minAge = 2,
            maxAge = 4,
            startTime = "2024-01-07T10:00:00Z",
            endTime = "2024-01-07T11:00:00Z",
            location = "Toddler Room",
            maxCapacity = 15,
            currentCapacity = 15, // At capacity
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-3"),
            createdAt = "2024-01-01T09:00:00Z"
        ),
        KidsService(
            id = "service-3",
            name = "Youth Group",
            description = "Service for teenagers",
            minAge = 13,
            maxAge = 18,
            startTime = "2024-01-07T11:00:00Z",
            endTime = "2024-01-07T12:30:00Z",
            location = "Youth Room",
            maxCapacity = 25,
            currentCapacity = 10,
            isAcceptingCheckIns = false, // Not accepting check-ins
            staffMembers = listOf("staff-4", "staff-5"),
            createdAt = "2024-01-01T09:00:00Z"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockKidsRepository.getAvailableServicesResult = KidsResult.Success(testServices)
        viewModel = ServicesViewModel(mockKidsRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state loads services automatically`() = runTest {
        // When
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.services.size)
        assertEquals(3, state.filteredServices.size) // No filters applied initially
        assertNull(state.error)
        assertNull(state.selectedChild)
    }
    
    @Test
    fun `loadServices handles success result`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.Success(testServices)
        
        // When
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.services.size)
        assertEquals("Kids Church", state.services[0].name)
        assertNull(state.error)
    }
    
    @Test
    fun `loadServices handles error result`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.Error("Failed to load services")
        
        // When
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.services.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load services"))
    }
    
    @Test
    fun `loadServices handles network error result`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.NetworkError("Network connection failed")
        
        // When
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.services.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error loading services"))
    }
    
    @Test
    fun `refreshServices updates isRefreshing state`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When
        viewModel.refreshServices()
        
        // Then (before completion)
        assertTrue(viewModel.uiState.value.isRefreshing)
        
        // When (after completion)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(3, state.services.size)
    }
    
    @Test
    fun `setSelectedChild updates state and filters services`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When
        viewModel.setSelectedChild(testChild)
        
        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.selectedChild)
        assertEquals(testChild.id, state.selectedChild?.id)
        
        // Services should be sorted with eligible services first
        val eligibleServices = state.filteredServices.filter { service ->
            service.isAgeEligible(testChild.calculateAge())
        }
        assertTrue(eligibleServices.isNotEmpty())
    }
    
    @Test
    fun `updateFilters applies availability filter correctly`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When - Filter to only available services
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        viewModel.updateFilters(filters)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(filters, state.filters)
        
        // Should exclude the service at capacity (service-2)
        val availableServices = state.filteredServices
        assertTrue(availableServices.all { it.hasAvailableSpots() })
        assertFalse(availableServices.any { it.id == "service-2" })
    }
    
    @Test
    fun `updateFilters applies accepting check-ins filter correctly`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When - Filter to only services accepting check-ins
        val filters = ServiceFilters(availability = ServiceFilters.Availability.ACCEPTING_CHECKINS)
        viewModel.updateFilters(filters)
        
        // Then
        val state = viewModel.uiState.value
        val acceptingServices = state.filteredServices
        assertTrue(acceptingServices.all { it.canAcceptCheckIn() })
        
        // Should exclude service-3 (not accepting check-ins) and service-2 (at capacity)
        assertFalse(acceptingServices.any { it.id == "service-3" })
        assertFalse(acceptingServices.any { it.id == "service-2" })
    }
    
    @Test
    fun `updateFilters applies age range filter correctly`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When - Filter to services for ages 5-10
        val filters = ServiceFilters(minAge = 5, maxAge = 10)
        viewModel.updateFilters(filters)
        
        // Then
        val state = viewModel.uiState.value
        val ageFilteredServices = state.filteredServices
        
        // Should include service-1 (5-12 age range overlaps with 5-10)
        // Should exclude service-2 (2-4 age range) and service-3 (13-18 age range)
        assertTrue(ageFilteredServices.any { it.id == "service-1" })
        assertFalse(ageFilteredServices.any { it.id == "service-2" })
        assertFalse(ageFilteredServices.any { it.id == "service-3" })
    }
    
    @Test
    fun `updateFilters applies showFullServices filter correctly`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When - Hide full services
        val filters = ServiceFilters(showFullServices = false)
        viewModel.updateFilters(filters)
        
        // Then
        val state = viewModel.uiState.value
        val nonFullServices = state.filteredServices
        
        // Should exclude service-2 which is at capacity
        assertTrue(nonFullServices.all { !it.isAtCapacity() })
        assertFalse(nonFullServices.any { it.id == "service-2" })
    }
    
    @Test
    fun `clearFilters resets to default state`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // Apply some filters first
        val filters = ServiceFilters(
            availability = ServiceFilters.Availability.AVAILABLE_ONLY,
            minAge = 5,
            showFullServices = false
        )
        viewModel.updateFilters(filters)
        
        // When
        viewModel.clearFilters()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(ServiceFilters(), state.filters)
        assertEquals(3, state.filteredServices.size) // All services should be shown again
        assertFalse(state.hasActiveFilters)
    }
    
    @Test
    fun `getServicesForChild returns eligible services`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When
        val eligibleServices = viewModel.getServicesForChild(testChild)
        
        // Then
        // testChild is 8-9 years old, so should be eligible for service-1 (5-12 age range)
        // and service-1 can accept check-ins
        assertEquals(1, eligibleServices.size)
        assertEquals("service-1", eligibleServices[0].id)
    }
    
    @Test
    fun `clearError removes error message`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.Error("Test error")
        viewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `UI state computed properties work correctly`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle() // Complete initial load
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertEquals(2, state.availableServices.size) // Services with available spots
        assertEquals(1, state.fullServices.size) // Services at capacity
        
        // Test with selected child
        viewModel.setSelectedChild(testChild)
        val stateWithChild = viewModel.uiState.value
        assertEquals(1, stateWithChild.eligibleServices.size) // Services eligible for child
        
        // Test with active filters
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        viewModel.updateFilters(filters)
        val stateWithFilters = viewModel.uiState.value
        assertTrue(stateWithFilters.hasActiveFilters)
    }
    
    @Test
    fun `isOperationInProgress reflects loading and refreshing states`() = runTest {
        // Test loading state
        mockKidsRepository.getAvailableServicesResult = KidsResult.Success(testServices)
        viewModel.loadServices()
        assertTrue(viewModel.uiState.value.isOperationInProgress)
        
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isOperationInProgress)
        
        // Test refreshing state
        viewModel.refreshServices()
        assertTrue(viewModel.uiState.value.isOperationInProgress)
        
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isOperationInProgress)
    }
}