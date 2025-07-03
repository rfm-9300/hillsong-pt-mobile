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
        LoggerHelper.logDebug("signIn() called")
        val request = getCredentialRequest()

        try {
            val credential = credentialManager.getCredential(
                context = activityContext,
                request = request
            ).credential
            LoggerHelper.logDebug("Credential received: $credential")
            handleSignIn(credential)
        } catch (e: Exception) {
            when {
                // Handle the specific NoCredentialException case
                e.javaClass.simpleName == "NoCredentialException" -> {
                    LoggerHelper.logDebug("No credentials available - attempting to show sign-in UI explicitly")

                    // Create a request that forces the UI to show
                    val fallbackRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setAutoSelectEnabled(false) // Force UI to show
                                .setServerClientId("1033467061192-m3dcm1ieebgp26fbijigfj7gqant7mdg.apps.googleusercontent.com")
                                .build()
                        )
                        .build()

                    try {
                        val fallbackCredential = credentialManager.getCredential(
                            context = activityContext,
                            request = fallbackRequest
                        ).credential
                        LoggerHelper.logDebug("Fallback credential received: $fallbackCredential")
                        handleSignIn(fallbackCredential)
                    } catch (fallbackException: Exception) {
                        LoggerHelper.logDebug("Fallback authentication failed: ${fallbackException.message}")
                        null
                    }
                }
                else -> {
                    LoggerHelper.logDebug("Authentication exception: ${e.javaClass.simpleName} - ${e.message}")
                    null
                }
            }
        }
    } catch (e: Exception) {
        LoggerHelper.logDebug("Unexpected exception during sign-in flow: ${e.javaClass.simpleName} - ${e.message}")
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
        .setServerClientId("1033467061192-m3dcm1ieebgp26fbijigfj7gqant7mdg.apps.googleusercontent.com")
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