package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.*
import rfm.com.service.UserService
import java.time.LocalDateTime

@WebMvcTest(AuthController::class)
@ContextConfiguration(classes = [AuthController::class])
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `login should return success response when credentials are valid`() {
        // Given
        val authRequest = AuthRequest(
            email = "test@example.com",
            password = "password123"
        )
        
        val userResponse = UserResponse(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            verified = true,
            createdAt = LocalDateTime.now(),
            authProvider = "LOCAL"
        )
        
        val authResponse = AuthResponse(
            token = "jwt-token",
            user = userResponse
        )
        
        val apiResponse = ApiResponse(
            success = true,
            message = "Authentication successful",
            data = authResponse
        )

        every { userService.authenticateUser(authRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Authentication successful"))
        .andExpect(jsonPath("$.data.token").value("jwt-token"))
        .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
    }

    @Test
    fun `login should return unauthorized when credentials are invalid`() {
        // Given
        val authRequest = AuthRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )
        
        val apiResponse = ApiResponse<AuthResponse>(
            success = false,
            message = "Invalid email or password"
        )

        every { userService.authenticateUser(authRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest))
        )
        .andExpect(status().isUnauthorized)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid email or password"))
    }

    @Test
    fun `signup should return created status when registration is successful`() {
        // Given
        val signUpRequest = SignUpRequest(
            email = "newuser@example.com",
            password = "password123",
            confirmPassword = "password123",
            firstName = "Jane",
            lastName = "Doe"
        )
        
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "Registration successful. Please check your email to verify your account."
        )

        every { userService.registerUser(signUpRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."))
    }

    @Test
    fun `verify should return success when token is valid`() {
        // Given
        val verificationRequest = VerificationRequest(token = "valid-token")
        
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "Email verified successfully"
        )

        every { userService.verifyUser(verificationRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Email verified successfully"))
    }

    @Test
    fun `forgotPassword should always return success for security`() {
        // Given
        val passwordResetRequest = PasswordResetRequest(email = "test@example.com")
        
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "If the email exists, a reset link has been sent"
        )

        every { userService.requestPasswordReset(passwordResetRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("If the email exists, a reset link has been sent"))
    }

    @Test
    fun `resetPassword should return success when token is valid`() {
        // Given
        val resetPasswordRequest = ResetPasswordRequest(
            token = "valid-reset-token",
            newPassword = "newpassword123"
        )
        
        val apiResponse = ApiResponse<String>(
            success = true,
            message = "Password reset successfully"
        )

        every { userService.resetPassword(resetPasswordRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Password reset successfully"))
    }

    @Test
    fun `googleLogin should return success when Google token is valid`() {
        // Given
        val googleAuthRequest = GoogleAuthRequest(idToken = "valid-google-token")
        
        val userResponse = UserResponse(
            id = 1L,
            email = "google@example.com",
            firstName = "Google",
            lastName = "User",
            verified = true,
            createdAt = LocalDateTime.now(),
            authProvider = "GOOGLE"
        )
        
        val authResponse = AuthResponse(
            token = "jwt-token",
            user = userResponse
        )
        
        val apiResponse = ApiResponse(
            success = true,
            message = "Google authentication successful",
            data = authResponse
        )

        every { userService.authenticateWithGoogle(googleAuthRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(googleAuthRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Google authentication successful"))
        .andExpect(jsonPath("$.data.user.authProvider").value("GOOGLE"))
    }

    @Test
    fun `facebookLogin should return success when Facebook token is valid`() {
        // Given
        val facebookAuthRequest = FacebookAuthRequest(accessToken = "valid-facebook-token")
        
        val userResponse = UserResponse(
            id = 1L,
            email = "facebook@example.com",
            firstName = "Facebook",
            lastName = "User",
            verified = true,
            createdAt = LocalDateTime.now(),
            authProvider = "FACEBOOK"
        )
        
        val authResponse = AuthResponse(
            token = "jwt-token",
            user = userResponse
        )
        
        val apiResponse = ApiResponse(
            success = true,
            message = "Facebook authentication successful",
            data = authResponse
        )

        every { userService.authenticateWithFacebook(facebookAuthRequest) } returns apiResponse

        // When & Then
        mockMvc.perform(
            post("/api/auth/facebook-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(facebookAuthRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Facebook authentication successful"))
        .andExpect(jsonPath("$.data.user.authProvider").value("FACEBOOK"))
    }
}