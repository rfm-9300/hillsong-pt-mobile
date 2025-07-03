package example.com.useCases

import example.com.data.db.user.UserRepository
import example.com.data.db.user.User
import example.com.data.db.user.AuthProvider
import example.com.plugins.Logger
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.util.*

class AuthUser(
    private val userRepository: UserRepository, 
    private val httpClient: HttpClient = HttpClient(CIO) // Specify CIO engine explicitly
) {
    
    suspend fun authenticateGoogleUser(idToken: String): User? {
        // Verify the token with Google
        val googleUserInfo = verifyGoogleToken(idToken) ?: return null
        
        // Extract user information from Google response
        val googleId = googleUserInfo["sub"]?.jsonPrimitive?.content ?: return null
        val email = googleUserInfo["email"]?.jsonPrimitive?.content ?: return null
        val firstName = googleUserInfo["given_name"]?.jsonPrimitive?.content ?: ""
        val lastName = googleUserInfo["family_name"]?.jsonPrimitive?.content ?: ""
        val pictureUrl = googleUserInfo["picture"]?.jsonPrimitive?.content ?: ""
        
        Logger.d("Got Google user info - ID: $googleId, Email: $email, Picture URL: $pictureUrl")
        
        // Check if user exists by Google ID
        val existingUser = userRepository.getUserByGoogleId(googleId)
        
        // Always update profile information regardless of whether user exists or not
        return userRepository.createOrUpdateGoogleUser(
            email = email,
            googleId = googleId,
            firstName = firstName,
            lastName = lastName,
            profileImageUrl = pictureUrl
        )
    }
    
    suspend fun authenticateFacebookUser(accessToken: String): User? {
        // Verify the token with Facebook
        val facebookUserInfo = verifyFacebookToken(accessToken) ?: return null
        
        // Extract user information from Facebook response
        val facebookId = facebookUserInfo["id"]?.jsonPrimitive?.content ?: return null
        val email = facebookUserInfo["email"]?.jsonPrimitive?.content ?: return null
        val firstName = facebookUserInfo["first_name"]?.jsonPrimitive?.content ?: ""
        val lastName = facebookUserInfo["last_name"]?.jsonPrimitive?.content ?: ""
        val pictureUrl = facebookUserInfo["picture"]?.jsonObject?.get("data")?.jsonObject?.get("url")?.jsonPrimitive?.content ?: ""
        
        Logger.d("Got Facebook user info - ID: $facebookId, Email: $email, Picture URL: $pictureUrl")
        
        // Check if user exists by Facebook ID
        val existingUser = userRepository.getUserByFacebookId(facebookId)
        
        // Always update profile information regardless of whether user exists or not
        return userRepository.createOrUpdateFacebookUser(
            email = email,
            facebookId = facebookId,
            firstName = firstName,
            lastName = lastName,
            profileImageUrl = pictureUrl
        )
    }
    
    private suspend fun verifyGoogleToken(idToken: String): JsonObject? {
        return try {
            // Google endpoint to verify token
            val url = "https://oauth2.googleapis.com/tokeninfo?id_token=$idToken"
            
            val response = withContext(Dispatchers.IO) {
                httpClient.get(url)
            }
            
            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                Json.parseToJsonElement(responseText).jsonObject
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error verifying Google token: ${e.message}")
            null
        }
    }
    
    private suspend fun verifyFacebookToken(accessToken: String): JsonObject? {
        return try {
            // Use App ID and App Secret to create an app access token for validation
            val appId = System.getenv("FACEBOOK_APP_ID") ?: ""
            val appSecret = System.getenv("FACEBOOK_APP_SECRET") ?: ""

            Logger.d("Facebook App ID: $appId")
            Logger.d("Facebook App Secret: $appSecret")
            
            // First validate the token with Facebook using App Access Token
            val appAccessToken = "$appId|$appSecret"
            val validateUrl = "https://graph.facebook.com/debug_token?input_token=$accessToken&access_token=$appAccessToken"
            
            Logger.d("Validating Facebook token with URL: $validateUrl")
            
            val validateResponse = withContext(Dispatchers.IO) {
                httpClient.get(validateUrl)
            }
            
            if (validateResponse.status.isSuccess()) {
                val validateResponseText = validateResponse.bodyAsText()
                Logger.d("Facebook validation response: $validateResponseText")
                
                val validateData = Json.parseToJsonElement(validateResponseText).jsonObject
                val isValid = validateData["data"]?.jsonObject?.get("is_valid")?.jsonPrimitive?.booleanOrNull ?: false
                
                if (!isValid) {
                    Logger.d("Facebook token is not valid")
                    return null
                }
                
                // Token is valid, get user info
                val userUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,picture&access_token=$accessToken"
                
                val userResponse = withContext(Dispatchers.IO) {
                    httpClient.get(userUrl)
                }
                
                if (userResponse.status.isSuccess()) {
                    val responseText = userResponse.bodyAsText()
                    Logger.d("Facebook user info response: $responseText")
                    Json.parseToJsonElement(responseText).jsonObject
                } else {
                    Logger.d("Failed to get Facebook user info: ${userResponse.status}")
                    null
                }
            } else {
                Logger.d("Failed to validate Facebook token: ${validateResponse.status}")
                Logger.d("Response body: ${validateResponse.bodyAsText()}")
                null
            }
        } catch (e: Exception) {
            Logger.d("Error verifying Facebook token: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}