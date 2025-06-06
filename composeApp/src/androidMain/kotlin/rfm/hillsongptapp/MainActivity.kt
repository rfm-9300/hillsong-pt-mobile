package rfm.hillsongptapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import rfm.hillsongptapp.core.designsystem.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicColorApp {
                enableEdgeToEdge()
                RootApp()
            }
        }
    }
}

@Composable
fun DynamicColorApp(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val hasDynamicColorsFeature = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)

    val dynamicColorScheme =
        if (hasDynamicColorsFeature) {
            if (isSystemInDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            null
        }

    AppTheme(
        dynamicColorScheme = dynamicColorScheme,
        content = content,
    )
}

@Preview
@Composable
fun AppAndroidPreview() {
    RootApp()
}