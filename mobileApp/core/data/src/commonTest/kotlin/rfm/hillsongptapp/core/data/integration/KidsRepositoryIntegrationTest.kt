package rfm.hillsongptapp.core.data.integration

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsRepositoryImpl
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.network.api.KidsApiService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for KidsRepository that test end-to-end functionality
 * Uses in-memory database and mock network layer
 */
class KidsRepositoryIntegrationTest {
    
    // In-memory implementations of DAOs
    private val childDao = InMemoryChildDao()
    private val checkInRecordDao = InMemoryCheckInRecordDao()
    private val kidsServiceDao = InMemoryKidsServiceDao()
    private val mockApiService = MockKidsApiServiceForIntegration()
    
    // System under test
    private val repository: KidsRepository = KidsRepositoryImpl(
        childDao = childDao,
        checkInRecordDao = checkInRecordDao,
        kidsServiceDao = kidsServiceDao,
        kidsApiService = mockApiService
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
    
    @Test
    fun `complete child registration workflow works end-to-end`() = runTest {
        // Given - Child to register
        val childToRegister = testChild.copy(id = "")
        
        // When - Register child
        val registerResult = repository.registerChild(childToRegister)
        
        // Then - Child should be registered successfully
        assertIs<KidsResult.Success<Child>>(registerResult)
        val registeredChild = registerResult.data
        assertNotNull(registeredChild.id)
        assertEquals(testChild.name, registeredChild.name)
        
        // Verify child is stored in database
        val storedChild = childDao.getChildById(registeredChild.id)
        assertNotNull(storedChild)
        assertEquals(registeredChild.name, storedChild.name)
        
        // When - Retrieve child by ID
        val getChildResult = repository.getChildById(registeredChild.id)
        
        // Then - Should return the same child
        assertIs<KidsResult.Success<Child>>(getChildResult)
        assertEquals(registeredChild.id, getChildResult.data.id)
        assertEquals(registeredChild.name, getChildResult.data.name)
    }
    
    @Test
    fun `complete check-in workflow works end-to-end`() = runTest {
        // Given - Pre-registered child and service
        val childEntity = testChild.toEntity()
        val serviceEntity = testService.toEntity()
        
        childDao.insertChild(childEntity)
        kidsServiceDao.insertKidsService(serviceEntity)
        
        // When - Check in child
        val checkInResult = repository.checkInChild(
            childId = testChild.id,
            serviceId = testService.id,
            checkedInBy = "staff-1",
            notes = "Integration test check-in"
        )
        
        // Then - Check-in should succeed
        assertIs<KidsResult.Success<CheckInRecord>>(checkInResult)
        val checkInRecord = checkInResult.data
        assertEquals(testChild.id, checkInRecord.childId)
        assertEquals(testService.id, checkInRecord.serviceId)
        assertEquals(CheckInStatus.CHECKED_IN, checkInRecord.status)
        
        // Verify child status is updated in database
        val updatedChild = childDao.getChildById(testChild.id)
        assertNotNull(updatedChild)
        assertEquals(CheckInStatus.CHECKED_IN, updatedChild.status)
        assertEquals(testService.id, updatedChild.currentServiceId)
        
        // Verify service capacity is updated
        val updatedService = kidsServiceDao.getKidsServiceById(testService.id)
        assertNotNull(updatedService)
        assertEquals(testService.currentCapacity + 1, updatedService.currentCapacity)
        
        // Verify check-in record is stored
        val storedRecord = checkInRecordDao.getCheckInRecordById(checkInRecord.id)
        assertNotNull(storedRecord)
        assertEquals(checkInRecord.childId, storedRecord.childId)
        assertEquals(checkInRecord.serviceId, storedRecord.serviceId)
    }
    
    @Test
    fun `complete check-out workflow works end-to-end`() = runTest {
        // Given - Child already checked in
        val checkedInChild = testChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = testService.id,
            checkInTime = "2024-01-07T10:00:00Z"
        )
        val serviceWithChild = testService.copy(currentCapacity = testService.currentCapacity + 1)
        
        val existingCheckInRecord = CheckInRecord(
            id = "record-1",
            childId = testChild.id,
            serviceId = testService.id,
            checkInTime = "2024-01-07T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "staff-1",
            checkedOutBy = null,
            notes = "Initial check-in",
            status = CheckInStatus.CHECKED_IN
        )
        
        childDao.insertChild(checkedInChild.toEntity())
        kidsServiceDao.insertKidsService(serviceWithChild.toEntity())
        checkInRecordDao.insertCheckInRecord(existingCheckInRecord.toEntity())
        
        // When - Check out child
        val checkOutResult = repository.checkOutChild(
            childId = testChild.id,
            checkedOutBy = "staff-2",
            notes = "Integration test check-out"
        )
        
        // Then - Check-out should succeed
        assertIs<KidsResult.Success<CheckInRecord>>(checkOutResult)
        val checkOutRecord = checkOutResult.data
        assertEquals(testChild.id, checkOutRecord.childId)
        assertEquals(CheckInStatus.CHECKED_OUT, checkOutRecord.status)
        assertNotNull(checkOutRecord.checkOutTime)
        assertEquals("staff-2", checkOutRecord.checkedOutBy)
        
        // Verify child status is updated in database
        val updatedChild = childDao.getChildById(testChild.id)
        assertNotNull(updatedChild)
        assertEquals(CheckInStatus.CHECKED_OUT, updatedChild.status)
        assertEquals(null, updatedChild.currentServiceId)
        
        // Verify service capacity is decremented
        val updatedService = kidsServiceDao.getKidsServiceById(testService.id)
        assertNotNull(updatedService)
        assertEquals(testService.currentCapacity, updatedService.currentCapacity)
    }
    
