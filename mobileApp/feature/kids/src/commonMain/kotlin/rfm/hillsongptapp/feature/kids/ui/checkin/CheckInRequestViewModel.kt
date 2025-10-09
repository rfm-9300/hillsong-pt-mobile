package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.network.api.CheckInRequestApiService
import rfm.hillsongptapp.core.network.ktor.requests.CreateCheckInRequestDto
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInStatusNotification
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.websocket.CheckInWebSocketClient
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * ViewModel for managing check-in requests
 * Handles creating, canceling, and tracking active check-in requests
 * Integrates WebSocket for real-time status updates
 */
class CheckInRequestViewModel(
    private val checkInRequestApiService: CheckInRequestApiService,
    private val authRepository: AuthRepository,
    private val webSocketClient: CheckInWebSocketClient
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CheckInRequestUiState())
    val uiState: StateFlow<CheckInRequestUiState> = _uiState.asStateFlow()
    
    private var isWebSocketConnected = false
    
    /**
     * Create a new check-in request for a child
     * 
     * @param childId The ID of the child to check in
     * @param serviceId The ID of the service to check in to
     * @param notes Optional notes for the check-in request
     */
    fun createCheckInRequest(childId: Long, serviceId: Long, notes: String? = null) {
        LoggerHelper.logDebug("createCheckInRequest called with childId=$childId, serviceId=$serviceId", "CheckInRequestViewModel")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            LoggerHelper.logDebug("State updated to loading", "CheckInRequestViewModel")
            
            val request = CreateCheckInRequestDto(
                childId = childId,
                serviceId = serviceId,
                notes = notes
            )
            
            LoggerHelper.logDebug("Calling API to create check-in request", "CheckInRequestViewModel")
            when (val result = checkInRequestApiService.createCheckInRequest(request)) {
                is NetworkResult.Success -> {
                    LoggerHelper.logDebug("API call successful", "CheckInRequestViewModel")
                    result.data.data?.let { checkInRequest ->
                        LoggerHelper.logDebug("Check-in request created: id=${checkInRequest.id}, token=${checkInRequest.token}", "CheckInRequestViewModel")
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                activeRequests = currentState.activeRequests + checkInRequest,
                                currentRequest = checkInRequest,
                                error = null
                            )
                        }
                        LoggerHelper.logDebug("State updated with new request", "CheckInRequestViewModel")
                    } ?: run {
                        LoggerHelper.logDebug("ERROR: API returned success but no data", "CheckInRequestViewModel")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to create check-in request: No data received"
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    LoggerHelper.logDebug("ERROR: API call failed: ${result.exception.message}", "CheckInRequestViewModel")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to create check-in request"
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    LoggerHelper.logDebug("API call in loading state", "CheckInRequestViewModel")
                }
            }
        }
    }
    
    /**
     * Cancel a pending check-in request
     * 
     * @param requestId The ID of the request to cancel
     */
    fun cancelRequest(requestId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = checkInRequestApiService.cancelRequest(requestId)) {
                is NetworkResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            activeRequests = currentState.activeRequests.filter { it.id != requestId },
                            currentRequest = if (currentState.currentRequest?.id == requestId) null else currentState.currentRequest,
                            error = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to cancel check-in request"
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Load all active check-in requests for the current user
     */
    fun loadActiveRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = checkInRequestApiService.getActiveRequests()) {
                is NetworkResult.Success -> {
                    val requests = result.data.data ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            activeRequests = requests,
                            error = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to load active requests"
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Clear the current request from state
     */
    fun clearCurrentRequest() {
        _uiState.update { it.copy(currentRequest = null) }
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Update the current request (e.g., when status changes via WebSocket)
     */
    fun updateRequestStatus(requestId: Long, newStatus: String) {
        _uiState.update { currentState ->
            val updatedRequests = currentState.activeRequests.map { request ->
                if (request.id == requestId) {
                    request.copy(status = newStatus)
                } else {
                    request
                }
            }
            
            val updatedCurrentRequest = if (currentState.currentRequest?.id == requestId) {
                currentState.currentRequest.copy(status = newStatus)
            } else {
                currentState.currentRequest
            }
            
            currentState.copy(
                activeRequests = updatedRequests,
                currentRequest = updatedCurrentRequest
            )
        }
    }
    
    /**
     * Connect to WebSocket for real-time status updates
     * Should be called when the screen becomes active
     */
    fun connectWebSocket() {
        if (isWebSocketConnected) {
            LoggerHelper.logDebug("WebSocket already connected", "CheckInRequestViewModel")
            return
        }
        
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    LoggerHelper.logDebug("Connecting WebSocket for user ${user.id}", "CheckInRequestViewModel")
                    
                    webSocketClient.connect(user.id) { notification ->
                        handleStatusNotification(notification)
                    }
                    
                    isWebSocketConnected = true
                } else {
                    LoggerHelper.logError("Cannot connect WebSocket: No authenticated user", tag = "CheckInRequestViewModel")
                }
            } catch (e: Exception) {
                LoggerHelper.logError("Error connecting WebSocket: ${e.message}", tag = "CheckInRequestViewModel")
            }
        }
    }
    
    /**
     * Disconnect from WebSocket
     * Should be called when the screen is disposed or becomes inactive
     */
    fun disconnectWebSocket() {
        if (!isWebSocketConnected) {
            return
        }
        
        LoggerHelper.logDebug("Disconnecting WebSocket", "CheckInRequestViewModel")
        webSocketClient.disconnect()
        isWebSocketConnected = false
    }
    
    /**
     * Handle incoming WebSocket status notifications
     */
    private fun handleStatusNotification(notification: CheckInStatusNotification) {
        LoggerHelper.logDebug(
            "Received status notification: requestId=${notification.requestId}, status=${notification.status}",
            "CheckInRequestViewModel"
        )
        
        // Update the request status in the UI state
        updateRequestStatus(notification.requestId, notification.status)
        
        // If the current request was approved or rejected, we might want to show a message
        _uiState.update { currentState ->
            if (currentState.currentRequest?.id == notification.requestId) {
                when (notification.status) {
                    "APPROVED" -> {
                        currentState.copy(
                            statusMessage = "Check-in approved! Your child has been checked in."
                        )
                    }
                    "REJECTED" -> {
                        currentState.copy(
                            statusMessage = "Check-in request was rejected by staff."
                        )
                    }
                    else -> currentState
                }
            } else {
                currentState
            }
        }
    }
    
    /**
     * Clear status message
     */
    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
    
    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
        webSocketClient.close()
    }
}

/**
 * UI state for check-in requests
 */
data class CheckInRequestUiState(
    val isLoading: Boolean = false,
    val activeRequests: List<CheckInRequestResponse> = emptyList(),
    val currentRequest: CheckInRequestResponse? = null,
    val error: String? = null,
    val statusMessage: String? = null
)
