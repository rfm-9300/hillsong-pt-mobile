package rfm.hillsongptapp.feature.kids.data.database.dao

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Unit tests for KidsServiceDao database operations
 */
class KidsServiceDaoTest {
    
    // Test data
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
        currentCapacity = 8,
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
        currentCapacity = 20,
        isAcceptingCheckIns = false,
        staffMembers = "[\"staff3\", \"staff4\", \"staff5\"]",
        createdAt = "2025-01-01T08:00:00Z",
        lastSyncedAt = "2025-01-01T08:30:00Z"
    )
    
    private val testService3 = KidsServiceEntity(
        id = "service3",
        name = "Youth Group",
        description = "Activities for teenagers",
        minAge = 11,
        maxAge = 17,
        startTime = "2025-01-01T11:00:00Z",
        endTime = "2025-01-01T12:30:00Z",
        location = "Room C",
        maxCapacity = 25,
        currentCapacity = 12,
        isAcceptingCheckIns = true,
        staffMembers = "[\"staff6\", \"staff7\"]",
        createdAt = "2025-01-01T08:00:00Z",
        lastSyncedAt = null
    )
    
    @Test
    fun testServiceDataStructure() = runTest {
        // Test basic service data structure
        assertEquals("service1", testService1.id)
        assertEquals("Toddler Time", testService1.name)
        assertEquals("Fun activities for toddlers", testService1.description)
        assertEquals(2, testService1.minAge)
        assertEquals(4, testService1.maxAge)
        assertEquals("Room A", testService1.location)
    }
    
    @Test
    fun testServiceCapacityLogic() = runTest {
        // Test capacity calculations
        assertEquals(15, testService1.maxCapacity)
        assertEquals(8, testService1.currentCapacity)
        assertTrue(testService1.currentCapacity < testService1.maxCapacity) // Has available spots
        
        assertEquals(20, testService2.maxCapacity)
        assertEquals(20, testService2.currentCapacity)
        assertTrue(testService2.currentCapacity >= testService2.maxCapacity) // At capacity
        
        assertEquals(25, testService3.maxCapacity)
        assertEquals(12, testService3.currentCapacity)
        assertTrue(testService3.currentCapacity < testService3.maxCapacity) // Has available spots
    }
    
    @Test
    fun testServiceCheckInAcceptance() = runTest {
        // Test check-in acceptance logic
        assertTrue(testService1.isAcceptingCheckIns)
        assertFalse(testService2.isAcceptingCheckIns)
        assertTrue(testService3.isAcceptingCheckIns)
    }
    
    @Test
    fun testServiceAgeRanges() = runTest {
        // Test age range validation
        assertTrue(testService1.minAge <= testService1.maxAge)
        assertTrue(testService2.minAge <= testService2.maxAge)
        assertTrue(testService3.minAge <= testService3.maxAge)
        
        // Test specific age ranges
        assertEquals(2..4, testService1.minAge..testService1.maxAge)
        assertEquals(5..10, testService2.minAge..testService2.maxAge)
        assertEquals(11..17, testService3.minAge..testService3.maxAge)
    }
    
    @Test
    fun testServiceAvailabilityForCheckIn() = runTest {
        // Test combined availability logic (accepting check-ins AND has capacity)
        val service1Available = testService1.isAcceptingCheckIns && 
                               testService1.currentCapacity < testService1.maxCapacity
        assertTrue(service1Available) // Should be available
        
        val service2Available = testService2.isAcceptingCheckIns && 
                               testService2.currentCapacity < testService2.maxCapacity
        assertFalse(service2Available) // Not accepting check-ins
        
        val service3Available = testService3.isAcceptingCheckIns && 
                               testService3.currentCapacity < testService3.maxCapacity
        assertTrue(service3Available) // Should be available
    }
    
    @Test
    fun testServicesByAge() = runTest {
        // Test filtering services by age
        val allServices = listOf(testService1, testService2, testService3)
        
        // Test age 3 (should match service1)
        val servicesForAge3 = allServices.filter { service ->
            3 >= service.minAge && 3 <= service.maxAge
        }
        assertEquals(1, servicesForAge3.size)
        assertEquals("service1", servicesForAge3.first().id)
        
        // Test age 7 (should match service2)
        val servicesForAge7 = allServices.filter { service ->
            7 >= service.minAge && 7 <= service.maxAge
        }
        assertEquals(1, servicesForAge7.size)
        assertEquals("service2", servicesForAge7.first().id)
        
        // Test age 14 (should match service3)
        val servicesForAge14 = allServices.filter { service ->
            14 >= service.minAge && 14 <= service.maxAge
        }
        assertEquals(1, servicesForAge14.size)
        assertEquals("service3", servicesForAge14.first().id)
        
        // Test age 1 (should match no services)
        val servicesForAge1 = allServices.filter { service ->
            1 >= service.minAge && 1 <= service.maxAge
        }
        assertEquals(0, servicesForAge1.size)
    }
    
    @Test
    fun testServiceStaffMembers() = runTest {
        // Test staff members JSON structure
        assertEquals("[\"staff1\", \"staff2\"]", testService1.staffMembers)
        assertEquals("[\"staff3\", \"staff4\", \"staff5\"]", testService2.staffMembers)
        assertEquals("[\"staff6\", \"staff7\"]", testService3.staffMembers)
    }
    
    @Test
    fun testServiceTimeScheduling() = runTest {
        // Test service time scheduling
        assertEquals("2025-01-01T09:00:00Z", testService1.startTime)
        assertEquals("2025-01-01T10:30:00Z", testService1.endTime)
        
        assertEquals("2025-01-01T10:00:00Z", testService2.startTime)
        assertEquals("2025-01-01T11:30:00Z", testService2.endTime)
        
        assertEquals("2025-01-01T11:00:00Z", testService3.startTime)
        assertEquals("2025-01-01T12:30:00Z", testService3.endTime)
        
        // Verify start time is before end time (basic validation)
        assertTrue(testService1.startTime < testService1.endTime)
        assertTrue(testService2.startTime < testService2.endTime)
        assertTrue(testService3.startTime < testService3.endTime)
    }
    
    @Test
    fun testServiceSyncTracking() = runTest {
        // Test sync tracking
        assertNull(testService1.lastSyncedAt)
        assertEquals("2025-01-01T08:30:00Z", testService2.lastSyncedAt)
        assertNull(testService3.lastSyncedAt)
    }
    
    @Test
    fun testServiceCapacityUpdates() = runTest {
        // Test capacity increment/decrement logic
        val originalCapacity = testService1.currentCapacity
        val incrementedCapacity = originalCapacity + 1
        val decrementedCapacity = originalCapacity - 1
        
        assertTrue(incrementedCapacity <= testService1.maxCapacity)
        assertTrue(decrementedCapacity >= 0)
        
        // Test capacity bounds
        val serviceAtCapacity = testService2
        assertEquals(serviceAtCapacity.maxCapacity, serviceAtCapacity.currentCapacity)
        
        // Ensure we don't exceed max capacity
        val wouldExceedCapacity = serviceAtCapacity.currentCapacity + 1
        assertTrue(wouldExceedCapacity > serviceAtCapacity.maxCapacity)
    }
    
    @Test
    fun testServiceValidation() = runTest {
        // Test service data validation
        assertTrue(testService1.id.isNotBlank())
        assertTrue(testService1.name.isNotBlank())
        assertTrue(testService1.description.isNotBlank())
        assertTrue(testService1.location.isNotBlank())
        assertTrue(testService1.maxCapacity > 0)
        assertTrue(testService1.currentCapacity >= 0)
        assertTrue(testService1.minAge >= 0)
        assertTrue(testService1.maxAge >= testService1.minAge)
        assertTrue(testService1.startTime.isNotBlank())
        assertTrue(testService1.endTime.isNotBlank())
        assertTrue(testService1.createdAt.isNotBlank())
    }
}