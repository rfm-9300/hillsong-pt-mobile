package rfm.hillsongptapp.feature.kids.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult

/**
 * ViewModel for the Services screen
 * Manages state for services loading, filtering, and display
 */
class ServicesViewModel(
    private val kidsRepository: KidsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()
    
    init {
        loadServices()
    }
    
    /**
     * Load all available services
     */
    fun loadServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = kidsRepository.getAvailableServices()
                
                when (result) {
                    is KidsResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            services = result.data,
                            filteredServices = applyFilters(result.data, _uiState.value.filters),
                            isLoading = false,
                            error = null
                        )
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load services: ${result.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Network error loading services: ${result.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load services: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Refresh services with pull-to-refresh
     */
    fun refreshServices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                val result = kidsRepository.getAvailableServices()
                
                when (result) {
                    is KidsResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            services = result.data,
                            filteredServices = applyFilters(result.data, _uiState.value.filters),
                            isRefreshing = false,
                            error = null
                        )
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = "Failed to refresh services: ${result.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = "Network error refreshing services: ${result.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Failed to refresh services: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Set the selected child for age-based filtering
     */
    fun setSelectedChild(child: Child?) {
        _uiState.value = _uiState.value.copy(
            selectedChild = child,
            filteredServices = applyFilters(_uiState.value.services, _uiState.value.filters, child)
        )
    }
    
    /**
     * Update filter criteria and apply to services
     */
    fun updateFilters(filters: ServiceFilters) {
        _uiState.value = _uiState.value.copy(
            filters = filters,
            filteredServices = applyFilters(_uiState.value.services, filters, _uiState.value.selectedChild)
        )
    }
    
    /**
     * Clear all filters
     */
    fun clearFilters() {
        val defaultFilters = ServiceFilters()
        _uiState.value = _uiState.value.copy(
            filters = defaultFilters,
            filteredServices = applyFilters(_uiState.value.services, defaultFilters, _uiState.value.selectedChild)
        )
    }
    
    /**
     * Get services appropriate for a specific child
     */
    fun getServicesForChild(child: Child): List<KidsService> {
        return _uiState.value.services.filter { service ->
            service.isAgeEligible(child.calculateAge()) && service.canAcceptCheckIn()
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Apply filters to the list of services
     */
    private fun applyFilters(
        services: List<KidsService>,
        filters: ServiceFilters,
        selectedChild: Child? = null
    ): List<KidsService> {
        var filteredServices = services
        
        // Apply general filters
        filteredServices = filteredServices.filter { service ->
            filters.matches(service)
        }
        
        // Apply child-specific filtering if a child is selected
        selectedChild?.let { child ->
            val childAge = child.calculateAge()
            
            // Prioritize services that the child is eligible for
            filteredServices = filteredServices.sortedWith(
                compareByDescending<KidsService> { service ->
                    service.isAgeEligible(childAge)
                }.thenByDescending { service ->
                    service.canAcceptCheckIn()
                }.thenBy { service ->
                    service.name
                }
            )
        }
        
        return filteredServices
    }
}