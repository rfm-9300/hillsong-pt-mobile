package example.com.routes

import com.auth0.jwt.JWT
import example.com.data.requests.AuthRequest
import example.com.data.db.user.User
import example.com.data.db.user.UserProfile
import example.com.data.db.user.UserRepository
import example.com.data.requests.*
import example.com.data.responses.ApiResponse
import example.com.data.responses.ApiResponseData
import example.com.plugins.Logger
import example.com.security.hashing.HashingService
import example.com.security.hashing.SaltedHash
import example.com.security.token.TokenClaim
import example.com.security.token.TokenConfig
import example.com.security.token.TokenService
import example.com.services.EmailService
import example.com.useCases.AuthUser
import example.com.web.pages.auth.loginPage
import example.com.web.pages.auth.signupPage
import example.com.web.pages.auth.resetPasswordPage
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

const val PASSWORD_MIN_LENGTH = 8


fun Route.loginRoutes(
    hashingService: HashingService,
    userRepository: UserRepository,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    emailService: EmailService
) {
    // Initialize AuthUser service
    val authUser = AuthUser(userRepository)

    /****
     * UI Routes
     ****/

    get(Routes.Ui.Auth.SIGNUP) {
        call.respondHtml(HttpStatusCode.OK) {
            signupPage()
        }
    }

    get(Routes.Ui.Auth.LOGIN) {
        val googleClientId = System.getenv("GOOGLE_CLIENT_ID") ?: ""
        val facebookAppId = System.getenv("FACEBOOK_APP_ID") ?: ""
        
        call.respondHtml(HttpStatusCode.OK) {
            loginPage(googleClientId, facebookAppId)
        }
    }

    get(Routes.Ui.Auth.FORGOT_PASSWORD) {
        call.respondHtml(HttpStatusCode.OK) {
            resetPasswordPage()
        }
    }

    get(Routes.Ui.Auth.RESET_PASSWORD_WITH_TOKEN) {
        val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        Logger.d("Received reset password token: $token")
        call.respondHtml(HttpStatusCode.OK) {
            resetPasswordPage(token)
        }
    }


    /****
     * Api Routes
     ****/

    post(Routes.Api.Auth.LOGIN) {
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull() ?: return@post respondHelper(success = false, message = "Invalid request", call = call)

        val areFieldsEmpty = request.email.isEmpty() || request.password.isEmpty()

        if (areFieldsEmpty) return@post respondHelper(success = false, message = "Email or Password empty", call = call)

        val user = userRepository.getUser(request.email) ?: return@post respondHelper(success = false, message = "User not found", call = call, statusCode = HttpStatusCode.NotFound)

        val isPasswordCorrect = hashingService.verifySaltedHash(
            password = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isPasswordCorrect) return@post respondHelper(success = false, message = "Invalid password", call = call, statusCode = HttpStatusCode.Unauthorized)

        val token = tokenService.generateAuthToken(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        Logger.d("Generated token: $token")
        val decodedJWT = JWT.decode(token)
        Logger.d("Decoded JWT claims: ${decodedJWT.claims}")
        Logger.d("UserID in token: ${decodedJWT.getClaim("userId").asString()}")

        respondHelper(success = true, message = "User logged in", data = ApiResponseData.AuthResponse(token = token), call = call)
    }
    
    // Google Sign-In route
    post(Routes.Api.Auth.GOOGLE_LOGIN) {
        val request = kotlin.runCatching { call.receiveNullable<GoogleAuthRequest>() }.getOrNull() 
            ?: return@post respondHelper(success = false, message = "Invalid request", call = call)
            
        if (request.idToken.isBlank()) {
            return@post respondHelper(success = false, message = "ID token is required", call = call, statusCode = HttpStatusCode.BadRequest)
        }
        
        // Authenticate with Google
        val user = authUser.authenticateGoogleUser(request.idToken)
            ?: return@post respondHelper(success = false, message = "Failed to authenticate with Google", call = call, statusCode = HttpStatusCode.Unauthorized)
            
        // Generate JWT token
        val token = tokenService.generateAuthToken(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )
        
        Logger.d("Generated token for Google user: $token")
        respondHelper(success = true, message = "Google sign-in successful", data = ApiResponseData.AuthResponse(token = token), call = call)
    }

    post(Routes.Api.Auth.SIGNUP){
        Logger.d("Received a request to sign up a new user")
        val request = kotlin.runCatching { call.receiveNullable<SingUpRequest>() }.getOrNull() ?: return@post respondHelper(success = false, message = "Invalid request", call = call, statusCode = HttpStatusCode.BadRequest)
        Logger.d("Request: $request")
        val areFieldsEmpty = request.email.isEmpty() || request.password.isEmpty()
        val isPasswordTooShort = request.password.length < PASSWORD_MIN_LENGTH

        if (areFieldsEmpty || isPasswordTooShort) {
            return@post respondHelper(success = false, message = "Empty fields or weak password", call = call, statusCode = HttpStatusCode.BadRequest)
        }

        if (!request.email.contains('@')) {
            return@post respondHelper(success = false, message = "Invalid email", call = call, statusCode = HttpStatusCode.BadRequest)
        }

        if (request.password != request.confirmPassword) {
            return@post respondHelper(success = false, message = "Passwords do not match", call = call, statusCode = HttpStatusCode.BadRequest)
        }

        

        val user = userRepository.getUser(request.email)
        if (user != null) {
            return@post respondHelper(success = false, message = "User already exists", call = call, statusCode = HttpStatusCode.Conflict)
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val verificationToken = tokenService.generateVerificationToken(
            config = tokenConfig,
            TokenClaim(
                name = "Type",
                value = "Verification"
            )
        )

        val newUser = User(
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            verificationToken = verificationToken,
            profile = UserProfile(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                profileImagePath = "default-user-image.webp",
                isAdmin = false
            )
        )

        // try to add the user to the database
        val isUserAdded = userRepository.addUser(newUser)

        // if the user was not added, return an internal server error
        if (!isUserAdded) {
            return@post call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse(
                    success = false,
                    message = "Failed to add user"
                )
            )
        }

        call.respond(
            HttpStatusCode.Created,
            ApiResponse(
                success = true,
                message = "User added successfully"
            )
        )
    }

    authenticate {
        post(Routes.Api.Auth.VERIFY) {
            val request = kotlin.runCatching { call.receiveNullable<VerificationRequest>() }.getOrNull() ?: return@post respondHelper(success = false, message = "Invalid request", call = call, statusCode = HttpStatusCode.BadRequest)
            val token = request.token

            val userId = getUserIdFromRequestToken(call) ?: return@post
            val user = userRepository.getUserById(userId.toInt()) ?: return@post respondHelper(success = false, message = "User not found", call = call, statusCode = HttpStatusCode.NotFound)

            if (user.verified) {
                return@post respondHelper(success = false, message = "User already verified", call = call, statusCode = HttpStatusCode.Conflict)
            }

            val isTokenValid = user.verificationToken == token

            respondHelper(success = isTokenValid,
                message = if (isTokenValid) "User verified" else "Invalid token",
                call = call,
                statusCode = if (isTokenValid) HttpStatusCode.OK else HttpStatusCode.Unauthorized
            )
        }
    }
    
    // Password reset request
    post(Routes.Api.Auth.REQUEST_PASSWORD_RESET) {
        val request = kotlin.runCatching { call.receiveNullable<PasswordResetRequest>() }.getOrNull() 
            ?: return@post respondHelper(success = false, message = "Invalid request", call = call, statusCode = HttpStatusCode.BadRequest)
        
        val email = request.email
        if (email.isBlank() || !email.contains('@')) {
            return@post respondHelper(success = false, message = "Invalid email", call = call, statusCode = HttpStatusCode.BadRequest)
        }
        
        val user = userRepository.getUser(email)
        if (user == null) {
            // For security reasons, don't reveal if the email exists or not
            return@post respondHelper(success = true, message = "If your email exists, you will receive a password reset link", call = call)
        }
        
        // Generate a unique reset token
        val resetToken = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // 24 hours
        
        val tokenSaved = userRepository.saveResetToken(email, resetToken, expiresAt)
        if (!tokenSaved) {
            return@post respondHelper(success = false, message = "Failed to process request", call = call, statusCode = HttpStatusCode.InternalServerError)
        }
        
        // Send email with reset token
        try {
            // Get base URL from request
            val baseUrl = call.request.origin.scheme + "://" + call.request.host()

            // First respond to the client
            respondHelper(success = true, message = "Password reset link sent", call = call)

            // Then send the email in the background

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    emailService.sendPasswordResetEmail(email, resetToken, baseUrl)
                    Logger.d("Password reset email sent to $email")
                } catch (e: Exception) {
                    Logger.d("Failed to send password reset email: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Logger.d("Failed to setup email sending: ${e.message}")
            respondHelper(success = false, message = "Failed to send password reset email", call = call, statusCode = HttpStatusCode.InternalServerError)
        }
    }
    
    // Reset password with token
    post(Routes.Api.Auth.RESET_PASSWORD) {
        val request = kotlin.runCatching { call.receiveNullable<ResetPasswordRequest>() }.getOrNull() 
            ?: return@post respondHelper(success = false, message = "Invalid request", call = call, statusCode = HttpStatusCode.BadRequest)
        
        val token = request.token
        val newPassword = request.newPassword
        
        if (newPassword.length < PASSWORD_MIN_LENGTH) {
            return@post respondHelper(success = false, message = "Password too short", call = call, statusCode = HttpStatusCode.BadRequest)
        }
        
        // Verify token and get user
        val user = userRepository.getUserByResetToken(token)
        if (user == null) {
            return@post respondHelper(success = false, message = "Invalid or expired token", call = call, statusCode = HttpStatusCode.BadRequest)
        }
        
        // Generate new password hash
        val saltedHash = hashingService.generateSaltedHash(newPassword)
        
        // Update password
        val updated = userRepository.updatePassword(user.id!!, saltedHash.hash, saltedHash.salt)
        if (!updated) {
            return@post respondHelper(success = false, message = "Failed to update password", call = call, statusCode = HttpStatusCode.InternalServerError)
        }
        
        // Delete used token
        userRepository.deleteResetToken(user.id)
        
        respondHelper(success = true, message = "Password has been reset successfully", call = call)
    }

    // Facebook Sign-In route
    post(Routes.Api.Auth.FACEBOOK_LOGIN) {
        val request = kotlin.runCatching { call.receiveNullable<FacebookAuthRequest>() }.getOrNull() 
            ?: return@post respondHelper(success = false, message = "Invalid request", call = call)
            
        if (request.accessToken.isBlank()) {
            return@post respondHelper(success = false, message = "Access token is required", call = call, statusCode = HttpStatusCode.BadRequest)
        }
        
        // Authenticate with Facebook
        val user = authUser.authenticateFacebookUser(request.accessToken)
            ?: return@post respondHelper(success = false, message = "Failed to authenticate with Facebook", call = call, statusCode = HttpStatusCode.Unauthorized)
            
        // Generate JWT token
        val token = tokenService.generateAuthToken(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )
        
        Logger.d("Generated token for Facebook user: $token")
        respondHelper(success = true, message = "Facebook sign-in successful", data = ApiResponseData.AuthResponse(token = token), call = call)
    }
}
