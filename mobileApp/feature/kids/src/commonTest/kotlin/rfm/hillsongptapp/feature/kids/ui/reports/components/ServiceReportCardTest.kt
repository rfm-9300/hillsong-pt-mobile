package rfm.hillsongptapp.feature.kids.ui.reports.components

import kotlin.test.*
import rfm.hillsongptapp.feature.kids.domain.model.ServiceReport

/**
 * Unit tests for ServiceReportCard component logic
 * Tests capacity calculations and status indicators
 */
class ServiceReportCardTest {
    
    @Test
    fun `service report should calculate capacity correctly`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Toddlers",
            currentCheckIns = 15,
            totalCapacity = 20
        )
        
        // Then
        assertEquals(15, report.currentCheckIns)
        assertEquals(20, report.totalCapacity)
        assertEquals(5, report.availableSpots)
        assertEquals("15/20", report.getCapacityDisplay())
        assertEquals(75, report.getCapacityUtilizationPercent())
        assertFalse(report.isAtCapacity())
        assertTrue(report.hasAvailableSpots())
    }
    
    @Test
    fun `service report should identify full capacity`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Kids",
            currentCheckIns = 30,
            totalCapacity = 30
        )
        
        // Then
        assertTrue(report.isAtCapacity())
        assertFalse(report.hasAvailableSpots())
        assertEquals(0, report.availableSpots)
        assertEquals(100, report.getCapacityUtilizationPercent())
        assertEquals("Full", report.getStatusSummary())
    }
    
    @Test
    fun `service report should identify nearly full capacity`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Youth",
            currentCheckIns = 18,
            totalCapacity = 20
        )
        
        // Then
        assertFalse(report.isAtCapacity())
        assertTrue(report.hasAvailableSpots())
        assertEquals(2, report.availableSpots)
        assertEquals(90, report.getCapacityUtilizationPercent())
        assertEquals("Nearly Full", report.getStatusSummary())
    }
    
    @Test
    fun `service report should identify empty service`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Nursery",
            currentCheckIns = 0,
            totalCapacity = 15
        )
        
        // Then
        assertFalse(report.isAtCapacity())
        assertTrue(report.hasAvailableSpots())
        assertEquals(15, report.availableSpots)
        assertEquals(0, report.getCapacityUtilizationPercent())
        assertEquals("Empty", report.getStatusSummary())
    }
    
    @Test
    fun `service report should identify available service`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Elementary",
            currentCheckIns = 10,
            totalCapacity = 25
        )
        
        // Then
        assertFalse(report.isAtCapacity())
        assertTrue(report.hasAvailableSpots())
        assertEquals(15, report.availableSpots)
        assertEquals(40, report.getCapacityUtilizationPercent())
        assertEquals("Available", report.getStatusSummary())
    }
    
    @Test
    fun `service report should handle edge case with 3 available spots`() {
        // Given - exactly 3 spots available (boundary for "Nearly Full")
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Boundary Test",
            currentCheckIns = 17,
            totalCapacity = 20
        )
        
        // Then
        assertEquals(3, report.availableSpots)
        assertEquals("Nearly Full", report.getStatusSummary())
    }
    
    @Test
    fun `service report should handle edge case with 4 available spots`() {
        // Given - 4 spots available (should be "Available", not "Nearly Full")
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Boundary Test",
            currentCheckIns = 16,
            totalCapacity = 20
        )
        
        // Then
        assertEquals(4, report.availableSpots)
        assertEquals("Available", report.getStatusSummary())
    }
    
    @Test
    fun `service report capacity utilization should be accurate`() {
        // Given
        val report = createTestServiceReport(
            serviceId = "1",
            serviceName = "Precision Test",
            currentCheckIns = 7,
            totalCapacity = 10
        )
        
        // Then
        assertEquals(0.7f, report.getCapacityUtilization())
        assertEquals(70, report.getCapacityUtilizationPercent())
    }
    
    @Test
    fun `service report should handle zero capacity edge case`() {
        // This test ensures the model validation works correctly
        assertFailsWith<IllegalArgumentException> {
            ServiceReport(
                serviceId = "1",
                serviceName = "Invalid",
                totalCapacity = 0, // Invalid - should be positive
                currentCheckIns = 0,
                availableSpots = 0,
                checkedInChildren = emptyList(),
                staffMembers = emptyList(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
    }
    
    @Test
    fun `service report should validate check-ins don't exceed capacity`() {
        // This test ensures the model validation works correctly
        assertFailsWith<IllegalArgumentException> {
            ServiceReport(
                serviceId = "1",
                serviceName = "Invalid",
                totalCapacity = 10,
                currentCheckIns = 15, // Invalid - exceeds capacity
                availableSpots = -5,  // This would be calculated incorrectly
                checkedInChildren = emptyList(),
                staffMembers = emptyList(),
                generatedAt = "2024-01-01T12:00:00Z"
            )
        }
    }
    
    // Helper function for creating test data
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
}