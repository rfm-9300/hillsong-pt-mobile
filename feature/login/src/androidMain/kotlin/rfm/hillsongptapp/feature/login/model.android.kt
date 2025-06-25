package rfm.hillsongptapp.feature.login

import android.R
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import rfm.hillsongptapp.logging.LoggerHelper

actual class GoogleAuthUiProvider(
    private val activityContext: Context,
    private val credentialManager: CredentialManager
) {
    actual suspend fun signIn(): GoogleAccount? = try {
        LoggerHelper.logDebug( "signIn() called")
        val credential = credentialManager.getCredential(
            context = activityContext,
            request = getCredentialRequest()
        ).credential
        LoggerHelper.logDebug( "Credential received: $credential")
        handleSignIn(credential)
    } catch (e: Exception) {
        LoggerHelper.logDebug( "Exception: ${e.message}")
        null
    }

    private fun handleSignIn(credential: Credential): GoogleAccount? = when {
        credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleAccount(
                    token = googleIdTokenCredential.idToken,
                    displayName = googleIdTokenCredential.displayName ?: "",
                    profileImageUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } catch (e: GoogleIdTokenParsingException) {
                null
            }
        }
        else -> null
    }

    private fun getCredentialRequest(): GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(getGoogleIdOption())
        .build()

    private fun getGoogleIdOption(): GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(true)
        .setServerClientId("1033467061192-r10l64e9rui96nr6qo0m1h46u4u6up3i.apps.googleusercontent.com")
        .build()
}

actual class GoogleAuthProvider(
    private val credentialManager: CredentialManager
) {
    @Composable
    actual fun getUiProvider(): GoogleAuthUiProvider {
        val activityContext = LocalContext.current
        return GoogleAuthUiProvider(activityContext, credentialManager)
    }

    actual suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}