package example.com.data.db.post

interface PostRepository {
    suspend fun addPost(post: Post) : Int?
    suspend fun getAllPosts(): List<Post>
    suspend fun deletePost(postId: Int): Boolean
}