package rfm.hillsongptapp.feature.kids.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import rfm.hillsongptapp.core.navigation.KidsNav
import rfm.hillsongptapp.core.navigation.navigateToKidsCheckIn
import rfm.hillsongptapp.core.navigation.navigateToKidsCheckOut
import rfm.hillsongptapp.core.navigation.navigateToKidsEditChild
import rfm.hillsongptapp.core.navigation.navigateToKidsManagement
import rfm.hillsongptapp.core.navigation.navigateToKidsRegistration
import rfm.hillsongptapp.core.navigation.navigateToKidsReports
import rfm.hillsongptapp.core.navigation.navigateToKidsServices
import rfm.hillsongptapp.core.navigation.navigateToKidsServicesForChild
import rfm.hillsongptapp.core.navigation.navigateToStaffDashboard
import rfm.hillsongptapp.core.navigation.navigateToQRCodeScanner
import rfm.hillsongptapp.core.navigation.navigateToCheckInVerification
import rfm.hillsongptapp.feature.kids.ui.KidsManagementScreen
import rfm.hillsongptapp.feature.kids.ui.checkin.CheckInScreen
import rfm.hillsongptapp.feature.kids.ui.checkout.CheckOutScreen
import rfm.hillsongptapp.feature.kids.ui.edit.ChildEditScreen
import rfm.hillsongptapp.feature.kids.ui.registration.ChildRegistrationScreen
import rfm.hillsongptapp.feature.kids.ui.reports.ReportsScreen
import rfm.hillsongptapp.feature.kids.ui.services.ServicesScreen
import rfm.hillsongptapp.feature.kids.ui.staff.StaffDashboardScreen
import rfm.hillsongptapp.feature.kids.ui.staff.QRCodeScannerScreen
import rfm.hillsongptapp.feature.kids.ui.staff.CheckInVerificationScreen

/**
 * Main navigation composable for Kids feature
 * Uses Compose Navigation for proper navigation handling
 */
@Composable
fun KidsNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = KidsNav.Management,
        modifier = modifier
    ) {
        kidsNavigationGraph(navController)
    }
}

/**
 * Navigation graph for Kids feature screens
 */
fun NavGraphBuilder.kidsNavigationGraph(
    navController: NavHostController
) {
    composable<KidsNav.Management> {
        KidsManagementScreen(
            onNavigateToRegistration = {
                navController.navigateToKidsRegistration()
            },
            onNavigateToServices = {
                navController.navigateToKidsServices()
            },
            onNavigateToReports = {
                navController.navigateToKidsReports()
            },
            onNavigateToServicesForChild = { childId ->
                navController.navigateToKidsServicesForChild(childId)
            },
            onNavigateToCheckIn = { childId ->
                navController.navigateToKidsCheckIn(childId)
            },
            onNavigateToCheckOut = { childId ->
                navController.navigateToKidsCheckOut(childId)
            },
            onNavigateToChildEdit = { childId ->
                navController.navigateToKidsEditChild(childId)
            },
            onNavigateToStaffDashboard = {
                navController.navigateToStaffDashboard()
            }
        )
    }
    
    composable<KidsNav.Registration> {
        ChildRegistrationScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onRegistrationSuccess = {
                navController.navigateToKidsManagement()
            }
        )
    }
    
    composable<KidsNav.Services> {
        ServicesScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
    
    composable<KidsNav.ServicesForChild> { backStackEntry ->
        val route = backStackEntry.toRoute<KidsNav.ServicesForChild>()
        ServicesScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            selectedChildId = route.childId
        )
    }
    
    composable<KidsNav.CheckIn> { backStackEntry ->
        val route = backStackEntry.toRoute<KidsNav.CheckIn>()
        CheckInScreen(
            childId = route.childId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onCheckInSuccess = {
                navController.navigateToKidsManagement()
            }
        )
    }
    
    composable<KidsNav.CheckOut> { backStackEntry ->
        val route = backStackEntry.toRoute<KidsNav.CheckOut>()
        CheckOutScreen(
            childId = route.childId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
    
    composable<KidsNav.EditChild> { backStackEntry ->
        val route = backStackEntry.toRoute<KidsNav.EditChild>()
        ChildEditScreen(
            childId = route.childId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onUpdateSuccess = {
                navController.navigateToKidsManagement()
            }
        )
    }
    
    composable<KidsNav.Reports> {
        ReportsScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
    
    composable<KidsNav.StaffDashboard> {
        StaffDashboardScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToScanner = {
                navController.navigateToQRCodeScanner()
            }
        )
    }
    
    composable<KidsNav.QRCodeScanner> {
        QRCodeScannerScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onQRCodeScanned = { token ->
                navController.navigateToCheckInVerification(token)
            }
        )
    }
    
    composable<KidsNav.CheckInVerification> { backStackEntry ->
        val route = backStackEntry.toRoute<KidsNav.CheckInVerification>()
        CheckInVerificationScreen(
            token = route.token,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}

/**
 * Breadcrumb navigation state for complex workflows
 */
data class KidsBreadcrumb(
    val title: String,
    val route: KidsNav,
    val isClickable: Boolean = true
)

/**
 * Generate breadcrumbs for the current navigation state
 */
fun generateKidsBreadcrumbs(currentRoute: KidsNav): List<KidsBreadcrumb> {
    return when (currentRoute) {
        is KidsNav.Management -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management, false)
        )
        
        is KidsNav.Registration -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Register Child", KidsNav.Registration, false)
        )
        
        is KidsNav.Services -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Services", KidsNav.Services, false)
        )
        
        is KidsNav.ServicesForChild -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Services", KidsNav.Services),
            KidsBreadcrumb("Child Services", currentRoute, false)
        )
        
        is KidsNav.CheckIn -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Check In", currentRoute, false)
        )
        
        is KidsNav.CheckOut -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Check Out", currentRoute, false)
        )
        
        is KidsNav.EditChild -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Edit Child", currentRoute, false)
        )
        
        is KidsNav.Reports -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Reports", KidsNav.Reports, false)
        )
        
        is KidsNav.StaffDashboard -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Staff Dashboard", KidsNav.StaffDashboard, false)
        )
        
        is KidsNav.QRCodeScanner -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Staff Dashboard", KidsNav.StaffDashboard),
            KidsBreadcrumb("QR Scanner", KidsNav.QRCodeScanner, false)
        )
        
        is KidsNav.CheckInVerification -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("Staff Dashboard", KidsNav.StaffDashboard),
            KidsBreadcrumb("Verification", currentRoute, false)
        )
        
        is KidsNav.QRCodeDisplay -> listOf(
            KidsBreadcrumb("Kids Management", KidsNav.Management),
            KidsBreadcrumb("QR Check-In", currentRoute, false)
        )
    }
}