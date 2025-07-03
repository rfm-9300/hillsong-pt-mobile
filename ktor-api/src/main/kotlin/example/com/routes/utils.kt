package example.com.routes

import com.auth0.jwt.JWT
import example.com.data.responses.ApiResponse
import example.com.data.responses.ApiResponseData
import example.com.plugins.Logger
import example.com.security.Roles
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

suspend fun respondHelper(
    call: RoutingCall,
    success: Boolean,
    message: String,
    data: ApiResponseData? = null,
    statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respond(statusCode, ApiResponse(
        success = success,
        message = message,
        data = data
    ))
}

// return the userID from the JWT token for authenticated routes
suspend fun getUserIdFromRequestToken(call: RoutingCall): String? {
    val principal = call.principal<JWTPrincipal>()
    if (principal == null) {
        respondHelper(
            call = call,
            success = false,
            message = "User not found",
            statusCode = HttpStatusCode.Unauthorized
        )
        return null
    }
    // check expiration
    if (principal.expiresAt?.before(java.util.Date()) == true) {
        respondHelper(
            call = call,
            success = false,
            message = "Token expired",
            statusCode = HttpStatusCode.Unauthorized
        )
        return null
    }

    val userId = principal.getClaim("userId", String::class)
    if (userId == null) {
        respondHelper(
            call = call,
            success = false,
            message = "User not found",
            statusCode = HttpStatusCode.Unauthorized
        )
        return null
    }
    return userId
}

fun isUserAdmin(userId: String?): Boolean {
    if (userId?.toIntOrNull() == null) return false
    return Roles.returnRole(userId.toInt()) == Roles.Role.ADMIN
}

fun RoutingContext.getUserIdFromCookies(): Int? {
    val token = call.request.cookies["authToken"]
    return getUserIdFromToken(token)
}

fun getUserIdFromToken(token: String?): Int? {
    try {
        val decodedJWT = JWT.decode(token)
        // check if the token has expired
        if (decodedJWT.expiresAt?.before(Date()) == true) {
            Logger.d("Token expired")
            return null
        }
        Logger.d("Decoded JWT claims: ${decodedJWT.claims}")
        Logger.d("UserID in token: ${decodedJWT.getClaim("userId").asString()}")
        return decodedJWT.getClaim("userId").asString().toIntOrNull()
    } catch (e: Exception){
        Logger.d("Error decoding JWT: $e")
        return null
    }
}