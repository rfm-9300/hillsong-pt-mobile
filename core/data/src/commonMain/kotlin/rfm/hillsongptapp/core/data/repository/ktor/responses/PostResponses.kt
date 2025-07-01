package rfm.hillsongptapp.core.data.repository.ktor.responses

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val content: String,
    val date: String,
    val likes: Int,
    val headerImagePath: String
)

@Serializable
data class PostListResponse(
    val type: String,
    val postList: List<Post>
)
