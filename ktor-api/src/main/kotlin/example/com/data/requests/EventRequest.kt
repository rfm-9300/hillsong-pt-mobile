package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class EventRequest(
    val eventId: Int
)