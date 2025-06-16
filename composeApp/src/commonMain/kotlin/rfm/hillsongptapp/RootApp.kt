package rfm.hillsongptapp


import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import rfm.hillsongptapp.core.navigation.LoginGraph
import rfm.hillsongptapp.core.navigation.homeGraph
import rfm.hillsongptapp.core.navigation.loginGraph
import rfm.hillsongptapp.feature.login.LoginScreen

@Composable
@Preview
fun RootApp() {
    RootNavigation()
}

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()
    NavHost(
        navController = rootNavController,
        startDestination = LoginGraph
    ) {
        loginGraph(content = { LoginScreen(navigator = rootNavController) }, rootNavController = rootNavController)
        homeGraph(rootNavController)
    }
}