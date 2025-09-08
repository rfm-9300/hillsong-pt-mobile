package rfm.hillsongptapp.feature.kids.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
// Using simple timestamp for now - in real implementation would use kotlinx-datetime

/**
 * ViewModel for Child Registration screen
 * Manages form state, validation, and child registration operations
 */
class ChildRegistrationViewModel(
    private val kidsRepository: KidsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChildRegistrationUiState())
    val uiState: StateFlow<ChildRegistrationUiState> = _uiState.asStateFlow()
    
    // TODO: Get actual parent ID from user session/authentication
    private val currentParentId = "parent_123" // This would come from authentication
    
    /**
     * Update child name field
     */
    fun updateChildName(name: String) {
        _uiState.value = _uiState.value.copy(
            childName = name,
            nameError = validateChildName(name)
        )
    }
    
    /**
     * Update date of birth field
     */
    fun updateDateOfBirth(date: String) {
        _uiState.value = _uiState.value.copy(
            dateOfBirth = date,
            dateOfBirthError = validateDateOfBirth(date)
        )
    }
    
    /**
     * Update medical information field
     */
    fun updateMedicalInfo(info: String) {
        _uiState.value = _uiState.value.copy(medicalInfo = info)
    }
    
    /**
     * Update dietary restrictions field
     */
    fun updateDietaryRestrictions(restrictions: String) {
        _uiState.value = _uiState.value.copy(dietaryRestrictions = restrictions)
    }
    
    /**
     * Update emergency contact name field
     */
    fun updateEmergencyContactName(name: String) {
        _uiState.value = _uiState.value.copy(
            emergencyContactName = name,
            emergencyContactNameError = validateEmergencyContactName(name)
        )
    }
    
    /**
     * Update emergency contact phone field
     */
    fun updateEmergencyContactPhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            emergencyContactPhone = phone,
            emergencyContactPhoneError = validateEmergencyContactPhone(phone)
        )
    }
    
    /**
     * Update emergency contact relationship field
     */
    fun updateEmergencyContactRelationship(relationship: String) {
        _uiState.value = _uiState.value.copy(
            emergencyContactRelationship = relationship,
            emergencyContactRelationshipError = validateEmergencyContactRelationship(relationship)
        )
    }
    
    /**
     * Show date picker dialog
     */
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }
    
    /**
     * Hide date picker dialog
     */
    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Reset form to initial state
     */
    fun resetForm() {
        _uiState.value = ChildRegistrationUiState()
    }
    
    /**
     * Register child with current form data
     */
    fun registerChild() {
        val currentState = _uiState.value
        
        // Validate all fields before submission
        val nameError = validateChildName(currentState.childName)
        val dateError = validateDateOfBirth(currentState.dateOfBirth)
        val contactNameError = validateEmergencyContactName(currentState.emergencyContactName)
        val contactPhoneError = validateEmergencyContactPhone(currentState.emergencyContactPhone)
        val contactRelationshipError = validateEmergencyContactRelationship(currentState.emergencyContactRelationship)
        
        // Update state with validation errors
        _uiState.value = currentState.copy(
            nameError = nameError,
            dateOfBirthError = dateError,
            emergencyContactNameError = contactNameError,
            emergencyContactPhoneError = contactPhoneError,
            emergencyContactRelationshipError = contactRelationshipError
        )
        
        // Check if form is valid
        if (nameError != null || dateError != null || contactNameError != null || 
            contactPhoneError != null || contactRelationshipError != null) {
            return
        }
        
        // Create emergency contact
        val emergencyContact = currentState.createEmergencyContact()
        if (emergencyContact == null) {
            _uiState.value = currentState.copy(
                error = "Failed to create emergency contact information"
            )
            return
        }
        
        // Create child object
        val currentTime = System.currentTimeMillis().toString() // Simplified timestamp
        val child = Child(
            id = "", // Will be assigned by repository
            parentId = currentParentId,
            name = currentState.childName.trim(),
            dateOfBirth = currentState.dateOfBirth,
            medicalInfo = currentState.medicalInfo.takeIf { it.isNotBlank() },
            dietaryRestrictions = currentState.dietaryRestrictions.takeIf { it.isNotBlank() },
            emergencyContact = emergencyContact,
            status = CheckInStatus.NOT_IN_SERVICE,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        // Register child
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = kidsRepository.registerChild(child)
                
                result.fold(
                    onSuccess = { registeredChild ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Registration failed: ${error.message}"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Registration failed: ${e.message}"
                )
            }
        }
    }
    
    // Validation functions
    
    /**
     * Validate child name field
     */
    private fun validateChildName(name: String): String? {
        return when {
            name.isBlank() -> "Child name is required"
            name.length < 2 -> "Child name must be at least 2 characters"
            name.length > 50 -> "Child name must be less than 50 characters"
            !name.matches(Regex("^[a-zA-Z\\s'-]+$")) -> "Child name can only contain letters, spaces, hyphens, and apostrophes"
            else -> null
        }
    }
    
    /**
     * Validate date of birth field
     */
    private fun validateDateOfBirth(date: String): String? {
        return when {
            date.isBlank() -> "Date of birth is required"
            !isValidDateFormat(date) -> "Please enter date in YYYY-MM-DD format"
            !isValidDate(date) -> "Please enter a valid date"
            !isReasonableAge(date) -> "Child must be between 0 and 18 years old"
            else -> null
        }
    }
    
    /**
     * Validate emergency contact name field
     */
    private fun validateEmergencyContactName(name: String): String? {
        return when {
            name.isBlank() -> "Emergency contact name is required"
            name.length < 2 -> "Emergency contact name must be at least 2 characters"
            name.length > 50 -> "Emergency contact name must be less than 50 characters"
            !name.matches(Regex("^[a-zA-Z\\s'-]+$")) -> "Emergency contact name can only contain letters, spaces, hyphens, and apostrophes"
            else -> null
        }
    }
    
    /**
     * Validate emergency contact phone field
     */
    private fun validateEmergencyContactPhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Emergency contact phone is required"
            !isValidPhoneNumber(phone) -> "Please enter a valid phone number"
            else -> null
        }
    }
    
    /**
     * Validate emergency contact relationship field
     */
    private fun validateEmergencyContactRelationship(relationship: String): String? {
        return when {
            relationship.isBlank() -> "Emergency contact relationship is required"
            relationship.length < 2 -> "Relationship must be at least 2 characters"
            relationship.length > 30 -> "Relationship must be less than 30 characters"
            else -> null
        }
    }
    
    // Helper validation functions
    
    /**
     * Check if date string is in valid YYYY-MM-DD format
     */
    private fun isValidDateFormat(date: String): Boolean {
        val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        return date.matches(dateRegex)
    }
    
    /**
     * Check if date string represents a valid date
     */
    private fun isValidDate(date: String): Boolean {
        return try {
            val parts = date.split("-")
            if (parts.size != 3) return false
            
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            
            // Basic date validation
            when {
                year < 1900 || year > 2025 -> false
                month < 1 || month > 12 -> false
                day < 1 || day > 31 -> false
                // February validation
                month == 2 && day > 29 -> false
                month == 2 && day == 29 && !isLeapYear(year) -> false
                // April, June, September, November have 30 days
                (month == 4 || month == 6 || month == 9 || month == 11) && day > 30 -> false
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if year is a leap year
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
    
    /**
     * Check if age calculated from date is reasonable for kids service
     */
    private fun isReasonableAge(date: String): Boolean {
        return try {
            val currentYear = 2025 // This would be dynamic in real implementation
            val birthYear = date.substring(0, 4).toInt()
            val age = currentYear - birthYear
            age in 0..18
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate phone number format
     */
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation - contains only digits, spaces, hyphens, parentheses, and plus
        val phoneRegex = Regex("^[+]?[0-9\\s\\-()]+$")
        val digitsOnly = phone.replace(Regex("[^0-9]"), "")
        return phone.matches(phoneRegex) && digitsOnly.length >= 10
    }
}