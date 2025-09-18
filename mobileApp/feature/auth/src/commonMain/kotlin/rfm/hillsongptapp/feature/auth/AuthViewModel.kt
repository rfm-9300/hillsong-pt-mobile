package rfm.hillsongptapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.AuthResult
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.logging.LoggerHelper

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    init {
        LoggerHelper.setTag("Login")
        viewModelScope.launch {
            val user = authRepository.getUserById(1)
            if (user != null) {
                LoggerHelper.logDebug("User already exists: ${user.email}", "LoginFlow")
                _uiState.value = _uiState.value.copy(isAuthorized = true)
            } else {
                LoggerHelper.logDebug("No user found, initializing empty state", "LoginFlow")
                _uiState.value = defaultEmptyState()
            }
        }
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = authRepository.login(username, password)) {
                is AuthResult.Success -> {
                    val response = result.data
                    LoggerHelper.logDebug("Login successful for user: $username, message: ${response.message}", "LoginFlow")
                    val token = response.data?.token
                    if (token == null) {
                        LoggerHelper.logDebug("Login response did not contain token", "LoginFlow")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Login failed: No token received",
                            isLoading = false
                        )
                        return@launch
                    }
                    saveUser(username, password, token)
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                        isLoading = false
                    )
                }
                is AuthResult.Error -> {
                    LoggerHelper.logDebug("Login failed: ${result.message}", "LoginFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is AuthResult.NetworkError -> {
                    LoggerHelper.logDebug("Login network error: ${result.message}", "LoginFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Network error: ${result.message}",
                        isLoading = false
                    )
                }
                is AuthResult.Loading -> {
                    // Loading state is already set above
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
        authRepository.insertUser(user)
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = authRepository.signUp(email, password, confirmPassword, firstName, lastName)) {
                is AuthResult.Success -> {
                    LoggerHelper.logDebug("Signup successful for user: $email", "SignupFlow")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    doLogin(email, password)
                }
                is AuthResult.Error -> {
                    LoggerHelper.logDebug("Signup failed: ${result.message}", "SignupFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is AuthResult.NetworkError -> {
                    LoggerHelper.logDebug("Signup network error: ${result.message}", "SignupFlow")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Network error: ${result.message}",
                        isLoading = false
                    )
                }
                is AuthResult.Loading -> {
                    // Loading state is already set above
                }
            }
        }
    }

    private fun doGoogleLogin(googleAccount: GoogleAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = authRepository.googleLogin(googleAccount.token)) {
                is AuthResult.Success -> {
                    val response = result.data
                    val token = response.data?.token
                    if (token == null) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Google login failed: No token received",
                            isLoading = false
                        )
                        return@launch
                    }
                    saveUser(googleAccount.displayName, "", token)
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                        isLoading = false
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
                is AuthResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Network error: ${result.message}",
                        isLoading = false
                    )
                }
                is AuthResult.Loading -> {
                    // Loading state is already set above
                }
            }
        }
    }
}

