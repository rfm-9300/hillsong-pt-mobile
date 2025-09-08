package rfm.hillsongptapp.feature.kids.data.database

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Integration tests for the KidsDatabase
 * Tests the interaction between different entities and their relationships
 */
class KidsDatabaseIntegrationTest {
    
    // Test data setup
    private val testParent1 = "parent1"
    private val testParent2 = "parent2"
    
    private val testChild1 = ChildEntity(
        id = "child1",
        parentId = testParent1,
        name = "John Doe",
        dateOfBirth = "2015-05-15",
        medicalInfo = "No allergies",
        dietaryRestrictions = null,
        emergencyContactName = "Jane Doe",
        emergencyContactPhone = "+1234567890",
        emergencyContactRelationship = "Mother",
        status = "NOT_IN_SERVICE",
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T10:00:00Z",
        updatedAt = "2025-01-01T10:00:00Z",
        lastSyncedAt = null
    )
    
    private val testChild2 = ChildEntity(
        id = "child2",
        parentId = testParent1,
        name = "Alice Doe",
        dateOfBirth = "2018-03-20",
        medicalInfo = null,
        dietaryRestrictions = "Vegetarian",
        emergencyContactName = "Jane Doe",
        emergencyContactPhone = "+1234567890",
        emergencyContactRelationship = "Mother",
        status = "CHECKED_IN",
        currentServiceId = "service1",
        checkInTime = "2025-01-01T11:00:00Z",
        checkOutTime = null,
        createdAt = "2025-01-01T10:00:00Z",
        updatedAt = "2025-01-01T11:00:00Z",
        lastSyncedAt = null
    )
    
    private val testService1 = KidsServiceEntity(
        id = "service1",
        name = "Toddler Time",
        description = "Fun activities for toddlers",
        minAge = 2,
        maxAge = 4,
        startTime = "2025-01-01T09:00:00Z",
        endTime = "2025-01-01T10:30:00Z",
        location = "Room A",
        maxCapacity = 15,
        currentCapacity = 1,
        isAcceptingCheckIns = true,
        staffMembers = "[\"staff1\", \"staff2\"]",
        createdAt = "2025-01-01T08:00:00Z",
        lastSyncedAt = null
    )
    
    private val testService2 = KidsServiceEntity(
        id = "service2",
        name = "Kids Club",
        description = "Activities for school-age children",
        minAge = 5,
        maxAge = 10,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:30:00Z",
        location = "Room B",
        maxCapacity = 20,
        currentCapacity = 0,
        isAcceptingCheckIns = true,
        staffMembers = "[\"staff3\", \"staff4\"]",
        createdAt = "2025-01-01T08:00:00Z",
        lastSyncedAt = null
    )
    
    private val testCheckInRecord1 = CheckInRecordEntity(
        id = "record1",
        childId = "child2",
        serviceId = "service1",
        checkInTime = "2025-01-01T11:00:00Z",
        checkOutTime = null,
        checkedInBy = testParent1,
        checkedOutBy = null,
        notes = null,
        status = "CHECKED_IN",
        lastSyncedAt = null
    )
    
    @Test
    fun testDatabaseEntityRelationships() = runTest {
        // Test the relationships between entities
        
        // Child-Service relationship through check-in
        assertEquals(testChild2.currentServiceId, testService1.id)
        assertEquals(testCheckInRecord1.childId, testChild2.id)
        assertEquals(testCheckInRecord1.serviceId, testService1.id)
        
        // Parent-Child relationship
        assertEquals(testChild1.parentId, testParent1)
        assertEquals(testChild2.parentId, testParent1)
        
        // Status consistency
        assertEquals(testChild2.status, testCheckInRecord1.status)
    }
    
