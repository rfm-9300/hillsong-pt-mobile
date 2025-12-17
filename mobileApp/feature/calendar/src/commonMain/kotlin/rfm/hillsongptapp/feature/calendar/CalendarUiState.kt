package rfm.hillsongptapp.feature.calendar

import rfm.hillsongptapp.core.network.api.CalendarEvent

data class CalendarUiState(
    val currentMonth: Int = 1,          // 1-12
    val currentYear: Int = 2024,
    val events: Map<String, List<CalendarEvent>> = emptyMap(), // date (YYYY-MM-DD) -> events
    val selectedDate: String? = null,
    val selectedDayEvents: List<CalendarEvent> = emptyList(),
    val selectedEvent: CalendarEvent? = null,
    val isLoading: Boolean = false,
    val isLoadingEvent: Boolean = false,
    val showEventSheet: Boolean = false,
    val errorMessage: String? = null
)

sealed class CalendarUiEvent {
    data object PreviousMonth : CalendarUiEvent()
    data object NextMonth : CalendarUiEvent()
    data class SelectDate(val date: String) : CalendarUiEvent()
    data class SelectEvent(val eventId: Long) : CalendarUiEvent()
    data object DismissEventSheet : CalendarUiEvent()
    data object DismissError : CalendarUiEvent()
    data object Refresh : CalendarUiEvent()
}
