package rfm.hillsongptapp.feature.kids.ui.checkin

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInEligibilityInfo
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests for the complete check-in flow
 * Tests end-to-end scenarios from loading child information to successful check-in
 */
class CheckInIntegrationTest {
    
    @Test
    fun `complete check-in flow should succeed with valid child and service`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId, name = "Johnny Smith")
        val service = createTestService(
            id = "service_1",
            name = "Elementary Service",
            currentCapacity = 5,
            maxCapacity = 20
        )
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 15,
            isRecommended = true
        )
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        val checkInRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = service.id
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.success(checkInRecord))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When - Load child and services
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then - Child and services should be loaded
        var state = viewModel.uiState.value
        assertEquals(child, state.child)
        assertEquals(1, state.eligibleServices.size)
        assertEquals(serviceInfo, state.eligibleServices[0])
        assertFalse(state.isLoading)
        assertNull(state.error)
        
        // When - Select service
        viewModel.selectService(serviceInfo)
        
        // Then - Service should be selected
        state = viewModel.uiState.value
        assertEquals(serviceInfo, state.selectedService)
        
        // When - Show confirmation dialog
        viewModel.showCheckInConfirmation()
        
        // Then - Dialog should be shown
        state = viewModel.uiState.value
        assertTrue(state.showConfirmationDialog)
        
        // When - Confirm check-in
        viewModel.checkInChild("Test notes")
        
        // Then - Check-in should succeed
        state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.showConfirmationDialog)
        assertTrue(state.checkInSuccess)
        assertNull(state.checkInError)
    }
    
    @Test
    fun `check-in flow should handle child not found error gracefully`() = runTest {
        // Given
        val childId = "nonexistent_child"
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.ChildNotFound))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.child)
        assertTrue(state.eligibleServices.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("Child not found. Please check the child ID and try again.", state.error)
    }
    
    @Test
    fun `check-in flow should handle service at capacity error during check-in`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId)
        val service = createTestService(id = "service_1")
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 1,
            isRecommended = false
        )
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ServiceAtCapacity))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When - Load and select service
        viewModel.loadChildAndEligibleServices(childId)
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        viewModel.checkInChild()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertEquals("This service is at full capacity. Please try another service or wait for availability.", state.checkInError)
    }
    
    @Test
    fun `check-in flow should handle child already checked in error`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId)
        val service = createTestService(id = "service_1")
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ChildAlreadyCheckedIn))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        viewModel.checkInChild()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertEquals("This child is already checked into a service. Please check them out first.", state.checkInError)
    }
    
    @Test
    fun `check-in flow should handle network error during loading`() = runTest {
        // Given
        val childId = "child_1"
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.NetworkError))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.child)
        assertTrue(state.eligibleServices.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("Network connection error. Please check your connection and try again.", state.error)
    }
    
    @Test
    fun `check-in flow should handle no eligible services scenario`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId, dateOfBirth = "2023-01-01") // Too young
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = emptyList()
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(child, state.child)
        assertTrue(state.eligibleServices.isEmpty())
        assertFalse(state.hasEligibleServices)
        assertTrue(state.recommendedServices.isEmpty())
        assertTrue(state.limitedAvailabilityServices.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `check-in flow should categorize services correctly`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId)
        
        val recommendedService = createTestService(
            id = "service_1",
            name = "Recommended Service",
            currentCapacity = 5,
            maxCapacity = 20 // 15 spots available
        )
        val limitedService = createTestService(
            id = "service_2",
            name = "Limited Service",
            currentCapacity = 18,
            maxCapacity = 20 // 2 spots available
        )
        
        val recommendedServiceInfo = EligibleServiceInfo(
            service = recommendedService,
            availableSpots = 15,
            isRecommended = true
        )
        val limitedServiceInfo = EligibleServiceInfo(
            service = limitedService,
            availableSpots = 2,
            isRecommended = false
        )
        
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(recommendedServiceInfo, limitedServiceInfo)
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.eligibleServices.size)
        assertTrue(state.hasEligibleServices)
        
        assertEquals(1, state.recommendedServices.size)
        assertEquals(recommendedServiceInfo, state.recommendedServices[0])
        
        assertEquals(1, state.limitedAvailabilityServices.size)
        assertEquals(limitedServiceInfo, state.limitedAvailabilityServices[0])
    }
    
    @Test
    fun `error clearing should work correctly`() = runTest {
        // Given
        val childId = "child_1"
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.NetworkError))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When - Load with error
        viewModel.loadChildAndEligibleServices(childId)
        
        // Then - Error should be present
        var state = viewModel.uiState.value
        assertNotNull(state.error)
        
        // When - Clear error
        viewModel.clearError()
        
        // Then - Error should be cleared
        state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `check-in error clearing should work correctly`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId)
        val service = createTestService(id = "service_1")
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        
        val mockRepository = createMockRepository()
        val mockUseCase = MockCheckInChildUseCase(mockRepository)
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ServiceAtCapacity))
        
        val viewModel = CheckInViewModel(mockUseCase)
        
        // When - Perform check-in with error
        viewModel.loadChildAndEligibleServices(childId)
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        viewModel.checkInChild()
        
        // Then - Check-in error should be present
        var state = viewModel.uiState.value
        assertNotNull(state.checkInError)
        
        // When - Clear check-in error
        viewModel.clearCheckInError()
        
        // Then - Check-in error should be cleared
        state = viewModel.uiState.value
        assertNull(state.checkInError)
    }
    
    // Helper functions
    
    private fun createMockRepository(): rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository {
        return object : rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository {
            override suspend fun getChildById(childId: String) = Result.failure<Child>(KidsManagementError.ChildNotFound)
            override suspend fun getServiceById(serviceId: String) = Result.failure<rfm.hillsongptapp.feature.kids.domain.model.KidsService>(KidsManagementError.ServiceNotFound)
            override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?) = Result.failure<CheckInRecord>(KidsManagementError.UnknownError("Mock"))
            override suspend fun getServicesAcceptingCheckIns() = Result.success(emptyList<rfm.hillsongptapp.feature.kids.domain.model.KidsService>())
            override suspend fun getChildrenForParent(parentId: String) = TODO()
            override suspend fun registerChild(child: Child) = TODO()
            override suspend fun updateChild(child: Child) = TODO()
            override suspend fun deleteChild(childId: String) = TODO()
            override suspend fun getAvailableServices() = TODO()
            override suspend fun getServicesForAge(age: Int) = TODO()
            override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?) = TODO()
            override suspend fun getCheckInHistory(childId: String, limit: Int?) = TODO()
            override suspend fun getCurrentCheckIns(serviceId: String) = TODO()
            override suspend fun getAllCurrentCheckIns() = TODO()
            override suspend fun getCheckInRecord(recordId: String) = TODO()
            override suspend fun getServiceReport(serviceId: String) = TODO()
            override suspend fun getAttendanceReport(startDate: String, endDate: String) = TODO()
            override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) = TODO()
            override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.KidsService) -> Unit) = TODO()
            override suspend fun unsubscribeFromUpdates() = TODO()
        }
    }
    
    private fun createTestChild(
        id: String = "child_1",
        name: String = "Test Child",
        dateOfBirth: String = "2018-01-01",
        status: CheckInStatus = CheckInStatus.CHECKED_OUT
    ) = Child(
        id = id,
        parentId = "parent_1",
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = status,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestService(
        id: String = "service_1",
        name: String = "Test Service",
        currentCapacity: Int = 10,
        maxCapacity: Int = 20
    ) = KidsService(
        id = id,
        name = name,
        description = "Test Description",
        minAge = 3,
        maxAge = 12,
        startTime = "09:00:00",
        endTime = "10:30:00",
        location = "Room 101",
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff_1"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestCheckInRecord(
        childId: String = "child_1",
        serviceId: String = "service_1"
    ) = CheckInRecord(
        id = "record_1",
        childId = childId,
        serviceId = serviceId,
        checkInTime = "2025-01-08T09:00:00Z",
        checkOutTime = null,
        checkedInBy = "user_123",
        checkedOutBy = null,
        notes = "Test notes",
        status = CheckInStatus.CHECKED_IN
    )
    
    /**
     * Mock implementation of CheckInChildUseCase for integration testing
     */
    private class MockCheckInChildUseCase(
        kidsRepository: rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
    ) : CheckInChildUseCase(kidsRepository) {
        private var eligibilityResult: Result<CheckInEligibilityInfo>? = null
        private var checkInResult: Result<CheckInRecord>? = null
        
        fun setEligibilityResult(result: Result<CheckInEligibilityInfo>) {
            eligibilityResult = result
        }
        
        fun setCheckInResult(result: Result<CheckInRecord>) {
            checkInResult = result
        }
        
        override suspend fun getEligibleServicesForChild(childId: String): Result<CheckInEligibilityInfo> {
            return eligibilityResult ?: Result.failure(KidsManagementError.UnknownError("Not configured"))
        }
        
        override suspend fun execute(
            childId: String,
            serviceId: String,
            checkedInBy: String,
            notes: String?
        ): Result<CheckInRecord> {
            return checkInResult ?: Result.failure(KidsManagementError.UnknownError("Not configured"))
        }
    }
}