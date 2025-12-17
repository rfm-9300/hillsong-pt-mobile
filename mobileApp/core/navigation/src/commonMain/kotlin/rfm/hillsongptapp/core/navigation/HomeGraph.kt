package rfm.hillsongptapp.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
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
    calendar: @Composable AnimatedContentScope.() -> Unit,
    homeScreen: @Composable AnimatedContentScope.() -> Unit,
    youtubeVideoScreen: @Composable AnimatedContentScope.(videoId: Long, videoUrl: String) -> Unit,
    youtubePlayerFullScreen: @Composable AnimatedContentScope.(videoId: String) -> Unit,
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
        composable<HomeNav.ProfileScreen>(
            enterTransition = {
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { fullHeight -> fullHeight }
                )
            },
            exitTransition = {
                slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { fullHeight -> fullHeight }
                )
            },
            popEnterTransition = {
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { fullHeight -> fullHeight }
                )
            },
            popExitTransition = {
                slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { fullHeight -> fullHeight }
                )
            }
        ) {
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
        composable<HomeNav.CalendarScreen> {
            calendar()
        }
        composable<HomeNav.YouTubeVideoScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeNav.YouTubeVideoScreen>()
            youtubeVideoScreen(args.videoId, args.videoUrl)
        }
        composable<HomeNav.YouTubePlayerFullScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<HomeNav.YouTubePlayerFullScreen>()
            youtubePlayerFullScreen(args.videoId)
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
    @Serializable
    object CalendarScreen : HomeNav()
    @Serializable
    data class YouTubeVideoScreen(val videoId: Long, val videoUrl: String) : HomeNav()
    @Serializable
    data class YouTubePlayerFullScreen(val videoId: String) : HomeNav()
}

