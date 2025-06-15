package rfm.hillsongptapp.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import rfm.hillsongptapp.feature.login.LoginScreen

fun NavGraphBuilder.loginGraph(
    rootNavController: NavHostController
) {
    navigation<LoginGraph>(startDestination = LoginNav.LoginScreen) {
        composable<LoginNav.LoginScreen> {
            LoginScreen(
                navigator = rootNavController
            )
        }
        // Add other composable destinations here
    }
}

@Serializable
object LoginGraph

sealed class LoginNav {
    @Serializable
    object LoginScreen : LoginNav()
} 