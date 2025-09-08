package rfm.hillsongptapp.feature.kids.ui.checkout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutEligibilityInfo
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutResult
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CheckOutViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var mockUseCase: MockCheckOutChildUseCase
    private lateinit var viewModel: CheckOutViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        mockUseCase = MockCheckOutChildUseCase()
        viewModel = CheckOutViewModel(mockRepository, mockUseCase)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        // When
        val initialState = viewModel.uiState.first()
        
        // Then
        assertNull(initialState.childId)
        assertNull(initialState.child)
        assertNull(initialState.currentService)
        assertNull(initialState.eligibilityInfo)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isCheckingOut)
        assertNull(initialState.error)
        assertFalse(initialState.showParentVerification)
        assertFalse(initialState.showCheckOutConfirmation)
        assertFalse(initialState.showSuccessDialog)
        assertFalse(initialState.showErrorDialog)
        assertNull(initialState.checkOutResult)
        assertNull(initialState.checkOutError)
    }
    
    @Test
    fun `loadChild should successfully load child and service information`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        val service = createTestService(id = "service123")
        val eligibilityInfo = CheckOutEligibilityInfo(
            child = child,
            canCheckOut = true,
            currentService = service,
            checkInTime = child.checkInTime
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        mockUseCase.setEligibilityInfo(eligibilityInfo)
        
        // When
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(childId, state.childId)
        assertEquals(child, state.child)
        assertEquals(service, state.currentService)
        assertEquals(eligibilityInfo, state.eligibilityInfo)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `loadChild should handle child not found error`() = runTest {
        // Given
        val childId = "nonexistent"
        mockRepository.setChildResult(Result.failure(KidsManagementError.ChildNotFound))
        
        // When
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(childId, state.childId)
        assertNull(state.child)
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Child not found"))
    }
    
    @Test
    fun `loadChild should handle service not found gracefully`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "nonexistent_service"
        )
        val eligibilityInfo = CheckOutEligibilityInfo(
            child = child,
            canCheckOut = true,
            currentService = null,
            checkInTime = child.checkInTime
        )
        
        mockRepository.setChild(child)
        mockRepository.setServiceResult(Result.failure(KidsManagementError.ServiceNotFound))
        mockUseCase.setEligibilityInfo(eligibilityInfo)
        
        // When
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(child, state.child)
        assertNull(state.currentService) // Service should be null when not found
        assertEquals(eligibilityInfo, state.eligibilityInfo)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `startCheckOutProcess should show parent verification dialog`() = runTest {
        // When
        viewModel.startCheckOutProcess()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showParentVerification)
    }
    
    @Test
    fun `onParentVerified should hide verification and show confirmation dialog`() = runTest {
        // Given
        viewModel.startCheckOutProcess()
        
        // When
        viewModel.onParentVerified()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showParentVerification)
        assertTrue(state.showCheckOutConfirmation)
    }
    
    @Test
    fun `confirmCheckOut should successfully check out child`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(id = childId)
        val service = createTestService()
        val record = createTestCheckInRecord(childId = childId)
        val checkOutResult = CheckOutResult(record, child, service)
        
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        mockUseCase.setCheckOutResult(checkOutResult)
        
        // When
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingOut)
        assertEquals(checkOutResult, state.checkOutResult)
        assertTrue(state.showSuccessDialog)
        assertFalse(state.showErrorDialog)
        assertNull(state.checkOutError)
    }
    
    @Test
    fun `confirmCheckOut should handle check-out failure`() = runTest {
        // Given
        val childId = "child123"
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        mockUseCase.setCheckOutError(KidsManagementError.ChildNotCheckedIn)
        
        // When
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingOut)
        assertNull(state.checkOutResult)
        assertFalse(state.showSuccessDialog)
        assertTrue(state.showErrorDialog)
        assertEquals("Child is not currently checked into any service", state.checkOutError)
    }
    
    @Test
    fun `confirmCheckOut should handle null child ID`() = runTest {
        // Given - no child loaded
        
        // When
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isCheckingOut)
        assertFalse(state.showSuccessDialog)
        assertFalse(state.showErrorDialog)
        // Should not attempt check-out when child ID is null
    }
    
    @Test
    fun `hideParentVerification should hide verification dialog`() = runTest {
        // Given
        viewModel.startCheckOutProcess()
        
        // When
        viewModel.hideParentVerification()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showParentVerification)
    }
    
    @Test
    fun `hideCheckOutConfirmation should hide confirmation dialog`() = runTest {
        // Given
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        
        // When
        viewModel.hideCheckOutConfirmation()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showCheckOutConfirmation)
    }
    
    @Test
    fun `hideSuccessDialog should hide success dialog and clear result`() = runTest {
        // Given
        val checkOutResult = CheckOutResult(
            createTestCheckInRecord(),
            createTestChild(),
            createTestService()
        )
        // Simulate successful check-out
        viewModel.loadChild("child123")
        testDispatcher.scheduler.advanceUntilIdle()
        mockUseCase.setCheckOutResult(checkOutResult)
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.hideSuccessDialog()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showSuccessDialog)
        assertNull(state.checkOutResult)
    }
    
    @Test
    fun `hideErrorDialog should hide error dialog and clear error`() = runTest {
        // Given
        viewModel.loadChild("child123")
        testDispatcher.scheduler.advanceUntilIdle()
        mockUseCase.setCheckOutError(KidsManagementError.NetworkError)
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.hideErrorDialog()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showErrorDialog)
        assertNull(state.checkOutError)
    }
    
    @Test
    fun `retryCheckOut should hide error dialog and retry check-out`() = runTest {
        // Given
        val childId = "child123"
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // First attempt fails
        mockUseCase.setCheckOutError(KidsManagementError.NetworkError)
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Setup successful retry
        val checkOutResult = CheckOutResult(
            createTestCheckInRecord(),
            createTestChild(),
            createTestService()
        )
        mockUseCase.setCheckOutResult(checkOutResult)
        
        // When
        viewModel.retryCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.showErrorDialog)
        assertNull(state.checkOutError)
        assertTrue(state.showSuccessDialog)
        assertEquals(checkOutResult, state.checkOutResult)
    }
    
    @Test
    fun `clearError should clear general error message`() = runTest {
        // Given
        viewModel.loadChild("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
    
    @Test
    fun `isOperationInProgress should return true when loading or checking out`() = runTest {
        // Test loading state
        viewModel.loadChild("child123")
        var state = viewModel.uiState.first()
        assertTrue(state.isOperationInProgress)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Test checking out state
        viewModel.confirmCheckOut()
        state = viewModel.uiState.first()
        assertTrue(state.isOperationInProgress)
    }
    
    @Test
    fun `canCheckOut should return correct value based on eligibility info`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(id = childId, status = CheckInStatus.CHECKED_IN)
        val eligibilityInfo = CheckOutEligibilityInfo(
            child = child,
            canCheckOut = true,
            currentService = null,
            checkInTime = null
        )
        
        mockRepository.setChild(child)
        mockUseCase.setEligibilityInfo(eligibilityInfo)
        
        // When
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.canCheckOut)
    }
    
    // Helper functions
    private fun createTestChild(
        id: String = "child123",
        parentId: String = "parent123",
        name: String = "Test Child",
        dateOfBirth: String = "2018-01-01",
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId: String? = null,
        checkInTime: String? = null,
        checkOutTime: String? = null
    ) = Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "123-456-7890",
            relationship = "Parent"
        ),
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestService(
        id: String = "service123",
        name: String = "Test Service",
        location: String = "Room 101"
    ) = KidsService(
        id = id,
        name = name,
        description = "Test service description",
        minAge = 3,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:30:00Z",
        location = location,
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff1", "staff2"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestCheckInRecord(
        id: String = "record123",
        childId: String = "child123",
        serviceId: String = "service123",
        checkInTime: String = "2025-01-01T10:00:00Z",
        checkOutTime: String? = "2025-01-01T11:00:00Z",
        checkedInBy: String = "parent123",
        checkedOutBy: String? = "parent123",
        notes: String? = null,
        status: CheckInStatus = CheckInStatus.CHECKED_OUT
    ) = CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status
    )
    
    // Mock implementations
    private class MockKidsRepository : KidsRepository {
        private var childResult: Result<Child>? = null
        private var serviceResult: Result<KidsService>? = null
        
        fun setChild(child: Child) {
            childResult = Result.success(child)
        }
        
        fun setChildResult(result: Result<Child>) {
            childResult = result
        }
        
        fun setService(service: KidsService) {
            serviceResult = Result.success(service)
        }
        
        fun setServiceResult(result: Result<KidsService>) {
            serviceResult = result
        }
        
        override suspend fun getChildById(childId: String): Result<Child> {
            return childResult ?: Result.failure(KidsManagementError.ChildNotFound)
        }
        
        override suspend fun getServiceById(serviceId: String): Result<KidsService> {
            return serviceResult ?: Result.failure(KidsManagementError.ServiceNotFound)
        }
        
        // Other methods not used in tests
        override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = TODO()
        override suspend fun registerChild(child: Child): Result<Child> = TODO()
        override suspend fun updateChild(child: Child): Result<Child> = TODO()
        override suspend fun deleteChild(childId: String): Result<Unit> = TODO()
        override suspend fun getAvailableServices(): Result<List<KidsService>> = TODO()
        override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> = TODO()
        override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> = TODO()
        override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?): Result<CheckInRecord> = TODO()
        override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<CheckInRecord> = TODO()
        override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<CheckInRecord>> = TODO()
        override suspend fun getCurrentCheckIns(serviceId: String): Result<List<CheckInRecord>> = TODO()
        override suspend fun getAllCurrentCheckIns(): Result<List<CheckInRecord>> = TODO()
        override suspend fun getCheckInRecord(recordId: String): Result<CheckInRecord> = TODO()
        override suspend fun getServiceReport(serviceId: String): Result<ServiceReport> = TODO()
        override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<AttendanceReport> = TODO()
        override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) = TODO()
        override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) = TODO()
        override suspend fun unsubscribeFromUpdates() = TODO()
    }
    
    private class MockCheckOutChildUseCase : CheckOutChildUseCase(MockKidsRepository()) {
        private var eligibilityInfo: CheckOutEligibilityInfo? = null
        private var checkOutResult: CheckOutResult? = null
        private var checkOutError: KidsManagementError? = null
        
        fun setEligibilityInfo(info: CheckOutEligibilityInfo) {
            eligibilityInfo = info
        }
        
        fun setCheckOutResult(result: CheckOutResult) {
            checkOutResult = result
            checkOutError = null
        }
        
        fun setCheckOutError(error: KidsManagementError) {
            checkOutError = error
            checkOutResult = null
        }
        
        override suspend fun getCheckOutEligibilityInfo(childId: String): Result<CheckOutEligibilityInfo> {
            return eligibilityInfo?.let { Result.success(it) } 
                ?: Result.failure(KidsManagementError.ChildNotFound)
        }
        
        override suspend fun execute(
            childId: String,
            checkedOutBy: String,
            notes: String?
        ): Result<CheckOutResult> {
            return checkOutResult?.let { Result.success(it) }
                ?: checkOutError?.let { Result.failure(it) }
                ?: Result.failure(KidsManagementError.NetworkError)
        }
    }
}