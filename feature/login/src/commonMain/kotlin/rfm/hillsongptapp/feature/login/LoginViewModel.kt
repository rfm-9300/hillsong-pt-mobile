package rfm.hillsongptapp.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.UserRepository
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
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
            LoginUiEvent.ToggleSignupMode -> {
                _uiState.value = _uiState.value.copy(isSignupMode = !_uiState.value.isSignupMode)
            }
        }
    }

    private fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            userRepository.login(username, password).let { response ->
                if (response.success) {
                    LoggerHelper.logDebug("Login successful for user: $username", "LoginFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                        errorMessage = null
                    )
                } else {
                    LoggerHelper.logDebug("Login failed: ${response.message}", "LoginFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = false,
                        errorMessage = response.message
                    )
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
            userRepository.signUp(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                firstName = firstName,
                lastName = lastName
            ).let { response ->
                if (response.success) {
                    LoggerHelper.logDebug("Signup successful for user: $email", "SignupFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                        errorMessage = null
                    )
                } else {
                    LoggerHelper.logDebug("Signup failed: ${response.message}", "SignupFlow")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = false,
                        errorMessage = response.message
                    )
                }
            }
        }
    }
}

