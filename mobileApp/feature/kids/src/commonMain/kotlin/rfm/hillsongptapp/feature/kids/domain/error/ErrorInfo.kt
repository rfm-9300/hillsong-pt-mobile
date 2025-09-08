package rfm.hillsongptapp.feature.kids.domain.error

/**
 * Comprehensive error information for user-friendly error handling
 */
data class ErrorInfo(
    val type: ErrorType,
    val userMessage: String,
    val technicalMessage: String,
    val isRetryable: Boolean,
    val suggestedAction: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Get a short summary of the error
     */
    val summary: String
        get() = when (type) {
            ErrorType.NETWORK -> "Connection Error"
            ErrorType.VALIDATION -> "Invalid Information"
            ErrorType.BUSINESS_RULE -> "Operation Not Allowed"
            ErrorType.NOT_FOUND -> "Not Found"
            ErrorType.CONFLICT -> "Conflict"
            ErrorType.AUTHENTICATION -> "Access Denied"
            ErrorType.SERVER -> "Server Error"
            ErrorType.REAL_TIME -> "Connection Issue"
            ErrorType.API -> "Service Error"
            ErrorType.OPERATION_FAILED -> "Operation Failed"
            ErrorType.UNKNOWN -> "Unexpected Error"
        }
    
    /**
     * Get appropriate icon for the error type
     */
    val iconType: ErrorIconType
        get() = when (type) {
            ErrorType.NETWORK -> ErrorIconType.NETWORK
            ErrorType.VALIDATION -> ErrorIconType.WARNING
            ErrorType.BUSINESS_RULE -> ErrorIconType.INFO
            ErrorType.NOT_FOUND -> ErrorIconType.SEARCH
            ErrorType.CONFLICT -> ErrorIconType.WARNING
            ErrorType.AUTHENTICATION -> ErrorIconType.LOCK
            ErrorType.SERVER -> ErrorIconType.SERVER
            ErrorType.REAL_TIME -> ErrorIconType.SYNC
            ErrorType.API -> ErrorIconType.SERVER
            ErrorType.OPERATION_FAILED -> ErrorIconType.ERROR
            ErrorType.UNKNOWN -> ErrorIconType.ERROR
        }
    
    /**
     * Get severity level for the error
     */
    val severity: ErrorSeverity
        get() = when (type) {
            ErrorType.NETWORK, ErrorType.SERVER, ErrorType.API -> ErrorSeverity.HIGH
            ErrorType.AUTHENTICATION, ErrorType.OPERATION_FAILED -> ErrorSeverity.HIGH
            ErrorType.VALIDATION, ErrorType.BUSINESS_RULE -> ErrorSeverity.MEDIUM
            ErrorType.NOT_FOUND, ErrorType.CONFLICT -> ErrorSeverity.MEDIUM
            ErrorType.REAL_TIME -> ErrorSeverity.LOW
            ErrorType.UNKNOWN -> ErrorSeverity.HIGH
        }
    
    /**
     * Check if error should be logged for debugging
     */
    val shouldLog: Boolean
        get() = severity == ErrorSeverity.HIGH || type == ErrorType.UNKNOWN
    
    /**
     * Check if error should show detailed technical information to user
     */
    val shouldShowTechnicalDetails: Boolean
        get() = type in setOf(ErrorType.VALIDATION, ErrorType.BUSINESS_RULE)
}

/**
 * Types of errors that can occur in the kids management system
 */
enum class ErrorType {
    NETWORK,           // Network connectivity issues
    VALIDATION,        // Form validation errors
    BUSINESS_RULE,     // Business logic violations
    NOT_FOUND,         // Resource not found
    CONFLICT,          // Data conflicts
    AUTHENTICATION,    // Auth/authorization issues
    SERVER,            // Server-side errors
    REAL_TIME,         // Real-time connection issues
    API,               // API-specific errors
    OPERATION_FAILED,  // General operation failures
    UNKNOWN            // Unexpected errors
}

/**
 * Icon types for different error categories
 */
enum class ErrorIconType {
    NETWORK,    // WiFi/connection icon
    WARNING,    // Warning triangle
    INFO,       // Information circle
    SEARCH,     // Search/not found icon
    LOCK,       // Lock/security icon
    SERVER,     // Server/cloud icon
    SYNC,       // Sync/refresh icon
    ERROR       // Error/X icon
}

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    LOW,        // Minor issues, app remains functional
    MEDIUM,     // Moderate issues, some features affected
    HIGH        // Critical issues, major functionality affected
}

/**
 * Error recovery strategies
 */
enum class RecoveryStrategy {
    RETRY,              // Simple retry
    RETRY_WITH_DELAY,   // Retry after delay
    REFRESH_DATA,       // Refresh and retry
    FALLBACK_MODE,      // Use offline/cached data
    USER_ACTION,        // Requires user intervention
    RESTART_REQUIRED    // App restart needed
}

/**
 * Extension functions for error handling
 */
fun ErrorInfo.getRecoveryStrategy(): RecoveryStrategy {
    return when {
        isRetryable && type == ErrorType.NETWORK -> RecoveryStrategy.RETRY_WITH_DELAY
        isRetryable && type == ErrorType.SERVER -> RecoveryStrategy.RETRY_WITH_DELAY
        isRetryable && type in setOf(ErrorType.NOT_FOUND, ErrorType.CONFLICT) -> RecoveryStrategy.REFRESH_DATA
        type == ErrorType.VALIDATION -> RecoveryStrategy.USER_ACTION
        type == ErrorType.BUSINESS_RULE -> RecoveryStrategy.USER_ACTION
        type == ErrorType.AUTHENTICATION -> RecoveryStrategy.USER_ACTION
        type == ErrorType.REAL_TIME -> RecoveryStrategy.FALLBACK_MODE
        isRetryable -> RecoveryStrategy.RETRY
        else -> RecoveryStrategy.USER_ACTION
    }
}

/**
 * Get user-friendly recovery instruction
 */
fun ErrorInfo.getRecoveryInstruction(): String {
    return when (getRecoveryStrategy()) {
        RecoveryStrategy.RETRY -> "Tap 'Retry' to try again"
        RecoveryStrategy.RETRY_WITH_DELAY -> "Please wait a moment and tap 'Retry'"
        RecoveryStrategy.REFRESH_DATA -> "Pull down to refresh and try again"
        RecoveryStrategy.FALLBACK_MODE -> "Limited functionality available offline"
        RecoveryStrategy.USER_ACTION -> suggestedAction
        RecoveryStrategy.RESTART_REQUIRED -> "Please restart the app"
    }
}