    @Test
    fun `service management workflow works end-to-end`() = runTest {
        // Given - Service in database
        kidsServiceDao.insertKidsService(testService.toEntity())
        
        // When - Get available services
        val servicesResult = repository.getAvailableServices()
        
        // Then - Should return services from database
        assertIs<KidsResult.Success<List<KidsService>>>(servicesResult)
        val services = servicesResult.data
        assertEquals(1, services.size)
        assertEquals(testService.id, services[0].id)
        assertEquals(testService.name, services[0].name)
        
        // When - Get services accepting check-ins
        val acceptingServicesResult = repository.getServicesAcceptingCheckIns()
        
        // Then - Should return only accepting services
        assertIs<KidsResult.Success<List<KidsService>>>(acceptingServicesResult)
        val acceptingServices = acceptingServicesResult.data
        assertEquals(1, acceptingServices.size)
        assertTrue(acceptingServices[0].isAcceptingCheckIns)
        
        // When - Get service by ID
        val serviceByIdResult = repository.getServiceById(testService.id)
        
        // Then - Should return the specific service
        assertIs<KidsResult.Success<KidsService>>(serviceByIdResult)
        assertEquals(testService.id, serviceByIdResult.data.id)
    }
    
    @Test
    fun `reporting workflow works end-to-end`() = runTest {
        // Given - Service with checked-in children
        val service = testService.copy(currentCapacity = 2)
        val child1 = testChild.copy(id = "child-1", status = CheckInStatus.CHECKED_IN, currentServiceId = testService.id)
        val child2 = testChild.copy(id = "child-2", name = "Jane Smith", status = CheckInStatus.CHECKED_IN, currentServiceId = testService.id)
        
        val record1 = CheckInRecord(
            id = "record-1",
            childId = "child-1",
            serviceId = testService.id,
            checkInTime = "2024-01-07T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "staff-1",
            checkedOutBy = null,
            notes = null,
            status = CheckInStatus.CHECKED_IN
        )
        
        val record2 = CheckInRecord(
            id = "record-2",
            childId = "child-2",
            serviceId = testService.id,
            checkInTime = "2024-01-07T10:15:00Z",
            checkOutTime = null,
            checkedInBy = "staff-1",
            checkedOutBy = null,
            notes = null,
            status = CheckInStatus.CHECKED_IN
        )
        
        kidsServiceDao.insertKidsService(service.toEntity())
        childDao.insertChild(child1.toEntity())
        childDao.insertChild(child2.toEntity())
        checkInRecordDao.insertCheckInRecord(record1.toEntity())
        checkInRecordDao.insertCheckInRecord(record2.toEntity())
        
        // When - Generate service report
        val serviceReportResult = repository.getServiceReport(testService.id)
        
        // Then - Should generate comprehensive report
        assertIs<KidsResult.Success<rfm.hillsongptapp.core.data.model.ServiceReport>>(serviceReportResult)
        val serviceReport = serviceReportResult.data
        assertEquals(testService.id, serviceReport.serviceId)
        assertEquals(testService.name, serviceReport.serviceName)
        assertEquals(testService.maxCapacity, serviceReport.totalCapacity)
        assertEquals(2, serviceReport.currentCheckIns)
        assertEquals(2, serviceReport.checkedInChildren.size)
        
        // When - Generate attendance report
        val attendanceReportResult = repository.getAttendanceReport("2024-01-01", "2024-01-31")
        
        // Then - Should generate attendance statistics
        assertIs<KidsResult.Success<rfm.hillsongptapp.core.data.model.AttendanceReport>>(attendanceReportResult)
        val attendanceReport = attendanceReportResult.data
        assertEquals("2024-01-01", attendanceReport.startDate)
        assertEquals("2024-01-31", attendanceReport.endDate)
        assertEquals(2, attendanceReport.totalCheckIns)
        assertEquals(2, attendanceReport.uniqueChildren)
        assertTrue(attendanceReport.serviceBreakdown.containsKey(testService.id))
    }
    
