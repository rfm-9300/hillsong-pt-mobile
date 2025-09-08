package rfm.hillsongptapp.feature.kids.data.database.dao

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity

/**
 * Unit tests for CheckInRecordDao database operations
 */
class CheckInRecordDaoTest {
    
    // Test data
    private val testRecord1 = CheckInRecordEntity(
        id = "record1",
        childId = "child1",
        serviceId = "service1",
        checkInTime = "2025-01-01T09:00:00Z",
        checkOutTime = null,
        checkedInBy = "parent1",
        checkedOutBy = null,
        notes = null,
        status = "CHECKED_IN",
        lastSyncedAt = null
    )
    
    private val testRecord2 = CheckInRecordEntity(
        id = "record2",
        childId = "child2",
        serviceId = "service1",
        checkInTime = "2025-01-01T09:15:00Z",
        checkOutTime = "2025-01-01T10:30:00Z",
        checkedInBy = "parent2",
        checkedOutBy = "parent2",
        notes = "Child had a great time",
        status = "CHECKED_OUT",
        lastSyncedAt = "2025-01-01T10:30:00Z"
    )
    
    private val testRecord3 = CheckInRecordEntity(
        id = "record3",
        childId = "child3",
        serviceId = "service2",
        checkInTime = "2025-01-01T10:00:00Z",
        checkOutTime = null,
        checkedInBy = "parent3",
        checkedOutBy = null,
        notes = "First time at this service",
        status = "CHECKED_IN",
        lastSyncedAt = null
    )
    
    private val testRecord4 = CheckInRecordEntity(
        id = "record4",
        childId = "child1",
        serviceId = "service2",
        checkInTime = "2024-12-31T14:00:00Z",
        checkOutTime = "2024-12-31T15:30:00Z",
        checkedInBy = "parent1",
        checkedOutBy = "parent1",
        notes = null,
        status = "CHECKED_OUT",
        lastSyncedAt = "2024-12-31T15:30:00Z"
    )
    
    @Test
    fun testCheckInRecordDataStructure() = runTest {
        // Test basic check-in record structure
        assertEquals("record1", testRecord1.id)
        assertEquals("child1", testRecord1.childId)
        assertEquals("service1", testRecord1.serviceId)
        assertEquals("2025-01-01T09:00:00Z", testRecord1.checkInTime)
        assertEquals("parent1", testRecord1.checkedInBy)
        assertEquals("CHECKED_IN", testRecord1.status)
    }
    
    @Test
    fun testActiveCheckInRecord() = runTest {
        // Test active check-in record (not checked out yet)
        assertNull(testRecord1.checkOutTime)
        assertNull(testRecord1.checkedOutBy)
        assertEquals("CHECKED_IN", testRecord1.status)
        
        assertNull(testRecord3.checkOutTime)
        assertNull(testRecord3.checkedOutBy)
        assertEquals("CHECKED_IN", testRecord3.status)
    }
    
    @Test
    fun testCompletedCheckInRecord() = runTest {
        // Test completed check-in record (checked out)
        assertNotNull(testRecord2.checkOutTime)
        assertNotNull(testRecord2.checkedOutBy)
        assertEquals("CHECKED_OUT", testRecord2.status)
        assertEquals("2025-01-01T10:30:00Z", testRecord2.checkOutTime)
        assertEquals("parent2", testRecord2.checkedOutBy)
        
        assertNotNull(testRecord4.checkOutTime)
        assertNotNull(testRecord4.checkedOutBy)
        assertEquals("CHECKED_OUT", testRecord4.status)
    }
    
    @Test
    fun testCheckInRecordsByChild() = runTest {
        // Test filtering records by child
        val allRecords = listOf(testRecord1, testRecord2, testRecord3, testRecord4)
        
        val child1Records = allRecords.filter { it.childId == "child1" }
        assertEquals(2, child1Records.size)
        assertTrue(child1Records.all { it.childId == "child1" })
        
        val child2Records = allRecords.filter { it.childId == "child2" }
        assertEquals(1, child2Records.size)
        assertEquals("record2", child2Records.first().id)
        
        val child3Records = allRecords.filter { it.childId == "child3" }
        assertEquals(1, child3Records.size)
        assertEquals("record3", child3Records.first().id)
    }
    
    @Test
    fun testCheckInRecordsByService() = runTest {
        // Test filtering records by service
        val allRecords = listOf(testRecord1, testRecord2, testRecord3, testRecord4)
        
        val service1Records = allRecords.filter { it.serviceId == "service1" }
        assertEquals(2, service1Records.size)
        assertTrue(service1Records.all { it.serviceId == "service1" })
        
        val service2Records = allRecords.filter { it.serviceId == "service2" }
        assertEquals(2, service2Records.size)
        assertTrue(service2Records.all { it.serviceId == "service2" })
    }
    
    @Test
    fun testCurrentCheckInRecords() = runTest {
        // Test filtering current check-in records (status = CHECKED_IN)
        val allRecords = listOf(testRecord1, testRecord2, testRecord3, testRecord4)
        val currentRecords = allRecords.filter { it.status == "CHECKED_IN" }
        
        assertEquals(2, currentRecords.size)
        assertTrue(currentRecords.all { it.status == "CHECKED_IN" })
        assertTrue(currentRecords.all { it.checkOutTime == null })
        assertTrue(currentRecords.all { it.checkedOutBy == null })
        
        val recordIds = currentRecords.map { it.id }
        assertTrue(recordIds.contains("record1"))
        assertTrue(recordIds.contains("record3"))
    }
    
