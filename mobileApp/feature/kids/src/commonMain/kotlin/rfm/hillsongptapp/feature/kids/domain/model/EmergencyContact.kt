package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an emergency contact for a child
 */
@Serializable
data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val relationship: String
) {
    init {
        require(name.isNotBlank()) { "Emergency contact name cannot be blank" }
        require(phoneNumber.isNotBlank()) { "Emergency contact phone number cannot be blank" }
        require(relationship.isNotBlank()) { "Emergency contact relationship cannot be blank" }
    }
    
    /**
     * Validate phone number format (basic validation)
     */
    fun isValidPhoneNumber(): Boolean {
        // Basic phone number validation - contains only digits, spaces, hyphens, parentheses, and plus
        val phoneRegex = Regex("^[+]?[0-9\\s\\-()]+$")
        return phoneNumber.matches(phoneRegex) && phoneNumber.replace(Regex("[^0-9]"), "").length >= 10
    }
}