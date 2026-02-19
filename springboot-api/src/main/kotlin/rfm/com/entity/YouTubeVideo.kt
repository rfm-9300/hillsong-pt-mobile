package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "youtube_videos")
data class YouTubeVideo(
    @Id
    val id: String? = null,

    val title: String,

    val description: String? = null,

    val videoUrl: String,

    val thumbnailUrl: String,

    val displayOrder: Int = 0,

    val active: Boolean = true,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

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
