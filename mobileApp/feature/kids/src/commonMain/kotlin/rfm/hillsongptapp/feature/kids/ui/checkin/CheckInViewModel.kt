package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckInChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo
import rfm.hillsongptapp.feature.kids.domain.error.ErrorHandler
import rfm.hillsongptapp.feature.kids.domain.error.ErrorRecoveryManager
import rfm.hillsongptapp.feature.kids.domain.offline.OfflineHandler
import co.touchlab.kermit.Logger
import rfm.hillsongptapp.feature.kids.domain.error.getRecoveryStrategy

/**
 * ViewModel for the Check-In screen with comprehensive validation and error handling
 */
class CheckInViewModel(
    private val checkInChildUseCase: CheckInChildUseCase,
    private val errorHandler: ErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager,
    private val offlineHandler: OfflineHandler
) : ViewModel() {
    
    private val logger = Logger.withTag("CheckInViewModel")
    
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()
    
    // TODO: Get actual user ID from authentication
    private val currentUserId = "user_123"
    
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
                logger.d { "Loading child and eligible services for child: $childId" }
                
                val result = checkInChildUseCase.getEligibleServicesForChild(childId)
                
                result.fold(
                    onSuccess = { eligibilityInfo ->
                        logger.i { "Loaded child ${eligibilityInfo.child.name} with ${eligibilityInfo.eligibleServices.size} eligible services" }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            child = eligibilityInfo.child,
                            eligibleServices = eligibilityInfo.eligibleServices,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        logger.e { "Failed to load eligible services: ${error.message}" }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
                )
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error loading child and services" }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load child information separately
     */
    private suspend fun loadChildInfo(childId: String, eligibleServices: List<EligibleServiceInfo>) {
        try {
            // We'll need to access the repository directly for this
            // In a real implementation, you might want to modify the use case to return both
            // For now, we'll create a mock child or modify the approach
            
            // Since we can't access the repository directly here, we'll need to modify our approach
            // Let's assume the use case will be enhanced to return child info as well
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                eligibleServices = eligibleServices,
                error = null
            )
            
            logger.i { "Loaded ${eligibleServices.size} eligible services" }
            
        } catch (e: Exception) {
            logger.e(e) { "Error loading child info" }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Failed to load child information: ${e.message}"
            )
        }
    }
    
    /**
     * Select a service for check-in
     */
    fun selectService(serviceInfo: EligibleServiceInfo) {
        logger.d { "Selected service: ${serviceInfo.service.name}" }
        _uiState.value = _uiState.value.copy(selectedService = serviceInfo)
    }
    
    /**
     * Show check-in confirmation dialog
     */
    fun showCheckInConfirmation() {
        val selectedService = _uiState.value.selectedService
        if (selectedService != null) {
            logger.d { "Showing check-in confirmation for service: ${selectedService.service.name}" }
            _uiState.value = _uiState.value.copy(showConfirmationDialog = true)
        } else {
            logger.w { "Attempted to show confirmation without selected service" }
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
            logger.w { "Cannot check in: missing child or service" }
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingIn = true,
                checkInError = null
            )
            
            try {
                logger.i { "Starting check-in process for ${child.name} to ${selectedService.service.name}" }
                
                val result = checkInChildUseCase.execute(
                    childId = child.id,
                    serviceId = selectedService.service.id,
                    checkedInBy = currentUserId,
                    notes = notes
                )
                
                result.fold(
                    onSuccess = { checkInRecord ->
                        logger.i { "Check-in successful: " }
                        _uiState.value = _uiState.value.copy(
                            isCheckingIn = false,
                            showConfirmationDialog = false,
                            checkInSuccess = true
                        )
                    },
                    onFailure = { error ->
                        logger.e { "Check-in failed: ${error.message}" }
                        _uiState.value = _uiState.value.copy(
                            isCheckingIn = false,
                            checkInError = getErrorMessage(error)
                        )
                    }
                )
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error during check-in" }
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
     * Convert exception to user-friendly error message using enhanced error handler
     */
    private fun getErrorMessage(error: Throwable): String {
        val errorInfo = errorHandler.handleError(error)
        return errorInfo.userMessage
    }

    
    /**
     * Check if error is retryable
     */
    private fun isErrorRetryable(error: Throwable): Boolean {
        val errorInfo = errorHandler.handleError(error)
        return errorInfo.isRetryable
    }
}

/**
 * UI state for the Check-In screen
 */
data class CheckInUiState(
    val child: Child? = null,
    val eligibleServices: List<EligibleServiceInfo> = emptyList(),
    val selectedService: EligibleServiceInfo? = null,
    val isLoading: Boolean = false,
    val isCheckingIn: Boolean = false,
    val error: String? = null,
    val checkInError: String? = null,
    val showConfirmationDialog: Boolean = false,
    val checkInSuccess: Boolean = false,
    val isRetryable: Boolean = false,
    val errorSuggestions: List<String> = emptyList(),
    val isOffline: Boolean = false,
    val offlineMessage: String? = null
) {
    /**
     * Check if there are any eligible services available
     */
    val hasEligibleServices: Boolean
        get() = eligibleServices.isNotEmpty()
    
    /**
     * Get recommended services (services with more available spots)
     */
    val recommendedServices: List<EligibleServiceInfo>
        get() = eligibleServices.filter { it.isRecommended }
    
    /**
     * Get services with limited availability
     */
    val limitedAvailabilityServices: List<EligibleServiceInfo>
        get() = eligibleServices.filter { !it.isRecommended && it.availableSpots > 0 }
}