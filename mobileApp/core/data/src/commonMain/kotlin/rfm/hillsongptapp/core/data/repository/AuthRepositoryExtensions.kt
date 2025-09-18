package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Extension functions to improve error handling in repositories
 */

/**
 * Converts NetworkResult to AuthResult with better error messages
 */
fun <T> NetworkResult<T>.toAuthResult(): AuthResult<T> {
    return when (this) {
        is NetworkResult.Success -> AuthResult.Success(data)
        is NetworkResult.Error -> AuthResult.NetworkError(exception.toUserFriendlyMessage())
        is NetworkResult.Loading -> AuthResult.Loading
    }
}

/**
 * Converts NetworkException to user-friendly error messages
 */
fun NetworkException.toUserFriendlyMessage(): String {
    return when (this) {
        is NetworkException.NoInternetConnection -> "No internet connection. Please check your network and try again."
        is NetworkException.Timeout -> "Request timed out. Please try again."
        is NetworkException.Unauthorized -> "Authentication failed. Please check your credentials."
        is NetworkException.HttpError -> when (statusCode.value) {
            400 -> "Invalid request. Please check your input."
            401 -> "Authentication required. Please log in."
            403 -> "Access denied. You don't have permission to perform this action."
            404 -> "Resource not found."
            500 -> "Server error. Please try again later."
            else -> "HTTP error (${statusCode.value}): $errorMessage"
        }
        is NetworkException.NetworkError -> "Network error: $errorMessage"
        is NetworkException.UnknownError -> "An unexpected error occurred: $errorMessage"
    }
}