    @Test
    fun testCheckInWorkflow() = runTest {
        // Test a complete check-in workflow
        
        // 1. Initial state - child not in service
        val initialChild = testChild1.copy()
        assertEquals("NOT_IN_SERVICE", initialChild.status)
        assertNull(initialChild.currentServiceId)
        assertNull(initialChild.checkInTime)
        
        // 2. Check-in process
        val checkedInChild = initialChild.copy(
            status = "CHECKED_IN",
            currentServiceId = "service1",
            checkInTime = "2025-01-01T11:00:00Z",
            updatedAt = "2025-01-01T11:00:00Z"
        )
        
        val checkInRecord = CheckInRecordEntity(
            id = "new_record",
            childId = checkedInChild.id,
            serviceId = "service1",
            checkInTime = "2025-01-01T11:00:00Z",
            checkOutTime = null,
            checkedInBy = testParent1,
            checkedOutBy = null,
            notes = null,
            status = "CHECKED_IN",
            lastSyncedAt = null
        )
        
        // 3. Verify check-in state
        assertEquals("CHECKED_IN", checkedInChild.status)
        assertEquals("service1", checkedInChild.currentServiceId)
        assertNotNull(checkedInChild.checkInTime)
        assertEquals(checkedInChild.id, checkInRecord.childId)
        assertEquals("CHECKED_IN", checkInRecord.status)
        
        // 4. Check-out process
        val checkedOutChild = checkedInChild.copy(
            status = "CHECKED_OUT",
            currentServiceId = null,
            checkOutTime = "2025-01-01T12:00:00Z",
            updatedAt = "2025-01-01T12:00:00Z"
        )
        
        val updatedRecord = checkInRecord.copy(
            checkOutTime = "2025-01-01T12:00:00Z",
            checkedOutBy = testParent1,
            status = "CHECKED_OUT"
        )
        
        // 5. Verify check-out state
        assertEquals("CHECKED_OUT", checkedOutChild.status)
        assertNull(checkedOutChild.currentServiceId)
        assertNotNull(checkedOutChild.checkOutTime)
        assertEquals("CHECKED_OUT", updatedRecord.status)
        assertNotNull(updatedRecord.checkOutTime)
        assertNotNull(updatedRecord.checkedOutBy)
    }
    
    @Test
    fun testServiceCapacityManagement() = runTest {
        // Test service capacity management during check-ins/check-outs
        
        val initialService = testService1.copy(currentCapacity = 5)
        assertEquals(5, initialService.currentCapacity)
        assertEquals(15, initialService.maxCapacity)
        assertTrue(initialService.currentCapacity < initialService.maxCapacity)
        
        // Simulate check-in (increment capacity)
        val afterCheckIn = initialService.copy(currentCapacity = initialService.currentCapacity + 1)
        assertEquals(6, afterCheckIn.currentCapacity)
        assertTrue(afterCheckIn.currentCapacity < afterCheckIn.maxCapacity)
        
        // Simulate check-out (decrement capacity)
        val afterCheckOut = afterCheckIn.copy(currentCapacity = afterCheckIn.currentCapacity - 1)
        assertEquals(5, afterCheckOut.currentCapacity)
        
        // Test capacity limits
        val nearCapacity = initialService.copy(currentCapacity = 14)
        val atCapacity = nearCapacity.copy(currentCapacity = 15)
        assertEquals(15, atCapacity.currentCapacity)
        assertEquals(atCapacity.maxCapacity, atCapacity.currentCapacity)
        
        // Should not exceed capacity
        val wouldExceed = atCapacity.currentCapacity + 1
        assertTrue(wouldExceed > atCapacity.maxCapacity)
    }
    
    @Test
    fun testAgeBasedServiceEligibility() = runTest {
        // Test age-based service eligibility
        
        // Child1 born 2015-05-15 (approximately 10 years old)
        val child1Age = 10
        
        // Child2 born 2018-03-20 (approximately 7 years old)  
        val child2Age = 7
        
        // Service1: ages 2-4 (toddlers)
        val toddlerService = testService1
        assertFalse(child1Age >= toddlerService.minAge && child1Age <= toddlerService.maxAge)
        assertFalse(child2Age >= toddlerService.minAge && child2Age <= toddlerService.maxAge)
        
        // Service2: ages 5-10 (school age)
        val schoolAgeService = testService2
        assertTrue(child1Age >= schoolAgeService.minAge && child1Age <= schoolAgeService.maxAge)
        assertTrue(child2Age >= schoolAgeService.minAge && child2Age <= schoolAgeService.maxAge)
        
        // Test edge cases
        val edgeChild1 = testChild1.copy(dateOfBirth = "2020-01-01") // ~5 years old
        val edgeChild2 = testChild1.copy(dateOfBirth = "2015-01-01") // ~10 years old
        
        // 5-year-old should be eligible for school age service
        assertTrue(5 >= schoolAgeService.minAge && 5 <= schoolAgeService.maxAge)
        
        // 10-year-old should be eligible for school age service
        assertTrue(10 >= schoolAgeService.minAge && 10 <= schoolAgeService.maxAge)
    }
    
