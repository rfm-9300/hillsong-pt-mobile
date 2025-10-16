package rfm.hillsongptapp.feature.home.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.AuthRepository
import rfm.hillsongptapp.core.network.api.Event
import rfm.hillsongptapp.core.network.api.EventsApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

data class HomeUiState(
    val upcomingEvents: List<Event> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val eventsError: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val eventsApiService: EventsApiService
): ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUpcomingEvents()
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvents = true, eventsError = null)
            
            when (val result = eventsApiService.getUpcomingEvents()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        upcomingEvents = result.data.take(5), // Show max 5 events
                        isLoadingEvents = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingEvents = false,
                        eventsError = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoadingEvents = true)
                }
            }
        }
    }
}