package rfm.hillsongptapp.feature.kids.ui.model

import kotlinx.datetime.Instant
import rfm.hillsongptapp.feature.kids.ui.model.*

/** UI-specific models for the Kids feature */

// Connection Status for UI
enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    RECONNECTING,
    ERROR,
    OFFLINE
}

// Error handling models for UI
enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH
}

enum class ErrorType {
    NETWORK,
    VALIDATION,
    PERMISSION,
    DATA,
    SYSTEM,
    USER,
    TIMEOUT,
    AUTHENTICATION,
    UNKNOWN
}

data class ErrorInfo(
        val type: ErrorType,
        val severity: ErrorSeverity,
        val summary: String,
        val userMessage: String,
        val technicalMessage: String? = null,
        val suggestedAction: String? = null,
        val isRetryable: Boolean = false,
        val timestamp: Instant,
        val iconType: String = "error"
)

// Validation models for UI
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(override val errorMessage: String) : ValidationResult()

    val isValid: Boolean
        get() = this is Valid
    val isInvalid: Boolean
        get() = this is Invalid
    open val errorMessage: String?
        get() = (this as? Invalid)?.errorMessage
}

// Notification models for UI
enum class NotificationType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR,
    SYNC,
    CHECK_IN,
    CHECK_OUT,
    REGISTRATION,
    SYSTEM
}

data class StatusNotification(
        val id: String,
        val type: NotificationType,
        val title: String,
        val message: String,
        val timestamp: Instant,
        val isRead: Boolean = false,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
)

// Check-in/Check-out result models for UI
data class CheckInResult(
        val success: Boolean,
        val child: rfm.hillsongptapp.core.data.model.Child,
        val service: rfm.hillsongptapp.core.data.model.KidsService,
        val record: rfm.hillsongptapp.core.data.model.CheckInRecord,
        val message: String? = null
)

data class CheckOutResult(
        val success: Boolean,
        val child: rfm.hillsongptapp.core.data.model.Child,
        val service: rfm.hillsongptapp.core.data.model.KidsService,
        val record: rfm.hillsongptapp.core.data.model.CheckInRecord,
        val message: String? = null
)

// Service eligibility for UI
data class ServiceEligibility(
        val service: rfm.hillsongptapp.core.data.model.KidsService,
        val isEligible: Boolean,
        val reason: String? = null,
        val isRecommended: Boolean = false,
        val availableSpots: Int = 0,
        val canCheckOut: Boolean = false
)

// Check-in eligibility info
data class CheckInEligibilityInfo(
        val canCheckIn: Boolean,
        val reason: String? = null,
        val eligibleServices: List<ServiceEligibility> = emptyList()
)

// Check-out eligibility info
data class CheckOutEligibilityInfo(
        val canCheckOut: Boolean,
        val reason: String? = null,
        val currentService: rfm.hillsongptapp.core.data.model.KidsService? = null
)

// Legacy alias for backward compatibility
typealias EligibleServiceInfo = ServiceEligibility

// Utility functions for UI
fun getAttendanceDuration(checkInTime: Instant?, checkOutTime: Instant?): String {
    if (checkInTime == null || checkOutTime == null) return "Unknown"

    val duration = checkOutTime.epochSeconds - checkInTime.epochSeconds
    val hours = duration / 3600
    val minutes = (duration % 3600) / 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
