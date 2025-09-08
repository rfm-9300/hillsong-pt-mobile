package rfm.hillsongptapp.feature.kids.ui.registration

import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact

/**
 * UI state for the Child Registration screen
 */
data class ChildRegistrationUiState(
    // Form fields
    val childName: String = "",
    val dateOfBirth: String = "",
    val medicalInfo: String = "",
    val dietaryRestrictions: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactRelationship: String = "",
    
    // Form validation
    val nameError: String? = null,
    val dateOfBirthError: String? = null,
    val emergencyContactNameError: String? = null,
    val emergencyContactPhoneError: String? = null,
    val emergencyContactRelationshipError: String? = null,
    
    // UI state
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false
) {
    /**
     * Check if all required fields are filled
     */
    val areRequiredFieldsFilled: Boolean
        get() = childName.isNotBlank() &&
                dateOfBirth.isNotBlank() &&
                emergencyContactName.isNotBlank() &&
                emergencyContactPhone.isNotBlank() &&
                emergencyContactRelationship.isNotBlank()
    
    /**
     * Check if form has any validation errors
     */
    val hasValidationErrors: Boolean
        get() = nameError != null ||
                dateOfBirthError != null ||
                emergencyContactNameError != null ||
                emergencyContactPhoneError != null ||
                emergencyContactRelationshipError != null
    
    /**
     * Check if form is valid and ready for submission
     */
    val isFormValid: Boolean
        get() = areRequiredFieldsFilled && !hasValidationErrors
    
    /**
     * Get calculated age from date of birth
     */
    fun getCalculatedAge(): Int? {
        return if (dateOfBirth.isNotBlank() && dateOfBirthError == null) {
            try {
                val currentYear = 2025 // This would be dynamic in real implementation
                val birthYear = dateOfBirth.substring(0, 4).toInt()
                currentYear - birthYear
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Create EmergencyContact from form data
     */
    fun createEmergencyContact(): EmergencyContact? {
        return if (emergencyContactName.isNotBlank() && 
                   emergencyContactPhone.isNotBlank() && 
                   emergencyContactRelationship.isNotBlank()) {
            EmergencyContact(
                name = emergencyContactName.trim(),
                phoneNumber = emergencyContactPhone.trim(),
                relationship = emergencyContactRelationship.trim()
            )
        } else {
            null
        }
    }
}