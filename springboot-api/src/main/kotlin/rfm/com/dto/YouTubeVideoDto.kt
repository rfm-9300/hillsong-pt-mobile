package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * Request DTO for creating a new YouTube video
 */
data class CreateYouTubeVideoRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    val description: String? = null,
    
    @field:NotBlank(message = "Video URL is required")
    @field:Size(max = 500, message = "Video URL must not exceed 500 characters")
    val videoUrl: String,
    
    @field:NotBlank(message = "Thumbnail URL is required")
    @field:Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    val thumbnailUrl: String,
    
    val displayOrder: Int = 0,
    
    val active: Boolean = true
)

/**
 * Request DTO for updating an existing YouTube video
 */
data class UpdateYouTubeVideoRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must not exceed 255 characters")
    val title: String,
    
    @field:Size(max = 2000, message = "Description must not exceed 2000 characters")
    val description: String? = null,
    
    @field:NotBlank(message = "Video URL is required")
    @field:Size(max = 500, message = "Video URL must not exceed 500 characters")
    val videoUrl: String,
    
    @field:NotBlank(message = "Thumbnail URL is required")
    @field:Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    val thumbnailUrl: String,
    
    val displayOrder: Int = 0,
    
    val active: Boolean = true
)

/**
 * Response DTO for YouTube video information
 */
data class YouTubeVideoResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val videoUrl: String,
    val thumbnailUrl: String,
    val displayOrder: Int,
    val active: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime?
)
