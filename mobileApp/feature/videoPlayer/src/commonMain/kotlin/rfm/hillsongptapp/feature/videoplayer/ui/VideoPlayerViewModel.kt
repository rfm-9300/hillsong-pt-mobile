package rfm.hillsongptapp.feature.videoplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.YouTubeVideo
import rfm.hillsongptapp.core.network.api.YouTubeVideosApiService
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * UI State for Video Player Screen
 */
data class VideoPlayerUiState(
    val video: YouTubeVideo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Video Player Screen
 * Handles fetching video details and tracking video views
 */
class VideoPlayerViewModel(
    private val videoId: Long,
    private val videoUrl: String,
    private val youtubeVideosApiService: YouTubeVideosApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoPlayerUiState())
    val uiState: StateFlow<VideoPlayerUiState> = _uiState.asStateFlow()

    init {
        LoggerHelper.setTag("VideoPlayerViewModel")
        loadVideoDetails()
    }

    private fun loadVideoDetails() {
        viewModelScope.launch {
            LoggerHelper.logDebug("Loading video details for ID: $videoId", "VideoPlayerViewModel")

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = youtubeVideosApiService.getVideoById(videoId)) {
                is NetworkResult.Success -> {
                    LoggerHelper.logDebug("Video loaded successfully: ${result.data.title}", "VideoPlayerViewModel")

                    _uiState.value = _uiState.value.copy(
                        video = result.data,
                        isLoading = false
                    )

                    // Track video view (analytics)
                    trackVideoView()
                }
                is NetworkResult.Error -> {
                    LoggerHelper.logDebug("Error loading video: ${result.exception.message}", "VideoPlayerViewModel")

                    // Fallback: create minimal video object from passed URL
                    val fallbackVideo = YouTubeVideo(
                        id = videoId,
                        title = "Video",
                        description = null,
                        videoUrl = videoUrl,
                        thumbnailUrl = "",
                        displayOrder = 0,
                        active = true,
                        createdAt = "",
                        updatedAt = null
                    )

                    _uiState.value = _uiState.value.copy(
                        video = fallbackVideo,
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun trackVideoView() {
        // TODO: Implement analytics tracking
        // This could call an API endpoint to record the view
        LoggerHelper.logDebug("Tracking video view for video ID: $videoId", "VideoPlayerViewModel")
    }

    fun retry() {
        loadVideoDetails()
    }
}
