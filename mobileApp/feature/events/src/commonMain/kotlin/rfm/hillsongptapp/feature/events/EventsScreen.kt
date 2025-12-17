package rfm.hillsongptapp.feature.events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import rfm.hillsongptapp.core.designsystem.HillsongTopAppBar

@Composable
fun EventsScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            HillsongTopAppBar(
                title = "Events",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Events Screen")
        }
    }
}
