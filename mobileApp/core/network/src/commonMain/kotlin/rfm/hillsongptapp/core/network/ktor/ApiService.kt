package rfm.hillsongptapp.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import rfm.hillsongptapp.core.network.ktor.requests.FacebookAuthRequest
import rfm.hillsongptapp.core.network.ktor.requests.GoogleAuthRequest
import rfm.hillsongptapp.core.network.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.network.ktor.requests.PasswordResetRequest
import rfm.hillsongptapp.core.network.ktor.requests.ResetPasswordRequest
import rfm.hillsongptapp.core.network.ktor.requests.SignUpRequest
import rfm.hillsongptapp.core.network.ktor.requests.VerificationRequest
import rfm.hillsongptapp.core.network.ktor.responses.ApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.LoginResponse
import rfm.hillsongptapp.core.network.ktor.responses.PasswordResetResponse
import rfm.hillsongptapp.core.network.ktor.responses.PostListResponse
import rfm.hillsongptapp.core.network.ktor.responses.SignUpResponse
import rfm.hillsongptapp.core.network.ktor.responses.VerificationResponse

class ApiService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
) {
    // Regular login
    suspend fun login(request: LoginRequest): LoginResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            handleNetworkError(e, "Login failed")
        }
    }

    // Google login
    suspend fun googleLogin(request: GoogleAuthRequest): LoginResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/google-login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            handleNetworkError(e, "Google login failed")
        }
    }

    // Facebook login
    suspend fun facebookLogin(request: FacebookAuthRequest): LoginResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/facebook-login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            handleNetworkError(e, "Facebook login failed")
        }
    }

    // Sign up
    suspend fun signUp(request: SignUpRequest): SignUpResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            handleSignUpNetworkError(e, "Sign up failed")
        }
    }

    // Verify email
    suspend fun verifyEmail(request: VerificationRequest): VerificationResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/verify") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            VerificationResponse(
                success = false,
                message = getNetworkErrorMessage(e, "Email verification failed"),
            )
        }
    }

    // Request password reset
    suspend fun requestPasswordReset(request: PasswordResetRequest): PasswordResetResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/request-password-reset") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            PasswordResetResponse(
                success = false,
                message = getNetworkErrorMessage(e, "Password reset request failed"),
            )
        }
    }

    // Reset password
    suspend fun resetPassword(request: ResetPasswordRequest): PasswordResetResponse {
        return try {
            httpClient.post("$baseUrl/api/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            PasswordResetResponse(
                success = false,
                message = getNetworkErrorMessage(e, "Password reset failed"),
            )
        }
    }

    private suspend inline fun <reified T> fetchData(endpoint: String): T =
        httpClient.get("$baseUrl/$endpoint").body()

    suspend fun getPosts(): ApiResponse<PostListResponse> {
        return try {
            httpClient.get("$baseUrl/api/posts").body()
        } catch (e: Exception) {
            // Return a failed response for posts
            ApiResponse(
                success = false,
                message = getNetworkErrorMessage(e),
                data = null
            )
        }
    }

    private fun handleNetworkError(e: Exception, defaultMessage: String): LoginResponse {
        return LoginResponse(
            success = false,
            message = getNetworkErrorMessage(e, defaultMessage),
            data = null
        )
    }

    private fun handleSignUpNetworkError(e: Exception, defaultMessage: String): SignUpResponse {
        return SignUpResponse(
            success = false,
            message = getNetworkErrorMessage(e, defaultMessage),
        )
    }

    private fun getNetworkErrorMessage(e: Exception, defaultMessage: String = "Network error occurred"): String {
        return when (e) {
            is ClientRequestException -> "Request failed: ${e.response.status.description}"
            is ServerResponseException -> "Server error: ${e.response.status.description}"
            else -> {
                // Handle common network errors by checking the exception message
                val message = e.message?.lowercase() ?: ""
                when {
                    message.contains("unknown host") || message.contains("no address associated") -> 
                        "Cannot connect to server. Please check your internet connection."
                    message.contains("connection") && (message.contains("refused") || message.contains("timeout")) -> 
                        "Cannot connect to server. Please try again later."
                    message.contains("timeout") -> 
                        "Connection timeout. Please try again."
                    else -> "$defaultMessage: ${e.message ?: "Unknown error"}"
                }
            }
        }
    }
}
