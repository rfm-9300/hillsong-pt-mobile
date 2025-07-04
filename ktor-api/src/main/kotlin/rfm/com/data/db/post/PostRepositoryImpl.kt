package rfm.com.data.db.post

import rfm.com.data.db.user.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class PostRepositoryImpl: PostRepository {
    override suspend fun addPost(post: Post): Int? = suspendTransaction {
        try {
            val postId = PostTable.insertAndGetId {
                it[title] = post.title
                it[content] = post.content
                it[date] = post.date
                it[userId] = post.userId
                it[headerImagePath] = post.headerImagePath // Insert image path
            }
            postId.value
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAllPosts(): List<Post> = suspendTransaction {
        PostTable.selectAll().map {
            Post(
                id = it[PostTable.id].value,
                title = it[PostTable.title],
                content = it[PostTable.content],
                date = it[PostTable.date],
                userId = it[PostTable.userId].value,
                headerImagePath = it[PostTable.headerImagePath] // Retrieve image path
            )
        }
    }

    override suspend fun getPostById(postId: Int): Post? = suspendTransaction {
        PostTable.select { PostTable.id eq postId }.map {
            Post(
                id = it[PostTable.id].value,
                title = it[PostTable.title],
                content = it[PostTable.content],
                date = it[PostTable.date],
                userId = it[PostTable.userId].value,
                headerImagePath = it[PostTable.headerImagePath]
            )
        }.singleOrNull()
    }

    override suspend fun deletePost(postId: Int): Boolean = suspendTransaction {
        PostLikeTable.deleteWhere { PostLikeTable.postId eq postId }
        PostCommentTable.deleteWhere { PostCommentTable.postId eq postId }
        PostTable.deleteWhere { PostTable.id eq postId } > 0
    }
}