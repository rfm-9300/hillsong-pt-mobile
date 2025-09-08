package rfm.hillsongptapp.feature.kids.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.data.network.websocket.RealTimeStatusManager
import rfm.hillsongptapp.feature.kids.data.network.websocket.ConnectionStatus
import rfm.hillsongptapp.feature.kids.data.network.websocket.StatusNotification
import rfm.hillsongptapp.feature.kids.data.network.websocket.NotificationType
import rfm.hillsongptapp.feature.kids.data.network.websocket.ChildStatusUpdate
import rfm.hillsongptapp.feature.kids.data.network.websocket.ServiceStatusUpdate
import rfm.hillsongptapp.feature.kids.data.network.websocket.CheckInStatusUpdate
import rfm.hillsongptapp.feature.kids.data.network.websocket.CheckInAction

@OptIn(ExperimentalCoroutinesApi::class)
class KidsManagementViewModelTest {
    
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var mockRealTimeStatusManager: MockRealTimeStatusManager
    private lateinit var viewModel: KidsManagementViewModel
    private val testScope = TestScope()
    
    @BeforeTest
    fun setup() {
        mockRepository = MockKidsRepository()
        mockRealTimeStatusManager = MockRealTimeStatusManager()
        
        // Set up test dispatcher
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        
        viewModel = KidsManagementViewModel(
            kidsRepository = mockRepository,
            realTimeStatusManager = mockRealTimeStatusManager
        )
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be loading`() = testScope.runTest {
        // Given - fresh ViewModel
        
        // When - check initial state
        val initialState = viewModel.uiState.value
        
        // Then
        assertTrue(initialState.isLoading)
        assertTrue(initialState.children.isEmpty())
        assertTrue(initialState.services.isEmpty())
        assertNull(initialState.error)
    }
    
    @Test
    fun `loadInitialData should update state with children and services`() = testScope.runTest {
        // Given
        val testChildren = listOf(createTestChild("child1"))
        val testServices = listOf(createTestService("service1"))
        
        mockRepository.childrenResult = Result.success(testChildren)
        mockRepository.servicesResult = Result.success(testServices)
        
        // When
        viewModel.loadInitialData()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testChildren, state.children)
        assertEquals(testServices, state.services)
        assertNull(state.error)
        assertTrue(state.lastUpdated > 0)
    }
    
    @Test
    fun `loadInitialData should handle repository error`() = testScope.runTest {
        // Given
        mockRepository.childrenResult = Result.failure(Exception("Network error"))
        
        // When
        viewModel.loadInitialData()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.children.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }
    
    @Test
    fun `refreshData should update state and reset refresh flag`() = testScope.runTest {
        // Given
        val testChildren = listOf(createTestChild("child1"))
        val testServices = listOf(createTestService("service1"))
        
        mockRepository.childrenResult = Result.success(testChildren)
        mockRepository.servicesResult = Result.success(testServices)
        
        // When
        viewModel.refreshData()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(testChildren, state.children)
        assertEquals(testServices, state.services)
    }
    
    @Test
    fun `checkInChild should call repository and refresh data on success`() = testScope.runTest {
        // Given
        val childId = "child1"
        val serviceId = "service1"
        mockRepository.checkInResult = Result.success(createTestCheckInRecord())
        mockRepository.childrenResult = Result.success(listOf(createTestChild(childId)))
        mockRepository.servicesResult = Result.success(listOf(createTestService(serviceId)))
        
        // When
        viewModel.checkInChild(childId, serviceId)
        advanceUntilIdle()
        
        // Then
        assertTrue(mockRepository.checkInCalled)
        assertEquals(childId, mockRepository.lastCheckInChildId)
        assertEquals(serviceId, mockRepository.lastCheckInServiceId)
        assertFalse(viewModel.uiState.value.showCheckInDialog)
    }
    
    @Test
    fun `checkInChild should set error on repository failure`() = testScope.runTest {
        // Given
        val childId = "child1"
        val serviceId = "service1"
        mockRepository.checkInResult = Result.failure(Exception("Service is full"))
        
        // When
        viewModel.checkInChild(childId, serviceId)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Service is full"))
    }
    
    @Test
    fun `checkOutChild should call repository and refresh data on success`() = testScope.runTest {
        // Given
        val childId = "child1"
        mockRepository.checkOutResult = Result.success(createTestCheckInRecord())
        mockRepository.childrenResult = Result.success(listOf(createTestChild(childId)))
        mockRepository.servicesResult = Result.success(listOf(createTestService("service1")))
        
        // When
        viewModel.checkOutChild(childId)
        advanceUntilIdle()
        
        // Then
        assertTrue(mockRepository.checkOutCalled)
        assertEquals(childId, mockRepository.lastCheckOutChildId)
        assertFalse(viewModel.uiState.value.showCheckOutDialog)
    }
    
    @Test
    fun `dismissNotification should remove notification from list`() = testScope.runTest {
        // Given
        val notification = StatusNotification(
            type = NotificationType.CHILD_CHECKED_IN,
            title = "Test",
            message = "Test message",
            timestamp = System.currentTimeMillis()
        )
        
        // Simulate adding a notification
        mockRealTimeStatusManager.emitNotification(notification)
        advanceUntilIdle()
        
        // When
        viewModel.dismissNotification(notification)
        
        // Then
        val notifications = viewModel.activeNotifications.value
        assertFalse(notifications.contains(notification))
    }
    
    @Test
    fun `clearAllNotifications should remove all notifications`() = testScope.runTest {
        // Given
        val notification1 = StatusNotification(
            type = NotificationType.CHILD_CHECKED_IN,
            title = "Test 1",
            message = "Test message 1",
            timestamp = System.currentTimeMillis()
        )
        val notification2 = StatusNotification(
            type = NotificationType.CHILD_CHECKED_OUT,
            title = "Test 2",
            message = "Test message 2",
            timestamp = System.currentTimeMillis()
        )
        
        // Simulate adding notifications
        mockRealTimeStatusManager.emitNotification(notification1)
        mockRealTimeStatusManager.emitNotification(notification2)
        advanceUntilIdle()
        
        // When
        viewModel.clearAllNotifications()
        
        // Then
        val notifications = viewModel.activeNotifications.value
        assertTrue(notifications.isEmpty())
    }
    
    @Test
    fun `retryConnection should call real-time status manager connect`() = testScope.runTest {
        // When
        viewModel.retryConnection()
        advanceUntilIdle()
        
        // Then
        assertTrue(mockRealTimeStatusManager.connectCalled)
    }
    
    @Test
    fun `getLastUpdatedTime should return formatted time`() = testScope.runTest {
        // Given - load some data to set lastUpdated
        mockRepository.childrenResult = Result.success(emptyList())
        mockRepository.servicesResult = Result.success(emptyList())
        viewModel.loadInitialData()
        advanceUntilIdle()
        
        // When
        val lastUpdatedTime = viewModel.getLastUpdatedTime()
        
        // Then
        assertEquals("Just now", lastUpdatedTime)
    }
    
    @Test
    fun `setAutoRefreshEnabled should update UI state`() {
        // When
        viewModel.setAutoRefreshEnabled(false)
        
        // Then
        assertFalse(viewModel.uiState.value.autoRefreshEnabled)
        
        // When
        viewModel.setAutoRefreshEnabled(true)
        
        // Then
        assertTrue(viewModel.uiState.value.autoRefreshEnabled)
    }
    
    // Helper functions
    
    private fun createTestChild(id: String) = Child(
        id = id,
        parentId = "parent123",
        name = "Test Child",
        dateOfBirth = "2020-01-01",
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Test Parent",
            phoneNumber = "123-456-7890",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_OUT,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2023-01-01T09:00:00Z",
        updatedAt = "2023-01-01T09:00:00Z"
    )
    
    private fun createTestService(id: String) = KidsService(
        id = id,
        name = "Test Service",
        description = "Test Description",
        minAge = 3,
        maxAge = 12,
        startTime = "10:00",
        endTime = "11:00",
        location = "Room 1",
        maxCapacity = 10,
        currentCapacity = 5,
        isAcceptingCheckIns = true,
        staffMembers = listOf("Staff 1"),
        createdAt = "2023-01-01T09:00:00Z"
    )
    
    private fun createTestCheckInRecord() = rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord(
        id = "record123",
        childId = "child1",
        serviceId = "service1",
        checkInTime = "2023-01-01T10:00:00Z",
        checkOutTime = null,
        checkedInBy = "parent123",
        checkedOutBy = null,
        notes = null,
        status = CheckInStatus.CHECKED_IN
    )
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    
    var childrenResult: Result<List<Child>> = Result.success(emptyList())
    var servicesResult: Result<List<KidsService>> = Result.success(emptyList())
    var checkInResult: Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = Result.success(
        rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord(
            id = "record123",
            childId = "child1",
            serviceId = "service1",
            checkInTime = "2023-01-01T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "parent123",
            checkedOutBy = null,
            notes = null,
            status = CheckInStatus.CHECKED_IN
        )
    )
    var checkOutResult: Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = Result.success(
        rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord(
            id = "record123",
            childId = "child1",
            serviceId = "service1",
            checkInTime = "2023-01-01T10:00:00Z",
            checkOutTime = "2023-01-01T11:00:00Z",
            checkedInBy = "parent123",
            checkedOutBy = "parent123",
            notes = null,
            status = CheckInStatus.CHECKED_OUT
        )
    )
    
    var checkInCalled = false
    var checkOutCalled = false
    var lastCheckInChildId: String? = null
    var lastCheckInServiceId: String? = null
    var lastCheckOutChildId: String? = null
    
    override suspend fun getChildrenForParent(parentId: String) = childrenResult
    override suspend fun getAvailableServices() = servicesResult
    
    override suspend fun checkInChild(
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String?
    ): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> {
        checkInCalled = true
        lastCheckInChildId = childId
        lastCheckInServiceId = serviceId
        return checkInResult
    }
    
    override suspend fun checkOutChild(
        childId: String,
        checkedOutBy: String,
        notes: String?
    ): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> {
        checkOutCalled = true
        lastCheckOutChildId = childId
        return checkOutResult
    }
    
    // Other methods not needed for these tests
    override suspend fun registerChild(child: Child) = TODO()
    override suspend fun updateChild(child: Child) = TODO()
    override suspend fun deleteChild(childId: String) = TODO()
    override suspend fun getChildById(childId: String) = TODO()
    override suspend fun getServicesForAge(age: Int) = TODO()
    override suspend fun getServiceById(serviceId: String) = TODO()
    override suspend fun getServicesAcceptingCheckIns() = TODO()
    override suspend fun getCheckInHistory(childId: String, limit: Int?) = TODO()
    override suspend fun getCurrentCheckIns(serviceId: String) = TODO()
    override suspend fun getAllCurrentCheckIns() = TODO()
    override suspend fun getCheckInRecord(recordId: String) = TODO()
    override suspend fun getServiceReport(serviceId: String) = TODO()
    override suspend fun getAttendanceReport(startDate: String, endDate: String) = TODO()
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) = TODO()
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) = TODO()
    override suspend fun unsubscribeFromUpdates() = TODO()
}

/**
 * Mock implementation of RealTimeStatusManager for testing
 */
private class MockRealTimeStatusManager : RealTimeStatusManager {
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    override val connectionStatus = _connectionStatus
    
    private val _childStatusUpdates = MutableSharedFlow<ChildStatusUpdate>()
    override val childStatusUpdates = _childStatusUpdates
    
    private val _serviceStatusUpdates = MutableSharedFlow<ServiceStatusUpdate>()
    override val serviceStatusUpdates = _serviceStatusUpdates
    
    private val _checkInUpdates = MutableSharedFlow<CheckInStatusUpdate>()
    override val checkInUpdates = _checkInUpdates
    
    private val _notifications = MutableSharedFlow<StatusNotification>()
    override val notifications = _notifications
    
    var connectCalled = false
    var disconnectCalled = false
    var subscribeToChildCalled = false
    var subscribeToServiceCalled = false
    
    override suspend fun connect(): Result<Unit> {
        connectCalled = true
        _connectionStatus.value = ConnectionStatus.CONNECTED
        return Result.success(Unit)
    }
    
    override suspend fun disconnect() {
        disconnectCalled = true
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
    
    override suspend fun subscribeToChild(childId: String): Result<Unit> {
        subscribeToChildCalled = true
        return Result.success(Unit)
    }
    
    override suspend fun subscribeToService(serviceId: String): Result<Unit> {
        subscribeToServiceCalled = true
        return Result.success(Unit)
    }
    
    override suspend fun unsubscribeFromAll(): Result<Unit> = Result.success(Unit)
    
    override fun isConnected(): Boolean = _connectionStatus.value == ConnectionStatus.CONNECTED
    
    override fun hasFallbackMechanism(): Boolean = true
    
    override fun cleanup() {
        // Mock cleanup
    }
    
    fun emitNotification(notification: StatusNotification) {
        _notifications.tryEmit(notification)
    }
}