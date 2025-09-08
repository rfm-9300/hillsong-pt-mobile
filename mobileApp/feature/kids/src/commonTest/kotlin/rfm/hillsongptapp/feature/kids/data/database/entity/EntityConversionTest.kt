package rfm.hillsongptapp.feature.kids.data.database.entity

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

/**
 * Unit tests for entity conversion functions between domain models and database entities
 */
class EntityConversionTest {
    
    // Test domain models
    private val testEmergencyContact = EmergencyContact(
        name = "Jane Doe",
        phoneNumber = "+1234567890",
        relationship = "Mother"
    )
    
    private val testChild = Child(
        id = "child1",
        parentId = "parent1",
        name = "John Doe",
        dateOfBirth = "2015-05-15",
        medicalInfo = "No allergies",
        dietaryRestrictions = null,
        emergencyContact = testEmergencyContact,
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T10:00:00Z",
        updatedAt = "2025-01-01T10:00:00Z"
    )
    
    private val testKidsService = KidsService(
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
        staffMembers = listOf("staff1", "staff2"),
        createdAt = "2025-01-01T08:00:00Z"
    )
    
    private val testCheckInRecord = CheckInRecord(
        id = "record1",
        childId = "child1",
        serviceId = "service1",
        checkInTime = "2025-01-01T11:00:00Z",
        checkOutTime = null,
        checkedInBy = "parent1",
        checkedOutBy = null,
        notes = null,
        status = CheckInStatus.CHECKED_IN
    )
    
    @Test
    fun testChildDomainToEntityConversion() {
        // Test converting Child domain model to ChildEntity
        val entity = testChild.toEntity()
        
        assertEquals(testChild.id, entity.id)
        assertEquals(testChild.parentId, entity.parentId)
        assertEquals(testChild.name, entity.name)
        assertEquals(testChild.dateOfBirth, entity.dateOfBirth)
        assertEquals(testChild.medicalInfo, entity.medicalInfo)
        assertEquals(testChild.dietaryRestrictions, entity.dietaryRestrictions)
        
        // Emergency contact fields
        assertEquals(testChild.emergencyContact.name, entity.emergencyContactName)
        assertEquals(testChild.emergencyContact.phoneNumber, entity.emergencyContactPhone)
        assertEquals(testChild.emergencyContact.relationship, entity.emergencyContactRelationship)
        
        // Status and service fields
        assertEquals(testChild.status.name, entity.status)
        assertEquals(testChild.currentServiceId, entity.currentServiceId)
        assertEquals(testChild.checkInTime, entity.checkInTime)
        assertEquals(testChild.checkOutTime, entity.checkOutTime)
        
        // Timestamps
        assertEquals(testChild.createdAt, entity.createdAt)
        assertEquals(testChild.updatedAt, entity.updatedAt)
        
        // Sync tracking should be null by default
        assertNull(entity.lastSyncedAt)
    }
    
    @Test
    fun testChildEntityToDomainConversion() {
        // Test converting ChildEntity to Child domain model
        val entity = testChild.toEntity()
        val domainModel = entity.toDomain()
        
        assertEquals(entity.id, domainModel.id)
        assertEquals(entity.parentId, domainModel.parentId)
        assertEquals(entity.name, domainModel.name)
        assertEquals(entity.dateOfBirth, domainModel.dateOfBirth)
        assertEquals(entity.medicalInfo, domainModel.medicalInfo)
        assertEquals(entity.dietaryRestrictions, domainModel.dietaryRestrictions)
        
        // Emergency contact reconstruction
        assertEquals(entity.emergencyContactName, domainModel.emergencyContact.name)
        assertEquals(entity.emergencyContactPhone, domainModel.emergencyContact.phoneNumber)
        assertEquals(entity.emergencyContactRelationship, domainModel.emergencyContact.relationship)
        
        // Status conversion
        assertEquals(CheckInStatus.valueOf(entity.status), domainModel.status)
        assertEquals(entity.currentServiceId, domainModel.currentServiceId)
        assertEquals(entity.checkInTime, domainModel.checkInTime)
        assertEquals(entity.checkOutTime, domainModel.checkOutTime)
        
        // Timestamps
        assertEquals(entity.createdAt, domainModel.createdAt)
        assertEquals(entity.updatedAt, domainModel.updatedAt)
    }
    
