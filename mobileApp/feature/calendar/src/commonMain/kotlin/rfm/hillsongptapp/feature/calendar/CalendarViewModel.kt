package rfm.hillsongptapp.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import rfm.hillsongptapp.core.network.api.CalendarApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

class CalendarViewModel(
    private val calendarApiService: CalendarApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEventsForCurrentMonth()
    }

    private fun createInitialState(): CalendarUiState {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return CalendarUiState(
            currentMonth = now.monthNumber,
            currentYear = now.year
        )
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.PreviousMonth -> navigateToPreviousMonth()
            is CalendarUiEvent.NextMonth -> navigateToNextMonth()
            is CalendarUiEvent.SelectDate -> selectDate(event.date)
            is CalendarUiEvent.SelectEvent -> selectEvent(event.eventId)
            is CalendarUiEvent.DismissEventSheet -> dismissEventSheet()
            is CalendarUiEvent.DismissError -> dismissError()
            is CalendarUiEvent.Refresh -> loadEventsForCurrentMonth()
        }
    }

    private fun navigateToPreviousMonth() {
        val currentState = _uiState.value
        val newMonth: Int
        val newYear: Int

        if (currentState.currentMonth == 1) {
            newMonth = 12
            newYear = currentState.currentYear - 1
        } else {
            newMonth = currentState.currentMonth - 1
            newYear = currentState.currentYear
        }

        _uiState.value = currentState.copy(
            currentMonth = newMonth,
            currentYear = newYear,
            selectedDate = null,
            selectedDayEvents = emptyList(),
            showEventSheet = false
        )
        loadEventsForMonth(newMonth, newYear)
    }

    private fun navigateToNextMonth() {
        val currentState = _uiState.value
        val newMonth: Int
        val newYear: Int

        if (currentState.currentMonth == 12) {
            newMonth = 1
            newYear = currentState.currentYear + 1
        } else {
            newMonth = currentState.currentMonth + 1
            newYear = currentState.currentYear
        }

        _uiState.value = currentState.copy(
            currentMonth = newMonth,
            currentYear = newYear,
            selectedDate = null,
            selectedDayEvents = emptyList(),
            showEventSheet = false
        )
        loadEventsForMonth(newMonth, newYear)
    }

    private fun selectDate(date: String) {
        val currentState = _uiState.value
        val eventsForDay = currentState.events[date] ?: emptyList()

        _uiState.value = currentState.copy(
            selectedDate = date,
            selectedDayEvents = eventsForDay,
            showEventSheet = false
        )
    }

    private fun selectEvent(eventId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvent = true)

            when (val result = calendarApiService.getEventById(eventId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        selectedEvent = result.data.data,
                        isLoadingEvent = false
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exception.message,
                        isLoadingEvent = false
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }

    private fun dismissEventSheet() {
        _uiState.value = _uiState.value.copy(
            showEventSheet = false
        )
    }

    private fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun loadEventsForCurrentMonth() {
        val currentState = _uiState.value
        loadEventsForMonth(currentState.currentMonth, currentState.currentYear)
    }

    private fun loadEventsForMonth(month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = calendarApiService.getEventsForMonth(month, year)) {
                is NetworkResult.Success -> {
                    val eventsList = result.data.data ?: emptyList()
                    val eventsMap = eventsList.groupBy { it.date }

                    _uiState.value = _uiState.value.copy(
                        events = eventsMap,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled
                }
            }
        }
    }
}
