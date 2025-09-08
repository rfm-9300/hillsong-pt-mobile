package rfm.hillsongptapp.feature.kids.data.network.websocket

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.dto.ChildDto
import rfm.hillsongptapp.feature.kids.data.network.dto.KidsServiceDto

@OptIn(ExperimentalCoroutinesApi::class)
class RealTimeStatusManagerTest {
    
    private lateinit var mockRemoteDataSource: MockKidsRemoteDataSource
    private lateinit var realTimeStatusManager: RealTimeStatusManager
    private val testScope = TestScope()
    
    @BeforeTest
    fun setup() {
        mockRemoteDataSource = MockKidsRemoteDataSource()
        realTimeStatusManager = RealTimeStatusManager(
            remoteDataSource = mockRemoteDataSource,
            coroutineScope = testScope
        )
    }
    
    @AfterTest
    fun tearDown() {
        realTimeStatusManager.cleanup()
    }
    
    @Test
    fun `connect should update connection status to connected on success`() = testScope.runTest {
        // Given
        mockRemoteDataSource.connectResult = Result.success(Unit)
        
        // When
        val result = realTimeStatusManager.connect()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(ConnectionStatus.CONNECTED, realTimeStatusManager.connectionStatus.value)
    }
    
    @Test
    fun `connect should update connection status to disconnected on failure`() = testScope.runTest {
        // Given
        mockRemoteDataSource.connectResult = Result.failure(Exception("Connection failed"))
        
        // When
        val result = realTimeStatusManager.connect()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(ConnectionStatus.DISCONNECTED, realTimeStatusManager.connectionStatus.value)
    }
    
    @Test
    fun `disconnect should update connection status to disconnected`() = testScope.runTest {
        // Given
        mockRemoteDataSource.connectResult = Result.success(Unit)
        realTimeStatusManager.connect()
        
        // When
        realTimeStatusManager.disconnect()
        
        // Then
        assertEquals(ConnectionStatus.DISCONNECTED, realTimeStatusManager.connectionStatus.value)
    }
    
    @Test
    fun `subscribeToChild should store subscription when not connected`() = testScope.runTest {
        // Given
        val childId = "child123"
        
        // When
        val result = realTimeStatusManager.subscribeToChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        // Subscription should be stored for when connection is established
    }
    
    @Test
    fun `subscribeToChild should call remote data source when connected`() = testScope.runTest {
        // Given
        val childId = "child123"
        mockRemoteDataSource.connectResult = Result.success(Unit)
        mockRemoteDataSource.subscribeToChildResult = Result.success(Unit)
        realTimeStatusManager.connect()
        
        // When
        val result = realTimeStatusManager.subscribeToChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockRemoteDataSource.subscribeToChildCalled)
        assertEquals(childId, mockRemoteDataSource.lastChildSubscription)
    }
    
    @Test
    fun `subscribeToService should call remote data source when connected`() = testScope.runTest {
        // Given
        val serviceId = "service123"
        mockRemoteDataSource.connectResult = Result.success(Unit)
        mockRemoteDataSource.subscribeToServiceResult = Result.success(Unit)
        realTimeStatusManager.connect()
        
        // When
        val result = realTimeStatusManager.subscribeToService(serviceId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockRemoteDataSource.subscribeToServiceCalled)
        assertEquals(serviceId, mockRemoteDataSource.lastServiceSubscription)
    }
    
    @Test
    fun `child status update should emit update and notification`() = testScope.runTest {
        // Given
        val childDto = ChildDto(
            id = "child123",
            parentId = "parent123",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = null,
            status = "CHECKED_IN",
            currentServiceId = "service123",
            checkInTime = "2023-01-01T10:00:00Z",
            checkOutTime = null,
            createdAt = "2023-01-01T09:00:00Z",
            updatedAt = "2023-01-01T10:00:00Z"
        )
        
        val message = ChildStatusUpdateMessage(
            child = childDto,
            previousStatus = "CHECKED_OUT",
            newStatus = "CHECKED_IN",
            serviceId = "service123",
            timestamp = "1672574400000"
        )
        
        val updates = mutableListOf<ChildStatusUpdate>()
        val notifications = mutableListOf<StatusNotification>()
        
        val updateJob = launch {
            realTimeStatusManager.childStatusUpdates.toList(updates)
        }
        
        val notificationJob = launch {
            realTimeStatusManager.notifications.toList(notifications)
        }
        
        // When
        mockRemoteDataSource.emitMessage(message)
        
        // Then
        advanceUntilIdle()
        
        assertEquals(1, updates.size)
        assertEquals("child123", updates[0].childId)
        assertEquals("CHECKED_OUT", updates[0].previousStatus)
        assertEquals("CHECKED_IN", updates[0].newStatus)
        
        assertEquals(1, notifications.size)
        assertEquals(NotificationType.CHILD_STATUS_CHANGED, notifications[0].type)
        assertEquals("Child Status Updated", notifications[0].title)
        
        updateJob.cancel()
        notificationJob.cancel()
    }
    
    @Test
    fun `service capacity update should emit update and notification when full`() = testScope.runTest {
        // Given
        val serviceDto = KidsServiceDto(
            id = "service123",
            name = "Test Service",
            description = "Test Description",
            minAge = 3,
            maxAge = 12,
            startTime = "10:00",
            endTime = "11:00",
            location = "Room 1",
            maxCapacity = 10,
            currentCapacity = 10,
            isAcceptingCheckIns = false,
            staffMembers = emptyList(),
            createdAt = "2023-01-01T09:00:00Z"
        )
        
        val message = ServiceCapacityUpdateMessage(
            service = serviceDto,
            previousCapacity = 9,
            newCapacity = 10,
            timestamp = "1672574400000"
        )
        
        val updates = mutableListOf<ServiceStatusUpdate>()
        val notifications = mutableListOf<StatusNotification>()
        
        val updateJob = launch {
            realTimeStatusManager.serviceStatusUpdates.toList(updates)
        }
        
        val notificationJob = launch {
            realTimeStatusManager.notifications.toList(notifications)
        }
        
        // When
        mockRemoteDataSource.emitMessage(message)
        
        // Then
        advanceUntilIdle()
        
        assertEquals(1, updates.size)
        assertEquals("service123", updates[0].serviceId)
        assertEquals(9, updates[0].previousCapacity)
        assertEquals(10, updates[0].newCapacity)
        
        assertEquals(1, notifications.size)
        assertEquals(NotificationType.SERVICE_FULL, notifications[0].type)
        assertEquals("Service Full", notifications[0].title)
        
        updateJob.cancel()
        notificationJob.cancel()
    }
    
    @Test
    fun `isConnected should return correct status`() = testScope.runTest {
        // Initially disconnected
        assertFalse(realTimeStatusManager.isConnected())
        
        // Connect
        mockRemoteDataSource.connectResult = Result.success(Unit)
        realTimeStatusManager.connect()
        assertTrue(realTimeStatusManager.isConnected())
        
        // Disconnect
        realTimeStatusManager.disconnect()
        assertFalse(realTimeStatusManager.isConnected())
    }
    
    @Test
    fun `hasFallbackMechanism should return true`() {
        assertTrue(realTimeStatusManager.hasFallbackMechanism())
    }
}

