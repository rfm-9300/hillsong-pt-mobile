package rfm.hillsongptapp

import androidx.compose.ui.window.ComposeUIViewController
import rfm.hillsongptapp.di.initKoin


fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin {
            // iOS uses production API by default
            properties(
                mapOf(
                    "API_BASE_URL" to "https://activehive.pt:443"
                )
            )
        }
    }
) { RootApp() }