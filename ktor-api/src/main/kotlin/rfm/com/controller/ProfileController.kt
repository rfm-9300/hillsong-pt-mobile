package rfm.com.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.service.FileStorageService
import rfm.com.service.UserService

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val userService: UserService,
    private val fileStorageService: FileStorageService
) {
    
    private val logger = LoggerFactory.getLogger(ProfileController::class.java)
    
    /**
     * Get current user's profile
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun getCurrentUserProfile(authentication: Authentication): ResponseEntity<ApiResponse<UserProfileResponse>> {
        return try {
            val userId = authentication.name.toLong()
            val response = userService.getUserProfile(userId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to get current user profile", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to retrieve profile")
            )
        }
    }
    
    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    fun getUserProfile(@PathVariable userId: Long): ResponseEntity<ApiResponse<UserProfileResponse>> {
        return try {
            val response = userService.getUserProfile(userId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to get user profile for ID: $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to retrieve profile")
            )
        }
    }
    
    /**
     * Update current user's profile
     */
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    fun updateCurrentUserProfile(
        @Valid @RequestBody updateProfileRequest: UpdateProfileRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authentication.name.toLong()
            val response = userService.updateUserProfile(
                userId = userId,
                firstName = updateProfileRequest.firstName,
                lastName = updateProfileRequest.lastName,
                phone = updateProfileRequest.phone
            )
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to update current user profile", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to update profile")
            )
        }
    }
    
    /**
     * Upload profile image for current user
     */
    @PostMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('USER')")
    fun uploadProfileImage(
        @RequestParam("image") image: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val userId = authentication.name.toLong()
            
            // Validate file
            if (image.isEmpty) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Image file is required")
                )
            }
            
            // Check file type
            val contentType = image.contentType
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Only image files are allowed")
                )
            }
            
            // Check file size (max 5MB)
            if (image.size > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Image file size must not exceed 5MB")
                )
            }
            
            // Store the file
            val imagePath = fileStorageService.storeProfileImage(image)
            
            // Update user profile with image path
            val response = userService.updateUserProfileImage(userId, imagePath)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to upload profile image", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to upload profile image")
            )
        }
    }
    
    /**
     * Get all user profiles (admin only)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUserProfiles(): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
        return try {
            val response = userService.getAllUserProfiles()
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            logger.error("Failed to get all user profiles", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to retrieve user profiles")
            )
        }
    }
    
    /**
     * Search user profiles (admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    fun searchUserProfiles(@RequestParam searchTerm: String): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
        return try {
            if (searchTerm.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Search term cannot be empty")
                )
            }
            
            val response = userService.searchUserProfiles(searchTerm)
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            logger.error("Failed to search user profiles with term: $searchTerm", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to search user profiles")
            )
        }
    }
    
    /**
     * Get admin profiles (admin only)
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminProfiles(): ResponseEntity<ApiResponse<List<UserProfileResponse>>> {
        return try {
            val response = userService.getAdminProfiles()
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            logger.error("Failed to get admin profiles", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to retrieve admin profiles")
            )
        }
    }
    
    /**
     * Update user admin status (admin only)
     */
    @PutMapping("/{userId}/admin-status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserAdminStatus(
        @PathVariable userId: Long,
        @Valid @RequestBody updateAdminStatusRequest: UpdateAdminStatusRequest,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val currentUserId = authentication.name.toLong()
            
            // Prevent users from changing their own admin status
            if (currentUserId == userId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Cannot change your own admin status")
                )
            }
            
            val response = userService.updateUserAdminStatus(userId, updateAdminStatusRequest.isAdmin)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to update admin status for user ID: $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to update admin status")
            )
        }
    }
    
    /**
     * Delete user account (admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(
        @PathVariable userId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            val currentUserId = authentication.name.toLong()
            
            // Prevent users from deleting their own account
            if (currentUserId == userId) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse(success = false, message = "Cannot delete your own account")
                )
            }
            
            val response = userService.deleteUser(userId)
            
            if (response.success) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (ex: Exception) {
            logger.error("Failed to delete user ID: $userId", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(success = false, message = "Failed to delete user")
            )
        }
    }
}