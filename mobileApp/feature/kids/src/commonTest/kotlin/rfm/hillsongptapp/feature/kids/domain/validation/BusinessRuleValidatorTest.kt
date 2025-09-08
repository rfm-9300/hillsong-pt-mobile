package rfm.hillsongptapp.feature.kids.domain.validation

import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BusinessRuleValidatorTest {
    
    private val validator = BusinessRuleValidator()
    
    private val sampleChild = Child(
        id = "child123",
        parentId = "parent123",
        name = "John Doe",
        dateOfBirth = "2015-01-01", // 10 years old
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Jane Doe",
            phoneNumber = "1234567890",
            relationship = "parent"
        ),
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private val sampleService = KidsService(
        id = "service123",
        name = "Kids Church",
        description = "Sunday service for kids",
        minAge = 5,
        maxAge = 12,
        startTime = "09:00",
        endTime = "10:30",
        location = "Kids Room",
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff1", "staff2"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    @Test
    fun `validateAgeRequirements should return Valid for eligible child`() {
        val result = validator.validateAgeRequirements(sampleChild, sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateAgeRequirements should return Invalid for child too young`() {
        val youngChild = sampleChild.copy(dateOfBirth = "2022-01-01") // 3 years old
        val result = validator.validateAgeRequirements(youngChild, sampleService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("must be at least 5 years old"))
        assertTrue(result.errorMessage!!.contains("Current age: 3 years"))
    }
    
    @Test
    fun `validateAgeRequirements should return Invalid for child too old`() {
        val oldChild = sampleChild.copy(dateOfBirth = "2010-01-01") // 15 years old
        val result = validator.validateAgeRequirements(oldChild, sampleService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("must be under 12 years old"))
        assertTrue(result.errorMessage!!.contains("Current age: 15 years"))
    }
    
    @Test
    fun `validateServiceCapacity should return Valid for service with available capacity`() {
        val result = validator.validateServiceCapacity(sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateServiceCapacity should return Invalid for service at capacity`() {
        val fullService = sampleService.copy(currentCapacity = 20, maxCapacity = 20)
        val result = validator.validateServiceCapacity(fullService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("at full capacity (20/20)"))
    }
    
    @Test
    fun `validateServiceCapacity should return Invalid for service over capacity`() {
        val overCapacityService = sampleService.copy(currentCapacity = 25, maxCapacity = 20)
        val result = validator.validateServiceCapacity(overCapacityService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("at full capacity (25/20)"))
    }
    
    @Test
    fun `validateServiceCapacity should return Invalid for negative capacity`() {
        val invalidService = sampleService.copy(currentCapacity = -1)
        val result = validator.validateServiceCapacity(invalidService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("Invalid capacity data"))
    }
    
    @Test
    fun `validateServiceAvailability should return Valid for accepting service`() {
        val result = validator.validateServiceAvailability(sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateServiceAvailability should return Invalid for non-accepting service`() {
        val closedService = sampleService.copy(isAcceptingCheckIns = false)
        val result = validator.validateServiceAvailability(closedService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("not currently accepting check-ins"))
    }
    
    @Test
    fun `validateChildStatusForCheckIn should return Valid for available child`() {
        val availableChild = sampleChild.copy(status = CheckInStatus.NOT_IN_SERVICE)
        val result = validator.validateChildStatusForCheckIn(availableChild)
        assertTrue(result.isValid)
        
        val checkedOutChild = sampleChild.copy(status = CheckInStatus.CHECKED_OUT)
        val result2 = validator.validateChildStatusForCheckIn(checkedOutChild)
        assertTrue(result2.isValid)
    }
    
    @Test
    fun `validateChildStatusForCheckIn should return Invalid for already checked in child`() {
        val checkedInChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "other-service"
        )
        val result = validator.validateChildStatusForCheckIn(checkedInChild)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("already checked into"))
    }
    
    @Test
    fun `validateChildStatusForCheckOut should return Valid for checked in child`() {
        val checkedInChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        val result = validator.validateChildStatusForCheckOut(checkedInChild)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateChildStatusForCheckOut should return Invalid for already checked out child`() {
        val checkedOutChild = sampleChild.copy(status = CheckInStatus.CHECKED_OUT)
        val result = validator.validateChildStatusForCheckOut(checkedOutChild)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("already checked out"))
    }
    
    @Test
    fun `validateChildStatusForCheckOut should return Invalid for child not in service`() {
        val notInServiceChild = sampleChild.copy(status = CheckInStatus.NOT_IN_SERVICE)
        val result = validator.validateChildStatusForCheckOut(notInServiceChild)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("not currently in any service"))
    }
    
    @Test
    fun `validateChildStatusForCheckOut should return Invalid for invalid check-in data`() {
        val invalidChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = null
        )
        val result = validator.validateChildStatusForCheckOut(invalidChild)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("Invalid check-in data"))
    }
    
    @Test
    fun `validateCheckInEligibility should return valid result for eligible check-in`() {
        val result = validator.validateCheckInEligibility(sampleChild, sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateCheckInEligibility should return invalid result for ineligible check-in`() {
        val checkedInChild = sampleChild.copy(status = CheckInStatus.CHECKED_IN)
        val fullService = sampleService.copy(currentCapacity = 20, maxCapacity = 20)
        
        val result = validator.validateCheckInEligibility(checkedInChild, fullService)
        assertFalse(result.isValid)
        assertTrue(result.allErrorMessages.size >= 2)
    }
    
    @Test
    fun `validateCheckOutEligibility should return valid result for eligible check-out`() {
        val checkedInChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        val result = validator.validateCheckOutEligibility(checkedInChild)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateCheckOutEligibility should return invalid result for ineligible check-out`() {
        val checkedOutChild = sampleChild.copy(status = CheckInStatus.CHECKED_OUT)
        val result = validator.validateCheckOutEligibility(checkedOutChild)
        assertFalse(result.isValid)
    }
    
    @Test
    fun `validateServiceTiming should return Valid for service with valid timing`() {
        val result = validator.validateServiceTiming(sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateServiceTiming should return Invalid for service with missing timing`() {
        val invalidService = sampleService.copy(startTime = "", endTime = "")
        val result = validator.validateServiceTiming(invalidService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("timing information is incomplete"))
    }
    
    @Test
    fun `validateConcurrentCheckIn should return Valid for unchanged capacity`() {
        val result = validator.validateConcurrentCheckIn(sampleChild, sampleService, 10)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateConcurrentCheckIn should return Invalid for changed capacity`() {
        val result = validator.validateConcurrentCheckIn(sampleChild, sampleService, 15)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("capacity has changed"))
    }
    
    @Test
    fun `validateConcurrentCheckIn should return Invalid for service that became full`() {
        val result = validator.validateConcurrentCheckIn(sampleChild, sampleService, 20)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("became full while processing"))
    }
    
    @Test
    fun `validateParentAuthorization should return Valid for authorized parent`() {
        val result = validator.validateParentAuthorization(sampleChild, "parent123")
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateParentAuthorization should return Invalid for unauthorized parent`() {
        val result = validator.validateParentAuthorization(sampleChild, "other-parent")
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("not authorized to manage"))
    }
    
    @Test
    fun `validateServiceStaffing should return Valid for staffed service`() {
        val result = validator.validateServiceStaffing(sampleService)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateServiceStaffing should return Invalid for unstaffed service`() {
        val unstaffedService = sampleService.copy(staffMembers = emptyList())
        val result = validator.validateServiceStaffing(unstaffedService)
        
        assertTrue(result.isInvalid)
        assertTrue(result.errorMessage!!.contains("no assigned staff"))
    }
    
    @Test
    fun `getComprehensiveCheckInValidation should return valid result for valid check-in`() {
        val result = validator.getComprehensiveCheckInValidation(sampleChild, sampleService, "parent123")
        assertTrue(result.isValid)
    }
    
    @Test
    fun `getComprehensiveCheckInValidation should return invalid result for invalid check-in`() {
        val checkedInChild = sampleChild.copy(status = CheckInStatus.CHECKED_IN)
        val fullService = sampleService.copy(currentCapacity = 20, maxCapacity = 20)
        
        val result = validator.getComprehensiveCheckInValidation(checkedInChild, fullService, "other-parent")
        assertFalse(result.isValid)
        assertTrue(result.allErrorMessages.size >= 3)
    }
    
    @Test
    fun `getComprehensiveCheckOutValidation should return valid result for valid check-out`() {
        val checkedInChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service123"
        )
        val result = validator.getComprehensiveCheckOutValidation(checkedInChild, "parent123")
        assertTrue(result.isValid)
    }
    
    @Test
    fun `getComprehensiveCheckOutValidation should return invalid result for invalid check-out`() {
        val checkedOutChild = sampleChild.copy(status = CheckInStatus.CHECKED_OUT)
        val result = validator.getComprehensiveCheckOutValidation(checkedOutChild, "other-parent")
        assertFalse(result.isValid)
        assertTrue(result.allErrorMessages.size >= 2)
    }
    
    @Test
    fun `getBusinessRuleSummary should return appropriate messages`() {
        val validResult = FormValidationResult.valid(listOf("test"))
        assertEquals("All business rules satisfied", validator.getBusinessRuleSummary(validResult))
        
        val invalidResult = FormValidationResult.from(
            "test" to ValidationResult.Invalid("Test error message")
        )
        assertEquals("Test error message", validator.getBusinessRuleSummary(invalidResult))
        
        val emptyInvalidResult = FormValidationResult.from(
            "test" to ValidationResult.Valid
        )
        assertEquals("All business rules satisfied", validator.getBusinessRuleSummary(emptyInvalidResult))
    }
}