package rfm.hillsongptapp.feature.kids.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for CheckInRecord API responses
 */
@Serializable
data class CheckInRecordDto(
    val id: String,
    val childId: String,
    val serviceId: String,
    val checkInTime: String, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format
    val checkedInBy: String,
    val checkedOutBy: String? = null,
    val notes: String? = null,
    val status: String // CheckInStatus as string
)

/**
 * Request DTO for check-in operations
 */
@Serializable
data class CheckInRequest(
    val childId: String,
    val serviceId: String,
    val checkedInBy: String,
    val notes: String? = null
)

/**
 * Request DTO for check-out operations
 */
@Serializable
data class CheckOutRequest(
    val childId: String,
    val checkedOutBy: String,
    val notes: String? = null
)

/**
 * Response DTO for check-in operations
 */
@Serializable
data class CheckInResponse(
    val success: Boolean,
    val record: CheckInRecordDto? = null,
    val updatedChild: ChildDto? = null,
    val updatedService: KidsServiceDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for check-out operations
 */
@Serializable
data class CheckOutResponse(
    val success: Boolean,
    val record: CheckInRecordDto? = null,
    val updatedChild: ChildDto? = null,
    val updatedService: KidsServiceDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for check-in history
 */
@Serializable
data class CheckInHistoryResponse(
    val success: Boolean,
    val records: List<CheckInRecordDto> = emptyList(),
    val pagination: PaginationDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)

/**
 * Response DTO for current check-ins
 */
@Serializable
data class CurrentCheckInsResponse(
    val success: Boolean,
    val records: List<CheckInRecordDto> = emptyList(),
    val serviceInfo: KidsServiceDto? = null,
    val message: String? = null,
    val errors: List<String>? = null
)