package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.MutableStateFlow
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for CheckInScreen
 * Tests user interactions and UI state display
 */
@OptIn(ExperimentalTestApi::class)
class CheckInScreenTest {
    
    @Test
    fun `should display loading state initially`() = runComposeUiTest {
        // Given
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { it.copy(isLoading = true) }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Loading child information and available services...").assertIsDisplayed()
    }
    
    @Test
    fun `should display error state when child not found`() = runComposeUiTest {
        // Given
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                error = "Child not found. Please check the child ID and try again."
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "nonexistent_child",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Unable to Load Check-In Information").assertIsDisplayed()
        onNodeWithText("Child not found. Please check the child ID and try again.").assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
        onNodeWithText("Dismiss").assertIsDisplayed()
    }
    
    @Test
    fun `should display child information and eligible services`() = runComposeUiTest {
        // Given
        val child = createTestChild(name = "Johnny Smith")
        val service = createTestService(name = "Elementary Service", location = "Room 101")
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo)
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Check In Johnny Smith").assertIsDisplayed()
        onNodeWithText("Johnny Smith").assertIsDisplayed()
        onNodeWithText("Age: 7 years").assertIsDisplayed()
        onNodeWithText("Current Status: Checked Out").assertIsDisplayed()
        onNodeWithText("Available Services").assertIsDisplayed()
        onNodeWithText("Elementary Service").assertIsDisplayed()
        onNodeWithText("Room 101").assertIsDisplayed()
    }
    
    @Test
    fun `should display no services available message when no eligible services`() = runComposeUiTest {
        // Given
        val child = createTestChild(name = "Johnny Smith")
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = emptyList()
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("No Services Available").assertIsDisplayed()
        onNodeWithText("There are currently no services available for Johnny Smith (age 7).").assertIsDisplayed()
    }
    
    @Test
    fun `should enable check-in button when service is selected`() = runComposeUiTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo),
                selectedService = serviceInfo
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Check In to Selected Service").assertIsEnabled()
    }
    
    @Test
    fun `should disable check-in button when no service is selected`() = runComposeUiTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo),
                selectedService = null
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Check In to Selected Service").assertIsNotEnabled()
    }
    
    @Test
    fun `should show checking in state when check-in is in progress`() = runComposeUiTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo),
                selectedService = serviceInfo,
                isCheckingIn = true
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Checking In...").assertIsDisplayed()
    }
    
    @Test
    fun `should call onNavigateBack when back button is clicked`() = runComposeUiTest {
        // Given
        var backButtonClicked = false
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = createTestChild()
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = { backButtonClicked = true },
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        onNodeWithContentDescription("Back").performClick()
        
        // Then
        assertTrue(backButtonClicked)
    }
    
    @Test
    fun `should call onCheckInSuccess when check-in succeeds`() = runComposeUiTest {
        // Given
        var checkInSuccessCalled = false
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = { checkInSuccessCalled = true },
                viewModel = mockViewModel
            )
        }
        
        // Simulate successful check-in
        mockViewModel.updateState { it.copy(checkInSuccess = true) }
        
        // Then
        assertTrue(checkInSuccessCalled)
    }
    
    @Test
    fun `should show confirmation dialog when showConfirmationDialog is true`() = runComposeUiTest {
        // Given
        val child = createTestChild(name = "Johnny Smith")
        val service = createTestService(name = "Elementary Service")
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo),
                selectedService = serviceInfo,
                showConfirmationDialog = true
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Confirm Check-In").assertIsDisplayed()
        onNodeWithText("Please confirm that you want to check in:").assertIsDisplayed()
        onNodeWithText("Johnny Smith").assertIsDisplayed()
        onNodeWithText("Elementary Service").assertIsDisplayed()
    }
    
    @Test
    fun `should show error dialog when checkInError is present`() = runComposeUiTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        val mockUseCase = createMockUseCase()
        val mockViewModel = MockCheckInViewModel(mockUseCase)
        mockViewModel.updateState { 
            it.copy(
                isLoading = false,
                child = child,
                eligibleServices = listOf(serviceInfo),
                selectedService = serviceInfo,
                checkInError = "This service is at full capacity."
            )
        }
        
        // When
        setContent {
            CheckInScreen(
                childId = "child_1",
                onNavigateBack = {},
                onCheckInSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        // Then
        onNodeWithText("Check-In Failed").assertIsDisplayed()
        onNodeWithText("This service is at full capacity.").assertIsDisplayed()
    }
    
    // Helper functions
    
    private fun createMockUseCase(): CheckInChildUseCase {
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
        return CheckInChildUseCase(mockRepository)
    }
    
    private fun createTestChild(
        id: String = "child_1",
        name: String = "Test Child",
        status: CheckInStatus = CheckInStatus.CHECKED_OUT
    ) = Child(
        id = id,
        parentId = "parent_1",
        name = name,
        dateOfBirth = "2018-01-01", // 7 years old
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
        location: String = "Room 101"
    ) = KidsService(
        id = id,
        name = name,
        description = "Test Description",
        minAge = 3,
        maxAge = 12,
        startTime = "09:00:00",
        endTime = "10:30:00",
        location = location,
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff_1"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    /**
     * Mock CheckInViewModel for testing UI interactions
     */
    private class MockCheckInViewModel(
        checkInChildUseCase: CheckInChildUseCase
    ) : CheckInViewModel(checkInChildUseCase) {
        private val _uiState = MutableStateFlow(CheckInUiState())
        override val uiState = _uiState
        
        fun updateState(update: (CheckInUiState) -> CheckInUiState) {
            _uiState.value = update(_uiState.value)
        }
        
        override fun loadChildAndEligibleServices(childId: String) {
            // Mock implementation - state updates handled by test
        }
        
        override fun selectService(serviceInfo: EligibleServiceInfo) {
            _uiState.value = _uiState.value.copy(selectedService = serviceInfo)
        }
        
        override fun showCheckInConfirmation() {
            _uiState.value = _uiState.value.copy(showConfirmationDialog = true)
        }
        
        override fun hideCheckInConfirmation() {
            _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
        }
        
        override fun checkInChild(notes: String?) {
            _uiState.value = _uiState.value.copy(isCheckingIn = true)
        }
        
        override fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }
        
        override fun clearCheckInError() {
            _uiState.value = _uiState.value.copy(checkInError = null)
        }
    }
}