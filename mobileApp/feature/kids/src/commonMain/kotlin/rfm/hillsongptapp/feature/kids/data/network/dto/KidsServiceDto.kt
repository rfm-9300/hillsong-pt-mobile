package rfm.hillsongptapp.feature.kids.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for KidsService API responses
 */
@Serializable
data class KidsServiceDto(
    val id: String,
    val name: String,
    val description: String,
    val minAge: Int,
    val maxAge: Int,
    val startTime: String, // ISO 8601 format
    val endTime: String, // ISO 8601 format
    val location: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val isAcceptingCheckIns: Boolean,
    val staffMembers: List<String>,
    val createdAt: String // ISO 8601 format
)

/**
 * Response DTO for service operations
 */
@Serializable
data class ServiceResponse(
    val success: Boolean,
    val service: KidsServiceDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for multiple services
 */
@Serializable
data class ServicesResponse(
    val success: Boolean,
    val services: List<KidsServiceDto> = emptyList(),
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Request DTO for filtering services by age
 */
@Serializable
data class ServiceFilterRequest(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val acceptingCheckIns: Boolean? = null,
    val location: String? = null
)