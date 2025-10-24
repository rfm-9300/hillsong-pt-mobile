package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data classes for Encounters API
 */
@kotlinx.serialization.Serializable
data class Encounter(
    val id: Long,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val organizerName: String,
    val organizerId: Long,
    val imagePath: String? = null,
    val createdAt: String
)

@kotlinx.serialization.Serializable
data class EncountersPageResponse(
    val content: List<Encounter>,
    val totalPages: Int,
    val totalElements: Long,
    val number: Int,
    val size: Int
)

@kotlinx.serialization.Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

/**
 * Encounters API service handling all encounter-related network operations
 */
interface EncountersApiService {
    suspend fun getEncounters(page: Int = 0, size: Int = 20, sortBy: String = "date", sortDir: String = "asc"): NetworkResult<EncountersPageResponse>
    suspend fun getEncounter(encounterId: Long): NetworkResult<Encounter>
    suspend fun getUpcomingEncounters(): NetworkResult<List<Encounter>>
    suspend fun getMyEncounters(page: Int = 0, size: Int = 20): NetworkResult<EncountersPageResponse>
    suspend fun searchEncounters(query: String): NetworkResult<List<Encounter>>
    
    // Reactive streams
    fun getEncountersStream(): Flow<NetworkResult<EncountersPageResponse>>
}

/**
 * Implementation of EncountersApiService using Ktor HTTP client
 */
class EncountersApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), EncountersApiService {
    
    override suspend fun getEncounters(page: Int, size: Int, sortBy: String, sortDir: String): NetworkResult<EncountersPageResponse> {
        return safeGet<ApiResponse<EncountersPageResponse>>("api/encounters") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                parameters.append("sortBy", sortBy)
                parameters.append("sortDir", sortDir)
            }
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(Exception("No data in response") as NetworkException)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
    
    override suspend fun getEncounter(encounterId: Long): NetworkResult<Encounter> {
        return safeGet<ApiResponse<Encounter>>("api/encounters/$encounterId").let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(Exception("No data in response") as NetworkException)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
    
    override suspend fun getUpcomingEncounters(): NetworkResult<List<Encounter>> {
        return safeGet<ApiResponse<List<Encounter>>>("api/encounters/upcoming").let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(NetworkException.UnknownError("No data in response"))
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
    
    override suspend fun getMyEncounters(page: Int, size: Int): NetworkResult<EncountersPageResponse> {
        return safeGet<ApiResponse<EncountersPageResponse>>("api/encounters/my-encounters") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(Exception("No data in response") as NetworkException)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
    
    override suspend fun searchEncounters(query: String): NetworkResult<List<Encounter>> {
        return safeGet<ApiResponse<List<Encounter>>>("api/encounters/search") {
            url {
                parameters.append("query", query)
            }
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(Exception("No data in response") as NetworkException)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
    
    override fun getEncountersStream(): Flow<NetworkResult<EncountersPageResponse>> = flow {
        emit(NetworkResult.Loading)
        emit(getEncounters())
    }
}
