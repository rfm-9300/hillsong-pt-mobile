package rfm.com.controller

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.service.EncounterService
import rfm.com.util.getCurrentUserId

/**
 * REST controller for encounter management operations
 */
@RestController
@RequestMapping("/api/encounters")
@PreAuthorize("hasRole('USER')")
class EncounterController(
    private val encounterService: EncounterService
) {
    
    private val logger = LoggerFactory.getLogger(EncounterController::class.java)
    
    /**
     * Get all encounters with pagination and sorting
     */
    @GetMapping
    fun getAllEncounters(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ApiResponse<Page<EncounterResponse>>> = runBlocking {
        logger.debug("Fetching all encounters - page: $page, size: $size, sortBy: $sortBy, sortDir: $sortDir")
        
        val sort = if (sortDir.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val encounters = encounterService.getAllEncounters(pageable)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Encounters retrieved successfully",
                data = encounters
            )
        )
    }
    
    /**
     * Get upcoming encounters
     */
    @GetMapping("/upcoming")
    fun getUpcomingEncounters(): ResponseEntity<ApiResponse<List<EncounterResponse>>> = runBlocking {
        logger.debug("Fetching upcoming encounters")
        
        val encounters = encounterService.getUpcomingEncounters()
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Upcoming encounters retrieved successfully",
                data = encounters
            )
        )
    }
    
    /**
     * Get encounter by ID
     */
    @GetMapping("/{id}")
    fun getEncounterById(@PathVariable id: Long): ResponseEntity<ApiResponse<EncounterResponse>> = runBlocking {
        logger.debug("Fetching encounter by id: $id")
        
        val encounter = encounterService.getEncounterById(id)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Encounter retrieved successfully",
                data = encounter
            )
        )
    }
    
    /**
     * Create a new encounter
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createEncounter(
        @Valid @RequestPart("encounter") createEncounterRequest: CreateEncounterRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EncounterResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Creating encounter: ${createEncounterRequest.title} for user: $userId")
        
        val encounter = encounterService.createEncounter(createEncounterRequest, userId, image)
        
        ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                success = true,
                message = "Encounter created successfully",
                data = encounter
            )
        )
    }
    
    /**
     * Update an existing encounter
     */
    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateEncounter(
        @PathVariable id: Long,
        @Valid @RequestPart("encounter") updateEncounterRequest: UpdateEncounterRequest,
        @RequestPart("image", required = false) image: MultipartFile?,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<EncounterResponse>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Updating encounter: $id by user: $userId")
        
        val encounter = encounterService.updateEncounter(id, updateEncounterRequest, userId, image)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Encounter updated successfully",
                data = encounter
            )
        )
    }
    
    /**
     * Delete an encounter
     */
    @DeleteMapping("/{id}")
    fun deleteEncounter(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.info("Deleting encounter: $id by user: $userId")
        
        encounterService.deleteEncounter(id, userId)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Encounter deleted successfully",
                data = "Encounter with id $id has been deleted"
            )
        )
    }
    
    /**
     * Get encounters organized by the current user
     */
    @GetMapping("/my-encounters")
    fun getMyEncounters(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Page<EncounterResponse>>> = runBlocking {
        val userId = authentication.getCurrentUserId()
        logger.debug("Fetching encounters organized by user: $userId")
        
        val sort = if (sortDir.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }
        
        val pageable: Pageable = PageRequest.of(page, size, sort)
        val encounters = encounterService.getEncountersByOrganizer(userId, pageable)
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Your encounters retrieved successfully",
                data = encounters
            )
        )
    }
    
    /**
     * Search encounters by title or location
     */
    @GetMapping("/search")
    fun searchEncounters(
        @RequestParam query: String
    ): ResponseEntity<ApiResponse<List<EncounterResponse>>> = runBlocking {
        logger.debug("Searching encounters with query: $query")
        
        if (query.isBlank()) {
            return@runBlocking ResponseEntity.badRequest().body(
                ApiResponse<List<EncounterResponse>>(
                    success = false,
                    message = "Search query cannot be empty"
                )
            )
        }
        
        val encounters = encounterService.searchEncounters(query.trim())
        
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Search completed successfully",
                data = encounters
            )
        )
    }
}