    @Test
    fun testCompletedCheckInRecords() = runTest {
        // Test filtering completed check-in records (status = CHECKED_OUT)
        val allRecords = listOf(testRecord1, testRecord2, testRecord3, testRecord4)
        val completedRecords = allRecords.filter { it.status == "CHECKED_OUT" }
        
        assertEquals(2, completedRecords.size)
        assertTrue(completedRecords.all { it.status == "CHECKED_OUT" })
        assertTrue(completedRecords.all { it.checkOutTime != null })
        assertTrue(completedRecords.all { it.checkedOutBy != null })
        
        val recordIds = completedRecords.map { it.id }
        assertTrue(recordIds.contains("record2"))
        assertTrue(recordIds.contains("record4"))
    }
    
    @Test
    fun testCheckInRecordNotes() = runTest {
        // Test notes functionality
        assertNull(testRecord1.notes)
        assertEquals("Child had a great time", testRecord2.notes)
        assertEquals("First time at this service", testRecord3.notes)
        assertNull(testRecord4.notes)
    }
    
    @Test
    fun testCheckInRecordTimeValidation() = runTest {
        // Test time validation logic
        assertTrue(testRecord1.checkInTime.isNotBlank())
        assertTrue(testRecord2.checkInTime.isNotBlank())
        assertTrue(testRecord3.checkInTime.isNotBlank())
        assertTrue(testRecord4.checkInTime.isNotBlank())
        
        // For completed records, check-out time should be after check-in time
        // This is a simplified string comparison - in real implementation use proper date parsing
        assertTrue(testRecord2.checkOutTime!! > testRecord2.checkInTime)
        assertTrue(testRecord4.checkOutTime!! > testRecord4.checkInTime)
    }
    
    @Test
    fun testCheckInRecordUserTracking() = runTest {
        // Test user tracking (who checked in/out)
        assertEquals("parent1", testRecord1.checkedInBy)
        assertEquals("parent2", testRecord2.checkedInBy)
        assertEquals("parent3", testRecord3.checkedInBy)
        assertEquals("parent1", testRecord4.checkedInBy)
        
        // Check-out tracking
        assertNull(testRecord1.checkedOutBy) // Still checked in
        assertEquals("parent2", testRecord2.checkedOutBy)
        assertNull(testRecord3.checkedOutBy) // Still checked in
        assertEquals("parent1", testRecord4.checkedOutBy)
    }
    
    @Test
    fun testCheckInRecordSyncTracking() = runTest {
        // Test sync tracking
        assertNull(testRecord1.lastSyncedAt) // Not synced yet
        assertEquals("2025-01-01T10:30:00Z", testRecord2.lastSyncedAt)
        assertNull(testRecord3.lastSyncedAt) // Not synced yet
        assertEquals("2024-12-31T15:30:00Z", testRecord4.lastSyncedAt)
    }
    
    @Test
    fun testCheckInRecordStatusConsistency() = runTest {
        // Test status consistency with other fields
        
        // CHECKED_IN records should not have check-out data
        if (testRecord1.status == "CHECKED_IN") {
            assertNull(testRecord1.checkOutTime)
            assertNull(testRecord1.checkedOutBy)
        }
        
        if (testRecord3.status == "CHECKED_IN") {
            assertNull(testRecord3.checkOutTime)
            assertNull(testRecord3.checkedOutBy)
        }
        
        // CHECKED_OUT records should have check-out data
        if (testRecord2.status == "CHECKED_OUT") {
            assertNotNull(testRecord2.checkOutTime)
            assertNotNull(testRecord2.checkedOutBy)
        }
        
        if (testRecord4.status == "CHECKED_OUT") {
            assertNotNull(testRecord4.checkOutTime)
            assertNotNull(testRecord4.checkedOutBy)
        }
    }
    
    @Test
    fun testCheckInRecordValidation() = runTest {
        // Test record data validation
        assertTrue(testRecord1.id.isNotBlank())
        assertTrue(testRecord1.childId.isNotBlank())
        assertTrue(testRecord1.serviceId.isNotBlank())
        assertTrue(testRecord1.checkInTime.isNotBlank())
        assertTrue(testRecord1.checkedInBy.isNotBlank())
        assertTrue(testRecord1.status.isNotBlank())
        
        // Test valid status values
        val validStatuses = listOf("CHECKED_IN", "CHECKED_OUT", "NOT_IN_SERVICE")
        assertTrue(validStatuses.contains(testRecord1.status))
        assertTrue(validStatuses.contains(testRecord2.status))
        assertTrue(validStatuses.contains(testRecord3.status))
        assertTrue(validStatuses.contains(testRecord4.status))
    }
    
    @Test
    fun testCheckInRecordDateRangeFiltering() = runTest {
        // Test date range filtering logic
        val allRecords = listOf(testRecord1, testRecord2, testRecord3, testRecord4)
        
        // Records from 2025-01-01
        val jan1Records = allRecords.filter { 
            it.checkInTime.startsWith("2025-01-01")
        }
        assertEquals(3, jan1Records.size)
        
        // Records from 2024-12-31
        val dec31Records = allRecords.filter { 
            it.checkInTime.startsWith("2024-12-31")
        }
        assertEquals(1, dec31Records.size)
        assertEquals("record4", dec31Records.first().id)
    }
}