package rfm.hillsongptapp.core.designsystem.theme

import AppTypography
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun AppTheme(
    dynamicColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val isDark by ThemeManager.isDarkMode.collectAsState()
    val colorScheme = dynamicColorScheme ?: if (isDark) {
        hillsongPtAppDarkColorScheme
    } else {
        hillsongPtAppLightColorScheme
    }

    MaterialTheme(
        content = content,
        colorScheme = colorScheme,
        typography = AppTypography()
    )
}