    @Test
    fun testChildRoundTripConversion() {
        // Test that domain -> entity -> domain conversion preserves data
        val entity = testChild.toEntity()
        val convertedBack = entity.toDomain()
        
        assertEquals(testChild.id, convertedBack.id)
        assertEquals(testChild.parentId, convertedBack.parentId)
        assertEquals(testChild.name, convertedBack.name)
        assertEquals(testChild.dateOfBirth, convertedBack.dateOfBirth)
        assertEquals(testChild.medicalInfo, convertedBack.medicalInfo)
        assertEquals(testChild.dietaryRestrictions, convertedBack.dietaryRestrictions)
        assertEquals(testChild.emergencyContact.name, convertedBack.emergencyContact.name)
        assertEquals(testChild.emergencyContact.phoneNumber, convertedBack.emergencyContact.phoneNumber)
        assertEquals(testChild.emergencyContact.relationship, convertedBack.emergencyContact.relationship)
        assertEquals(testChild.status, convertedBack.status)
        assertEquals(testChild.currentServiceId, convertedBack.currentServiceId)
        assertEquals(testChild.checkInTime, convertedBack.checkInTime)
        assertEquals(testChild.checkOutTime, convertedBack.checkOutTime)
        assertEquals(testChild.createdAt, convertedBack.createdAt)
        assertEquals(testChild.updatedAt, convertedBack.updatedAt)
    }
    
    @Test
    fun testKidsServiceDomainToEntityConversion() {
        // Test converting KidsService domain model to KidsServiceEntity
        val entity = testKidsService.toEntity()
        
        assertEquals(testKidsService.id, entity.id)
        assertEquals(testKidsService.name, entity.name)
        assertEquals(testKidsService.description, entity.description)
        assertEquals(testKidsService.minAge, entity.minAge)
        assertEquals(testKidsService.maxAge, entity.maxAge)
        assertEquals(testKidsService.startTime, entity.startTime)
        assertEquals(testKidsService.endTime, entity.endTime)
        assertEquals(testKidsService.location, entity.location)
        assertEquals(testKidsService.maxCapacity, entity.maxCapacity)
        assertEquals(testKidsService.currentCapacity, entity.currentCapacity)
        assertEquals(testKidsService.isAcceptingCheckIns, entity.isAcceptingCheckIns)
        assertEquals(testKidsService.createdAt, entity.createdAt)
        
        // Staff members should be JSON serialized
        assertNotNull(entity.staffMembers)
        assertEquals("[\"staff1\",\"staff2\"]", entity.staffMembers)
        
        // Sync tracking should be null by default
        assertNull(entity.lastSyncedAt)
    }
    
    @Test
    fun testKidsServiceEntityToDomainConversion() {
        // Test converting KidsServiceEntity to KidsService domain model
        val entity = testKidsService.toEntity()
        val domainModel = entity.toDomain()
        
        assertEquals(entity.id, domainModel.id)
        assertEquals(entity.name, domainModel.name)
        assertEquals(entity.description, domainModel.description)
        assertEquals(entity.minAge, domainModel.minAge)
        assertEquals(entity.maxAge, domainModel.maxAge)
        assertEquals(entity.startTime, domainModel.startTime)
        assertEquals(entity.endTime, domainModel.endTime)
        assertEquals(entity.location, domainModel.location)
        assertEquals(entity.maxCapacity, domainModel.maxCapacity)
        assertEquals(entity.currentCapacity, domainModel.currentCapacity)
        assertEquals(entity.isAcceptingCheckIns, domainModel.isAcceptingCheckIns)
        assertEquals(entity.createdAt, domainModel.createdAt)
        
        // Staff members should be deserialized from JSON
        assertEquals(2, domainModel.staffMembers.size)
        assertEquals("staff1", domainModel.staffMembers[0])
        assertEquals("staff2", domainModel.staffMembers[1])
    }
    
    @Test
    fun testKidsServiceRoundTripConversion() {
        // Test that domain -> entity -> domain conversion preserves data
        val entity = testKidsService.toEntity()
        val convertedBack = entity.toDomain()
        
        assertEquals(testKidsService.id, convertedBack.id)
        assertEquals(testKidsService.name, convertedBack.name)
        assertEquals(testKidsService.description, convertedBack.description)
        assertEquals(testKidsService.minAge, convertedBack.minAge)
        assertEquals(testKidsService.maxAge, convertedBack.maxAge)
        assertEquals(testKidsService.startTime, convertedBack.startTime)
        assertEquals(testKidsService.endTime, convertedBack.endTime)
        assertEquals(testKidsService.location, convertedBack.location)
        assertEquals(testKidsService.maxCapacity, convertedBack.maxCapacity)
        assertEquals(testKidsService.currentCapacity, convertedBack.currentCapacity)
        assertEquals(testKidsService.isAcceptingCheckIns, convertedBack.isAcceptingCheckIns)
        assertEquals(testKidsService.staffMembers, convertedBack.staffMembers)
        assertEquals(testKidsService.createdAt, convertedBack.createdAt)
    }
    
