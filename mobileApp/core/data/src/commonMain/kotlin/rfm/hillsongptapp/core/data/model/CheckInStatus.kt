package rfm.hillsongptapp.core.data.model

import kotlinx.serialization.Serializable

/**
 * Enum representing the check-in status of a child
 */
@Serializable
enum class CheckInStatus {
    /**
     * Child is currently checked out and not in any service
     */
    CHECKED_OUT,
    
    /**
     * Child is currently checked into a service
     */
    CHECKED_IN,
    
    /**
     * Child is not currently in any service (default state)
     */
    NOT_IN_SERVICE;
    
    /**
     * Check if the status indicates the child is available for check-in
     */
    fun isAvailableForCheckIn(): Boolean {
        return this == CHECKED_OUT || this == NOT_IN_SERVICE
    }
    
    /**
     * Check if the status indicates the child can be checked out
     */
    fun canBeCheckedOut(): Boolean {
        return this == CHECKED_IN
    }
    
    /**
     * Get a human-readable display name for the status
     */
    fun getDisplayName(): String {
        return when (this) {
            CHECKED_OUT -> "Checked Out"
            CHECKED_IN -> "Checked In"
            NOT_IN_SERVICE -> "Not in Service"
        }
    }
}