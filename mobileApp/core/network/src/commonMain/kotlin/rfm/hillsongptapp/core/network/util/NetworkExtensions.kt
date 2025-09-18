package rfm.hillsongptapp.core.network.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.result.*

/**
 * Extension functions for NetworkResult to make working with network responses easier
 */

/**
 * Maps a successful result to another type
 */
inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}

/**
 * Returns the data if successful, or null if error/loading
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        else -> null
    }
}

/**
 * Returns the data if successful, or the default value if error/loading
 */
fun <T> NetworkResult<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is NetworkResult.Success -> data
        else -> defaultValue
    }
}

/**
 * Executes the given action if the result is successful
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Executes the given action if the result is an error
 */
inline fun <T> NetworkResult<T>.onError(action: (rfm.hillsongptapp.core.network.result.NetworkException) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(exception)
    }
    return this
}

/**
 * Executes the given action if the result is loading
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

/**
 * Converts a Flow to emit NetworkResult with proper error handling
 */
fun <T> Flow<T>.asNetworkResult(): Flow<NetworkResult<T>> {
    return this
        .map<T, NetworkResult<T>> { NetworkResult.Success(it) }
        .onStart { emit(NetworkResult.Loading) }
        .catch { emit(NetworkResult.Error(it.toNetworkException())) }
}

/**
 * Filters out loading states from a NetworkResult Flow
 */
fun <T> Flow<NetworkResult<T>>.filterLoading(): Flow<NetworkResult<T>> {
    return this.map { result ->
        when (result) {
            is NetworkResult.Loading -> return@map result
            else -> result
        }
    }
}

/**
 * Maps only successful results in a Flow
 */
inline fun <T, R> Flow<NetworkResult<T>>.mapSuccess(crossinline transform: (T) -> R): Flow<NetworkResult<R>> {
    return this.map { result ->
        result.mapSuccess(transform)
    }
}

/**
 * Utility function to check if network result indicates authentication is required
 */
fun NetworkResult<*>.requiresAuthentication(): Boolean {
    return this is NetworkResult.Error && 
           exception is rfm.hillsongptapp.core.network.result.NetworkException.Unauthorized
}

/**
 * Utility function to check if network result indicates no internet connection
 */
fun NetworkResult<*>.isNetworkUnavailable(): Boolean {
    return this is NetworkResult.Error && 
           (exception is rfm.hillsongptapp.core.network.result.NetworkException.NoInternetConnection ||
            exception is rfm.hillsongptapp.core.network.result.NetworkException.Timeout)
}