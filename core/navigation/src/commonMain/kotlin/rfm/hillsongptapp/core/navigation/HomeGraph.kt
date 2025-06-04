package rfm.hillsongptapp.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import rfm.hillsongptapp.feature.home.ui.screens.homeScreen

fun NavGraphBuilder.homeGraph(
    rootNavController: NavHostController
){
    navigation<HomeGraph>(startDestination = HomeNav.HomeScreen) {
        composable<HomeNav.HomeScreen> {
            homeScreen()
        }
        // Add other composable destinations here
    }
}


@Serializable
object HomeGraph

sealed class HomeNav {
    @Serializable
    object HomeScreen : HomeNav()
}