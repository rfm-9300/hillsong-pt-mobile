package rfm.hillsongptapp.feature.kids.ui.edit

import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import kotlin.test.*

class ChildEditUiStateTest {
    
    private val testChild = Child(
        id = "child_123",
        parentId = "parent_123",
        name = "John Doe",
        dateOfBirth = "2015-05-15",
        medicalInfo = "No allergies",
        dietaryRestrictions = "Vegetarian",
        emergencyContact = EmergencyContact(
            name = "Jane Doe",
            phoneNumber = "(555) 123-4567",
            relationship = "Mother"
        ),
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )
    
    @Test
    fun `initial state should have default values`() {
        val state = ChildEditUiState()
        
        assertNull(state.originalChild)
        assertEquals("", state.childName)
        assertEquals("", state.dateOfBirth)
        assertEquals("", state.medicalInfo)
        assertEquals("", state.dietaryRestrictions)
        assertEquals("", state.emergencyContactName)
        assertEquals("", state.emergencyContactPhone)
        assertEquals("", state.emergencyContactRelationship)
        assertNull(state.nameError)
        assertNull(state.dateOfBirthError)
        assertNull(state.emergencyContactNameError)
        assertNull(state.emergencyContactPhoneError)
        assertNull(state.emergencyContactRelationshipError)
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertNull(state.error)
        assertFalse(state.showDatePicker)
        assertFalse(state.showDiscardChangesDialog)
        assertFalse(state.showSuccessDialog)
    }
    
    @Test
    fun `fromChild should populate state with child data`() {
        val state = ChildEditUiState.fromChild(testChild)
        
        assertEquals(testChild, state.originalChild)
        assertEquals("John Doe", state.childName)
        assertEquals("2015-05-15", state.dateOfBirth)
        assertEquals("No allergies", state.medicalInfo)
        assertEquals("Vegetarian", state.dietaryRestrictions)
        assertEquals("Jane Doe", state.emergencyContactName)
        assertEquals("(555) 123-4567", state.emergencyContactPhone)
        assertEquals("Mother", state.emergencyContactRelationship)
    }
    
    @Test
    fun `fromChild should handle null optional fields`() {
        val childWithNulls = testChild.copy(
            medicalInfo = null,
            dietaryRestrictions = null
        )
        
        val state = ChildEditUiState.fromChild(childWithNulls)
        
        assertEquals("", state.medicalInfo)
        assertEquals("", state.dietaryRestrictions)
    }
    
