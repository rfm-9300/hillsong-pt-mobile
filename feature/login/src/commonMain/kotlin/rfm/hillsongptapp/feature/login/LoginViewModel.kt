package rfm.hillsongptapp.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.UserRepository

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(this.defaultEmptyState())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.Eagerly, _uiState.value)

    private fun defaultEmptyState() = LoginUiState.empty()


    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.UsernameTextFieldChanged -> {
                _uiState.value = _uiState.value.copy(usernameField = event.value)
            }
            is LoginUiEvent.PasswordTextFieldChanged -> {
                _uiState.value = _uiState.value.copy(passwordField = event.value)
            }
            LoginUiEvent.RememberMeCheckboxChanged -> {
                _uiState.value = _uiState.value.copy(isRememberMeChecked = !_uiState.value.isRememberMeChecked)
            }
            is LoginUiEvent.LoginButtonClicked -> {
                doLogin(event.username, event.password)
            }
            is LoginUiEvent.ErrorDismissed -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    private fun doLogin(username: String, password: String) {
        viewModelScope.launch {
            userRepository.login(username, password).let { response ->
                if (response.success) {
                    println("Login successful: ${response.data?.token}")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = true,
                        errorMessage = null
                    )
                } else {
                    println("Login failed: ${response.message}")
                    _uiState.value = _uiState.value.copy(
                        isAuthorized = false,
                        errorMessage = response.message
                    )
                }
            }
        }
    }
}