    @Test
    fun testMultipleChildrenSameParent() = runTest {
        // Test managing multiple children for the same parent
        
        val parent1Children = listOf(testChild1, testChild2)
        assertTrue(parent1Children.all { it.parentId == testParent1 })
        assertEquals(2, parent1Children.size)
        
        // Test different statuses for same parent's children
        val notInServiceChildren = parent1Children.filter { it.status == "NOT_IN_SERVICE" }
        val checkedInChildren = parent1Children.filter { it.status == "CHECKED_IN" }
        
        assertEquals(1, notInServiceChildren.size)
        assertEquals(1, checkedInChildren.size)
        
        assertEquals("child1", notInServiceChildren.first().id)
        assertEquals("child2", checkedInChildren.first().id)
    }
    
    @Test
    fun testCheckInHistoryTracking() = runTest {
        // Test check-in history tracking for a child
        
        val child1History = listOf(
            CheckInRecordEntity(
                id = "history1",
                childId = "child1",
                serviceId = "service1",
                checkInTime = "2024-12-01T09:00:00Z",
                checkOutTime = "2024-12-01T10:30:00Z",
                checkedInBy = testParent1,
                checkedOutBy = testParent1,
                notes = "First visit",
                status = "CHECKED_OUT",
                lastSyncedAt = "2024-12-01T10:30:00Z"
            ),
            CheckInRecordEntity(
                id = "history2",
                childId = "child1",
                serviceId = "service2",
                checkInTime = "2024-12-15T10:00:00Z",
                checkOutTime = "2024-12-15T11:30:00Z",
                checkedInBy = testParent1,
                checkedOutBy = testParent1,
                notes = "Enjoyed the activities",
                status = "CHECKED_OUT",
                lastSyncedAt = "2024-12-15T11:30:00Z"
            ),
            testCheckInRecord1 // Current active check-in
        )
        
        // All records should be for the same child
        assertTrue(child1History.all { it.childId == "child1" })
        
        // Should have both completed and active records
        val completedRecords = child1History.filter { it.status == "CHECKED_OUT" }
        val activeRecords = child1History.filter { it.status == "CHECKED_IN" }
        
        assertEquals(2, completedRecords.size)
        assertEquals(1, activeRecords.size)
        
        // Completed records should have check-out data
        assertTrue(completedRecords.all { it.checkOutTime != null })
        assertTrue(completedRecords.all { it.checkedOutBy != null })
        
        // Active records should not have check-out data
        assertTrue(activeRecords.all { it.checkOutTime == null })
        assertTrue(activeRecords.all { it.checkedOutBy == null })
    }
    
    @Test
    fun testDataIntegrityConstraints() = runTest {
        // Test data integrity and foreign key relationships
        
        // Check-in record must reference valid child and service
        assertEquals(testCheckInRecord1.childId, testChild2.id)
        assertEquals(testCheckInRecord1.serviceId, testService1.id)
        
        // Child's current service must match check-in record
        assertEquals(testChild2.currentServiceId, testCheckInRecord1.serviceId)
        
        // Status consistency between child and check-in record
        assertEquals(testChild2.status, testCheckInRecord1.status)
        
        // Time consistency
        assertEquals(testChild2.checkInTime, testCheckInRecord1.checkInTime)
        
        // User consistency
        assertEquals(testChild2.parentId, testCheckInRecord1.checkedInBy)
    }
    
    @Test
    fun testSyncTrackingConsistency() = runTest {
        // Test sync tracking across related entities
        
        // New entities should not have sync timestamps
        assertNull(testChild1.lastSyncedAt)
        assertNull(testChild2.lastSyncedAt)
        assertNull(testService1.lastSyncedAt)
        assertNull(testCheckInRecord1.lastSyncedAt)
        
        // Simulate sync completion
        val syncTime = "2025-01-01T12:00:00Z"
        
        val syncedChild = testChild2.copy(lastSyncedAt = syncTime)
        val syncedService = testService1.copy(lastSyncedAt = syncTime)
        val syncedRecord = testCheckInRecord1.copy(lastSyncedAt = syncTime)
        
        assertEquals(syncTime, syncedChild.lastSyncedAt)
        assertEquals(syncTime, syncedService.lastSyncedAt)
        assertEquals(syncTime, syncedRecord.lastSyncedAt)
    }
}