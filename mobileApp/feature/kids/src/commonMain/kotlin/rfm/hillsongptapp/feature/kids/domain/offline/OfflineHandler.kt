package rfm.hillsongptapp.feature.kids.domain.offline

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import co.touchlab.kermit.Logger

/**
 * Handles offline scenarios and graceful degradation for kids management
 */
class OfflineHandler(
    private val logger: Logger
) {
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    private val _offlineCapabilities = MutableStateFlow(OfflineCapabilities())
    val offlineCapabilities: StateFlow<OfflineCapabilities> = _offlineCapabilities.asStateFlow()
    
    private val _pendingOperations = MutableStateFlow<List<PendingOperation>>(emptyList())
    val pendingOperations: StateFlow<List<PendingOperation>> = _pendingOperations.asStateFlow()
    
    /**
     * Set offline status
     */
    fun setOfflineStatus(isOffline: Boolean) {
        logger.i { "Setting offline status: $isOffline" }
        _isOffline.value = isOffline
        
        if (isOffline) {
            updateOfflineCapabilities()
        }
    }
    
    /**
     * Update available offline capabilities
     */
    private fun updateOfflineCapabilities() {
        _offlineCapabilities.value = OfflineCapabilities(
            canViewChildren = true,
            canViewServices = true,
            canEditChildInfo = false,
            canCheckIn = false,
            canCheckOut = false,
            canRegisterChild = false,
            canViewReports = true,
            hasRealTimeUpdates = false
        )
    }
    
    /**
     * Check if operation is available offline
     */
    fun isOperationAvailableOffline(operation: OfflineOperation): Boolean {
        val capabilities = _offlineCapabilities.value
        return when (operation) {
            OfflineOperation.VIEW_CHILDREN -> capabilities.canViewChildren
            OfflineOperation.VIEW_SERVICES -> capabilities.canViewServices
            OfflineOperation.EDIT_CHILD -> capabilities.canEditChildInfo
            OfflineOperation.CHECK_IN -> capabilities.canCheckIn
            OfflineOperation.CHECK_OUT -> capabilities.canCheckOut
            OfflineOperation.REGISTER_CHILD -> capabilities.canRegisterChild
            OfflineOperation.VIEW_REPORTS -> capabilities.canViewReports
        }
    }
    
    /**
     * Get offline message for unavailable operation
     */
    fun getOfflineMessage(operation: OfflineOperation): String {
        return when (operation) {
            OfflineOperation.EDIT_CHILD -> 
                "Child information cannot be edited while offline. Changes will be available when you reconnect."
            
            OfflineOperation.CHECK_IN -> 
                "Check-in is not available offline for safety reasons. Please connect to the internet to check in children."
            
            OfflineOperation.CHECK_OUT -> 
                "Check-out is not available offline for safety reasons. Please connect to the internet to check out children."
            
            OfflineOperation.REGISTER_CHILD -> 
                "New child registration requires an internet connection. Please connect and try again."
            
            OfflineOperation.VIEW_CHILDREN -> 
                "Child list is not available offline. Please connect to view your children."
            
            OfflineOperation.VIEW_SERVICES -> 
                "Service information is not available offline. Please connect to view available services."
            
            OfflineOperation.VIEW_REPORTS -> 
                "Reports are not available offline. Please connect to view current reports."
        }
    }
    
    /**
     * Add pending operation for when connection is restored
     */
    fun addPendingOperation(operation: PendingOperation) {
        logger.d { "Adding pending operation: ${operation.type}" }
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.add(operation)
        _pendingOperations.value = currentOperations
    }
    
    /**
     * Remove pending operation
     */
    fun removePendingOperation(operationId: String) {
        logger.d { "Removing pending operation: $operationId" }
        val currentOperations = _pendingOperations.value.toMutableList()
        currentOperations.removeAll { it.id == operationId }
        _pendingOperations.value = currentOperations
    }
    
    /**
     * Clear all pending operations
     */
    fun clearPendingOperations() {
        logger.d { "Clearing all pending operations" }
        _pendingOperations.value = emptyList()
    }
    
    /**
     * Get fallback data message
     */
    fun getFallbackDataMessage(): String {
        return "Showing cached information. Some data may be outdated. Connect to the internet for the latest updates."
    }
    
    /**
     * Get reconnection message
     */
    fun getReconnectionMessage(): String {
        val pendingCount = _pendingOperations.value.size
        return if (pendingCount > 0) {
            "Connection restored! Syncing $pendingCount pending operation${if (pendingCount > 1) "s" else ""}..."
        } else {
            "Connection restored! All features are now available."
        }
    }
    
    /**
     * Get offline status message
     */
    fun getOfflineStatusMessage(): String {
        return "You're currently offline. Some features are limited. Check your internet connection."
    }
    
    /**
     * Check if cached data is stale
     */
    fun isCachedDataStale(lastUpdated: Long, maxAgeMs: Long = 300_000L): Boolean { // 5 minutes default
        return System.currentTimeMillis() - lastUpdated > maxAgeMs
    }
    
    /**
     * Get stale data warning message
     */
    fun getStaleDataWarning(lastUpdated: Long): String {
        val ageMinutes = (System.currentTimeMillis() - lastUpdated) / 60_000L
        return "This information is $ageMinutes minute${if (ageMinutes > 1) "s" else ""} old. Connect to refresh."
    }
    
    /**
     * Get offline feature limitations
     */
    fun getOfflineFeatureLimitations(): List<String> {
        return listOf(
            "Check-in and check-out operations are disabled for safety",
            "Child registration and editing require internet connection",
            "Real-time status updates are not available",
            "Service capacity information may be outdated",
            "Reports show cached data only"
        )
    }
    
    /**
     * Get recovery suggestions for offline mode
     */
    fun getOfflineRecoverySuggestions(): List<String> {
        return listOf(
            "Check your WiFi or mobile data connection",
            "Move to an area with better signal strength",
            "Restart your internet connection",
            "Contact church staff if you need immediate assistance",
            "Try again when connection is restored"
        )
    }
}

/**
 * Available offline capabilities
 */
data class OfflineCapabilities(
    val canViewChildren: Boolean = false,
    val canViewServices: Boolean = false,
    val canEditChildInfo: Boolean = false,
    val canCheckIn: Boolean = false,
    val canCheckOut: Boolean = false,
    val canRegisterChild: Boolean = false,
    val canViewReports: Boolean = false,
    val hasRealTimeUpdates: Boolean = false
)

/**
 * Types of operations that may be available offline
 */
enum class OfflineOperation {
    VIEW_CHILDREN,
    VIEW_SERVICES,
    EDIT_CHILD,
    CHECK_IN,
    CHECK_OUT,
    REGISTER_CHILD,
    VIEW_REPORTS
}

/**
 * Pending operation to be executed when connection is restored
 */
data class PendingOperation(
    val id: String,
    val type: PendingOperationType,
    val data: Map<String, Any>,
    val timestamp: Long,
    val description: String
)

/**
 * Types of operations that can be queued for later execution
 */
enum class PendingOperationType {
    REGISTER_CHILD,
    UPDATE_CHILD,
    CHECK_IN_CHILD,
    CHECK_OUT_CHILD,
    SYNC_DATA
}