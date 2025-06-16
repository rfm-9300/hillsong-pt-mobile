package rfm.hillsongptapp.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable

fun NavGraphBuilder.loginGraph(
    content: @Composable AnimatedContentScope.() -> Unit,
    rootNavController: NavHostController
) {
    navigation<LoginGraph>(startDestination = LoginNav.LoginScreen) {
        composable<LoginNav.LoginScreen> {
            content()
        }
    }
    composable<LoginScreen> {
        content()
    }
}

@Serializable
object LoginGraph

@Serializable
object LoginScreen

sealed class LoginNav {
    @Serializable
    object LoginScreen : LoginNav()
} 