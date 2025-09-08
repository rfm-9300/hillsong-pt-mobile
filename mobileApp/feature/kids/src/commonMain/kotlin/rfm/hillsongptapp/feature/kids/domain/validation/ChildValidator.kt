package rfm.hillsongptapp.feature.kids.domain.validation

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact

/**
 * Validator for child-related data with comprehensive validation rules
 */
class ChildValidator {
    
    companion object {
        // Validation constants
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 50
        private const val MIN_CHILD_AGE = 0
        private const val MAX_CHILD_AGE = 18
        private const val MIN_PHONE_LENGTH = 10
        private const val MAX_PHONE_LENGTH = 15
        private const val MAX_MEDICAL_INFO_LENGTH = 500
        private const val MAX_DIETARY_RESTRICTIONS_LENGTH = 500
        
        // Regex patterns
        private val PHONE_REGEX = Regex("^[+]?[1-9]?[0-9]{7,14}$")
        private val NAME_REGEX = Regex("^[a-zA-Z\\s'-]+$")
        private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    }
    
    /**
     * Validate child name
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Child name is required")
            name.length < MIN_NAME_LENGTH -> ValidationResult.Invalid("Name must be at least $MIN_NAME_LENGTH characters")
            name.length > MAX_NAME_LENGTH -> ValidationResult.Invalid("Name must not exceed $MAX_NAME_LENGTH characters")
            !NAME_REGEX.matches(name.trim()) -> ValidationResult.Invalid("Name can only contain letters, spaces, hyphens, and apostrophes")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate date of birth
     */
    fun validateDateOfBirth(dateOfBirth: String): ValidationResult {
        return when {
            dateOfBirth.isBlank() -> ValidationResult.Invalid("Date of birth is required")
            !DATE_REGEX.matches(dateOfBirth) -> ValidationResult.Invalid("Date must be in YYYY-MM-DD format")
            else -> {
                try {
                    val birthDate = LocalDate.parse(dateOfBirth)
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    val age = today.year - birthDate.year
                    
                    when {
                        birthDate > today -> ValidationResult.Invalid("Date of birth cannot be in the future")
                        age < MIN_CHILD_AGE -> ValidationResult.Invalid("Child must be at least $MIN_CHILD_AGE years old")
                        age > MAX_CHILD_AGE -> ValidationResult.Invalid("Child must be under $MAX_CHILD_AGE years old")
                        else -> ValidationResult.Valid
                    }
                } catch (e: Exception) {
                    ValidationResult.Invalid("Invalid date format. Please use YYYY-MM-DD")
                }
            }
        }
    }
    
