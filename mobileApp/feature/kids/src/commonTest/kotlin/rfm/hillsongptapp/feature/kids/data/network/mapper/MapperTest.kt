package rfm.hillsongptapp.feature.kids.data.network.mapper

import kotlin.test.*
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.mapper.*
import rfm.hillsongptapp.core.data.model.*

class MapperTest {
    
    @Test
    fun `ChildDto toDomain maps correctly`() {
        // Given
        val dto = ChildDto(
            id = "child123",
            parentId = "parent123",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = "Vegetarian",
            emergencyContact = EmergencyContactDto(
                name = "Emergency Contact",
                phoneNumber = "+1234567890",
                relationship = "Parent"
            ),
            status = "CHECKED_IN",
            currentServiceId = "service123",
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T10:00:00Z"
        )
        
        // When
        val domain = dto.toDomain()
        
        // Then
        assertEquals(dto.id, domain.id)
        assertEquals(dto.parentId, domain.parentId)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.dateOfBirth, domain.dateOfBirth)
        assertEquals(dto.medicalInfo, domain.medicalInfo)
        assertEquals(dto.dietaryRestrictions, domain.dietaryRestrictions)
        assertEquals(dto.emergencyContact.name, domain.emergencyContact.name)
        assertEquals(dto.emergencyContact.phoneNumber, domain.emergencyContact.phoneNumber)
        assertEquals(dto.emergencyContact.relationship, domain.emergencyContact.relationship)
        assertEquals(CheckInStatus.CHECKED_IN, domain.status)
        assertEquals(dto.currentServiceId, domain.currentServiceId)
        assertEquals(dto.checkInTime, domain.checkInTime)
        assertEquals(dto.checkOutTime, domain.checkOutTime)
        assertEquals(dto.createdAt, domain.createdAt)
        assertEquals(dto.updatedAt, domain.updatedAt)
    }
    
    @Test
    fun `Child toDto maps correctly`() {
        // Given
        val domain = Child(
            id = "child123",
            parentId = "parent123",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = "Vegetarian",
            emergencyContact = EmergencyContact(
                name = "Emergency Contact",
                phoneNumber = "+1234567890",
                relationship = "Parent"
            ),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = "2024-01-01T11:00:00Z",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T11:00:00Z"
        )
        
        // When
        val dto = domain.toDto()
        
        // Then
        assertEquals(domain.id, dto.id)
        assertEquals(domain.parentId, dto.parentId)
        assertEquals(domain.name, dto.name)
        assertEquals(domain.dateOfBirth, dto.dateOfBirth)
        assertEquals(domain.medicalInfo, dto.medicalInfo)
        assertEquals(domain.dietaryRestrictions, dto.dietaryRestrictions)
        assertEquals(domain.emergencyContact.name, dto.emergencyContact.name)
        assertEquals(domain.emergencyContact.phoneNumber, dto.emergencyContact.phoneNumber)
        assertEquals(domain.emergencyContact.relationship, dto.emergencyContact.relationship)
        assertEquals("CHECKED_OUT", dto.status)
        assertEquals(domain.currentServiceId, dto.currentServiceId)
        assertEquals(domain.checkInTime, dto.checkInTime)
        assertEquals(domain.checkOutTime, dto.checkOutTime)
        assertEquals(domain.createdAt, dto.createdAt)
        assertEquals(domain.updatedAt, dto.updatedAt)
    }
    
    @Test
    fun `KidsServiceDto toDomain maps correctly`() {
        // Given
        val dto = KidsServiceDto(
            id = "service123",
            name = "Kids Church",
            description = "Sunday kids service",
            minAge = 3,
            maxAge = 12,
            startTime = "2024-01-01T10:00:00Z",
            endTime = "2024-01-01T11:00:00Z",
            location = "Kids Room",
            maxCapacity = 20,
            currentCapacity = 5,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff1", "staff2"),
            createdAt = "2024-01-01T00:00:00Z"
        )
        
        // When
        val domain = dto.toDomain()
        
        // Then
        assertEquals(dto.id, domain.id)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.description, domain.description)
        assertEquals(dto.minAge, domain.minAge)
        assertEquals(dto.maxAge, domain.maxAge)
        assertEquals(dto.startTime, domain.startTime)
        assertEquals(dto.endTime, domain.endTime)
        assertEquals(dto.location, domain.location)
        assertEquals(dto.maxCapacity, domain.maxCapacity)
        assertEquals(dto.currentCapacity, domain.currentCapacity)
        assertEquals(dto.isAcceptingCheckIns, domain.isAcceptingCheckIns)
        assertEquals(dto.staffMembers, domain.staffMembers)
        assertEquals(dto.createdAt, domain.createdAt)
    }
    
    @Test
    fun `CheckInRecordDto toDomain maps correctly`() {
        // Given
        val dto = CheckInRecordDto(
            id = "record123",
            childId = "child123",
            serviceId = "service123",
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = "2024-01-01T11:00:00Z",
            checkedInBy = "user123",
            checkedOutBy = "user456",
            notes = "Test notes",
            status = "CHECKED_OUT"
        )
        
        // When
        val domain = dto.toDomain()
        
        // Then
        assertEquals(dto.id, domain.id)
        assertEquals(dto.childId, domain.childId)
        assertEquals(dto.serviceId, domain.serviceId)
        assertEquals(dto.checkInTime, domain.checkInTime)
        assertEquals(dto.checkOutTime, domain.checkOutTime)
        assertEquals(dto.checkedInBy, domain.checkedInBy)
        assertEquals(dto.checkedOutBy, domain.checkedOutBy)
        assertEquals(dto.notes, domain.notes)
        assertEquals(CheckInStatus.CHECKED_OUT, domain.status)
    }
    
    @Test
    fun `String toCheckInStatus maps correctly`() {
        // Test all valid status values
        assertEquals(CheckInStatus.CHECKED_IN, "CHECKED_IN".toCheckInStatus())
        assertEquals(CheckInStatus.CHECKED_OUT, "CHECKED_OUT".toCheckInStatus())
        assertEquals(CheckInStatus.NOT_IN_SERVICE, "NOT_IN_SERVICE".toCheckInStatus())
        
        // Test case insensitive
        assertEquals(CheckInStatus.CHECKED_IN, "checked_in".toCheckInStatus())
        assertEquals(CheckInStatus.CHECKED_OUT, "checked_out".toCheckInStatus())
        
        // Test invalid value defaults to NOT_IN_SERVICE
        assertEquals(CheckInStatus.NOT_IN_SERVICE, "INVALID_STATUS".toCheckInStatus())
        assertEquals(CheckInStatus.NOT_IN_SERVICE, "".toCheckInStatus())
    }
    
    @Test
    fun `CheckInStatus toApiString maps correctly`() {
        assertEquals("CHECKED_IN", CheckInStatus.CHECKED_IN.toApiString())
        assertEquals("CHECKED_OUT", CheckInStatus.CHECKED_OUT.toApiString())
        assertEquals("NOT_IN_SERVICE", CheckInStatus.NOT_IN_SERVICE.toApiString())
    }
    
    @Test
    fun `Child toRegistrationRequest maps correctly`() {
        // Given
        val child = Child(
            id = "child123",
            parentId = "parent123",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = "Vegetarian",
            emergencyContact = EmergencyContact(
                name = "Emergency Contact",
                phoneNumber = "+1234567890",
                relationship = "Parent"
            ),
            status = CheckInStatus.NOT_IN_SERVICE,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        // When
        val request = child.toRegistrationRequest()
        
        // Then
        assertEquals(child.name, request.name)
        assertEquals(child.dateOfBirth, request.dateOfBirth)
        assertEquals(child.medicalInfo, request.medicalInfo)
        assertEquals(child.dietaryRestrictions, request.dietaryRestrictions)
        assertEquals(child.emergencyContact.name, request.emergencyContact.name)
        assertEquals(child.emergencyContact.phoneNumber, request.emergencyContact.phoneNumber)
        assertEquals(child.emergencyContact.relationship, request.emergencyContact.relationship)
    }
    
    @Test
    fun `List mapping functions work correctly`() {
        // Given
        val childDtos = listOf(
            ChildDto(
                id = "child1",
                parentId = "parent123",
                name = "Child 1",
                dateOfBirth = "2020-01-01",
                emergencyContact = EmergencyContactDto("Contact 1", "+1111111111", "Parent"),
                status = "NOT_IN_SERVICE",
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            ChildDto(
                id = "child2",
                parentId = "parent123",
                name = "Child 2",
                dateOfBirth = "2021-01-01",
                emergencyContact = EmergencyContactDto("Contact 2", "+2222222222", "Parent"),
                status = "CHECKED_IN",
                currentServiceId = "service123",
                checkInTime = "2024-01-01T10:00:00Z",
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T10:00:00Z"
            )
        )
        
        // When
        val domainChildren = childDtos.toDomain()
        val backToDtos = domainChildren.toDto()
        
        // Then
        assertEquals(2, domainChildren.size)
        assertEquals(2, backToDtos.size)
        assertEquals("child1", domainChildren[0].id)
        assertEquals("child2", domainChildren[1].id)
        assertEquals(CheckInStatus.NOT_IN_SERVICE, domainChildren[0].status)
        assertEquals(CheckInStatus.CHECKED_IN, domainChildren[1].status)
        
        // Verify round-trip conversion
        assertEquals(childDtos[0].id, backToDtos[0].id)
        assertEquals(childDtos[1].id, backToDtos[1].id)
        assertEquals(childDtos[0].status, backToDtos[0].status)
        assertEquals(childDtos[1].status, backToDtos[1].status)
    }
}