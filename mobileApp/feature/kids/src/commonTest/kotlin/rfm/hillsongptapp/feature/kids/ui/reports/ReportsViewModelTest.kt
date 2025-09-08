package rfm.hillsongptapp.feature.kids.ui.reports

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository

/**
 * Unit tests for ReportsViewModel
 * Tests state management, data loading, filtering, and export functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {
    
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var viewModel: ReportsViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        viewModel = ReportsViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should have default date range`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
        assertTrue(initialState.selectedStartDate.isNotBlank())
        assertTrue(initialState.selectedEndDate.isNotBlank())
        assertTrue(initialState.availableServices.isEmpty())
        assertTrue(initialState.selectedServices.isEmpty())
        assertNull(initialState.attendanceReport)
        assertTrue(initialState.serviceReports.isEmpty())
    }
    
    @Test
    fun `loadInitialData should load services and reports successfully`() = runTest {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids")
        )
        val serviceReport1 = createTestServiceReport("1", "Toddlers", 5, 20)
        val serviceReport2 = createTestServiceReport("2", "Kids", 15, 30)
        val attendanceReport = createTestAttendanceReport()
        
        mockRepository.servicesResult = Result.success(services)
        mockRepository.serviceReportsResult = mapOf(
            "1" to Result.success(serviceReport1),
            "2" to Result.success(serviceReport2)
        )
        mockRepository.attendanceReportResult = Result.success(attendanceReport)
        
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.availableServices.size)
        assertEquals(setOf("1", "2"), state.selectedServices)
        assertEquals(2, state.serviceReports.size)
        assertNotNull(state.attendanceReport)
        
        // Verify service reports are sorted by current check-ins (descending)
        assertEquals("2", state.serviceReports[0].serviceId) // 15 check-ins
        assertEquals("1", state.serviceReports[1].serviceId) // 5 check-ins
    }
    
    @Test
    fun `loadInitialData should handle service loading error`() = runTest {
        // Given
        mockRepository.servicesResult = Result.failure(Exception("Network error"))
        
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error"))
    }
    
    @Test
    fun `updateDateRange should reload attendance report`() = runTest {
        // Given
        val newStartDate = "2024-01-01"
        val newEndDate = "2024-01-07"
        val attendanceReport = createTestAttendanceReport(startDate = newStartDate, endDate = newEndDate)
        
        mockRepository.attendanceReportResult = Result.success(attendanceReport)
        
        // When
        viewModel.updateDateRange(newStartDate, newEndDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(newStartDate, state.selectedStartDate)
        assertEquals(newEndDate, state.selectedEndDate)
        assertNotNull(state.attendanceReport)
        assertEquals(newStartDate, state.attendanceReport!!.startDate)
        assertEquals(newEndDate, state.attendanceReport!!.endDate)
    }
    
    @Test
    fun `updateServiceFilter should update selected services and reload report`() = runTest {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids")
        )
        val selectedServices = setOf("1")
        val filteredReport = createTestAttendanceReport(
            serviceBreakdown = mapOf("1" to 10),
            totalCheckIns = 10
        )
        
        mockRepository.servicesResult = Result.success(services)
        mockRepository.attendanceReportResult = Result.success(filteredReport)
        
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.updateServiceFilter(selectedServices)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(selectedServices, state.selectedServices)
        assertNotNull(state.attendanceReport)
        // The report should be filtered to only include selected service
        assertEquals(1, state.attendanceReport!!.serviceBreakdown.size)
        assertTrue(state.attendanceReport!!.serviceBreakdown.containsKey("1"))
    }
    
    @Test
    fun `selectService should update selected service ID`() = runTest {
        // Given
        val serviceId = "test-service-1"
        
        // When
        viewModel.selectService(serviceId)
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals(serviceId, state.selectedServiceId)
    }
    
    @Test
    fun `exportReport should show success message`() = runTest {
        // Given
        val attendanceReport = createTestAttendanceReport()
        mockRepository.attendanceReportResult = Result.success(attendanceReport)
        
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.exportReport()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertEquals("Report exported successfully", state.exportStatus)
    }
    
    @Test
    fun `refreshData should reload all data`() = runTest {
        // Given
        val services = listOf(createTestService("1", "Toddlers"))
        val serviceReport = createTestServiceReport("1", "Toddlers", 5, 20)
        val attendanceReport = createTestAttendanceReport()
        
        mockRepository.servicesResult = Result.success(services)
        mockRepository.serviceReportsResult = mapOf("1" to Result.success(serviceReport))
        mockRepository.attendanceReportResult = Result.success(attendanceReport)
        
        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.availableServices.size)
        assertEquals(1, state.serviceReports.size)
        assertNotNull(state.attendanceReport)
    }
    
    @Test
    fun `service report loading should handle individual service failures`() = runTest {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids")
        )
        val serviceReport1 = createTestServiceReport("1", "Toddlers", 5, 20)
        
        mockRepository.servicesResult = Result.success(services)
        mockRepository.serviceReportsResult = mapOf(
            "1" to Result.success(serviceReport1),
            "2" to Result.failure(Exception("Service 2 error"))
        )
        
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.first()
        // Should still load successfully with partial data
        assertFalse(state.isLoading)
        assertEquals(2, state.availableServices.size)
        assertEquals(1, state.serviceReports.size) // Only successful service report
        assertEquals("1", state.serviceReports[0].serviceId)
    }
    
    // Helper functions for creating test data
    
    private fun createTestService(
        id: String,
        name: String,
        minAge: Int = 2,
        maxAge: Int = 12,
        maxCapacity: Int = 20,
        currentCapacity: Int = 0
    ): KidsService {
        return KidsService(
            id = id,
            name = name,
            description = "$name service",
            minAge = minAge,
            maxAge = maxAge,
            startTime = "09:00",
            endTime = "10:30",
            location = "Room $id",
            maxCapacity = maxCapacity,
            currentCapacity = currentCapacity,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Staff 1", "Staff 2"),
            createdAt = "2024-01-01T00:00:00Z"
        )
    }
    
    private fun createTestServiceReport(
        serviceId: String,
        serviceName: String,
        currentCheckIns: Int,
        totalCapacity: Int
    ): ServiceReport {
        return ServiceReport(
            serviceId = serviceId,
            serviceName = serviceName,
            totalCapacity = totalCapacity,
            currentCheckIns = currentCheckIns,
            availableSpots = totalCapacity - currentCheckIns,
            checkedInChildren = emptyList(),
            staffMembers = listOf("Staff 1", "Staff 2"),
            generatedAt = "2024-01-01T12:00:00Z"
        )
    }
    
    private fun createTestAttendanceReport(
        startDate: String = "2024-01-01",
        endDate: String = "2024-01-07",
        totalCheckIns: Int = 25,
        uniqueChildren: Int = 15,
        serviceBreakdown: Map<String, Int> = mapOf("1" to 10, "2" to 15),
        dailyBreakdown: Map<String, Int> = mapOf("2024-01-01" to 12, "2024-01-02" to 13)
    ): AttendanceReport {
        return AttendanceReport(
            startDate = startDate,
            endDate = endDate,
            totalCheckIns = totalCheckIns,
            uniqueChildren = uniqueChildren,
            serviceBreakdown = serviceBreakdown,
            dailyBreakdown = dailyBreakdown,
            generatedAt = "2024-01-01T12:00:00Z"
        )
    }
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    var servicesResult: Result<List<KidsService>> = Result.success(emptyList())
    var serviceReportsResult: Map<String, Result<ServiceReport>> = emptyMap()
    var attendanceReportResult: Result<AttendanceReport> = Result.success(
        AttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-07",
            totalCheckIns = 0,
            uniqueChildren = 0,
            serviceBreakdown = emptyMap(),
            dailyBreakdown = emptyMap(),
            generatedAt = "2024-01-01T12:00:00Z"
        )
    )
    
    override suspend fun getAvailableServices(): Result<List<KidsService>> = servicesResult
    
    override suspend fun getServiceReport(serviceId: String): Result<ServiceReport> {
        return serviceReportsResult[serviceId] ?: Result.failure(Exception("Service not found"))
    }
    
    override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<AttendanceReport> {
        return attendanceReportResult
    }
    
    // Unused methods for this test
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = Result.success(emptyList())
    override suspend fun registerChild(child: Child): Result<Child> = Result.success(child)
    override suspend fun updateChild(child: Child): Result<Child> = Result.success(child)
    override suspend fun deleteChild(childId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getChildById(childId: String): Result<Child> = Result.failure(Exception("Not implemented"))
    override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> = Result.success(emptyList())
    override suspend fun getServiceById(serviceId: String): Result<KidsService> = Result.failure(Exception("Not implemented"))
    override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> = Result.success(emptyList())
    override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?): Result<CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<CheckInRecord>> = Result.success(emptyList())
    override suspend fun getCurrentCheckIns(serviceId: String): Result<List<CheckInRecord>> = Result.success(emptyList())
    override suspend fun getAllCurrentCheckIns(): Result<List<CheckInRecord>> = Result.success(emptyList())
    override suspend fun getCheckInRecord(recordId: String): Result<CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {}
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) {}
    override suspend fun unsubscribeFromUpdates() {}
}