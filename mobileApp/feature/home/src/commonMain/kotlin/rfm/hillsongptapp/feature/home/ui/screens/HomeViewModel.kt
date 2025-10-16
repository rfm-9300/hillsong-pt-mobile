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
import rfm.hillsongptapp.core.network.result.NetworkResult

data class HomeUiState(
    val upcomingEncounters: List<Encounter> = emptyList(),
    val isLoadingEncounters: Boolean = false,
    val encountersError: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val encountersApiService: EncountersApiService
): ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadUpcomingEncounters()
    }

    private fun loadUpcomingEncounters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEncounters = true, encountersError = null)
            
            when (val result = encountersApiService.getUpcomingEncounters()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        upcomingEncounters = result.data.take(5), // Show max 5 encounters
                        isLoadingEncounters = false
                    )
                }
                is NetworkResult.Error -> {
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
}