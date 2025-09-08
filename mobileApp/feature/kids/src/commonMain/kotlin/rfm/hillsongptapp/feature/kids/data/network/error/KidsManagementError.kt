package rfm.hillsongptapp.feature.kids.data.network.error

/**
 * Sealed class representing different types of errors that can occur in kids management operations
 */
sealed class KidsManagementError : Exception() {
    
    /**
     * Network-related errors
     */
    data object NetworkError : KidsManagementError() {
        override val message: String = "Network connection error occurred"
    }
    
    data object TimeoutError : KidsManagementError() {
        override val message: String = "Request timed out"
    }
    
    /**
     * Child-related errors
     */
    data object ChildNotFound : KidsManagementError() {
        override val message: String = "Child not found"
    }
    
    data object ChildAlreadyExists : KidsManagementError() {
        override val message: String = "Child with this information already exists"
    }
    
    data object ChildAlreadyCheckedIn : KidsManagementError() {
        override val message: String = "Child is already checked into a service"
    }
    
    data object ChildNotCheckedIn : KidsManagementError() {
        override val message: String = "Child is not currently checked into any service"
    }
    
    /**
     * Service-related errors
     */
    data object ServiceNotFound : KidsManagementError() {
        override val message: String = "Service not found"
    }
    
    data object ServiceAtCapacity : KidsManagementError() {
        override val message: String = "Service has reached maximum capacity"
    }
    
    data object ServiceNotAcceptingCheckIns : KidsManagementError() {
        override val message: String = "Service is not currently accepting check-ins"
    }
    
    data object InvalidAgeForService : KidsManagementError() {
        override val message: String = "Child does not meet age requirements for this service"
    }
    
    /**
     * Registration and validation errors
     */
    data object RegistrationFailed : KidsManagementError() {
        override val message: String = "Failed to register child"
    }
    
    data class ValidationError(val field: String, val reason: String) : KidsManagementError() {
        override val message: String = "Validation error in field '$field': $reason"
    }
    
    /**
     * Authentication and authorization errors
     */
    data object Unauthorized : KidsManagementError() {
        override val message: String = "User is not authorized to perform this operation"
    }
    
    data object Forbidden : KidsManagementError() {
        override val message: String = "Access to this resource is forbidden"
    }
    
    /**
     * Server errors
     */
    data object ServerError : KidsManagementError() {
        override val message: String = "Internal server error occurred"
    }
    
    data object ServiceUnavailable : KidsManagementError() {
        override val message: String = "Service is temporarily unavailable"
    }
    
    /**
     * Real-time connection errors
     */
    data object WebSocketConnectionFailed : KidsManagementError() {
        override val message: String = "Failed to establish real-time connection"
    }
    
    data object WebSocketDisconnected : KidsManagementError() {
        override val message: String = "Real-time connection was lost"
    }
    
    /**
     * Generic errors
     */
    data class UnknownError(val reason: String) : KidsManagementError() {
        override val message: String = "Unknown error occurred: $reason"
    }
    
    data class ApiError(val code: Int, val reason: String) : KidsManagementError() {
        override val message: String = "API error (code $code): $reason"
    }
}

/**
 * Extension function to convert HTTP status codes to appropriate KidsManagementError
 */
fun Int.toKidsManagementError(message: String = ""): KidsManagementError {
    return when (this) {
        400 -> KidsManagementError.ValidationError("request", message.ifEmpty { "Bad request" })
        401 -> KidsManagementError.Unauthorized
        403 -> KidsManagementError.Forbidden
        404 -> KidsManagementError.ChildNotFound // Default to child not found, can be overridden
        409 -> KidsManagementError.ChildAlreadyExists
        422 -> KidsManagementError.ValidationError("data", message.ifEmpty { "Unprocessable entity" })
        429 -> KidsManagementError.ServiceUnavailable
        500 -> KidsManagementError.ServerError
        503 -> KidsManagementError.ServiceUnavailable
        else -> KidsManagementError.ApiError(this, message.ifEmpty { "HTTP error $this" })
    }
}