package rfm.hillsongptapp.feature.kids.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.ui.KidsManagementViewModel
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

/**
 * Integration test for real-time updates functionality
 * Tests the complete flow from WebSocket messages to UI state updates
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealTimeUpdatesIntegrationTest {
    
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
    fun `complete real-time update flow should work end-to-end`() = testScope.runTest {
        // Given - Initial setup with some data
        val testChild = createTestChild("child1")
        val testService = createTestService("service1")
        
        mockRepository.childrenResult = Result.success(listOf(testChild))
        mockRepository.servicesResult = Result.success(listOf(testService))
        
        // Load initial data
        viewModel.loadInitialData()
        advanceUntilIdle()
        
        // Verify initial state
        val initialState = viewModel.uiState.value
        assertEquals(1, initialState.children.size)
        assertEquals(1, initialState.services.size)
        assertEquals(ConnectionStatus.DISCONNECTED, viewModel.connectionStatus.value)
        
        // When - Connect to real-time updates
        mockRealTimeStatusManager.simulateConnection()
        advanceUntilIdle()
        
        // Then - Connection status should be updated
        assertEquals(ConnectionStatus.CONNECTED, viewModel.connectionStatus.value)
        assertTrue(mockRealTimeStatusManager.subscribeToChildCalled)
        assertTrue(mockRealTimeStatusManager.subscribeToServiceCalled)
        
        // When - Simulate child status update from WebSocket
        val childStatusUpdate = ChildStatusUpdate(
            childId = "child1",
            previousStatus = "CHECKED_OUT",
            newStatus = "CHECKED_IN",
            serviceId = "service1",
            timestamp = System.currentTimeMillis()
        )
        
        mockRealTimeStatusManager.emitChildStatusUpdate(childStatusUpdate)
        advanceUntilIdle()
        
        // Then - Data should be refreshed (in real implementation, specific child would be updated)
        assertTrue(mockRepository.getChildrenCalled)
        
        // When - Simulate service capacity update
        val serviceStatusUpdate = ServiceStatusUpdate(
            serviceId = "service1",
            serviceName = "Test Service",
            previousCapacity = 5,
            newCapacity = 6,
            maxCapacity = 10,
            timestamp = System.currentTimeMillis()
        )
        
        mockRealTimeStatusManager.emitServiceStatusUpdate(serviceStatusUpdate)
        advanceUntilIdle()
        
        // Then - Data should be refreshed
        assertTrue(mockRepository.getServicesCalledMultipleTimes)
        
        // When - Simulate check-in update
        val checkInUpdate = CheckInStatusUpdate(
            recordId = "record1",
            childId = "child1",
            childName = "Test Child",
            serviceId = "service1",
            serviceName = "Test Service",
            action = CheckInAction.CHECK_IN,
            timestamp = System.currentTimeMillis()
        )
        
        mockRealTimeStatusManager.emitCheckInUpdate(checkInUpdate)
        advanceUntilIdle()
        
        // Then - Data should be refreshed again
        assertTrue(mockRepository.refreshCalledMultipleTimes)
        
        // When - Simulate notification
        val notification = StatusNotification(
            type = NotificationType.CHILD_CHECKED_IN,
            title = "Child Checked In",
            message = "Test Child checked into Test Service",
            childId = "child1",
            serviceId = "service1",
            timestamp = System.currentTimeMillis()
        )
        
        mockRealTimeStatusManager.emitNotification(notification)
        advanceUntilIdle()
        
        // Then - Notification should be added to active notifications
        val notifications = viewModel.activeNotifications.value
        assertEquals(1, notifications.size)
        assertEquals(notification.title, notifications[0].title)
        assertEquals(notification.message, notifications[0].message)
        
        // When - Dismiss notification
        viewModel.dismissNotification(notification)
        
        // Then - Notification should be removed
        val updatedNotifications = viewModel.activeNotifications.value
        assertTrue(updatedNotifications.isEmpty())
    }
    
    @Test
    fun `connection failure and retry should work correctly`() = testScope.runTest {
        // Given - Connection fails initially
        mockRealTimeStatusManager.simulateConnectionFailure()
        
        // When - Try to connect
        viewModel.retryConnection()
        advanceUntilIdle()
        
        // Then - Status should be failed
        assertEquals(ConnectionStatus.FAILED, viewModel.connectionStatus.value)
        
        // When - Retry connection successfully
        mockRealTimeStatusManager.simulateConnection()
        viewModel.retryConnection()
        advanceUntilIdle()
        
        // Then - Status should be connected
        assertEquals(ConnectionStatus.CONNECTED, viewModel.connectionStatus.value)
    }
    
    @Test
    fun `fallback mechanism should work when real-time is unavailable`() = testScope.runTest {
        // Given - Real-time connection is not available
        mockRealTimeStatusManager.simulateConnectionFailure()
        
        // When - Load data
        mockRepository.childrenResult = Result.success(listOf(createTestChild("child1")))
        mockRepository.servicesResult = Result.success(listOf(createTestService("service1")))
        viewModel.loadInitialData()
        advanceUntilIdle()
        
        // Then - Data should still be loaded successfully
        val state = viewModel.uiState.value
        assertEquals(1, state.children.size)
        assertEquals(1, state.services.size)
        assertFalse(state.isLoading)
        
        // When - Manually refresh data
        viewModel.refreshData()
        advanceUntilIdle()
        
        // Then - Data should be refreshed even without real-time connection
        assertFalse(state.isRefreshing)
        assertTrue(mockRepository.refreshCalled)
    }
    
    @Test
    fun `notification management should handle multiple notifications correctly`() = testScope.runTest {
        // Given - Multiple notifications
        val notifications = listOf(
            StatusNotification(
                type = NotificationType.CHILD_CHECKED_IN,
                title = "Child 1 Checked In",
                message = "Child 1 message",
                timestamp = System.currentTimeMillis()
            ),
            StatusNotification(
                type = NotificationType.CHILD_CHECKED_OUT,
                title = "Child 2 Checked Out",
                message = "Child 2 message",
                timestamp = System.currentTimeMillis()
            ),
            StatusNotification(
                type = NotificationType.SERVICE_FULL,
                title = "Service Full",
                message = "Service is at capacity",
                timestamp = System.currentTimeMillis()
            )
        )
        
        // When - Emit all notifications
        notifications.forEach { notification ->
            mockRealTimeStatusManager.emitNotification(notification)
        }
        advanceUntilIdle()
        
        // Then - All notifications should be present
        val activeNotifications = viewModel.activeNotifications.value
        assertEquals(3, activeNotifications.size)
        
        // When - Dismiss one notification
        viewModel.dismissNotification(notifications[1])
        
        // Then - Only 2 notifications should remain
        val remainingNotifications = viewModel.activeNotifications.value
        assertEquals(2, remainingNotifications.size)
        assertFalse(remainingNotifications.contains(notifications[1]))
        
        // When - Clear all notifications
        viewModel.clearAllNotifications()
        
        // Then - No notifications should remain
        val finalNotifications = viewModel.activeNotifications.value
        assertTrue(finalNotifications.isEmpty())
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
}

/**
 * Enhanced mock repository for integration testing
 */
