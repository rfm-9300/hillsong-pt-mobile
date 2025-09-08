package rfm.hillsongptapp.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

fun NavGraphBuilder.kidsGraph(
    kidsManagement: @Composable AnimatedContentScope.() -> Unit,
    kidsRegistration: @Composable AnimatedContentScope.() -> Unit,
    kidsServices: @Composable AnimatedContentScope.() -> Unit,
    kidsServicesForChild: @Composable AnimatedContentScope.(childId: String) -> Unit,
    kidsCheckIn: @Composable AnimatedContentScope.(childId: String) -> Unit,
    kidsCheckOut: @Composable AnimatedContentScope.(childId: String) -> Unit,
    kidsEditChild: @Composable AnimatedContentScope.(childId: String) -> Unit,
    kidsReports: @Composable AnimatedContentScope.() -> Unit,
    rootNavController: NavHostController
) {
    navigation<KidsGraph>(startDestination = KidsNav.Management) {
        composable<KidsNav.Management> {
            kidsManagement()
        }
        
        composable<KidsNav.Registration> {
            kidsRegistration()
        }
        
        composable<KidsNav.Services> {
            kidsServices()
        }
        
        composable<KidsNav.ServicesForChild> { backStackEntry ->
            val route = backStackEntry.toRoute<KidsNav.ServicesForChild>()
            kidsServicesForChild(route.childId)
        }
        
        composable<KidsNav.CheckIn> { backStackEntry ->
            val route = backStackEntry.toRoute<KidsNav.CheckIn>()
            kidsCheckIn(route.childId)
        }
        
        composable<KidsNav.CheckOut> { backStackEntry ->
            val route = backStackEntry.toRoute<KidsNav.CheckOut>()
            kidsCheckOut(route.childId)
        }
        
        composable<KidsNav.EditChild> { backStackEntry ->
            val route = backStackEntry.toRoute<KidsNav.EditChild>()
            kidsEditChild(route.childId)
        }
        
        composable<KidsNav.Reports> {
            kidsReports()
        }
    }
}

@Serializable
object KidsGraph