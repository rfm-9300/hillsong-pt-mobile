package rfm.hillsongptapp.feature.kids.ui.checkin

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
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * ViewModel for the Check-In screen with comprehensive validation and error handling
 */
class CheckInViewModel(
    private val kidsRepository: KidsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val logger = LoggerHelper
    
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()
    
    // Current user ID from authentication
    private var currentUserId = ""
    
    init {
        loadCurrentUser()
    }
    
    /**
     * Load current user information
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getUserById(1) // Assuming user ID 1 is logged in
                currentUserId = user?.id?.toString() ?: ""
            } catch (e: Exception) {
                LoggerHelper.logError("Failed to load current user", e)
            }
        }
    }
    
    /**
     * Load child information and eligible services
     */
    fun loadChildAndEligibleServices(childId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                child = null,
                eligibleServices = emptyList()
            )
            
            try {
                LoggerHelper.logDebug("Loading child and eligible services for child: $childId")
                
                // Load child information
                val childResult = kidsRepository.getChildById(childId)
                when (childResult) {
                    is KidsResult.Success -> {
                        val child = childResult.data
                        
                        // Load available services
                        val servicesResult = kidsRepository.getServicesAcceptingCheckIns()
                        when (servicesResult) {
                            is KidsResult.Success -> {
                                val allServices = servicesResult.data
                                val eligibleServices = allServices.filter { service ->
                                    child.isEligibleForService(service) && service.canAcceptCheckIn()
                                }
                                
                                LoggerHelper.logInfo("Loaded child ${child.name} with ${eligibleServices.size} eligible services")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    child = child,
                                    eligibleServices = eligibleServices,
                                    error = null
                                )
                            }
                            is KidsResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Failed to load services: ${servicesResult.message}"
                                )
                            }
                            is KidsResult.NetworkError -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Network error loading services: ${servicesResult.message}"
                                )
                            }
                            is KidsResult.Loading -> {
                                // Should not happen in suspend function
                            }
                        }
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load child: ${childResult.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Network error loading child: ${childResult.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                LoggerHelper.logError("Unexpected error loading child and services", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select a service for check-in
     */
    fun selectService(service: KidsService) {
        LoggerHelper.logDebug("Selected service: ${service.name}")
        _uiState.value = _uiState.value.copy(selectedService = service)
    }
    
    /**
     * Show check-in confirmation dialog
     */
    fun showCheckInConfirmation() {
        val selectedService = _uiState.value.selectedService
        if (selectedService != null) {
            LoggerHelper.logDebug("Showing check-in confirmation for service: ${selectedService.name}")
            _uiState.value = _uiState.value.copy(showConfirmationDialog = true)
        } else {
            LoggerHelper.logError("Attempted to show confirmation without selected service")
        }
    }
    
    /**
     * Hide check-in confirmation dialog
     */
    fun hideCheckInConfirmation() {
        _uiState.value = _uiState.value.copy(showConfirmationDialog = false)
    }
    
    /**
     * Perform the check-in operation
     */
    fun checkInChild(notes: String? = null) {
        val child = _uiState.value.child
        val selectedService = _uiState.value.selectedService
        
        if (child == null || selectedService == null) {
            LoggerHelper.logError("Cannot check in: missing child or service")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingIn = true,
                checkInError = null
            )
            
            try {
                LoggerHelper.logInfo("Starting check-in process for ${child.name} to ${selectedService.name}")
                
                val result = kidsRepository.checkInChild(
                    childId = child.id,
                    serviceId = selectedService.id,
                    checkedInBy = currentUserId,
                    notes = notes
                )
                
                when (result) {
                    is KidsResult.Success -> {
                        LoggerHelper.logInfo("Check-in successful")
                        _uiState.value = _uiState.value.copy(
                            isCheckingIn = false,
                            showConfirmationDialog = false,
                            checkInSuccess = true
                        )
                    }
                    is KidsResult.Error -> {
                        LoggerHelper.logError("Check-in failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isCheckingIn = false,
                            checkInError = result.message
                        )
                    }
                    is KidsResult.NetworkError -> {
                        LoggerHelper.logError("Network error during check-in: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isCheckingIn = false,
                            checkInError = "Network error: ${result.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                LoggerHelper.logError("Unexpected error during check-in", e)
                _uiState.value = _uiState.value.copy(
                    isCheckingIn = false,
                    checkInError = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear general error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear check-in specific error
     */
    fun clearCheckInError() {
        _uiState.value = _uiState.value.copy(checkInError = null)
    }
    
    /**
     * Reset success state
     */
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(checkInSuccess = false)
    }
}

/**
 * UI state for the Check-In screen
 */
data class CheckInUiState(
    val child: Child? = null,
    val eligibleServices: List<KidsService> = emptyList(),
    val selectedService: KidsService? = null,
    val isLoading: Boolean = false,
    val isCheckingIn: Boolean = false,
    val error: String? = null,
    val checkInError: String? = null,
    val showConfirmationDialog: Boolean = false,
    val checkInSuccess: Boolean = false
) {
    /**
     * Check if there are any eligible services available
     */
    val hasEligibleServices: Boolean
        get() = eligibleServices.isNotEmpty()
    
    /**
     * Get recommended services (services with more available spots)
     */
    val recommendedServices: List<KidsService>
        get() = eligibleServices.filter { it.getAvailableSpots() > 5 }
    
    /**
     * Get services with limited availability
     */
    val limitedAvailabilityServices: List<KidsService>
        get() = eligibleServices.filter { it.getAvailableSpots() in 1..5 }
    
    /**
     * Check if any operation is in progress
     */
    val isOperationInProgress: Boolean
        get() = isLoading || isCheckingIn
}