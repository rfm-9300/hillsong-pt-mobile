package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.CreateYouTubeVideoRequest
import rfm.com.dto.UpdateYouTubeVideoRequest
import rfm.com.dto.YouTubeVideoResponse
import rfm.com.entity.YouTubeVideo
import rfm.com.repository.YouTubeVideoRepository
import java.time.LocalDateTime

@Service
class YouTubeVideoService(
    private val youTubeVideoRepository: YouTubeVideoRepository
) {
    
    private val logger = LoggerFactory.getLogger(YouTubeVideoService::class.java)
    
    /**
     * Create a new YouTube video
     */
    @Transactional
    fun createVideo(request: CreateYouTubeVideoRequest): YouTubeVideoResponse {
        logger.info("Creating new YouTube video: ${request.title}")
        
        val video = YouTubeVideo(
            title = request.title,
            description = request.description,
            videoUrl = request.videoUrl,
            thumbnailUrl = request.thumbnailUrl,
            displayOrder = request.displayOrder,
            active = request.active
        )
        
        val savedVideo = youTubeVideoRepository.save(video)
        logger.info("YouTube video created with ID: ${savedVideo.id}")
        
        return mapToResponse(savedVideo)
    }
    
    /**
     * Get all active YouTube videos ordered by display order
     */
    @Transactional(readOnly = true)
    fun getActiveVideos(): List<YouTubeVideoResponse> {
        logger.debug("Fetching all active YouTube videos")
        
        val videos = youTubeVideoRepository.findByActiveTrueOrderByDisplayOrderAsc()
        
        return videos.map { mapToResponse(it) }
    }
    
    /**
     * Get all YouTube videos (including inactive)
     */
    @Transactional(readOnly = true)
    fun getAllVideos(): List<YouTubeVideoResponse> {
        logger.debug("Fetching all YouTube videos")
        
        val videos = youTubeVideoRepository.findAll()
            .sortedBy { it.displayOrder }
        
        return videos.map { mapToResponse(it) }
    }
    
    /**
     * Get a specific YouTube video by ID
     */
    @Transactional(readOnly = true)
    fun getVideoById(id: Long): YouTubeVideoResponse {
        logger.debug("Fetching YouTube video with ID: $id")
        
        val video = youTubeVideoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("YouTube video not found with ID: $id") }
        
        return mapToResponse(video)
    }
    
    /**
     * Update an existing YouTube video
     */
    @Transactional
    fun updateVideo(id: Long, request: UpdateYouTubeVideoRequest): YouTubeVideoResponse {
        logger.info("Updating YouTube video with ID: $id")
        
        val video = youTubeVideoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("YouTube video not found with ID: $id") }
        
        val updatedVideo = video.copy(
            title = request.title,
            description = request.description,
            videoUrl = request.videoUrl,
            thumbnailUrl = request.thumbnailUrl,
            displayOrder = request.displayOrder,
            active = request.active,
            updatedAt = LocalDateTime.now()
        )
        
        val savedVideo = youTubeVideoRepository.save(updatedVideo)
        logger.info("YouTube video updated with ID: ${savedVideo.id}")
        
        return mapToResponse(savedVideo)
    }
    
    /**
     * Delete a YouTube video
     */
    @Transactional
    fun deleteVideo(id: Long) {
        logger.info("Deleting YouTube video with ID: $id")
        
        if (!youTubeVideoRepository.existsById(id)) {
            throw IllegalArgumentException("YouTube video not found with ID: $id")
        }
        
        youTubeVideoRepository.deleteById(id)
        logger.info("YouTube video deleted with ID: $id")
    }
    
    /**
     * Toggle active status of a YouTube video
     */
    @Transactional
    fun toggleActiveStatus(id: Long): YouTubeVideoResponse {
        logger.info("Toggling active status for YouTube video with ID: $id")
        
        val video = youTubeVideoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("YouTube video not found with ID: $id") }
        
        val updatedVideo = video.copy(
            active = !video.active,
            updatedAt = LocalDateTime.now()
        )
        
        val savedVideo = youTubeVideoRepository.save(updatedVideo)
        logger.info("YouTube video active status toggled to ${savedVideo.active} for ID: ${savedVideo.id}")
        
        return mapToResponse(savedVideo)
    }
    
    /**
     * Map YouTubeVideo entity to YouTubeVideoResponse DTO
     */
    private fun mapToResponse(video: YouTubeVideo): YouTubeVideoResponse {
        return YouTubeVideoResponse(
            id = video.id!!,
            title = video.title,
            description = video.description,
            videoUrl = video.videoUrl,
            thumbnailUrl = video.thumbnailUrl,
            displayOrder = video.displayOrder,
            active = video.active,
            createdAt = video.createdAt,
            updatedAt = video.updatedAt
        )
    }
}
