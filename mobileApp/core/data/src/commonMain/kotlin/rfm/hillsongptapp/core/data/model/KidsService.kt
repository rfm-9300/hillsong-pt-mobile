package rfm.hillsongptapp.core.data.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a kids service or program
 */
@Serializable
data class KidsService(
    val id: String,
    val name: String,
    val description: String,
    val minAge: Int,
    val maxAge: Int,
    val startTime: String, // ISO 8601 format
    val endTime: String, // ISO 8601 format
    val location: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val isAcceptingCheckIns: Boolean,
    val staffMembers: List<String>,
    val createdAt: String // ISO 8601 format
) {
    init {
        require(name.isNotBlank()) { "Service name cannot be blank" }
        require(minAge >= 0) { "Minimum age cannot be negative" }
        require(maxAge >= minAge) { "Maximum age must be greater than or equal to minimum age" }
        require(maxCapacity > 0) { "Maximum capacity must be positive" }
        require(currentCapacity >= 0) { "Current capacity cannot be negative" }
        require(currentCapacity <= maxCapacity) { "Current capacity cannot exceed maximum capacity" }
        require(location.isNotBlank()) { "Service location cannot be blank" }
    }
    
    /**
     * Check if the service has available spots for check-in
     */
    fun hasAvailableSpots(): Boolean {
        return currentCapacity < maxCapacity
    }
    
    /**
     * Check if the service is at full capacity
     */
    fun isAtCapacity(): Boolean {
        return currentCapacity >= maxCapacity
    }
    
    /**
     * Get the number of available spots remaining
     */
    fun getAvailableSpots(): Int {
        return maxCapacity - currentCapacity
    }
    
    /**
     * Check if a child with the given age is eligible for this service
     */
    fun isAgeEligible(age: Int): Boolean {
        return age >= minAge && age <= maxAge
    }
    
    /**
     * Check if the service can accept new check-ins
     */
    fun canAcceptCheckIn(): Boolean {
        return isAcceptingCheckIns && hasAvailableSpots()
    }
    
    /**
     * Get the age range as a formatted string
     */
    fun getAgeRangeDisplay(): String {
        return if (minAge == maxAge) {
            "$minAge years"
        } else {
            "$minAge-$maxAge years"
        }
    }
    
    /**
     * Get capacity information as a formatted string
     */
    fun getCapacityDisplay(): String {
        return "$currentCapacity/$maxCapacity"
    }
}