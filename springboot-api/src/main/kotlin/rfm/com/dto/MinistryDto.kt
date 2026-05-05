package rfm.com.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import rfm.com.entity.MembershipStatus
import rfm.com.entity.MinistryRole
import rfm.com.entity.MinistryType
import java.time.LocalDateTime

data class CreateMinistryRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 120)
    val name: String,

    @field:NotBlank(message = "Description is required")
    @field:Size(max = 2000)
    val description: String,

    @field:NotNull(message = "Type is required")
    val type: MinistryType,

    val isActive: Boolean = true,

    val isJoinable: Boolean = true,

    val requiresApproval: Boolean = true,

    val tags: List<String> = emptyList(),

    @field:Valid
    val location: GroupLocationDto? = null,

    val parentMinistryId: String? = null
)

data class UpdateMinistryRequest(
    @field:Size(max = 120)
    val name: String? = null,

    @field:Size(max = 2000)
    val description: String? = null,

    val type: MinistryType? = null,

    val isActive: Boolean? = null,

    val isJoinable: Boolean? = null,

    val requiresApproval: Boolean? = null,

    val tags: List<String>? = null,

    @field:Valid
    val location: GroupLocationDto? = null,

    val parentMinistryId: String? = null
)

data class MinistryResponse(
    val id: String,
    val name: String,
    val description: String,
    val type: MinistryType,
    val imagePath: String?,
    val isActive: Boolean,
    val isJoinable: Boolean,
    val requiresApproval: Boolean,
    val tags: List<String>,
    val location: GroupLocationResponse?,
    val leaderUserIds: List<String>,
    val coordinatorUserIds: List<String>,
    val parentMinistryId: String?,
    val memberCount: Long,
    val pendingCount: Long,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val updatedAt: LocalDateTime
)

data class MinistrySummaryResponse(
    val id: String,
    val name: String,
    val description: String,
    val type: MinistryType,
    val imagePath: String?,
    val city: String?,
    val isActive: Boolean,
    val isJoinable: Boolean,
    val requiresApproval: Boolean,
    val memberCount: Long,
    val leaderCount: Int
)

data class JoinMinistryRequest(
    @field:Size(max = 500)
    val message: String? = null
)

data class MembershipResponse(
    val id: String,
    val userId: String,
    val ministryId: String,
    val role: MinistryRole,
    val status: MembershipStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val joinedAt: LocalDateTime,
    val approvedBy: String?,
    val addedBy: String?,
    val notes: String?
)

data class MemberResponse(
    val id: String,
    val userId: String,
    val ministryId: String,
    val role: MinistryRole,
    val status: MembershipStatus,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String?,
    val imagePath: String?,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val joinedAt: LocalDateTime,
    val notes: String?
)

data class MyMinistryResponse(
    val membershipId: String,
    val ministry: MinistrySummaryResponse,
    val role: MinistryRole,
    val status: MembershipStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val joinedAt: LocalDateTime
)

data class AddMemberRequest(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    val role: MinistryRole = MinistryRole.VOLUNTEER,
    @field:Size(max = 500)
    val notes: String? = null
)

data class UpdateMemberRequest(
    val role: MinistryRole? = null,
    val status: MembershipStatus? = null,
    @field:Size(max = 500)
    val notes: String? = null
)

data class AssignLeaderRequest(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    @field:NotNull
    val role: MinistryRole = MinistryRole.LEADER
) {
    init {
        require(role == MinistryRole.LEADER || role == MinistryRole.COORDINATOR) {
            "Role must be LEADER or COORDINATOR"
        }
    }
}

data class RejectMemberRequest(
    @field:Size(max = 500)
    val reason: String? = null
)

data class MinistryTypeOption(
    val value: MinistryType,
    val labelEn: String,
    val labelPt: String
)
