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
    val isSignupMode: Boolean = false, // Added to toggle between login and signup
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
    data class LoginButtonClicked(val username:String, val password: String): LoginUiEvent()
    data class SignupButtonClicked(
        val email: String,
        val password: String,
        val confirmPassword: String,
        val firstName: String,
        val lastName: String
    ): LoginUiEvent()
    data object ErrorDismissed : LoginUiEvent()
    data object ToggleSignupMode : LoginUiEvent() // Event to toggle between login and signup
}