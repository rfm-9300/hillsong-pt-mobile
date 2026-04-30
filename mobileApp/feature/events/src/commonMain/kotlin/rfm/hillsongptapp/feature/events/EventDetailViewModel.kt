package rfm.hillsongptapp.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.Event
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.api.UserEventStatusResponse
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

data class EventDetailUiState(
    val event: Event? = null,
    val status: UserEventStatusResponse? = null,
    val isLoading: Boolean = false,
    val isActing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

class EventDetailViewModel(
    private val eventsApiService: EventsApiService,
    val baseUrl: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val eventDeferred = async { eventsApiService.getEventById(eventId) }
            val statusDeferred = async { eventsApiService.getMyStatus(eventId) }

            val eventResult = eventDeferred.await()
            val statusResult = statusDeferred.await()

            val event = (eventResult as? NetworkResult.Success)?.data
            val status = (statusResult as? NetworkResult.Success)?.data
            val error = (eventResult as? NetworkResult.Error)?.exception?.message

            _uiState.value = EventDetailUiState(event = event, status = status, error = error)
        }
    }

    fun joinEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true, successMessage = null, error = null)
            when (val result = eventsApiService.joinEvent(eventId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActing = false,
                        successMessage = result.data.message
                    )
                    load(eventId)
                }
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isActing = false,
                    error = result.exception.toEventActionMessage()
                )
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true, successMessage = null, error = null)
            when (val result = eventsApiService.leaveEvent(eventId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isActing = false,
                        successMessage = result.data.message
                    )
                    load(eventId)
                }
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isActing = false,
                    error = result.exception.toEventActionMessage()
                )
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }

    private fun NetworkException.toEventActionMessage(): String =
        when (this) {
            NetworkException.Unauthorized -> "Precisas de iniciar sessão para te inscreveres neste evento."
            is NetworkException.HttpError -> errorMessage
            else -> message ?: "Não foi possível atualizar a tua inscrição."
        }
}
