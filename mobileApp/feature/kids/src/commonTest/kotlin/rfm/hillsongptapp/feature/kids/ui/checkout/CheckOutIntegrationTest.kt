package rfm.hillsongptapp.feature.kids.ui.checkout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutChildUseCase
import kotlin.test.*

/**
 * Integration test for the complete check-out flow
 * Tests the interaction between CheckOutViewModel, CheckOutChildUseCase, and KidsRepository
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckOutIntegrationTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var useCase: CheckOutChildUseCase
    private lateinit var viewModel: CheckOutViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        useCase = CheckOutChildUseCase(mockRepository)
        viewModel = CheckOutViewModel(mockRepository, useCase)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `complete check-out flow should work end-to-end`() = runTest {
        // Given - A child is checked into a service
        val childId = "child123"
        val serviceId = "service123"
        val parentId = "parent123"
        
        val child = createTestChild(
            id = childId,
            parentId = parentId,
            name = "John Doe",
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = serviceId,
            checkInTime = "2025-01-01T10:00:00Z"
        )
        
        val service = createTestService(
            id = serviceId,
            name = "Kids Church",
            location = "Room 101"
        )
        
        val checkOutRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = serviceId,
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = "2025-01-01T11:30:00Z",
            checkedInBy = parentId,
            checkedOutBy = parentId,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        mockRepository.setCheckOutResult(Result.success(checkOutRecord))
        
        // When - Load child and go through check-out process
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify child is loaded correctly
        var state = viewModel.uiState.first()
        assertEquals(child, state.child)
        assertEquals(service, state.currentService)
        assertTrue(state.canCheckOut)
        
        // Start check-out process
        viewModel.startCheckOutProcess()
        state = viewModel.uiState.first()
        assertTrue(state.showParentVerification)
        
        // Complete parent verification
        viewModel.onParentVerified()
        state = viewModel.uiState.first()
        assertFalse(state.showParentVerification)
        assertTrue(state.showCheckOutConfirmation)
        
        // Confirm check-out
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Check-out should be successful
        state = viewModel.uiState.first()
        assertFalse(state.isCheckingOut)
        assertTrue(state.showSuccessDialog)
        assertNotNull(state.checkOutResult)
        
        val result = state.checkOutResult!!
        assertEquals(checkOutRecord, result.record)
        assertEquals(child, result.child)
        assertEquals(service, result.service)
        
        // Verify repository was called correctly
        assertEquals(childId, mockRepository.lastCheckOutChildId)
        assertEquals(parentId, mockRepository.lastCheckedOutBy)
    }
    
    @Test
    fun `check-out flow should handle child not checked in error`() = runTest {
        // Given - A child that is not checked in
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT // Already checked out
        )
        
        mockRepository.setChild(child)
        
        // When - Load child and attempt check-out
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify child is loaded but cannot be checked out
        var state = viewModel.uiState.first()
        assertEquals(child, state.child)
        assertFalse(state.canCheckOut)
        assertEquals("Child is already checked out", state.eligibilityInfo?.reason)
        
        // Attempt check-out anyway (should fail)
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Should show error
        state = viewModel.uiState.first()
        assertTrue(state.showErrorDialog)
        assertNotNull(state.checkOutError)
        assertTrue(state.checkOutError!!.contains("not currently checked into any service"))
    }
    
    @Test
    fun `check-out flow should handle network errors gracefully`() = runTest {
        // Given - A child checked in but network fails during check-out
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        
        mockRepository.setChild(child)
        mockRepository.setCheckOutResult(Result.failure(KidsManagementError.NetworkError))
        
        // When - Go through check-out process
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Should show network error
        val state = viewModel.uiState.first()
        assertTrue(state.showErrorDialog)
        assertEquals("Network connection error occurred", state.checkOutError)
        
        // Verify retry functionality
        mockRepository.setCheckOutResult(Result.success(createTestCheckInRecord()))
        viewModel.retryCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val retryState = viewModel.uiState.first()
        assertTrue(retryState.showSuccessDialog)
        assertFalse(retryState.showErrorDialog)
    }
    
    @Test
    fun `check-out flow should handle missing service gracefully`() = runTest {
        // Given - A child checked in but service no longer exists
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "nonexistent_service"
        )
        
        val checkOutRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = "nonexistent_service"
        )
        
        mockRepository.setChild(child)
        mockRepository.setServiceResult(Result.failure(KidsManagementError.ServiceNotFound))
        mockRepository.setCheckOutResult(Result.success(checkOutRecord))
        
        // When - Load child and check out
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify child is loaded but service is null
        var state = viewModel.uiState.first()
        assertEquals(child, state.child)
        assertNull(state.currentService)
        assertTrue(state.canCheckOut) // Should still be able to check out
        
        // Complete check-out process
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Should succeed with null service
        state = viewModel.uiState.first()
        assertTrue(state.showSuccessDialog)
        assertNotNull(state.checkOutResult)
        assertNull(state.checkOutResult!!.service)
    }
    
    @Test
    fun `dialog state management should work correctly`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(id = childId, status = CheckInStatus.CHECKED_IN)
        mockRepository.setChild(child)
        
        viewModel.loadChild(childId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Test parent verification dialog
        viewModel.startCheckOutProcess()
        var state = viewModel.uiState.first()
        assertTrue(state.showParentVerification)
        
        viewModel.hideParentVerification()
        state = viewModel.uiState.first()
        assertFalse(state.showParentVerification)
        
        // Test confirmation dialog
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        state = viewModel.uiState.first()
        assertTrue(state.showCheckOutConfirmation)
        
        viewModel.hideCheckOutConfirmation()
        state = viewModel.uiState.first()
        assertFalse(state.showCheckOutConfirmation)
        
        // Test success dialog
        mockRepository.setCheckOutResult(Result.success(createTestCheckInRecord()))
        viewModel.startCheckOutProcess()
        viewModel.onParentVerified()
        viewModel.confirmCheckOut()
        testDispatcher.scheduler.advanceUntilIdle()
        
        state = viewModel.uiState.first()
        assertTrue(state.showSuccessDialog)
        
        viewModel.hideSuccessDialog()
        state = viewModel.uiState.first()
        assertFalse(state.showSuccessDialog)
        assertNull(state.checkOutResult)
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
    
    // Mock repository implementation
    private class MockKidsRepository : KidsRepository {
        private var childResult: Result<Child>? = null
        private var serviceResult: Result<KidsService>? = null
        private var checkOutResult: Result<CheckInRecord>? = null
        
        var lastCheckOutChildId: String? = null
        var lastCheckedOutBy: String? = null
        var lastCheckOutNotes: String? = null
        
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
        
        fun setCheckOutResult(result: Result<CheckInRecord>) {
            checkOutResult = result
        }
        
        override suspend fun getChildById(childId: String): Result<Child> {
            return childResult ?: Result.failure(KidsManagementError.ChildNotFound)
        }
        
        override suspend fun getServiceById(serviceId: String): Result<KidsService> {
            return serviceResult ?: Result.failure(KidsManagementError.ServiceNotFound)
        }
        
        override suspend fun checkOutChild(
            childId: String,
            checkedOutBy: String,
            notes: String?
        ): Result<CheckInRecord> {
            lastCheckOutChildId = childId
            lastCheckedOutBy = checkedOutBy
            lastCheckOutNotes = notes
            return checkOutResult ?: Result.failure(KidsManagementError.NetworkError)
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
}