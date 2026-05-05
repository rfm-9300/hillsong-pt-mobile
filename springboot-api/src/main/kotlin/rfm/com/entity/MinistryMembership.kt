package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * User ↔ Ministry join with role and lifecycle status. A user has at
 * most one membership row per ministry; status changes mutate this row.
 */
@Document(collection = "ministryMemberships")
@CompoundIndexes(
    CompoundIndex(name = "user_ministry_unique", def = "{'userId': 1, 'ministryId': 1}", unique = true),
    CompoundIndex(name = "ministry_status", def = "{'ministryId': 1, 'status': 1}"),
    CompoundIndex(name = "user_status", def = "{'userId': 1, 'status': 1}"),
    CompoundIndex(name = "ministry_role", def = "{'ministryId': 1, 'role': 1}")
)
data class MinistryMembership(
    @Id
    val id: String? = null,

    @Indexed
    val userId: String,

    @Indexed
    val ministryId: String,

    val role: MinistryRole = MinistryRole.VOLUNTEER,

    val status: MembershipStatus = MembershipStatus.ACTIVE,

    @CreatedDate
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    val approvedBy: String? = null,

    val addedBy: String? = null,

    val notes: String? = null,

    val leftAt: LocalDateTime? = null,

    @LastModifiedDate
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MinistryMembership
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

enum class MinistryRole {
    VOLUNTEER,
    LEADER,
    COORDINATOR
}

enum class MembershipStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    REJECTED
}
