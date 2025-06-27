package rfm.hillsongptapp.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarDestination(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    object Events : BottomBarDestination(
        route = "events",
        title = "Events",
        icon = Icons.Default.DateRange
    )

    object Profile : BottomBarDestination(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}

@Composable
fun HillsongBottomAppBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarDestinations = listOf(
        BottomBarDestination.Home,
        BottomBarDestination.Events,
        BottomBarDestination.Profile
    )

    NavigationBar(modifier = modifier) {
        bottomBarDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onItemClick(destination.route) },
                icon = { Icon(imageVector = destination.icon, contentDescription = destination.title) },
                label = { Text(text = destination.title) }
            )
        }
    }
}
