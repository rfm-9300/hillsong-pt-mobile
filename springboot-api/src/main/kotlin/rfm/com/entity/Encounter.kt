package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "encounter")
data class Encounter(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val title: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val description: String,
    
    @Column(nullable = false)
    val date: LocalDateTime,
    
    @Column(nullable = false, length = 255)
    val location: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    val organizer: UserProfile,
    
    @Column(name = "image_path", length = 255)
    val imagePath: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Encounter
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "Encounter(id=$id, title='$title', date=$date, location='$location')"
}
