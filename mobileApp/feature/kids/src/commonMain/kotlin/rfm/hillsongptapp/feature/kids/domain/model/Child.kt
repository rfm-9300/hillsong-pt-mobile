package rfm.hillsongptapp.feature.kids.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a child in the kids management system
 */
@Serializable
data class Child(
    val id: String,
    val parentId: String,
    val name: String,
    val dateOfBirth: String, // ISO 8601 format (YYYY-MM-DD)
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContact: EmergencyContact,
    val status: CheckInStatus,
    val currentServiceId: String? = null,
    val checkInTime: String? = null, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format
    val createdAt: String, // ISO 8601 format
    val updatedAt: String // ISO 8601 format
) {
    /**
     * Calculate the child's age in years based on their date of birth
     */
    fun calculateAge(): Int {
        // This is a simplified age calculation
        // In a real implementation, you'd use proper date/time libraries
        val currentYear = 2025 // This would be dynamic in real implementation
        val birthYear = dateOfBirth.substring(0, 4).toInt()
        return currentYear - birthYear
    }
    
    /**
     * Check if the child is currently checked into a service
     */
    fun isCheckedIn(): Boolean = status == CheckInStatus.CHECKED_IN
    
    /**
     * Check if the child is eligible for a service based on age requirements
     */
    fun isEligibleForService(service: KidsService): Boolean {
        val age = calculateAge()
        return age >= service.minAge && age <= service.maxAge
    }
}