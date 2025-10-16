package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDateTime

/**
 * Request DTO for creating a new encounter
 */
data class CreateEncounterRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    val description: String,
    
    @field:Future(message = "Encounter date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    
    @field:NotBlank(message = "Location is required")
    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String
)

/**
 * Request DTO for updating an existing encounter
 */
data class UpdateEncounterRequest(
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String?,
    
    val description: String?,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime?,
    
    @field:Size(max = 255, message = "Location must not exceed 255 characters")
    val location: String?
)

/**
 * Response DTO for encounter details
 */
data class EncounterResponse(
    val id: Long,
    val title: String,
    val description: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val date: LocalDateTime,
    val location: String,
    val organizerName: String,
    val organizerId: Long,
    val imagePath: String?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime
)
