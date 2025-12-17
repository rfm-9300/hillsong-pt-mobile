package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import rfm.com.entity.CalendarEventType
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

// Request DTOs

data class CreateCalendarEventRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,

    val description: String? = null,

    @field:NotNull(message = "Event date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val eventDate: LocalDate,

    @JsonFormat(pattern = "HH:mm")
    val startTime: LocalTime? = null,

    @JsonFormat(pattern = "HH:mm")
    val endTime: LocalTime? = null,

    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String? = null,

    val isAllDay: Boolean = false,

    val eventType: CalendarEventType = CalendarEventType.GENERAL
)

data class UpdateCalendarEventRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String? = null,

    val description: String? = null,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val eventDate: LocalDate? = null,

    @JsonFormat(pattern = "HH:mm")
    val startTime: LocalTime? = null,

    @JsonFormat(pattern = "HH:mm")
    val endTime: LocalTime? = null,

    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String? = null,

    val isAllDay: Boolean? = null,

    val eventType: CalendarEventType? = null
)

// Response DTOs

data class CalendarEventResponse(
    val id: Long,
    val title: String,
    val description: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: String,
    @JsonFormat(pattern = "HH:mm")
    val startTime: String?,
    @JsonFormat(pattern = "HH:mm")
    val endTime: String?,
    val location: String?,
    val imageUrl: String?,
    val isAllDay: Boolean,
    val eventType: CalendarEventType,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime
)

data class CalendarEventsResponse(
    val success: Boolean,
    val message: String,
    val data: List<CalendarEventResponse>? = null
)

data class CalendarEventDetailResponse(
    val success: Boolean,
    val message: String,
    val data: CalendarEventResponse? = null
)

// Summary response for month view (lighter payload)
data class CalendarEventSummary(
    val id: Long,
    val title: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val date: String,
    @JsonFormat(pattern = "HH:mm")
    val startTime: String?,
    @JsonFormat(pattern = "HH:mm")
    val endTime: String?,
    val location: String?,
    val eventType: CalendarEventType
)
