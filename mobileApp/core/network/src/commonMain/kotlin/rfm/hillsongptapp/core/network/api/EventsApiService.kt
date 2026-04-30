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

@kotlinx.serialization.Serializable
data class EventActionResponse(
    val success: Boolean,
    val message: String,
    val eventId: String,
    val userId: String,
    val currentStatus: String
)

@kotlinx.serialization.Serializable
data class UserEventStatusResponse(
    val eventId: String,
    val userId: String,
    val status: String,
    val canJoin: Boolean,
    val canLeave: Boolean
)

interface EventsApiService {
    suspend fun getUpcomingEvents(): NetworkResult<List<Event>>
    suspend fun getEvents(page: Int = 0, size: Int = 20): NetworkResult<EventsPageResponse>
    suspend fun getEventById(id: String): NetworkResult<Event>
    suspend fun joinEvent(id: String): NetworkResult<EventActionResponse>
    suspend fun leaveEvent(id: String): NetworkResult<EventActionResponse>
    suspend fun getMyStatus(id: String): NetworkResult<UserEventStatusResponse>
    suspend fun getAttending(): NetworkResult<List<Event>>
}

class EventsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), EventsApiService {

    private fun <T> unwrap(result: NetworkResult<ApiResponse<T>>): NetworkResult<T> = when (result) {
        is NetworkResult.Success -> result.data.data?.let { NetworkResult.Success(it) }
            ?: NetworkResult.Error(rfm.hillsongptapp.core.network.result.NetworkException.UnknownError("No data"))
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun getUpcomingEvents(): NetworkResult<List<Event>> =
        unwrap(safeGet("api/events/upcoming"))

    override suspend fun getEvents(page: Int, size: Int): NetworkResult<EventsPageResponse> =
        unwrap(safeGet("api/events") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                parameters.append("sortBy", "date")
                parameters.append("sortDir", "asc")
            }
        })

    override suspend fun getEventById(id: String): NetworkResult<Event> =
        unwrap(safeGet("api/events/$id"))

    override suspend fun joinEvent(id: String): NetworkResult<EventActionResponse> =
        unwrap(safePost("api/events/$id/join"))

    override suspend fun leaveEvent(id: String): NetworkResult<EventActionResponse> =
        unwrap(safePost("api/events/$id/leave"))

    override suspend fun getMyStatus(id: String): NetworkResult<UserEventStatusResponse> =
        unwrap(safeGet("api/events/$id/status"))

    override suspend fun getAttending(): NetworkResult<List<Event>> =
        unwrap(safeGet("api/events/attending"))
}
