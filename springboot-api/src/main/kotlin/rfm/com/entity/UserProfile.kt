package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "user_profile")
data class UserProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
    
    @Column(name = "first_name", length = 128, nullable = false)
    val firstName: String,
    
    @Column(name = "last_name", length = 128, nullable = false)
    val lastName: String,
    
    @Column(length = 128, nullable = false)
    val email: String,
    
    @Column(length = 18, nullable = false)
    val phone: String = "",
    
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "image_path", length = 500, nullable = false)
    val imagePath: String = "",
    
    @Column(name = "is_admin", nullable = false)
    val isAdmin: Boolean = false,
    
    @OneToMany(mappedBy = "organizer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val organizedEvents: MutableSet<Event> = mutableSetOf()
) {
    val fullName: String
        get() = "$firstName $lastName"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserProfile
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "UserProfile(id=$id, fullName='$fullName', email='$email')"
}