    @Test
    fun `offline-first behavior works correctly`() = runTest {
        // Given - Network is unavailable
        mockApiService.shouldFailAllRequests = true
        
        // When - Register child (should work offline)
        val childToRegister = testChild.copy(id = "")
        val registerResult = repository.registerChild(childToRegister)
        
        // Then - Should succeed with local storage
        assertIs<KidsResult.Success<Child>>(registerResult)
        val registeredChild = registerResult.data
        assertNotNull(registeredChild.id)
        
        // Verify child is in local database
        val storedChild = childDao.getChildById(registeredChild.id)
        assertNotNull(storedChild)
        
        // When - Get children for parent (should work from local storage)
        val getChildrenResult = repository.getChildrenForParent(testChild.parentId)
        
        // Then - Should return local data
        assertIs<KidsResult.Success<List<Child>>>(getChildrenResult)
        val children = getChildrenResult.data
        assertEquals(1, children.size)
        assertEquals(registeredChild.id, children[0].id)
    }
    
    @Test
    fun `data consistency is maintained across operations`() = runTest {
        // Given - Initial setup
        childDao.insertChild(testChild.toEntity())
        kidsServiceDao.insertKidsService(testService.toEntity())
        
        // When - Perform multiple operations
        val checkInResult = repository.checkInChild(
            childId = testChild.id,
            serviceId = testService.id,
            checkedInBy = "staff-1",
            notes = "First check-in"
        )
        
        assertIs<KidsResult.Success<CheckInRecord>>(checkInResult)
        
        // Then - Verify data consistency
        val childAfterCheckIn = childDao.getChildById(testChild.id)
        val serviceAfterCheckIn = kidsServiceDao.getKidsServiceById(testService.id)
        val checkInRecords = checkInRecordDao.getCheckInRecordsByChildId(testChild.id)
        
        assertNotNull(childAfterCheckIn)
        assertNotNull(serviceAfterCheckIn)
        
        // Child should be marked as checked in
        assertEquals(CheckInStatus.CHECKED_IN, childAfterCheckIn.status)
        assertEquals(testService.id, childAfterCheckIn.currentServiceId)
        
        // Service capacity should be incremented
        assertEquals(testService.currentCapacity + 1, serviceAfterCheckIn.currentCapacity)
        
        // Check-in record should exist
        assertEquals(1, checkInRecords.size)
        assertEquals(CheckInStatus.CHECKED_IN, checkInRecords[0].status)
        
        // When - Check out the child
        val checkOutResult = repository.checkOutChild(
            childId = testChild.id,
            checkedOutBy = "staff-2",
            notes = "Check-out"
        )
        
        assertIs<KidsResult.Success<CheckInRecord>>(checkOutResult)
        
        // Then - Verify consistency after check-out
        val childAfterCheckOut = childDao.getChildById(testChild.id)
        val serviceAfterCheckOut = kidsServiceDao.getKidsServiceById(testService.id)
        val updatedRecords = checkInRecordDao.getCheckInRecordsByChildId(testChild.id)
        
        assertNotNull(childAfterCheckOut)
        assertNotNull(serviceAfterCheckOut)
        
        // Child should be marked as checked out
        assertEquals(CheckInStatus.CHECKED_OUT, childAfterCheckOut.status)
        assertEquals(null, childAfterCheckOut.currentServiceId)
        
        // Service capacity should be decremented
        assertEquals(testService.currentCapacity, serviceAfterCheckOut.currentCapacity)
        
        // Check-in record should be updated
        assertEquals(1, updatedRecords.size)
        assertEquals(CheckInStatus.CHECKED_OUT, updatedRecords[0].status)
        assertNotNull(updatedRecords[0].checkOutTime)
    }
}
// Ex
tension functions for converting between domain models and entities
private fun Child.toEntity(): ChildEntity {
    return ChildEntity(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact,
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun KidsService.toEntity(): KidsServiceEntity {
    return KidsServiceEntity(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

private fun CheckInRecord.toEntity(): CheckInRecordEntity {
    return CheckInRecordEntity(
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
}