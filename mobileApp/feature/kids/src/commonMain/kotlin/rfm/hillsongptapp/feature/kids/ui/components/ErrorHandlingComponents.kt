package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.feature.kids.domain.error.ErrorInfo
import rfm.hillsongptapp.feature.kids.domain.error.ErrorType
import rfm.hillsongptapp.feature.kids.domain.error.ErrorSeverity

/**
 * Comprehensive error display component with recovery options
 */
@Composable
fun ErrorDisplay(
    errorInfo: ErrorInfo,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onContactSupport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (errorInfo.severity) {
                ErrorSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer
                ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                ErrorSeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErrorIcon(errorInfo.iconType)
                
                Text(
                    text = errorInfo.summary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (errorInfo.severity) {
                        ErrorSeverity.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                        ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                        ErrorSeverity.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Error message
            Text(
                text = errorInfo.userMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = when (errorInfo.severity) {
                    ErrorSeverity.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                    ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                    ErrorSeverity.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            // Suggested action
            if (errorInfo.suggestedAction.isNotBlank()) {
                Text(
                    text = errorInfo.suggestedAction,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = when (errorInfo.severity) {
                        ErrorSeverity.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                        ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                        ErrorSeverity.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                // Contact support button for high severity errors
                if (errorInfo.severity == ErrorSeverity.HIGH && onContactSupport != null) {
                    OutlinedButton(
                        onClick = onContactSupport,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Contact Support")
                    }
                }
                
                // Retry button for retryable errors
                if (errorInfo.isRetryable && onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (errorInfo.severity) {
                                ErrorSeverity.HIGH -> MaterialTheme.colorScheme.error
                                ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.secondary
                                ErrorSeverity.LOW -> MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Text("Retry")
                    }
                }
                
                // Dismiss button
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = when (errorInfo.severity) {
                            ErrorSeverity.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                            ErrorSeverity.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                            ErrorSeverity.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

/**
 * Error dialog with comprehensive recovery options
 */
@Composable
fun ErrorDialog(
    errorInfo: ErrorInfo,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onContactSupport: (() -> Unit)? = null,
    showTechnicalDetails: Boolean = false,
    onToggleTechnicalDetails: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            ErrorIcon(errorInfo.iconType)
        },
        title = {
            Text(
                text = errorInfo.summary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = errorInfo.userMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (errorInfo.suggestedAction.isNotBlank()) {
                    Text(
                        text = errorInfo.suggestedAction,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Technical details toggle
                if (onToggleTechnicalDetails != null) {
                    TextButton(
                        onClick = onToggleTechnicalDetails,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (showTechnicalDetails) "Hide Technical Details" else "Show Technical Details"
                        )
                    }
                }
                
                // Technical details
                if (showTechnicalDetails) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Technical Details:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = errorInfo.technicalMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Error Type: ${errorInfo.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(errorInfo.timestamp))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorInfo.isRetryable && onRetry != null) {
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
                
                if (errorInfo.severity == ErrorSeverity.HIGH && onContactSupport != null) {
                    OutlinedButton(onClick = onContactSupport) {
                        Text("Support")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

/**
 * Validation error display for forms
 */
@Composable
fun ValidationErrorDisplay(
    errors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    if (errors.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Please fix the following errors:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                errors.forEach { (field, error) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "$field: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Offline status indicator
 */
@Composable
fun OfflineStatusIndicator(
    isOffline: Boolean,
    message: String,
    onRetryConnection: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (isOffline) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErrorIcon(rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.NETWORK)
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Offline Mode",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                if (onRetryConnection != null) {
                    TextButton(
                        onClick = onRetryConnection,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Error icon based on error type
 */
@Composable
private fun ErrorIcon(iconType: rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType) {
    // This would use actual icons from the design system
    // For now, using text representations
    Text(
        text = when (iconType) {
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.NETWORK -> "📶"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.WARNING -> "⚠️"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.INFO -> "ℹ️"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.SEARCH -> "🔍"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.LOCK -> "🔒"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.SERVER -> "🖥️"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.SYNC -> "🔄"
            rfm.hillsongptapp.feature.kids.domain.error.ErrorIconType.ERROR -> "❌"
        },
        style = MaterialTheme.typography.titleMedium
    )
}