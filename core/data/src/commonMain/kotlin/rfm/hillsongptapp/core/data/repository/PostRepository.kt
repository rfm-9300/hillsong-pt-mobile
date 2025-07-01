package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.repository.ktor.ApiService
import rfm.hillsongptapp.core.data.repository.ktor.responses.Post

class PostRepository(private val api: ApiService) {

    suspend fun getPosts(): List<Post> {
        val response = api.getPosts()
        return response.data?.postList ?: emptyList()
    }
}
