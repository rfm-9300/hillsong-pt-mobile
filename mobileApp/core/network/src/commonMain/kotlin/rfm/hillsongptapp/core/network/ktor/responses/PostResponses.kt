package rfm.hillsongptapp.core.network.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    val title: String,
    val content: String,
    val date: String,
    val headerImagePath: String?,
    val author: Author,
    val likeCount: Int,
    val commentCount: Int,
    val isLikedByCurrentUser: Boolean = false
)

@Serializable
data class Author(
    val id: Long,
    val fullName: String,
    val email: String,
    val imagePath: String?
)

@Serializable
data class PostPageResponse(
    val posts: List<Post>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

// Response structure that matches the actual API response
@Serializable
data class PostsDataResponse(
    val posts: List<Post>
)

// Keep the old structure for backward compatibility if needed
@Serializable
data class PostListResponse(
    val type: String,
    val postList: List<Post>
)
