package rfm.hillsongptapp.feature.kids.ui.reports

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.feature.kids.ui.MockKidsRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for ReportsViewModel
 * Tests report loading, filtering, and UI state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val mockKidsRepository = MockKidsRepository()
    
    private lateinit var viewModel: ReportsViewModel
    
    // Test data
    private val testServices = listOf(
        KidsService(
            id = "service-1",
            name = "Kids Church",
            description = "Sunday kids service",
            minAge = 5,
            maxAge = 12,
            startTime = "2024-01-07T10:00:00Z",
            endTime = "2024-01-07T11:30:00Z",
            location = "Kids Room A",
            maxCapacity = 20,
            currentCapacity = 15,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-1", "staff-2"),
            createdAt = "2024-01-01T09:00:00Z"
        ),
        KidsService(
            id = "service-2",
            name = "Toddler Time",
            description = "Service for toddlers",
            minAge = 2,
            maxAge = 4,
            startTime = "2024-01-07T10:00:00Z",
            endTime = "2024-01-07T11:00:00Z",
            location = "Toddler Room",
            maxCapacity = 15,
            currentCapacity = 10,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-3"),
            createdAt = "2024-01-01T09:00:00Z"
        )
    )
    
    private val testServiceReports = listOf(
        ServiceReport(
            serviceId = "service-1",
            serviceName = "Kids Church",
            totalCapacity = 20,
            currentCheckIns = 15,
            availableSpots = 5,
            checkedInChildren = emptyList(),
            staffMembers = listOf("staff-1", "staff-2"),
            generatedAt = "2024-01-07T12:00:00Z"
        ),
        ServiceReport(
            serviceId = "service-2",
            serviceName = "Toddler Time",
            totalCapacity = 15,
            currentCheckIns = 10,
            availableSpots = 5,
            checkedInChildren = emptyList(),
            staffMembers = listOf("staff-3"),
            generatedAt = "2024-01-07T12:00:00Z"
        )
    )
    
    private val testAttendanceReport = AttendanceReport(
        startDate = "2024-01-01",
        endDate = "2024-01-07",
        totalCheckIns = 50,
        uniqueChildren = 25,
        serviceBreakdown = mapOf(
            "service-1" to 30,
            "service-2" to 20
        ),
        dailyBreakdown = mapOf(
            "2024-01-01" to 10,
            "2024-01-02" to 8,
            "2024-01-03" to 12,
            "2024-01-04" to 0,
            "2024-01-05" to 0,
            "2024-01-06" to 15,
            "2024-01-07" to 5
        ),
        generatedAt = "2024-01-07T12:00:00Z"
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup mock responses
        mockKidsRepository.getAvailableServicesResult = KidsResult.Success(testServices)
        mockKidsRepository.getServiceReportResult = KidsResult.Success(testServiceReports[0])
        mockKidsRepository.getAttendanceReportResult = KidsResult.Success(testAttendanceReport)
        
        viewModel = ReportsViewModel(mockKidsRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has default date range set`() {
        val initialState = viewModel.uiState.value
        
        assertTrue(initialState.selectedStartDate.isNotBlank())
        assertTrue(initialState.selectedEndDate.isNotBlank())
        assertTrue(initialState.availableServices.isEmpty())
        assertNull(initialState.attendanceReport)
        assertTrue(initialState.serviceReports.isEmpty())
    }
    
    @Test
    fun `loadInitialData loads services and reports successfully`() = runTest {
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.availableServices.size)
        assertEquals(2, state.selectedServices.size) // All services selected by default
        assertNotNull(state.attendanceReport)
        assertNull(state.error)
    }
    
    @Test
    fun `loadInitialData handles services loading error`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.Error("Failed to load services")
        
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.availableServices.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load services"))
    }
    
    @Test
    fun `loadInitialData handles network error`() = runTest {
        // Given
        mockKidsRepository.getAvailableServicesResult = KidsResult.NetworkError("Network connection failed")
        
        // When
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error loading services"))
    }
    
    @Test
    fun `updateDateRange updates state and reloads attendance report`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val newStartDate = "2024-01-15"
        val newEndDate = "2024-01-21"
        
        // When
        viewModel.updateDateRange(newStartDate, newEndDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(newStartDate, state.selectedStartDate)
        assertEquals(newEndDate, state.selectedEndDate)
        
        // Verify that attendance report was requested with new dates
        assertTrue(mockKidsRepository.getAttendanceReportCalls.any { 
            it.first == newStartDate && it.second == newEndDate 
        })
    }
    
    @Test
    fun `updateServiceFilter updates selected services and reloads report`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val selectedServices = setOf("service-1")
        
        // When
        viewModel.updateServiceFilter(selectedServices)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(selectedServices, state.selectedServices)
        
        // Attendance report should be filtered to only include selected service
        val report = state.attendanceReport
        assertNotNull(report)
        assertTrue(report.serviceBreakdown.keys.all { selectedServices.contains(it) })
    }
    
    @Test
    fun `selectService updates selected service ID`() {
        // When
        viewModel.selectService("service-1")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("service-1", state.selectedServiceId)
    }
    
    @Test
    fun `exportReport shows success status temporarily`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.exportReport()
        
        // Then (immediately after export)
        val stateAfterExport = viewModel.uiState.value
        assertNotNull(stateAfterExport.exportStatus)
        assertTrue(stateAfterExport.exportStatus!!.contains("exported successfully"))
        
        // When (after delay)
        testDispatcher.scheduler.advanceTimeBy(3100) // Advance past the 3 second delay
        
        // Then (status should be cleared)
        val stateAfterDelay = viewModel.uiState.value
        assertNull(stateAfterDelay.exportStatus)
    }
    
    @Test
    fun `refreshData reloads all data`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Clear previous calls
        mockKidsRepository.getAttendanceReportCalls.clear()
        
        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(mockKidsRepository.getAttendanceReportCalls.isNotEmpty())
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.attendanceReport)
    }
    
    @Test
    fun `attendance report filtering works correctly`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When - Filter to only service-1
        viewModel.updateServiceFilter(setOf("service-1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        val report = state.attendanceReport
        assertNotNull(report)
        
        // Should only include service-1 in breakdown
        assertEquals(1, report.serviceBreakdown.size)
        assertTrue(report.serviceBreakdown.containsKey("service-1"))
        assertFalse(report.serviceBreakdown.containsKey("service-2"))
        
        // Total check-ins should be filtered
        assertEquals(30, report.totalCheckIns) // Only service-1's count
    }
    
    @Test
    fun `UI state computed properties work correctly`() = runTest {
        // Given
        viewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val state = viewModel.uiState.value
        
        // Then
        assertTrue(state.hasDateRange())
        assertTrue(state.hasData())
        assertEquals(2, state.getSelectedServiceNames().size)
        
        // Test with service filter
        viewModel.updateServiceFilter(setOf("service-1"))
        val stateWithFilter = viewModel.uiState.value
        assertTrue(stateWithFilter.hasServiceFilter())
        assertEquals(1, stateWithFilter.getSelectedServiceNames().size)
        assertEquals("Kids Church", stateWithFilter.getSelectedServiceNames()[0])
        
        // Test formatted date range
        assertTrue(stateWithFilter.getFormattedDateRange().contains("to"))
    }
    
    @Test
    fun `service report capacity calculations work correctly`() = runTest {
        // Given - Mock service reports with different capacity levels
        val highCapacityReport = ServiceReport(
            serviceId = "service-1",
            serviceName = "Kids Church",
            totalCapacity = 20,
            currentCheckIns = 19, // 95% capacity
            availableSpots = 1,
            checkedInChildren = emptyList(),
            staffMembers = listOf("staff-1"),
            generatedAt = "2024-01-07T12:00:00Z"
        )
        
        val fullCapacityReport = ServiceReport(
            serviceId = "service-2",
            serviceName = "Toddler Time",
            totalCapacity = 15,
            currentCheckIns = 15, // 100% capacity
            availableSpots = 0,
            checkedInChildren = emptyList(),
            staffMembers = listOf("staff-2"),
            generatedAt = "2024-01-07T12:00:00Z"
        )
        
        // Create state with these reports
        val stateWithReports = ReportsUiState(
            serviceReports = listOf(highCapacityReport, fullCapacityReport)
        )
        
        // Then
        assertEquals(35, stateWithReports.getTotalCapacity())
        assertEquals(34, stateWithReports.getTotalCurrentCheckIns())
        assertEquals(97, stateWithReports.getOverallCapacityUtilization()) // 34/35 = 97%
        
        assertEquals(2, stateWithReports.getServicesNearCapacity().size) // Both >= 90%
        assertEquals(1, stateWithReports.getFullServices().size) // Only service-2 is full
        
        assertTrue(stateWithReports.hasFullServices())
        assertTrue(stateWithReports.hasServicesNearCapacity())
    }
}