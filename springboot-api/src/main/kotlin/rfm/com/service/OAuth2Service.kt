package rfm.com.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import rfm.com.dto.*
import rfm.com.entity.AuthProvider
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.repository.UserRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.security.jwt.JwtTokenProvider
import java.security.SecureRandom
import java.util.*

@Service
class OAuth2Service(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val restTemplate: RestTemplate = RestTemplate(),
    private val objectMapper: ObjectMapper = ObjectMapper(),
    @Value("\${app.oauth2.google.client-id:}") private val googleClientId: String,
    @Value("\${app.oauth2.facebook.app-id:}") private val facebookAppId: String,
    @Value("\${app.oauth2.facebook.app-secret:}") private val facebookAppSecret: String
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)
    private val secureRandom = SecureRandom()
    
    /**
     * Authenticate user with Google OAuth2
     */
    fun authenticateWithGoogle(googleAuthRequest: GoogleAuthRequest): ApiResponse<AuthResponse> {
        return try {
            logger.debug("Attempting Google OAuth authentication")
            
            // Verify Google ID token
            val googleUserInfo = verifyGoogleIdToken(googleAuthRequest.idToken)
                ?: return ApiResponse(success = false, message = "Invalid Google ID token")
            
            val email = googleUserInfo["email"]?.asText()
                ?: return ApiResponse(success = false, message = "Email not provided by Google")
            
            val googleId = googleUserInfo["sub"]?.asText()
                ?: return ApiResponse(success = false, message = "Google ID not found")
            
            val firstName = googleUserInfo["given_name"]?.asText() ?: ""
            val lastName = googleUserInfo["family_name"]?.asText() ?: ""
            val profilePicture = googleUserInfo["picture"]?.asText() ?: ""
            
            // Check if user exists by Google ID
            var user = userRepository.findByGoogleId(googleId)
            
            if (user == null) {
                // Check if user exists by email
                user = userRepository.findByEmail(email)
                
                if (user != null) {
                    // Link existing account with Google
                    val updatedUser = user.copy(
                        googleId = googleId,
                        verified = true // Auto-verify Google users
                    )
                    user = userRepository.save(updatedUser)
                } else {
                    // Create new user
                    user = createGoogleUser(email, googleId, firstName, lastName, profilePicture)
                }
            }
            
            // At this point user should never be null, but let's be safe
            requireNotNull(user) { "User should not be null after authentication process" }
            
            // Generate JWT token
            val token = jwtTokenProvider.generateTokenFromUserId(user.id!!, user.email)
            
            logger.info("Google authentication successful for user: $email")
            // Create user response
            val userResponse = UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.profile?.firstName ?: "",
                lastName = user.profile?.lastName ?: "",
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )
            
            ApiResponse(
                success = true,
                message = "Google authentication successful",
                data = AuthResponse(token, userResponse)
            )
        } catch (ex: Exception) {
            logger.error("Google authentication failed", ex)
            ApiResponse(success = false, message = "Google authentication failed: ${ex.message}")
        }
    }
    
    /**
     * Authenticate user with Facebook OAuth2
     */
    fun authenticateWithFacebook(facebookAuthRequest: FacebookAuthRequest): ApiResponse<AuthResponse> {
        return try {
            logger.debug("Attempting Facebook OAuth authentication")
            
            // Verify Facebook access token
            val facebookUserInfo = verifyFacebookAccessToken(facebookAuthRequest.accessToken)
                ?: return ApiResponse(success = false, message = "Invalid Facebook access token")
            
            val email = facebookUserInfo["email"]?.asText()
                ?: return ApiResponse(success = false, message = "Email not provided by Facebook")
            
            val facebookId = facebookUserInfo["id"]?.asText()
                ?: return ApiResponse(success = false, message = "Facebook ID not found")
            
            val firstName = facebookUserInfo["first_name"]?.asText() ?: ""
            val lastName = facebookUserInfo["last_name"]?.asText() ?: ""
            val profilePicture = facebookUserInfo["picture"]?.get("data")?.get("url")?.asText() ?: ""
            
            // Check if user exists by Facebook ID
            var user = userRepository.findByFacebookId(facebookId)
            
            if (user == null) {
                // Check if user exists by email
                user = userRepository.findByEmail(email)
                
                if (user != null) {
                    // Link existing account with Facebook
                    val updatedUser = user.copy(
                        facebookId = facebookId,
                        verified = true // Auto-verify Facebook users
                    )
                    user = userRepository.save(updatedUser)
                } else {
                    // Create new user
                    user = createFacebookUser(email, facebookId, firstName, lastName, profilePicture)
                }
            }
            
            // At this point user should never be null, but let's be safe
            requireNotNull(user) { "User should not be null after authentication process" }
            
            // Generate JWT token
            val token = jwtTokenProvider.generateTokenFromUserId(user.id!!, user.email)
            
            logger.info("Facebook authentication successful for user: $email")
            // Create user response
            val userResponse = UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.profile?.firstName ?: "",
                lastName = user.profile?.lastName ?: "",
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )
            
            ApiResponse(
                success = true,
                message = "Facebook authentication successful",
                data = AuthResponse(token, userResponse)
            )
        } catch (ex: Exception) {
            logger.error("Facebook authentication failed", ex)
            ApiResponse(success = false, message = "Facebook authentication failed: ${ex.message}")
        }
    }
    
    /**
     * Verify Google ID token with Google's tokeninfo endpoint
     */
    private fun verifyGoogleIdToken(idToken: String): JsonNode? {
        return try {
            val url = "https://oauth2.googleapis.com/tokeninfo?id_token=$idToken"
            val response = restTemplate.getForObject(url, String::class.java)
            
            if (response != null) {
                val jsonNode = objectMapper.readTree(response)
                
                // Verify the token is for our application
                val audience = jsonNode["aud"]?.asText()
                if (googleClientId.isNotEmpty() && audience != googleClientId) {
                    logger.warn("Google ID token audience mismatch. Expected: $googleClientId, Got: $audience")
                    return null
                }
                
                // Check if token is expired
                val exp = jsonNode["exp"]?.asLong()
                if (exp != null && exp < System.currentTimeMillis() / 1000) {
                    logger.warn("Google ID token is expired")
                    return null
                }
                
                jsonNode
            } else {
                null
            }
        } catch (ex: Exception) {
            logger.error("Failed to verify Google ID token", ex)
            null
        }
    }
    
    /**
     * Verify Facebook access token with Facebook's debug_token endpoint
     */
    private fun verifyFacebookAccessToken(accessToken: String): JsonNode? {
        return try {
            // First, get app access token
            val appAccessToken = "$facebookAppId|$facebookAppSecret"
            
            // Verify the user access token
            val debugUrl = "https://graph.facebook.com/debug_token?input_token=$accessToken&access_token=$appAccessToken"
            val debugResponse = restTemplate.getForObject(debugUrl, String::class.java)
            
            if (debugResponse != null) {
                val debugJson = objectMapper.readTree(debugResponse)
                val data = debugJson["data"]
                
                // Check if token is valid
                val isValid = data["is_valid"]?.asBoolean() ?: false
                if (!isValid) {
                    logger.warn("Facebook access token is not valid")
                    return null
                }
                
                // Check if token is for our app
                val appId = data["app_id"]?.asText()
                if (facebookAppId.isNotEmpty() && appId != facebookAppId) {
                    logger.warn("Facebook access token app ID mismatch. Expected: $facebookAppId, Got: $appId")
                    return null
                }
                
                // Get user information
                val userInfoUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,picture&access_token=$accessToken"
                val userInfoResponse = restTemplate.getForObject(userInfoUrl, String::class.java)
                
                if (userInfoResponse != null) {
                    objectMapper.readTree(userInfoResponse)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            logger.error("Failed to verify Facebook access token", ex)
            null
        }
    }
    
    /**
     * Create new user from Google OAuth
     */
    private fun createGoogleUser(email: String, googleId: String, firstName: String, lastName: String, profilePicture: String): User {
        // Generate a random password (won't be used for OAuth users)
        val randomPassword = generateRandomPassword()
        
        val user = User(
            email = email,
            password = randomPassword, // This won't be used for OAuth login
            salt = "", // Not needed for OAuth users
            verified = true, // Auto-verify OAuth users
            googleId = googleId,
            authProvider = AuthProvider.GOOGLE
        )
        
        val savedUser = userRepository.save(user)
        
        // Create user profile
        val userProfile = UserProfile(
            user = savedUser,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = "",
            imagePath = profilePicture,
            isAdmin = false
        )
        
        userProfileRepository.save(userProfile)
        
        logger.info("Created new Google user: $email")
        return savedUser
    }
    
    /**
     * Create new user from Facebook OAuth
     */
    private fun createFacebookUser(email: String, facebookId: String, firstName: String, lastName: String, profilePicture: String): User {
        // Generate a random password (won't be used for OAuth users)
        val randomPassword = generateRandomPassword()
        
        val user = User(
            email = email,
            password = randomPassword, // This won't be used for OAuth login
            salt = "", // Not needed for OAuth users
            verified = true, // Auto-verify OAuth users
            facebookId = facebookId,
            authProvider = AuthProvider.FACEBOOK
        )
        
        val savedUser = userRepository.save(user)
        
        // Create user profile
        val userProfile = UserProfile(
            user = savedUser,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = "",
            imagePath = profilePicture,
            isAdmin = false
        )
        
        userProfileRepository.save(userProfile)
        
        logger.info("Created new Facebook user: $email")
        return savedUser
    }
    
    /**
     * Generate random password for OAuth users
     */
    private fun generateRandomPassword(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}