package rfm.hillsongptapp.feature.kids.ui.reports

import kotlin.test.*
import rfm.hillsongptapp.feature.kids.domain.model.*

/**
 * Unit tests for ReportsUiState
 * Tests utility methods and computed properties
 */
class ReportsUiStateTest {
    
    @Test
    fun `hasServiceFilter should return true when not all services are selected`() {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids"),
            createTestService("3", "Youth")
        )
        val state = ReportsUiState(
            availableServices = services,
            selectedServices = setOf("1", "2") // Only 2 of 3 selected
        )
        
        // Then
        assertTrue(state.hasServiceFilter())
    }
    
    @Test
    fun `hasServiceFilter should return false when all services are selected`() {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids")
        )
        val state = ReportsUiState(
            availableServices = services,
            selectedServices = setOf("1", "2") // All services selected
        )
        
        // Then
        assertFalse(state.hasServiceFilter())
    }
    
    @Test
    fun `hasServiceFilter should return false when no services are selected`() {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids")
        )
        val state = ReportsUiState(
            availableServices = services,
            selectedServices = emptySet()
        )
        
        // Then
        assertFalse(state.hasServiceFilter())
    }
    
    @Test
    fun `getSelectedServiceNames should return correct service names`() {
        // Given
        val services = listOf(
            createTestService("1", "Toddlers"),
            createTestService("2", "Kids"),
            createTestService("3", "Youth")
        )
        val state = ReportsUiState(
            availableServices = services,
            selectedServices = setOf("1", "3")
        )
        
        // When
        val selectedNames = state.getSelectedServiceNames()
        
        // Then
        assertEquals(2, selectedNames.size)
        assertTrue(selectedNames.contains("Toddlers"))
        assertTrue(selectedNames.contains("Youth"))
        assertFalse(selectedNames.contains("Kids"))
    }
    
    @Test
    fun `hasDateRange should return true when both dates are set`() {
        // Given
        val state = ReportsUiState(
            selectedStartDate = "2024-01-01",
            selectedEndDate = "2024-01-07"
        )
        
        // Then
        assertTrue(state.hasDateRange())
    }
    
    @Test
    fun `hasDateRange should return false when dates are blank`() {
        // Given
        val state = ReportsUiState(
            selectedStartDate = "",
            selectedEndDate = ""
        )
        
        // Then
        assertFalse(state.hasDateRange())
    }
    
    @Test
    fun `getFormattedDateRange should return formatted range`() {
        // Given
        val state = ReportsUiState(
            selectedStartDate = "2024-01-01",
            selectedEndDate = "2024-01-07"
        )
        
        // When
        val formatted = state.getFormattedDateRange()
        
        // Then
        assertEquals("2024-01-01 to 2024-01-07", formatted)
    }
    
    @Test
    fun `getFormattedDateRange should return default message when no range`() {
        // Given
        val state = ReportsUiState()
        
        // When
        val formatted = state.getFormattedDateRange()
        
        // Then
        assertEquals("No date range selected", formatted)
    }
    
    @Test
    fun `hasData should return true when reports are available`() {
        // Given
        val attendanceReport = createTestAttendanceReport()
        val serviceReports = listOf(createTestServiceReport("1", "Toddlers", 5, 20))
        
        val state = ReportsUiState(
            attendanceReport = attendanceReport,
            serviceReports = serviceReports
        )
        
        // Then
        assertTrue(state.hasData())
    }
    
    @Test
    fun `hasData should return false when no reports are available`() {
        // Given
        val state = ReportsUiState()
        
        // Then
        assertFalse(state.hasData())
    }
    
    @Test
    fun `getTotalCapacity should sum all service capacities`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Toddlers", 5, 20),
            createTestServiceReport("2", "Kids", 15, 30),
            createTestServiceReport("3", "Youth", 8, 25)
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // When
        val totalCapacity = state.getTotalCapacity()
        
        // Then
        assertEquals(75, totalCapacity) // 20 + 30 + 25
    }
    
    @Test
    fun `getTotalCurrentCheckIns should sum all current check-ins`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Toddlers", 5, 20),
            createTestServiceReport("2", "Kids", 15, 30),
            createTestServiceReport("3", "Youth", 8, 25)
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // When
        val totalCheckIns = state.getTotalCurrentCheckIns()
        
        // Then
        assertEquals(28, totalCheckIns) // 5 + 15 + 8
    }
    
    @Test
    fun `getOverallCapacityUtilization should calculate correct percentage`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Toddlers", 10, 20), // 50%
            createTestServiceReport("2", "Kids", 30, 40)      // 75%
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // When
        val utilization = state.getOverallCapacityUtilization()
        
        // Then
        assertEquals(66, utilization) // (40/60) * 100 = 66.67% rounded to 66%
    }
    
    @Test
    fun `getOverallCapacityUtilization should return 0 when no capacity`() {
        // Given
        val state = ReportsUiState(serviceReports = emptyList())
        
        // When
        val utilization = state.getOverallCapacityUtilization()
        
        // Then
        assertEquals(0, utilization)
    }
    
    @Test
    fun `getServicesNearCapacity should return services at 90% or higher`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Low", 5, 20),      // 25%
            createTestServiceReport("2", "High", 18, 20),    // 90%
            createTestServiceReport("3", "Full", 20, 20),    // 100%
            createTestServiceReport("4", "Medium", 15, 20)   // 75%
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // When
        val nearCapacity = state.getServicesNearCapacity()
        
        // Then
        assertEquals(2, nearCapacity.size)
        assertTrue(nearCapacity.any { it.serviceId == "2" })
        assertTrue(nearCapacity.any { it.serviceId == "3" })
    }
    
    @Test
    fun `getFullServices should return only services at 100% capacity`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Low", 5, 20),      // 25%
            createTestServiceReport("2", "High", 18, 20),    // 90%
            createTestServiceReport("3", "Full", 20, 20),    // 100%
            createTestServiceReport("4", "AlsoFull", 15, 15) // 100%
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // When
        val fullServices = state.getFullServices()
        
        // Then
        assertEquals(2, fullServices.size)
        assertTrue(fullServices.any { it.serviceId == "3" })
        assertTrue(fullServices.any { it.serviceId == "4" })
    }
    
    @Test
    fun `hasFullServices should return true when services are at capacity`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "Full", 20, 20)
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // Then
        assertTrue(state.hasFullServices())
    }
    
    @Test
    fun `hasFullServices should return false when no services are at capacity`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "NotFull", 18, 20)
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // Then
        assertFalse(state.hasFullServices())
    }
    
    @Test
    fun `hasServicesNearCapacity should return true when services are near capacity`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "NearFull", 18, 20) // 90%
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // Then
        assertTrue(state.hasServicesNearCapacity())
    }
    
    @Test
    fun `hasServicesNearCapacity should return false when no services are near capacity`() {
        // Given
        val serviceReports = listOf(
            createTestServiceReport("1", "NotNearFull", 15, 20) // 75%
        )
        val state = ReportsUiState(serviceReports = serviceReports)
        
        // Then
        assertFalse(state.hasServicesNearCapacity())
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
        uniqueChildren: Int = 15
    ): AttendanceReport {
        return AttendanceReport(
            startDate = startDate,
            endDate = endDate,
            totalCheckIns = totalCheckIns,
            uniqueChildren = uniqueChildren,
            serviceBreakdown = mapOf("1" to 10, "2" to 15),
            dailyBreakdown = mapOf("2024-01-01" to 12, "2024-01-02" to 13),
            generatedAt = "2024-01-01T12:00:00Z"
        )
    }
}