package rfm.com.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.auth.dto.*
import rfm.com.auth.service.AuthService

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val result = authService.authenticateUser(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<String>> {
        val result = authService.registerUser(request)
        return if (result.success) {
            ResponseEntity.status(HttpStatus.CREATED).body(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }

    @PostMapping("/verify")
    fun verify(@Valid @RequestBody request: VerificationRequest): ResponseEntity<ApiResponse<String>> {
        val result = authService.verifyUser(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }
    
    @GetMapping("/verify")
    fun verifyByToken(@RequestParam token: String): ResponseEntity<ApiResponse<String>> {
        val request = VerificationRequest(token)
        val result = authService.verifyUser(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
             ResponseEntity.badRequest().body(result)
        }
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: PasswordResetRequest): ResponseEntity<ApiResponse<String>> {
        val result = authService.requestPasswordReset(request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<String>> {
        val result = authService.resetPassword(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }
    
    @PostMapping("/google-login")
    fun googleLogin(@Valid @RequestBody request: GoogleAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val result = authService.authenticateWithGoogle(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }

    @PostMapping("/facebook-login")
    fun facebookLogin(@Valid @RequestBody request: FacebookAuthRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val result = authService.authenticateWithFacebook(request)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result)
        }
    }
}
