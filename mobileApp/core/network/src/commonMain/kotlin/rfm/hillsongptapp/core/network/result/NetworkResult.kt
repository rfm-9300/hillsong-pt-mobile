package rfm.hillsongptapp.core.network.result

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode

/**
 * A sealed class representing the result of a network operation
 * Following modern Android best practices for error handling
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

/**
 * Custom exception types for better error handling
 */
sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    data class HttpError(
        val statusCode: HttpStatusCode,
        val errorMessage: String
    ) : NetworkException("HTTP ${statusCode.value}: $errorMessage")
    
    data class NetworkError(
        val errorMessage: String
    ) : NetworkException("Network error: $errorMessage")
    
    data class UnknownError(
        val errorMessage: String
    ) : NetworkException("Unknown error: $errorMessage")
    
    data object NoInternetConnection : NetworkException("No internet connection")
    data object Timeout : NetworkException("Request timeout")
    data object Unauthorized : NetworkException("Unauthorized access")
}

/**
 * Extension function to convert exceptions to NetworkException
 */
suspend fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is ClientRequestException -> {
            // Try to parse error message from response body
            val errorMessage = try {
                val errorBody = response.body<rfm.hillsongptapp.core.network.ktor.responses.ApiResponse<Any>>()
                errorBody.message ?: response.status.description
            } catch (e: Exception) {
                response.status.description
            }
            
            when (response.status) {
                HttpStatusCode.Unauthorized -> NetworkException.Unauthorized
                else -> NetworkException.HttpError(
                    statusCode = response.status,
                    errorMessage = errorMessage
                )
            }
        }
        is ServerResponseException -> {
            // Try to parse error message from response body
            val errorMessage = try {
                val errorBody = response.body<rfm.hillsongptapp.core.network.ktor.responses.ApiResponse<Any>>()
                errorBody.message ?: response.status.description
            } catch (e: Exception) {
                response.status.description
            }
            
            NetworkException.HttpError(
                statusCode = response.status,
                errorMessage = errorMessage
            )
        }
        else -> {
            val message = this.message?.lowercase() ?: ""
            when {
                message.contains("unknown host") || message.contains("no address associated") -> 
                    NetworkException.NoInternetConnection
                message.contains("timeout") -> NetworkException.Timeout
                message.contains("connection") && (message.contains("refused") || message.contains("timeout")) -> 
                    NetworkException.NetworkError("Cannot connect to server")
                else -> NetworkException.UnknownError(this.message ?: "Unknown error occurred")
            }
        }
    }
}

/**
 * Extension functions for NetworkResult
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (NetworkException) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(exception)
    return this
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}