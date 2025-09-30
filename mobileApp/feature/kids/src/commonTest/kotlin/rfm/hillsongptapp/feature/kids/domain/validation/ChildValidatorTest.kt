package rfm.hillsongptapp.feature.kids.domain.validation

import rfm.hillsongptapp.core.data.model.EmergencyContact
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChildValidatorTest {
    
    private val validator = ChildValidator()
    
    @Test
    fun `validateName should return Valid for valid names`() {
        val validNames = listOf(
            "John Doe",
            "Mary-Jane",
            "O'Connor",
            "Jean-Luc",
            "Anna Maria"
        )
        
        validNames.forEach { name ->
            val result = validator.validateName(name)
            assertTrue(result.isValid, "Name '$name' should be valid")
        }
    }
    
    @Test
    fun `validateName should return Invalid for invalid names`() {
        val testCases = mapOf(
            "" to "Child name is required",
            "   " to "Child name is required",
            "A" to "Name must be at least 2 characters",
            "A".repeat(51) to "Name must not exceed 50 characters",
            "John123" to "Name can only contain letters, spaces, hyphens, and apostrophes",
            "John@Doe" to "Name can only contain letters, spaces, hyphens, and apostrophes"
        )
        
        testCases.forEach { (name, expectedMessage) ->
            val result = validator.validateName(name)
            assertTrue(result.isInvalid, "Name '$name' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `validateDateOfBirth should return Valid for valid dates`() {
        val validDates = listOf(
            "2020-01-01",
            "2015-12-31",
            "2010-06-15"
        )
        
        validDates.forEach { date ->
            val result = validator.validateDateOfBirth(date)
            assertTrue(result.isValid, "Date '$date' should be valid")
        }
    }
    
    @Test
    fun `validateDateOfBirth should return Invalid for invalid dates`() {
        val testCases = mapOf(
            "" to "Date of birth is required",
            "2025-12-31" to "Date of birth cannot be in the future",
            "2000-01-01" to "Child must be under 18 years old",
            "invalid-date" to "Date must be in YYYY-MM-DD format",
            "01-01-2020" to "Date must be in YYYY-MM-DD format"
        )
        
        testCases.forEach { (date, expectedMessage) ->
            val result = validator.validateDateOfBirth(date)
            assertTrue(result.isInvalid, "Date '$date' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `validateMedicalInfo should return Valid for valid medical info`() {
        val validMedicalInfo = listOf(
            null,
            "",
            "   ",
            "No known allergies",
            "Allergic to peanuts",
            "A".repeat(500) // Max length
        )
        
        validMedicalInfo.forEach { info ->
            val result = validator.validateMedicalInfo(info)
            assertTrue(result.isValid, "Medical info '$info' should be valid")
        }
    }
    
    @Test
    fun `validateMedicalInfo should return Invalid for too long medical info`() {
        val tooLongInfo = "A".repeat(501)
        val result = validator.validateMedicalInfo(tooLongInfo)
        
        assertTrue(result.isInvalid)
        assertEquals("Medical information must not exceed 500 characters", result.errorMessage)
    }
    
    @Test
    fun `validateDietaryRestrictions should return Valid for valid dietary restrictions`() {
        val validRestrictions = listOf(
            null,
            "",
            "   ",
            "Vegetarian",
            "No dairy",
            "A".repeat(500) // Max length
        )
        
        validRestrictions.forEach { restriction ->
            val result = validator.validateDietaryRestrictions(restriction)
            assertTrue(result.isValid, "Dietary restriction '$restriction' should be valid")
        }
    }
    
    @Test
    fun `validateDietaryRestrictions should return Invalid for too long restrictions`() {
        val tooLongRestriction = "A".repeat(501)
        val result = validator.validateDietaryRestrictions(tooLongRestriction)
        
        assertTrue(result.isInvalid)
        assertEquals("Dietary restrictions must not exceed 500 characters", result.errorMessage)
    }
    
    @Test
    fun `validateEmergencyContactName should return Valid for valid names`() {
        val validNames = listOf(
            "John Doe",
            "Mary-Jane Smith",
            "O'Connor"
        )
        
        validNames.forEach { name ->
            val result = validator.validateEmergencyContactName(name)
            assertTrue(result.isValid, "Contact name '$name' should be valid")
        }
    }
    
    @Test
    fun `validateEmergencyContactName should return Invalid for invalid names`() {
        val testCases = mapOf(
            "" to "Emergency contact name is required",
            "A" to "Contact name must be at least 2 characters",
            "A".repeat(51) to "Contact name must not exceed 50 characters",
            "John123" to "Contact name can only contain letters, spaces, hyphens, and apostrophes"
        )
        
        testCases.forEach { (name, expectedMessage) ->
            val result = validator.validateEmergencyContactName(name)
            assertTrue(result.isInvalid, "Contact name '$name' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `validateEmergencyContactPhone should return Valid for valid phone numbers`() {
        val validPhones = listOf(
            "1234567890",
            "+1234567890",
            "12345678901234",
            "(123) 456-7890",
            "123-456-7890"
        )
        
        validPhones.forEach { phone ->
            val result = validator.validateEmergencyContactPhone(phone)
            assertTrue(result.isValid, "Phone '$phone' should be valid")
        }
    }
    
    @Test
    fun `validateEmergencyContactPhone should return Invalid for invalid phone numbers`() {
        val testCases = mapOf(
            "" to "Emergency contact phone number is required",
            "123456789" to "Phone number must be at least 10 digits",
            "123456789012345678901" to "Phone number must not exceed 15 digits",
            "abc1234567" to "Please enter a valid phone number"
        )
        
        testCases.forEach { (phone, expectedMessage) ->
            val result = validator.validateEmergencyContactPhone(phone)
            assertTrue(result.isInvalid, "Phone '$phone' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `validateEmergencyContactRelationship should return Valid for valid relationships`() {
        val validRelationships = listOf(
            "parent",
            "mother",
            "father",
            "guardian",
            "grandparent",
            "grandmother",
            "grandfather",
            "aunt",
            "uncle",
            "family friend"
        )
        
        validRelationships.forEach { relationship ->
            val result = validator.validateEmergencyContactRelationship(relationship)
            assertTrue(result.isValid, "Relationship '$relationship' should be valid")
        }
    }
    
    @Test
    fun `validateEmergencyContactRelationship should return Invalid for invalid relationships`() {
        val testCases = mapOf(
            "" to "Emergency contact relationship is required",
            "friend" to "Please select a valid relationship (e.g., Parent, Guardian, Grandparent)",
            "neighbor" to "Please select a valid relationship (e.g., Parent, Guardian, Grandparent)",
            "A".repeat(51) to "Relationship must not exceed 50 characters"
        )
        
        testCases.forEach { (relationship, expectedMessage) ->
            val result = validator.validateEmergencyContactRelationship(relationship)
            assertTrue(result.isInvalid, "Relationship '$relationship' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `validateEmergencyContact should return valid result for valid contact`() {
        val validContact = EmergencyContact(
            name = "John Doe",
            phoneNumber = "1234567890",
            relationship = "parent"
        )
        
        val result = validator.validateEmergencyContact(validContact)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateEmergencyContact should return invalid result for invalid contact`() {
        val invalidContact = EmergencyContact(
            name = "",
            phoneNumber = "123",
            relationship = "friend"
        )
        
        val result = validator.validateEmergencyContact(invalidContact)
        assertFalse(result.isValid)
        assertEquals(3, result.allErrorMessages.size)
    }
    
    @Test
    fun `validateChildForRegistration should return valid result for valid data`() {
        val validEmergencyContact = EmergencyContact(
            name = "John Doe",
            phoneNumber = "1234567890",
            relationship = "parent"
        )
        
        val result = validator.validateChildForRegistration(
            name = "Jane Doe",
            dateOfBirth = "2015-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = null,
            emergencyContact = validEmergencyContact
        )
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateChildForRegistration should return invalid result for invalid data`() {
        val invalidEmergencyContact = EmergencyContact(
            name = "",
            phoneNumber = "123",
            relationship = "friend"
        )
        
        val result = validator.validateChildForRegistration(
            name = "",
            dateOfBirth = "invalid-date",
            medicalInfo = "A".repeat(501),
            dietaryRestrictions = null,
            emergencyContact = invalidEmergencyContact
        )
        
        assertFalse(result.isValid)
        assertTrue(result.allErrorMessages.size > 5)
    }
    
    @Test
    fun `validateChildId should return Valid for valid IDs`() {
        val validIds = listOf(
            "child123",
            "abc-def-ghi",
            "1234567890"
        )
        
        validIds.forEach { id ->
            val result = validator.validateChildId(id)
            assertTrue(result.isValid, "Child ID '$id' should be valid")
        }
    }
    
    @Test
    fun `validateChildId should return Invalid for invalid IDs`() {
        val testCases = mapOf(
            "" to "Child ID is required",
            "ab" to "Invalid child ID format"
        )
        
        testCases.forEach { (id, expectedMessage) ->
            val result = validator.validateChildId(id)
            assertTrue(result.isInvalid, "Child ID '$id' should be invalid")
            assertEquals(expectedMessage, result.errorMessage)
        }
    }
    
    @Test
    fun `getValidationSummary should return appropriate messages`() {
        val validResult = FormValidationResult.valid(listOf("name", "dateOfBirth"))
        assertEquals("All information is valid", validator.getValidationSummary(validResult))
        
        val invalidResult = FormValidationResult.from(
            "name" to ValidationResult.Invalid("Name is required"),
            "phone" to ValidationResult.Invalid("Phone is invalid")
        )
        assertEquals("Please fix 2 validation errors", validator.getValidationSummary(invalidResult))
        
        val singleErrorResult = FormValidationResult.from(
            "name" to ValidationResult.Invalid("Name is required")
        )
        assertEquals("Please fix 1 validation error", validator.getValidationSummary(singleErrorResult))
    }
}