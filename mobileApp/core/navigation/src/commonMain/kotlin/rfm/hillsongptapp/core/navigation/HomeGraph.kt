package rfm.hillsongptapp.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.serialization.Serializable


fun NavGraphBuilder.homeGraph(
    stream: @Composable AnimatedContentScope.() -> Unit,
    settings: @Composable AnimatedContentScope.() -> Unit,
    profile: @Composable AnimatedContentScope.() -> Unit,
    ministries: @Composable AnimatedContentScope.() -> Unit,
    kids: @Composable AnimatedContentScope.() -> Unit,
    groups: @Composable AnimatedContentScope.() -> Unit,
    giving: @Composable AnimatedContentScope.() -> Unit,
    feed: @Composable AnimatedContentScope.() -> Unit,
    events: @Composable AnimatedContentScope.() -> Unit,
    homeScreen: @Composable AnimatedContentScope.() -> Unit,
    rootNavController: NavHostController
){
    navigation<HomeGraph>(startDestination = HomeNav.HomeScreen) {
        composable<HomeNav.HomeScreen> {
            homeScreen()
        }
        composable<HomeNav.StreamScreen> {
            stream()
        }
        composable<HomeNav.SettingsScreen> {
            settings()
        }
        composable<HomeNav.ProfileScreen> {
            profile()
        }
        composable<HomeNav.MinistriesScreen> {
            ministries()
        }
        composable<HomeNav.KidsScreen> {
            kids()
        }
        composable<HomeNav.GroupsScreen> {
            groups()
        }
        composable<HomeNav.GivingScreen> {
            giving()
        }
        composable<HomeNav.FeedScreen> {
            feed()
        }
        composable<HomeNav.EventsScreen> {
            events()
        }
        // Add other composable destinations here
    }
}


@Serializable
object HomeGraph

sealed class HomeNav {
    @Serializable
    object HomeScreen : HomeNav()
    @Serializable
    object StreamScreen : HomeNav()
    @Serializable
    object SettingsScreen : HomeNav()
    @Serializable
    object ProfileScreen : HomeNav()
    @Serializable
    object MinistriesScreen : HomeNav()
    @Serializable
    object KidsScreen : HomeNav()
    @Serializable
    object GroupsScreen : HomeNav()
    @Serializable
    object GivingScreen : HomeNav()
    @Serializable
    object FeedScreen : HomeNav()
    @Serializable
    object EventsScreen : HomeNav()
}

// Kids Management Navigation Routes
sealed class KidsNav {
    @Serializable
    object Management : KidsNav()
    
    @Serializable
    object Registration : KidsNav()
    
    @Serializable
    object Services : KidsNav()
    
    @Serializable
    data class ServicesForChild(val childId: String) : KidsNav()
    
    @Serializable
    data class CheckIn(val childId: String) : KidsNav()
    
    @Serializable
    data class CheckOut(val childId: String) : KidsNav()
    
    @Serializable
    data class EditChild(val childId: String) : KidsNav()
    
    @Serializable
    object Reports : KidsNav()
    
    // Staff Navigation Routes
    @Serializable
    object StaffDashboard : KidsNav()
    
    @Serializable
    object QRCodeScanner : KidsNav()
    
    @Serializable
    data class CheckInVerification(val token: String) : KidsNav()
    
    // Parent QR Check-in Route
    @Serializable
    data class QRCodeDisplay(val childId: Long, val serviceId: Long) : KidsNav()
}