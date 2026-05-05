package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * A configurable ministry — service team or connection-group umbrella.
 *
 * The classification (`type`) reuses [MinistryType] which historically
 * tagged connection groups; multiple Ministry documents can share a
 * type (e.g., "Worship Team Lisboa" and "Worship Team Porto" both
 * have type=WORSHIP).
 *
 * `leaderUserIds`/`coordinatorUserIds` are denormalized from
 * MinistryMembership for fast permission checks. They MUST be kept in
 * sync via MinistryMembershipService.setRole() (single write site).
 */
@Document(collection = "ministries")
data class Ministry(
    @Id
    val id: String? = null,

    @Indexed
    val name: String,

    val description: String,

    @Indexed
    val type: MinistryType,

    val imagePath: String? = null,

    @Indexed
    val isActive: Boolean = true,

    val isJoinable: Boolean = true,

    val requiresApproval: Boolean = true,

    val tags: List<String> = emptyList(),

    val location: GroupLocation? = null,

    @Indexed
    val leaderUserIds: List<String> = emptyList(),

    @Indexed
    val coordinatorUserIds: List<String> = emptyList(),

    val parentMinistryId: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Ministry
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String =
        "Ministry(id=$id, name='$name', type=$type)"
}
