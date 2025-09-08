package rfm.hillsongptapp.feature.kids.domain.usecase

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Test suite for CheckInChildUseCase
 * Tests all validation scenarios and success cases for child check-in operations
 */
class CheckInChildUseCaseTest {
    
    private val mockRepository = MockKidsRepository()
    private val useCase = CheckInChildUseCase(mockRepository)
    
    @Test
    fun `execute should succeed when all validations pass`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        val notes = "Test check-in"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        val service = createTestService(
            id = serviceId,
            currentCapacity = 5,
            maxCapacity = 10,
            isAcceptingCheckIns = true,
            minAge = 3,
            maxAge = 12
        )
        val expectedRecord = createTestCheckInRecord(
            childId = childId,
            serviceId = serviceId,
            checkedInBy = checkedInBy,
            notes = notes
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        mockRepository.setCheckInResult(Result.success(expectedRecord))
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy, notes)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecord, result.getOrNull())
    }
    
    @Test
    fun `execute should fail when child not found`() = runTest {
        // Given
        val childId = "nonexistent_child"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        mockRepository.setChildResult(Result.failure(KidsManagementError.ChildNotFound))
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.ChildNotFound)
    }
    
    @Test
    fun `execute should fail when child already checked in`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "other_service"
        )
        
        mockRepository.setChild(child)
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.ChildAlreadyCheckedIn)
    }
    
    @Test
    fun `execute should fail when service not found`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "nonexistent_service"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        
        mockRepository.setChild(child)
        mockRepository.setServiceResult(Result.failure(KidsManagementError.ServiceNotFound))
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.ServiceNotFound)
    }
    
    @Test
    fun `execute should fail when service not accepting check-ins`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        val service = createTestService(
            id = serviceId,
            isAcceptingCheckIns = false
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.ServiceNotAcceptingCheckIns)
    }
    
    @Test
    fun `execute should fail when child too young for service`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT,
            dateOfBirth = "2023-01-01" // 2 years old
        )
        val service = createTestService(
            id = serviceId,
            minAge = 5,
            maxAge = 12,
            isAcceptingCheckIns = true
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.InvalidAgeForService)
    }
    
    @Test
    fun `execute should fail when child too old for service`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT,
            dateOfBirth = "2010-01-01" // 15 years old
        )
        val service = createTestService(
            id = serviceId,
            minAge = 5,
            maxAge = 12,
            isAcceptingCheckIns = true
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.InvalidAgeForService)
    }
    
    @Test
    fun `execute should fail when service at capacity`() = runTest {
        // Given
        val childId = "child_1"
        val serviceId = "service_1"
        val checkedInBy = "user_1"
        
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT
        )
        val service = createTestService(
            id = serviceId,
            currentCapacity = 10,
            maxCapacity = 10, // At full capacity
            isAcceptingCheckIns = true,
            minAge = 3,
            maxAge = 12
        )
        
        mockRepository.setChild(child)
        mockRepository.setService(service)
        
        // When
        val result = useCase.execute(childId, serviceId, checkedInBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KidsManagementError.ServiceAtCapacity)
    }
    
    @Test
    fun `getEligibleServicesForChild should return eligible services`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT,
            dateOfBirth = "2018-01-01" // 7 years old
        )
        
        val eligibleService = createTestService(
            id = "service_1",
            name = "Elementary Service",
            minAge = 5,
            maxAge = 10,
            currentCapacity = 5,
            maxCapacity = 20,
            isAcceptingCheckIns = true
        )
        
        val ineligibleService = createTestService(
            id = "service_2",
            name = "Teen Service",
            minAge = 13,
            maxAge = 18,
            currentCapacity = 3,
            maxCapacity = 15,
            isAcceptingCheckIns = true
        )
        
        mockRepository.setChild(child)
        mockRepository.setServicesAcceptingCheckIns(listOf(eligibleService, ineligibleService))
        
        // When
        val result = useCase.getEligibleServicesForChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrNull()!!
        assertEquals(child, eligibilityInfo.child)
        assertEquals(1, eligibilityInfo.eligibleServices.size)
        assertEquals(eligibleService, eligibilityInfo.eligibleServices[0].service)
        assertEquals(15, eligibilityInfo.eligibleServices[0].availableSpots)
        assertTrue(eligibilityInfo.eligibleServices[0].isRecommended) // More than 5 spots
    }
    
    @Test
    fun `getEligibleServicesForChild should mark services with limited spots as not recommended`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT,
            dateOfBirth = "2018-01-01" // 7 years old
        )
        
        val limitedService = createTestService(
            id = "service_1",
            name = "Limited Service",
            minAge = 5,
            maxAge = 10,
            currentCapacity = 8,
            maxCapacity = 10, // Only 2 spots left
            isAcceptingCheckIns = true
        )
        
        mockRepository.setChild(child)
        mockRepository.setServicesAcceptingCheckIns(listOf(limitedService))
        
        // When
        val result = useCase.getEligibleServicesForChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrNull()!!
        assertEquals(1, eligibilityInfo.eligibleServices.size)
        assertEquals(2, eligibilityInfo.eligibleServices[0].availableSpots)
        assertTrue(!eligibilityInfo.eligibleServices[0].isRecommended) // 5 or fewer spots
    }
    
    @Test
    fun `getEligibleServicesForChild should return empty list when no eligible services`() = runTest {
        // Given
        val childId = "child_1"
        val child = createTestChild(
            id = childId,
            status = CheckInStatus.CHECKED_OUT,
            dateOfBirth = "2023-01-01" // 2 years old
        )
        
        val service = createTestService(
            id = "service_1",
            name = "Elementary Service",
            minAge = 5,
            maxAge = 10,
            isAcceptingCheckIns = true
        )
        
        mockRepository.setChild(child)
        mockRepository.setServicesAcceptingCheckIns(listOf(service))
        
        // When
        val result = useCase.getEligibleServicesForChild(childId)
        
        // Then
        assertTrue(result.isSuccess)
        val eligibilityInfo = result.getOrNull()!!
        assertEquals(child, eligibilityInfo.child)
        assertTrue(eligibilityInfo.eligibleServices.isEmpty())
    }
    
    // Helper functions
    
    private fun createTestChild(
        id: String = "child_1",
        parentId: String = "parent_1",
        name: String = "Test Child",
        dateOfBirth: String = "2018-01-01",
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId: String? = null
    ) = Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestService(
        id: String = "service_1",
        name: String = "Test Service",
        description: String = "Test Description",
        minAge: Int = 3,
        maxAge: Int = 12,
        startTime: String = "09:00:00",
        endTime: String = "10:30:00",
        location: String = "Room 101",
        maxCapacity: Int = 20,
        currentCapacity: Int = 0,
        isAcceptingCheckIns: Boolean = true,
        staffMembers: List<String> = listOf("staff_1"),
        createdAt: String = "2025-01-01T00:00:00Z"
    ) = KidsService(
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
    
    private fun createTestCheckInRecord(
        id: String = "record_1",
        childId: String = "child_1",
        serviceId: String = "service_1",
        checkInTime: String = "2025-01-08T09:00:00Z",
        checkedInBy: String = "user_1",
        notes: String? = null,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ) = CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = null,
        checkedInBy = checkedInBy,
        checkedOutBy = null,
        notes = notes,
        status = status
    )
    
    /**
     * Mock implementation of KidsRepository for testing
     */
    private class MockKidsRepository : KidsRepository {
        private var childResult: Result<Child>? = null
        private var serviceResult: Result<KidsService>? = null
        private var checkInResult: Result<CheckInRecord>? = null
        private var servicesAcceptingCheckIns: List<KidsService> = emptyList()
        
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
        
        fun setCheckInResult(result: Result<CheckInRecord>) {
            checkInResult = result
        }
        
        fun setServicesAcceptingCheckIns(services: List<KidsService>) {
            servicesAcceptingCheckIns = services
        }
        
        override suspend fun getChildById(childId: String): Result<Child> {
            return childResult ?: Result.failure(KidsManagementError.ChildNotFound)
        }
        
        override suspend fun getServiceById(serviceId: String): Result<KidsService> {
            return serviceResult ?: Result.failure(KidsManagementError.ServiceNotFound)
        }
        
        override suspend fun checkInChild(
            childId: String,
            serviceId: String,
            checkedInBy: String,
            notes: String?
        ): Result<CheckInRecord> {
            return checkInResult ?: Result.failure(KidsManagementError.UnknownError("Not configured"))
        }
        
        override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> {
            return Result.success(servicesAcceptingCheckIns)
        }
        
        // Unused methods for this test
        override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = TODO()
        override suspend fun registerChild(child: Child): Result<Child> = TODO()
        override suspend fun updateChild(child: Child): Result<Child> = TODO()
        override suspend fun deleteChild(childId: String): Result<Unit> = TODO()
        override suspend fun getAvailableServices(): Result<List<KidsService>> = TODO()
        override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> = TODO()
        override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<CheckInRecord> = TODO()
        override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<CheckInRecord>> = TODO()
        override suspend fun getCurrentCheckIns(serviceId: String): Result<List<CheckInRecord>> = TODO()
        override suspend fun getAllCurrentCheckIns(): Result<List<CheckInRecord>> = TODO()
        override suspend fun getCheckInRecord(recordId: String): Result<CheckInRecord> = TODO()
        override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.ServiceReport> = TODO()
        override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport> = TODO()
        override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) = TODO()
        override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) = TODO()
        override suspend fun unsubscribeFromUpdates() = TODO()
    }
}