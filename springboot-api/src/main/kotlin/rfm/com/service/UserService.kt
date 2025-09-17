package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.repository.UserRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.security.jwt.JwtTokenProvider
import rfm.com.service.EmailService
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val passwordService: PasswordService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val emailService: EmailService,
    private val oAuth2Service: OAuth2Service,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String,
    @Value("\${app.verification.token-expiry:86400000}") private val verificationTokenExpiry: Long, // 24 hours
    @Value("\${app.reset.token-expiry:3600000}") private val resetTokenExpiry: Long // 1 hour
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private val secureRandom = SecureRandom()
    
    /**
     * Authenticate user with email and password
     */
    fun authenticateUser(authRequest: AuthRequest): ApiResponse<AuthResponse> {
        return try {
            logger.debug("Attempting to authenticate user with email: ${authRequest.email}")
            
            val user = userRepository.findByEmail(authRequest.email)
                ?: throw UsernameNotFoundException("User not found with email: ${authRequest.email}")
            
            if (!user.verified) {
                logger.warn("User attempted to login with unverified email: ${authRequest.email}")
                return ApiResponse(
                    success = false,
                    message = "Please verify your email address before logging in"
                )
            }
            
            // Verify password using BCrypt
            logger.debug("Verifying password with BCrypt")
            val isPasswordValid = passwordService.verifyPassword(authRequest.password, user.password)
            logger.debug("Password verification result: $isPasswordValid")
            
            if (!isPasswordValid) {
                logger.warn("Invalid password attempt for user: ${authRequest.email}")
                throw BadCredentialsException("Invalid email or password")
            }
            
            // Get user ID safely
            val userId = user.id ?: throw IllegalStateException("User ID cannot be null for authenticated user")
            
            // Generate JWT token
            val token = jwtTokenProvider.generateTokenFromUserId(userId, user.email)
            
            logger.info("User authenticated successfully: ${authRequest.email}")
            // Create user response
            val userResponse = UserResponse(
                id = userId,
                email = user.email,
                firstName = user.profile?.firstName ?: "",
                lastName = user.profile?.lastName ?: "",
                verified = user.verified,
                createdAt = user.createdAt,
                authProvider = user.authProvider.name
            )
            
            ApiResponse(
                success = true,
                message = "Authentication successful",
                data = AuthResponse(token, userResponse)
            )
        } catch (ex: UsernameNotFoundException) {
            logger.error("Authentication failed - user not found: ${authRequest.email}")
            ApiResponse(success = false, message = "Invalid email or password")
        } catch (ex: BadCredentialsException) {
            logger.error("Authentication failed - invalid credentials: ${authRequest.email}")
            ApiResponse(success = false, message = "Invalid email or password")
        } catch (ex: Exception) {
            logger.error("Authentication failed with unexpected error", ex)
            ApiResponse(success = false, message = "Authentication failed")
        }
    }
    
    /**
     * Register a new user
     */
    fun registerUser(signUpRequest: SignUpRequest): ApiResponse<String> {
        return try {
            logger.debug("Attempting to register user with email: ${signUpRequest.email}")
            
            // Validate input
            if (signUpRequest.password != signUpRequest.confirmPassword) {
                return ApiResponse(success = false, message = "Passwords do not match")
            }
            
            if (signUpRequest.password.length < 8) {
                return ApiResponse(success = false, message = "Password must be at least 8 characters long")
            }
            
            // Check if user already exists
            if (userRepository.existsByEmail(signUpRequest.email)) {
                logger.warn("Registration attempt with existing email: ${signUpRequest.email}")
                return ApiResponse(success = false, message = "User with this email already exists")
            }
            
            // Generate BCrypt hash for password
            val hashedPassword = passwordService.encodePassword(signUpRequest.password)
            
            // Generate verification token
            val verificationToken = generateSecureToken()
            
            // Create user entity
            val user = User(
                email = signUpRequest.email,
                password = hashedPassword,
                salt = "", // Empty salt (BCrypt handles salting internally)
                verified = false,
                verificationToken = verificationToken,
                authProvider = AuthProvider.LOCAL
            )
            
            val savedUser = userRepository.save(user)
            
            // Create user profile
            val userProfile = UserProfile(
                user = savedUser,
                firstName = signUpRequest.firstName,
                lastName = signUpRequest.lastName,
                email = savedUser.email,
                phone = "",
                imagePath = "",
                isAdmin = false
            )
            
            userProfileRepository.save(userProfile)
            
            // Send verification email
            sendVerificationEmail(savedUser.email, verificationToken)
            
            logger.info("User registered successfully: ${signUpRequest.email}")
            ApiResponse(
                success = true,
                message = "Registration successful. Please check your email to verify your account."
            )
        } catch (ex: Exception) {
            logger.error("Registration failed for email: ${signUpRequest.email}", ex)
            ApiResponse(success = false, message = "Registration failed: ${ex.message}")
        }
    }
    
    /**
     * Verify user email with verification token
     */
    fun verifyUser(verificationRequest: VerificationRequest): ApiResponse<String> {
        return try {
            logger.debug("Attempting to verify user with token")
            
            val user = userRepository.findByVerificationToken(verificationRequest.token)
                ?: return ApiResponse(success = false, message = "Invalid verification token")
            
            if (user.verified) {
                return ApiResponse(success = false, message = "User is already verified")
            }
            
            // Update user as verified and clear verification token
            val updatedUser = user.copy(
                verified = true,
                verificationToken = null
            )
            
            userRepository.save(updatedUser)
            
            logger.info("User verified successfully: ${user.email}")
            ApiResponse(success = true, message = "Email verified successfully")
        } catch (ex: Exception) {
            logger.error("Email verification failed", ex)
            ApiResponse(success = false, message = "Email verification failed")
        }
    }
    
    /**
     * Request password reset
     */
    fun requestPasswordReset(passwordResetRequest: PasswordResetRequest): ApiResponse<String> {
        return try {
            logger.debug("Password reset requested for email: ${passwordResetRequest.email}")
            
            val user = userRepository.findByEmail(passwordResetRequest.email)
                ?: return ApiResponse(success = true, message = "If the email exists, a reset link has been sent")
            
            // Generate reset token and expiry
            val resetToken = generateSecureToken()
            val resetTokenExpiresAt = System.currentTimeMillis() + resetTokenExpiry
            
            // Update user with reset token
            val updatedUser = user.copy(
                resetToken = resetToken,
                resetTokenExpiresAt = resetTokenExpiresAt
            )
            
            userRepository.save(updatedUser)
            
            // Send password reset email
            emailService.sendPasswordResetEmail(user.email, resetToken, baseUrl)
            
            logger.info("Password reset email sent to: ${passwordResetRequest.email}")
            ApiResponse(
                success = true,
                message = "If the email exists, a reset link has been sent"
            )
        } catch (ex: Exception) {
            logger.error("Password reset request failed for email: ${passwordResetRequest.email}", ex)
            ApiResponse(success = false, message = "Password reset request failed")
        }
    }
    
    /**
     * Reset password with token
     */
    fun resetPassword(resetPasswordRequest: ResetPasswordRequest): ApiResponse<String> {
        return try {
            logger.debug("Attempting to reset password with token")
            
            val user = userRepository.findByResetToken(resetPasswordRequest.token)
                ?: return ApiResponse(success = false, message = "Invalid or expired reset token")
            
            // Check if token is expired
            if (user.resetTokenExpiresAt == null || user.resetTokenExpiresAt < System.currentTimeMillis()) {
                return ApiResponse(success = false, message = "Reset token has expired")
            }
            
            if (resetPasswordRequest.newPassword.length < 8) {
                return ApiResponse(success = false, message = "Password must be at least 8 characters long")
            }
            
            // Generate new BCrypt hash for password
            val hashedPassword = passwordService.encodePassword(resetPasswordRequest.newPassword)
            
            // Update user with new password and clear reset token
            val updatedUser = user.copy(
                password = hashedPassword,
                salt = "", // Empty salt (BCrypt handles salting internally)
                resetToken = null,
                resetTokenExpiresAt = null
            )
            
            userRepository.save(updatedUser)
            
            logger.info("Password reset successfully for user: ${user.email}")
            ApiResponse(success = true, message = "Password reset successfully")
        } catch (ex: Exception) {
            logger.error("Password reset failed", ex)
            ApiResponse(success = false, message = "Password reset failed")
        }
    }
    
    /**
     * Authenticate user with Google OAuth2
     */
    fun authenticateWithGoogle(googleAuthRequest: GoogleAuthRequest): ApiResponse<AuthResponse> {
        return oAuth2Service.authenticateWithGoogle(googleAuthRequest)
    }
    
    /**
     * Authenticate user with Facebook OAuth2
     */
    fun authenticateWithFacebook(facebookAuthRequest: FacebookAuthRequest): ApiResponse<AuthResponse> {
        return oAuth2Service.authenticateWithFacebook(facebookAuthRequest)
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    fun getUserById(userId: Long): User? {
        return userRepository.findByIdWithProfile(userId)
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmailWithProfile(email)
    }
    
    /**
     * Update user profile
     */
    fun updateUserProfile(userId: Long, firstName: String?, lastName: String?, phone: String?): ApiResponse<String> {
        return try {
            val user = userRepository.findByIdWithProfile(userId)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val profile = user.profile
                ?: return ApiResponse(success = false, message = "User profile not found")
            
            val updatedProfile = profile.copy(
                firstName = firstName ?: profile.firstName,
                lastName = lastName ?: profile.lastName,
                phone = phone ?: profile.phone
            )
            
            userProfileRepository.save(updatedProfile)
            
            logger.info("User profile updated for user ID: $userId")
            ApiResponse(success = true, message = "Profile updated successfully")
        } catch (ex: Exception) {
            logger.error("Profile update failed for user ID: $userId", ex)
            ApiResponse(success = false, message = "Profile update failed")
        }
    }
    
    /**
     * Update user profile image
     */
    fun updateUserProfileImage(userId: Long, imagePath: String): ApiResponse<String> {
        return try {
            val user = userRepository.findByIdWithProfile(userId)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val profile = user.profile
                ?: return ApiResponse(success = false, message = "User profile not found")
            
            val updatedProfile = profile.copy(imagePath = imagePath)
            userProfileRepository.save(updatedProfile)
            
            logger.info("User profile image updated for user ID: $userId")
            ApiResponse(success = true, message = "Profile image updated successfully")
        } catch (ex: Exception) {
            logger.error("Profile image update failed for user ID: $userId", ex)
            ApiResponse(success = false, message = "Profile image update failed")
        }
    }
    
    /**
     * Get user profile by user ID
     */
    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): ApiResponse<UserProfileResponse> {
        return try {
            val user = userRepository.findByIdWithProfile(userId)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val profile = user.profile
                ?: return ApiResponse(success = false, message = "User profile not found")
            
            val profileResponse = UserProfileResponse(
                id = profile.id!!,
                userId = user.id!!,
                firstName = profile.firstName,
                lastName = profile.lastName,
                email = profile.email,
                phone = profile.phone,
                imagePath = profile.imagePath,
                isAdmin = profile.isAdmin,
                joinedAt = profile.joinedAt,
                fullName = profile.fullName
            )
            
            ApiResponse(success = true, message = "Profile retrieved successfully", data = profileResponse)
        } catch (ex: Exception) {
            logger.error("Failed to get user profile for user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to retrieve user profile")
        }
    }
    
    /**
     * Get all user profiles (admin functionality)
     */
    @Transactional(readOnly = true)
    fun getAllUserProfiles(): ApiResponse<List<UserProfileResponse>> {
        return try {
            val profiles = userProfileRepository.findAllOrderByName()
            
            val profileResponses = profiles.map { profile ->
                UserProfileResponse(
                    id = profile.id!!,
                    userId = profile.user.id!!,
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    email = profile.email,
                    phone = profile.phone,
                    imagePath = profile.imagePath,
                    isAdmin = profile.isAdmin,
                    joinedAt = profile.joinedAt,
                    fullName = profile.fullName
                )
            }
            
            ApiResponse(success = true, message = "User profiles retrieved successfully", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to get all user profiles", ex)
            ApiResponse(success = false, message = "Failed to retrieve user profiles")
        }
    }
    
    /**
     * Search user profiles by name or email
     */
    @Transactional(readOnly = true)
    fun searchUserProfiles(searchTerm: String): ApiResponse<List<UserProfileResponse>> {
        return try {
            val profiles = userProfileRepository.searchProfiles(searchTerm)
            
            val profileResponses = profiles.map { profile ->
                UserProfileResponse(
                    id = profile.id!!,
                    userId = profile.user.id!!,
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    email = profile.email,
                    phone = profile.phone,
                    imagePath = profile.imagePath,
                    isAdmin = profile.isAdmin,
                    joinedAt = profile.joinedAt,
                    fullName = profile.fullName
                )
            }
            
            ApiResponse(success = true, message = "User profiles search completed", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to search user profiles with term: $searchTerm", ex)
            ApiResponse(success = false, message = "Failed to search user profiles")
        }
    }
    
    /**
     * Update user admin status (admin functionality)
     */
    fun updateUserAdminStatus(userId: Long, isAdmin: Boolean): ApiResponse<String> {
        return try {
            val user = userRepository.findByIdWithProfile(userId)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val profile = user.profile
                ?: return ApiResponse(success = false, message = "User profile not found")
            
            val updatedProfile = profile.copy(isAdmin = isAdmin)
            userProfileRepository.save(updatedProfile)
            
            val action = if (isAdmin) "granted" else "revoked"
            logger.info("Admin privileges $action for user ID: $userId")
            ApiResponse(success = true, message = "Admin status updated successfully")
        } catch (ex: Exception) {
            logger.error("Failed to update admin status for user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to update admin status")
        }
    }
    
    /**
     * Get admin user profiles
     */
    @Transactional(readOnly = true)
    fun getAdminProfiles(): ApiResponse<List<UserProfileResponse>> {
        return try {
            val profiles = userProfileRepository.findAdminProfiles()
            
            val profileResponses = profiles.map { profile ->
                UserProfileResponse(
                    id = profile.id!!,
                    userId = profile.user.id!!,
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    email = profile.email,
                    phone = profile.phone,
                    imagePath = profile.imagePath,
                    isAdmin = profile.isAdmin,
                    joinedAt = profile.joinedAt,
                    fullName = profile.fullName
                )
            }
            
            ApiResponse(success = true, message = "Admin profiles retrieved successfully", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to get admin profiles", ex)
            ApiResponse(success = false, message = "Failed to retrieve admin profiles")
        }
    }
    
    /**
     * Delete user account (admin functionality)
     */
    fun deleteUser(userId: Long): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId)
                .orElse(null) ?: return ApiResponse(success = false, message = "User not found")
            
            userRepository.delete(user)
            
            logger.info("User deleted successfully: ${user.email}")
            ApiResponse(success = true, message = "User deleted successfully")
        } catch (ex: Exception) {
            logger.error("Failed to delete user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to delete user")
        }
    }
    
    /**
     * Send verification email
     */
    private fun sendVerificationEmail(email: String, verificationToken: String) {
        try {
            emailService.sendVerificationEmail(email, verificationToken)
            logger.debug("Verification email sent to: $email")
        } catch (ex: Exception) {
            logger.error("Failed to send verification email to: $email", ex)
            // Don't throw exception here as user registration should still succeed
        }
    }
    
    /**
     * Generate secure random token
     */
    private fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    /**
     * Clean up expired reset tokens (should be called periodically)
     */
    @Transactional
    fun cleanupExpiredResetTokens() {
        try {
            val currentTime = System.currentTimeMillis()
            val usersWithExpiredTokens = userRepository.findUsersWithExpiredResetTokens(currentTime)
            
            usersWithExpiredTokens.forEach { user ->
                val updatedUser = user.copy(
                    resetToken = null,
                    resetTokenExpiresAt = null
                )
                userRepository.save(updatedUser)
            }
            
            if (usersWithExpiredTokens.isNotEmpty()) {
                logger.info("Cleaned up ${usersWithExpiredTokens.size} expired reset tokens")
            }
        } catch (ex: Exception) {
            logger.error("Failed to cleanup expired reset tokens", ex)
        }
    }
}