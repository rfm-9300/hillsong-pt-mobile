package rfm.hillsongptapp.feature.kids.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Child API responses
 */
@Serializable
data class ChildDto(
    val id: String,
    val parentId: String,
    val name: String,
    val dateOfBirth: String, // ISO 8601 format (YYYY-MM-DD)
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContact: EmergencyContactDto,
    val status: String, // CheckInStatus as string
    val currentServiceId: String? = null,
    val checkInTime: String? = null, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format
    val createdAt: String, // ISO 8601 format
    val updatedAt: String // ISO 8601 format
)

/**
 * Data Transfer Object for EmergencyContact API responses
 */
@Serializable
data class EmergencyContactDto(
    val name: String,
    val phoneNumber: String,
    val relationship: String
)

/**
 * Request DTO for child registration
 */
@Serializable
data class ChildRegistrationRequest(
    val name: String,
    val dateOfBirth: String,
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContact: EmergencyContactDto
)

/**
 * Request DTO for child updates
 */
@Serializable
data class ChildUpdateRequest(
    val name: String? = null,
    val dateOfBirth: String? = null,
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContact: EmergencyContactDto? = null
)

/**
 * Response DTO for child operations
 */
@Serializable
data class ChildResponse(
    val success: Boolean,
    val child: ChildDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for multiple children
 */
@Serializable
data class ChildrenResponse(
    val success: Boolean,
    val children: List<ChildDto> = emptyList(),
    val pagination: PaginationDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Pagination information for API responses
 */
@Serializable
data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)