/**
 * Mock implementation of KidsRemoteDataSource for testing
 */
private class MockKidsRemoteDataSource : KidsRemoteDataSource {
    
    var connectResult: Result<Unit> = Result.success(Unit)
    var subscribeToChildResult: Result<Unit> = Result.success(Unit)
    var subscribeToServiceResult: Result<Unit> = Result.success(Unit)
    var unsubscribeResult: Result<Unit> = Result.success(Unit)
    
    var subscribeToChildCalled = false
    var subscribeToServiceCalled = false
    var lastChildSubscription: String? = null
    var lastServiceSubscription: String? = null
    
    private val messageFlow = kotlinx.coroutines.flow.MutableSharedFlow<WebSocketMessage>()
    
    override suspend fun connectWebSocket(): Result<Unit> = connectResult
    
    override suspend fun disconnectWebSocket() {
        // Mock implementation
    }
    
    override suspend fun subscribeToChildUpdates(childId: String): Result<Unit> {
        subscribeToChildCalled = true
        lastChildSubscription = childId
        return subscribeToChildResult
    }
    
    override suspend fun subscribeToServiceUpdates(serviceId: String): Result<Unit> {
        subscribeToServiceCalled = true
        lastServiceSubscription = serviceId
        return subscribeToServiceResult
    }
    
    override suspend fun unsubscribeFromUpdates(): Result<Unit> = unsubscribeResult
    
    override fun getWebSocketMessages() = messageFlow
    
    override fun isWebSocketConnected(): Boolean = connectResult.isSuccess
    
    fun emitMessage(message: WebSocketMessage) {
        messageFlow.tryEmit(message)
    }
    
    // Other methods not needed for these tests
    override suspend fun getChildrenForParent(parentId: String) = TODO()
    override suspend fun registerChild(request: rfm.hillsongptapp.feature.kids.data.network.dto.ChildRegistrationRequest) = TODO()
    override suspend fun updateChild(childId: String, request: rfm.hillsongptapp.feature.kids.data.network.dto.ChildUpdateRequest) = TODO()
    override suspend fun deleteChild(childId: String) = TODO()
    override suspend fun getChildById(childId: String) = TODO()
    override suspend fun getAvailableServices() = TODO()
    override suspend fun getServicesForAge(age: Int) = TODO()
    override suspend fun getServiceById(serviceId: String) = TODO()
    override suspend fun getServicesAcceptingCheckIns() = TODO()
    override suspend fun checkInChild(request: rfm.hillsongptapp.feature.kids.data.network.dto.CheckInRequest) = TODO()
    override suspend fun checkOutChild(request: rfm.hillsongptapp.feature.kids.data.network.dto.CheckOutRequest) = TODO()
    override suspend fun getCheckInHistory(childId: String, limit: Int?) = TODO()
    override suspend fun getCurrentCheckIns(serviceId: String) = TODO()
    override suspend fun getAllCurrentCheckIns() = TODO()
    override suspend fun getServiceReport(serviceId: String) = TODO()
    override suspend fun getAttendanceReport(request: rfm.hillsongptapp.feature.kids.data.network.dto.AttendanceReportRequest) = TODO()
}