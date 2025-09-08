package rfm.hillsongptapp.feature.kids.domain.error

import kotlinx.coroutines.delay
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import co.touchlab.kermit.Logger
import kotlin.math.pow
/**
 * Centralized error handler for kids management operations
 */
class ErrorHandler(
    private val logger: Logger
) {
    
    companion object {
        private const val DEFAULT_RETRY_ATTEMPTS = 3
        private const val DEFAULT_RETRY_DELAY_MS = 1000L
        private const val EXPONENTIAL_BACKOFF_MULTIPLIER = 2.0
    }
    
    /**
     * Handle and categorize errors with appropriate user messages
     */
    fun handleError(error: Throwable): ErrorInfo {
        logger.e(error) { "Handling error: ${error.message}" }
        
        return when (error) {
            is KidsManagementError -> handleKidsManagementError(error)
            else -> handleGenericError(error)
        }
    }
    
    /**
     * Handle specific kids management errors
     */
    private fun handleKidsManagementError(error: KidsManagementError): ErrorInfo {
        return when (error) {
            // Network errors - retryable
            is KidsManagementError.NetworkError -> ErrorInfo(
                type = ErrorType.NETWORK,
                userMessage = "Unable to connect to the server. Please check your internet connection and try again.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Check your internet connection and tap 'Retry'"
            )
            
            is KidsManagementError.TimeoutError -> ErrorInfo(
                type = ErrorType.NETWORK,
                userMessage = "The request took too long to complete. Please try again.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Tap 'Retry' to try again"
            )
            
            // Child-related errors - mostly non-retryable
            is KidsManagementError.ChildNotFound -> ErrorInfo(
                type = ErrorType.NOT_FOUND,
                userMessage = "Child not found. The child may have been removed or you may not have access.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Refresh the child list or contact support"
            )
            
            is KidsManagementError.ChildAlreadyExists -> ErrorInfo(
                type = ErrorType.CONFLICT,
                userMessage = "A child with this information already exists. Please check the details and try again.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Review child information or edit existing child"
            )
            
            is KidsManagementError.ChildAlreadyCheckedIn -> ErrorInfo(
                type = ErrorType.BUSINESS_RULE,
                userMessage = "This child is already checked into a service. Please check them out first.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Check out the child from their current service first"
            )
            
            is KidsManagementError.ChildNotCheckedIn -> ErrorInfo(
                type = ErrorType.BUSINESS_RULE,
                userMessage = "This child is not currently checked into any service.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Check the child's current status"
            )
            
            // Service-related errors
            is KidsManagementError.ServiceNotFound -> ErrorInfo(
                type = ErrorType.NOT_FOUND,
                userMessage = "The selected service is no longer available. Please refresh and try another service.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Refresh services list and select another service"
            )
            
            is KidsManagementError.ServiceAtCapacity -> ErrorInfo(
                type = ErrorType.BUSINESS_RULE,
                userMessage = "This service is at full capacity. Please try another service or wait for availability.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Try another service or wait for capacity to become available"
            )
            
            is KidsManagementError.ServiceNotAcceptingCheckIns -> ErrorInfo(
                type = ErrorType.BUSINESS_RULE,
                userMessage = "This service is not currently accepting check-ins. Please contact staff or try another service.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Contact church staff or select another service"
            )
            
            is KidsManagementError.InvalidAgeForService -> ErrorInfo(
                type = ErrorType.BUSINESS_RULE,
                userMessage = "This child does not meet the age requirements for this service.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Select an age-appropriate service for your child"
            )
            
            // Validation errors
            is KidsManagementError.ValidationError -> ErrorInfo(
                type = ErrorType.VALIDATION,
                userMessage = "Please check the information you entered: ${error.reason}",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Correct the highlighted fields and try again"
            )
            
            is KidsManagementError.RegistrationFailed -> ErrorInfo(
                type = ErrorType.OPERATION_FAILED,
                userMessage = "Unable to register the child. Please check the information and try again.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Review child information and tap 'Retry'"
            )
            
            // Authentication errors
            is KidsManagementError.Unauthorized -> ErrorInfo(
                type = ErrorType.AUTHENTICATION,
                userMessage = "You are not authorized to perform this action. Please sign in again.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Sign out and sign in again"
            )
            
            is KidsManagementError.Forbidden -> ErrorInfo(
                type = ErrorType.AUTHENTICATION,
                userMessage = "You don't have permission to access this feature. Please contact support.",
                technicalMessage = error.message,
                isRetryable = false,
                suggestedAction = "Contact church administration for access"
            )
            
            // Server errors - retryable
            is KidsManagementError.ServerError -> ErrorInfo(
                type = ErrorType.SERVER,
                userMessage = "The server is experiencing issues. Please try again in a moment.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Wait a moment and tap 'Retry'"
            )
            
            is KidsManagementError.ServiceUnavailable -> ErrorInfo(
                type = ErrorType.SERVER,
                userMessage = "The service is temporarily unavailable. Please try again later.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Try again in a few minutes"
            )
            
            // Real-time connection errors
            is KidsManagementError.WebSocketConnectionFailed -> ErrorInfo(
                type = ErrorType.REAL_TIME,
                userMessage = "Unable to establish real-time updates. Some features may be limited.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Check your connection and tap 'Retry' for real-time updates"
            )
            
            is KidsManagementError.WebSocketDisconnected -> ErrorInfo(
                type = ErrorType.REAL_TIME,
                userMessage = "Real-time connection lost. Updates may be delayed.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Tap 'Reconnect' to restore real-time updates"
            )
            
            // API errors
            is KidsManagementError.ApiError -> ErrorInfo(
                type = ErrorType.API,
                userMessage = "Server error (${error.code}). Please try again or contact support if the problem persists.",
                technicalMessage = error.message,
                isRetryable = error.code in 500..599,
                suggestedAction = if (error.code in 500..599) "Tap 'Retry' or contact support" else "Contact support"
            )
            
            // Unknown errors
            is KidsManagementError.UnknownError -> ErrorInfo(
                type = ErrorType.UNKNOWN,
                userMessage = "An unexpected error occurred. Please try again or contact support.",
                technicalMessage = error.message,
                isRetryable = true,
                suggestedAction = "Tap 'Retry' or contact support if the problem persists"
            )
        }
    }
    
    /**
     * Handle generic (non-kids management) errors
     */
    private fun handleGenericError(error: Throwable): ErrorInfo {
        return ErrorInfo(
            type = ErrorType.UNKNOWN,
            userMessage = "An unexpected error occurred. Please try again.",
            technicalMessage = error.message ?: "Unknown error",
            isRetryable = true,
            suggestedAction = "Tap 'Retry' or restart the app if the problem persists"
        )
    }
    
    /**
     * Execute operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        operation: suspend () -> Result<T>,
        maxAttempts: Int = DEFAULT_RETRY_ATTEMPTS,
        initialDelayMs: Long = DEFAULT_RETRY_DELAY_MS,
        shouldRetry: (Throwable) -> Boolean = ::isRetryableError
    ): Result<T> {
        var lastError: Throwable? = null
        var delayMs = initialDelayMs
        
        repeat(maxAttempts) { attempt ->
            try {
                val result = operation()
                if (result.isSuccess) {
                    logger.d { "Operation succeeded on attempt ${attempt + 1}" }
                    return result
                }
                
                val error = result.exceptionOrNull()
                if (error != null && shouldRetry(error)) {
                    lastError = error
                    logger.w { "Operation failed on attempt ${attempt + 1}, will retry: ${error.message}" }
                    
                    if (attempt < maxAttempts - 1) {
                        delay(delayMs)
                        delayMs = (delayMs * EXPONENTIAL_BACKOFF_MULTIPLIER).toLong()
                    }
                } else {
                    logger.w { "Operation failed on attempt ${attempt + 1}, not retrying: ${error?.message}" }
                    return result
                }
            } catch (e: Exception) {
                lastError = e
                if (shouldRetry(e)) {
                    logger.w { "Operation threw exception on attempt ${attempt + 1}, will retry: ${e.message}" }
                    
                    if (attempt < maxAttempts - 1) {
                        delay(delayMs)
                        delayMs = (delayMs * EXPONENTIAL_BACKOFF_MULTIPLIER).toLong()
                    }
                } else {
                    logger.w { "Operation threw exception on attempt ${attempt + 1}, not retrying: ${e.message}" }
                    return Result.failure(e)
                }
            }
        }
        
        logger.e { "Operation failed after $maxAttempts attempts" }
        return Result.failure(lastError ?: Exception("Operation failed after $maxAttempts attempts"))
    }
    
    /**
     * Determine if an error is retryable
     */
    private fun isRetryableError(error: Throwable): Boolean {
        return when (error) {
            is KidsManagementError.NetworkError,
            is KidsManagementError.TimeoutError,
            is KidsManagementError.ServerError,
            is KidsManagementError.ServiceUnavailable,
            is KidsManagementError.WebSocketConnectionFailed,
            is KidsManagementError.WebSocketDisconnected -> true
            
            is KidsManagementError.ApiError -> error.code in 500..599
            
            is KidsManagementError.ServiceNotFound,
            is KidsManagementError.ServiceAtCapacity,
            is KidsManagementError.RegistrationFailed -> true
            
            else -> false
        }
    }
    
    /**
     * Get retry delay for specific error types
     */
    fun getRetryDelay(error: Throwable, attempt: Int): Long {
        val baseDelay = when (error) {
            is KidsManagementError.NetworkError -> 2000L
            is KidsManagementError.TimeoutError -> 3000L
            is KidsManagementError.ServerError -> 5000L
            is KidsManagementError.ServiceUnavailable -> 10000L
            else -> DEFAULT_RETRY_DELAY_MS
        }
        
        return baseDelay.toLong()
    }
}