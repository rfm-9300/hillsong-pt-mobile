package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Encounter
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.EncounterRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

/**
 * Service for managing encounters
 */
@Service
class EncounterService(
    private val encounterRepository: EncounterRepository,
    private val userRepository: UserRepository,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(EncounterService::class.java)
    
    /**
     * Create a new encounter
     */
    suspend fun createEncounter(
        createEncounterRequest: CreateEncounterRequest,
        organizerId: String,
        image: MultipartFile? = null
    ): EncounterResponse = withContext(Dispatchers.IO) {
        logger.info("Creating encounter: ${createEncounterRequest.title} for organizer: $organizerId")
        
        userRepository.findById(organizerId).orElse(null)
            ?: throw EntityNotFoundException("User", organizerId)
        
        val imagePath = image?.let { img ->
            try {
                fileStorageService.storeEncounterImage(img)
            } catch (ex: Exception) {
                logger.error("Failed to store encounter image", ex)
                throw RuntimeException("Failed to upload encounter image: ${ex.message}")
            }
        }
        
        val encounter = Encounter(
            title = createEncounterRequest.title,
            description = createEncounterRequest.description,
            date = createEncounterRequest.date,
            location = createEncounterRequest.location,
            organizerId = organizerId,
            imagePath = imagePath
        )
        
        val savedEncounter = encounterRepository.save(encounter)
        logger.info("Encounter created successfully with id: ${savedEncounter.id}")
        
        mapToEncounterResponse(savedEncounter)
    }
    
    /**
     * Get all encounters with pagination
     */
    suspend fun getAllEncounters(pageable: Pageable): Page<EncounterResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching all encounters with pagination")
        encounterRepository.findAll(pageable)
            .map { mapToEncounterResponse(it) }
    }
    
    /**
     * Get all upcoming encounters
     */
    suspend fun getUpcomingEncounters(): List<EncounterResponse> = withContext(Dispatchers.IO) {
        logger.debug("Fetching upcoming encounters")
        val now = LocalDateTime.now()
        encounterRepository.findUpcomingEncounters(now)
            .map { mapToEncounterResponse(it) }
    }
    
    /**
     * Get encounter by ID
     */
    suspend fun getEncounterById(encounterId: String): EncounterResponse = withContext(Dispatchers.IO) {
        logger.debug("Fetching encounter by id: $encounterId")
        
        val encounter = encounterRepository.findById(encounterId).orElse(null)
            ?: throw EntityNotFoundException("Encounter", encounterId)
        
        mapToEncounterResponse(encounter)
    }
    
    /**
     * Update an existing encounter
     */
    suspend fun updateEncounter(
        encounterId: String,
        updateRequest: UpdateEncounterRequest,
        userId: String,
        image: MultipartFile? = null
    ): EncounterResponse = withContext(Dispatchers.IO) {
        logger.info("Updating encounter: $encounterId by user: $userId")
        
        val encounter = encounterRepository.findById(encounterId).orElse(null)
            ?: throw EntityNotFoundException("Encounter", encounterId)
        
        if (encounter.organizerId != userId) {
            throw AccessDeniedException("Only the encounter organizer can update this encounter")
        }
        
        val newImagePath = image?.let { img ->
            try {
                encounter.imagePath?.let { oldPath ->
                    fileStorageService.deleteFile(oldPath)
                }
                fileStorageService.storeEncounterImage(img)
            } catch (ex: Exception) {
                logger.error("Failed to store updated encounter image", ex)
                throw RuntimeException("Failed to upload encounter image: ${ex.message}")
            }
        }
        
        val updatedEncounter = encounter.copy(
            title = updateRequest.title ?: encounter.title,
            description = updateRequest.description ?: encounter.description,
            date = updateRequest.date ?: encounter.date,
            location = updateRequest.location ?: encounter.location,
            imagePath = newImagePath ?: encounter.imagePath
        )
        
        val savedEncounter = encounterRepository.save(updatedEncounter)
        logger.info("Encounter updated successfully: ${savedEncounter.id}")
        
        mapToEncounterResponse(savedEncounter)
    }
    
    /**
     * Delete an encounter
     */
    suspend fun deleteEncounter(encounterId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        logger.info("Deleting encounter: $encounterId by user: $userId")
        
        val encounter = encounterRepository.findById(encounterId).orElse(null)
            ?: throw EntityNotFoundException("Encounter", encounterId)
        
        if (encounter.organizerId != userId) {
            throw AccessDeniedException("Only the encounter organizer can delete this encounter")
        }
        
        encounter.imagePath?.let { imagePath ->
            fileStorageService.deleteFile(imagePath)
        }
        
        encounterRepository.delete(encounter)
        logger.info("Encounter deleted successfully: $encounterId")
        true
    }
    
    /**
     * Get encounters organized by a user
     */
    suspend fun getEncountersByOrganizer(organizerId: String, pageable: Pageable): Page<EncounterResponse> = withContext(Dispatchers.IO) {
        userRepository.findById(organizerId).orElse(null)
            ?: throw EntityNotFoundException("User", organizerId)
        
        encounterRepository.findByOrganizerId(organizerId, pageable)
            .map { mapToEncounterResponse(it) }
    }
    
    /**
     * Search encounters by title or location
     */
    suspend fun searchEncounters(query: String): List<EncounterResponse> = withContext(Dispatchers.IO) {
        val titleResults = encounterRepository.findEncountersByTitleContainingIgnoreCase(query)
        val locationResults = encounterRepository.findEncountersByLocationContainingIgnoreCase(query)
        
        (titleResults + locationResults)
            .distinctBy { it.id }
            .map { mapToEncounterResponse(it) }
    }
    
    // Helper method for mapping entity to DTO
    private fun mapToEncounterResponse(encounter: Encounter): EncounterResponse {
        val organizer = userRepository.findById(encounter.organizerId).orElse(null)
        return EncounterResponse(
            id = encounter.id!!,
            title = encounter.title,
            description = encounter.description,
            date = encounter.date,
            location = encounter.location,
            organizerName = organizer?.fullName ?: "Unknown",
            organizerId = encounter.organizerId,
            imagePath = encounter.imagePath,
            createdAt = encounter.createdAt
        )
    }
}
