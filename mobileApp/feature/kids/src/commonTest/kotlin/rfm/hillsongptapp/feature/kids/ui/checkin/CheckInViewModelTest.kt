package rfm.hillsongptapp.feature.kids.ui.checkin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInEligibilityInfo
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for CheckInViewModel
 * Tests UI state management and check-in flow operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockUseCase: MockCheckInChildUseCase
    private lateinit var viewModel: CheckInViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val mockRepository = object : rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository {
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
        mockUseCase = MockCheckInChildUseCase(mockRepository)
        viewModel = CheckInViewModel(mockUseCase)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // Given - fresh ViewModel
        
        // When
        val initialState = viewModel.uiState.first()
        
        // Then
        assertNull(initialState.child)
        assertTrue(initialState.eligibleServices.isEmpty())
        assertNull(initialState.selectedService)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isCheckingIn)
        assertNull(initialState.error)
        assertNull(initialState.checkInError)
        assertFalse(initialState.showConfirmationDialog)
        assertFalse(initialState.checkInSuccess)
    }
    
    @Test
    fun `loadChildAndEligibleServices should update state correctly on success`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(id = childId)
        val service = createTestService()
        val eligibleServiceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(eligibleServiceInfo)
        )
        
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(child, state.child)
        assertEquals(1, state.eligibleServices.size)
        assertEquals(eligibleServiceInfo, state.eligibleServices[0])
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `loadChildAndEligibleServices should handle child not found error`() = runTest {
        // Given
        val childId = "nonexistent_child"
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.ChildNotFound))
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertNull(state.child)
        assertTrue(state.eligibleServices.isEmpty())
        assertFalse(state.isLoading)
        assertEquals("Child not found. Please check the child ID and try again.", state.error)
    }
    
    @Test
    fun `loadChildAndEligibleServices should handle network error`() = runTest {
        // Given
        val childId = "child_1"
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.NetworkError))
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("Network connection error. Please check your connection and try again.", state.error)
    }
    
    @Test
    fun `selectService should update selected service`() = runTest {
        // Given
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        
        // When
        viewModel.selectService(serviceInfo)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(serviceInfo, state.selectedService)
    }
    
    @Test
    fun `showCheckInConfirmation should show dialog when service selected`() = runTest {
        // Given
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        viewModel.selectService(serviceInfo)
        
        // When
        viewModel.showCheckInConfirmation()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showConfirmationDialog)
    }
    
    @Test
    fun `hideCheckInConfirmation should hide dialog`() = runTest {
        // Given
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        
        // When
        viewModel.hideCheckInConfirmation()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showConfirmationDialog)
    }
    
    @Test
    fun `checkInChild should succeed when all conditions met`() = runTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        val checkInRecord = createTestCheckInRecord()
        
        // Set up initial state
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.success(checkInRecord))
        
        viewModel.loadChildAndEligibleServices(child.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        
        // When
        viewModel.checkInChild("Test notes")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingIn)
        assertFalse(state.showConfirmationDialog)
        assertTrue(state.checkInSuccess)
        assertNull(state.checkInError)
    }
    
    @Test
    fun `checkInChild should handle service at capacity error`() = runTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        
        // Set up initial state
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ServiceAtCapacity))
        
        viewModel.loadChildAndEligibleServices(child.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        
        // When
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertEquals("This service is at full capacity. Please try another service or wait for availability.", state.checkInError)
    }
    
    @Test
    fun `checkInChild should handle child already checked in error`() = runTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        
        // Set up initial state
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ChildAlreadyCheckedIn))
        
        viewModel.loadChildAndEligibleServices(child.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        
        // When
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertEquals("This child is already checked into a service. Please check them out first.", state.checkInError)
    }
    
    @Test
    fun `clearError should clear general error`() = runTest {
        // Given
        mockUseCase.setEligibilityResult(Result.failure(KidsManagementError.NetworkError))
        viewModel.loadChildAndEligibleServices("child_1")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error is set
        var state = viewModel.uiState.first()
        assertNotNull(state.error)
        
        // When
        viewModel.clearError()
        
        // Then
        state = viewModel.uiState.first()
        assertNull(state.error)
    }
    
    @Test
    fun `clearCheckInError should clear check-in specific error`() = runTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 5,
            isRecommended = false
        )
        
        // Set up state with check-in error
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(serviceInfo)
        )
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        mockUseCase.setCheckInResult(Result.failure(KidsManagementError.ServiceAtCapacity))
        
        viewModel.loadChildAndEligibleServices(child.id)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(serviceInfo)
        viewModel.showCheckInConfirmation()
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify check-in error is set
        var state = viewModel.uiState.first()
        assertNotNull(state.checkInError)
        
        // When
        viewModel.clearCheckInError()
        
        // Then
        state = viewModel.uiState.first()
        assertNull(state.checkInError)
    }
    
    @Test
    fun `uiState should have correct computed properties`() = runTest {
        // Given
        val child = createTestChild()
        val recommendedService = EligibleServiceInfo(
            service = createTestService(id = "service_1", name = "Recommended"),
            availableSpots = 10,
            isRecommended = true
        )
        val limitedService = EligibleServiceInfo(
            service = createTestService(id = "service_2", name = "Limited"),
            availableSpots = 3,
            isRecommended = false
        )
        
        val eligibilityInfo = CheckInEligibilityInfo(
            child = child,
            eligibleServices = listOf(recommendedService, limitedService)
        )
        mockUseCase.setEligibilityResult(Result.success(eligibilityInfo))
        
        // When
        viewModel.loadChildAndEligibleServices(child.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.hasEligibleServices)
        assertEquals(1, state.recommendedServices.size)
        assertEquals(recommendedService, state.recommendedServices[0])
        assertEquals(1, state.limitedAvailabilityServices.size)
        assertEquals(limitedService, state.limitedAvailabilityServices[0])
    }
    
    // Helper functions
    
    private fun createTestChild(
        id: String = "child_1",
        name: String = "Test Child",
        status: CheckInStatus = CheckInStatus.CHECKED_OUT
    ) = Child(
        id = id,
        parentId = "parent_1",
        name = name,
        dateOfBirth = "2018-01-01",
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
        name: String = "Test Service"
    ) = KidsService(
        id = id,
        name = name,
        description = "Test Description",
        minAge = 3,
        maxAge = 12,
        startTime = "09:00:00",
        endTime = "10:30:00",
        location = "Room 101",
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff_1"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestCheckInRecord() = CheckInRecord(
        id = "record_1",
        childId = "child_1",
        serviceId = "service_1",
        checkInTime = "2025-01-08T09:00:00Z",
        checkOutTime = null,
        checkedInBy = "user_123",
        checkedOutBy = null,
        notes = "Test check-in",
        status = CheckInStatus.CHECKED_IN
    )
    
    /**
     * Mock implementation of CheckInChildUseCase for testing
     */
    private class MockCheckInChildUseCase(
        private val kidsRepository: rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
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