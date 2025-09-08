package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rfm.hillsongptapp.feature.kids.data.network.websocket.ConnectionStatus

/**
 * Connection status indicator component that shows the real-time connection status
 */
@Composable
fun ConnectionStatusIndicator(
    connectionStatus: ConnectionStatus,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    compact: Boolean = false
) {
    val (statusColor, statusText, isAnimated) = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> Triple(
            Color(0xFF4CAF50), // Green
            "Live Updates",
            false
        )
        ConnectionStatus.CONNECTING -> Triple(
            Color(0xFFFF9800), // Orange
            "Connecting...",
            true
        )
        ConnectionStatus.RECONNECTING -> Triple(
            Color(0xFFFF9800), // Orange
            "Reconnecting...",
            true
        )
        ConnectionStatus.DISCONNECTING -> Triple(
            Color(0xFF9E9E9E), // Gray
            "Disconnecting...",
            false
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            Color(0xFF9E9E9E), // Gray
            "Offline",
            false
        )
        ConnectionStatus.FAILED -> Triple(
            Color(0xFFF44336), // Red
            "Connection Failed",
            false
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status indicator dot
        StatusDot(
            color = statusColor,
            isAnimated = isAnimated,
            size = if (compact) 8.dp else 12.dp
        )
        
        // Status text
        if (showText) {
            Text(
                text = statusText,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = if (connectionStatus == ConnectionStatus.CONNECTED) FontWeight.Medium else FontWeight.Normal,
                color = if (connectionStatus == ConnectionStatus.CONNECTED) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun StatusDot(
    color: Color,
    isAnimated: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (isAnimated) {
        // Pulsing animation for connecting/reconnecting states
        val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha_animation"
        )
        
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
    } else {
        // Static dot for stable states
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Expanded connection status card with more details
 */
@Composable
fun ConnectionStatusCard(
    connectionStatus: ConnectionStatus,
    lastUpdateTime: String? = null,
    onRetryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ConnectionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConnectionStatusIndicator(
                    connectionStatus = connectionStatus,
                    showText = true,
                    compact = false
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Retry button for failed connections
                if (connectionStatus == ConnectionStatus.FAILED && onRetryClick != null) {
                    TextButton(onClick = onRetryClick) {
                        Text("Retry")
                    }
                }
            }
            
            // Additional status information
            val statusDescription = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> "Real-time updates are active. You'll receive live notifications for check-ins and status changes."
                ConnectionStatus.CONNECTING -> "Establishing connection for real-time updates..."
                ConnectionStatus.RECONNECTING -> "Connection lost. Attempting to reconnect..."
                ConnectionStatus.DISCONNECTING -> "Disconnecting from real-time updates..."
                ConnectionStatus.DISCONNECTED -> "Real-time updates are not available. Data will be refreshed manually."
                ConnectionStatus.FAILED -> "Unable to establish real-time connection. You can still use the app with manual refresh."
            }
            
            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Last update time
            lastUpdateTime?.let { time ->
                Text(
                    text = "Last updated: $time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Compact connection status banner for top of screens
 */
@Composable
fun ConnectionStatusBanner(
    connectionStatus: ConnectionStatus,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Only show banner for non-connected states
    if (connectionStatus == ConnectionStatus.CONNECTED) return
    
    val backgroundColor = when (connectionStatus) {
        ConnectionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (connectionStatus) {
        ConnectionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConnectionStatusIndicator(
                connectionStatus = connectionStatus,
                showText = true,
                compact = true
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Dismiss button
            onDismiss?.let { dismiss ->
                IconButton(
                    onClick = dismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Dismiss",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}