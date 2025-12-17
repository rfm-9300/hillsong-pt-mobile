package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Calendar API service handling all calendar-related network operations
 */
interface CalendarApiService {
    suspend fun getEventsForMonth(month: Int, year: Int): NetworkResult<CalendarEventsResponse>
    suspend fun getEventById(eventId: Long): NetworkResult<CalendarEventResponse>

    // Reactive streams
    fun getEventsForMonthStream(month: Int, year: Int): Flow<NetworkResult<CalendarEventsResponse>>
}

/**
 * Implementation of CalendarApiService using Ktor HTTP client
 */
class CalendarApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), CalendarApiService {

    override suspend fun getEventsForMonth(month: Int, year: Int): NetworkResult<CalendarEventsResponse> {
        return safeGet("api/calendar/events") {
            url {
                parameters.append("month", month.toString())
                parameters.append("year", year.toString())
            }
        }
    }

    override suspend fun getEventById(eventId: Long): NetworkResult<CalendarEventResponse> {
        return safeGet("api/calendar/events/$eventId")
    }

    override fun getEventsForMonthStream(month: Int, year: Int): Flow<NetworkResult<CalendarEventsResponse>> = flow {
        emit(NetworkResult.Loading)
        emit(getEventsForMonth(month, year))
    }
}
