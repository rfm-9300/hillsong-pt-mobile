package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

@Serializable
data class CheckInByTokenRequest(
    val qrToken: String,
    val attendanceType: String,
    val eventId: String? = null,
    val serviceId: String? = null,
    val kidsServiceId: String? = null,
    val notes: String? = null
)

@Serializable
data class AttendanceUserResponse(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val createdAt: String = "",
) {
    val fullName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { email }
}

@Serializable
data class AttendanceResponse(
    val id: String = "",
    val user: AttendanceUserResponse = AttendanceUserResponse(),
    val attendanceType: String = "",
    val status: String = "",
    val checkInTime: String = "",
    val checkOutTime: String? = null,
    val notes: String? = null,
    val checkedInBy: String? = null,
    val checkedOutBy: String? = null,
    val duration: Long? = null,
    val isCheckedOut: Boolean = false,
)

interface AttendanceApiService {
    suspend fun checkInByToken(request: CheckInByTokenRequest): NetworkResult<AttendanceResponse>
}

class AttendanceApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), AttendanceApiService {

    override suspend fun checkInByToken(request: CheckInByTokenRequest): NetworkResult<AttendanceResponse> {
        return when (val result = safePost<ApiResponse<AttendanceResponse>>("api/attendance/check-in/by-token", request)) {
            is NetworkResult.Success -> result.data.data?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error(NetworkException.UnknownError(result.data.message.ifBlank { "No data" }))
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }
}
