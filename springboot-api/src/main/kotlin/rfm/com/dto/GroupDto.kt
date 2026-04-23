package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import rfm.com.entity.MeetingFrequency
import rfm.com.entity.Ministry
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Embedded location payload.
 *
 * Latitude/longitude are the source of truth for the map view and
 * geo filtering. The address fields are for display only.
 */
data class GroupLocationDto(
    @field:NotBlank(message = "Address line is required")
    @field:Size(max = 255)
    val addressLine: String,

    @field:NotBlank(message = "City is required")
    @field:Size(max = 100)
    val city: String,

    @field:Size(max = 100)
    val region: String? = null,

    @field:Size(max = 20)
    val postalCode: String? = null,

    @field:NotBlank
    @field:Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code")
    val country: String = "PT",

    @field:DecimalMin(value = "-90.0")
    @field:DecimalMax(value = "90.0")
    val latitude: Double,

    @field:DecimalMin(value = "-180.0")
    @field:DecimalMax(value = "180.0")
    val longitude: Double
)

data class CreateGroupRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 120)
    val name: String,

    @field:NotNull(message = "Ministry is required")
    val ministry: Ministry,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 2000)
    val description: String,

    @field:NotBlank(message = "Leader name is required")
    @field:Size(max = 120)
    val leaderName: String,

    @field:NotBlank(message = "Leader contact is required")
    @field:Size(max = 60)
    val leaderContact: String,

    @field:NotNull(message = "Meeting day is required")
    val meetingDay: DayOfWeek,

    @field:NotNull(message = "Meeting time is required")
    @JsonFormat(pattern = "HH:mm")
    val meetingTime: LocalTime,

    val frequency: MeetingFrequency = MeetingFrequency.WEEKLY,

    @field:Valid
    @field:NotNull
    val location: GroupLocationDto,

    @field:Min(1)
    @field:Max(1000)
    val maxMembers: Int? = null,

    @field:Min(0)
    @field:Max(10000)
    val currentMembers: Int = 0,

    val isActive: Boolean = true,

    val isJoinable: Boolean = true,

    val tags: List<String> = emptyList()
)

data class UpdateGroupRequest(
    @field:Size(max = 120)
    val name: String? = null,

    val ministry: Ministry? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    @field:Size(max = 120)
    val leaderName: String? = null,

    @field:Size(max = 60)
    val leaderContact: String? = null,

    val meetingDay: DayOfWeek? = null,

    @JsonFormat(pattern = "HH:mm")
    val meetingTime: LocalTime? = null,

    val frequency: MeetingFrequency? = null,

    @field:Valid
    val location: GroupLocationDto? = null,

    @field:Min(1)
    @field:Max(1000)
    val maxMembers: Int? = null,

    @field:Min(0)
    @field:Max(10000)
    val currentMembers: Int? = null,

    val isActive: Boolean? = null,

    val isJoinable: Boolean? = null,

    val tags: List<String>? = null
)

data class GroupLocationResponse(
    val addressLine: String,
    val city: String,
    val region: String?,
    val postalCode: String?,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

data class GroupResponse(
    val id: String,
    val name: String,
    val ministry: Ministry,
    val description: String,
    val leaderName: String,
    val leaderContact: String,
    val meetingDay: DayOfWeek,
    @JsonFormat(pattern = "HH:mm")
    val meetingTime: LocalTime,
    val frequency: MeetingFrequency,
    val location: GroupLocationResponse,
    val imagePath: String?,
    val maxMembers: Int?,
    val currentMembers: Int,
    val isActive: Boolean,
    val isJoinable: Boolean,
    val tags: List<String>,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime
)

data class GroupSummaryResponse(
    val id: String,
    val name: String,
    val ministry: Ministry,
    val description: String,
    val leaderName: String,
    val meetingDay: DayOfWeek,
    @JsonFormat(pattern = "HH:mm")
    val meetingTime: LocalTime,
    val frequency: MeetingFrequency,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String?,
    val currentMembers: Int,
    val maxMembers: Int?,
    val isJoinable: Boolean,
    val isActive: Boolean
)

data class MinistryOption(
    val value: Ministry,
    val labelEn: String,
    val labelPt: String
)
