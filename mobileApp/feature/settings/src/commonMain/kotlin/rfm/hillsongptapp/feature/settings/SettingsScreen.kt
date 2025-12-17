package rfm.hillsongptapp.feature.settings

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
fun SettingsScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            HillsongTopAppBar(
                title = "Settings",
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
            Text("Settings Screen")
        }
    }
}
