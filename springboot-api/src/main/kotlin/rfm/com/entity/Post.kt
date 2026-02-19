package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Embedded comment within a Post document.
 */
data class PostComment(
    val id: String? = null,
    val userId: String,
    val content: String,
    val date: LocalDateTime = LocalDateTime.now()
)

@Document(collection = "posts")
data class Post(
    @Id
    val id: String? = null,

    val title: String,

    val content: String,

    @CreatedDate
    val date: LocalDateTime = LocalDateTime.now(),

    val headerImagePath: String = "default-header.jpg",

    val authorId: String,

    val likedByUserIds: MutableList<String> = mutableListOf(),

    val comments: MutableList<PostComment> = mutableListOf()
) {
    val likeCount: Int
        get() = likedByUserIds.size

    val commentCount: Int
        get() = comments.size

    fun addLike(userId: String): Boolean {
        return if (!likedByUserIds.contains(userId)) {
            likedByUserIds.add(userId)
            true
        } else false
    }

    fun removeLike(userId: String): Boolean {
        return likedByUserIds.remove(userId)
    }

    fun isLikedBy(userId: String): Boolean {
        return likedByUserIds.contains(userId)
    }

    fun addComment(comment: PostComment): Boolean {
        return comments.add(comment)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Post
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Post(id=$id, title='$title', authorId=$authorId, likes=$likeCount)"
}