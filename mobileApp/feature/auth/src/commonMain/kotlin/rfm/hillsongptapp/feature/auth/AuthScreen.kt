package rfm.hillsongptapp.feature.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.theme.AppTheme
import rfm.hillsongptapp.core.designsystem.theme.HillsongColors
import rfm.hillsongptapp.core.designsystem.ui.components.AppSnackbarHost
import rfm.hillsongptapp.core.designsystem.ui.components.EditorialTextField
import rfm.hillsongptapp.core.designsystem.ui.components.GoldCtaButton
import rfm.hillsongptapp.core.navigation.navigateToHome

private fun isDebugBuild(): Boolean = true

@Composable
fun LoginScreen(viewModel: AuthViewModel = koinViewModel(), navigator: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthorized) {
        if (uiState.isAuthorized) {
            navigator.navigateToHome()
        }
    }

    val (username, setUsername) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "rodrigomartins@msn.com" else "")
    }
    val (password, setPassword) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "!Feller158" else "")
    }
    val (email, setEmail) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "rodrigomartins@msn.com" else "")
    }

    if (uiState.isSignupMode) {
        SignupScreen(
            uiState = uiState,
            username = username,
            setUsername = setUsername,
            password = password,
            setPassword = setPassword,
            email = email,
            setEmail = setEmail,
            onEvent = viewModel::onEvent,
            onSkip = { navigator.navigateToHome() },
        )
    } else {
        LoginScreenMain(
            uiState = uiState,
            username = username,
            setUsername = setUsername,
            password = password,
            setPassword = setPassword,
            onEvent = viewModel::onEvent,
            onSkip = { navigator.navigateToHome() },
        )
    }
}

// ────────────────────────── LOGIN ──────────────────────────

@Composable
fun LoginScreenMain(
    uiState: LoginUiState,
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit,
    onSkip: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = HillsongColors.Black.copy(alpha = 0f),
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            // Atmospheric gradient backdrop (radial fade from top)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HillsongColors.Gold.copy(alpha = 0.08f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.background,
                            ),
                        ),
                    ),
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HillsongColors.Gold)
                }
            } else {
                LoginScreenContent(
                    username = username,
                    setUsername = setUsername,
                    password = password,
                    setPassword = setPassword,
                    onEvent = onEvent,
                    onSkip = onSkip,
                )
            }

            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let { message ->
                    snackbarHostState.showSnackbar(message = message, actionLabel = "Dismiss")
                    onEvent(LoginUiEvent.ErrorDismissed)
                }
            }
        }
    }
}

@Composable
fun LoginScreenContent(
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit,
    onSkip: () -> Unit = {},
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val sub = HillsongColors.Gray500

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(80.dp))

        // Circular H emblem
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(1.5.dp, HillsongColors.Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "H",
                style = TextStyle(
                    fontFamily = AppFonts.mogra(),
                    fontSize = 24.sp,
                    color = HillsongColors.Gold,
                ),
            )
        }
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Hillsong",
            style = TextStyle(
                fontFamily = AppFonts.mogra(),
                fontSize = 38.sp,
                color = onBg,
                letterSpacing = (-0.5).sp,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "PORTUGAL",
            style = TextStyle(
                fontFamily = AppFonts.anta(),
                fontSize = 13.sp,
                letterSpacing = 6.sp,
                color = HillsongColors.Gold,
            ),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Welcome back. Sign in to continue.",
            style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = sub),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(44.dp))

        EditorialTextField(
            value = username,
            onValueChange = setUsername,
            label = "Email",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(22.dp))
        EditorialTextField(
            value = password,
            onValueChange = setPassword,
            label = "Password",
            isPassword = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Forgot password?",
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HillsongColors.Gold,
            ),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { /* TODO: forgot password */ },
        )

        Spacer(Modifier.height(24.dp))

        GoldCtaButton(
            text = "Sign In",
            onClick = { onEvent(LoginUiEvent.LoginButtonClicked(username, password)) },
        )

        Spacer(Modifier.height(28.dp))

        // Divider with OR
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(onBg.copy(alpha = 0.08f)),
            )
            Text(
                text = "OR",
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    color = sub,
                ),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .weight(1f)
                    .background(onBg.copy(alpha = 0.08f)),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Google button (styled outline)
        GoogleSignInButton(
            onGoogleSignInResult = { googleUser -> onEvent(LoginUiEvent.GoogleLoginResult(googleUser)) },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f, fill = true))
        Spacer(Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Don't have an account? ",
                style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = sub),
            )
            Text(
                text = "Sign up",
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = HillsongColors.Gold,
                ),
                modifier = Modifier.clickable { onEvent(LoginUiEvent.ToggleSignupMode) },
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Skip for now",
            style = TextStyle(
                fontFamily = AppFonts.andika(),
                fontSize = 12.sp,
                color = sub,
                textDecoration = TextDecoration.Underline,
            ),
            modifier = Modifier.clickable { onSkip() },
        )
        Spacer(Modifier.height(40.dp))
    }
}

