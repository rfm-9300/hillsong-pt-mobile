package rfm.hillsongptapp.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.auth.AuthState
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.data.repository.AuthResult
import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.logging.LoggerHelper

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(defaultEmptyState())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    init {
        LoggerHelper.setTag("AuthViewModel")
        initializeAuthState()
    }
    
    private fun initializeAuthState() {
        viewModelScope.launch {
            LoggerHelper.logDebug("Initializing authentication state", "AuthInit")
            
            // Initialize the auth token manager
            authRepository.initializeAuthState()
            
            // Observe auth state changes
            authRepository.getAuthStateFlow().collect { authState ->
                when (authState) {
                    is AuthState.Loading -> {
                        LoggerHelper.logDebug("Auth state: Loading", "AuthInit")
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is AuthState.Authenticated -> {
                        LoggerHelper.logDebug("Auth state: Authenticated for user: ${authState.user.email}", "AuthInit")
                        _uiState.value = _uiState.value.copy(
                            isAuthorized = true,
                            isLoading = false
                        )
                    }
                    is AuthState.Unauthenticated -> {
                        LoggerHelper.logDebug("Auth state: Unauthenticated", "AuthInit")
                        _uiState.value = _uiState.value.copy(
                            isAuthorized = false,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

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
            is LoginUiEvent.LogoutClicked -> {
                doLogout()
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
                    // Token is automatically saved by AuthRepository
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
                    // Token is automatically saved by AuthRepository
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
    
    private fun doLogout() {
        viewModelScope.launch {
            LoggerHelper.logDebug("Logging out user", "LogoutFlow")
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = authRepository.logout()) {
                is AuthResult.Success -> {
                    LoggerHelper.logDebug("Logout successful", "LogoutFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = false,
                        isLoading = false
                    )
                }
                is AuthResult.Error, is AuthResult.NetworkError -> {
                    // Even if logout API fails, we still consider it successful locally
                    LoggerHelper.logDebug("Logout completed (with API error)", "LogoutFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = false,
                        isLoading = false
                    )
                }
                is AuthResult.Loading -> {
                    // Keep loading state
                }
            }
        }
    }
}

