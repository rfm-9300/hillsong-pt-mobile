package rfm.hillsongptapp.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.designsystem.ui.components.AppSnackbarHost
import rfm.hillsongptapp.core.navigation.HomeGraph
import rfm.hillsongptapp.core.navigation.navigateToHome


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    navigator: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    // if the user is authorized, navigate to the home screen
    LaunchedEffect(uiState.isAuthorized) {
        if (uiState.isAuthorized) {
            navigator.navigateToHome()
        }
    }

    val (username, setUsername) = rememberSaveable { mutableStateOf("") }
    val (password, setPassword) = rememberSaveable { mutableStateOf("") }
    val (email, setEmail) = rememberSaveable { mutableStateOf("") }

    if (uiState.isSignupMode) {
        SignupScreen(
            uiState = uiState,
            username = username,
            setUsername = setUsername,
            password = password,
            setPassword = setPassword,
            email = email,
            setEmail = setEmail,
            onEvent = viewModel::onEvent
        )
    } else {
        LoginScreenMain(
            uiState = uiState,
            username = username,
            setUsername = setUsername,
            password = password,
            setPassword = setPassword,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun SignupScreen(
    uiState: LoginUiState,
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    email: String,
    setEmail: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val (confirmPassword, setConfirmPassword) = rememberSaveable { mutableStateOf("") }
    val (firstName, setFirstName) = rememberSaveable { mutableStateOf("") }
    val (lastName, setLastName) = rememberSaveable { mutableStateOf("") }

    Scaffold(
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            SignupScreenContent(
                paddingValues = paddingValues,
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
                onEvent = onEvent
            )
        }

        // Show snackbar if there is an error
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Dismiss"
                )
                // Clear the error message after showing it
                onEvent(LoginUiEvent.ErrorDismissed)
            }
        }
    }
}

@Composable
fun SignupScreenContent(
    paddingValues: PaddingValues,
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
    onEvent: (LoginUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            TextField(
                value = firstName,
                onValueChange = setFirstName,
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = lastName,
                onValueChange = setLastName,
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = email,
                onValueChange = setEmail,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = password,
                onValueChange = setPassword,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = confirmPassword,
                onValueChange = setConfirmPassword,
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    onEvent(LoginUiEvent.SignupButtonClicked(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        firstName = firstName,
                        lastName = lastName
                    )) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }
            
            TextButton(
                onClick = { onEvent(LoginUiEvent.ToggleSignupMode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login")
            }
        }
    }
}

@Composable
fun LoginScreenMain(
    uiState: LoginUiState,
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LoginScreenContent(
                paddingValues = paddingValues,
                username = username,
                setUsername = setUsername,
                password = password,
                setPassword = setPassword,
                onEvent = onEvent
            )
        }

        // Show snackbar if there is an error
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = "Dismiss"
                )
                // Clear the error message after showing it
                onEvent(LoginUiEvent.ErrorDismissed)
            }
        }
    }
}

@Composable
fun LoginScreenContent(
    paddingValues: PaddingValues,
    username: String,
    setUsername: (String) -> Unit,
    password: String,
    setPassword: (String) -> Unit,
    onEvent: (LoginUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            TextField(
                value = username,
                onValueChange = setUsername,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = password,
                onValueChange = setPassword,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onEvent(LoginUiEvent.LoginButtonClicked(username, password)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            
            TextButton(
                onClick = { onEvent(LoginUiEvent.ToggleSignupMode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreenMain(
        uiState = LoginUiState.empty(),
        username = "testuser",
        setUsername = {},
        password = "password",
        setPassword = {},
        onEvent = {}
    )
}

@Preview
@Composable
fun SignupScreenPreview() {
    SignupScreen(
        uiState = LoginUiState.empty(),
        username = "testuser",
        setUsername = {},
        password = "password",
        setPassword = {},
        email = "test@example.com",
        setEmail = {},
        onEvent = {}
    )
}