package rfm.com.auth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import rfm.com.auth.dto.*
import rfm.com.auth.model.AuthProvider
import rfm.com.auth.model.User
import rfm.com.auth.repository.RoleRepository
import rfm.com.auth.repository.UserRepository
import rfm.com.auth.security.jwt.JwtTokenProvider
import java.security.SecureRandom
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordService: PasswordService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailService: EmailService,
    private val oAuth2Service: OAuth2Service,
    @Value("\${app.verification.token-expiry:86400000}") private val verificationTokenExpiry: Long,
    @Value("\${app.reset.token-expiry:3600000}") private val resetTokenExpiry: Long
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    private val secureRandom = SecureRandom()

    fun authenticateUser(authRequest: AuthRequest): ApiResponse<AuthResponse> {
        return try {
            val user = userRepository.findByEmail(authRequest.email)
                .orElseThrow { UsernameNotFoundException("User not found with email: ${authRequest.email}") }

            if (!user.verified) {
                return ApiResponse(false, "Please verify your email address before logging in")
            }

            if (user.password == null || !passwordService.verifyPassword(authRequest.password, user.password)) {
                 throw BadCredentialsException("Invalid email or password")
            }

            val userId = user.id ?: throw IllegalStateException("User ID cannot be null")
            val token = jwtTokenProvider.generateTokenFromUserId(userId, user.email)

            val userResponse = UserResponse(
                id = userId,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )

            ApiResponse(true, "Authentication successful", AuthResponse(token, userResponse))
        } catch (ex: Exception) {
            logger.error("Authentication failed", ex)
            ApiResponse(false, "Invalid email or password")
        }
    }

    fun registerUser(signUpRequest: SignUpRequest): ApiResponse<String> {
        try {
            if (signUpRequest.password != signUpRequest.confirmPassword) {
                return ApiResponse(false, "Passwords do not match")
            }
            
            if (userRepository.existsByEmail(signUpRequest.email)) {
                return ApiResponse(false, "User with this email already exists")
            }

            val hashedPassword = passwordService.encodePassword(signUpRequest.password)
            val verificationToken = generateSecureToken()
            
            val userRole = roleRepository.findByName("USER")
                .orElseThrow { RuntimeException("Default role 'USER' not set.") }

            val user = User(
                email = signUpRequest.email,
                password = hashedPassword,
                firstName = signUpRequest.firstName,
                lastName = signUpRequest.lastName,
                verified = false,
                verificationToken = verificationToken,
                authProvider = AuthProvider.LOCAL,
                roles = mutableSetOf(userRole)
            )

            userRepository.save(user)
            emailService.sendVerificationEmail(user.email, verificationToken)

            return ApiResponse(true, "Registration successful. Please check your email.")
        } catch (ex: Exception) {
            logger.error("Registration failed", ex)
            return ApiResponse(false, "Registration failed: ${ex.message}")
        }
    }
    
    fun verifyUser(request: VerificationRequest): ApiResponse<String> {
        return try {
            val user = userRepository.findByVerificationToken(request.token)
                .orElse(null) ?: return ApiResponse(false, "Invalid verification token")
            
            if (user.verified) {
                return ApiResponse(false, "User is already verified")
            }
            
            val updatedUser = user.copy(
                verified = true,
                verificationToken = null
            )
            userRepository.save(updatedUser)
            
            ApiResponse(true, "Email verified successfully")
        } catch (ex: Exception) {
            logger.error("Verification failed", ex)
            ApiResponse(false, "Verification failed")
        }
    }
    
    fun requestPasswordReset(request: PasswordResetRequest): ApiResponse<String> {
        try {
            val user = userRepository.findByEmail(request.email).orElse(null)
            if (user != null) {
                val resetToken = generateSecureToken()
                val expiresAt = System.currentTimeMillis() + resetTokenExpiry
                val updatedUser = user.copy(
                    resetToken = resetToken,
                    resetTokenExpiresAt = expiresAt
                )
                userRepository.save(updatedUser)
                emailService.sendPasswordResetEmail(user.email, resetToken)
            }
            return ApiResponse(true, "If the email exists, a reset link has been sent")
        } catch (ex: Exception) {
            logger.error("Password reset request failed", ex)
            return ApiResponse(false, "Password reset request failed")
        }
    }
    
    fun resetPassword(request: ResetPasswordRequest): ApiResponse<String> {
        try {
             val user = userRepository.findByResetToken(request.token)
                .orElse(null) ?: return ApiResponse(false, "Invalid or expired reset token")

             if (user.resetTokenExpiresAt == null || user.resetTokenExpiresAt < System.currentTimeMillis()) {
                 return ApiResponse(false, "Reset token has expired")
             }
             
             val hashedPassword = passwordService.encodePassword(request.newPassword)
             val updatedUser = user.copy(
                 password = hashedPassword,
                 resetToken = null,
                 resetTokenExpiresAt = null
             )
             userRepository.save(updatedUser)
             
             return ApiResponse(true, "Password reset successfully")
        } catch (ex: Exception) {
            logger.error("Password reset failed", ex)
            return ApiResponse(false, "Password reset failed")
        }
    }
    
    fun authenticateWithGoogle(request: GoogleAuthRequest): ApiResponse<AuthResponse> {
        return oAuth2Service.authenticateWithGoogle(request)
    }

    fun authenticateWithFacebook(request: FacebookAuthRequest): ApiResponse<AuthResponse> {
        return oAuth2Service.authenticateWithFacebook(request)
    }

    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
