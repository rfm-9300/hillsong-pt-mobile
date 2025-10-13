package rfm.hillsongptapp.feature.kids

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import rfm.hillsongptapp.feature.kids.ui.KidsManagementScreen

@Composable
fun KidsScreen(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController()
) {
    // Since kids navigation is already integrated in the main app navigation,
    // this screen should just show the kids management screen directly
    // and use the main navController for navigation

    KidsManagementScreen(modifier = modifier, navController = navController)
}
