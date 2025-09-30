package rfm.hillsongptapp.feature.kids.ui

import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.feature.kids.ui.model.*

/**
 * UI state for the Kids Management screen with real-time updates support
 */
data class KidsManagementUiState(
    val children: List<Child> = emptyList(),
    val services: List<KidsService> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedChild: Child? = null,
    val showCheckInDialog: Boolean = false,
    val showCheckOutDialog: Boolean = false,
    val lastUpdated: Long = 0L,
    val autoRefreshEnabled: Boolean = true,
    val showConnectionStatus: Boolean = true,
    val hasStaffPermissions: Boolean = false,
    val currentUserId: String = ""
) {
    /**
     * Check if there are any children registered
     */
    val hasChildren: Boolean
        get() = children.isNotEmpty()
    
    /**
     * Get children currently checked in
     */
    val checkedInChildren: List<Child>
        get() = children.filter { it.isCheckedIn() }
    
    /**
     * Get children available for check-in
     */
    val availableChildren: List<Child>
        get() = children.filter { it.status.isAvailableForCheckIn() }
    
    /**
     * Check if any operation is in progress
     */
    val isOperationInProgress: Boolean
        get() = isLoading || isRefreshing
}