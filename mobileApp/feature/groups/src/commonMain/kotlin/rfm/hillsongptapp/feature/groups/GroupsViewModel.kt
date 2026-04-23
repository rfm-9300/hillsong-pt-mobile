package rfm.hillsongptapp.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.GroupsApiService
import rfm.hillsongptapp.core.network.api.Ministry
import rfm.hillsongptapp.core.network.result.NetworkResult

class GroupsViewModel(
    private val groupsApiService: GroupsApiService,
    val baseUrl: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (
                val result = groupsApiService.getGroups(
                    ministry = _uiState.value.selectedMinistry,
                    city = _uiState.value.selectedCity,
                    query = _uiState.value.query.takeIf { it.isNotBlank() },
                )
            ) {
                is NetworkResult.Success -> {
                    val groups = result.data.content.filter { it.isActive }
                    _uiState.value = _uiState.value.copy(
                        groups = groups,
                        selectedGroupId = _uiState.value.selectedGroupId?.takeIf { selectedId ->
                            groups.any { it.id == selectedId }
                        } ?: groups.firstOrNull()?.id,
                        isLoading = false,
                        error = null,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message,
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun selectMinistry(ministry: Ministry?) {
        _uiState.value = _uiState.value.copy(selectedMinistry = ministry)
        load()
    }

    fun selectCity(city: String?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        load()
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        load()
    }

    fun selectGroup(groupId: String) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }
}
