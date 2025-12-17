package rfm.hillsongptapp.core.network.api

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String? = null,
    val date: String,               // YYYY-MM-DD
    val startTime: String? = null,  // HH:mm
    val endTime: String? = null,    // HH:mm
    val location: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class CalendarEventsResponse(
    val success: Boolean,
    val message: String,
    val data: List<CalendarEvent>? = null
)

@Serializable
data class CalendarEventResponse(
    val success: Boolean,
    val message: String,
    val data: CalendarEvent? = null
)
