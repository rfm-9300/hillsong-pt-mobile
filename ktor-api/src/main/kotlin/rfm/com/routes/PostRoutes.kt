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
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.io.File

fun Route.postRoutes(
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
                val userId = getUserIdFromRequestToken(call) ?: return@post
                if (!isUserAdmin(userId)) {
                    return@post respondHelper(success = false, message = "Unauthorized", call = call, statusCode = HttpStatusCode.Unauthorized)
                }
                Logger.d("User $userId is authorized to create post")

                val multiPart = call.receiveMultipart()
                var title = ""
                var content = ""
                var imagePath: String? = null

                multiPart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "title" -> title = part.value
                                "content" -> content = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if (part.name == "image") {
                                val fileName = part.originalFileName as String
                                val fileBytes = part.streamProvider().readBytes()
                                val uploadDir = File("uploads/posts")
                                if (!uploadDir.exists()) {
                                    uploadDir.mkdirs()
                                }
                                val serverFile = File(uploadDir, fileName)
                                serverFile.writeBytes(fileBytes)
                                imagePath = "uploads/posts/$fileName"
                            }
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    val newPost = Post(
                        userId = userId.toInt(),
                        title = title,
                        content = content,
                        headerImagePath = imagePath ?: "default-header.jpg"
                    )
                    val createdPost = postRepository.addPost(newPost)
                    respondHelper(success = createdPost != null, message = if (createdPost != null) "Post created" else "Error creating post",  call = call, statusCode = if (createdPost != null) HttpStatusCode.OK else HttpStatusCode.InternalServerError)
                } else {
                    respondHelper(success = false, message = "Missing title or content", call = call, statusCode = HttpStatusCode.BadRequest)
                }
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

    get(Routes.Api.Post.GET) {
        Logger.d("Get post request")
        try {
            val postId = call.parameters["id"]?.toIntOrNull()
            if (postId == null) {
                respondHelper(success = false, message = "Invalid post ID", call = call, statusCode = HttpStatusCode.BadRequest)
                return@get
            }

            val post = postRepository.getPostById(postId)
            if (post != null) {
                respondHelper(success = true, data = ApiResponseData.SinglePostResponse(post), call = call, message = "Post fetched successfully", statusCode = HttpStatusCode.OK)
            } else {
                respondHelper(success = false, message = "Post not found", call = call, statusCode = HttpStatusCode.NotFound)
            }
        } catch (e: Exception) {
            Logger.d("Error fetching post: ${e.message}")
            respondHelper(success = false, message = e.message ?: "Error fetching post", call = call, statusCode = HttpStatusCode.InternalServerError)
        }
    }

    staticFiles("/resources", File(Strings.RESOURCES_DIR)){
        default("htmx.js")
    }
}
