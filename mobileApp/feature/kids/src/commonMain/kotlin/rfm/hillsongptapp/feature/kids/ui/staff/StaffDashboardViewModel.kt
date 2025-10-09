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
        loadDashboardData()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Load current check-ins
            loadCurrentCheckIns()
            
            // Load pending requests
            loadPendingRequests()
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Load current check-ins for today
     */
    private suspend fun loadCurrentCheckIns() {
        when (val result = kidsApiService.getCurrentCheckIns()) {
            is NetworkResult.Success -> {
                result.data.data?.let { checkIns ->
                    _uiState.update { 
                        it.copy(
                            currentCheckIns = checkIns,
                            totalCheckInsToday = checkIns.size
                        )
                    }
                }
            }
            
            is NetworkResult.Error -> {
                _uiState.update { 
                    it.copy(
                        error = result.exception.message ?: "Failed to load current check-ins"
                    )
                }
            }
            
            is NetworkResult.Loading -> {
                // Loading state already handled
            }
        }
    }
    
    /**
     * Load pending check-in requests
     * Note: This endpoint returns active requests for the current user's children,
     * but for staff we want to see all pending requests. This may need a backend endpoint update.
     */
    private suspend fun loadPendingRequests() {
        when (val result = checkInRequestApiService.getActiveRequests()) {
            is NetworkResult.Success -> {
                result.data.data?.let { requests ->
                    _uiState.update { 
                        it.copy(
                            pendingRequests = requests,
                            pendingRequestsCount = requests.size
                        )
                    }
                }
            }
            
            is NetworkResult.Error -> {
                // Don't override error if we already have one from check-ins
                if (_uiState.value.error == null) {
                    _uiState.update { 
                        it.copy(
                            error = result.exception.message ?: "Failed to load pending requests"
                        )
                    }
                }
            }
            
            is NetworkResult.Loading -> {
                // Loading state already handled
            }
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
    val error: String? = null
)
