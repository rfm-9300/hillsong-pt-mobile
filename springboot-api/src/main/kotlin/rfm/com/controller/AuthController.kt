package rfm.com.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.service.UserService

/**
 * REST Controller for authentication endpoints
 * Handles user login, signup, verification, password reset, and OAuth2 authentication
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody authRequest: AuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Login attempt for email: ${authRequest.email}")
        
        val result = userService.authenticateUser(authRequest)
        
        return if (result.success) {
            logger.info("Login successful for email: ${authRequest.email}")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Login failed for email: ${authRequest.email} - ${result.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }
    
    /**
     * User signup endpoint
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<ApiResponse<String>> {
        logger.info("Signup attempt for email: ${signUpRequest.email}")
        
        val result = userService.registerUser(signUpRequest)
        
        return if (result.success) {
            logger.info("Signup successful for email: ${signUpRequest.email}")
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } else {
            logger.warn("Signup failed for email: ${signUpRequest.email} - ${result.message}")
            ResponseEntity.badRequest().body(result)
        }
    }
    
    /**
     * Email verification endpoint
     * POST /api/auth/verify
     */
    @PostMapping("/verify")
    fun verify(@Valid @RequestBody verificationRequest: VerificationRequest): ResponseEntity<ApiResponse<String>> {
        logger.info("Email verification attempt")
        
        val result = userService.verifyUser(verificationRequest)
        
        return if (result.success) {
            logger.info("Email verification successful")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Email verification failed - ${result.message}")
            ResponseEntity.badRequest().body(result)
        }
    }
    
    /**
     * Email verification endpoint via GET (for email links)
     * GET /api/auth/verify?token=...
     */
    @GetMapping("/verify")
    fun verifyByToken(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        logger.info("Email verification attempt via GET")
        
        val verificationRequest = VerificationRequest(token)
        val result = userService.verifyUser(verificationRequest)
        
        return if (result.success) {
            logger.info("Email verification successful via GET")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Email verification failed via GET - ${result.message}")
            ResponseEntity.badRequest().body(result)
        }
    }
    
    /**
     * Password reset request endpoint
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody passwordResetRequest: PasswordResetRequest): ResponseEntity<ApiResponse<String>> {
        logger.info("Password reset request for email: ${passwordResetRequest.email}")
        
        val result = userService.requestPasswordReset(passwordResetRequest)
        
        // Always return success for security reasons (don't reveal if email exists)
        logger.info("Password reset request processed for email: ${passwordResetRequest.email}")
        return ResponseEntity.ok(result)
    }
    
    /**
     * Password reset confirmation endpoint
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody resetPasswordRequest: ResetPasswordRequest): ResponseEntity<ApiResponse<String>> {
        logger.info("Password reset confirmation attempt")
        
        val result = userService.resetPassword(resetPasswordRequest)
        
        return if (result.success) {
            logger.info("Password reset successful")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Password reset failed - ${result.message}")
            ResponseEntity.badRequest().body(result)
        }
    }
    
    /**
     * Google OAuth2 login endpoint
     * POST /api/auth/google-login
     */
    @PostMapping("/google-login")
    fun googleLogin(@Valid @RequestBody googleAuthRequest: GoogleAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Google OAuth login attempt")
        
        val result = userService.authenticateWithGoogle(googleAuthRequest)
        
        return if (result.success) {
            logger.info("Google OAuth login successful")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Google OAuth login failed - ${result.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }
    
    /**
     * Facebook OAuth2 login endpoint
     * POST /api/auth/facebook-login
     */
    @PostMapping("/facebook-login")
    fun facebookLogin(@Valid @RequestBody facebookAuthRequest: FacebookAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        logger.info("Facebook OAuth login attempt")
        
        val result = userService.authenticateWithFacebook(facebookAuthRequest)
        
        return if (result.success) {
            logger.info("Facebook OAuth login successful")
            ResponseEntity.ok(result)
        } else {
            logger.warn("Facebook OAuth login failed - ${result.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }
    
    /**
     * Refresh token endpoint (if needed for future implementation)
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<ApiResponse<String>> {
        logger.info("Token refresh attempt")
        
        // This is a placeholder for future refresh token implementation
        // Currently, the JWT tokens are stateless and don't require refresh
        return ResponseEntity.ok(
            ApiResponse(
                success = false,
                message = "Refresh token functionality not implemented yet"
            )
        )
    }
    
    /**
     * Logout endpoint (for future implementation with token blacklisting)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authHeader: String): ResponseEntity<ApiResponse<String>> {
        logger.info("Logout attempt")
        
        // This is a placeholder for future logout implementation
        // Currently, JWT tokens are stateless, so logout is handled client-side
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Logout successful. Please remove the token from client storage."
            )
        )
    }
}