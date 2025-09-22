package rfm.hillsongptapp.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
// Remove datetime imports for now
import rfm.hillsongptapp.core.data.repository.PostRepository
import rfm.hillsongptapp.core.data.repository.PostResult
import rfm.hillsongptapp.core.data.repository.PostResponse
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * UI model for feed items with additional computed properties
 */
data class FeedItem(
    val id: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val author: AuthorInfo,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean,
    val publishedDate: String,
    val timeAgo: String
)

data class AuthorInfo(
    val id: Long,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)

/**
 * UI state for the feed screen
 */
data class FeedUiState(
    val title: String = "Feed",
    val feedItems: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
    val searchQuery: String = "",
    val isSearching: Boolean = false
)

/**
 * Modern FeedViewModel with comprehensive state management
 */
class FeedViewModel(
    private val postRepository: PostRepository,
    private val authRepository: rfm.hillsongptapp.core.data.repository.AuthRepository,
    private val baseUrl: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initializeAndLoadFeed()
    }
    
    private fun initializeAndLoadFeed() {
        viewModelScope.launch {
            LoggerHelper.logDebug("Initializing auth state before loading feed", "FeedInit")
            
            // Initialize auth state first
            authRepository.initializeAuthState()
            
            // Wait a bit for auth state to be ready
            kotlinx.coroutines.delay(100)
            
            // Check if user is authenticated
            val isAuthenticated = authRepository.isUserAuthenticated()
            LoggerHelper.logDebug("User authenticated: $isAuthenticated", "FeedInit")
            
            if (isAuthenticated) {
                LoggerHelper.logDebug("User is authenticated, loading feed", "FeedInit")
                loadFeed()
            } else {
                LoggerHelper.logDebug("User is NOT authenticated", "FeedInit")
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Please log in to view posts"
                    ) 
                }
            }
        }
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.Refresh -> refreshFeed()
            is FeedEvent.LoadMore -> loadMorePosts()
            is FeedEvent.LikePost -> togglePostLike(event.postId)
            is FeedEvent.Search -> searchPosts(event.query)
            is FeedEvent.ClearSearch -> clearSearch()
            is FeedEvent.RetryLoad -> loadFeed()
            is FeedEvent.ErrorDismissed -> dismissError()
        }
    }

    private fun loadFeed() {
        viewModelScope.launch {
            LoggerHelper.logDebug("loadFeed() called", "FeedLoad")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Double-check authentication before making API call
            val isAuthenticated = authRepository.isUserAuthenticated()
            LoggerHelper.logDebug("About to call API, authenticated: $isAuthenticated", "FeedLoad")
            
            when (val result = postRepository.getPosts(page = 0, size = 20)) {
                is PostResult.Success -> {
                    LoggerHelper.logDebug("PostRepository returned ${result.data.posts.size} posts", "FeedLoad")
                    val feedItems = result.data.posts.map { it.toFeedItem() }
                    LoggerHelper.logDebug("Converted to ${feedItems.size} feed items", "FeedLoad")
                    _uiState.update { 
                        it.copy(
                            feedItems = feedItems,
                            isLoading = false,
                            hasMorePages = result.data.hasNext,
                            currentPage = result.data.currentPage
                        )
                    }
                    LoggerHelper.logDebug("UI state updated with ${feedItems.size} items", "FeedLoad")
                }
                is PostResult.Error -> {
                    LoggerHelper.logDebug("PostRepository error: ${result.message}", "FeedLoad")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is PostResult.NetworkError -> {
                    LoggerHelper.logDebug("PostRepository network error: ${result.message}", "FeedLoad")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Network error: ${result.message}"
                        )
                    }
                }
                is PostResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }

    private fun refreshFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            
            when (val result = postRepository.getPosts(page = 0, size = 20)) {
                is PostResult.Success -> {
                    val feedItems = result.data.posts.map { it.toFeedItem() }
                    _uiState.update { 
                        it.copy(
                            feedItems = feedItems,
                            isRefreshing = false,
                            hasMorePages = result.data.hasNext,
                            currentPage = result.data.currentPage
                        )
                    }
                }
                is PostResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            errorMessage = result.message
                        )
                    }
                }
                is PostResult.NetworkError -> {
                    _uiState.update { 
                        it.copy(
                            isRefreshing = false,
                            errorMessage = "Network error: ${result.message}"
                        )
                    }
                }
                is PostResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }

    private fun loadMorePosts() {
        val currentState = _uiState.value
        if (!currentState.hasMorePages || currentState.isLoading) return

        viewModelScope.launch {
            val nextPage = currentState.currentPage + 1
            
            when (val result = postRepository.getPosts(page = nextPage, size = 20)) {
                is PostResult.Success -> {
                    val newFeedItems = result.data.posts.map { it.toFeedItem() }
                    _uiState.update { 
                        it.copy(
                            feedItems = it.feedItems + newFeedItems,
                            hasMorePages = result.data.hasNext,
                            currentPage = result.data.currentPage
                        )
                    }
                }
                is PostResult.Error -> {
                    _uiState.update { 
                        it.copy(errorMessage = result.message)
                    }
                }
                is PostResult.NetworkError -> {
                    _uiState.update { 
                        it.copy(errorMessage = "Network error: ${result.message}")
                    }
                }
                is PostResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    private fun togglePostLike(postId: Long) {
        viewModelScope.launch {
            // Optimistically update UI
            _uiState.update { state ->
                state.copy(
                    feedItems = state.feedItems.map { item ->
                        if (item.id == postId) {
                            item.copy(
                                isLikedByCurrentUser = !item.isLikedByCurrentUser,
                                likeCount = if (item.isLikedByCurrentUser) 
                                    item.likeCount - 1 else item.likeCount + 1
                            )
                        } else item
                    }
                )
            }
            
            // Make API call
            when (val result = postRepository.togglePostLike(postId.toInt())) {
                is PostResult.Error, is PostResult.NetworkError -> {
                    // Revert optimistic update on error
                    _uiState.update { state ->
                        state.copy(
                            feedItems = state.feedItems.map { item ->
                                if (item.id == postId) {
                                    item.copy(
                                        isLikedByCurrentUser = !item.isLikedByCurrentUser,
                                        likeCount = if (item.isLikedByCurrentUser) 
                                            item.likeCount - 1 else item.likeCount + 1
                                    )
                                } else item
                            },
                            errorMessage = when (result) {
                                is PostResult.Error -> result.message
                                is PostResult.NetworkError -> "Network error: ${result.message}"
                                else -> "Unknown error"
                            }
                        )
                    }
                }
                else -> {
                    // Success - optimistic update was correct
                }
            }
        }
    }

    private fun searchPosts(query: String) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    searchQuery = query,
                    isSearching = true,
                    errorMessage = null
                )
            }
            
            when (val result = postRepository.searchPosts(query)) {
                is PostResult.Success -> {
                    val feedItems = result.data.map { it.toFeedItem() }
                    _uiState.update { 
                        it.copy(
                            feedItems = feedItems,
                            isSearching = false
                        )
                    }
                }
                is PostResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isSearching = false,
                            errorMessage = result.message
                        )
                    }
                }
                is PostResult.NetworkError -> {
                    _uiState.update { 
                        it.copy(
                            isSearching = false,
                            errorMessage = "Network error: ${result.message}"
                        )
                    }
                }
                is PostResult.Loading -> {
                    // Loading state already set
                }
            }
        }
    }

    private fun clearSearch() {
        _uiState.update { 
            it.copy(
                searchQuery = "",
                isSearching = false
            )
        }
        loadFeed() // Reload original feed
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Extension function to convert PostResponse to FeedItem
     */
    private fun PostResponse.toFeedItem(): FeedItem {
        return FeedItem(
            id = this.id,
            title = this.title,
            content = this.content,
            imageUrl = this.headerImagePath?.let { 
                "$baseUrl/api/files/$it" 
            },
            author = AuthorInfo(
                id = this.author.id,
                fullName = this.author.fullName,
                email = this.author.email,
                avatarUrl = this.author.imagePath?.let { 
                    "$baseUrl/api/files/$it" 
                }
            ),
            likeCount = this.likeCount,
            commentCount = this.commentCount,
            isLikedByCurrentUser = this.isLikedByCurrentUser,
            publishedDate = this.date,
            timeAgo = calculateTimeAgo(this.date)
        )
    }

    /**
     * Calculate time ago from ISO date string (simplified)
     */
    private fun calculateTimeAgo(dateString: String): String {
        // For now, just return a simple format
        // TODO: Implement proper time calculation when kotlinx-datetime is available
        return try {
            // Extract date part for simple display
            if (dateString.contains("T")) {
                val datePart = dateString.split("T")[0]
                datePart
            } else {
                dateString
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

/**
 * Events that can be triggered from the UI
 */
sealed class FeedEvent {
    data object Refresh : FeedEvent()
    data object LoadMore : FeedEvent()
    data class LikePost(val postId: Long) : FeedEvent()
    data class Search(val query: String) : FeedEvent()
    data object ClearSearch : FeedEvent()
    data object RetryLoad : FeedEvent()
    data object ErrorDismissed : FeedEvent()
}
