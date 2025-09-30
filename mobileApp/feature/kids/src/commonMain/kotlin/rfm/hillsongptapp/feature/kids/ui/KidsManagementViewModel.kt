package rfm.hillsongptapp.feature.kids.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.logging.LoggerHelper
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.KidsRepository
import rfm.hillsongptapp.core.data.repository.KidsResult
import rfm.hillsongptapp.core.data.repository.AuthRepository
import kotlinx.datetime.Clock

/**
 * ViewModel for the Kids Management screen
 * Manages state for children and services loading, check-in/check-out operations
 */
class KidsManagementViewModel(
    private val kidsRepository: KidsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val logger = LoggerHelper
    
    private val _uiState = MutableStateFlow(KidsManagementUiState())
    val uiState: StateFlow<KidsManagementUiState> = _uiState.asStateFlow()
    
    // Current user/parent ID from authentication system
    private var currentParentId: String = ""
    private var isStaffUser: Boolean = false
    
    init {
        loadUserSession()
        loadInitialData()
    }
    
    /**
     * Load current user session and determine permissions
     */
    private fun loadUserSession() {
        viewModelScope.launch {
            try {
                // Get current user (assuming user ID 1 is the logged-in user)
                val user = authRepository.getUserById(1)
                val userProfile = user?.let { authRepository.getUserProfileByUserId(it.id) }
                
                if (user != null && userProfile != null) {
                    currentParentId = user.id.toString()
                    isStaffUser = userProfile.isAdmin
                    
                    // Update UI state with user permissions
                    _uiState.value = _uiState.value.copy(
                        hasStaffPermissions = isStaffUser,
                        currentUserId = currentParentId
                    )
                    
                    LoggerHelper.logDebug("User session loaded: parentId=$currentParentId, isStaff=$isStaffUser")
                } else {
                    LoggerHelper.logError("No user session found")
                    _uiState.value = _uiState.value.copy(
                        error = "User session not found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                LoggerHelper.logError("Failed to load user session", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user session: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check if current user has staff permissions
     */
    fun hasStaffPermissions(): Boolean = isStaffUser
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String = currentParentId
    
    /**
     * Load initial data (children and services)
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load children and services concurrently
                val childrenResult = kidsRepository.getChildrenForParent(currentParentId)
                val servicesResult = kidsRepository.getAvailableServices()
                
                when (childrenResult) {
                    is KidsResult.Success -> {
                        when (servicesResult) {
                            is KidsResult.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    children = childrenResult.data,
                                    services = servicesResult.data,
                                    isLoading = false,
                                    error = null,
                                    lastUpdated = Clock.System.now().toEpochMilliseconds()
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
                            error = "Failed to load children: ${childrenResult.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Network error loading children: ${childrenResult.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Refresh data with pull-to-refresh
     */
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                // Load children and services concurrently
                val childrenResult = kidsRepository.getChildrenForParent(currentParentId)
                val servicesResult = kidsRepository.getAvailableServices()
                
                when (childrenResult) {
                    is KidsResult.Success -> {
                        when (servicesResult) {
                            is KidsResult.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    children = childrenResult.data,
                                    services = servicesResult.data,
                                    isRefreshing = false,
                                    error = null,
                                    lastUpdated = Clock.System.now().toEpochMilliseconds()
                                )
                            }
                            is KidsResult.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isRefreshing = false,
                                    error = "Failed to refresh services: ${servicesResult.message}"
                                )
                            }
                            is KidsResult.NetworkError -> {
                                _uiState.value = _uiState.value.copy(
                                    isRefreshing = false,
                                    error = "Network error refreshing services: ${servicesResult.message}"
                                )
                            }
                            is KidsResult.Loading -> {
                                // Should not happen in suspend function
                            }
                        }
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = "Failed to refresh children: ${childrenResult.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = "Network error refreshing children: ${childrenResult.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Failed to refresh data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Show check-in dialog for a child
     */
    fun showCheckInDialog(child: Child) {
        _uiState.value = _uiState.value.copy(
            selectedChild = child,
            showCheckInDialog = true
        )
    }
    
    /**
     * Show check-out dialog for a child
     */
    fun showCheckOutDialog(child: Child) {
        _uiState.value = _uiState.value.copy(
            selectedChild = child,
            showCheckOutDialog = true
        )
    }
    
    /**
     * Hide check-in dialog
     */
    fun hideCheckInDialog() {
        _uiState.value = _uiState.value.copy(
            selectedChild = null,
            showCheckInDialog = false
        )
    }
    
    /**
     * Hide check-out dialog
     */
    fun hideCheckOutDialog() {
        _uiState.value = _uiState.value.copy(
            selectedChild = null,
            showCheckOutDialog = false
        )
    }
    
    /**
     * Check in a child to a service
     */
    fun checkInChild(childId: String, serviceId: String) {
        viewModelScope.launch {
            try {
                val result = kidsRepository.checkInChild(
                    childId = childId,
                    serviceId = serviceId,
                    checkedInBy = currentParentId
                )
                
                when (result) {
                    is KidsResult.Success -> {
                        // Refresh data to get updated status
                        refreshData()
                        hideCheckInDialog()
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Check-in failed: ${result.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Network error during check-in: ${result.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Check-in failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check out a child from their current service
     */
    fun checkOutChild(childId: String) {
        viewModelScope.launch {
            try {
                val result = kidsRepository.checkOutChild(
                    childId = childId,
                    checkedOutBy = currentParentId
                )
                
                when (result) {
                    is KidsResult.Success -> {
                        // Refresh data to get updated status
                        refreshData()
                        hideCheckOutDialog()
                    }
                    is KidsResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Check-out failed: ${result.message}"
                        )
                    }
                    is KidsResult.NetworkError -> {
                        _uiState.value = _uiState.value.copy(
                            error = "Network error during check-out: ${result.message}"
                        )
                    }
                    is KidsResult.Loading -> {
                        // Should not happen in suspend function
                    }
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Check-out failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get services appropriate for a specific child
     */
    fun getServicesForChild(child: Child): List<KidsService> {
        return _uiState.value.services.filter { service ->
            child.isEligibleForService(service) && service.canAcceptCheckIn()
        }
    }
    
    /**
     * Enable/disable automatic UI refresh when real-time updates are received
     */
    fun setAutoRefreshEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoRefreshEnabled = enabled)
    }
    
    /**
     * Get formatted last updated time
     */
    fun getLastUpdatedTime(): String {
        val lastUpdated = _uiState.value.lastUpdated
        if (lastUpdated == 0L) return "Never"
        
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - lastUpdated
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${(diff / 60_000)}m ago"
            diff < 86400_000 -> "${(diff / 3600_000)}h ago"
            else -> "${(diff / 86400_000)}d ago"
        }
    }
}