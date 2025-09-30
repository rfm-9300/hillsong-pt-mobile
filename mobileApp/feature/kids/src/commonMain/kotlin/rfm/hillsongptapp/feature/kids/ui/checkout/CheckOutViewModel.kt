package rfm.hillsongptapp.feature.kids.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * ViewModel for the Check-Out screen
 * Manages the check-out process including parent verification and confirmation
 */
class CheckOutViewModel(
    private val kidsRepository: KidsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val logger = LoggerHelper
    
    private val _uiState = MutableStateFlow(CheckOutUiState())
    val uiState: StateFlow<CheckOutUiState> = _uiState.asStateFlow()
    
    // Current parent ID from user session/authentication
    private var currentParentId = ""
    
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
                currentParentId = user?.id?.toString() ?: ""
            } catch (e: Exception) {
                LoggerHelper.logError("Failed to load current user", e)
            }
        }
    }
    
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
                when (childResult) {
                    is KidsResult.Success -> {
                        val child = childResult.data
                        LoggerHelper.logDebug("Loaded child: ${child.name}, status: ${child.status}")
                        
                        // Load current service if child is checked in
                        val currentService = child.currentServiceId?.let { serviceId ->
                            val serviceResult = kidsRepository.getServiceById(serviceId)
                            when (serviceResult) {
                                is KidsResult.Success -> serviceResult.data
                                else -> null
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            child = child,
                            currentService = currentService,
                            error = null
                        )
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
                LoggerHelper.logError("Error loading child information", e)
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
        LoggerHelper.logDebug("Starting check-out process")
        _uiState.value = _uiState.value.copy(
            showParentVerification = true
        )
    }
    
    /**
     * Handle parent verification completion
     */
    fun onParentVerified() {
        LoggerHelper.logDebug("Parent verification completed")
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
    fun confirmCheckOut(notes: String? = null) {
        val childId = _uiState.value.childId
        if (childId == null) {
            LoggerHelper.logError("Cannot check out: child ID is null")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showCheckOutConfirmation = false,
                isCheckingOut = true
            )
            
            try {
                LoggerHelper.logDebug("Executing check-out for child $childId")
                val result = kidsRepository.checkOutChild(
                    childId = childId,
                    checkedOutBy = currentParentId,
                    notes = notes
                )
                
                when (result) {
                    is KidsResult.Success -> {
                        LoggerHelper.logInfo("Check-out successful for child")
                        _uiState.value = _uiState.value.copy(
                            isCheckingOut = false,
                            checkOutRecord = result.data,
                            showSuccessDialog = true
                        )
                    }
                    is KidsResult.Error -> {
                        LoggerHelper.logError("Check-out failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isCheckingOut = false,
                            checkOutError = result.message,
                            showErrorDialog = true
                        )
                    }
                    is KidsResult.NetworkError -> {
                        LoggerHelper.logError("Network error during check-out: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isCheckingOut = false,
                            checkOutError = "Network error: ${result.message}",
                            showErrorDialog = true
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                LoggerHelper.logError("Unexpected error during check-out", e)
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
            checkOutRecord = null
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
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val error: String? = null,
    val showParentVerification: Boolean = false,
    val showCheckOutConfirmation: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val checkOutRecord: CheckInRecord? = null,
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
        get() = child?.status?.canBeCheckedOut() == true
}