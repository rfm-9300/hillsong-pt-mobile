package rfm.hillsongptapp.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.ktor.requests.CheckInRequest
import rfm.hillsongptapp.core.network.ktor.requests.CheckOutRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildRegistrationRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildUpdateRequest
import rfm.hillsongptapp.core.network.ktor.responses.CheckInApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckOutApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildrenApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.EmergencyContactResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServicesApiResponse
import rfm.hillsongptapp.core.network.result.NetworkResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for KidsRepositoryImpl
 * Tests all CRUD operations, error scenarios, and offline behavior
 */
class KidsRepositoryImplTest {
    
    // Mock dependencies
    private val mockChildDao = MockChildDao()
    private val mockCheckInRecordDao = MockCheckInRecordDao()
    private val mockKidsServiceDao = MockKidsServiceDao()
    private val mockKidsApiService = MockKidsApiService()
    private val testCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // System under test
    private val repository = KidsRepositoryImpl(
        childDao = mockChildDao,
        checkInRecordDao = mockCheckInRecordDao,
        kidsServiceDao = mockKidsServiceDao,
        kidsApiService = mockKidsApiService,
        coroutineScope = testCoroutineScope
    )
    
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
    
    // Child Management Tests
    
    @Test
    fun `getChildrenForParent returns success with local data when network succeeds`() = runTest {
        // Given
        val parentId = "parent-1"
        val childEntities = listOf(testChild.toEntity())
        mockChildDao.childrenByParent[parentId] = childEntities
        mockKidsApiService.childrenByParentResult = NetworkResult.Success(
            ChildrenApiResponse(
                success = true,
                children = listOf(testChild.toResponse()),
                message = "Success"
            )
        )
        
        // When
        val result = repository.getChildrenForParent(parentId)
        
        // Then
        assertIs<KidsResult.Success<List<Child>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(testChild.id, result.data[0].id)
        assertEquals(testChild.name, result.data[0].name)
    }
    
    @Test
    fun `getChildrenForParent returns local data when network fails`() = runTest {
        // Given
        val parentId = "parent-1"
        val childEntities = listOf(testChild.toEntity())
        mockChildDao.childrenByParent[parentId] = childEntities
        mockKidsApiService.childrenByParentResult = NetworkResult.Error("Network error")
        
        // When
        val result = repository.getChildrenForParent(parentId)
        
        // Then
        assertIs<KidsResult.Success<List<Child>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(testChild.id, result.data[0].id)
    }
    
