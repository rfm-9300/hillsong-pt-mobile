package rfm.hillsongptapp

import androidx.compose.ui.window.ComposeUIViewController
import rfm.hillsongptapp.di.initKoin


fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { RootApp() }