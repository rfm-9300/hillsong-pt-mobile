package rfm.hillsongptapp.feature.kids.navigation

import androidx.navigation.NavHostController
import rfm.hillsongptapp.core.navigation.KidsNav
import rfm.hillsongptapp.core.navigation.navigateToKidsCheckIn
import rfm.hillsongptapp.core.navigation.navigateToKidsCheckOut
import rfm.hillsongptapp.core.navigation.navigateToKidsEditChild
import rfm.hillsongptapp.core.navigation.navigateToKidsManagement
import rfm.hillsongptapp.core.navigation.navigateToKidsRegistration
import rfm.hillsongptapp.core.navigation.navigateToKidsReports
import rfm.hillsongptapp.core.navigation.navigateToKidsServices
import rfm.hillsongptapp.core.navigation.navigateToKidsServicesForChild

/**
 * Deep linking support for Kids Management feature
 */
object KidsDeepLinking {
    
    /**
     * Handle deep link navigation to specific kids screens
     */
    fun handleDeepLink(
        navController: NavHostController,
        deepLink: String
    ): Boolean {
        return when {
            deepLink.startsWith("kids://management") -> {
                navController.navigateToKidsManagement()
                true
            }
            
            deepLink.startsWith("kids://registration") -> {
                navController.navigateToKidsRegistration()
                true
            }
            
            deepLink.startsWith("kids://services") -> {
                val childId = extractChildIdFromDeepLink(deepLink)
                if (childId != null) {
                    navController.navigateToKidsServicesForChild(childId)
                } else {
                    navController.navigateToKidsServices()
                }
                true
            }
            
            deepLink.startsWith("kids://checkin") -> {
                val childId = extractChildIdFromDeepLink(deepLink)
                if (childId != null) {
                    navController.navigateToKidsCheckIn(childId)
                    true
                } else {
                    false
                }
            }
            
            deepLink.startsWith("kids://checkout") -> {
                val childId = extractChildIdFromDeepLink(deepLink)
                if (childId != null) {
                    navController.navigateToKidsCheckOut(childId)
                    true
                } else {
                    false
                }
            }
            
            deepLink.startsWith("kids://edit") -> {
                val childId = extractChildIdFromDeepLink(deepLink)
                if (childId != null) {
                    navController.navigateToKidsEditChild(childId)
                    true
                } else {
                    false
                }
            }
            
            deepLink.startsWith("kids://reports") -> {
                navController.navigateToKidsReports()
                true
            }
            
            else -> false
        }
    }

    
    /**
     * Extract child ID from deep link URL
     */
    private fun extractChildIdFromDeepLink(deepLink: String): String? {
        val regex = Regex("childId=([^&]+)")
        return regex.find(deepLink)?.groupValues?.get(1)
    }
    
    /**
     * Validate if deep link is for kids feature
     */
    fun isKidsDeepLink(deepLink: String): Boolean {
        return deepLink.startsWith("kids://")
    }
}

/**
 * Deep link routes for Kids Management
 */
object KidsDeepLinkRoutes {
    const val MANAGEMENT = "kids://management"
    const val REGISTRATION = "kids://registration"
    const val SERVICES = "kids://services"
    const val SERVICES_FOR_CHILD = "kids://services?childId={childId}"
    const val CHECK_IN = "kids://checkin?childId={childId}"
    const val CHECK_OUT = "kids://checkout?childId={childId}"
    const val EDIT_CHILD = "kids://edit?childId={childId}"
    const val REPORTS = "kids://reports"
}