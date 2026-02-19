package rfm.com.auth.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import rfm.com.auth.dto.*
import rfm.com.auth.model.AuthProvider
import rfm.com.auth.model.User
import rfm.com.auth.repository.RoleRepository
import rfm.com.auth.repository.UserRepository
import rfm.com.auth.security.jwt.JwtTokenProvider
import java.security.SecureRandom
import java.util.*

@Service
class OAuth2Service(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${app.oauth2.google.client-id:}") private val googleClientId: String,
    @Value("\${app.oauth2.facebook.app-id:}") private val facebookAppId: String,
    @Value("\${app.oauth2.facebook.app-secret:}") private val facebookAppSecret: String
) {
    
    private val logger = LoggerFactory.getLogger(OAuth2Service::class.java)
    private val secureRandom = SecureRandom()
    
    fun authenticateWithGoogle(googleAuthRequest: GoogleAuthRequest): ApiResponse<AuthResponse> {
        return try {
            val googleUserInfo = verifyGoogleIdToken(googleAuthRequest.idToken)
                ?: return ApiResponse(false, "Invalid Google ID token")
            
            val email = googleUserInfo["email"]?.asText()
                ?: return ApiResponse(false, "Email not provided by Google")
            
            val googleId = googleUserInfo["sub"]?.asText()
                ?: return ApiResponse(false, "Google ID not found")
            
            val firstName = googleUserInfo["given_name"]?.asText() ?: ""
            val lastName = googleUserInfo["family_name"]?.asText() ?: ""
            val profilePicture = googleUserInfo["picture"]?.asText() ?: ""
            
            var user = userRepository.findByGoogleId(googleId).orElse(null)
            
            if (user == null) {
                user = userRepository.findByEmail(email).orElse(null)
                
                if (user != null) {
                    val updatedUser = user.copy(
                        googleId = googleId,
                        verified = true
                    )
                    user = userRepository.save(updatedUser)
                } else {
                    user = createGoogleUser(email, googleId, firstName, lastName, profilePicture)
                }
            }
            
            val token = jwtTokenProvider.generateTokenFromUserId(user!!.id!!, user.email)
            
            val userResponse = UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )
            
            ApiResponse(true, "Google authentication successful", AuthResponse(token, userResponse))
        } catch (ex: Exception) {
            logger.error("Google authentication failed", ex)
            ApiResponse(false, "Google authentication failed: ${ex.message}")
        }
    }
    
    fun authenticateWithFacebook(facebookAuthRequest: FacebookAuthRequest): ApiResponse<AuthResponse> {
        return try {
            val facebookUserInfo = verifyFacebookAccessToken(facebookAuthRequest.accessToken)
                ?: return ApiResponse(false, "Invalid Facebook access token")
            
            val email = facebookUserInfo["email"]?.asText()
                ?: return ApiResponse(false, "Email not provided by Facebook")
            
            val facebookId = facebookUserInfo["id"]?.asText()
                ?: return ApiResponse(false, "Facebook ID not found")
            
            val firstName = facebookUserInfo["first_name"]?.asText() ?: ""
            val lastName = facebookUserInfo["last_name"]?.asText() ?: ""
            val profilePicture = facebookUserInfo["picture"]?.get("data")?.get("url")?.asText() ?: ""
            
            var user = userRepository.findByFacebookId(facebookId).orElse(null)
            
            if (user == null) {
                user = userRepository.findByEmail(email).orElse(null)
                
                if (user != null) {
                    val updatedUser = user.copy(
                        facebookId = facebookId,
                        verified = true
                    )
                    user = userRepository.save(updatedUser)
                } else {
                    user = createFacebookUser(email, facebookId, firstName, lastName, profilePicture)
                }
            }
            
            val token = jwtTokenProvider.generateTokenFromUserId(user!!.id!!, user.email)
            
            val userResponse = UserResponse(
                id = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )
            
            ApiResponse(true, "Facebook authentication successful", AuthResponse(token, userResponse))
        } catch (ex: Exception) {
            logger.error("Facebook authentication failed", ex)
            ApiResponse(false, "Facebook authentication failed: ${ex.message}")
        }
    }
    
    private fun verifyGoogleIdToken(idToken: String): JsonNode? {
        return try {
            val url = "https://oauth2.googleapis.com/tokeninfo?id_token=$idToken"
            val response = restTemplate.getForObject(url, String::class.java)
            
            if (response != null) {
                val jsonNode = objectMapper.readTree(response)
                
                val audience = jsonNode["aud"]?.asText()
                if (googleClientId.isNotEmpty() && audience != googleClientId) {
                    logger.warn("Google ID token audience mismatch.")
                    return null
                }
                
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
    
    private fun verifyFacebookAccessToken(accessToken: String): JsonNode? {
        return try {
            val appAccessToken = "$facebookAppId|$facebookAppSecret"
            val debugUrl = "https://graph.facebook.com/debug_token?input_token=$accessToken&access_token=$appAccessToken"
            val debugResponse = restTemplate.getForObject(debugUrl, String::class.java)
            
            if (debugResponse != null) {
                val debugJson = objectMapper.readTree(debugResponse)
                val data = debugJson["data"]
                
                val isValid = data["is_valid"]?.asBoolean() ?: false
                if (!isValid) {
                    logger.warn("Facebook access token is not valid")
                    return null
                }
                
                val appId = data["app_id"]?.asText()
                if (facebookAppId.isNotEmpty() && appId != facebookAppId) {
                    logger.warn("Facebook access token app ID mismatch.")
                    return null
                }
                
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
    
    private fun createGoogleUser(email: String, googleId: String, firstName: String, lastName: String, profilePicture: String): User {
        val randomPassword = generateRandomPassword()
        val userRole = roleRepository.findByName("USER").orElse(null)
        
        val roles = if (userRole != null) mutableSetOf(userRole) else mutableSetOf()
        
        val user = User(
            email = email,
            password = randomPassword,
            firstName = firstName,
            lastName = lastName,
            imagePath = profilePicture,
            verified = true,
            googleId = googleId,
            authProvider = AuthProvider.GOOGLE,
            roles = roles
        )
        
        return userRepository.save(user)
    }
    
    private fun createFacebookUser(email: String, facebookId: String, firstName: String, lastName: String, profilePicture: String): User {
        val randomPassword = generateRandomPassword()
        val userRole = roleRepository.findByName("USER").orElse(null)
        
        val roles = if (userRole != null) mutableSetOf(userRole) else mutableSetOf()
        
        val user = User(
            email = email,
            password = randomPassword,
            firstName = firstName,
            lastName = lastName,
            imagePath = profilePicture,
            verified = true,
            facebookId = facebookId,
            authProvider = AuthProvider.FACEBOOK,
            roles = roles
        )
        
        return userRepository.save(user)
    }
    
    private fun generateRandomPassword(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}