    @Test
    fun testCheckInRecordDomainToEntityConversion() {
        // Test converting CheckInRecord domain model to CheckInRecordEntity
        val entity = testCheckInRecord.toEntity()
        
        assertEquals(testCheckInRecord.id, entity.id)
        assertEquals(testCheckInRecord.childId, entity.childId)
        assertEquals(testCheckInRecord.serviceId, entity.serviceId)
        assertEquals(testCheckInRecord.checkInTime, entity.checkInTime)
        assertEquals(testCheckInRecord.checkOutTime, entity.checkOutTime)
        assertEquals(testCheckInRecord.checkedInBy, entity.checkedInBy)
        assertEquals(testCheckInRecord.checkedOutBy, entity.checkedOutBy)
        assertEquals(testCheckInRecord.notes, entity.notes)
        assertEquals(testCheckInRecord.status.name, entity.status)
        
        // Sync tracking should be null by default
        assertNull(entity.lastSyncedAt)
    }
    
    @Test
    fun testCheckInRecordEntityToDomainConversion() {
        // Test converting CheckInRecordEntity to CheckInRecord domain model
        val entity = testCheckInRecord.toEntity()
        val domainModel = entity.toDomain()
        
        assertEquals(entity.id, domainModel.id)
        assertEquals(entity.childId, domainModel.childId)
        assertEquals(entity.serviceId, domainModel.serviceId)
        assertEquals(entity.checkInTime, domainModel.checkInTime)
        assertEquals(entity.checkOutTime, domainModel.checkOutTime)
        assertEquals(entity.checkedInBy, domainModel.checkedInBy)
        assertEquals(entity.checkedOutBy, domainModel.checkedOutBy)
        assertEquals(entity.notes, domainModel.notes)
        assertEquals(CheckInStatus.valueOf(entity.status), domainModel.status)
    }
    
    @Test
    fun testCheckInRecordRoundTripConversion() {
        // Test that domain -> entity -> domain conversion preserves data
        val entity = testCheckInRecord.toEntity()
        val convertedBack = entity.toDomain()
        
        assertEquals(testCheckInRecord.id, convertedBack.id)
        assertEquals(testCheckInRecord.childId, convertedBack.childId)
        assertEquals(testCheckInRecord.serviceId, convertedBack.serviceId)
        assertEquals(testCheckInRecord.checkInTime, convertedBack.checkInTime)
        assertEquals(testCheckInRecord.checkOutTime, convertedBack.checkOutTime)
        assertEquals(testCheckInRecord.checkedInBy, convertedBack.checkedInBy)
        assertEquals(testCheckInRecord.checkedOutBy, convertedBack.checkedOutBy)
        assertEquals(testCheckInRecord.notes, convertedBack.notes)
        assertEquals(testCheckInRecord.status, convertedBack.status)
    }
    
    @Test
    fun testConversionWithSyncTimestamp() {
        // Test conversion with sync timestamp
        val syncTime = "2025-01-01T12:00:00Z"
        
        val childEntity = testChild.toEntity(syncTime)
        assertEquals(syncTime, childEntity.lastSyncedAt)
        
        val serviceEntity = testKidsService.toEntity(syncTime)
        assertEquals(syncTime, serviceEntity.lastSyncedAt)
        
        val recordEntity = testCheckInRecord.toEntity(syncTime)
        assertEquals(syncTime, recordEntity.lastSyncedAt)
    }
    
    @Test
    fun testEmptyStaffMembersConversion() {
        // Test service with empty staff members list
        val serviceWithNoStaff = testKidsService.copy(staffMembers = emptyList())
        val entity = serviceWithNoStaff.toEntity()
        val convertedBack = entity.toDomain()
        
        assertEquals("[]", entity.staffMembers)
        assertEquals(0, convertedBack.staffMembers.size)
    }
    
    @Test
    fun testNullFieldsConversion() {
        // Test conversion with null fields
        val childWithNulls = testChild.copy(
            medicalInfo = null,
            dietaryRestrictions = null,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null
        )
        
        val entity = childWithNulls.toEntity()
        val convertedBack = entity.toDomain()
        
        assertNull(convertedBack.medicalInfo)
        assertNull(convertedBack.dietaryRestrictions)
        assertNull(convertedBack.currentServiceId)
        assertNull(convertedBack.checkInTime)
        assertNull(convertedBack.checkOutTime)
    }
}