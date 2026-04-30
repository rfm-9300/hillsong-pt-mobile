package rfm.hillsongptapp.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.network.api.AttendanceApiService
import rfm.hillsongptapp.core.network.api.AttendanceResponse
import rfm.hillsongptapp.core.network.api.CheckInByTokenRequest
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

data class EventQrCheckInUiState(
    val isCheckingIn: Boolean = false,
    val checkedInAttendance: AttendanceResponse? = null,
    val error: String? = null,
)

class EventQrCheckInViewModel(
    private val attendanceApiService: AttendanceApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventQrCheckInUiState())
    val uiState: StateFlow<EventQrCheckInUiState> = _uiState.asStateFlow()

    fun checkIn(eventId: String, qrToken: String) {
        if (_uiState.value.isCheckingIn) return

        viewModelScope.launch {
            _uiState.value = EventQrCheckInUiState(isCheckingIn = true)

            val request = CheckInByTokenRequest(
                qrToken = qrToken,
                attendanceType = "EVENT",
                eventId = eventId
            )

            when (val result = attendanceApiService.checkInByToken(request)) {
                is NetworkResult.Success -> _uiState.value = EventQrCheckInUiState(
                    checkedInAttendance = result.data
                )
                is NetworkResult.Error -> _uiState.value = EventQrCheckInUiState(
                    error = result.exception.toCheckInMessage()
                )
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun scanAgain() {
        _uiState.value = EventQrCheckInUiState()
    }

    private fun NetworkException.toCheckInMessage(): String =
        when (this) {
            NetworkException.Unauthorized -> "Precisas de iniciar sessão como staff para fazer check-in."
            is NetworkException.HttpError -> errorMessage
            else -> message ?: "Não foi possível fazer check-in."
        }
}
