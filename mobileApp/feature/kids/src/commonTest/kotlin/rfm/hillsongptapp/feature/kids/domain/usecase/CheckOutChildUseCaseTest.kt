package rfm.hillsongptapp.feature.kids.domain.usecase

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import kotlin.test.*

class CheckOutChildUseCaseTest {
    
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var useCase: CheckOutChildUseCase
    
    @BeforeTest
    fun setup() {
        mockRepository = MockKidsRepository()
        useCase = CheckOutChildUseCase(mockRepository)
    }
    
    @Test
    fun `execute should successfully check out child when all validations pass`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        val service = createTestService(id = "service123")
        val expectedRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = "service123",
            checkOutTime = "2025-01-01T11:00:00Z",
            checkedOutBy = checkedOutBy,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        mockRepository.setCheckOutResult(Result.success(expectedRecord))
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isSuccess)
        val checkOutResult = result.getOrThrow()
        assertEquals(expectedRecord, checkOutResult.record)
        assertEquals(child, checkOutResult.child)
        assertEquals(service, checkOutResult.service)
        
        // Verify repository was called correctly
        assertEquals(childId, mockRepository.lastCheckOutChildId)
        assertEquals(checkedOutBy, mockRepository.lastCheckedOutBy)
    }
    
    @Test
    fun `execute should fail when child is not found`() = runTest {
        // Given
        val childId = "nonexistent"
        val checkedOutBy = "parent123"
        
        mockRepository.setChildResult(Result.failure(KidsManagementError.ChildNotFound))
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(KidsManagementError.ChildNotFound, result.exceptionOrNull())
    }
    
    @Test
    fun `execute should fail when child is already checked out`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(KidsManagementError.ChildNotCheckedIn, result.exceptionOrNull())
    }
    
    @Test
    fun `execute should fail when child is not in service`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.NOT_IN_SERVICE
        )
        
        mockRepository.setChild(child)
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(KidsManagementError.ChildNotCheckedIn, result.exceptionOrNull())
    }
    
    @Test
    fun `execute should succeed even when current service is not found`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "nonexistent_service"
        )
        val expectedRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = "nonexistent_service",
            checkOutTime = "2025-01-01T11:00:00Z",
            checkedOutBy = checkedOutBy,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        mockRepository.setServiceResult(Result.failure(KidsManagementError.ServiceNotFound))
        mockRepository.setCheckOutResult(Result.success(expectedRecord))
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isSuccess)
        val checkOutResult = result.getOrThrow()
        assertEquals(expectedRecord, checkOutResult.record)
        assertEquals(child, checkOutResult.child)
        assertNull(checkOutResult.service) // Service should be null when not found
    }
    
    @Test
    fun `execute should fail when repository check-out operation fails`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        
        mockRepository.setChild(child)
        mockRepository.setCheckOutResult(Result.failure(KidsManagementError.NetworkError))
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(KidsManagementError.NetworkError, result.exceptionOrNull())
    }
    
    @Test
    fun `execute should handle unexpected exceptions`() = runTest {
        // Given
        val childId = "child123"
        val checkedOutBy = "parent123"
        
        mockRepository.setChildResult(Result.failure(RuntimeException("Unexpected error")))
        
        // When
        val result = useCase.execute(childId, checkedOutBy)
        
        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KidsManagementError.UnknownError)
        assertTrue(exception.message!!.contains("Unexpected error"))
    }
    
    @Test
    fun `getCheckOutEligibilityInfo should return correct eligibility for checked in child`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123",
            checkInTime = "2025-01-01T10:00:00Z"
        )
        val service = createTestService(id = "service123")
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        
        // When
        val result = useCase.getCheckOutEligibilityInfo(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrThrow()
        assertEquals(child, eligibilityInfo.child)
        assertTrue(eligibilityInfo.canCheckOut)
        assertEquals(service, eligibilityInfo.currentService)
        assertEquals(child.checkInTime, eligibilityInfo.checkInTime)
        assertNull(eligibilityInfo.reason)
    }
    
    @Test
    fun `getCheckOutEligibilityInfo should return correct eligibility for checked out child`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        
        // When
        val result = useCase.getCheckOutEligibilityInfo(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrThrow()
        assertEquals(child, eligibilityInfo.child)
        assertFalse(eligibilityInfo.canCheckOut)
        assertNull(eligibilityInfo.currentService)
        assertEquals("Child is already checked out", eligibilityInfo.reason)
    }
    
    @Test
    fun `getCheckOutEligibilityInfo should return correct eligibility for child not in service`() = runTest {
        // Given
        val childId = "child123"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.NOT_IN_SERVICE
        )
        
        mockRepository.setChild(child)
        
        // When
        val result = useCase.getCheckOutEligibilityInfo(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrThrow()
        assertEquals(child, eligibilityInfo.child)
        assertFalse(eligibilityInfo.canCheckOut)
        assertNull(eligibilityInfo.currentService)
        assertEquals("Child is not currently in any service", eligibilityInfo.reason)
    }
    
    @Test
    fun `CheckOutResult getSummary should format correctly`() {
        // Given
        val child = createTestChild(name = "John Doe")
        val service = createTestService(name = "Kids Church")
        val record = createTestCheckInRecord(
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = "2025-01-01T11:30:00Z"
        )
        val result = CheckOutResult(record, child, service)
        
        // When
        val summary = result.getSummary()
        
        // Then
        assertTrue(summary.contains("John Doe"))
        assertTrue(summary.contains("Kids Church"))
        assertTrue(summary.contains("10:00 AM"))
        assertTrue(summary.contains("11:30 AM"))
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
        checkOutTime: String? = null,
        checkedInBy: String = "parent123",
        checkedOutBy: String? = null,
        notes: String? = null,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
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