    @Test
    fun `areRequiredFieldsFilled should return true when all required fields are filled`() {
        val state = ChildEditUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        assertTrue(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `areRequiredFieldsFilled should return false when required fields are missing`() {
        val state = ChildEditUiState(
            childName = "John Doe",
            dateOfBirth = "", // Missing
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        assertFalse(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `hasValidationErrors should return true when there are validation errors`() {
        val state = ChildEditUiState(
            nameError = "Name is required"
        )
        
        assertTrue(state.hasValidationErrors)
    }
    
    @Test
    fun `hasValidationErrors should return false when there are no validation errors`() {
        val state = ChildEditUiState()
        
        assertFalse(state.hasValidationErrors)
    }
    
    @Test
    fun `isFormValid should return true when form is complete and valid`() {
        val state = ChildEditUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother"
        )
        
        assertTrue(state.isFormValid)
    }
    
    @Test
    fun `isFormValid should return false when form has validation errors`() {
        val state = ChildEditUiState(
            childName = "John Doe",
            dateOfBirth = "2015-05-15",
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "(555) 123-4567",
            emergencyContactRelationship = "Mother",
            nameError = "Name is invalid"
        )
        
        assertFalse(state.isFormValid)
    }
    
    @Test
    fun `hasChanges should return false when no changes made`() {
        val state = ChildEditUiState.fromChild(testChild)
        
        assertFalse(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when child name changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            childName = "Jane Doe"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when date of birth changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            dateOfBirth = "2016-05-15"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when medical info changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            medicalInfo = "Peanut allergy"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when dietary restrictions changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            dietaryRestrictions = "Vegan"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when emergency contact name changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            emergencyContactName = "John Smith"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when emergency contact phone changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            emergencyContactPhone = "(555) 987-6543"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should return true when emergency contact relationship changed`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            emergencyContactRelationship = "Father"
        )
        
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `hasChanges should handle whitespace trimming`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            childName = "John Doe " // Extra space
        )
        
        assertFalse(state.hasChanges) // Should be trimmed and match original
    }
    
    @Test
    fun `hasChanges should handle null to empty string conversion`() {
        val childWithNullMedicalInfo = testChild.copy(medicalInfo = null)
        val state = ChildEditUiState.fromChild(childWithNullMedicalInfo).copy(
            medicalInfo = "" // Empty string should match null
        )
        
        assertFalse(state.hasChanges)
    }
    
    @Test
    fun `getCalculatedAge should return correct age`() {
        val state = ChildEditUiState(
            dateOfBirth = "2015-05-15"
        )
        
        val age = state.getCalculatedAge()
        assertEquals(10, age) // 2025 - 2015 = 10
    }
    
    @Test
    fun `getCalculatedAge should return null for invalid date`() {
        val state = ChildEditUiState(
            dateOfBirth = "invalid-date"
        )
        
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `getCalculatedAge should return null when date has error`() {
        val state = ChildEditUiState(
            dateOfBirth = "2015-05-15",
            dateOfBirthError = "Invalid date"
        )
        
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `createEmergencyContact should create contact with valid data`() {
        val state = ChildEditUiState(
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
    fun `createEmergencyContact should return null with missing data`() {
        val state = ChildEditUiState(
            emergencyContactName = "Jane Doe",
            emergencyContactPhone = "", // Missing
            emergencyContactRelationship = "Mother"
        )
        
        val contact = state.createEmergencyContact()
        
        assertNull(contact)
    }
    
    @Test
    fun `createEmergencyContact should trim whitespace`() {
        val state = ChildEditUiState(
            emergencyContactName = " Jane Doe ",
            emergencyContactPhone = " (555) 123-4567 ",
            emergencyContactRelationship = " Mother "
        )
        
        val contact = state.createEmergencyContact()
        
        assertNotNull(contact)
        assertEquals("Jane Doe", contact.name)
        assertEquals("(555) 123-4567", contact.phoneNumber)
        assertEquals("Mother", contact.relationship)
    }
    
    @Test
    fun `createUpdatedChild should create child with updated data`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            childName = "Jane Smith",
            medicalInfo = "Peanut allergy"
        )
        
        val updatedChild = state.createUpdatedChild()
        
        assertNotNull(updatedChild)
        assertEquals("Jane Smith", updatedChild.name)
        assertEquals("Peanut allergy", updatedChild.medicalInfo)
        assertEquals(testChild.id, updatedChild.id) // Should preserve ID
        assertEquals(testChild.parentId, updatedChild.parentId) // Should preserve parent ID
        assertEquals(testChild.status, updatedChild.status) // Should preserve status
    }
    
    @Test
    fun `createUpdatedChild should return null without original child`() {
        val state = ChildEditUiState(
            childName = "Jane Smith"
        )
        
        val updatedChild = state.createUpdatedChild()
        
        assertNull(updatedChild)
    }
    
    @Test
    fun `createUpdatedChild should return null with invalid emergency contact`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            emergencyContactPhone = "" // Invalid
        )
        
        val updatedChild = state.createUpdatedChild()
        
        assertNull(updatedChild)
    }
    
    @Test
    fun `createUpdatedChild should handle null optional fields`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            medicalInfo = "",
            dietaryRestrictions = ""
        )
        
        val updatedChild = state.createUpdatedChild()
        
        assertNotNull(updatedChild)
        assertNull(updatedChild.medicalInfo)
        assertNull(updatedChild.dietaryRestrictions)
    }
    
    @Test
    fun `createUpdatedChild should preserve non-editable fields`() {
        val state = ChildEditUiState.fromChild(testChild).copy(
            childName = "Updated Name"
        )
        
        val updatedChild = state.createUpdatedChild()
        
        assertNotNull(updatedChild)
        assertEquals(testChild.id, updatedChild.id)
        assertEquals(testChild.parentId, updatedChild.parentId)
        assertEquals(testChild.status, updatedChild.status)
        assertEquals(testChild.currentServiceId, updatedChild.currentServiceId)
        assertEquals(testChild.checkInTime, updatedChild.checkInTime)
        assertEquals(testChild.checkOutTime, updatedChild.checkOutTime)
        assertEquals(testChild.createdAt, updatedChild.createdAt)
        // updatedAt should be different (current timestamp)
        assertNotEquals(testChild.updatedAt, updatedChild.updatedAt)
    }
}