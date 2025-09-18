package rfm.hillsongptapp.feature.auth

import androidx.compose.runtime.Composable

data class LoginUiState (
    val isLoading: Boolean = false,
    val usernameField: String = "",
    val passwordField: String = "",
    val isUsernameValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isRememberMeChecked: Boolean = false,
    val isAuthorized: Boolean = false,
    val errorMessage: String? = null,
    val isSignupMode: Boolean = false,
    val isGoogleLoginInProgress: Boolean = false, // Added to toggle between login and signup
) {
    companion object {
        fun empty() = LoginUiState()
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
    data object GoogleLoginClicked : LoginUiEvent()
    data class GoogleLoginResult(val googleAccount: GoogleAccount?) : LoginUiEvent()
}

data class GoogleAccount(
    val token: String,
    val displayName: String = "",
    val profileImageUrl: String? = null
)

expect class GoogleAuthUiProvider {
    suspend fun signIn(): GoogleAccount?
}

expect class GoogleAuthProvider {
    @Composable
    fun getUiProvider(): GoogleAuthUiProvider

    suspend fun signOut()
}