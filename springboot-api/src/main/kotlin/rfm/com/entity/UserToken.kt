package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "user_token")
data class UserToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(name = "access_token", length = 512, nullable = false)
    val accessToken: String,
    
    @Column(name = "refresh_token", length = 512, nullable = false)
    val refreshToken: String,
    
    @Column(name = "access_token_expires_at", nullable = false)
    val accessTokenExpiresAt: Long,
    
    @Column(name = "refresh_token_expires_at", nullable = false)
    val refreshTokenExpiresAt: Long,
    
    @Column(name = "is_revoked", nullable = false)
    val isRevoked: Boolean = false,
    
    @Column(name = "device_info", length = 256)
    val deviceInfo: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "last_used_at", nullable = false)
    val lastUsedAt: LocalDateTime = LocalDateTime.now()
) {
    val isAccessTokenExpired: Boolean
        get() = System.currentTimeMillis() > accessTokenExpiresAt
    
    val isRefreshTokenExpired: Boolean
        get() = System.currentTimeMillis() > refreshTokenExpiresAt
    
    val isValid: Boolean
        get() = !isRevoked && !isAccessTokenExpired
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserToken
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "UserToken(id=$id, userId=${user.id}, isRevoked=$isRevoked, createdAt=$createdAt)"
}