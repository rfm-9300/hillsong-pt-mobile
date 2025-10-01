package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rfm.com.dto.ApiResponse
import rfm.com.job.KidsServiceJob
import rfm.com.service.AdminAuthService

/**
 * REST Controller for administrative operations
 * Uses simple token-based authentication for admin access
 */
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val kidsServiceJob: KidsServiceJob,
    private val adminAuthService: AdminAuthService
) {
    
    private val logger = LoggerFactory.getLogger(AdminController::class.java)
    
    /**
     * Trigger Sunday services creation job
     * POST /api/admin/create-sunday-services
     * Requires admin token in Authorization header
     */
    @PostMapping("/create-sunday-services")
    fun createSundayServices(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            // Check admin token
            if (!adminAuthService.isValidAdminToken(authHeader)) {
                logger.warn("Invalid admin token provided for Sunday services creation")
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                        ApiResponse(
                            success = false,
                            message = "Invalid admin token"
                        )
                    )
            }
            
            logger.info("Admin triggering Sunday services creation")
            
            kidsServiceJob.createSundayServices()
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Sunday services created successfully",
                    data = "Sunday kids services have been created"
                )
            )
        } catch (e: Exception) {
            logger.error("Error creating Sunday services", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while creating Sunday services: ${e.message}"
                    )
                )
        }
    }
    
    /**
     * Get the admin token (for development/testing purposes)
     * GET /api/admin/token
     */
    @GetMapping("/token")
    fun getAdminToken(): ResponseEntity<ApiResponse<String>> {
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Admin token retrieved",
                data = "Bearer ${adminAuthService.getAdminToken()}"
            )
        )
    }
}