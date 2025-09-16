package rfm.com.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import rfm.com.dto.ApiResponse
import java.time.LocalDateTime
import rfm.com.exception.*
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException

/**
 * Global exception handler for all REST controllers
 * Provides consistent error response format across the application
 */
@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * Helper function to create error responses
     */
    private fun <T> createErrorResponse(message: String): ApiResponse<T> {
        return ApiResponse(
            success = false,
            message = message,
            data = null,
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * Helper function to create validation error responses
     */
    private fun createValidationErrorResponse(message: String, errors: Map<String, String>): ApiResponse<Map<String, String>> {
        return ApiResponse(
            success = false,
            message = message,
            data = errors,
            timestamp = LocalDateTime.now()
        )
    }
    
    // ========== Business Logic Exceptions ==========
    
    /**
     * Handle entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException::class, rfm.com.exception.EntityNotFoundException::class)
    fun handleEntityNotFound(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Entity not found: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Entity not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
    
    /**
     * Handle unauthorized action exceptions
     */
    @ExceptionHandler(UnauthorizedActionException::class)
    fun handleUnauthorizedAction(
        ex: UnauthorizedActionException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Unauthorized action: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Unauthorized action")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }
    
    /**
     * Handle business rule violation exceptions
     */
    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolation(
        ex: BusinessRuleViolationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business rule violation: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Business rule violation")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle entity already exists exceptions
     */
    @ExceptionHandler(EntityAlreadyExistsException::class)
    fun handleEntityAlreadyExists(
        ex: EntityAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Entity already exists: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Entity already exists")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    /**
     * Handle conflict exceptions
     */
    @ExceptionHandler(ConflictException::class)
    fun handleConflict(
        ex: ConflictException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Conflict: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Conflict occurred")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    /**
     * Handle invalid data exceptions
     */
    @ExceptionHandler(InvalidDataException::class)
    fun handleInvalidData(
        ex: InvalidDataException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Invalid data: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Invalid data provided")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle service operation exceptions
     */
    @ExceptionHandler(ServiceOperationException::class)
    fun handleServiceOperation(
        ex: ServiceOperationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Service operation failed: ${ex.message}", ex)
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Service operation failed")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
    
    /**
     * Handle external service exceptions
     */
    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalService(
        ex: ExternalServiceException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("External service error: ${ex.message}", ex)
        
        val response = createErrorResponse<Nothing>("External service temporarily unavailable")
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response)
    }
    
    /**
     * Handle file operation exceptions
     */
    @ExceptionHandler(FileOperationException::class)
    fun handleFileOperation(
        ex: FileOperationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("File operation failed: ${ex.message}", ex)
        
        val response = createErrorResponse<Nothing>(ex.message ?: "File operation failed")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle capacity exceeded exceptions
     */
    @ExceptionHandler(CapacityExceededException::class)
    fun handleCapacityExceeded(
        ex: CapacityExceededException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Capacity exceeded: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Capacity exceeded")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    /**
     * Handle unavailable exceptions
     */
    @ExceptionHandler(UnavailableException::class)
    fun handleUnavailable(
        ex: UnavailableException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Resource unavailable: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Resource unavailable")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    // ========== Validation Exceptions ==========
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        logger.warn("Validation error: ${ex.message}")
        
        val errors = mutableMapOf<String, String>()
        
        // Handle field errors
        ex.bindingResult.fieldErrors.forEach { error: FieldError ->
            val fieldName = error.field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }
        
        // Handle global errors
        ex.bindingResult.globalErrors.forEach { error ->
            val objectName = error.objectName
            val errorMessage = error.defaultMessage ?: "Invalid object"
            errors[objectName] = errorMessage
        }
        
        val response = createValidationErrorResponse("Validation failed", errors)
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle constraint violation exceptions (for @Validated on method parameters)
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        logger.warn("Constraint violation: ${ex.message}")
        
        val errors = ex.constraintViolations.associate { violation ->
            val propertyPath = violation.propertyPath.toString()
            val message = violation.message ?: "Invalid value"
            propertyPath to message
        }
        
        val response = createValidationErrorResponse("Validation failed", errors)
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle missing request parameter exceptions
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        logger.warn("Missing parameter: ${ex.message}")
        
        val errors = mapOf(ex.parameterName to "Parameter is required")
        val response = createValidationErrorResponse("Missing required parameter", errors)
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle method argument type mismatch exceptions
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        logger.warn("Type mismatch: ${ex.message}")
        
        val paramName = ex.name
        val requiredType = ex.requiredType?.simpleName ?: "unknown"
        val errors = mapOf(paramName to "Invalid type. Expected: $requiredType")
        
        val response = createValidationErrorResponse("Invalid parameter type", errors)
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle HTTP message not readable exceptions (malformed JSON)
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Message not readable: ${ex.message}")
        
        val response = createErrorResponse<Nothing>("Invalid request format. Please check your JSON syntax.")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle file upload size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceeded(
        ex: MaxUploadSizeExceededException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("File upload size exceeded: ${ex.message}")
        
        val response = createErrorResponse<Nothing>("File size exceeds maximum allowed limit")
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response)
    }
    
    
    // ========== Security Exceptions ==========
    
    /**
     * Handle authentication failures
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(
        ex: BadCredentialsException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Authentication failed: ${ex.message}")
        
        val response = createErrorResponse<Nothing>("Invalid credentials")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }
    
    /**
     * Handle user not found exceptions
     */
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUserNotFound(
        ex: UsernameNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("User not found: ${ex.message}")
        
        val response = createErrorResponse<Nothing>("User not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
    
    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Access denied: ${ex.message}")
        
        val response = createErrorResponse<Nothing>("Access denied")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }
    
    // ========== General Exceptions ==========
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal argument: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Invalid argument")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: IllegalStateException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal state: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Invalid operation state")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
    
    /**
     * Handle business exceptions (catch-all for custom business exceptions)
     */
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business exception: ${ex.message}")
        
        val response = createErrorResponse<Nothing>(ex.message ?: "Business operation failed")
        return ResponseEntity.badRequest().body(response)
    }
    
    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Runtime exception occurred", ex)
        
        val response = createErrorResponse<Nothing>("An error occurred while processing your request")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
    
    /**
     * Handle all other exceptions (fallback)
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception occurred", ex)
        
        val response = createErrorResponse<Nothing>("An unexpected error occurred")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