    @Test
    fun `getChildrenForParent returns error when both local and network fail`() = runTest {
        // Given
        val parentId = "parent-1"
        mockChildDao.shouldThrowError = true
        mockKidsApiService.childrenByParentResult = NetworkResult.Error("Network error")
        
        // When
        val result = repository.getChildrenForParent(parentId)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("Failed to get children"))
    }
    
    @Test
    fun `registerChild creates local copy and syncs with remote`() = runTest {
        // Given
        val childToRegister = testChild.copy(id = "")
        mockKidsApiService.registerChildResult = NetworkResult.Success(
            ChildApiResponse(
                success = true,
                child = testChild.toResponse(),
                message = "Child registered successfully"
            )
        )
        
        // When
        val result = repository.registerChild(childToRegister)
        
        // Then
        assertIs<KidsResult.Success<Child>>(result)
        assertEquals(testChild.name, result.data.name)
        assertNotNull(result.data.id)
        assertTrue(mockChildDao.insertedChildren.isNotEmpty())
    }
    
    @Test
    fun `registerChild returns local copy when network fails`() = runTest {
        // Given
        val childToRegister = testChild.copy(id = "")
        mockKidsApiService.registerChildResult = NetworkResult.Error("Network error")
        
        // When
        val result = repository.registerChild(childToRegister)
        
        // Then
        assertIs<KidsResult.Success<Child>>(result)
        assertEquals(testChild.name, result.data.name)
        assertNotNull(result.data.id)
        assertTrue(mockChildDao.insertedChildren.isNotEmpty())
    }
    
    @Test
    fun `updateChild updates local and syncs with remote`() = runTest {
        // Given
        val updatedChild = testChild.copy(name = "Updated Name")
        mockKidsApiService.updateChildResult = NetworkResult.Success(
            ChildApiResponse(
                success = true,
                child = updatedChild.toResponse(),
                message = "Child updated successfully"
            )
        )
        
        // When
        val result = repository.updateChild(updatedChild)
        
        // Then
        assertIs<KidsResult.Success<Child>>(result)
        assertEquals("Updated Name", result.data.name)
        assertTrue(mockChildDao.updatedChildren.isNotEmpty())
    }
    
    @Test
    fun `deleteChild removes from local and remote`() = runTest {
        // Given
        val childId = "child-1"
        mockKidsApiService.deleteChildResult = NetworkResult.Success(
            ChildApiResponse(success = true, child = null, message = "Child deleted")
        )
        
        // When
        val result = repository.deleteChild(childId)
        
        // Then
        assertIs<KidsResult.Success<Unit>>(result)
        assertTrue(mockChildDao.deletedChildIds.contains(childId))
    }
    
    @Test
    fun `getChildById returns child from local database`() = runTest {
        // Given
        val childId = "child-1"
        mockChildDao.childrenById[childId] = testChild.toEntity()
        
        // When
        val result = repository.getChildById(childId)
        
        // Then
        assertIs<KidsResult.Success<Child>>(result)
        assertEquals(testChild.id, result.data.id)
        assertEquals(testChild.name, result.data.name)
    }
    
    @Test
    fun `getChildById fetches from remote when not in local database`() = runTest {
        // Given
        val childId = "child-1"
        mockKidsApiService.getChildResult = NetworkResult.Success(
            ChildApiResponse(
                success = true,
                child = testChild.toResponse(),
                message = "Success"
            )
        )
        
        // When
        val result = repository.getChildById(childId)
        
        // Then
        assertIs<KidsResult.Success<Child>>(result)
        assertEquals(testChild.id, result.data.id)
        assertTrue(mockChildDao.insertedChildren.isNotEmpty())
    }
    
    // Service Management Tests
    
    @Test
    fun `getAvailableServices returns services from local and syncs with remote`() = runTest {
        // Given
        val serviceEntities = listOf(testService.toEntity())
        mockKidsServiceDao.allServices = serviceEntities
        mockKidsApiService.getServicesResult = NetworkResult.Success(
            ServicesApiResponse(
                success = true,
                services = listOf(testService.toResponse()),
                message = "Success"
            )
        )
        
        // When
        val result = repository.getAvailableServices()
        
        // Then
        assertIs<KidsResult.Success<List<KidsService>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(testService.id, result.data[0].id)
    }
    
    @Test
    fun `getServicesForAge returns age-appropriate services`() = runTest {
        // Given
        val age = 8
        val serviceEntities = listOf(testService.toEntity())
        mockKidsServiceDao.servicesByAge[age] = serviceEntities
        
        // When
        val result = repository.getServicesForAge(age)
        
        // Then
        assertIs<KidsResult.Success<List<KidsService>>>(result)
        assertEquals(1, result.data.size)
        assertEquals(testService.id, result.data[0].id)
    }
    
    @Test
    fun `getServiceById returns service from local database`() = runTest {
        // Given
        val serviceId = "service-1"
        mockKidsServiceDao.servicesById[serviceId] = testService.toEntity()
        
        // When
        val result = repository.getServiceById(serviceId)
        
        // Then
        assertIs<KidsResult.Success<KidsService>>(result)
        assertEquals(testService.id, result.data.id)
        assertEquals(testService.name, result.data.name)
    }
    
    @Test
    fun `getServicesAcceptingCheckIns returns only accepting services`() = runTest {
        // Given
        val acceptingServices = listOf(testService.toEntity())
        mockKidsServiceDao.acceptingCheckInServices = acceptingServices
        
        // When
        val result = repository.getServicesAcceptingCheckIns()
        
        // Then
        assertIs<KidsResult.Success<List<KidsService>>>(result)
        assertEquals(1, result.data.size)
        assertTrue(result.data[0].isAcceptingCheckIns)
    }
    
    // Check-in/Check-out Tests
    
    @Test
    fun `checkInChild succeeds with valid child and service`() = runTest {
        // Given
        val childId = "child-1"
        val serviceId = "service-1"
        val checkedInBy = "staff-1"
        
        mockChildDao.childrenById[childId] = testChild.toEntity()
        mockKidsServiceDao.servicesById[serviceId] = testService.toEntity()
        mockKidsApiService.checkInChildResult = NetworkResult.Success(
            CheckInApiResponse(
                success = true,
                record = testCheckInRecord.toResponse(),
                updatedChild = null,
                updatedService = null,
                message = "Check-in successful"
            )
        )
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy, "Test notes")
        
        // Then
        assertIs<KidsResult.Success<CheckInRecord>>(result)
        assertEquals(childId, result.data.childId)
        assertEquals(serviceId, result.data.serviceId)
        assertEquals(CheckInStatus.CHECKED_IN, result.data.status)
        assertTrue(mockCheckInRecordDao.insertedRecords.isNotEmpty())
    }
    
    @Test
    fun `checkInChild fails when child not found`() = runTest {
        // Given
        val childId = "nonexistent-child"
        val serviceId = "service-1"
        val checkedInBy = "staff-1"
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy, null)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("Child not found"))
    }
    
    @Test
    fun `checkInChild fails when service not found`() = runTest {
        // Given
        val childId = "child-1"
        val serviceId = "nonexistent-service"
        val checkedInBy = "staff-1"
        
        mockChildDao.childrenById[childId] = testChild.toEntity()
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy, null)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("Service not found"))
    }
    
    @Test
    fun `checkInChild fails when child already checked in`() = runTest {
        // Given
        val childId = "child-1"
        val serviceId = "service-1"
        val checkedInBy = "staff-1"
        
        val checkedInChild = testChild.copy(status = CheckInStatus.CHECKED_IN)
        mockChildDao.childrenById[childId] = checkedInChild.toEntity()
        mockKidsServiceDao.servicesById[serviceId] = testService.toEntity()
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy, null)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("already checked in"))
    }
    
    @Test
    fun `checkInChild fails when service at capacity`() = runTest {
        // Given
        val childId = "child-1"
        val serviceId = "service-1"
        val checkedInBy = "staff-1"
        
        val fullService = testService.copy(currentCapacity = testService.maxCapacity)
        mockChildDao.childrenById[childId] = testChild.toEntity()
        mockKidsServiceDao.servicesById[serviceId] = fullService.toEntity()
        
        // When
        val result = repository.checkInChild(childId, serviceId, checkedInBy, null)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("full capacity"))
    }
    
    @Test
    fun `checkOutChild succeeds with valid checked-in child`() = runTest {
        // Given
        val childId = "child-1"
        val checkedOutBy = "staff-1"
        
        val checkedInChild = testChild.copy(status = CheckInStatus.CHECKED_IN, currentServiceId = "service-1")
        mockChildDao.childrenById[childId] = checkedInChild.toEntity()
        mockKidsServiceDao.servicesById["service-1"] = testService.toEntity()
        mockCheckInRecordDao.currentRecordsByChild[childId] = testCheckInRecord.toEntity()
        
        mockKidsApiService.checkOutChildResult = NetworkResult.Success(
            CheckOutApiResponse(
                success = true,
                record = testCheckInRecord.copy(
                    status = CheckInStatus.CHECKED_OUT,
                    checkOutTime = "2024-01-07T11:30:00Z",
                    checkedOutBy = checkedOutBy
                ).toResponse(),
                updatedChild = null,
                updatedService = null,
                message = "Check-out successful"
            )
        )
        
        // When
        val result = repository.checkOutChild(childId, checkedOutBy, "Test checkout notes")
        
        // Then
        assertIs<KidsResult.Success<CheckInRecord>>(result)
        assertEquals(childId, result.data.childId)
        assertEquals(CheckInStatus.CHECKED_OUT, result.data.status)
        assertNotNull(result.data.checkOutTime)
    }
    
    @Test
    fun `checkOutChild fails when child not checked in`() = runTest {
        // Given
        val childId = "child-1"
        val checkedOutBy = "staff-1"
        
        // When
        val result = repository.checkOutChild(childId, checkedOutBy, null)
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("not currently checked in"))
    }
    
    // Reporting Tests
    
    @Test
    fun `getServiceReport generates comprehensive service report`() = runTest {
        // Given
        val serviceId = "service-1"
        mockKidsServiceDao.servicesById[serviceId] = testService.toEntity()
        mockCheckInRecordDao.activeRecordsByService[serviceId] = listOf(testCheckInRecord.toEntity())
        mockChildDao.childrenById["child-1"] = testChild.toEntity()
        
        // When
        val result = repository.getServiceReport(serviceId)
        
        // Then
        assertIs<KidsResult.Success<ServiceReport>>(result)
        val report = result.data
        assertEquals(serviceId, report.serviceId)
        assertEquals(testService.name, report.serviceName)
        assertEquals(testService.maxCapacity, report.totalCapacity)
        assertEquals(testService.currentCapacity, report.currentCheckIns)
        assertEquals(1, report.checkedInChildren.size)
    }
    
    @Test
    fun `getAttendanceReport generates report for date range`() = runTest {
        // Given
        val startDate = "2024-01-01"
        val endDate = "2024-01-31"
        val records = listOf(testCheckInRecord.toEntity())
        mockCheckInRecordDao.recordsByDateRange["$startDate-$endDate"] = records
        
        // When
        val result = repository.getAttendanceReport(startDate, endDate)
        
        // Then
        assertIs<KidsResult.Success<AttendanceReport>>(result)
        val report = result.data
        assertEquals(startDate, report.startDate)
        assertEquals(endDate, report.endDate)
        assertEquals(1, report.totalCheckIns)
        assertEquals(1, report.uniqueChildren)
    }
    
    // Error Handling Tests
    
    @Test
    fun `repository handles database exceptions gracefully`() = runTest {
        // Given
        mockChildDao.shouldThrowError = true
        
        // When
        val result = repository.getChildrenForParent("parent-1")
        
        // Then
        assertIs<KidsResult.Error>(result)
        assertTrue(result.message.contains("Failed to get children"))
    }
    
    @Test
    fun `repository handles network exceptions gracefully`() = runTest {
        // Given
        mockKidsApiService.shouldThrowError = true
        mockChildDao.childrenByParent["parent-1"] = listOf(testChild.toEntity())
        
        // When
        val result = repository.getChildrenForParent("parent-1")
        
        // Then
        // Should still return local data even if network throws
        assertIs<KidsResult.Success<List<Child>>>(result)
        assertEquals(1, result.data.size)
    }
}