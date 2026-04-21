package rfm.hillsongptapp.feature.settings

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.EditorialSectionHeader

@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "SETTINGS",
                    style = TextStyle(
                        fontFamily = AppFonts.anta(),
                        fontSize = 14.sp,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            EditorialSectionHeader(
                title = "Preferences",
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(16.dp))

            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    onClick = {},
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                SettingsItem(
                    icon = Icons.Default.Settings,
                    label = "Language",
                    trailing = "Português",
                    onClick = {},
                )
            }

            Spacer(Modifier.height(28.dp))

            EditorialSectionHeader(
                title = "About",
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(16.dp))

            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Info,
                    label = "About Hillsong PT",
                    onClick = {},
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    label = "Privacy Policy",
                    onClick = {},
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 60.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    label = "Terms of Service",
                    onClick = {},
                )
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Hillsong PT · v1.0.0",
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 11.sp,
                    color = HillsongColors.Gray500,
                    letterSpacing = 0.5.sp,
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 40.dp),
            )
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(HillsongColors.Gold.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = HillsongColors.Gold, modifier = Modifier.size(16.dp))
        }
        Text(
            text = label,
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 13.sp,
                    color = HillsongColors.Gray500,
                ),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = HillsongColors.Gray500,
            modifier = Modifier.size(16.dp),
        )
    }
}
