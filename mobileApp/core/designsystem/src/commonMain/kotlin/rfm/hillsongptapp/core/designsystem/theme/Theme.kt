package rfm.hillsongptapp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    dynamicColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        dynamicColorScheme ?: if (isSystemInDarkTheme()) {
            hillsongPtAppDarkColorScheme
        } else {
            hillsongPtAppLightColorScheme
        }

    MaterialTheme(
        content = content,
        colorScheme = colorScheme,
    )
}
