package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data classes for Events API
 */
@kotlinx.serialization.Serializable
data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val startDateTime: String,
    val endDateTime: String,
    val location: String,
    val imageUrl: String? = null,
    val category: String,
    val isRegistrationRequired: Boolean,
    val maxAttendees: Int? = null,
    val currentAttendees: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

@kotlinx.serialization.Serializable
data class EventsResponse(
    val events: List<Event>,
    val totalCount: Int,
    val hasMore: Boolean
)

@kotlinx.serialization.Serializable
data class EventRegistrationRequest(
    val eventId: Int,
    val notes: String? = null
)

@kotlinx.serialization.Serializable
data class EventRegistrationResponse(
    val success: Boolean,
    val message: String,
    val registrationId: Int? = null
)

/**
 * Events API service handling all event-related network operations
 */
interface EventsApiService {
    suspend fun getEvents(page: Int = 0, limit: Int = 20): NetworkResult<EventsResponse>
    suspend fun getEvent(eventId: Int): NetworkResult<Event>
    suspend fun getUpcomingEvents(): NetworkResult<List<Event>>
    suspend fun getEventsByCategory(category: String): NetworkResult<List<Event>>
    suspend fun registerForEvent(request: EventRegistrationRequest): NetworkResult<EventRegistrationResponse>
    suspend fun unregisterFromEvent(eventId: Int): NetworkResult<EventRegistrationResponse>
    suspend fun getMyRegisteredEvents(): NetworkResult<List<Event>>
    
    // Reactive streams
    fun getEventsStream(): Flow<NetworkResult<EventsResponse>>
}

/**
 * Implementation of EventsApiService using Ktor HTTP client
 */
class EventsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), EventsApiService {
    
    override suspend fun getEvents(page: Int, limit: Int): NetworkResult<EventsResponse> {
        return safeGet("api/events") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }
    }
    
    override suspend fun getEvent(eventId: Int): NetworkResult<Event> {
        return safeGet("api/events/$eventId")
    }
    
    override suspend fun getUpcomingEvents(): NetworkResult<List<Event>> {
        return safeGet("api/events/upcoming")
    }
    
    override suspend fun getEventsByCategory(category: String): NetworkResult<List<Event>> {
        return safeGet("api/events/category/$category")
    }
    
    override suspend fun registerForEvent(request: EventRegistrationRequest): NetworkResult<EventRegistrationResponse> {
        return safePost("api/events/${request.eventId}/register", request)
    }
    
    override suspend fun unregisterFromEvent(eventId: Int): NetworkResult<EventRegistrationResponse> {
        return safeDelete("api/events/$eventId/register")
    }
    
    override suspend fun getMyRegisteredEvents(): NetworkResult<List<Event>> {
        return safeGet("api/events/my-registrations")
    }
    
    override fun getEventsStream(): Flow<NetworkResult<EventsResponse>> = flow {
        emit(NetworkResult.Loading)
        emit(getEvents())
    }
}