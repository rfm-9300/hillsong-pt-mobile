package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "password_reset")
data class PasswordReset(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(length = 256, nullable = false)
    val token: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Long,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "is_used", nullable = false)
    val isUsed: Boolean = false
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
    
    val isValid: Boolean
        get() = !isUsed && !isExpired
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PasswordReset
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "PasswordReset(id=$id, userId=${user.id}, isUsed=$isUsed, expiresAt=$expiresAt)"
}