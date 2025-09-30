package rfm.hillsongptapp.feature.kids.ui.checkin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for CheckInViewModel
 * Tests UI state management, error handling, and check-in operations
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val mockKidsRepository = MockKidsRepository()
    private val mockAuthRepository = MockAuthRepository()
    
    private lateinit var viewModel: CheckInViewModel
    
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
        dateOfBirth = "2015-05-15",
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
    
    private val testService = KidsService(
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
    )
    
    private val testCheckInRecord = CheckInRecord(
        id = "record-1",
        childId = "child-1",
        serviceId = "service-1",
        checkInTime = "2024-01-07T10:00:00Z",
        checkOutTime = null,
        checkedInBy = "staff-1",
        checkedOutBy = null,
        notes = "Check-in successful",
        status = CheckInStatus.CHECKED_IN
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CheckInViewModel(mockKidsRepository, mockAuthRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is correct`() {
        val initialState = viewModel.uiState.value
        
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
    fun `loadChildAndEligibleServices succeeds with valid child and services`() = runTest {
        // Given
        val childId = "child-1"
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.child)
        assertEquals(testChild.id, state.child?.id)
        assertEquals(1, state.eligibleServices.size)
        assertEquals(testService.id, state.eligibleServices[0].id)
        assertNull(state.error)
    }
    
    @Test
    fun `loadChildAndEligibleServices handles child not found error`() = runTest {
        // Given
        val childId = "nonexistent-child"
        mockKidsRepository.getChildByIdResult = KidsResult.Error("Child not found")
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.child)
        assertTrue(state.eligibleServices.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load child"))
    }
    
    @Test
    fun `loadChildAndEligibleServices handles network error`() = runTest {
        // Given
        val childId = "child-1"
        mockKidsRepository.getChildByIdResult = KidsResult.NetworkError("Network connection failed")
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.child)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error loading child"))
    }
    
    @Test
    fun `loadChildAndEligibleServices filters ineligible services`() = runTest {
        // Given
        val childId = "child-1"
        val youngChild = testChild.copy(dateOfBirth = "2020-01-01") // Too young for service
        val ineligibleService = testService.copy(minAge = 8, maxAge = 15) // Child is too young
        
        mockKidsRepository.getChildByIdResult = KidsResult.Success(youngChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(ineligibleService))
        
        // When
        viewModel.loadChildAndEligibleServices(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.child)
        assertTrue(state.eligibleServices.isEmpty()) // Service should be filtered out
        assertNull(state.error)
    }
    
    @Test
    fun `selectService updates selected service in state`() {
        // When
        viewModel.selectService(testService)
        
        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.selectedService)
        assertEquals(testService.id, state.selectedService?.id)
    }
    
    @Test
    fun `showCheckInConfirmation shows dialog when service is selected`() {
        // Given
        viewModel.selectService(testService)
        
        // When
        viewModel.showCheckInConfirmation()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showConfirmationDialog)
    }
    
    @Test
    fun `hideCheckInConfirmation hides dialog`() {
        // Given
        viewModel.selectService(testService)
        viewModel.showCheckInConfirmation()
        
        // When
        viewModel.hideCheckInConfirmation()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showConfirmationDialog)
    }
    
    @Test
    fun `checkInChild succeeds with valid child and service`() = runTest {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        mockKidsRepository.checkInChildResult = KidsResult.Success(testCheckInRecord)
        
        viewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(testService)
        
        // When
        viewModel.checkInChild("Test notes")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.showConfirmationDialog)
        assertTrue(state.checkInSuccess)
        assertNull(state.checkInError)
    }
    
    @Test
    fun `checkInChild handles repository error`() = runTest {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        mockKidsRepository.checkInChildResult = KidsResult.Error("Service is at full capacity")
        
        viewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(testService)
        
        // When
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertNotNull(state.checkInError)
        assertTrue(state.checkInError!!.contains("Service is at full capacity"))
    }
    
    @Test
    fun `checkInChild handles network error`() = runTest {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        mockKidsRepository.checkInChildResult = KidsResult.NetworkError("Network connection failed")
        
        viewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(testService)
        
        // When
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingIn)
        assertFalse(state.checkInSuccess)
        assertNotNull(state.checkInError)
        assertTrue(state.checkInError!!.contains("Network error"))
    }
    
    @Test
    fun `clearError clears general error`() {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Error("Test error")
        viewModel.loadChildAndEligibleServices("child-1")
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.error)
    }
    
    @Test
    fun `clearCheckInError clears check-in specific error`() = runTest {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        mockKidsRepository.checkInChildResult = KidsResult.Error("Test check-in error")
        
        viewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(testService)
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearCheckInError()
        
        // Then
        val state = viewModel.uiState.value
        assertNull(state.checkInError)
    }
    
    @Test
    fun `resetSuccessState clears success flag`() = runTest {
        // Given
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(listOf(testService))
        mockKidsRepository.checkInChildResult = KidsResult.Success(testCheckInRecord)
        
        viewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectService(testService)
        viewModel.checkInChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.resetSuccessState()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.checkInSuccess)
    }
    
    @Test
    fun `UI state computed properties work correctly`() {
        val stateWithServices = CheckInUiState(
            eligibleServices = listOf(
                testService.copy(currentCapacity = 5), // 15 spots available
                testService.copy(id = "service-2", currentCapacity = 18) // 2 spots available
            )
        )
        
        assertTrue(stateWithServices.hasEligibleServices)
        assertEquals(1, stateWithServices.recommendedServices.size) // Services with >5 spots
        assertEquals(1, stateWithServices.limitedAvailabilityServices.size) // Services with 1-5 spots
        
        val stateWithLoading = CheckInUiState(isLoading = true)
        assertTrue(stateWithLoading.isOperationInProgress)
        
        val stateWithCheckingIn = CheckInUiState(isCheckingIn = true)
        assertTrue(stateWithCheckingIn.isOperationInProgress)
    }
}