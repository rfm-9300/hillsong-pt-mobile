package rfm.hillsongptapp.feature.kids.domain.error

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import co.touchlab.kermit.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ErrorHandlerTest {
    
    private val logger = Logger.withTag("ErrorHandlerTest")
    private val errorHandler = ErrorHandler(logger)
    
    @Test
    fun `handleError should return appropriate ErrorInfo for NetworkError`() {
        val error = KidsManagementError.NetworkError
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.NETWORK, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("Unable to connect"))
        assertEquals("Check your internet connection and tap 'Retry'", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for TimeoutError`() {
        val error = KidsManagementError.TimeoutError
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.NETWORK, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("took too long"))
        assertEquals("Tap 'Retry' to try again", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ChildNotFound`() {
        val error = KidsManagementError.ChildNotFound
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.NOT_FOUND, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("Child not found"))
        assertEquals("Refresh the child list or contact support", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ChildAlreadyExists`() {
        val error = KidsManagementError.ChildAlreadyExists
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.CONFLICT, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("already exists"))
        assertEquals("Review child information or edit existing child", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ChildAlreadyCheckedIn`() {
        val error = KidsManagementError.ChildAlreadyCheckedIn
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.BUSINESS_RULE, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("already checked into"))
        assertEquals("Check out the child from their current service first", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ServiceAtCapacity`() {
        val error = KidsManagementError.ServiceAtCapacity
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.BUSINESS_RULE, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("full capacity"))
        assertEquals("Try another service or wait for capacity to become available", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ValidationError`() {
        val error = KidsManagementError.ValidationError("name", "Name is required")
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.VALIDATION, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("Name is required"))
        assertEquals("Correct the highlighted fields and try again", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for Unauthorized`() {
        val error = KidsManagementError.Unauthorized
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.AUTHENTICATION, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("not authorized"))
        assertEquals("Sign out and sign in again", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ServerError`() {
        val error = KidsManagementError.ServerError
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.SERVER, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("server is experiencing issues"))
        assertEquals("Wait a moment and tap 'Retry'", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for WebSocketConnectionFailed`() {
        val error = KidsManagementError.WebSocketConnectionFailed
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.REAL_TIME, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("real-time updates"))
        assertEquals("Check your connection and tap 'Retry' for real-time updates", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ApiError with 5xx status`() {
        val error = KidsManagementError.ApiError(500, "Internal server error")
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.API, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("Server error (500)"))
        assertEquals("Tap 'Retry' or contact support", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for ApiError with 4xx status`() {
        val error = KidsManagementError.ApiError(400, "Bad request")
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.API, errorInfo.type)
        assertFalse(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("Server error (400)"))
        assertEquals("Contact support", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for UnknownError`() {
        val error = KidsManagementError.UnknownError("Something went wrong")
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.UNKNOWN, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("unexpected error"))
        assertEquals("Tap 'Retry' or contact support if the problem persists", errorInfo.suggestedAction)
    }
    
    @Test
    fun `handleError should return appropriate ErrorInfo for generic Exception`() {
        val error = RuntimeException("Generic error")
        val errorInfo = errorHandler.handleError(error)
        
        assertEquals(ErrorType.UNKNOWN, errorInfo.type)
        assertTrue(errorInfo.isRetryable)
        assertTrue(errorInfo.userMessage.contains("unexpected error"))
        assertEquals("Tap 'Retry' or restart the app if the problem persists", errorInfo.suggestedAction)
    }
    
    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            Result.success("Success")
        }
        
        val result = errorHandler.executeWithRetry(operation)
        
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should retry on retryable error and eventually succeed`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            if (attemptCount < 3) {
                Result.failure(KidsManagementError.NetworkError)
            } else {
                Result.success("Success after retry")
            }
        }
        
        val result = errorHandler.executeWithRetry(operation, maxAttempts = 3, initialDelayMs = 10L)
        
        assertTrue(result.isSuccess)
        assertEquals("Success after retry", result.getOrNull())
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should not retry on non-retryable error`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            Result.failure(KidsManagementError.ChildNotFound)
        }
        
        val result = errorHandler.executeWithRetry(operation, maxAttempts = 3, initialDelayMs = 10L)
        
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should fail after max attempts`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            Result.failure(KidsManagementError.NetworkError)
        }
        
        val result = errorHandler.executeWithRetry(operation, maxAttempts = 3, initialDelayMs = 10L)
        
        assertTrue(result.isFailure)
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should handle thrown exceptions`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            if (attemptCount < 2) {
                throw KidsManagementError.NetworkError
            } else {
                Result.success("Success after exception")
            }
        }
        
        val result = errorHandler.executeWithRetry(operation, maxAttempts = 3, initialDelayMs = 10L)
        
        assertTrue(result.isSuccess)
        assertEquals("Success after exception", result.getOrNull())
        assertEquals(2, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should not retry thrown non-retryable exceptions`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            throw KidsManagementError.ChildNotFound
        }
        
        val result = errorHandler.executeWithRetry(operation, maxAttempts = 3, initialDelayMs = 10L)
        
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `getRetryDelay should return appropriate delays for different error types`() {
        val networkDelay = errorHandler.getRetryDelay(KidsManagementError.NetworkError, 0)
        assertEquals(2000L, networkDelay)
        
        val timeoutDelay = errorHandler.getRetryDelay(KidsManagementError.TimeoutError, 0)
        assertEquals(3000L, timeoutDelay)
        
        val serverDelay = errorHandler.getRetryDelay(KidsManagementError.ServerError, 0)
        assertEquals(5000L, serverDelay)
        
        val serviceUnavailableDelay = errorHandler.getRetryDelay(KidsManagementError.ServiceUnavailable, 0)
        assertEquals(10000L, serviceUnavailableDelay)
        
        val unknownDelay = errorHandler.getRetryDelay(KidsManagementError.UnknownError("test"), 0)
        assertEquals(1000L, unknownDelay)
    }
    
    @Test
    fun `getRetryDelay should apply exponential backoff`() {
        val baseDelay = errorHandler.getRetryDelay(KidsManagementError.NetworkError, 0)
        val secondAttemptDelay = errorHandler.getRetryDelay(KidsManagementError.NetworkError, 1)
        val thirdAttemptDelay = errorHandler.getRetryDelay(KidsManagementError.NetworkError, 2)
        
        assertEquals(2000L, baseDelay)
        assertEquals(4000L, secondAttemptDelay)
        assertEquals(8000L, thirdAttemptDelay)
    }
}