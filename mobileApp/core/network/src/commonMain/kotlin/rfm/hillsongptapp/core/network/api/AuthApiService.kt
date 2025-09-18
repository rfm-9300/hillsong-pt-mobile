package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.ktor.requests.FacebookAuthRequest
import rfm.hillsongptapp.core.network.ktor.requests.GoogleAuthRequest
import rfm.hillsongptapp.core.network.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.network.ktor.requests.PasswordResetRequest
import rfm.hillsongptapp.core.network.ktor.requests.ResetPasswordRequest
import rfm.hillsongptapp.core.network.ktor.requests.SignUpRequest
import rfm.hillsongptapp.core.network.ktor.requests.VerificationRequest
import rfm.hillsongptapp.core.network.ktor.responses.LoginResponse
import rfm.hillsongptapp.core.network.ktor.responses.PasswordResetResponse
import rfm.hillsongptapp.core.network.ktor.responses.SignUpResponse
import rfm.hillsongptapp.core.network.ktor.responses.VerificationResponse
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Authentication API service handling all auth-related network operations
 * Follows modern Android architecture patterns with proper error handling
 */
interface AuthApiService {
    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse>
    suspend fun googleLogin(request: GoogleAuthRequest): NetworkResult<LoginResponse>
    suspend fun facebookLogin(request: FacebookAuthRequest): NetworkResult<LoginResponse>
    suspend fun signUp(request: SignUpRequest): NetworkResult<SignUpResponse>
    suspend fun verifyEmail(request: VerificationRequest): NetworkResult<VerificationResponse>
    suspend fun requestPasswordReset(request: PasswordResetRequest): NetworkResult<PasswordResetResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): NetworkResult<PasswordResetResponse>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun refreshToken(): NetworkResult<LoginResponse>
}

/**
 * Implementation of AuthApiService using Ktor HTTP client
 */
class AuthApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), AuthApiService {
    
    override suspend fun login(request: LoginRequest): NetworkResult<LoginResponse> {
        return safePost("api/auth/login", request)
    }
    
    override suspend fun googleLogin(request: GoogleAuthRequest): NetworkResult<LoginResponse> {
        return safePost("api/auth/google-login", request)
    }
    
    override suspend fun facebookLogin(request: FacebookAuthRequest): NetworkResult<LoginResponse> {
        return safePost("api/auth/facebook-login", request)
    }
    
    override suspend fun signUp(request: SignUpRequest): NetworkResult<SignUpResponse> {
        return safePost("api/auth/signup", request)
    }
    
    override suspend fun verifyEmail(request: VerificationRequest): NetworkResult<VerificationResponse> {
        return safePost("api/auth/verify", request)
    }
    
    override suspend fun requestPasswordReset(request: PasswordResetRequest): NetworkResult<PasswordResetResponse> {
        return safePost("api/auth/request-password-reset", request)
    }
    
    override suspend fun resetPassword(request: ResetPasswordRequest): NetworkResult<PasswordResetResponse> {
        return safePost("api/auth/reset-password", request)
    }
    
    override suspend fun logout(): NetworkResult<Unit> {
        return safePost("api/auth/logout")
    }
    
    override suspend fun refreshToken(): NetworkResult<LoginResponse> {
        return safePost("api/auth/refresh")
    }
}