private class MockKidsRepository : KidsRepository {
    
    var childrenResult: Result<List<Child>> = Result.success(emptyList())
    var servicesResult: Result<List<KidsService>> = Result.success(emptyList())
    
    var getChildrenCalled = false
    var getServicesCalledMultipleTimes = false
    var refreshCalled = false
    var refreshCalledMultipleTimes = false
    
    private var getChildrenCallCount = 0
    private var getServicesCallCount = 0
    private var refreshCallCount = 0
    
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> {
        getChildrenCalled = true
        getChildrenCallCount++
        return childrenResult
    }
    
    override suspend fun getAvailableServices(): Result<List<KidsService>> {
        getServicesCallCount++
        getServicesCalledMultipleTimes = getServicesCallCount > 2
        return servicesResult
    }
    
    fun simulateRefresh() {
        refreshCalled = true
        refreshCallCount++
        refreshCalledMultipleTimes = refreshCallCount > 2
    }
    
    // Other methods not needed for these tests
    override suspend fun registerChild(child: Child) = TODO()
    override suspend fun updateChild(child: Child) = TODO()
    override suspend fun deleteChild(childId: String) = TODO()
    override suspend fun getChildById(childId: String) = TODO()
    override suspend fun getServicesForAge(age: Int) = TODO()
    override suspend fun getServiceById(serviceId: String) = TODO()
    override suspend fun getServicesAcceptingCheckIns() = TODO()
    override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?) = TODO()
    override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?) = TODO()
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
 * Enhanced mock real-time status manager for integration testing
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
    
    var subscribeToChildCalled = false
    var subscribeToServiceCalled = false
    
    override suspend fun connect(): Result<Unit> {
        return if (_connectionStatus.value == ConnectionStatus.FAILED) {
            Result.failure(Exception("Connection failed"))
        } else {
            _connectionStatus.value = ConnectionStatus.CONNECTED
            Result.success(Unit)
        }
    }
    
    override suspend fun disconnect() {
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
    
    // Test helper methods
    
    fun simulateConnection() {
        _connectionStatus.value = ConnectionStatus.CONNECTED
    }
    
    fun simulateConnectionFailure() {
        _connectionStatus.value = ConnectionStatus.FAILED
    }
    
    fun emitChildStatusUpdate(update: ChildStatusUpdate) {
        _childStatusUpdates.tryEmit(update)
    }
    
    fun emitServiceStatusUpdate(update: ServiceStatusUpdate) {
        _serviceStatusUpdates.tryEmit(update)
    }
    
    fun emitCheckInUpdate(update: CheckInStatusUpdate) {
        _checkInUpdates.tryEmit(update)
    }
    
    fun emitNotification(notification: StatusNotification) {
        _notifications.tryEmit(notification)
    }
}