package rfm.hillsongptapp.feature.kids

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import rfm.hillsongptapp.core.navigation.KidsNav
import rfm.hillsongptapp.feature.kids.ui.KidsManagementScreen

@Composable
fun KidsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Since kids navigation is already integrated in the main app navigation,
    // this screen should just show the kids management screen directly
    // and use the main navController for navigation
    KidsManagementScreen(
        modifier = modifier,
        onNavigateToRegistration = {
            navController.navigate(KidsNav.Registration)
        },
        onNavigateToServices = {
            navController.navigate(KidsNav.Services)
        },
        onNavigateToReports = {
            navController.navigate(KidsNav.Reports)
        },
        onNavigateToServicesForChild = { childId ->
            navController.navigate(KidsNav.ServicesForChild(childId))
        },
        onNavigateToCheckIn = { childId ->
            navController.navigate(KidsNav.CheckIn(childId))
        },
        onNavigateToCheckOut = { childId ->
            navController.navigate(KidsNav.CheckOut(childId))
        },
        onNavigateToChildEdit = { childId ->
            navController.navigate(KidsNav.EditChild(childId))
        },
        onNavigateToQRCheckIn = { childId, serviceId ->
            navController.navigate(KidsNav.QRCodeDisplay(childId, serviceId))
        },
        onNavigateToStaffDashboard = {
            navController.navigate(KidsNav.StaffDashboard)
        }
    )
}

 