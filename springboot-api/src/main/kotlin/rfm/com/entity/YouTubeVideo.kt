package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "youtube_video")
data class YouTubeVideo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val title: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "video_url", nullable = false, length = 500)
    val videoUrl: String,
    
    @Column(name = "thumbnail_url", nullable = false, length = 500)
    val thumbnailUrl: String,
    
    @Column(name = "display_order", nullable = false)
    val displayOrder: Int = 0,
    
    @Column(nullable = false)
    val active: Boolean = true,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as YouTubeVideo
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "YouTubeVideo(id=$id, title='$title', active=$active)"
}
