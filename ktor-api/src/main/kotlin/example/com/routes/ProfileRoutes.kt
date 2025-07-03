package example.com.routes


import example.com.data.db.user.UserRepository
import example.com.plugins.Logger
import example.com.web.pages.profilePage.profilePage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray

fun Route.profileRoutes(
    userRepository: UserRepository
){

    /**
     * UI Routes
     */
    get(Routes.Ui.Profile.ROOT) {
        val token = call.request.cookies["authToken"]
        Logger.d("Token: $token")
        val userId = getUserIdFromToken(token) ?: return@get Logger.d("Invalid token")
        val userProfile = userRepository.getUserProfile(userId) ?: return@get Logger.d("User not found")
        call.respondHtml(HttpStatusCode.OK){
            profilePage(userProfile)
        }
    }


    /**
     * Api Routes
     */
    authenticate {
        post(Routes.Api.Profile.UPDATE){
            val userId = getUserIdFromRequestToken(call) ?: return@post respondHelper(call, false, "User not found", statusCode = HttpStatusCode.Unauthorized)
            val userProfile = userRepository.getUserProfile(userId.toInt()) ?: return@post respondHelper(call, false, "User not found", statusCode = HttpStatusCode.NotFound)
            try {
                val multiPart = call.receiveMultipart()
                Logger.d("Updating profile, multipart: $multiPart")
                var image = userProfile.profileImagePath

                multiPart.forEachPart {
                    when(it){
                        is PartData.FormItem -> {
                            Logger.d("Form field: ${it.name} = ${it.value}")
                        }
                        is PartData.FileItem -> {
                            if(it.name == "image"){
                                val fileName = it.originalFileName ?: "unnamed.jpg"
                                val fileBytes = it.provider().readRemaining().readByteArray()
                                image = ImageFileHandler.saveImage(fileBytes, fileName)
                                Logger.d("Image saved: $image")
                            }
                        }
                        else -> {
                            Logger.d("Unknown part: $it")
                        }
                    }
                }
                // After processing, check if image was successfully updated
                if (image == userProfile.profileImagePath) respondHelper(call = call, success = false, message = "No image uploaded or image save failed")

                val newProfile = userProfile.copy(profileImagePath = image)

                val isUpdated = userRepository.updateUserProfile(newProfile)

                respondHelper(
                    call = call,
                    success = isUpdated,
                    message = if(isUpdated) "Profile updated" else "Profile not updated",
                    statusCode = if(isUpdated) HttpStatusCode.OK else HttpStatusCode.InternalServerError
                )

            }catch (e: Exception){
                respondHelper(
                    call = call,
                    success = false,
                    message = e.message ?: "Error updating profile",
                    statusCode = HttpStatusCode.InternalServerError
                )
            }
        }
    }

}


