package rfm.hillsongptapp.feature.home.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.network.api.Encounter
import rfm.hillsongptapp.core.network.api.EncountersApiService
import rfm.hillsongptapp.core.network.api.YouTubeVideo
import rfm.hillsongptapp.core.network.api.YouTubeVideosApiService
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.logging.LoggerHelper

data class EncounterWithImageUrl(
    val encounter: Encounter,
    val imageUrl: String?
)

data class HomeUiState(
    val upcomingEncounters: List<EncounterWithImageUrl> = emptyList(),
    val isLoadingEncounters: Boolean = false,
    val encountersError: String? = null,
    val youtubeVideos: List<YouTubeVideo> = emptyList(),
    val isLoadingVideos: Boolean = false,
    val videosError: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val encountersApiService: EncountersApiService,
    private val youtubeVideosApiService: YouTubeVideosApiService,
    private val baseUrl: String
): ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUpcomingEncounters()
        loadYouTubeVideos()
    }

    private fun loadUpcomingEncounters() {
        viewModelScope.launch {
            rfm.hillsongptapp.logging.LoggerHelper.logDebug("Loading upcoming encounters", "HomeViewModel")
            rfm.hillsongptapp.logging.LoggerHelper.logDebug("Base URL: $baseUrl", "HomeViewModel")
            
            _uiState.value = _uiState.value.copy(isLoadingEncounters = true, encountersError = null)
            
            when (val result = encountersApiService.getUpcomingEncounters()) {
                is NetworkResult.Success -> {
                    rfm.hillsongptapp.logging.LoggerHelper.logDebug("Received ${result.data.size} encounters", "HomeViewModel")
                    
                    val encountersWithUrls = result.data.take(5).map { encounter ->
                        val imageUrl = encounter.imagePath?.let { "$baseUrl/api/files/$it" }
                        rfm.hillsongptapp.logging.LoggerHelper.logDebug(
                            "Encounter: ${encounter.title}, imagePath: ${encounter.imagePath}, constructed URL: $imageUrl",
                            "HomeViewModel"
                        )
                        EncounterWithImageUrl(
                            encounter = encounter,
                            imageUrl = imageUrl
                        )
                    }
                    
                    LoggerHelper.logDebug("Mapped ${encountersWithUrls.size} encounters with URLs", "HomeViewModel")
                    
                    _uiState.value = _uiState.value.copy(
                        upcomingEncounters = encountersWithUrls,
                        isLoadingEncounters = false
                    )
                }
                is NetworkResult.Error -> {
                    LoggerHelper.logDebug("Error loading encounters: ${result.exception.message}", "HomeViewModel")
                    _uiState.value = _uiState.value.copy(
                        isLoadingEncounters = false,
                        encountersError = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoadingEncounters = true)
                }
            }
        }
    }
    
    private fun loadYouTubeVideos() {
        viewModelScope.launch {
            LoggerHelper.logDebug("Loading YouTube videos", "HomeViewModel")
            
            _uiState.value = _uiState.value.copy(isLoadingVideos = true, videosError = null)
            
            when (val result = youtubeVideosApiService.getActiveVideos()) {
                is NetworkResult.Success -> {
                    LoggerHelper.logDebug("Received ${result.data.size} YouTube videos", "HomeViewModel")
                    
                    _uiState.value = _uiState.value.copy(
                        youtubeVideos = result.data,
                        isLoadingVideos = false
                    )
                }
                is NetworkResult.Error -> {
                    LoggerHelper.logDebug("Error loading YouTube videos: ${result.exception.message}", "HomeViewModel")
                    _uiState.value = _uiState.value.copy(
                        isLoadingVideos = false,
                        videosError = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoadingVideos = true)
                }
            }
        }
    }
}