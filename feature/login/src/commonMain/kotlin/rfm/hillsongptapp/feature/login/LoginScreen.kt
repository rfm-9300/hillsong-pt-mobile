package rfm.hillsongptapp.feature.login

import androidx.compose.foundation.layout.*
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

    LoginScreenMain(
        uiState = uiState,
        username = username,
        setUsername = setUsername,
        password = password,
        setPassword = setPassword,
        onEvent = viewModel::onEvent
    )
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
    Scaffold(

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = setUsername,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {  },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
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