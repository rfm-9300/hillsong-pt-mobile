package rfm.com.data.db.post

interface PostRepository {
    suspend fun addPost(post: Post) : Int?
    suspend fun getAllPosts(): List<Post>
    suspend fun getPostById(postId: Int): Post?
    suspend fun deletePost(postId: Int): Boolean
}