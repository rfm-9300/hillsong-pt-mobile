package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChildTest {
    
    private val sampleEmergencyContact = EmergencyContact(
        name = "John Doe",
        phoneNumber = "+1234567890",
        relationship = "Father"
    )
    
    private val sampleChild = Child(
        id = "child-123",
        parentId = "parent-456",
        name = "Jane Doe",
        dateOfBirth = "2015-05-15",
        medicalInfo = "No allergies",
        dietaryRestrictions = "Vegetarian",
        emergencyContact = sampleEmergencyContact,
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T10:00:00Z",
        updatedAt = "2025-01-01T10:00:00Z"
    )
    
    @Test
    fun testChildSerialization() {
        val json = Json.encodeToString(Child.serializer(), sampleChild)
        val deserializedChild = Json.decodeFromString(Child.serializer(), json)
        
        assertEquals(sampleChild, deserializedChild)
    }
    
    @Test
    fun testCalculateAge() {
        // Child born in 2015, current year is 2025 (as per simplified implementation)
        val age = sampleChild.calculateAge()
        assertEquals(10, age)
    }
    
    @Test
    fun testIsCheckedIn() {
        val checkedInChild = sampleChild.copy(status = CheckInStatus.CHECKED_IN)
        val checkedOutChild = sampleChild.copy(status = CheckInStatus.CHECKED_OUT)
        val notInServiceChild = sampleChild.copy(status = CheckInStatus.NOT_IN_SERVICE)
        
        assertTrue(checkedInChild.isCheckedIn())
        assertFalse(checkedOutChild.isCheckedIn())
        assertFalse(notInServiceChild.isCheckedIn())
    }
    
    @Test
    fun testIsEligibleForService() {
        val service = KidsService(
            id = "service-123",
            name = "Kids Service",
            description = "Service for kids",
            minAge = 8,
            maxAge = 12,
            startTime = "2025-01-01T10:00:00Z",
            endTime = "2025-01-01T11:00:00Z",
            location = "Room A",
            maxCapacity = 20,
            currentCapacity = 10,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-1"),
            createdAt = "2025-01-01T09:00:00Z"
        )
        
        // Child is 10 years old (born 2015), service accepts 8-12 years
        assertTrue(sampleChild.isEligibleForService(service))
        
        // Test with service that doesn't accept this age
        val youngerService = service.copy(minAge = 12, maxAge = 15)
        assertFalse(sampleChild.isEligibleForService(youngerService))
        
        val olderService = service.copy(minAge = 3, maxAge = 7)
        assertFalse(sampleChild.isEligibleForService(olderService))
    }
    
    @Test
    fun testChildWithNullOptionalFields() {
        val minimalChild = Child(
            id = "child-123",
            parentId = "parent-456",
            name = "Jane Doe",
            dateOfBirth = "2015-05-15",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = sampleEmergencyContact,
            status = CheckInStatus.NOT_IN_SERVICE,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2025-01-01T10:00:00Z",
            updatedAt = "2025-01-01T10:00:00Z"
        )
        
        val json = Json.encodeToString(Child.serializer(), minimalChild)
        val deserializedChild = Json.decodeFromString(Child.serializer(), json)
        
        assertEquals(minimalChild, deserializedChild)
    }
}