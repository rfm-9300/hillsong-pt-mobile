package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId::class)
data class UserRole(
    @Id
    @Column(name = "user_id")
    val userId: Long,
    
    @Id
    @Column(name = "role_id")
    val roleId: Long,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    val user: User? = null,
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    val role: Role? = null,
    
    @CreationTimestamp
    @Column(name = "granted_at", nullable = false)
    val grantedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "granted_by")
    val grantedBy: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserRole
        return userId == other.userId && roleId == other.roleId
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + roleId.hashCode()
        return result
    }
    
    override fun toString(): String = "UserRole(userId=$userId, roleId=$roleId)"
}

/**
 * Composite primary key for UserRole entity
 */
data class UserRoleId(
    val userId: Long = 0,
    val roleId: Long = 0
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserRoleId
        return userId == other.userId && roleId == other.roleId
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + roleId.hashCode()
        return result
    }
}
