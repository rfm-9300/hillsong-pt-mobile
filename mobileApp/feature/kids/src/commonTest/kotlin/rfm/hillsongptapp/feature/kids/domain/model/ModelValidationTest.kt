package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test to verify all models work together correctly
 */
class ModelValidationTest {
    
    @Test
    fun testCompleteWorkflow() {
        // Create emergency contact
        val emergencyContact = EmergencyContact(
            name = "John Doe",
            phoneNumber = "+1234567890",
            relationship = "Father"
        )
        
        // Create child
        val child = Child(
            id = "child-123",
            parentId = "parent-456",
            name = "Jane Doe",
            dateOfBirth = "2015-05-15",
            medicalInfo = "No allergies",
            dietaryRestrictions = null,
            emergencyContact = emergencyContact,
            status = CheckInStatus.NOT_IN_SERVICE,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2025-01-01T10:00:00Z",
            updatedAt = "2025-01-01T10:00:00Z"
        )
        
        // Create service
        val service = KidsService(
            id = "service-123",
            name = "Kids Service",
            description = "Service for kids aged 8-12",
            minAge = 8,
            maxAge = 12,
            startTime = "2025-01-01T10:00:00Z",
            endTime = "2025-01-01T11:00:00Z",
            location = "Room A",
            maxCapacity = 20,
            currentCapacity = 10,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff-1", "staff-2"),
            createdAt = "2025-01-01T09:00:00Z"
        )
        
        // Create check-in record
        val checkInRecord = CheckInRecord(
            id = "record-123",
            childId = child.id,
            serviceId = service.id,
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = null,
            checkedInBy = child.parentId,
            checkedOutBy = null,
            notes = "Child was excited to join",
            status = CheckInStatus.CHECKED_IN
        )
        
        // Verify relationships
        assertEquals(child.id, checkInRecord.childId)
        assertEquals(service.id, checkInRecord.serviceId)
        assertTrue(child.isEligibleForService(service))
        assertTrue(service.canAcceptCheckIn())
        assertTrue(checkInRecord.isCurrentlyCheckedIn())
        
        // Test serialization of all models
        val childJson = Json.encodeToString(Child.serializer(), child)
        val serviceJson = Json.encodeToString(KidsService.serializer(), service)
        val recordJson = Json.encodeToString(CheckInRecord.serializer(), checkInRecord)
        
        assertNotNull(childJson)
        assertNotNull(serviceJson)
        assertNotNull(recordJson)
        
        // Test deserialization
        val deserializedChild = Json.decodeFromString(Child.serializer(), childJson)
        val deserializedService = Json.decodeFromString(KidsService.serializer(), serviceJson)
        val deserializedRecord = Json.decodeFromString(CheckInRecord.serializer(), recordJson)
        
        assertEquals(child, deserializedChild)
        assertEquals(service, deserializedService)
        assertEquals(checkInRecord, deserializedRecord)
    }
    
    @Test
    fun testAllCheckInStatuses() {
        val statuses = listOf(
            CheckInStatus.CHECKED_OUT,
            CheckInStatus.CHECKED_IN,
            CheckInStatus.NOT_IN_SERVICE
        )
        
        statuses.forEach { status ->
            val json = Json.encodeToString(CheckInStatus.serializer(), status)
            val deserialized = Json.decodeFromString(CheckInStatus.serializer(), json)
            assertEquals(status, deserialized)
            assertNotNull(status.getDisplayName())
        }
    }
}