// ────────────────────────── SIGN UP ──────────────────────────

@Composable
fun SignupScreen(
    uiState: LoginUiState,
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    email: String,
    setEmail: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit,
    onSkip: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val (confirmPassword, setConfirmPassword) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "feller123" else "")
    }
    val (firstName, setFirstName) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "Rodrigo" else "")
    }
    val (lastName, setLastName) = rememberSaveable {
        mutableStateOf(if (isDebugBuild()) "Martins" else "")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = HillsongColors.Gold) }
        } else {
            SignupScreenContent(
                username = username,
                setUsername = setUsername,
                password = password,
                setPassword = setPassword,
                email = email,
                setEmail = setEmail,
                confirmPassword = confirmPassword,
                setConfirmPassword = setConfirmPassword,
                firstName = firstName,
                setFirstName = setFirstName,
                lastName = lastName,
                setLastName = setLastName,
                onEvent = onEvent,
                onSkip = onSkip,
                modifier = Modifier.padding(paddingValues),
            )
        }

        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                snackbarHostState.showSnackbar(message = message, actionLabel = "Dismiss")
                onEvent(LoginUiEvent.ErrorDismissed)
            }
        }
    }
}

@Composable
fun SignupScreenContent(
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    email: String,
    setEmail: (String) -> Unit,
    confirmPassword: String,
    setConfirmPassword: (String) -> Unit,
    firstName: String,
    setFirstName: (String) -> Unit,
    lastName: String,
    setLastName: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val sub = HillsongColors.Gray500

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSkip) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = onBg,
                )
            }
            Text(
                text = "BACK",
                style = TextStyle(
                    fontFamily = AppFonts.anta(),
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    color = sub,
                ),
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Create\naccount",
            style = TextStyle(
                fontFamily = AppFonts.mogra(),
                fontSize = 34.sp,
                color = onBg,
                letterSpacing = (-0.5).sp,
                lineHeight = 36.sp,
            ),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Join the Hillsong PT community.",
            style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = sub),
        )

        Spacer(Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditorialTextField(
                value = firstName,
                onValueChange = setFirstName,
                label = "First name",
                modifier = Modifier.weight(1f),
            )
            EditorialTextField(
                value = lastName,
                onValueChange = setLastName,
                label = "Last name",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(22.dp))
        EditorialTextField(value = email, onValueChange = setEmail, label = "Email", modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(22.dp))

        val passwordError = password.isNotEmpty() && password.length < 8
        EditorialTextField(
            value = password,
            onValueChange = setPassword,
            label = "Password",
            isPassword = true,
            isError = passwordError,
            errorText = if (passwordError) "At least 8 characters with one number" else null,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(22.dp))
        EditorialTextField(
            value = confirmPassword,
            onValueChange = setConfirmPassword,
            label = "Confirm password",
            isPassword = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        GoldCtaButton(
            text = "Create account",
            onClick = {
                onEvent(
                    LoginUiEvent.SignupButtonClicked(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        firstName = firstName,
                        lastName = lastName,
                    ),
                )
            },
        )

        Spacer(Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().clickable { onEvent(LoginUiEvent.ToggleSignupMode) },
        ) {
            Text(
                text = "Already have an account? ",
                style = TextStyle(fontFamily = AppFonts.andika(), fontSize = 13.sp, color = sub),
            )
            Text(
                text = "Sign in",
                style = TextStyle(
                    fontFamily = AppFonts.andika(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = HillsongColors.Gold,
                ),
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginScreenMain(
            uiState = LoginUiState.empty(),
            username = "testuser",
            setUsername = {},
            password = "password",
            setPassword = {},
            onEvent = {},
        )
    }
}

@Preview
@Composable
fun SignupScreenPreview() {
    AppTheme {
        SignupScreen(
            uiState = LoginUiState.empty(),
            username = "testuser",
            setUsername = {},
            password = "password",
            setPassword = {},
            email = "test@example.com",
            setEmail = {},
            onEvent = {},
        )
    }
}
