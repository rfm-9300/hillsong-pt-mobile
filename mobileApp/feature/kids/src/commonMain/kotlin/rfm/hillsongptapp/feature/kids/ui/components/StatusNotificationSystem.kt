package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import rfm.hillsongptapp.feature.kids.ui.model.StatusNotification
import rfm.hillsongptapp.feature.kids.ui.model.NotificationType
import rfm.hillsongptapp.feature.kids.ui.model.*

/**
 * Notification system for displaying status change notifications
 */
@Composable
fun StatusNotificationSystem(
    notifications: List<StatusNotification>,
    onDismissNotification: (StatusNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        notifications.forEach { notification ->
            StatusNotificationCard(
                notification = notification,
                onDismiss = { onDismissNotification(notification) }
            )
        }
    }
}

/**
 * Individual notification card with auto-dismiss
 */
@Composable
fun StatusNotificationCard(
    notification: StatusNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissDelay: Long = 5000L // 5 seconds
) {
    var isVisible by remember { mutableStateOf(true) }
    
    // Auto-dismiss after delay
    LaunchedEffect(notification) {
        delay(autoDismissDelay)
        isVisible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        NotificationContent(
            notification = notification,
            onDismiss = {
                isVisible = false
            }
        )
    }
}

@Composable
private fun NotificationContent(
    notification: StatusNotification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, icon) = getNotificationStyle(notification.type)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notification icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            
            // Notification content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
                
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.9f)
                )
                
                // Timestamp
                Text(
                    text = formatTimestamp(notification.timestamp.toEpochMilliseconds()),
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
            
            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun getNotificationStyle(type: NotificationType): Triple<Color, Color, ImageVector> {
    return when (type) {
        NotificationType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle
        )
        NotificationType.WARNING -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )
        NotificationType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )
        NotificationType.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            Icons.Default.Info
        )
        NotificationType.CHECK_IN -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle
        )
        NotificationType.CHECK_OUT -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.ExitToApp
        )
        NotificationType.SYNC -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Refresh
        )
        NotificationType.REGISTRATION -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Person
        )
        NotificationType.SYSTEM -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Settings
        )
    }
}

/**
 * Floating notification overlay that appears at the top of the screen
 */
@Composable
fun FloatingNotificationOverlay(
    notification: StatusNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { notif ->
            StatusNotificationCard(
                notification = notif,
                onDismiss = onDismiss,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                autoDismissDelay = 4000L // Shorter delay for floating notifications
            )
        }
    }
}

/**
 * Compact notification toast for minimal disruption
 */
@Composable
fun NotificationToast(
    notification: StatusNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { notif ->
            val (backgroundColor, contentColor, icon) = getNotificationStyle(notif.type)
            
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = backgroundColor,
                contentColor = contentColor,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                    
                    Text(
                        text = notif.message,
                        fontSize = 14.sp,
                        color = contentColor,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(14.dp),
                            tint = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
    
    // Auto-dismiss
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3000L)
            onDismiss()
        }
    }
}

/**
 * Notification badge for showing unread notification count
 */
@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Surface(
            modifier = modifier,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return ""
}