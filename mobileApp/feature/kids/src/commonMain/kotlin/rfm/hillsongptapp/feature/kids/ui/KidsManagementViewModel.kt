package rfm.hillsongptapp.feature.kids.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import rfm.hillsongptapp.feature.kids.data.network.websocket.RealTimeStatusManager
import rfm.hillsongptapp.feature.kids.data.network.websocket.StatusNotification
import rfm.hillsongptapp.feature.kids.data.network.websocket.ConnectionStatus
import rfm.hillsongptapp.core.data.repository.AuthRepository

/**
 * ViewModel for the Kids Management screen
 * Manages state for children and services loading, check-in/check-out operations with real-time updates
 */
class KidsManagementViewModel(
    private val kidsRepository: KidsRepository,
    private val realTimeStatusManager: RealTimeStatusManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val logger = Logger.withTag("KidsManagementViewModel")
    
    private val _uiState = MutableStateFlow(KidsManagementUiState())
    val uiState: StateFlow<KidsManagementUiState> = _uiState.asStateFlow()
    
    // Real-time status flows
    val connectionStatus: StateFlow<ConnectionStatus> = realTimeStatusManager.connectionStatus
    val notifications: StateFlow<List<StatusNotification>> = MutableStateFlow(emptyList())
    
    // Current user/parent ID from authentication system
    private var currentParentId: String = ""
    private var isStaffUser: Boolean = false
    
    // Notification management
    private val _activeNotifications = MutableStateFlow<List<StatusNotification>>(emptyList())
    val activeNotifications: StateFlow<List<StatusNotification>> = _activeNotifications.asStateFlow()
    
    init {
        loadUserSession()
        loadInitialData()
        setupRealTimeUpdates()
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
                    
                    logger.d { "User session loaded: parentId=$currentParentId, isStaff=$isStaffUser" }
                } else {
                    logger.w { "No user session found" }
                    _uiState.value = _uiState.value.copy(
                        error = "User session not found. Please log in again."
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load user session" }
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
     * Setup real-time updates and connection monitoring
     */
    private fun setupRealTimeUpdates() {
        viewModelScope.launch {
            // Connect to real-time updates
            connectToRealTimeUpdates()
            
            // Listen for child status updates
            realTimeStatusManager.childStatusUpdates.collect { update ->
                logger.d { "Received child status update: ${update.childId} -> ${update.newStatus}" }
                handleChildStatusUpdate(update)
            }
        }
        
        viewModelScope.launch {
            // Listen for service status updates
            realTimeStatusManager.serviceStatusUpdates.collect { update ->
                logger.d { "Received service status update: ${update.serviceId} capacity: ${update.newCapacity}" }
                handleServiceStatusUpdate(update)
            }
        }
        
        viewModelScope.launch {
            // Listen for check-in/check-out updates
            realTimeStatusManager.checkInUpdates.collect { update ->
                logger.d { "Received check-in update: ${update.childName} ${update.action}" }
                handleCheckInUpdate(update)
            }
        }
        
        viewModelScope.launch {
            // Listen for notifications
            realTimeStatusManager.notifications.collect { notification ->
                logger.d { "Received notification: ${notification.title}" }
                addNotification(notification)
            }
        }
    }
    
    /**
     * Connect to real-time updates
     */
    private suspend fun connectToRealTimeUpdates() {
        try {
            val result = realTimeStatusManager.connect()
            if (result.isSuccess) {
                logger.i { "Connected to real-time updates" }
                
                // Subscribe to updates for all children
                _uiState.value.children.forEach { child ->
                    realTimeStatusManager.subscribeToChild(child.id)
                }
                
                // Subscribe to updates for all services
                _uiState.value.services.forEach { service ->
                    realTimeStatusManager.subscribeToService(service.id)
                }
            } else {
                logger.w { "Failed to connect to real-time updates: ${result.exceptionOrNull()?.message}" }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error connecting to real-time updates" }
        }
    }
    
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
                
                val children = childrenResult.getOrElse { 
                    throw it
                }
                
                val services = servicesResult.getOrElse {
                    throw it
                }
                
                _uiState.value = _uiState.value.copy(
                    children = children,
                    services = services,
                    isLoading = false,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
                
                // Subscribe to real-time updates for loaded data
                if (realTimeStatusManager.isConnected()) {
                    children.forEach { child ->
                        realTimeStatusManager.subscribeToChild(child.id)
                    }
                    services.forEach { service ->
                        realTimeStatusManager.subscribeToService(service.id)
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
     * Handle child status updates from real-time system
     */
    private fun handleChildStatusUpdate(update: rfm.hillsongptapp.feature.kids.data.network.websocket.ChildStatusUpdate) {
        val currentChildren = _uiState.value.children.toMutableList()
        val childIndex = currentChildren.indexOfFirst { it.id == update.childId }
        
        if (childIndex != -1) {
            // Update the child's status in the local list
            // Note: In a real implementation, you'd need to fetch the updated child from repository
            // For now, we'll trigger a refresh to get the latest data
            refreshData()
        }
    }
    
    /**
     * Handle service status updates from real-time system
     */
    private fun handleServiceStatusUpdate(update: rfm.hillsongptapp.feature.kids.data.network.websocket.ServiceStatusUpdate) {
        val currentServices = _uiState.value.services.toMutableList()
        val serviceIndex = currentServices.indexOfFirst { it.id == update.serviceId }
        
        if (serviceIndex != -1) {
            // Update the service's capacity in the local list
            // Note: In a real implementation, you'd update the specific service object
            // For now, we'll trigger a refresh to get the latest data
            refreshData()
        }
    }
    
    /**
     * Handle check-in/check-out updates from real-time system
     */
    private fun handleCheckInUpdate(update: rfm.hillsongptapp.feature.kids.data.network.websocket.CheckInStatusUpdate) {
        // Refresh data to get the latest check-in status
        refreshData()
    }
    
    /**
     * Add a notification to the active notifications list
     */
    private fun addNotification(notification: StatusNotification) {
        val currentNotifications = _activeNotifications.value.toMutableList()
        currentNotifications.add(0, notification) // Add to beginning
        
        // Keep only the last 5 notifications
        if (currentNotifications.size > 5) {
            currentNotifications.removeAt(currentNotifications.size - 1)
        }
        
        _activeNotifications.value = currentNotifications
    }
    
    /**
     * Dismiss a notification
     */
    fun dismissNotification(notification: StatusNotification) {
        val currentNotifications = _activeNotifications.value.toMutableList()
        currentNotifications.remove(notification)
        _activeNotifications.value = currentNotifications
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _activeNotifications.value = emptyList()
    }
    
    /**
     * Retry connection to real-time updates
     */
    fun retryConnection() {
        viewModelScope.launch {
            connectToRealTimeUpdates()
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
                
                val children = childrenResult.getOrElse { 
                    throw it
                }
                
                val services = servicesResult.getOrElse {
                    throw it
                }
                
                _uiState.value = _uiState.value.copy(
                    children = children,
                    services = services,
                    isRefreshing = false,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
                
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
                
                result.fold(
                    onSuccess = {
                        // Refresh data to get updated status
                        refreshData()
                        hideCheckInDialog()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Check-in failed: ${error.message}"
                        )
                    }
                )
                
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
                
                result.fold(
                    onSuccess = {
                        // Refresh data to get updated status
                        refreshData()
                        hideCheckOutDialog()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Check-out failed: ${error.message}"
                        )
                    }
                )
                
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
    fun getServicesForChild(child: Child): List<rfm.hillsongptapp.feature.kids.domain.model.KidsService> {
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
        
        val now = System.currentTimeMillis()
        val diff = now - lastUpdated
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> "${diff / 86400_000}d ago"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cleanup real-time connections
        viewModelScope.launch {
            realTimeStatusManager.disconnect()
        }
    }
}