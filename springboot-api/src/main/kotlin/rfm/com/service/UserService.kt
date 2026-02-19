package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * Get user by ID
     */
    fun getUserById(userId: String): User? {
        return userRepository.findById(userId).orElse(null)
    }
    
    /**
     * Get user by email
     */
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    /**
     * Update user profile
     */
    fun updateUserProfile(userId: String, firstName: String?, lastName: String?, phone: String?): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val updatedUser = user.copy(
                firstName = firstName ?: user.firstName,
                lastName = lastName ?: user.lastName,
                phone = phone ?: user.phone,
                updatedAt = LocalDateTime.now()
            )
            
            userRepository.save(updatedUser)
            
            logger.info("User profile updated for user ID: $userId")
            ApiResponse(success = true, message = "Profile updated successfully")
        } catch (ex: Exception) {
            logger.error("Profile update failed for user ID: $userId", ex)
            ApiResponse(success = false, message = "Profile update failed")
        }
    }
    
    /**
     * Update user profile image
     */
    fun updateUserProfileImage(userId: String, imagePath: String): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val updatedUser = user.copy(
                imagePath = imagePath,
                updatedAt = LocalDateTime.now()
            )
            userRepository.save(updatedUser)
            
            logger.info("User profile image updated for user ID: $userId")
            ApiResponse(success = true, message = "Profile image updated successfully")
        } catch (ex: Exception) {
            logger.error("Profile image update failed for user ID: $userId", ex)
            ApiResponse(success = false, message = "Profile image update failed")
        }
    }
    
    /**
     * Get user profile by user ID
     */
    fun getUserProfile(userId: String): ApiResponse<UserProfileResponse> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val profileResponse = UserProfileResponse(
                id = user.id!!,
                userId = user.id!!,
                firstName = user.firstName,
                lastName = user.lastName,
                email = user.email,
                phone = user.phone,
                imagePath = user.imagePath,
                isAdmin = user.isAdmin,
                joinedAt = user.joinedAt,
                fullName = user.fullName
            )
            
            ApiResponse(success = true, message = "Profile retrieved successfully", data = profileResponse)
        } catch (ex: Exception) {
            logger.error("Failed to get user profile for user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to retrieve user profile")
        }
    }
    
    /**
     * Get all user profiles (admin functionality)
     */
    fun getAllUserProfiles(): ApiResponse<List<UserProfileResponse>> {
        return try {
            val users = userRepository.findAll()
            
            val profileResponses = users.map { user ->
                UserProfileResponse(
                    id = user.id!!,
                    userId = user.id!!,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    phone = user.phone,
                    imagePath = user.imagePath,
                    isAdmin = user.isAdmin,
                    joinedAt = user.joinedAt,
                    fullName = user.fullName
                )
            }
            
            ApiResponse(success = true, message = "User profiles retrieved successfully", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to get all user profiles", ex)
            ApiResponse(success = false, message = "Failed to retrieve user profiles")
        }
    }
    
    /**
     * Search user profiles by name or email
     */
    fun searchUserProfiles(searchTerm: String): ApiResponse<List<UserProfileResponse>> {
        return try {
            val allUsers = userRepository.findAll()
            val lowerSearch = searchTerm.lowercase()
            
            val matchingUsers = allUsers.filter { user ->
                user.firstName.lowercase().contains(lowerSearch) ||
                user.lastName.lowercase().contains(lowerSearch) ||
                user.email.lowercase().contains(lowerSearch) ||
                user.fullName.lowercase().contains(lowerSearch)
            }
            
            val profileResponses = matchingUsers.map { user ->
                UserProfileResponse(
                    id = user.id!!,
                    userId = user.id!!,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    phone = user.phone,
                    imagePath = user.imagePath,
                    isAdmin = user.isAdmin,
                    joinedAt = user.joinedAt,
                    fullName = user.fullName
                )
            }
            
            ApiResponse(success = true, message = "User profiles search completed", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to search user profiles with term: $searchTerm", ex)
            ApiResponse(success = false, message = "Failed to search user profiles")
        }
    }
    
    /**
     * Update user admin status (admin functionality)
     */
    fun updateUserAdminStatus(userId: String, isAdmin: Boolean): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val updatedUser = user.copy(
                isAdmin = isAdmin,
                updatedAt = LocalDateTime.now()
            )
            userRepository.save(updatedUser)
            
            val action = if (isAdmin) "granted" else "revoked"
            logger.info("Admin privileges $action for user ID: $userId")
            ApiResponse(success = true, message = "Admin status updated successfully")
        } catch (ex: Exception) {
            logger.error("Failed to update admin status for user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to update admin status")
        }
    }
    
    /**
     * Get admin user profiles
     */
    fun getAdminProfiles(): ApiResponse<List<UserProfileResponse>> {
        return try {
            val adminUsers = userRepository.findByIsAdminTrue()
            
            val profileResponses = adminUsers.map { user ->
                UserProfileResponse(
                    id = user.id!!,
                    userId = user.id!!,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    phone = user.phone,
                    imagePath = user.imagePath,
                    isAdmin = user.isAdmin,
                    joinedAt = user.joinedAt,
                    fullName = user.fullName
                )
            }
            
            ApiResponse(success = true, message = "Admin profiles retrieved successfully", data = profileResponses)
        } catch (ex: Exception) {
            logger.error("Failed to get admin profiles", ex)
            ApiResponse(success = false, message = "Failed to retrieve admin profiles")
        }
    }
    
    /**
     * Delete user account (admin functionality)
     */
    fun deleteUser(userId: String): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId)
                .orElse(null) ?: return ApiResponse(success = false, message = "User not found")
            
            userRepository.delete(user)
            
            logger.info("User deleted successfully: ${user.email}")
            ApiResponse(success = true, message = "User deleted successfully")
        } catch (ex: Exception) {
            logger.error("Failed to delete user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to delete user")
        }
    }
    
    /**
     * Get all roles for a user
     */
    fun getUserRoles(userId: String): ApiResponse<List<String>> {
        return try {
            val user = userRepository.findById(userId)
                .orElse(null) ?: return ApiResponse(success = false, message = "User not found")
            
            val roleNames = user.roles.map { it.name }
            ApiResponse(success = true, message = "User roles retrieved successfully", data = roleNames)
        } catch (ex: Exception) {
            logger.error("Failed to get roles for user ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to retrieve user roles")
        }
    }
    
    /**
     * Create user profile by Admin (profile-only, credentials managed by auth-service)
     */
    fun createUserByAdmin(request: CreateUserRequest): ApiResponse<String> {
        return try {
            logger.debug("Admin creating user profile with email: ${request.email}")
            
            if (userRepository.existsByEmail(request.email)) {
                return ApiResponse(success = false, message = "User with this email already exists")
            }
            
            val user = User(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone ?: "",
                isAdmin = request.isAdmin
            )
            
            userRepository.save(user)
            
            logger.info("User profile created by admin successfully: ${request.email}")
            ApiResponse(success = true, message = "User created successfully")
        } catch (ex: Exception) {
            logger.error("Failed to create user by admin: ${request.email}", ex)
            ApiResponse(success = false, message = "User creation failed: ${ex.message}")
        }
    }
    
    /**
     * Update user by Admin (Full update)
     */
    fun updateUserByAdmin(userId: String, request: AdminUpdateUserRequest): ApiResponse<String> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
                ?: return ApiResponse(success = false, message = "User not found")
            
            val updatedUser = user.copy(
                firstName = request.firstName ?: user.firstName,
                lastName = request.lastName ?: user.lastName,
                phone = request.phone ?: user.phone,
                isAdmin = request.isAdmin ?: user.isAdmin,
                updatedAt = LocalDateTime.now()
            )
            
            userRepository.save(updatedUser)
            
            logger.info("User updated by admin for user ID: $userId")
            ApiResponse(success = true, message = "User updated successfully")
        } catch (ex: Exception) {
            logger.error("Failed to update user by admin for ID: $userId", ex)
            ApiResponse(success = false, message = "Failed to update user: ${ex.message}")
        }
    }
}