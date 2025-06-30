package rfm.hillsongptapp


import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import rfm.hillsongptapp.core.navigation.LoginGraph
import rfm.hillsongptapp.core.navigation.homeGraph
import rfm.hillsongptapp.core.navigation.loginGraph
import rfm.hillsongptapp.feature.login.LoginScreen

import rfm.hillsongptapp.feature.stream.StreamScreen
import rfm.hillsongptapp.feature.settings.SettingsScreen
import rfm.hillsongptapp.feature.profile.ProfileScreen
import rfm.hillsongptapp.feature.ministries.MinistriesScreen
import rfm.hillsongptapp.feature.kids.KidsScreen
import rfm.hillsongptapp.feature.groups.GroupsScreen
import rfm.hillsongptapp.feature.giving.GivingScreen
import rfm.hillsongptapp.feature.feed.FeedScreen
import rfm.hillsongptapp.feature.events.EventsScreen
import rfm.hillsongptapp.feature.home.ui.screens.homeScreen

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
        homeGraph(
            stream = { StreamScreen() },
            settings = { SettingsScreen() },
            profile = { ProfileScreen() },
            ministries = { MinistriesScreen() },
            kids = { KidsScreen() },
            groups = { GroupsScreen() },
            giving = { GivingScreen() },
            feed = { FeedScreen() },
            events = { EventsScreen() },
            homeScreen = { homeScreen(navController = rootNavController) },
            rootNavController = rootNavController
        )
    }
}