package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KidsServiceTest {
    
    private val sampleService = KidsService(
        id = "service-123",
        name = "Kids Service",
        description = "Service for kids aged 8-12",
        minAge = 8,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room A",
        maxCapacity = 20,
        currentCapacity = 15,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1", "staff-2"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    @Test
    fun testKidsServiceSerialization() {
        val json = Json.encodeToString(KidsService.serializer(), sampleService)
        val deserializedService = Json.decodeFromString(KidsService.serializer(), json)
        
        assertEquals(sampleService, deserializedService)
    }
    
    @Test
    fun testHasAvailableSpots() {
        assertTrue(sampleService.hasAvailableSpots()) // 15/20 capacity
        
        val fullService = sampleService.copy(currentCapacity = 20)
        assertFalse(fullService.hasAvailableSpots())
    }
    
    @Test
    fun testIsAtCapacity() {
        assertFalse(sampleService.isAtCapacity()) // 15/20 capacity
        
        val fullService = sampleService.copy(currentCapacity = 20)
        assertTrue(fullService.isAtCapacity())
    }
    
    @Test
    fun testGetAvailableSpots() {
        assertEquals(5, sampleService.getAvailableSpots()) // 20 - 15 = 5
        
        val fullService = sampleService.copy(currentCapacity = 20)
        assertEquals(0, fullService.getAvailableSpots())
    }
    
    @Test
    fun testIsAgeEligible() {
        assertTrue(sampleService.isAgeEligible(8)) // Min age
        assertTrue(sampleService.isAgeEligible(10)) // Middle age
        assertTrue(sampleService.isAgeEligible(12)) // Max age
        
        assertFalse(sampleService.isAgeEligible(7)) // Below min
        assertFalse(sampleService.isAgeEligible(13)) // Above max
    }
    
    @Test
    fun testCanAcceptCheckIn() {
        assertTrue(sampleService.canAcceptCheckIn()) // Accepting and has spots
        
        val fullService = sampleService.copy(currentCapacity = 20)
        assertFalse(fullService.canAcceptCheckIn()) // Full capacity
        
        val notAcceptingService = sampleService.copy(isAcceptingCheckIns = false)
        assertFalse(notAcceptingService.canAcceptCheckIn()) // Not accepting
    }
    
    @Test
    fun testGetAgeRangeDisplay() {
        assertEquals("8-12 years", sampleService.getAgeRangeDisplay())
        
        val singleAgeService = sampleService.copy(minAge = 10, maxAge = 10)
        assertEquals("10 years", singleAgeService.getAgeRangeDisplay())
    }
    
    @Test
    fun testGetCapacityDisplay() {
        assertEquals("15/20", sampleService.getCapacityDisplay())
    }
    
    @Test
    fun testValidationConstraints() {
        // Test blank name
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(name = "")
        }
        
        // Test negative min age
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(minAge = -1)
        }
        
        // Test max age less than min age
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(minAge = 12, maxAge = 8)
        }
        
        // Test non-positive max capacity
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(maxCapacity = 0)
        }
        
        // Test negative current capacity
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(currentCapacity = -1)
        }
        
        // Test current capacity exceeding max capacity
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(currentCapacity = 25, maxCapacity = 20)
        }
        
        // Test blank location
        assertFailsWith<IllegalArgumentException> {
            sampleService.copy(location = "")
        }
    }
}