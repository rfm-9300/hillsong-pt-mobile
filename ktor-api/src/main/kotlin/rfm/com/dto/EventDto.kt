package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * Request DTO for creating a new event
 */
data class CreateEventRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    val description: String,
    
    @field:Future(message = "Event date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    
    @field:NotBlank(message = "Location is required")
    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String,
    
    @field:Min(value = 1, message = "Max attendees must be at least 1")
    @field:Max(value = 10000, message = "Max attendees cannot exceed 10000")
    val maxAttendees: Int,
    
    val needsApproval: Boolean = false
)

/**
 * Request DTO for updating an existing event
 */
data class UpdateEventRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String?,
    
    val description: String?,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime?,
    
    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String?,
    
    @field:Min(value = 1, message = "Max attendees must be at least 1")
    @field:Max(value = 10000, message = "Max attendees cannot exceed 10000")
    val maxAttendees: Int?,
    
    val needsApproval: Boolean?
)

/**
 * Response DTO for event details
 */
data class EventResponse(
    val id: Long,
    val title: String,
    val description: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    val location: String,
    val organizerName: String,
    val organizerId: Long,
    val attendeeCount: Int,
    val maxAttendees: Int,
    val availableSpots: Int,
    val headerImagePath: String?,
    val needsApproval: Boolean,
    val isAtCapacity: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    val attendees: List<UserResponse> = emptyList(),
    val waitingListUsers: List<UserResponse> = emptyList()
)

/**
 * Simplified event response for list views
 */
data class EventSummaryResponse(
    val id: Long,
    val title: String,
    val description: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    val location: String,
    val organizerName: String,
    val organizerId: Long,
    val attendeeCount: Int,
    val maxAttendees: Int,
    val availableSpots: Int,
    val headerImagePath: String?,
    val needsApproval: Boolean,
    val isAtCapacity: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime
)

/**
 * Request DTO for joining an event
 */
data class JoinEventRequest(
    @field:NotNull(message = "Event ID is required")
    val eventId: Long
)

/**
 * Request DTO for approving a user for an event
 */
data class ApproveUserRequest(
    @field:NotNull(message = "Event ID is required")
    val eventId: Long,
    
    @field:NotNull(message = "User ID is required")
    val userId: Long
)

/**
 * Response DTO for event join/approval operations
 */
data class EventActionResponse(
    val success: Boolean,
    val message: String,
    val eventId: Long,
    val userId: Long,
    val currentStatus: EventUserStatus
)

/**
 * Enum representing the status of a user in relation to an event
 */
enum class EventUserStatus {
    NOT_JOINED,
    ATTENDEE,
    WAITING_LIST,
    PENDING_APPROVAL
}

/**
 * Response DTO for user's event status
 */
data class UserEventStatusResponse(
    val eventId: Long,
    val userId: Long,
    val status: EventUserStatus,
    val canJoin: Boolean,
    val canLeave: Boolean
)