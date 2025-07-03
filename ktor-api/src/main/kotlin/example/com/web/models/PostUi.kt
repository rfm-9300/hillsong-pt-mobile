package example.com.web.models

import example.com.data.model.uiDate

data class PostUi(
    val postId: Int = 0,
    val imgUrl: String = "/resources/images/default-user-image.webp",
    val userName: String = "User Name",
    val userHandle: String = "@username",
    val title: String = "Post Title",
    val content: String = "Post Content",
    val date: uiDate,
    val likes: Int = 0,
    val comments: Int = 0,
    val views: Int = 0
) {
}