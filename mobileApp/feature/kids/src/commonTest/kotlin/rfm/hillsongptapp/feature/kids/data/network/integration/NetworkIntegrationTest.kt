package rfm.hillsongptapp.feature.kids.data.network.integration

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.data.network.mapper.*
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.domain.model.*

/**
 * Integration tests to verify the complete network layer works together
 */
class NetworkIntegrationTest {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Test
    fun `complete child workflow - DTO to domain and back`() {
        // Given - Create a complete child DTO
        val originalDto = ChildDto(
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
        
        // When - Convert to domain and back
        val domainChild = originalDto.toDomain()
        val backToDto = domainChild.toDto()
        
        // Then - Verify round-trip conversion preserves data
        assertEquals(originalDto.id, backToDto.id)
        assertEquals(originalDto.parentId, backToDto.parentId)
        assertEquals(originalDto.name, backToDto.name)
        assertEquals(originalDto.dateOfBirth, backToDto.dateOfBirth)
        assertEquals(originalDto.medicalInfo, backToDto.medicalInfo)
        assertEquals(originalDto.dietaryRestrictions, backToDto.dietaryRestrictions)
        assertEquals(originalDto.emergencyContact.name, backToDto.emergencyContact.name)
        assertEquals(originalDto.emergencyContact.phoneNumber, backToDto.emergencyContact.phoneNumber)
        assertEquals(originalDto.emergencyContact.relationship, backToDto.emergencyContact.relationship)
        assertEquals(originalDto.status, backToDto.status)
        assertEquals(originalDto.currentServiceId, backToDto.currentServiceId)
        assertEquals(originalDto.checkInTime, backToDto.checkInTime)
        assertEquals(originalDto.checkOutTime, backToDto.checkOutTime)
        assertEquals(originalDto.createdAt, backToDto.createdAt)
        assertEquals(originalDto.updatedAt, backToDto.updatedAt)
        
        // Verify domain model business logic works
        assertTrue(domainChild.isCheckedIn())
        assertTrue(domainChild.calculateAge() > 0)
    }
    
    @Test
    fun `complete service workflow - DTO to domain and back`() {
        // Given - Create a complete service DTO
        val originalDto = KidsServiceDto(
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
        
        // When - Convert to domain and back
        val domainService = originalDto.toDomain()
        val backToDto = domainService.toDto()
        
        // Then - Verify round-trip conversion preserves data
        assertEquals(originalDto.id, backToDto.id)
        assertEquals(originalDto.name, backToDto.name)
        assertEquals(originalDto.description, backToDto.description)
        assertEquals(originalDto.minAge, backToDto.minAge)
        assertEquals(originalDto.maxAge, backToDto.maxAge)
        assertEquals(originalDto.startTime, backToDto.startTime)
        assertEquals(originalDto.endTime, backToDto.endTime)
        assertEquals(originalDto.location, backToDto.location)
        assertEquals(originalDto.maxCapacity, backToDto.maxCapacity)
        assertEquals(originalDto.currentCapacity, backToDto.currentCapacity)
        assertEquals(originalDto.isAcceptingCheckIns, backToDto.isAcceptingCheckIns)
        assertEquals(originalDto.staffMembers, backToDto.staffMembers)
        assertEquals(originalDto.createdAt, backToDto.createdAt)
        
        // Verify domain model business logic works
        assertTrue(domainService.hasAvailableSpots())
        assertTrue(domainService.canAcceptCheckIn())
        assertEquals(15, domainService.getAvailableSpots())
        assertTrue(domainService.isAgeEligible(8))
        assertFalse(domainService.isAgeEligible(15))
    }
    
    @Test
    fun `error handling integration test`() {
        // Test that error codes map correctly to domain errors
        val networkError = 404.toKidsManagementError("Child not found")
        assertTrue(networkError is KidsManagementError.ChildNotFound)
        
        val validationError = 400.toKidsManagementError("Invalid data")
        assertTrue(validationError is KidsManagementError.ValidationError)
        
        val serverError = 500.toKidsManagementError("Internal error")
        assertTrue(serverError is KidsManagementError.ServerError)
        
        val unauthorizedError = 401.toKidsManagementError()
        assertTrue(unauthorizedError is KidsManagementError.Unauthorized)
    }
    
    @Test
    fun `child eligibility for service integration test`() {
        // Given - Create a child and service
        val childDto = ChildDto(
            id = "child123",
            parentId = "parent123",
            name = "8 Year Old Child",
            dateOfBirth = "2016-01-01", // 8 years old
            emergencyContact = EmergencyContactDto("Contact", "+1234567890", "Parent"),
            status = "NOT_IN_SERVICE",
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        val serviceDto = KidsServiceDto(
            id = "service123",
            name = "Kids Church",
            description = "For ages 5-10",
            minAge = 5,
            maxAge = 10,
            startTime = "2024-01-01T10:00:00Z",
            endTime = "2024-01-01T11:00:00Z",
            location = "Kids Room",
            maxCapacity = 20,
            currentCapacity = 5,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff1"),
            createdAt = "2024-01-01T00:00:00Z"
        )
        
        // When - Convert to domain models
        val child = childDto.toDomain()
        val service = serviceDto.toDomain()
        
        // Then - Verify eligibility logic works
        assertTrue(child.isEligibleForService(service))
        assertTrue(service.isAgeEligible(child.calculateAge()))
        assertTrue(service.canAcceptCheckIn())
    }
    
    @Test
    fun `check-in record workflow integration test`() {
        // Given - Create a complete check-in record
        val recordDto = CheckInRecordDto(
            id = "record123",
            childId = "child123",
            serviceId = "service123",
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "user123",
            checkedOutBy = null,
            notes = "Regular check-in",
            status = "CHECKED_IN"
        )
        
        // When - Convert to domain and back
        val domainRecord = recordDto.toDomain()
        val backToDto = domainRecord.toDto()
        
        // Then - Verify conversion and business logic
        assertEquals(recordDto.id, backToDto.id)
        assertEquals(recordDto.childId, backToDto.childId)
        assertEquals(recordDto.serviceId, backToDto.serviceId)
        assertEquals(recordDto.status, backToDto.status)
        
        assertTrue(domainRecord.isCurrentlyCheckedIn())
        assertFalse(domainRecord.isSessionComplete())
        assertNull(domainRecord.getSessionDurationMinutes())
    }
    
    @Test
    fun `API request DTOs creation integration test`() {
        // Given - Create a domain child
        val child = Child(
            id = "child123",
            parentId = "parent123",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = null,
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
        
        // When - Create API request DTOs
        val registrationRequest = child.toRegistrationRequest()
        val updateRequest = child.toUpdateRequest()
        
        // Then - Verify request DTOs are created correctly
        assertEquals(child.name, registrationRequest.name)
        assertEquals(child.dateOfBirth, registrationRequest.dateOfBirth)
        assertEquals(child.medicalInfo, registrationRequest.medicalInfo)
        assertEquals(child.dietaryRestrictions, registrationRequest.dietaryRestrictions)
        assertEquals(child.emergencyContact.name, registrationRequest.emergencyContact.name)
        
        assertEquals(child.name, updateRequest.name)
        assertEquals(child.dateOfBirth, updateRequest.dateOfBirth)
        assertEquals(child.medicalInfo, updateRequest.medicalInfo)
        assertEquals(child.dietaryRestrictions, updateRequest.dietaryRestrictions)
        assertEquals(child.emergencyContact.name, updateRequest.emergencyContact?.name)
    }
}