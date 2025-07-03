package example.com.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class PostRequest (
    val postId: Int
)