package rfm.hillsongptapp.core.data.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a check-in/check-out record for a child
 */
@Serializable
data class CheckInRecord(
    val id: String,
    val childId: String,
    val serviceId: String,
    val checkInTime: String, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format, null if still checked in
    val checkedInBy: String, // User ID of the person who checked in the child
    val checkedOutBy: String? = null, // User ID of the person who checked out the child
    val notes: String? = null,
    val status: CheckInStatus
) {
    init {
        require(id.isNotBlank()) { "Check-in record ID cannot be blank" }
        require(childId.isNotBlank()) { "Child ID cannot be blank" }
        require(serviceId.isNotBlank()) { "Service ID cannot be blank" }
        require(checkInTime.isNotBlank()) { "Check-in time cannot be blank" }
        require(checkedInBy.isNotBlank()) { "Checked in by user ID cannot be blank" }
        
        // Validation based on status
        when (status) {
            CheckInStatus.CHECKED_IN -> {
                require(checkOutTime == null) { "Check-out time must be null when status is CHECKED_IN" }
                require(checkedOutBy == null) { "Checked out by must be null when status is CHECKED_IN" }
            }
            CheckInStatus.CHECKED_OUT -> {
                require(checkOutTime != null) { "Check-out time cannot be null when status is CHECKED_OUT" }
                require(checkedOutBy != null) { "Checked out by cannot be null when status is CHECKED_OUT" }
            }
            CheckInStatus.NOT_IN_SERVICE -> {
                // This status shouldn't typically be used for check-in records
                // but we'll allow it for flexibility
            }
        }
    }
    
    /**
     * Check if the child is currently checked in (no check-out time)
     */
    fun isCurrentlyCheckedIn(): Boolean {
        return checkOutTime == null && status == CheckInStatus.CHECKED_IN
    }
    
    /**
     * Check if the check-in session is complete (has both check-in and check-out times)
     */
    fun isSessionComplete(): Boolean {
        return checkOutTime != null && status == CheckInStatus.CHECKED_OUT
    }
    
    /**
     * Calculate the duration of the check-in session in minutes
     * Returns null if the session is not complete
     */
    fun getSessionDurationMinutes(): Long? {
        if (checkOutTime == null) return null
        
        // This is a simplified duration calculation
        // In a real implementation, you'd use proper date/time libraries
        return try {
            val checkInMillis = parseIsoDateTime(checkInTime)
            val checkOutMillis = parseIsoDateTime(checkOutTime)
            (checkOutMillis - checkInMillis) / (1000 * 60) // Convert to minutes
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get a formatted duration string
     */
    fun getFormattedDuration(): String? {
        val minutes = getSessionDurationMinutes() ?: return null
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            else -> "${minutes}m"
        }
    }
    
    // Simplified ISO date parsing - in real implementation use proper date/time library
    private fun parseIsoDateTime(isoString: String): Long {
        // This is a placeholder implementation
        // In real code, use kotlinx-datetime or similar
        return 0L // Placeholder value for compilation
    }
}