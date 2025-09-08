package rfm.hillsongptapp.feature.kids.ui.registration

import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import kotlin.test.*

class ChildRegistrationUiStateTest {
    
    @Test
    fun `areRequiredFieldsFilled should return false when fields are empty`() {
        val state = ChildRegistrationUiState()
        
        assertFalse(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `areRequiredFieldsFilled should return true when all required fields are filled`() {
        val state = ChildRegistrationUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        assertTrue(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `areRequiredFieldsFilled should return false when some required fields are missing`() {
        val state = ChildRegistrationUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15"
            // Missing emergency contact fields
        )
        
        assertFalse(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `hasValidationErrors should return false when no errors`() {
        val state = ChildRegistrationUiState()
        
        assertFalse(state.hasValidationErrors)
    }
    
    @Test
    fun `hasValidationErrors should return true when there are errors`() {
        val state = ChildRegistrationUiState(
            nameError = "Name is required"
        )
        
        assertTrue(state.hasValidationErrors)
    }
    
    @Test
    fun `isFormValid should return true when form is complete and valid`() {
        val state = ChildRegistrationUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        assertTrue(state.isFormValid)
    }
    
    @Test
    fun `isFormValid should return false when required fields are missing`() {
        val state = ChildRegistrationUiState(
            childName = "John Doe"
            // Missing other required fields
        )
        
        assertFalse(state.isFormValid)
    }
    
    @Test
    fun `isFormValid should return false when there are validation errors`() {
        val state = ChildRegistrationUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother",
            nameError = "Invalid name" // Has validation error
        )
        
        assertFalse(state.isFormValid)
    }
    
    @Test
    fun `getCalculatedAge should return correct age for valid date`() {
        val state = ChildRegistrationUiState(
            dateOfBirth = "2015-05-15"
        )
        
        val age = state.getCalculatedAge()
        assertEquals(10, age) // 2025 - 2015 = 10
    }
    
    @Test
    fun `getCalculatedAge should return null for empty date`() {
        val state = ChildRegistrationUiState(
            dateOfBirth = ""
        )
        
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `getCalculatedAge should return null for invalid date format`() {
        val state = ChildRegistrationUiState(
            dateOfBirth = "invalid-date"
        )
        
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `getCalculatedAge should return null when there are date errors`() {
        val state = ChildRegistrationUiState(
            dateOfBirth = "2015-05-15",
            dateOfBirthError = "Invalid date"
        )
        
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `createEmergencyContact should return contact when all fields are filled`() {
        val state = ChildRegistrationUiState(
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        val contact = state.createEmergencyContact()
        
        assertNotNull(contact)
        assertEquals("Jane Doe", contact.name)
        assertEquals("(555) 123-4567", contact.phoneNumber)
        assertEquals("Mother", contact.relationship)
    }
    
    @Test
    fun `createEmergencyContact should return null when fields are missing`() {
        val state = ChildRegistrationUiState(
            emergencyContactName = "Jane Doe"
            // Missing phone and relationship
        )
        
        val contact = state.createEmergencyContact()
        
        assertNull(contact)
    }
    
    @Test
    fun `createEmergencyContact should trim whitespace from fields`() {
        val state = ChildRegistrationUiState(
            emergencyContactName = "  Jane Doe  ",
            emergencyContactPhone = "  (555) 123-4567  ",
            emergencyContactRelationship = "  Mother  "
        )
        
        val contact = state.createEmergencyContact()
        
        assertNotNull(contact)
        assertEquals("Jane Doe", contact.name)
        assertEquals("(555) 123-4567", contact.phoneNumber)
        assertEquals("Mother", contact.relationship)
    }
    
    @Test
    fun `createEmergencyContact should return null when fields are blank after trimming`() {
        val state = ChildRegistrationUiState(
            emergencyContactName = "   ",
            emergencyContactPhone = "   ",
            emergencyContactRelationship = "   "
        )
        
        val contact = state.createEmergencyContact()
        
        assertNull(contact)
    }
}