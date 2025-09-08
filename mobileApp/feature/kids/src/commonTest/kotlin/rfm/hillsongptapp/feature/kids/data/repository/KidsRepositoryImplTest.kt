package rfm.hillsongptapp.feature.kids.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.WebSocketMessage
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.ServiceReport
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class KidsRepositoryImplTest {
    
    private lateinit var localDataSource: MockKidsLocalDataSource
    private lateinit var remoteDataSource: MockKidsRemoteDataSource
    private lateinit var repository: KidsRepositoryImpl
    private lateinit var testScope: TestScope
    
    @BeforeTest
    fun setup() {
        testScope = TestScope()
        localDataSource = MockKidsLocalDataSource()
        remoteDataSource = MockKidsRemoteDataSource()
        repository = KidsRepositoryImpl(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            coroutineScope = testScope
        )
    }
    
    @AfterTest
    fun tearDown() {
        repository.cleanup()
    }
    
    // Child Management Tests
    
    @Test
    fun `getChildrenForParent returns local children when remote sync fails`() = testScope.runTest {
        // Given
        val parentId = "parent123"
        val localChildren = listOf(createTestChildEntity("child1", parentId))
        localDataSource.childrenByParentId[parentId] = localChildren
        remoteDataSource.shouldFailGetChildren = true
        
        // When
        val result = repository.getChildrenForParent(parentId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("child1", result.getOrThrow().first().id)
    }
    
    @Test
    fun `getChildrenForParent syncs with remote when available`() = testScope.runTest {
        // Given
        val parentId = "parent123"
        val localChildren = listOf(createTestChildEntity("child1", parentId))
        val remoteChildren = listOf(createTestChildDto("child1", parentId), createTestChildDto("child2", parentId))
        
        localDataSource.childrenByParentId[parentId] = localChildren
        remoteDataSource.childrenResponse = ChildrenResponse(
            success = true,
            children = remoteChildren
        )
        
        // When
        val result = repository.getChildrenForParent(parentId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertTrue(localDataSource.upsertedChildren.isNotEmpty())
    }
    
    @Test
    fun `registerChild saves locally and syncs with remote`() = testScope.runTest {
        // Given
        val child = createTestChild("", "parent123")
        remoteDataSource.childResponse = ChildResponse(
            success = true,
            child = createTestChildDto("server_id", "parent123")
        )
        
        // When
        val result = repository.registerChild(child)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("server_id", result.getOrThrow().id)
        assertTrue(localDataSource.insertedChildren.isNotEmpty())
        assertTrue(localDataSource.upsertedChildren.isNotEmpty())
    }
    
    @Test
    fun `registerChild works offline when remote fails`() = testScope.runTest {
        // Given
        val child = createTestChild("", "parent123")
        remoteDataSource.shouldFailRegisterChild = true
        
        // When
        val result = repository.registerChild(child)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().id.startsWith("local_"))
        assertTrue(localDataSource.insertedChildren.isNotEmpty())
    }
    
    @Test
    fun `updateChild updates locally and syncs with remote`() = testScope.runTest {
        // Given
        val child = createTestChild("child123", "parent123")
        remoteDataSource.childResponse = ChildResponse(
            success = true,
            child = createTestChildDto("child123", "parent123")
        )
        
        // When
        val result = repository.updateChild(child)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(localDataSource.updatedChildren.isNotEmpty())
    }
    
    @Test
    fun `deleteChild removes locally and from remote`() = testScope.runTest {
        // Given
        val childId = "child123"
        remoteDataSource.deleteChildResult = Result.success(Unit)
        
        // When
        val result = repository.deleteChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(localDataSource.deletedChildIds.contains(childId))
    }
    
    // Service Management Tests
    
    @Test
    fun `getAvailableServices returns local services and syncs with remote`() = testScope.runTest {
        // Given
        val localServices = listOf(createTestServiceEntity("service1"))
        val remoteServices = listOf(createTestServiceDto("service1"), createTestServiceDto("service2"))
        
        localDataSource.allServices = localServices
        remoteDataSource.servicesResponse = ServicesResponse(
            success = true,
            services = remoteServices
        )
        
        // When
        val result = repository.getAvailableServices()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        assertTrue(localDataSource.upsertedServices.isNotEmpty())
    }
    
    @Test
    fun `getServicesForAge filters services by age`() = testScope.runTest {
        // Given
        val services = listOf(
            createTestServiceEntity("service1", minAge = 3, maxAge = 6),
            createTestServiceEntity("service2", minAge = 7, maxAge = 12)
        )
        localDataSource.allServices = services
        
        // When
        val result = repository.getServicesForAge(5)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("service1", result.getOrThrow().first().id)
    }
    
    // Check-in/Check-out Tests
    
    @Test
    fun `checkInChild performs optimistic update and syncs with remote`() = testScope.runTest {
        // Given
        val childId = "child123"
        val serviceId = "service123"
        val checkedInBy = "parent123"
        
        localDataSource.childrenById[childId] = createTestChildEntity(childId, "parent123", CheckInStatus.CHECKED_OUT)
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, currentCapacity = 5, maxCapacity = 10)
        
        remoteDataSource.checkInResponse = CheckInResponse(
            success = true,
            record = createTestCheckInRecordDto("record123", childId, serviceId),
            updatedChild = createTestChildDto(childId, "parent123", CheckInStatus.CHECKED_IN),
            updatedService = createTestServiceDto(serviceId, currentCapacity = 6)
        )
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(CheckInStatus.CHECKED_IN, result.getOrThrow().status)
        assertTrue(localDataSource.checkInStatusUpdates.isNotEmpty())
        assertTrue(localDataSource.insertedCheckInRecords.isNotEmpty())
    }
    
    @Test
    fun `checkInChild fails when child already checked in`() = testScope.runTest {
        // Given
        val childId = "child123"
        val serviceId = "service123"
        val checkedInBy = "parent123"
        
        localDataSource.childrenById[childId] = createTestChildEntity(childId, "parent123", CheckInStatus.CHECKED_IN)
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already checked in") == true)
    }
    
    @Test
    fun `checkInChild fails when service at capacity`() = testScope.runTest {
        // Given
        val childId = "child123"
        val serviceId = "service123"
        val checkedInBy = "parent123"
        
        localDataSource.childrenById[childId] = createTestChildEntity(childId, "parent123", CheckInStatus.CHECKED_OUT)
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, currentCapacity = 10, maxCapacity = 10)
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("full capacity") == true)
    }
    
    @Test
    fun `checkInChild fails when child not eligible for service`() = testScope.runTest {
        // Given
        val childId = "child123"
        val serviceId = "service123"
        val checkedInBy = "parent123"
        
        // Child is 10 years old (born in 2015), service is for 3-6 year olds
        localDataSource.childrenById[childId] = createTestChildEntity(childId, "parent123", CheckInStatus.CHECKED_OUT, dateOfBirth = "2015-01-01")
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, minAge = 3, maxAge = 6)
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not eligible") == true)
    }
    
    @Test
    fun `checkOutChild performs optimistic update and syncs with remote`() = testScope.runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val recordId = "record123"
        
        localDataSource.allCurrentCheckIns = listOf(
            createTestCheckInRecordEntity(recordId, childId, "service123", CheckInStatus.CHECKED_IN)
        )
        
        remoteDataSource.checkOutResponse = CheckOutResponse(
            success = true,
            record = createTestCheckInRecordDto(recordId, childId, "service123", CheckInStatus.CHECKED_OUT),
            updatedChild = createTestChildDto(childId, "parent123", CheckInStatus.CHECKED_OUT),
            updatedService = createTestServiceDto("service123", currentCapacity = 5)
        )
        
        // When
        val result = repository.checkOutChild(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(CheckInStatus.CHECKED_OUT, result.getOrThrow().status)
        assertTrue(localDataSource.checkInStatusUpdates.isNotEmpty())
        assertTrue(localDataSource.updatedCheckInRecords.isNotEmpty())
    }
    
    @Test
    fun `checkOutChild fails when child not checked in`() = testScope.runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        
        localDataSource.allCurrentCheckIns = emptyList()
        
        // When
        val result = repository.checkOutChild(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not currently checked in") == true)
    }
    
    // Real-time Synchronization Tests
    
    @Test
    fun `subscribeToChildUpdates registers callback and subscribes to WebSocket`() = testScope.runTest {
        // Given
        val childId = "child123"
        var updateReceived = false
        val callback: (Child) -> Unit = { updateReceived = true }
        
        // When
        repository.subscribeToChildUpdates(childId, callback)
        
        // Then
        assertTrue(remoteDataSource.subscribedChildIds.contains(childId))
    }
    
    @Test
    fun `subscribeToServiceUpdates registers callback and subscribes to WebSocket`() = testScope.runTest {
        // Given
        val serviceId = "service123"
        var updateReceived = false
        val callback: (KidsService) -> Unit = { updateReceived = true }
        
        // When
        repository.subscribeToServiceUpdates(serviceId, callback)
        
        // Then
        assertTrue(remoteDataSource.subscribedServiceIds.contains(serviceId))
    }
    
    @Test
    fun `unsubscribeFromUpdates clears callbacks and unsubscribes from WebSocket`() = testScope.runTest {
        // Given
        repository.subscribeToChildUpdates("child123") { }
        repository.subscribeToServiceUpdates("service123") { }
        
        // When
        repository.unsubscribeFromUpdates()
        
        // Then
        assertTrue(remoteDataSource.unsubscribeCalled)
    }
    
    // Reporting Tests
    
    @Test
    fun `getServiceReport generates report with current data`() = testScope.runTest {
        // Given
        val serviceId = "service123"
        val service = createTestServiceEntity(serviceId, currentCapacity = 3, maxCapacity = 10)
        val checkIns = listOf(
            createTestCheckInRecordEntity("record1", "child1", serviceId, CheckInStatus.CHECKED_IN),
            createTestCheckInRecordEntity("record2", "child2", serviceId, CheckInStatus.CHECKED_IN)
        )
        val children = listOf(
            createTestChildEntity("child1", "parent1"),
            createTestChildEntity("child2", "parent2")
        )
        
        localDataSource.servicesById[serviceId] = service
        localDataSource.currentCheckInsByService[serviceId] = checkIns
        localDataSource.childrenById["child1"] = children[0]
        localDataSource.childrenById["child2"] = children[1]
        
        // When
        val result = repository.getServiceReport(serviceId)
        
        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(serviceId, report.serviceId)
        assertEquals(3, report.currentCheckIns)
        assertEquals(7, report.availableSpots)
        assertEquals(2, report.checkedInChildren.size)
    }
    
    // Helper methods for creating test data
    
    private fun createTestChild(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        dateOfBirth: String = "2020-01-01"
    ) = TestDataFactory.createTestChild(id, parentId, status, dateOfBirth)
    
    private fun createTestChildEntity(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        dateOfBirth: String = "2020-01-01"
    ) = TestDataFactory.createTestChildEntity(id, parentId, status, dateOfBirth)
    
    private fun createTestChildDto(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE
    ) = TestDataFactory.createTestChildDto(id, parentId, status)
    
    private fun createTestServiceEntity(
        id: String,
        minAge: Int = 3,
        maxAge: Int = 12,
        currentCapacity: Int = 0,
        maxCapacity: Int = 20
    ) = TestDataFactory.createTestServiceEntity(id, minAge, maxAge, currentCapacity, maxCapacity)
    
    private fun createTestServiceDto(
        id: String,
        currentCapacity: Int = 0
    ) = TestDataFactory.createTestServiceDto(id, currentCapacity)
    
    private fun createTestCheckInRecordEntity(
        id: String,
        childId: String,
        serviceId: String,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ) = TestDataFactory.createTestCheckInRecordEntity(id, childId, serviceId, status)
    
    private fun createTestCheckInRecordDto(
        id: String,
        childId: String,
        serviceId: String,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ) = TestDataFactory.createTestCheckInRecordDto(id, childId, serviceId, status)
}