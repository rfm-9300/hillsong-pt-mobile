package rfm.hillsongptapp.feature.kids.ui.services

import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import kotlin.test.*

class ServicesUiStateTest {
    
    private val availableService = KidsService(
        id = "service-1",
        name = "Available Service",
        description = "Service with available spots",
        minAge = 5,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room A",
        maxCapacity = 20,
        currentCapacity = 15,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val fullService = KidsService(
        id = "service-2",
        name = "Full Service",
        description = "Service at capacity",
        minAge = 3,
        maxAge = 8,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room B",
        maxCapacity = 15,
        currentCapacity = 15,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-2"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val closedService = KidsService(
        id = "service-3",
        name = "Closed Service",
        description = "Service not accepting check-ins",
        minAge = 10,
        maxAge = 16,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room C",
        maxCapacity = 25,
        currentCapacity = 10,
        isAcceptingCheckIns = false,
        staffMembers = listOf("staff-3"),
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
    
    @Test
    fun `default state has correct initial values`() {
        val state = ServicesUiState()
        
        assertTrue(state.services.isEmpty())
        assertTrue(state.filteredServices.isEmpty())
        assertEquals(ServiceFilters(), state.filters)
        assertNull(state.selectedChild)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
    }
    
    @Test
    fun `hasActiveFilters returns correct value`() {
        val stateWithoutFilters = ServicesUiState()
        val stateWithFilters = ServicesUiState(
            filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        )
        
        assertFalse(stateWithoutFilters.hasActiveFilters)
        assertTrue(stateWithFilters.hasActiveFilters)
    }
    
    @Test
    fun `eligibleServices returns all services when no child selected`() {
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService)
        )
        
        assertEquals(3, state.eligibleServices.size)
        assertEquals(state.services, state.eligibleServices)
    }
    
    @Test
    fun `eligibleServices filters by child age when child selected`() {
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService),
            selectedChild = mockChild // 7 years old
        )
        
        // Child is 7 years old, eligible for:
        // - availableService (5-12 years) ✓
        // - fullService (3-8 years) ✓
        // - closedService (10-16 years) ✗
        assertEquals(2, state.eligibleServices.size)
        assertTrue(state.eligibleServices.any { it.id == "service-1" })
        assertTrue(state.eligibleServices.any { it.id == "service-2" })
        assertFalse(state.eligibleServices.any { it.id == "service-3" })
    }
    
    @Test
    fun `availableServices returns only services that can accept check-ins`() {
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService)
        )
        
        // Only availableService can accept check-ins (has spots and is accepting)
        assertEquals(1, state.availableServices.size)
        assertEquals("service-1", state.availableServices[0].id)
    }
    
    @Test
    fun `fullServices returns only services at capacity`() {
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService)
        )
        
        // Only fullService is at capacity
        assertEquals(1, state.fullServices.size)
        assertEquals("service-2", state.fullServices[0].id)
    }
    
    @Test
    fun `isOperationInProgress returns true when loading`() {
        val loadingState = ServicesUiState(isLoading = true)
        val refreshingState = ServicesUiState(isRefreshing = true)
        val bothState = ServicesUiState(isLoading = true, isRefreshing = true)
        val neitherState = ServicesUiState()
        
        assertTrue(loadingState.isOperationInProgress)
        assertTrue(refreshingState.isOperationInProgress)
        assertTrue(bothState.isOperationInProgress)
        assertFalse(neitherState.isOperationInProgress)
    }
    
    @Test
    fun `state with error maintains other properties`() {
        val state = ServicesUiState(
            services = listOf(availableService),
            filteredServices = listOf(availableService),
            selectedChild = mockChild,
            isLoading = false,
            error = "Network error"
        )
        
        assertEquals(1, state.services.size)
        assertEquals(1, state.filteredServices.size)
        assertEquals(mockChild, state.selectedChild)
        assertFalse(state.isLoading)
        assertEquals("Network error", state.error)
    }
    
    @Test
    fun `state with filters applied correctly`() {
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService),
            filteredServices = listOf(availableService), // Only available service matches filter
            filters = filters
        )
        
        assertEquals(3, state.services.size) // All services stored
        assertEquals(1, state.filteredServices.size) // Only filtered services shown
        assertEquals(filters, state.filters)
        assertTrue(state.hasActiveFilters)
    }
    
    @Test
    fun `eligibleServices with edge case ages`() {
        // Child exactly at min age boundary
        val youngChild = mockChild.copy(dateOfBirth = "2022-01-01") // 3 years old
        val stateWithYoungChild = ServicesUiState(
            services = listOf(availableService, fullService, closedService),
            selectedChild = youngChild
        )
        
        // 3 years old is eligible for fullService (3-8) only
        assertEquals(1, stateWithYoungChild.eligibleServices.size)
        assertEquals("service-2", stateWithYoungChild.eligibleServices[0].id)
        
        // Child exactly at max age boundary
        val oldChild = mockChild.copy(dateOfBirth = "2009-01-01") // 16 years old
        val stateWithOldChild = ServicesUiState(
            services = listOf(availableService, fullService, closedService),
            selectedChild = oldChild
        )
        
        // 16 years old is eligible for closedService (10-16) only
        assertEquals(1, stateWithOldChild.eligibleServices.size)
        assertEquals("service-3", stateWithOldChild.eligibleServices[0].id)
    }
    
    @Test
    fun `availableServices excludes services not accepting check-ins`() {
        val notAcceptingService = availableService.copy(
            id = "service-4",
            isAcceptingCheckIns = false,
            currentCapacity = 5 // Has spots but not accepting
        )
        
        val state = ServicesUiState(
            services = listOf(availableService, fullService, closedService, notAcceptingService)
        )
        
        // Only availableService should be in availableServices
        assertEquals(1, state.availableServices.size)
        assertEquals("service-1", state.availableServices[0].id)
    }
}