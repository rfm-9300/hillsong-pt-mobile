package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data class for YouTube Video
 */
@kotlinx.serialization.Serializable
data class YouTubeVideo(
    val id: Long,
    val title: String,
    val description: String?,
    val videoUrl: String,
    val thumbnailUrl: String,
    val displayOrder: Int,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String?
)

/**
 * YouTube Videos API service handling all YouTube video-related network operations
 */
interface YouTubeVideosApiService {
    suspend fun getActiveVideos(): NetworkResult<List<YouTubeVideo>>
}

/**
 * Implementation of YouTubeVideosApiService using Ktor HTTP client
 */
class YouTubeVideosApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), YouTubeVideosApiService {
    
    override suspend fun getActiveVideos(): NetworkResult<List<YouTubeVideo>> {
        return safeGet<ApiResponse<List<YouTubeVideo>>>("api/youtube-videos/active").let { result ->
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
}
