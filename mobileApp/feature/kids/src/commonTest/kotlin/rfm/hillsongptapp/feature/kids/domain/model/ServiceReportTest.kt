package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.*

class ServiceReportTest {
    
    private val mockChild1 = Child(
        id = "child-1",
        parentId = "parent-1",
        name = "Child One",
        dateOfBirth = "2018-01-01",
        emergencyContact = EmergencyContact(
            name = "Parent One",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_IN,
        currentServiceId = "service-1",
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    private val mockChild2 = Child(
        id = "child-2",
        parentId = "parent-2",
        name = "Child Two",
        dateOfBirth = "2019-01-01",
        emergencyContact = EmergencyContact(
            name = "Parent Two",
            phoneNumber = "+1234567891",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_IN,
        currentServiceId = "service-1",
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    private val sampleReport = ServiceReport(
        serviceId = "service-1",
        serviceName = "Kids Church",
        totalCapacity = 20,
        currentCheckIns = 15,
        availableSpots = 5,
        checkedInChildren = listOf(mockChild1, mockChild2).take(15), // Simulate 15 children
        staffMembers = listOf("staff-1", "staff-2"),
        generatedAt = "2025-01-01T10:00:00Z"
    )
    
    @Test
    fun `creates valid service report`() {
        val report = ServiceReport(
            serviceId = "service-1",
            serviceName = "Test Service",
            totalCapacity = 10,
            currentCheckIns = 5,
            availableSpots = 5,
            checkedInChildren = listOf(mockChild1, mockChild2, mockChild1, mockChild2, mockChild1), // 5 children
            staffMembers = listOf("staff-1"),
            generatedAt = "2025-01-01T10:00:00Z"
        )
        
        assertEquals("service-1", report.serviceId)
        assertEquals("Test Service", report.serviceName)
        assertEquals(10, report.totalCapacity)
        assertEquals(5, report.currentCheckIns)
        assertEquals(5, report.availableSpots)
        assertEquals(5, report.checkedInChildren.size)
    }
    
    @Test
    fun `serializes and deserializes correctly`() {
        val json = Json.encodeToString(ServiceReport.serializer(), sampleReport)
        val deserializedReport = Json.decodeFromString(ServiceReport.serializer(), json)
        
        assertEquals(sampleReport.serviceId, deserializedReport.serviceId)
        assertEquals(sampleReport.serviceName, deserializedReport.serviceName)
        assertEquals(sampleReport.totalCapacity, deserializedReport.totalCapacity)
        assertEquals(sampleReport.currentCheckIns, deserializedReport.currentCheckIns)
    }
    
    @Test
    fun `isAtCapacity returns correct values`() {
        val fullReport = sampleReport.copy(currentCheckIns = 20, availableSpots = 0, checkedInChildren = emptyList())
        val partialReport = sampleReport.copy(currentCheckIns = 10, availableSpots = 10, checkedInChildren = emptyList())
        
        assertTrue(fullReport.isAtCapacity())
        assertFalse(partialReport.isAtCapacity())
    }
    
    @Test
    fun `getCapacityUtilization returns correct percentage`() {
        val report = sampleReport.copy(
            totalCapacity = 20,
            currentCheckIns = 15,
            availableSpots = 5,
            checkedInChildren = emptyList()
        )
        
        assertEquals(0.75f, report.getCapacityUtilization())
        assertEquals(75, report.getCapacityUtilizationPercent())
    }
    
    @Test
    fun `hasAvailableSpots returns correct values`() {
        val availableReport = sampleReport.copy(availableSpots = 5, checkedInChildren = emptyList())
        val fullReport = sampleReport.copy(availableSpots = 0, checkedInChildren = emptyList())
        
        assertTrue(availableReport.hasAvailableSpots())
        assertFalse(fullReport.hasAvailableSpots())
    }
    
    @Test
    fun `getCapacityDisplay returns formatted string`() {
        val report = sampleReport.copy(
            currentCheckIns = 12,
            totalCapacity = 20,
            availableSpots = 8,
            checkedInChildren = emptyList()
        )
        
        assertEquals("12/20", report.getCapacityDisplay())
    }
    
    @Test
    fun `getStatusSummary returns correct status`() {
        val fullReport = sampleReport.copy(
            currentCheckIns = 20,
            totalCapacity = 20,
            availableSpots = 0,
            checkedInChildren = emptyList()
        )
        val nearlyFullReport = sampleReport.copy(
            currentCheckIns = 18,
            totalCapacity = 20,
            availableSpots = 2,
            checkedInChildren = emptyList()
        )
        val emptyReport = sampleReport.copy(
            currentCheckIns = 0,
            totalCapacity = 20,
            availableSpots = 20,
            checkedInChildren = emptyList()
        )
        val availableReport = sampleReport.copy(
            currentCheckIns = 10,
            totalCapacity = 20,
            availableSpots = 10,
            checkedInChildren = emptyList()
        )
        
        assertEquals("Full", fullReport.getStatusSummary())
        assertEquals("Nearly Full", nearlyFullReport.getStatusSummary())
        assertEquals("Empty", emptyReport.getStatusSummary())
        assertEquals("Available", availableReport.getStatusSummary())
    }
    
    @Test
    fun `validation fails for blank service ID`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(serviceId = "")
        }
    }
    
    @Test
    fun `validation fails for blank service name`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(serviceName = "")
        }
    }
    
    @Test
    fun `validation fails for non-positive total capacity`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(totalCapacity = 0, currentCheckIns = 0, availableSpots = 0, checkedInChildren = emptyList())
        }
        
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(totalCapacity = -1, currentCheckIns = 0, availableSpots = 0, checkedInChildren = emptyList())
        }
    }
    
    @Test
    fun `validation fails for negative current check-ins`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(currentCheckIns = -1, availableSpots = 21, checkedInChildren = emptyList())
        }
    }
    
    @Test
    fun `validation fails when current check-ins exceed total capacity`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(currentCheckIns = 25, availableSpots = -5, checkedInChildren = emptyList())
        }
    }
    
    @Test
    fun `validation fails for negative available spots`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(availableSpots = -1, currentCheckIns = 21, checkedInChildren = emptyList())
        }
    }
    
    @Test
    fun `validation fails when available spots calculation is incorrect`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(
                totalCapacity = 20,
                currentCheckIns = 15,
                availableSpots = 10, // Should be 5
                checkedInChildren = emptyList()
            )
        }
    }
    
    @Test
    fun `validation fails when checked-in children count doesn't match current check-ins`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(
                currentCheckIns = 10,
                availableSpots = 10,
                checkedInChildren = listOf(mockChild1) // Only 1 child but currentCheckIns is 10
            )
        }
    }
    
    @Test
    fun `validation fails for blank generated at timestamp`() {
        assertFailsWith<IllegalArgumentException> {
            sampleReport.copy(generatedAt = "")
        }
    }
    
    @Test
    fun `edge case - zero capacity utilization`() {
        val emptyReport = sampleReport.copy(
            currentCheckIns = 0,
            availableSpots = 20,
            checkedInChildren = emptyList()
        )
        
        assertEquals(0.0f, emptyReport.getCapacityUtilization())
        assertEquals(0, emptyReport.getCapacityUtilizationPercent())
    }
    
    @Test
    fun `edge case - full capacity utilization`() {
        val fullReport = sampleReport.copy(
            currentCheckIns = 20,
            availableSpots = 0,
            checkedInChildren = emptyList()
        )
        
        assertEquals(1.0f, fullReport.getCapacityUtilization())
        assertEquals(100, fullReport.getCapacityUtilizationPercent())
    }
}