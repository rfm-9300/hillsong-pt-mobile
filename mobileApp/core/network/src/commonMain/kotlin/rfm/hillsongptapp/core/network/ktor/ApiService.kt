package rfm.hillsongptapp.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
        return httpClient.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Google login
    suspend fun googleLogin(request: GoogleAuthRequest): LoginResponse {
        return httpClient.post("$baseUrl/api/auth/google-login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Facebook login
    suspend fun facebookLogin(request: FacebookAuthRequest): LoginResponse {
        return httpClient.post("$baseUrl/api/auth/facebook-login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Sign up
    suspend fun signUp(request: SignUpRequest): SignUpResponse {
        return httpClient.post("$baseUrl/api/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Verify email
    suspend fun verifyEmail(request: VerificationRequest): VerificationResponse {
        return httpClient.post("$baseUrl/api/auth/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Request password reset
    suspend fun requestPasswordReset(request: PasswordResetRequest): PasswordResetResponse {
        return httpClient.post("$baseUrl/api/auth/request-password-reset") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // Reset password
    suspend fun resetPassword(request: ResetPasswordRequest): PasswordResetResponse {
        return httpClient.post("$baseUrl/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    private suspend inline fun <reified T> fetchData(endpoint: String): T =
        httpClient.get("$baseUrl/$endpoint").body()

    suspend fun getPosts(): ApiResponse<PostListResponse> {
        return httpClient.get("$baseUrl/api/posts").body()
    }
}
