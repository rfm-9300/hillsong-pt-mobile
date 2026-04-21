package rfm.hillsongptapp.feature.profile

import AppFonts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import rfm.hillsongptapp.core.data.auth.AuthState
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.GoldCtaButton
import rfm.hillsongptapp.core.designsystem.ui.components.GoldPillTag
import rfm.hillsongptapp.core.designsystem.ui.components.OutlineGoldButton
import rfm.hillsongptapp.core.navigation.navigateToLogin
import rfm.hillsongptapp.core.navigation.navigateToSettings

@Composable
fun ProfileScreen(
    navController: NavHostController,
    authRepository: AuthRepository = koinInject(),
) {
    val authState by authRepository.getAuthStateFlow().collectAsState(initial = AuthState.Loading)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (authState) {
                is AuthState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HillsongColors.Gold)
                    }
                }
                is AuthState.Unauthenticated -> {
                    UnauthenticatedContent(
                        onBackClick = { navController.popBackStack() },
                        onLoginClick = { navController.navigateToLogin() },
                        onSettingsClick = { navController.navigateToSettings() },
                    )
                }
                is AuthState.Authenticated -> {
                    val user = (authState as AuthState.Authenticated).user
                    AuthenticatedContent(
                        userEmail = user.email,
                        onBackClick = { navController.popBackStack() },
                        onSettingsClick = { navController.navigateToSettings() },
                        onLogoutClick = {
                            coroutineScope.launch {
                                authRepository.logout()
                                navController.navigateToLogin()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedContent(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileTopBar(title = "Profile", onBackClick = onBackClick)

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(1.5.dp, HillsongColors.Gold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "H",
                        style = TextStyle(
                            fontFamily = AppFonts.mogra(),
                            fontSize = 30.sp,
                            color = HillsongColors.Gold,
                        ),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Welcome",
                    style = TextStyle(
                        fontFamily = AppFonts.mogra(),
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp,
                    ),
                )
                Text(
                    text = "Sign in to access your profile and connect with the community.",
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontSize = 13.sp,
                        color = HillsongColors.Gray500,
                    ),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                GoldCtaButton(text = "Sign In", onClick = onLoginClick)
            }

            // Settings shortcut pinned to bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onSettingsClick)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = HillsongColors.Gray500,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Settings",
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontSize = 13.sp,
                        color = HillsongColors.Gray500,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedContent(
    userEmail: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Gradient header section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            Column {
                ProfileTopBar(title = "Profile", onBackClick = onBackClick)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 24.dp),
                ) {
                    // Avatar circle with gold ring
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .border(2.dp, HillsongColors.Gold, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = userEmail.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style = TextStyle(
                                fontFamily = AppFonts.mogra(),
                                fontSize = 40.sp,
                                color = HillsongColors.Gold,
                            ),
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = userEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        style = TextStyle(
                            fontFamily = AppFonts.mogra(),
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.3).sp,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        style = TextStyle(
                            fontFamily = AppFonts.andika(),
                            fontSize = 13.sp,
                            color = HillsongColors.Gray500,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    GoldPillTag(text = "Member · Lisboa", showDot = true)

                    // Stats row
                    Spacer(Modifier.height(28.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ProfileStat("42", "Services")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        ProfileStat("3", "Groups")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        ProfileStat("2", "Kids")
                    }
                }
            }
        }

        // Menu list
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            ProfileMenuItem(icon = Icons.Default.Person, label = "My Kids", onClick = {})
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem(icon = Icons.Default.Person, label = "My Groups", onClick = {})
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem(icon = Icons.Default.Favorite, label = "Giving History", onClick = {})
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem(icon = Icons.Default.Notifications, label = "Notifications", onClick = {})
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Language", trailing = "Português", onClick = {})
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            OutlineGoldButton(text = "Settings", onClick = onSettingsClick)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onLogoutClick)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = HillsongColors.Error, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Log Out",
                    style = TextStyle(
                        fontFamily = AppFonts.andika(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HillsongColors.Error,
                    ),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Hillsong PT · v1.0.0",
                style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 11.sp, color = HillsongColors.Gray500, letterSpacing = 0.5.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ProfileTopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = title.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground,
            ),
        )
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(
                fontFamily = AppFonts.mogra(),
                fontSize = 26.sp,
                color = HillsongColors.Gold,
                lineHeight = 28.sp,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 10.sp,
                color = HillsongColors.Gray500,
                letterSpacing = 1.2.sp,
            ),
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = HillsongColors.Gold, modifier = Modifier.size(20.dp))
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
                style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = HillsongColors.Gray500),
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = HillsongColors.Gray500, modifier = Modifier.size(14.dp))
    }
}
