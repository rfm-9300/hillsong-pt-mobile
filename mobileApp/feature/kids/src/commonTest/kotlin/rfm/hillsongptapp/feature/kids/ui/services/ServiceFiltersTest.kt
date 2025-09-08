package rfm.hillsongptapp.feature.kids.ui.services

import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import kotlin.test.*

class ServiceFiltersTest {
    
    private val availableService = KidsService(
        id = "service-1",
        name = "Available Service",
        description = "Service with available spots",
        minAge = 5,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room A",
        maxCapacity = 20,
        currentCapacity = 15,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val fullService = KidsService(
        id = "service-2",
        name = "Full Service",
        description = "Service at capacity",
        minAge = 3,
        maxAge = 8,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room B",
        maxCapacity = 15,
        currentCapacity = 15, // At capacity
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-2"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val closedService = KidsService(
        id = "service-3",
        name = "Closed Service",
        description = "Service not accepting check-ins",
        minAge = 10,
        maxAge = 16,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room C",
        maxCapacity = 25,
        currentCapacity = 10,
        isAcceptingCheckIns = false, // Not accepting check-ins
        staffMembers = listOf("staff-3"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    @Test
    fun `default filters have no active filters`() {
        val filters = ServiceFilters()
        
        assertFalse(filters.hasActiveFilters())
        assertEquals(ServiceFilters.Availability.ALL, filters.availability)
        assertNull(filters.minAge)
        assertNull(filters.maxAge)
        assertTrue(filters.showFullServices)
    }
    
    @Test
    fun `hasActiveFilters detects availability filter`() {
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        
        assertTrue(filters.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters detects age filters`() {
        val filtersWithMinAge = ServiceFilters(minAge = 5)
        val filtersWithMaxAge = ServiceFilters(maxAge = 12)
        val filtersWithBothAges = ServiceFilters(minAge = 5, maxAge = 12)
        
        assertTrue(filtersWithMinAge.hasActiveFilters())
        assertTrue(filtersWithMaxAge.hasActiveFilters())
        assertTrue(filtersWithBothAges.hasActiveFilters())
    }
    
    @Test
    fun `hasActiveFilters detects capacity filter`() {
        val filters = ServiceFilters(showFullServices = false)
        
        assertTrue(filters.hasActiveFilters())
    }
    
    @Test
    fun `matches returns true for all services with default filters`() {
        val filters = ServiceFilters()
        
        assertTrue(filters.matches(availableService))
        assertTrue(filters.matches(fullService))
        assertTrue(filters.matches(closedService))
    }
    
    @Test
    fun `availability filter AVAILABLE_ONLY excludes full services`() {
        val filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        
        assertTrue(filters.matches(availableService)) // Has spots
        assertFalse(filters.matches(fullService)) // Full capacity
        assertTrue(filters.matches(closedService)) // Has spots (even though not accepting)
    }
    
    @Test
    fun `availability filter ACCEPTING_CHECKINS excludes closed services`() {
        val filters = ServiceFilters(availability = ServiceFilters.Availability.ACCEPTING_CHECKINS)
        
        assertTrue(filters.matches(availableService)) // Accepting and has spots
        assertTrue(filters.matches(fullService)) // Accepting but full
        assertFalse(filters.matches(closedService)) // Not accepting
    }
    
    @Test
    fun `age range filter works with minAge only`() {
        val filters = ServiceFilters(minAge = 6)
        
        assertTrue(filters.matches(availableService)) // maxAge 12 >= minAge 6
        assertFalse(filters.matches(fullService)) // maxAge 8 >= minAge 6, but service maxAge 8 < filter minAge 6 is false
        assertTrue(filters.matches(closedService)) // maxAge 16 >= minAge 6
    }
    
    @Test
    fun `age range filter works with maxAge only`() {
        val filters = ServiceFilters(maxAge = 10)
        
        assertTrue(filters.matches(availableService)) // minAge 5 <= maxAge 10
        assertTrue(filters.matches(fullService)) // minAge 3 <= maxAge 10
        assertFalse(filters.matches(closedService)) // minAge 10 <= maxAge 10, but service minAge 10 > filter maxAge 10 is false
    }
    
    @Test
    fun `age range filter works with both minAge and maxAge`() {
        val filters = ServiceFilters(minAge = 4, maxAge = 9)
        
        assertTrue(filters.matches(availableService)) // Service 5-12 overlaps with filter 4-9
        assertTrue(filters.matches(fullService)) // Service 3-8 overlaps with filter 4-9
        assertFalse(filters.matches(closedService)) // Service 10-16 doesn't overlap with filter 4-9
    }
    
    @Test
    fun `showFullServices filter excludes services at capacity`() {
        val filters = ServiceFilters(showFullServices = false)
        
        assertTrue(filters.matches(availableService)) // Not at capacity
        assertFalse(filters.matches(fullService)) // At capacity
        assertTrue(filters.matches(closedService)) // Not at capacity
    }
    
    @Test
    fun `complex filter combination works correctly`() {
        val filters = ServiceFilters(
            availability = ServiceFilters.Availability.ACCEPTING_CHECKINS,
            minAge = 4,
            maxAge = 10,
            showFullServices = false
        )
        
        assertTrue(filters.matches(availableService)) // Accepting, age overlap, not full
        assertFalse(filters.matches(fullService)) // Accepting, age overlap, but full
        assertFalse(filters.matches(closedService)) // Not accepting
    }
    
    @Test
    fun `availability enum has correct display names`() {
        assertEquals("All Services", ServiceFilters.Availability.ALL.displayName)
        assertEquals("Available Only", ServiceFilters.Availability.AVAILABLE_ONLY.displayName)
        assertEquals("Accepting Check-ins", ServiceFilters.Availability.ACCEPTING_CHECKINS.displayName)
    }
    
    @Test
    fun `edge case - service with single age matches correctly`() {
        val singleAgeService = availableService.copy(minAge = 8, maxAge = 8)
        
        val filtersIncluding = ServiceFilters(minAge = 7, maxAge = 9)
        val filtersExcluding = ServiceFilters(minAge = 9, maxAge = 10)
        
        assertTrue(filtersIncluding.matches(singleAgeService))
        assertFalse(filtersExcluding.matches(singleAgeService))
    }
    
    @Test
    fun `edge case - filter with single age matches correctly`() {
        val filters = ServiceFilters(minAge = 8, maxAge = 8)
        
        assertTrue(filters.matches(availableService)) // Service 5-12 includes age 8
        assertTrue(filters.matches(fullService)) // Service 3-8 includes age 8
        assertFalse(filters.matches(closedService)) // Service 10-16 doesn't include age 8
    }
}