package rfm.com.routes

import rfm.com.data.responses.ApiResponseData
import rfm.com.plugins.Logger
import io.ktor.http.*
import io.ktor.server.routing.*
import rfm.com.data.db.user.UserRepository

fun Route.userRoutes(
    userRepository: UserRepository
) {
    get(Routes.Api.User.LIST) {
        Logger.d("List users request")
        try {
            val users = userRepository.getAllUsers()
            if (users.isEmpty()) {
                Logger.d("No users found")
                return@get respondHelper(
                    call = call,
                    success = false,
                    message = "No users found",
                    statusCode = HttpStatusCode.NotFound
                )
            }
            Logger.d("Users fetched successfully: ${users} users found")
            respondHelper(
                call = call,
                success = true,
                message = "Users fetched successfully",
                data = ApiResponseData.UserListResponse(users),
            )
        } catch (e: Exception) {
            Logger.d("Error fetching users: ${e.message}")
            respondHelper(
                call = call,
                success = false,
                message = e.message ?: "Error fetching users",
                statusCode = HttpStatusCode.InternalServerError
            )
        }
    }
}
