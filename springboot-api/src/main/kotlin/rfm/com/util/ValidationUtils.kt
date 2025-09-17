package rfm.com.util

import rfm.com.exception.InvalidDataException
import rfm.com.exception.BusinessRuleViolationException

/**
 * Utility class for common validation operations
 */
object ValidationUtils {
    
    /**
     * Validate that a value is not null
     */
    fun <T> requireNotNull(value: T?, fieldName: String, message: String? = null): T {
        return value ?: throw InvalidDataException(
            field = fieldName,
            value = null,
            reason = message ?: "Value cannot be null"
        )
    }
    
    /**
     * Validate that a string is not blank
     */
    fun requireNotBlank(value: String?, fieldName: String, message: String? = null): String {
        if (value.isNullOrBlank()) {
            throw InvalidDataException(
                field = fieldName,
                value = value,
                reason = message ?: "Value cannot be blank"
            )
        }
        return value
    }
    
    /**
     * Validate that a number is positive
     */
    fun requirePositive(value: Number, fieldName: String, message: String? = null): Number {
        if (value.toDouble() <= 0) {
            throw InvalidDataException(
                field = fieldName,
                value = value,
                reason = message ?: "Value must be positive"
            )
        }
        return value
    }
    
    /**
     * Validate that a number is non-negative
     */
    fun requireNonNegative(value: Number, fieldName: String, message: String? = null): Number {
        if (value.toDouble() < 0) {
            throw InvalidDataException(
                field = fieldName,
                value = value,
                reason = message ?: "Value cannot be negative"
            )
        }
        return value
    }
    
    /**
     * Validate that a string matches a pattern
     */
    fun requirePattern(value: String, pattern: Regex, fieldName: String, message: String? = null): String {
        if (!pattern.matches(value)) {
            throw InvalidDataException(
                field = fieldName,
                value = value,
                reason = message ?: "Value does not match required pattern"
            )
        }
        return value
    }
    
    /**
     * Validate that a string length is within bounds
     */
    fun requireLength(
        value: String,
        minLength: Int = 0,
        maxLength: Int = Int.MAX_VALUE,
        fieldName: String,
        message: String? = null
    ): String {
        if (value.length < minLength || value.length > maxLength) {
            throw InvalidDataException(
                field = fieldName,
                value = value,
                reason = message ?: "Length must be between $minLength and $maxLength characters"
            )
        }
        return value
    }
    
    /**
     * Validate that a collection is not empty
     */
    fun <T> requireNotEmpty(collection: Collection<T>, fieldName: String, message: String? = null): Collection<T> {
        if (collection.isEmpty()) {
            throw InvalidDataException(
                field = fieldName,
                value = collection,
                reason = message ?: "Collection cannot be empty"
            )
        }
        return collection
    }
    
    /**
     * Validate that a collection size is within bounds
     */
    fun <T> requireSize(
        collection: Collection<T>,
        minSize: Int = 0,
        maxSize: Int = Int.MAX_VALUE,
        fieldName: String,
        message: String? = null
    ): Collection<T> {
        if (collection.size < minSize || collection.size > maxSize) {
            throw InvalidDataException(
                field = fieldName,
                value = collection,
                reason = message ?: "Size must be between $minSize and $maxSize"
            )
        }
        return collection
    }
    
    /**
     * Validate a business rule condition
     */
    fun requireBusinessRule(condition: Boolean, rule: String, details: String? = null) {
        if (!condition) {
            throw BusinessRuleViolationException(rule, details)
        }
    }
    
    /**
     * Validate email format
     */
    fun requireValidEmail(email: String, fieldName: String = "email"): String {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return requirePattern(email, emailPattern, fieldName, "Invalid email format")
    }
    
    /**
     * Validate phone number format (basic validation)
     */
    fun requireValidPhone(phone: String, fieldName: String = "phone"): String {
        val phonePattern = Regex("^[+]?[0-9\\s\\-()]{7,18}$")
        return requirePattern(phone, phonePattern, fieldName, "Invalid phone number format")
    }
}