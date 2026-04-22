package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

@kotlinx.serialization.Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val organizerName: String,
    val organizerId: String,
    val attendeeCount: Int,
    val maxAttendees: Int,
    val availableSpots: Int,
    val headerImagePath: String? = null,
    val needsApproval: Boolean,
    val isAtCapacity: Boolean,
    val createdAt: String
)

@kotlinx.serialization.Serializable
data class EventsPageResponse(
    val content: List<Event>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

interface EventsApiService {
    suspend fun getUpcomingEvents(): NetworkResult<List<Event>>
    suspend fun getEvents(page: Int = 0, size: Int = 20): NetworkResult<EventsPageResponse>
}

class EventsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), EventsApiService {

    override suspend fun getUpcomingEvents(): NetworkResult<List<Event>> {
        return safeGet<ApiResponse<List<Event>>>("api/events/upcoming").let { result ->
            when (result) {
                is NetworkResult.Success -> result.data.data?.let { NetworkResult.Success(it) }
                    ?: NetworkResult.Error(rfm.hillsongptapp.core.network.result.NetworkException.UnknownError("No data"))
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    override suspend fun getEvents(page: Int, size: Int): NetworkResult<EventsPageResponse> {
        return safeGet<ApiResponse<EventsPageResponse>>("api/events") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                parameters.append("sortBy", "date")
                parameters.append("sortDir", "asc")
            }
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> result.data.data?.let { NetworkResult.Success(it) }
                    ?: NetworkResult.Error(rfm.hillsongptapp.core.network.result.NetworkException.UnknownError("No data"))
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
}
