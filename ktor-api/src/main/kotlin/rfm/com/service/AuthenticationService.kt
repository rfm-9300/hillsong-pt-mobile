package rfm.com.service

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import rfm.com.config.SecurityLogger
import rfm.com.security.jwt.JwtTokenProvider
import rfm.com.security.jwt.UserPrincipal

@Service
class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) {
    
    /**
     * Authenticate user and generate JWT token
     */
    fun authenticateAndGenerateToken(email: String, password: String): AuthenticationResult {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, password)
            )
            
            val token = jwtTokenProvider.generateToken(authentication)
            val userPrincipal = authentication.principal as UserPrincipal
            
            // Log successful authentication
            SecurityLogger.logSecurityEvent(
                event = SecurityLogger.SecurityEvent.LOGIN_SUCCESS,
                userId = userPrincipal.id,
                email = userPrincipal.getEmail(),
                details = mapOf("authMethod" to "password")
            )
            
            AuthenticationResult.Success(
                token = token,
                userPrincipal = userPrincipal
            )
        } catch (ex: AuthenticationException) {
            // Log failed authentication
            SecurityLogger.logSecurityEvent(
                event = SecurityLogger.SecurityEvent.LOGIN_FAILURE,
                email = email,
                details = mapOf(
                    "reason" to ex.javaClass.simpleName,
                    "message" to (ex.message ?: "Authentication failed")
                )
            )
            
            AuthenticationResult.Failure(ex.message ?: "Authentication failed")
        }
    }
    
    /**
     * Generate token for user without password verification (for OAuth, etc.)
     */
    fun generateTokenForUser(userId: Long): String {
        val userDetails = customUserDetailsService.loadUserById(userId) as UserPrincipal
        return jwtTokenProvider.generateTokenFromUserId(userDetails.id, userDetails.username)
    }
    
    /**
     * Generate verification token
     */
    fun generateVerificationToken(userId: Long, email: String): String {
        return jwtTokenProvider.generateVerificationToken(userId, email)
    }
    
    /**
     * Generate refresh token
     */
    fun generateRefreshToken(userId: Long, email: String): String {
        return jwtTokenProvider.generateRefreshToken(userId, email)
    }
}

sealed class AuthenticationResult {
    data class Success(
        val token: String,
        val userPrincipal: UserPrincipal
    ) : AuthenticationResult()
    
    data class Failure(
        val message: String
    ) : AuthenticationResult()
}