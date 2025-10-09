package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.CheckInRequestApiService
import rfm.hillsongptapp.core.network.ktor.requests.ApproveCheckInDto
import rfm.hillsongptapp.core.network.ktor.requests.RejectCheckInDto
import rfm.hillsongptapp.core.network.ktor.responses.CheckInApprovalResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRejectionResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestDetailsResponse
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * ViewModel for staff check-in operations
 * 
 * Handles:
 * - Fetching check-in request details by token
 * - Approving check-in requests
 * - Rejecting check-in requests with reason
 * - Loading and error states
 * 
 * Requirements: 3.2, 4.1, 4.5
 */
class StaffCheckInViewModel(
    private val checkInRequestApiService: CheckInRequestApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StaffCheckInUiState())
    val uiState: StateFlow<StaffCheckInUiState> = _uiState.asStateFlow()
    
    /**
     * Get check-in request details by token
     * Called after QR code is scanned
     * 
     * @param token The token from the scanned QR code
     */
    fun getRequestDetails(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = checkInRequestApiService.getRequestByToken(token)) {
                is NetworkResult.Success -> {
                    result.data.data?.let { details ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                currentRequest = details,
                                currentToken = token,
                                error = null
                            )
                        }
                    } ?: run {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to load check-in request details"
                            )
                        }
                    }
                }
                
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load check-in request"
                        )
                    }
                }
                
                is NetworkResult.Loading -> {
                    // Loading state already handled
                }
            }
        }
    }
    
    /**
     * Approve a check-in request
     * Creates an attendance record and notifies the parent
     * 
     * @param notes Optional notes from staff
     */
    fun approveCheckIn(notes: String? = null) {
        val token = _uiState.value.currentToken ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = ApproveCheckInDto(notes = notes)
            
            when (val result = checkInRequestApiService.approveCheckIn(token, request)) {
                is NetworkResult.Success -> {
                    result.data.data?.let { approval ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                approvalResult = approval,
                                error = null
                            )
                        }
                    } ?: run {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to approve check-in"
                            )
                        }
                    }
                }
                
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to approve check-in"
                        )
                    }
                }
                
                is NetworkResult.Loading -> {
                    // Loading state already handled
                }
            }
        }
    }
    
    /**
     * Reject a check-in request
     * Marks the request as rejected and notifies the parent with reason
     * 
     * @param reason Required reason for rejection
     */
    fun rejectCheckIn(reason: String) {
        if (reason.isBlank()) {
            _uiState.update { it.copy(error = "Rejection reason is required") }
            return
        }
        
        val token = _uiState.value.currentToken ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val request = RejectCheckInDto(reason = reason)
            
            when (val result = checkInRequestApiService.rejectCheckIn(token, request)) {
                is NetworkResult.Success -> {
                    result.data.data?.let { rejection ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                rejectionResult = rejection,
                                error = null
                            )
                        }
                    } ?: run {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to reject check-in"
                            )
                        }
                    }
                }
                
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to reject check-in"
                        )
                    }
                }
                
                is NetworkResult.Loading -> {
                    // Loading state already handled
                }
            }
        }
    }
    
    /**
     * Clear the current error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Set an error message (used by scanner screen)
     */
    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
    
    /**
     * Clear the current request and results
     * Used when navigating back to scanner
     */
    fun clearCurrentRequest() {
        _uiState.update { 
            StaffCheckInUiState()
        }
    }
}

/**
 * UI state for staff check-in operations
 */
data class StaffCheckInUiState(
    val isLoading: Boolean = false,
    val currentRequest: CheckInRequestDetailsResponse? = null,
    val currentToken: String? = null,
    val approvalResult: CheckInApprovalResponse? = null,
    val rejectionResult: CheckInRejectionResponse? = null,
    val error: String? = null
)
