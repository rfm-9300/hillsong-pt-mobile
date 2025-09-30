package rfm.hillsongptapp.feature.kids.ui.services

import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService

/**
 * UI state for the Services screen
 */
data class ServicesUiState(
    val services: List<KidsService> = emptyList(),
    val filteredServices: List<KidsService> = emptyList(),
    val filters: ServiceFilters = ServiceFilters(),
    val selectedChild: Child? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * Check if there are any active filters
     */
    val hasActiveFilters: Boolean
        get() = filters.hasActiveFilters()
    
    /**
     * Get services that are eligible for the selected child
     */
    val eligibleServices: List<KidsService>
        get() = selectedChild?.let { child ->
            services.filter { service ->
                service.isAgeEligible(child.calculateAge())
            }
        } ?: services
    
    /**
     * Get services that can accept check-ins
     */
    val availableServices: List<KidsService>
        get() = services.filter { it.canAcceptCheckIn() }
    
    /**
     * Get services that are at capacity
     */
    val fullServices: List<KidsService>
        get() = services.filter { it.isAtCapacity() }
    
    /**
     * Check if any operation is in progress
     */
    val isOperationInProgress: Boolean
        get() = isLoading || isRefreshing
}