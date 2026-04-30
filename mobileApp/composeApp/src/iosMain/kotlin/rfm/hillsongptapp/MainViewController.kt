package rfm.hillsongptapp

import androidx.compose.ui.window.ComposeUIViewController
import rfm.hillsongptapp.core.designsystem.theme.AppTheme
import rfm.hillsongptapp.di.initKoin

private const val IOS_BASE_URL = "http://172.233.96.224"

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin {
            // iOS uses server API by default
            properties(
                mapOf(
                    "API_BASE_URL" to IOS_BASE_URL,
                    "AUTH_BASE_URL" to IOS_BASE_URL
                )
            )
        }
    }
) { AppTheme {
    RootApp()
} }
