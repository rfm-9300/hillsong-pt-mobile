package rfm.hillsongptapp.feature.kids.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class KidsRepositorySyncTest {
    
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
    
    @Test
    fun `repository syncs local changes with remote when network becomes available`() = testScope.runTest {
        // Given - offline scenario with local changes
        val parentId = "parent123"
        val child = createTestChild("local_child_123", parentId)
        
        // Initially fail remote operations (offline)
        remoteDataSource.shouldFailRegisterChild = true
        
        // Register child offline
        val offlineResult = repository.registerChild(child)
        assertTrue(offlineResult.isSuccess)
        assertTrue(offlineResult.getOrThrow().id.startsWith("local_"))
        
        // Verify local storage
        assertEquals(1, localDataSource.insertedChildren.size)
        
        // When - network becomes available
        remoteDataSource.shouldFailRegisterChild = false
        remoteDataSource.childResponse = ChildResponse(
            success = true,
            child = createTestChildDto("server_child_123", parentId)
        )
        
        // Sync with remote by getting children (triggers sync)
        val syncResult = repository.getChildrenForParent(parentId)
        
        // Then - local data should be updated with server data
        assertTrue(syncResult.isSuccess)
        assertTrue(localDataSource.upsertedChildren.isNotEmpty())
    }
    
    @Test
    fun `repository handles concurrent check-in operations with conflict resolution`() = testScope.runTest {
        // Given - two parents trying to check in the same child simultaneously
        val childId = "child123"
        val serviceId = "service123"
        val parent1Id = "parent1"
        val parent2Id = "parent2"
        
        // Setup initial state
        localDataSource.childrenById[childId] = createTestChildEntity(childId, parent1Id, CheckInStatus.CHECKED_OUT)
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, currentCapacity = 9, maxCapacity = 10)
        
        // First check-in succeeds
        remoteDataSource.checkInResponse = CheckInResponse(
            success = true,
            record = createTestCheckInRecordDto("record1", childId, serviceId),
            updatedChild = createTestChildDto(childId, parent1Id, CheckInStatus.CHECKED_IN),
            updatedService = createTestServiceDto(serviceId, currentCapacity = 10)
        )
        
        val firstCheckIn = repository.checkInChild(childId, serviceId, parent1Id)
        assertTrue(firstCheckIn.isSuccess)
        
        // Second check-in should fail due to conflict (child already checked in)
        localDataSource.childrenById[childId] = createTestChildEntity(childId, parent1Id, CheckInStatus.CHECKED_IN)
        
        val secondCheckIn = repository.checkInChild(childId, serviceId, parent2Id)
        assertTrue(secondCheckIn.isFailure)
        assertTrue(secondCheckIn.exceptionOrNull()?.message?.contains("already checked in") == true)
    }
    
    @Test
    fun `repository handles service capacity conflicts gracefully`() = testScope.runTest {
        // Given - service at capacity
        val childId = "child123"
        val serviceId = "service123"
        val parentId = "parent123"
        
        localDataSource.childrenById[childId] = createTestChildEntity(childId, parentId, CheckInStatus.CHECKED_OUT)
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, currentCapacity = 10, maxCapacity = 10)
        
        // When - trying to check in to full service
        val result = repository.checkInChild(childId, serviceId, parentId)
        
        // Then - should fail with capacity error
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("full capacity") == true)
        
        // Verify no local changes were made
        assertEquals(0, localDataSource.checkInStatusUpdates.size)
        assertEquals(0, localDataSource.insertedCheckInRecords.size)
    }
    
    @Test
    fun `repository maintains data consistency during network failures`() = testScope.runTest {
        // Given - network failure during check-in
        val childId = "child123"
        val serviceId = "service123"
        val parentId = "parent123"
        
        localDataSource.childrenById[childId] = createTestChildEntity(childId, parentId, CheckInStatus.CHECKED_OUT)
        localDataSource.servicesById[serviceId] = createTestServiceEntity(serviceId, currentCapacity = 5, maxCapacity = 10)
        
        remoteDataSource.shouldFailCheckIn = true
        
        // When - check-in with network failure
        val result = repository.checkInChild(childId, serviceId, parentId)
        
        // Then - should succeed with local optimistic update
        assertTrue(result.isSuccess)
        assertEquals(CheckInStatus.CHECKED_IN, result.getOrThrow().status)
        
        // Verify local changes were made
        assertEquals(1, localDataSource.checkInStatusUpdates.size)
        assertEquals(1, localDataSource.insertedCheckInRecords.size)
        
        // Local state should reflect the check-in
        val statusUpdate = localDataSource.checkInStatusUpdates.first()
        assertEquals(CheckInStatus.CHECKED_IN.name, statusUpdate.status)
        assertEquals(serviceId, statusUpdate.serviceId)
    }
    
    @Test
    fun `repository handles offline check-out operations`() = testScope.runTest {
        // Given - child is checked in and network is offline
        val childId = "child123"
        val serviceId = "service123"
        val parentId = "parent123"
        val recordId = "record123"
        
        localDataSource.allCurrentCheckIns = listOf(
            createTestCheckInRecordEntity(recordId, childId, serviceId, CheckInStatus.CHECKED_IN)
        )
        
        remoteDataSource.shouldFailCheckOut = true
        
        // When - check-out with network failure
        val result = repository.checkOutChild(childId, parentId)
        
        // Then - should succeed with local optimistic update
        assertTrue(result.isSuccess)
        assertEquals(CheckInStatus.CHECKED_OUT, result.getOrThrow().status)
        assertNotNull(result.getOrThrow().checkOutTime)
        
        // Verify local changes were made
        assertEquals(1, localDataSource.checkInStatusUpdates.size)
        assertEquals(1, localDataSource.updatedCheckInRecords.size)
    }
    
    @Test
    fun `repository provides accurate service reports with current data`() = testScope.runTest {
        // Given - service with checked-in children
        val serviceId = "service123"
        val service = createTestServiceEntity(serviceId, currentCapacity = 3, maxCapacity = 10)
        val checkIns = listOf(
            createTestCheckInRecordEntity("record1", "child1", serviceId, CheckInStatus.CHECKED_IN),
            createTestCheckInRecordEntity("record2", "child2", serviceId, CheckInStatus.CHECKED_IN),
            createTestCheckInRecordEntity("record3", "child3", serviceId, CheckInStatus.CHECKED_IN)
        )
        val children = listOf(
            createTestChildEntity("child1", "parent1"),
            createTestChildEntity("child2", "parent2"),
            createTestChildEntity("child3", "parent3")
        )
        
        localDataSource.servicesById[serviceId] = service
        localDataSource.currentCheckInsByService[serviceId] = checkIns
        children.forEach { child ->
            localDataSource.childrenById[child.id] = child
        }
        
        // When - generating service report
        val result = repository.getServiceReport(serviceId)
        
        // Then - report should be accurate
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(serviceId, report.serviceId)
        assertEquals(3, report.currentCheckIns)
        assertEquals(7, report.availableSpots)
        assertEquals(3, report.checkedInChildren.size)
        assertEquals(10, report.totalCapacity)
    }
    
    // Helper methods for creating test data
    
    private fun createTestChild(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE
    ) = TestDataFactory.createTestChild(id, parentId, status)
    
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