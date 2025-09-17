package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "post")
data class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val title: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,
    
    @CreationTimestamp
    @Column(nullable = false)
    val date: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "header_image_path", length = 255, nullable = false)
    val headerImagePath: String = "default-header.jpg",
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val author: User,
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_like",
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val likedByUsers: MutableSet<User> = mutableSetOf(),
    
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val comments: MutableSet<PostComment> = mutableSetOf()
) {
    val likeCount: Int
        get() = likedByUsers.size
    
    val commentCount: Int
        get() = comments.size
    
    fun addLike(user: User): Boolean {
        return likedByUsers.add(user)
    }
    
    fun removeLike(user: User): Boolean {
        return likedByUsers.remove(user)
    }
    
    fun isLikedBy(user: User): Boolean {
        return likedByUsers.contains(user)
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
    
    override fun toString(): String = "Post(id=$id, title='$title', author=${author.email}, likes=$likeCount)"
}