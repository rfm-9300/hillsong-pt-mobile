package example.com.routes

import example.com.data.db.event.EventRepository
import example.com.data.db.post.Post
import example.com.data.db.post.PostRepository
import example.com.data.db.user.UserRepository
import example.com.data.requests.CreatePostRequest
import example.com.data.requests.PostRequest
import example.com.data.responses.ApiResponseData
import example.com.data.utils.SseAction
import example.com.data.utils.SseManager
import example.com.plugins.Logger
import example.com.web.pages.homePage.homePage
import example.com.web.components.topbar.profileMenu
import example.com.web.pages.homePage.homeTab.createPostTab
import example.com.web.pages.homePage.homeTab.homeTab
import example.com.web.utils.Strings
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.html.body
import java.io.File

fun Route.homeRoutes(
    sseManager: SseManager,
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
                if (isDeleted) {
                    sseManager.emitEvent(SseAction.RefreshPosts)
                }
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



    /**
     * Ui Routes
     */

    get("/") {
        call.respondHtml(HttpStatusCode.OK){
            homePage(
                eventRepository = eventRepository,
                isAdminRequest = isUserAdmin(getUserIdFromCookies().toString())
            )
        }
    }



    get(Routes.Ui.Home.HOME) {
        call.respondHtml(HttpStatusCode.OK){
            body {
                homeTab(
                    isAdminRequest = isUserAdmin(getUserIdFromCookies().toString())
                )
            }
        }
    }

    get(Routes.Ui.Home.CREATE_POST) {
        call.respondHtml(HttpStatusCode.OK){
            createPostTab()
        }
    }

    authenticate {
        get(Routes.Ui.Home.PROFILE_MENU) {
            val principal = call.principal<JWTPrincipal>() ?: return@get respondHelper(success = false, message = "User not found", call = call)
            val userId = principal.getClaim("userId", String::class) ?: return@get respondHelper(success = false, message = "User not found", call = call)

            val userProfile = userRepository.getUserProfile(userId.toInt()) ?: return@get respondHelper(success = false, message = "User not found", call = call)

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    profileMenu(userProfile)
                }
            }
        }
    }


    sse(Routes.Sse.SSE_CONNECTION) {
        try {
            // Send initial connection message
            send(ServerSentEvent(data = "sse connected"))

            // Main collection job
            sseManager.sseAction
                .onEach { action ->
                    send(ServerSentEvent(
                        event = "sse-action",
                        data = action
                    ))
                }
                .catch { e ->
                    send(ServerSentEvent(data = "Error: ${e.message}", event = "error"))
                }
                .collect {
                    // Keep the connection alive while collecting the flow
                }
        } catch (e: CancellationException) {
            // Normal disconnection, no need to send error
            Logger.d("Client disconnected from SSE")
        } catch (e: Exception) {
            Logger.d("SSE error ${e.message}")
            try {
                send(ServerSentEvent(data = "Error: ${e.message}", event = "error"))
            } catch (_: Exception) {
                // Ignore send errors on connection close
            }
        }
    }

    staticFiles("/resources", File(Strings.RESOURCES_DIR)){
        default("htmx.js")
    }
}
