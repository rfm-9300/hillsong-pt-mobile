package rfm.hillsongptapp.feature.kids.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import rfm.hillsongptapp.core.navigation.KidsNav

/**
 * Navigation state manager for Kids Management feature
 */
class KidsNavigationState(
    val navController: NavHostController
) {
    
    /**
     * Get current route from navigation state
     */
    @Composable
    fun getCurrentRoute(): KidsNav? {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        return navBackStackEntry?.destination?.route?.let { route ->
            when {
                route.contains("Management") -> KidsNav.Management
                route.contains("Registration") -> KidsNav.Registration
                route.contains("Services") && route.contains("childId") -> {
                    // Extract childId from route if needed
                    KidsNav.ServicesForChild("") // Placeholder
                }
                route.contains("Services") -> KidsNav.Services
                route.contains("CheckIn") -> {
                    // Extract childId from route if needed
                    KidsNav.CheckIn("") // Placeholder
                }
                route.contains("CheckOut") -> {
                    // Extract childId from route if needed
                    KidsNav.CheckOut("") // Placeholder
                }
                route.contains("EditChild") -> {
                    // Extract childId from route if needed
                    KidsNav.EditChild("") // Placeholder
                }
                route.contains("Reports") -> KidsNav.Reports
                else -> null
            }
        }
    }
    
    /**
     * Check if we can navigate back
     */
    fun canNavigateBack(): Boolean {
        return navController.previousBackStackEntry != null
    }
    
    /**
     * Navigate back with proper handling
     */
    fun navigateBack(): Boolean {
        return if (canNavigateBack()) {
            navController.popBackStack()
            true
        } else {
            false
        }
    }
    
    /**
     * Navigate to management screen and clear back stack
     */
    fun navigateToManagementAndClearStack() {
        navController.navigate(KidsNav.Management) {
            popUpTo(KidsNav.Management) {
                inclusive = false
            }
        }
    }
    
    /**
     * Check if current screen is the root screen
     */
    @Composable
    fun isRootScreen(): Boolean {
        return getCurrentRoute() == KidsNav.Management
    }

}

/**
 * Remember navigation state for Kids Management
 */
@Composable
fun rememberKidsNavigationState(
    navController: NavHostController
): KidsNavigationState {
    return remember(navController) {
        KidsNavigationState(navController)
    }
}

/**
 * Navigation flow states for complex workflows
 */
sealed class KidsNavigationFlow {
    object Registration : KidsNavigationFlow()
    data class CheckInFlow(val childId: String) : KidsNavigationFlow()
    data class CheckOutFlow(val childId: String) : KidsNavigationFlow()
    data class EditChildFlow(val childId: String) : KidsNavigationFlow()
    object ReportsFlow : KidsNavigationFlow()
}

/**
 * Handle complex navigation flows with proper state management
 */
class KidsNavigationFlowHandler(
    private val navigationState: KidsNavigationState
) {
    
    /**
     * Start a navigation flow
     */
    fun startFlow(flow: KidsNavigationFlow) {
        when (flow) {
            is KidsNavigationFlow.Registration -> {
                navigationState.navController.navigate(KidsNav.Registration)
            }
            is KidsNavigationFlow.CheckInFlow -> {
                navigationState.navController.navigate(KidsNav.CheckIn(flow.childId))
            }
            is KidsNavigationFlow.CheckOutFlow -> {
                navigationState.navController.navigate(KidsNav.CheckOut(flow.childId))
            }
            is KidsNavigationFlow.EditChildFlow -> {
                navigationState.navController.navigate(KidsNav.EditChild(flow.childId))
            }
            is KidsNavigationFlow.ReportsFlow -> {
                navigationState.navController.navigate(KidsNav.Reports)
            }
        }
    }
    
    /**
     * Complete a navigation flow and return to management
     */
    fun completeFlow() {
        navigationState.navigateToManagementAndClearStack()
    }
    
    /**
     * Cancel a navigation flow and return to previous screen
     */
    fun cancelFlow() {
        navigationState.navigateBack()
    }
}