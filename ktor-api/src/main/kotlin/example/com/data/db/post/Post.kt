package example.com.data.db.post

import example.com.data.db.user.UserProfilesTable
import example.com.data.db.user.UserTable
import example.com.data.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

@Serializable
data class Post(
    val id: Int? = null,
    val userId : Int,
    val title: String,
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val date: LocalDateTime = LocalDateTime.now(),
    val likes: Int = 0,
    val headerImagePath: String = "" // Added image path
)

object PostTable: IntIdTable("post") {
    val title = varchar("title", 255)
    val content = text("content")
    val date = datetime("date")
    val userId = reference("user_id", UserProfilesTable)
    val headerImagePath = varchar("header_image_path", 255).default("default-header.jpg")
}
object PostLikeTable: Table("post_like") {
    val postId = reference("post_id", PostTable)
    val userId = reference("user_id", UserTable)
    override val primaryKey = PrimaryKey(postId, userId)
}
object PostCommentTable: IntIdTable("post_comment") {
    val postId = reference("post_id", PostTable)
    val userId = reference("user_id", UserTable)
    val content = text("content")
    val date = datetime("date")
}