    /**
     * Validate medical information (optional field)
     */
    fun validateMedicalInfo(medicalInfo: String?): ValidationResult {
        return when {
            medicalInfo == null || medicalInfo.isBlank() -> ValidationResult.Valid
            medicalInfo.length > MAX_MEDICAL_INFO_LENGTH -> ValidationResult.Invalid("Medical information must not exceed $MAX_MEDICAL_INFO_LENGTH characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate dietary restrictions (optional field)
     */
    fun validateDietaryRestrictions(dietaryRestrictions: String?): ValidationResult {
        return when {
            dietaryRestrictions == null || dietaryRestrictions.isBlank() -> ValidationResult.Valid
            dietaryRestrictions.length > MAX_DIETARY_RESTRICTIONS_LENGTH -> ValidationResult.Invalid("Dietary restrictions must not exceed $MAX_DIETARY_RESTRICTIONS_LENGTH characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate emergency contact name
     */
    fun validateEmergencyContactName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Emergency contact name is required")
            name.length < MIN_NAME_LENGTH -> ValidationResult.Invalid("Contact name must be at least $MIN_NAME_LENGTH characters")
            name.length > MAX_NAME_LENGTH -> ValidationResult.Invalid("Contact name must not exceed $MAX_NAME_LENGTH characters")
            !NAME_REGEX.matches(name.trim()) -> ValidationResult.Invalid("Contact name can only contain letters, spaces, hyphens, and apostrophes")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate emergency contact phone number
     */
    fun validateEmergencyContactPhone(phone: String): ValidationResult {
        val cleanPhone = phone.replace(Regex("[\\s()-]"), "")
        return when {
            phone.isBlank() -> ValidationResult.Invalid("Emergency contact phone number is required")
            cleanPhone.length < MIN_PHONE_LENGTH -> ValidationResult.Invalid("Phone number must be at least $MIN_PHONE_LENGTH digits")
            cleanPhone.length > MAX_PHONE_LENGTH -> ValidationResult.Invalid("Phone number must not exceed $MAX_PHONE_LENGTH digits")
            !PHONE_REGEX.matches(cleanPhone) -> ValidationResult.Invalid("Please enter a valid phone number")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate emergency contact relationship
     */
    fun validateEmergencyContactRelationship(relationship: String): ValidationResult {
        val validRelationships = setOf(
            "parent", "mother", "father", "guardian", "grandparent", "grandmother", "grandfather",
            "aunt", "uncle", "sibling", "sister", "brother", "family friend", "caregiver"
        )
        
        return when {
            relationship.isBlank() -> ValidationResult.Invalid("Emergency contact relationship is required")
            relationship.length > MAX_NAME_LENGTH -> ValidationResult.Invalid("Relationship must not exceed $MAX_NAME_LENGTH characters")
            !validRelationships.contains(relationship.lowercase().trim()) -> {
                ValidationResult.Invalid("Please select a valid relationship (e.g., Parent, Guardian, Grandparent)")
            }
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate complete emergency contact
     */
    fun validateEmergencyContact(emergencyContact: EmergencyContact): FormValidationResult {
        return FormValidationResult.from(
            "name" to validateEmergencyContactName(emergencyContact.name),
            "phoneNumber" to validateEmergencyContactPhone(emergencyContact.phoneNumber),
            "relationship" to validateEmergencyContactRelationship(emergencyContact.relationship)
        )
    }
    
    /**
     * Validate complete child data for registration
     */
    fun validateChildForRegistration(
        name: String,
        dateOfBirth: String,
        medicalInfo: String?,
        dietaryRestrictions: String?,
        emergencyContact: EmergencyContact
    ): FormValidationResult {
        val emergencyContactValidation = validateEmergencyContact(emergencyContact)
        
        val childValidation = FormValidationResult.from(
            "name" to validateName(name),
            "dateOfBirth" to validateDateOfBirth(dateOfBirth),
            "medicalInfo" to validateMedicalInfo(medicalInfo),
            "dietaryRestrictions" to validateDietaryRestrictions(dietaryRestrictions)
        )
        
        // Combine child and emergency contact validations
        val allResults = mutableMapOf<String, ValidationResult>()
        allResults.putAll(childValidation.fieldResults)
        allResults.putAll(emergencyContactValidation.fieldResults.mapKeys { "emergencyContact.${it.key}" })
        
        return FormValidationResult(allResults)
    }
    
    /**
     * Validate complete child data for updates
     */
    fun validateChildForUpdate(child: Child): FormValidationResult {
        return validateChildForRegistration(
            name = child.name,
            dateOfBirth = child.dateOfBirth,
            medicalInfo = child.medicalInfo,
            dietaryRestrictions = child.dietaryRestrictions,
            emergencyContact = child.emergencyContact
        )
    }
    
    /**
     * Validate child ID format
     */
    fun validateChildId(childId: String): ValidationResult {
        return when {
            childId.isBlank() -> ValidationResult.Invalid("Child ID is required")
            childId.length < 3 -> ValidationResult.Invalid("Invalid child ID format")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Get user-friendly validation summary
     */
    fun getValidationSummary(validationResult: FormValidationResult): String {
        return if (validationResult.isValid) {
            "All information is valid"
        } else {
            val errorCount = validationResult.allErrorMessages.size
            "Please fix $errorCount validation error${if (errorCount > 1) "s" else ""}"
        }
    }
}