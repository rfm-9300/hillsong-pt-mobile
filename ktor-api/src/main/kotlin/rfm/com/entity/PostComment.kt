package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "post_comment")
data class PostComment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,
    
    @CreationTimestamp
    @Column(nullable = false)
    val date: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PostComment
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String = "PostComment(id=$id, postId=${post.id}, userId=${user.id}, date=$date)"
}