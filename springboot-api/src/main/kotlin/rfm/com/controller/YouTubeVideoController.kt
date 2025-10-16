package rfm.com.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import rfm.com.dto.ApiResponse
import rfm.com.dto.CreateYouTubeVideoRequest
import rfm.com.dto.UpdateYouTubeVideoRequest
import rfm.com.dto.YouTubeVideoResponse
import rfm.com.service.YouTubeVideoService

/**
 * REST controller for YouTube video management operations
 */
@RestController
@RequestMapping("/api/youtube-videos")
class YouTubeVideoController(
    private val youTubeVideoService: YouTubeVideoService
) {
    
    private val logger = LoggerFactory.getLogger(YouTubeVideoController::class.java)
    
    /**
     * Create a new YouTube video (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createVideo(
        @Valid @RequestBody request: CreateYouTubeVideoRequest
    ): ResponseEntity<ApiResponse<YouTubeVideoResponse>> {
        return try {
            logger.info("Creating YouTube video: ${request.title}")
            
            val videoResponse = youTubeVideoService.createVideo(request)
            
            ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    success = true,
                    message = "YouTube video created successfully",
                    data = videoResponse
                )
            )
        } catch (ex: Exception) {
            logger.error("Error creating YouTube video", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "Failed to create YouTube video"
                )
            )
        }
    }
    
    /**
     * Get all active YouTube videos (Public endpoint)
     */
    @GetMapping("/active")
    fun getActiveVideos(): ResponseEntity<ApiResponse<List<YouTubeVideoResponse>>> {
        return try {
            logger.debug("Fetching active YouTube videos")
            
            val videos = youTubeVideoService.getActiveVideos()
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Active YouTube videos retrieved successfully",
                    data = videos
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching active YouTube videos", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<List<YouTubeVideoResponse>>(
                    success = false,
                    message = ex.message ?: "Failed to fetch active YouTube videos"
                )
            )
        }
    }
    
    /**
     * Get all YouTube videos including inactive (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllVideos(): ResponseEntity<ApiResponse<List<YouTubeVideoResponse>>> {
        return try {
            logger.debug("Fetching all YouTube videos")
            
            val videos = youTubeVideoService.getAllVideos()
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "All YouTube videos retrieved successfully",
                    data = videos
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching all YouTube videos", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<List<YouTubeVideoResponse>>(
                    success = false,
                    message = ex.message ?: "Failed to fetch all YouTube videos"
                )
            )
        }
    }
    
    /**
     * Get a specific YouTube video by ID
     */
    @GetMapping("/{id}")
    fun getVideoById(@PathVariable id: Long): ResponseEntity<ApiResponse<YouTubeVideoResponse>> {
        return try {
            logger.debug("Fetching YouTube video with ID: $id")
            
            val video = youTubeVideoService.getVideoById(id)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "YouTube video retrieved successfully",
                    data = video
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("YouTube video not found with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "YouTube video not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error fetching YouTube video with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "Failed to fetch YouTube video"
                )
            )
        }
    }
    
    /**
     * Update an existing YouTube video (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateVideo(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateYouTubeVideoRequest
    ): ResponseEntity<ApiResponse<YouTubeVideoResponse>> {
        return try {
            logger.info("Updating YouTube video with ID: $id")
            
            val videoResponse = youTubeVideoService.updateVideo(id, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "YouTube video updated successfully",
                    data = videoResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("YouTube video not found for update with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "YouTube video not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error updating YouTube video with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "Failed to update YouTube video"
                )
            )
        }
    }
    
    /**
     * Delete a YouTube video (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteVideo(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        return try {
            logger.info("Deleting YouTube video with ID: $id")
            
            youTubeVideoService.deleteVideo(id)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "YouTube video deleted successfully",
                    data = "YouTube video with ID $id has been deleted"
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("YouTube video not found for deletion with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "YouTube video not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error deleting YouTube video with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = ex.message ?: "Failed to delete YouTube video"
                )
            )
        }
    }
    
    /**
     * Toggle active status of a YouTube video (Admin only)
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleActiveStatus(@PathVariable id: Long): ResponseEntity<ApiResponse<YouTubeVideoResponse>> {
        return try {
            logger.info("Toggling active status for YouTube video with ID: $id")
            
            val videoResponse = youTubeVideoService.toggleActiveStatus(id)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "YouTube video active status toggled successfully",
                    data = videoResponse
                )
            )
        } catch (ex: IllegalArgumentException) {
            logger.warn("YouTube video not found for toggle with ID: $id")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "YouTube video not found"
                )
            )
        } catch (ex: Exception) {
            logger.error("Error toggling active status for YouTube video with ID: $id", ex)
            ResponseEntity.badRequest().body(
                ApiResponse<YouTubeVideoResponse>(
                    success = false,
                    message = ex.message ?: "Failed to toggle active status"
                )
            )
        }
    }
}
