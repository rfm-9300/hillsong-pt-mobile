package rfm.hillsongptapp.core.data.repository

/**
 * Sealed class for repository-level kids results
 * Provides a clean abstraction over NetworkResult for the UI layer
 */
sealed class KidsResult<out T> {
    data class Success<T>(val data: T) : KidsResult<T>()
    data class Error(val message: String) : KidsResult<Nothing>()
    data class NetworkError(val message: String) : KidsResult<Nothing>()
    data object Loading : KidsResult<Nothing>()
}