package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.CheckInRequestApiService
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * ViewModel for staff dashboard
 * 
 * Handles:
 * - Fetching current check-ins for the day
 * - Fetching pending check-in requests count
 * - Fetching service capacity status
 * - Refreshing dashboard data
 * 
 * Requirements: 5.1, 8.1
 */
class StaffDashboardViewModel(
    private val kidsApiService: KidsApiService,
    private val checkInRequestApiService: CheckInRequestApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StaffDashboardUiState())
    val uiState: StateFlow<StaffDashboardUiState> = _uiState.asStateFlow()
    
    init {
        try {
            LoggerHelper.logDebug("StaffDashboardViewModel initialized with services: kidsApiService=${kidsApiService::class.simpleName}, checkInRequestApiService=${checkInRequestApiService::class.simpleName}", "StaffDashboardViewModel")
            loadDashboardData()
        } catch (e: Exception) {
            LoggerHelper.logDebug("Error during StaffDashboardViewModel initialization: ${e.message}", "StaffDashboardViewModel")
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to initialize dashboard. Please restart the app."
                )
            }
        }
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                LoggerHelper.logDebug("Loading staff dashboard data", "StaffDashboardViewModel")
                _uiState.update { 
                    it.copy(
                        isLoading = true, 
                        error = null,
                        checkInsLoaded = false,
                        pendingRequestsLoaded = false
                    ) 
                }
                
                var checkInsError: String? = null
                var pendingRequestsError: String? = null
                
                // Load current check-ins
                try {
                    loadCurrentCheckIns()
                } catch (e: Exception) {
                    checkInsError = "Failed to load check-ins"
                    LoggerHelper.logDebug("Check-ins loading failed: ${e.message}", "StaffDashboardViewModel")
                }
                
                // Load pending requests
                try {
                    loadPendingRequests()
                } catch (e: Exception) {
                    pendingRequestsError = "Failed to load pending requests"
                    LoggerHelper.logDebug("Pending requests loading failed: ${e.message}", "StaffDashboardViewModel")
                }
                
                // Only show error if both failed
                val finalError = if (checkInsError != null && pendingRequestsError != null) {
                    "Unable to load dashboard data. Please check your connection and try again."
                } else if (checkInsError != null) {
                    // Only check-ins failed, but we have pending requests - don't show error
                    null
                } else if (pendingRequestsError != null) {
                    // Only pending requests failed, but we have check-ins - don't show error
                    null
                } else {
                    null
                }
                
                _uiState.update { it.copy(isLoading = false, error = finalError) }
                LoggerHelper.logDebug("Staff dashboard data loading completed. CheckIns error: $checkInsError, PendingRequests error: $pendingRequestsError", "StaffDashboardViewModel")
            } catch (e: Exception) {
                LoggerHelper.logDebug("Error loading staff dashboard data: ${e.message}", "StaffDashboardViewModel")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Unable to load dashboard data. Please try again."
                    )
                }
            }
        }
    }
    
    /**
     * Load current check-ins for today
     */
    private suspend fun loadCurrentCheckIns() {
        try {
            LoggerHelper.logDebug("Loading current check-ins", "StaffDashboardViewModel")
            when (val result = kidsApiService.getCurrentCheckIns()) {
                is NetworkResult.Success -> {
                    LoggerHelper.logDebug("Successfully loaded current check-ins: ${result.data.data?.size ?: 0} items", "StaffDashboardViewModel")
                    result.data.data?.let { checkIns ->
                        _uiState.update { 
                            it.copy(
                                currentCheckIns = checkIns,
                                totalCheckInsToday = checkIns.size,
                                checkInsLoaded = true
                            )
                        }
                    }
                }
                
                is NetworkResult.Error -> {
                    val errorMessage = result.exception.message ?: "Network error occurred"
                    LoggerHelper.logDebug("Error loading current check-ins: $errorMessage", "StaffDashboardViewModel")
                    throw Exception("Failed to load current check-ins: $errorMessage")
                }
                
                is NetworkResult.Loading -> {
                    // Loading state already handled
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logDebug("Exception loading current check-ins: ${e.message}", "StaffDashboardViewModel")
            throw e
        }
    }
    
    /**
     * Load pending check-in requests
     * Note: This endpoint returns active requests for the current user's children,
     * but for staff we want to see all pending requests. This may need a backend endpoint update.
     */
    private suspend fun loadPendingRequests() {
        try {
            LoggerHelper.logDebug("Loading pending requests", "StaffDashboardViewModel")
            when (val result = checkInRequestApiService.getActiveRequests()) {
                is NetworkResult.Success -> {
                    LoggerHelper.logDebug("Successfully loaded pending requests: ${result.data.data?.size ?: 0} items", "StaffDashboardViewModel")
                    result.data.data?.let { requests ->
                        _uiState.update { 
                            it.copy(
                                pendingRequests = requests,
                                pendingRequestsCount = requests.size,
                                pendingRequestsLoaded = true
                            )
                        }
                    }
                }
                
                is NetworkResult.Error -> {
                    val errorMessage = result.exception.message ?: "Network error occurred"
                    LoggerHelper.logDebug("Error loading pending requests: $errorMessage", "StaffDashboardViewModel")
                    throw Exception("Failed to load pending requests: $errorMessage")
                }
                
                is NetworkResult.Loading -> {
                    // Loading state already handled
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logDebug("Exception loading pending requests: ${e.message}", "StaffDashboardViewModel")
            throw e
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refresh() {
        loadDashboardData()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for staff dashboard
 */
data class StaffDashboardUiState(
    val isLoading: Boolean = false,
    val currentCheckIns: List<CheckInRecordResponse> = emptyList(),
    val pendingRequests: List<CheckInRequestResponse> = emptyList(),
    val totalCheckInsToday: Int = 0,
    val pendingRequestsCount: Int = 0,
    val error: String? = null,
    val checkInsLoaded: Boolean = false,
    val pendingRequestsLoaded: Boolean = false
) {
    val hasAnyData: Boolean
        get() = checkInsLoaded || pendingRequestsLoaded
}
