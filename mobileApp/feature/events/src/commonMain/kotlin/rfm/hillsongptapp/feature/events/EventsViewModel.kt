package rfm.hillsongptapp.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.Event
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

data class EventsUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class EventsViewModel(
    private val eventsApiService: EventsApiService,
    val baseUrl: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = eventsApiService.getUpcomingEvents()) {
                is NetworkResult.Success -> _uiState.value = EventsUiState(events = result.data)
                is NetworkResult.Error -> _uiState.value = EventsUiState(error = result.exception.message)
                is NetworkResult.Loading -> Unit
            }
        }
    }
}
