package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false, length = 128)
    val email: String,
    
    @Column(nullable = false, length = 256)
    val password: String,
    
    @Column(nullable = false, length = 256)
    val salt: String,
    
    @Column(nullable = false)
    val verified: Boolean = false,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "verification_token", length = 256)
    val verificationToken: String? = null,
    
    @Column(name = "google_id", length = 256)
    val googleId: String? = null,
    
    @Column(name = "facebook_id", length = 256)
    val facebookId: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20, nullable = false)
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    
    @Column(name = "reset_token", length = 256)
    val resetToken: String? = null,
    
    @Column(name = "reset_token_expires_at")
    val resetTokenExpiresAt: Long? = null,
    
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = true)
    val profile: UserProfile? = null,
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tokens: MutableSet<UserToken> = mutableSetOf(),
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val passwordResets: MutableSet<PasswordReset> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "attendees", fetch = FetchType.LAZY)
    val attendedEvents: MutableSet<Event> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "waitingListUsers", fetch = FetchType.LAZY)
    val waitingListEvents: MutableSet<Event> = mutableSetOf(),
    
    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val posts: MutableSet<Post> = mutableSetOf(),
    
    @ManyToMany(mappedBy = "likedByUsers", fetch = FetchType.LAZY)
    val likedPosts: MutableSet<Post> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "User(id=$id, email='$email', verified=$verified)"
}

enum class AuthProvider {
    LOCAL, GOOGLE, FACEBOOK
}