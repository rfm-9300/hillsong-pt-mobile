package rfm.hillsongptapp.feature.kids.domain.validation

/**
 * Represents the result of a validation operation
 */
sealed class ValidationResult {
    /**
     * Validation passed successfully
     */
    data object Valid : ValidationResult()
    
    /**
     * Validation failed with specific error message
     */
    data class Invalid(val message: String) : ValidationResult()
    
    /**
     * Check if validation result is valid
     */
    val isValid: Boolean
        get() = this is Valid
    
    /**
     * Check if validation result is invalid
     */
    val isInvalid: Boolean
        get() = this is Invalid
    
    /**
     * Get error message if validation failed, null otherwise
     */
    val errorMessage: String?
        get() = (this as? Invalid)?.message
}

/**
 * Represents validation results for multiple fields
 */
data class FormValidationResult(
    val fieldResults: Map<String, ValidationResult>
) {
    /**
     * Check if all fields are valid
     */
    val isValid: Boolean
        get() = fieldResults.values.all { it.isValid }
    
    /**
     * Check if any field has validation errors
     */
    val hasErrors: Boolean
        get() = fieldResults.values.any { it.isInvalid }
    
    /**
     * Get all error messages mapped by field name
     */
    val errorMessages: Map<String, String>
        get() = fieldResults.mapNotNull { (field, result) ->
            result.errorMessage?.let { field to it }
        }.toMap()
    
    /**
     * Get error message for specific field
     */
    fun getErrorForField(fieldName: String): String? {
        return fieldResults[fieldName]?.errorMessage
    }
    
    /**
     * Get all error messages as a list
     */
    val allErrorMessages: List<String>
        get() = fieldResults.values.mapNotNull { it.errorMessage }
    
    companion object {
        /**
         * Create a valid form validation result
         */
        fun valid(fields: List<String>): FormValidationResult {
            return FormValidationResult(
                fieldResults = fields.associateWith { ValidationResult.Valid }
            )
        }
        
        /**
         * Create form validation result from individual field results
         */
        fun from(vararg fieldResults: Pair<String, ValidationResult>): FormValidationResult {
            return FormValidationResult(fieldResults.toMap())
        }
    }
}