package rfm.com.routes

import rfm.com.data.db.event.EventRepository
import rfm.com.data.db.post.Post
import rfm.com.data.db.post.PostRepository
import rfm.com.data.db.user.UserRepository
import rfm.com.data.requests.CreatePostRequest
import rfm.com.data.requests.PostRequest
import rfm.com.data.responses.ApiResponseData
import rfm.com.data.utils.Strings
import rfm.com.plugins.Logger
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.io.File

fun Route.homeRoutes(
    eventRepository: EventRepository,
    userRepository: UserRepository,
    postRepository: PostRepository
){
    /**
     * Api Routes
     */

    authenticate {
        post(Routes.Api.Post.DELETE) {
            try{
                val request = kotlin.runCatching { call.receiveNullable<PostRequest>() }.getOrNull() ?: return@post respondHelper(success = false, message = "Invalid request", call = call)
                val postId = request.postId

                Logger.d("Delete post request")
                val userId = getUserIdFromRequestToken(call) ?: return@post
                if (!isUserAdmin(userId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call, statusCode = HttpStatusCode.Unauthorized)
                }
                Logger.d("User $userId is authorized to delete post")

                val isDeleted = postRepository.deletePost(postId)

                respondHelper(success = isDeleted, message = if (isDeleted) "Post deleted" else "Post not found", call = call, statusCode = if (isDeleted) HttpStatusCode.OK else HttpStatusCode.NotFound)
            } catch (e: Exception){
                respondHelper(success = false, message = e.message ?: "Error deleting post", call = call, statusCode = HttpStatusCode.InternalServerError)
            }
        }
    }

    authenticate {
        post(Routes.Api.Post.CREATE) {
            Logger.d("Create post request")
            try {
                val request = call.receive<CreatePostRequest>()
                Logger.d("Parsed request: $request")
                if (request == null) {
                    return@post respondHelper(success = false, message = "Invalid request", call = call, statusCode = HttpStatusCode.BadRequest)
                }
                val title = request.title
                val content = request.content

                Logger.d("Create post request")
                val userId = getUserIdFromRequestToken(call) ?: return@post
                if (!isUserAdmin(userId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call, statusCode = HttpStatusCode.Unauthorized)
                }
                Logger.d("User $userId is authorized to create post")

                val post = Post(userId = userId.toInt(), title = title, content = content)

                val postId = postRepository.addPost(post)

                respondHelper(success = postId != null, message = if (postId != null) "Post created" else "Error creating post",  call = call, statusCode = if (postId != null) HttpStatusCode.OK else HttpStatusCode.InternalServerError)
            } catch (e: Exception) {
                respondHelper(success = false, message = e.message ?: "Error creating post", call = call, statusCode = HttpStatusCode.InternalServerError)
            }
        }
    }

    get(Routes.Api.Post.LIST) {
        try {
            val posts = postRepository.getAllPosts()
            val postResponse = ApiResponseData.PostListResponse(postList = posts)
            respondHelper(success = true, data = postResponse, call = call, message = "Posts fetched successfully", statusCode = HttpStatusCode.OK)
        } catch (e: Exception) {
            respondHelper(success = false, message = e.message ?: "Error fetching posts", call = call, statusCode = HttpStatusCode.InternalServerError)
        }
    }

    staticFiles("/resources", File(Strings.RESOURCES_DIR)){
        default("htmx.js")
    }
}
