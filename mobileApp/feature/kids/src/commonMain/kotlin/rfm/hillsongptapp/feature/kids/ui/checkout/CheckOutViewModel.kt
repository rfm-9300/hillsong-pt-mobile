package rfm.hillsongptapp.feature.kids.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutChildUseCase
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutEligibilityInfo
import rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutResult
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import co.touchlab.kermit.Logger

/**
 * ViewModel for the Check-Out screen
 * Manages the check-out process including parent verification and confirmation
 */
class CheckOutViewModel(
    private val kidsRepository: KidsRepository,
    private val checkOutChildUseCase: CheckOutChildUseCase
) : ViewModel() {
    
    private val logger = Logger.withTag("CheckOutViewModel")
    
    private val _uiState = MutableStateFlow(CheckOutUiState())
    val uiState: StateFlow<CheckOutUiState> = _uiState.asStateFlow()
    
    // TODO: Get actual parent ID from user session/authentication
    private val currentParentId = "parent_123"
    
    /**
     * Load child information and check-out eligibility
     */
    fun loadChild(childId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                childId = childId
            )
            
            try {
                // Load child information
                val childResult = kidsRepository.getChildById(childId)
                if (childResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Child not found: ${childResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                val child = childResult.getOrThrow()
                logger.d { "Loaded child: ${child.name}, status: ${child.status}" }
                
                // Load current service if child is checked in
                val currentService = if (child.currentServiceId != null) {
                    val serviceResult = kidsRepository.getServiceById(child.currentServiceId)
                    serviceResult.getOrNull()
                } else {
                    null
                }
                
                // Get check-out eligibility information
                val eligibilityResult = checkOutChildUseCase.getCheckOutEligibilityInfo(childId)
                val eligibilityInfo = eligibilityResult.getOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    child = child,
                    currentService = currentService,
                    eligibilityInfo = eligibilityInfo,
                    error = null
                )
                
            } catch (e: Exception) {
                logger.e(e) { "Error loading child information" }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load child information: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Start the check-out process with parent verification
     */
    fun startCheckOutProcess() {
        logger.d { "Starting check-out process" }
        _uiState.value = _uiState.value.copy(
            showParentVerification = true
        )
    }
    
    /**
     * Handle parent verification completion
     */
    fun onParentVerified() {
        logger.d { "Parent verification completed" }
        _uiState.value = _uiState.value.copy(
            showParentVerification = false,
            showCheckOutConfirmation = true
        )
    }
    
    /**
     * Hide parent verification dialog
     */
    fun hideParentVerification() {
        _uiState.value = _uiState.value.copy(
            showParentVerification = false
        )
    }
    
    /**
     * Hide check-out confirmation dialog
     */
    fun hideCheckOutConfirmation() {
        _uiState.value = _uiState.value.copy(
            showCheckOutConfirmation = false
        )
    }
    
    /**
     * Confirm and execute the check-out
     */
    fun confirmCheckOut() {
        val childId = _uiState.value.childId
        if (childId == null) {
            logger.e { "Cannot check out: child ID is null" }
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showCheckOutConfirmation = false,
                isCheckingOut = true
            )
            
            try {
                logger.d { "Executing check-out for child $childId" }
                val result = checkOutChildUseCase.execute(
                    childId = childId,
                    checkedOutBy = currentParentId,
                    notes = null // Could be extended to allow notes
                )
                
                result.fold(
                    onSuccess = { checkOutResult ->
                        logger.i { "Check-out successful for child " }
                        _uiState.value = _uiState.value.copy(
                            isCheckingOut = false,
                            checkOutResult = checkOutResult as CheckOutResult?,
                            showSuccessDialog = true
                        )
                    },
                    onFailure = { error ->
                        logger.e { "Check-out failed: ${error.message}" }
                        _uiState.value = _uiState.value.copy(
                            isCheckingOut = false,
                            checkOutError = error.message ?: "Check-out failed",
                            showErrorDialog = true
                        )
                    }
                )
                
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error during check-out" }
                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    checkOutError = "Unexpected error: ${e.message}",
                    showErrorDialog = true
                )
            }
        }
    }
    
    /**
     * Hide success dialog
     */
    fun hideSuccessDialog() {
        _uiState.value = _uiState.value.copy(
            showSuccessDialog = false,
            checkOutResult = null
        )
    }
    
    /**
     * Hide error dialog
     */
    fun hideErrorDialog() {
        _uiState.value = _uiState.value.copy(
            showErrorDialog = false,
            checkOutError = null
        )
    }
    
    /**
     * Retry the check-out operation
     */
    fun retryCheckOut() {
        hideErrorDialog()
        confirmCheckOut()
    }
    
    /**
     * Clear general error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the Check-Out screen
 */
data class CheckOutUiState(
    val childId: String? = null,
    val child: Child? = null,
    val currentService: KidsService? = null,
    val eligibilityInfo: CheckOutEligibilityInfo? = null,
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val error: String? = null,
    val showParentVerification: Boolean = false,
    val showCheckOutConfirmation: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val checkOutResult: CheckOutResult? = null,
    val checkOutError: String? = null
) {
    /**
     * Check if any operation is in progress
     */
    val isOperationInProgress: Boolean
        get() = isLoading || isCheckingOut
    
    /**
     * Check if the child can be checked out
     */
    val canCheckOut: Boolean
        get() = eligibilityInfo?.canCheckOut == true
}