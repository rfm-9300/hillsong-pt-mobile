package rfm.hillsongptapp.feature.kids.ui.edit

import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.EmergencyContact
import kotlinx.datetime.Clock

/**
 * UI state for the Child Edit screen
 */
data class ChildEditUiState(
    // Original child data
    val originalChild: Child? = null,
    
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
    val isSaving: Boolean = false,
    val isUpdateSuccessful: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val showDiscardChangesDialog: Boolean = false,
    val showSuccessDialog: Boolean = false
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
     * Check if any changes have been made to the original data
     */
    val hasChanges: Boolean
        get() = originalChild?.let { original ->
            childName.trim() != original.name ||
            dateOfBirth != original.dateOfBirth ||
            medicalInfo.trim() != (original.medicalInfo ?: "") ||
            dietaryRestrictions.trim() != (original.dietaryRestrictions ?: "") ||
            emergencyContactName.trim() != original.emergencyContact.name ||
            emergencyContactPhone.trim() != original.emergencyContact.phoneNumber ||
            emergencyContactRelationship.trim() != original.emergencyContact.relationship
        } ?: false
    
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
    
    /**
     * Create updated Child object from form data
     */
    fun createUpdatedChild(): Child? {
        val original = originalChild ?: return null
        val emergencyContact = createEmergencyContact() ?: return null
        
        return original.copy(
            name = childName.trim(),
            dateOfBirth = dateOfBirth,
            medicalInfo = medicalInfo.takeIf { it.isNotBlank() },
            dietaryRestrictions = dietaryRestrictions.takeIf { it.isNotBlank() },
            emergencyContact = emergencyContact,
            updatedAt = Clock.System.now().toEpochMilliseconds().toString() // Simplified timestamp
        )
    }
    
    companion object {
        /**
         * Create initial state from existing child data
         */
        fun fromChild(child: Child): ChildEditUiState {
            return ChildEditUiState(
                originalChild = child,
                childName = child.name,
                dateOfBirth = child.dateOfBirth,
                medicalInfo = child.medicalInfo ?: "",
                dietaryRestrictions = child.dietaryRestrictions ?: "",
                emergencyContactName = child.emergencyContact.name,
                emergencyContactPhone = child.emergencyContact.phoneNumber,
                emergencyContactRelationship = child.emergencyContact.relationship
            )
        }
    }
}