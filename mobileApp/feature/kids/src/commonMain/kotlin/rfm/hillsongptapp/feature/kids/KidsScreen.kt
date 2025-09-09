package rfm.hillsongptapp.feature.kids

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun KidsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Since kids navigation is already integrated in the main app navigation,
    // this screen should just show the kids management screen directly
    // and use the main navController for navigation
    rfm.hillsongptapp.feature.kids.ui.KidsManagementScreen(
        modifier = modifier,
        onNavigateToRegistration = {
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.Registration)
        },
        onNavigateToServices = {
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.Services)
        },
        onNavigateToReports = {
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.Reports)
        },
        onNavigateToServicesForChild = { childId ->
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.ServicesForChild(childId))
        },
        onNavigateToCheckIn = { childId ->
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.CheckIn(childId))
        },
        onNavigateToCheckOut = { childId ->
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.CheckOut(childId))
        },
        onNavigateToChildEdit = { childId ->
            navController.navigate(rfm.hillsongptapp.core.navigation.KidsNav.EditChild(childId))
        }
    )
} 