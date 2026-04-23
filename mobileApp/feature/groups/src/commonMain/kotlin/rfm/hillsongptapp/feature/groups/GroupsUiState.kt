package rfm.hillsongptapp.feature.groups

import rfm.hillsongptapp.core.network.api.GroupSummary
import rfm.hillsongptapp.core.network.api.Ministry

data class GroupsUiState(
    val groups: List<GroupSummary> = emptyList(),
    val selectedMinistry: Ministry? = null,
    val selectedCity: String? = null,
    val query: String = "",
    val selectedGroupId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val selectedGroup: GroupSummary?
        get() = groups.firstOrNull { it.id == selectedGroupId } ?: groups.firstOrNull()
}
