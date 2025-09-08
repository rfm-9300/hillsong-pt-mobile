package rfm.hillsongptapp.feature.kids

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import rfm.hillsongptapp.feature.kids.navigation.KidsNavigation

@Composable
fun KidsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    KidsNavigation(
        modifier = modifier,
        navController = navController
    )
} 