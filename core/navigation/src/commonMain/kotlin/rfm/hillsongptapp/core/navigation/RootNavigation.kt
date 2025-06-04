package rfm.hillsongptapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavigation() {
   val rootNavController = rememberNavController()
    NavHost(
         navController = rootNavController,
         startDestination = HomeGraph
    ) {
         homeGraph(rootNavController)

    }
}