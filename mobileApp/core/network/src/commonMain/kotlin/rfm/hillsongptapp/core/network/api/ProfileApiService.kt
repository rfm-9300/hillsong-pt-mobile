package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data classes for Profile API
 */
@kotlinx.serialization.Serializable
data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val phone: String = "",
    val imagePath: String = "",
    val isAdmin: Boolean = false,
    val joinedAt: String = "",
    val qrToken: String? = null,
    // Legacy fields kept for backward compatibility
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val bio: String? = null
)

@kotlinx.serialization.Serializable
data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val bio: String? = null
)

@kotlinx.serialization.Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

@kotlinx.serialization.Serializable
data class ApiSuccessResponse(
    val success: Boolean,
    val message: String
)

/**
 * Profile API service handling all profile-related network operations
 */
interface ProfileApiService {
    suspend fun getProfile(): NetworkResult<UserProfile>
    suspend fun updateProfile(request: UpdateProfileRequest): NetworkResult<UserProfile>
    suspend fun uploadProfileImage(imageData: ByteArray, fileName: String): NetworkResult<String>
    suspend fun changePassword(request: ChangePasswordRequest): NetworkResult<ApiSuccessResponse>
    suspend fun deleteAccount(): NetworkResult<ApiSuccessResponse>
}

/**
 * Implementation of ProfileApiService using Ktor HTTP client
 */
class ProfileApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), ProfileApiService {
    
    override suspend fun getProfile(): NetworkResult<UserProfile> {
        return when (val result = safeGet<ApiResponse<UserProfile>>("api/profile")) {
            is NetworkResult.Success -> result.data.data?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error(rfm.hillsongptapp.core.network.result.NetworkException.UnknownError("No data"))
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }
    
    override suspend fun updateProfile(request: UpdateProfileRequest): NetworkResult<UserProfile> {
        return safePut("api/profile", request)
    }
    
    override suspend fun uploadProfileImage(imageData: ByteArray, fileName: String): NetworkResult<String> {
        // TODO: Implement multipart file upload
        // This would require additional Ktor configuration for multipart data
        return NetworkResult.Error(
            rfm.hillsongptapp.core.network.result.NetworkException.UnknownError(
                "Profile image upload not implemented yet"
            )
        )
    }
    
    override suspend fun changePassword(request: ChangePasswordRequest): NetworkResult<ApiSuccessResponse> {
        return safePost("api/profile/change-password", request)
    }
    
    override suspend fun deleteAccount(): NetworkResult<ApiSuccessResponse> {
        return safeDelete("api/profile")
    }
}