package rfm.hillsongptapp.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.PostRepository

data class FeedItem(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
)

data class FeedUiState(
    val title: String = "Feed",
    val feedItems: List<FeedItem> = emptyList(),
)

class FeedViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load initial data
        loadFeed()
    }

    fun onEvent(event: FeedEvent) {
        viewModelScope.launch {
            when (event) {
                is FeedEvent.Refresh -> {
                    loadFeed()
                }
            }
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            val posts = postRepository.getPosts()
            val items = posts.map {
                FeedItem(
                    id = it.id,
                    title = it.title,
                    description = it.content,
                    imageUrl = "http://192.168.1.67:8080/resources/uploads/images/" + it.headerImagePath,
                )
            }
            _uiState.update { it.copy(feedItems = items) }
        }
    }
}

sealed class FeedEvent {
    object Refresh : FeedEvent()
}
