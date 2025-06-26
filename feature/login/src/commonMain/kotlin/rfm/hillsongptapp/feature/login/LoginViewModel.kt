package rfm.hillsongptapp.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.logging.LoggerHelper

class LoginViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    init {
        LoggerHelper.setTag("Login")
    }

    private val _uiState = MutableStateFlow(this.defaultEmptyState())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    private fun defaultEmptyState() = LoginUiState.empty()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.LoginButtonClicked -> {
                doLogin(event.username, event.password)
            }
            is LoginUiEvent.SignupButtonClicked -> {
                doSignup(
                    email = event.email,
                    password = event.password,
                    confirmPassword = event.confirmPassword,
                    firstName = event.firstName,
                    lastName = event.lastName
                )
            }
            is LoginUiEvent.ErrorDismissed -> {
                _uiState.value = _uiState.value.copy()
            }
            LoginUiEvent.ToggleSignupMode -> {
                _uiState.value = _uiState.value.copy(isSignupMode = !_uiState.value.isSignupMode,)
            }
            is LoginUiEvent.GoogleLoginClicked -> {
                // Handle Google login click
                _uiState.value = _uiState.value.copy(isGoogleLoginInProgress = true)
            }
            is LoginUiEvent.GoogleLoginResult -> {
                if (event.googleAccount != null) {
                    LoggerHelper.logDebug("Google login successful for user: ${event.googleAccount.displayName}", "GoogleLoginFlow")
                    doGoogleLogin(event.googleAccount)
                    _uiState.value = _uiState.value.copy(
                        isGoogleLoginInProgress = false,
                    )
                } else {
                    LoggerHelper.logDebug("Google login failed: No account information received", "GoogleLoginFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Google login failed: No account information received",
                        isGoogleLoginInProgress = false
                    )
                }
            }
        }
    }

    private fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            userRepository.login(username, password).let { response ->
                if (response.success) {
                    LoggerHelper.logDebug("Login successful for user: $username", "LoginFlow")
                    val token = response.data?.token
                    if (token == null) {
                        LoggerHelper.logDebug("Login response did not contain token", "LoginFlow")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Login failed: No token received",
                        )
                        return@let
                    }
                    saveUser(username, password, token )
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                    )
                } else {
                    LoggerHelper.logDebug("Login failed: ${response.message}", "LoginFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message,
                    )
                }
            }
        }
    }
    private suspend fun saveUser(username: String, password: String, token: String) {
        LoggerHelper.logDebug("Saving user: $username", "LoginFlow")
        val user = User(
            email = username,
            password = password,
            token = token,
            expiryAt = null
        )
        userRepository.insertUser(user)
        LoggerHelper.logDebug("User saved: ${user.email}", "LoginFlow")
    }

    private fun doSignup(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            userRepository.signUp(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                firstName = firstName,
                lastName = lastName
            ).let { response ->
                if (response.success) {
                    LoggerHelper.logDebug("Signup successful for user: $email", "SignupFlow")
                    doLogin(email, password)
                } else {
                    LoggerHelper.logDebug("Signup failed: ${response.message}", "SignupFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message,
                    )
                }
            }
        }
    }

    private fun doGoogleLogin(googleAccount: GoogleAccount) {
        viewModelScope.launch {
            userRepository.googleLogin(googleAccount.token).let { response ->
                if (response.success) {
                    val token = response.data?.token
                    if (token == null) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Google login failed: No token received",
                        )
                        return@let
                    }
                    saveUser(googleAccount.displayName, "", token)
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = response.message,
                    )
                }
            }
        }
    }
}

