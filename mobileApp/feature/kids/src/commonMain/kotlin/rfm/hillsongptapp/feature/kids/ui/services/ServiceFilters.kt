package rfm.hillsongptapp.feature.kids.ui.services

/**
 * Data class representing filter criteria for services
 */
data class ServiceFilters(
    val availability: Availability = Availability.ALL,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val showFullServices: Boolean = true
) {
    /**
     * Enum representing availability filter options
     */
    enum class Availability(val displayName: String) {
        ALL("All Services"),
        AVAILABLE_ONLY("Available Only"),
        ACCEPTING_CHECKINS("Accepting Check-ins")
    }
    
    /**
     * Check if any filters are active (non-default)
     */
    fun hasActiveFilters(): Boolean {
        return availability != Availability.ALL ||
                minAge != null ||
                maxAge != null ||
                !showFullServices
    }
    
    /**
     * Check if a service matches the current filter criteria
     */
    fun matches(service: rfm.hillsongptapp.feature.kids.domain.model.KidsService): Boolean {
        // Check availability filter
        when (availability) {
            Availability.AVAILABLE_ONLY -> {
                if (!service.hasAvailableSpots()) return false
            }
            Availability.ACCEPTING_CHECKINS -> {
                if (!service.canAcceptCheckIn()) return false
            }
            Availability.ALL -> {
                // No availability filtering
            }
        }
        
        // Check age range filter
        if (minAge != null && service.maxAge < minAge) return false
        if (maxAge != null && service.minAge > maxAge) return false
        
        // Check capacity filter
        if (!showFullServices && service.isAtCapacity()) return false
        
        return true
    }
}