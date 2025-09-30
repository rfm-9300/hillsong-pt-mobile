package rfm.hillsongptapp.feature.kids.integration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.feature.kids.ui.MockKidsRepository
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInViewModel
import rfm.hillsongptapp.feature.kids.ui.services.ServicesViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsViewModel
import rfm.hillsongptapp.feature.kids.ui.MockAuthRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end integration tests for the kids feature
 * Tests complete user workflows across multiple ViewModels and components
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KidsFeatureEndToEndTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val mockKidsRepository = MockKidsRepository()
    private val mockAuthRepository = MockAuthRepository()
    
    private lateinit var servicesViewModel: ServicesViewModel
    private lateinit var checkInViewModel: CheckInViewModel
    private lateinit var reportsViewModel: ReportsViewModel
    
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
        dateOfBirth = "2015-05-15", // 8-9 years old
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
            currentCapacity = 5,
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
            currentCapacity = 15, // At capacity
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-3"),
            createdAt = "2024-01-01T09:00:00Z"
        )
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup common mock responses
        mockKidsRepository.getAvailableServicesResult = KidsResult.Success(testServices)
        mockKidsRepository.getServicesAcceptingCheckInsResult = KidsResult.Success(
            testServices.filter { it.canAcceptCheckIn() }
        )
        
        // Initialize ViewModels
        servicesViewModel = ServicesViewModel(mockKidsRepository)
        checkInViewModel = CheckInViewModel(mockKidsRepository, mockAuthRepository)
        reportsViewModel = ReportsViewModel(mockKidsRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `complete service discovery and check-in workflow`() = runTest {
        // Step 1: User opens services screen and views available services
        testDispatcher.scheduler.advanceUntilIdle()
        
        val servicesState = servicesViewModel.uiState.value
        assertFalse(servicesState.isLoading)
        assertEquals(2, servicesState.services.size)
        assertEquals(2, servicesState.filteredServices.size)
        
        // Step 2: User selects a child to filter services
        servicesViewModel.setSelectedChild(testChild)
        
        val servicesStateWithChild = servicesViewModel.uiState.value
        assertNotNull(servicesStateWithChild.selectedChild)
        assertEquals(testChild.id, servicesStateWithChild.selectedChild?.id)
        
        // Services should be filtered/sorted for the child's age
        val eligibleServices = servicesViewModel.getServicesForChild(testChild)
        assertEquals(1, eligibleServices.size) // Only service-1 is eligible for 8-9 year old
        assertEquals("service-1", eligibleServices[0].id)
        
        // Step 3: User proceeds to check-in screen for the child
        mockKidsRepository.getChildByIdResult = KidsResult.Success(testChild)
        
        checkInViewModel.loadChildAndEligibleServices(testChild.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val checkInState = checkInViewModel.uiState.value
        assertFalse(checkInState.isLoading)
        assertNotNull(checkInState.child)
        assertEquals(testChild.id, checkInState.child?.id)
        assertEquals(1, checkInState.eligibleServices.size)
        assertEquals("service-1", checkInState.eligibleServices[0].id)
        
        // Step 4: User selects a service and initiates check-in
        val selectedService = checkInState.eligibleServices[0]
        checkInViewModel.selectService(selectedService)
        checkInViewModel.showCheckInConfirmation()
        
        val stateWithSelection = checkInViewModel.uiState.value
        assertNotNull(stateWithSelection.selectedService)
        assertTrue(stateWithSelection.showConfirmationDialog)
        
        // Step 5: User confirms check-in
        mockKidsRepository.checkInChildResult = KidsResult.Success(
            rfm.hillsongptapp.core.data.model.CheckInRecord(
                id = "record-1",
                childId = testChild.id,
                serviceId = selectedService.id,
                checkInTime = "2024-01-07T10:00:00Z",
                checkOutTime = null,
                checkedInBy = "staff-1",
                checkedOutBy = null,
                notes = "End-to-end test check-in",
                status = CheckInStatus.CHECKED_IN
            )
        )
        
        checkInViewModel.checkInChild("End-to-end test check-in")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalCheckInState = checkInViewModel.uiState.value
        assertFalse(finalCheckInState.isCheckingIn)
        assertTrue(finalCheckInState.checkInSuccess)
        assertFalse(finalCheckInState.showConfirmationDialog)
        
        // Verify repository was called with correct parameters
        assertTrue(mockKidsRepository.checkInChildCalls.isNotEmpty())
        val checkInCall = mockKidsRepository.checkInChildCalls.last()
        assertEquals(testChild.id, checkInCall.first)
        assertEquals(selectedService.id, checkInCall.second)
    }
    
    @Test
    fun `service filtering workflow works correctly`() = runTest {
        // Step 1: Load services
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialState = servicesViewModel.uiState.value
        assertEquals(2, initialState.filteredServices.size)
        
        // Step 2: Apply availability filter to show only available services
        val availabilityFilter = rfm.hillsongptapp.feature.kids.ui.services.ServiceFilters(
            availability = rfm.hillsongptapp.feature.kids.ui.services.ServiceFilters.Availability.AVAILABLE_ONLY
        )
        servicesViewModel.updateFilters(availabilityFilter)
        
        val filteredState = servicesViewModel.uiState.value
        assertEquals(1, filteredState.filteredServices.size) // Only service-1 has availability
        assertEquals("service-1", filteredState.filteredServices[0].id)
        
        // Step 3: Apply age range filter
        val ageFilter = rfm.hillsongptapp.feature.kids.ui.services.ServiceFilters(
            minAge = 5,
            maxAge = 10
        )
        servicesViewModel.updateFilters(ageFilter)
        
        val ageFilteredState = servicesViewModel.uiState.value
        assertEquals(1, ageFilteredState.filteredServices.size) // Only service-1 matches age range
        assertEquals("service-1", ageFilteredState.filteredServices[0].id)
        
        // Step 4: Clear filters
        servicesViewModel.clearFilters()
        
        val clearedState = servicesViewModel.uiState.value
        assertEquals(2, clearedState.filteredServices.size) // All services shown again
        assertFalse(clearedState.hasActiveFilters)
    }
    
    @Test
    fun `reporting workflow generates comprehensive reports`() = runTest {
        // Setup mock data for reports
        val mockServiceReport = rfm.hillsongptapp.core.data.model.ServiceReport(
            serviceId = "service-1",
            serviceName = "Kids Church",
            totalCapacity = 20,
            currentCheckIns = 15,
            availableSpots = 5,
            checkedInChildren = emptyList(),
            staffMembers = listOf("staff-1", "staff-2"),
            generatedAt = "2024-01-07T12:00:00Z"
        )
        
        val mockAttendanceReport = rfm.hillsongptapp.core.data.model.AttendanceReport(
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
        
        mockKidsRepository.getServiceReportResult = KidsResult.Success(mockServiceReport)
        mockKidsRepository.getAttendanceReportResult = KidsResult.Success(mockAttendanceReport)
        
        // Step 1: Load initial reports data
        reportsViewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val initialState = reportsViewModel.uiState.value
        assertFalse(initialState.isLoading)
        assertEquals(2, initialState.availableServices.size)
        assertNotNull(initialState.attendanceReport)
        assertEquals(50, initialState.attendanceReport?.totalCheckIns)
        
        // Step 2: Filter reports by service
        reportsViewModel.updateServiceFilter(setOf("service-1"))
        testDispatcher.scheduler.advanceUntilIdle()
        
        val filteredState = reportsViewModel.uiState.value
        assertEquals(setOf("service-1"), filteredState.selectedServices)
        
        // Attendance report should be filtered
        val filteredReport = filteredState.attendanceReport
        assertNotNull(filteredReport)
        assertEquals(30, filteredReport.totalCheckIns) // Only service-1's count
        assertTrue(filteredReport.serviceBreakdown.containsKey("service-1"))
        assertFalse(filteredReport.serviceBreakdown.containsKey("service-2"))
        
        // Step 3: Update date range
        reportsViewModel.updateDateRange("2024-01-15", "2024-01-21")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val dateUpdatedState = reportsViewModel.uiState.value
        assertEquals("2024-01-15", dateUpdatedState.selectedStartDate)
        assertEquals("2024-01-21", dateUpdatedState.selectedEndDate)
        
        // Verify repository was called with new date range
        assertTrue(mockKidsRepository.getAttendanceReportCalls.any { 
            it.first == "2024-01-15" && it.second == "2024-01-21" 
        })
        
        // Step 4: Export report
        reportsViewModel.exportReport()
        
        val exportState = reportsViewModel.uiState.value
        assertNotNull(exportState.exportStatus)
        assertTrue(exportState.exportStatus!!.contains("exported successfully"))
    }
    
    @Test
    fun `error handling works across ViewModels`() = runTest {
        // Test error handling in ServicesViewModel
        mockKidsRepository.getAvailableServicesResult = KidsResult.Error("Services unavailable")
        
        servicesViewModel.loadServices()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val servicesErrorState = servicesViewModel.uiState.value
        assertFalse(servicesErrorState.isLoading)
        assertNotNull(servicesErrorState.error)
        assertTrue(servicesErrorState.error!!.contains("Services unavailable"))
        
        // Clear error
        servicesViewModel.clearError()
        val clearedServicesState = servicesViewModel.uiState.value
        assertEquals(null, clearedServicesState.error)
        
        // Test error handling in CheckInViewModel
        mockKidsRepository.getChildByIdResult = KidsResult.NetworkError("Network connection failed")
        
        checkInViewModel.loadChildAndEligibleServices("child-1")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val checkInErrorState = checkInViewModel.uiState.value
        assertFalse(checkInErrorState.isLoading)
        assertNotNull(checkInErrorState.error)
        assertTrue(checkInErrorState.error!!.contains("Network error"))
        
        // Clear error
        checkInViewModel.clearError()
        val clearedCheckInState = checkInViewModel.uiState.value
        assertEquals(null, clearedCheckInState.error)
        
        // Test error handling in ReportsViewModel
        mockKidsRepository.getAvailableServicesResult = KidsResult.NetworkError("Reports service unavailable")
        
        reportsViewModel.loadInitialData()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val reportsErrorState = reportsViewModel.uiState.value
        assertFalse(reportsErrorState.isLoading)
        assertNotNull(reportsErrorState.error)
        assertTrue(reportsErrorState.error!!.contains("Reports service unavailable"))
    }
    
    @Test
    fun `UI state consistency is maintained across operations`() = runTest {
        // Load initial data
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify initial states are consistent
        val servicesState = servicesViewModel.uiState.value
        val reportsState = reportsViewModel.uiState.value
        
        assertEquals(2, servicesState.services.size)
        assertEquals(2, reportsState.availableServices.size)
        
        // Both ViewModels should have the same services data
        assertEquals(servicesState.services.map { it.id }.toSet(), 
                    reportsState.availableServices.map { it.id }.toSet())
        
        // Test computed properties work correctly
        assertTrue(servicesState.availableServices.isNotEmpty())
        assertTrue(reportsState.hasData())
        
        // Test filtering consistency
        servicesViewModel.setSelectedChild(testChild)
        val servicesWithChild = servicesViewModel.uiState.value
        
        val eligibleServices = servicesWithChild.eligibleServices
        assertEquals(1, eligibleServices.size) // Child is eligible for service-1 only
        assertEquals("service-1", eligibleServices[0].id)
    }
}