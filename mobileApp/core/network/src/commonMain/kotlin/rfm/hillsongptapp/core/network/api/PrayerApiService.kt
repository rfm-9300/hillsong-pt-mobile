package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data classes for Prayer API
 */
@kotlinx.serialization.Serializable
data class PrayerRequest(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val isAnonymous: Boolean,
    val isAnswered: Boolean = false,
    val prayerCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

@kotlinx.serialization.Serializable
data class CreatePrayerRequest(
    val title: String,
    val description: String,
    val category: String,
    val isAnonymous: Boolean = false
)

@kotlinx.serialization.Serializable
data class PrayerRequestsResponse(
    val prayerRequests: List<PrayerRequest>,
    val totalCount: Int,
    val hasMore: Boolean
)

@kotlinx.serialization.Serializable
data class PrayerResponse(
    val success: Boolean,
    val message: String,
    val prayerRequestId: Int? = null
)

/**
 * Prayer API service handling all prayer-related network operations
 */
interface PrayerApiService {
    suspend fun getPrayerRequests(page: Int = 0, limit: Int = 20): NetworkResult<PrayerRequestsResponse>
    suspend fun getPrayerRequest(requestId: Int): NetworkResult<PrayerRequest>
    suspend fun createPrayerRequest(request: CreatePrayerRequest): NetworkResult<PrayerResponse>
    suspend fun updatePrayerRequest(requestId: Int, request: CreatePrayerRequest): NetworkResult<PrayerResponse>
    suspend fun deletePrayerRequest(requestId: Int): NetworkResult<PrayerResponse>
    suspend fun prayForRequest(requestId: Int): NetworkResult<PrayerResponse>
    suspend fun markAsAnswered(requestId: Int): NetworkResult<PrayerResponse>
    suspend fun getMyPrayerRequests(): NetworkResult<List<PrayerRequest>>
    suspend fun getPrayerRequestsByCategory(category: String): NetworkResult<List<PrayerRequest>>
    
    // Reactive streams
    fun getPrayerRequestsStream(): Flow<NetworkResult<PrayerRequestsResponse>>
}

/**
 * Implementation of PrayerApiService using Ktor HTTP client
 */
class PrayerApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), PrayerApiService {
    
    override suspend fun getPrayerRequests(page: Int, limit: Int): NetworkResult<PrayerRequestsResponse> {
        return safeGet("api/prayer-requests") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }
    }
    
    override suspend fun getPrayerRequest(requestId: Int): NetworkResult<PrayerRequest> {
        return safeGet("api/prayer-requests/$requestId")
    }
    
    override suspend fun createPrayerRequest(request: CreatePrayerRequest): NetworkResult<PrayerResponse> {
        return safePost("api/prayer-requests", request)
    }
    
    override suspend fun updatePrayerRequest(requestId: Int, request: CreatePrayerRequest): NetworkResult<PrayerResponse> {
        return safePut("api/prayer-requests/$requestId", request)
    }
    
    override suspend fun deletePrayerRequest(requestId: Int): NetworkResult<PrayerResponse> {
        return safeDelete("api/prayer-requests/$requestId")
    }
    
    override suspend fun prayForRequest(requestId: Int): NetworkResult<PrayerResponse> {
        return safePost("api/prayer-requests/$requestId/pray")
    }
    
    override suspend fun markAsAnswered(requestId: Int): NetworkResult<PrayerResponse> {
        return safePatch("api/prayer-requests/$requestId/answered")
    }
    
    override suspend fun getMyPrayerRequests(): NetworkResult<List<PrayerRequest>> {
        return safeGet("api/prayer-requests/my-requests")
    }
    
    override suspend fun getPrayerRequestsByCategory(category: String): NetworkResult<List<PrayerRequest>> {
        return safeGet("api/prayer-requests/category/$category")
    }
    
    override fun getPrayerRequestsStream(): Flow<NetworkResult<PrayerRequestsResponse>> = flow {
        emit(NetworkResult.Loading)
        emit(getPrayerRequests())
    }
}