package example.com.data.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SseAction {
    data class UpdateLike(val postId: Int, val likesCount: Int) : SseAction()
    data object RefreshPosts: SseAction()
    data object RefreshEvents: SseAction()
    data class RefreshEventDetail(val eventId: Int): SseAction()
}

class SseManager {
    companion object SseActions {
        const val UPDATE_LIKE = "update-like"
        const val REFRESH_EVENTS = "refresh-events"
        const val REFRESH_POSTS = "refresh-posts"
        const val REFRESH_EVENT_DETAIL = "refresh-event-detail"
    }

    private val _sseAction = MutableSharedFlow<String>()
    val sseAction = _sseAction.asSharedFlow()

    suspend fun emitEvent(event: SseAction) {
        when(event) {
            is SseAction.UpdateLike -> _sseAction.emit(UPDATE_LIKE)
            is SseAction.RefreshEvents -> _sseAction.emit(REFRESH_EVENTS)
            is SseAction.RefreshPosts -> _sseAction.emit(REFRESH_POSTS)
            is SseAction.RefreshEventDetail -> _sseAction.emit(REFRESH_EVENT_DETAIL)
        }
    }
}

