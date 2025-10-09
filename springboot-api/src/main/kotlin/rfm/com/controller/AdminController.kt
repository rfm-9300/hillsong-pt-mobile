package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import rfm.com.dto.ApiResponse
import rfm.com.dto.UserProfileResponse
import rfm.com.job.KidsServiceJob
import rfm.com.service.AdminAuthService
import rfm.com.service.UserService

/**
 * REST Controller for administrative operations
 * Uses simple token-based authentication for admin access
 */
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val kidsServiceJob: KidsServiceJob,
    private val adminAuthService: AdminAuthService,
    private val userService: UserService
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
    
    /**
     * Grant STAFF role to a user
     * POST /api/admin/users/{userId}/roles/staff
     * Requires ADMIN role
     */
    @PostMapping("/users/{userId}/roles/staff")
    @PreAuthorize("hasRole('ADMIN')")
    fun grantStaffRole(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val adminUserId = authentication.name.toLong()
            logger.info("Admin user $adminUserId granting STAFF role to user $userId")
            
            val response = userService.grantStaffRole(userId, adminUserId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Error granting STAFF role to user $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while granting STAFF role: ${ex.message}"
                    )
                )
        }
    }
    
    /**
     * Revoke STAFF role from a user
     * DELETE /api/admin/users/{userId}/roles/staff
     * Requires ADMIN role
     */
    @DeleteMapping("/users/{userId}/roles/staff")
    @PreAuthorize("hasRole('ADMIN')")
    fun revokeStaffRole(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val adminUserId = authentication.name.toLong()
            logger.info("Admin user $adminUserId revoking STAFF role from user $userId")
            
            val response = userService.revokeStaffRole(userId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Error revoking STAFF role from user $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while revoking STAFF role: ${ex.message}"
                    )
                )
        }
    }
    
    /**
     * Get all users with STAFF role
     * GET /api/admin/users/staff
     * Requires ADMIN role
     */
    @GetMapping("/users/staff")
    @PreAuthorize("hasRole('ADMIN')")
    fun getStaffUsers(authentication: Authentication): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
        return try {
            logger.info("Admin user ${authentication.name} retrieving staff users")
            
            val response = userService.getStaffUsers()
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Error retrieving staff users", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving staff users: ${ex.message}"
                    )
                )
        }
    }
    
    /**
     * Get all roles for a specific user
     * GET /api/admin/users/{userId}/roles
     * Requires ADMIN role
     */
    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserRoles(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<String>>> {
        return try {
            logger.info("Admin user ${authentication.name} retrieving roles for user $userId")
            
            val response = userService.getUserRoles(userId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Error retrieving roles for user $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving user roles: ${ex.message}"
                    )
                )
        }
    }
}