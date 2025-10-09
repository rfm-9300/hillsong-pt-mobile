package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestResponse
import kotlin.math.max

/**
 * Widget to display check-in status for a child
 * 
 * Shows different states:
 * - Not Checked In (default)
 * - Pending Verification (with countdown)
 * - Checked In (with time and staff name)
 * - Rejected (with reason)
 * - Expired
 * 
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
 */
@Composable
fun CheckInStatusWidget(
    checkInRequest: CheckInRequestResponse?,
    isCheckedIn: Boolean,
    checkInTime: String?,
    approvedByStaff: String?,
    onCancelRequest: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                checkInRequest?.status == "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                checkInRequest?.status == "EXPIRED" -> MaterialTheme.colorScheme.surfaceVariant
                checkInRequest?.status == "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer
                isCheckedIn -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when {
                    // Checked In
                    isCheckedIn -> {
                        CheckedInStatus(
                            checkInTime = checkInTime,
                            approvedByStaff = approvedByStaff
                        )
                    }
                    
                    // Pending Verification
                    checkInRequest?.status == "PENDING" && !checkInRequest.isExpired -> {
                        PendingVerificationStatus(
                            expiresInSeconds = checkInRequest.expiresInSeconds,
                            serviceName = checkInRequest.service.name
                        )
                    }
                    
                    // Rejected
                    checkInRequest?.status == "REJECTED" -> {
                        RejectedStatus()
                    }
                    
                    // Expired
                    checkInRequest?.isExpired == true || checkInRequest?.status == "EXPIRED" -> {
                        ExpiredStatus()
                    }
                    
                    // Not Checked In (default)
                    else -> {
                        NotCheckedInStatus()
                    }
                }
            }
            
            // Action button for pending requests
            if (checkInRequest?.status == "PENDING" && !checkInRequest.isExpired && onCancelRequest != null) {
                TextButton(
                    onClick = onCancelRequest,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun NotCheckedInStatus() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = "Not Checked In",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PendingVerificationStatus(
    expiresInSeconds: Long,
    serviceName: String
) {
    // Calculate minutes and seconds
    val minutes = max(0, expiresInSeconds / 60)
    val seconds = max(0, expiresInSeconds % 60)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = "Pending Verification",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        Text(
            text = serviceName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Expires in ${minutes}m ${seconds}s",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun CheckedInStatus(
    checkInTime: String?,
    approvedByStaff: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = "Checked In",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        checkInTime?.let {
            Text(
                text = "At $it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        approvedByStaff?.let {
            Text(
                text = "Approved by $it",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RejectedStatus() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = "Rejected",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ExpiredStatus() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = "Expired",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
