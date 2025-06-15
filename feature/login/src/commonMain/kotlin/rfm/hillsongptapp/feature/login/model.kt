package rfm.hillsongptapp.feature.login

data class LoginUiState (
    val isLoading: Boolean = false,
    val usernameField: String = "",
    val passwordField: String = "",
    val isUsernameValid : Boolean = false,
    val isPasswordValid : Boolean = false,
    val isRememberMeChecked : Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        fun empty() = LoginUiState(
            isLoading = false,
            usernameField = "",
            passwordField = "",
            isUsernameValid = false,
            isPasswordValid = false,
            isRememberMeChecked = false,
            isAuthorized = false,
            errorMessage = null
        )
    }
}


sealed class LoginUiEvent {
    data class UsernameTextFieldChanged(val value: String): LoginUiEvent()
    data class PasswordTextFieldChanged(val value: String): LoginUiEvent()
    data object RememberMeCheckboxChanged: LoginUiEvent()
    data class LoginButtonClicked(val username:String, val password: String): LoginUiEvent()
    data class ErrorDismissed(val id: Long) : LoginUiEvent()
}