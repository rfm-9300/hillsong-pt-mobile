package rfm.hillsongptapp.feature.kids.ui.reports.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.core.data.model.ServiceReport

/**
 * Card component displaying service report with capacity management and visual indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReportCard(
    report: ServiceReport,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onViewDetailsClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with service name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Service",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                ServiceStatusIndicator(report = report)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Capacity information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Capacity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = report.getCapacityDisplay(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Utilization",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.getCapacityUtilizationPercent()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = getCapacityColor(report.getCapacityUtilization())
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Capacity progress bar
            CapacityProgressBar(
                current = report.currentCheckIns,
                total = report.totalCapacity,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Additional information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available: ${report.availableSpots}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Staff: ${report.staffMembers.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Warning for full or near-full services
            if (report.isAtCapacity() || report.getCapacityUtilization() >= 0.9f) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (report.isAtCapacity()) {
                            "Service is at full capacity"
                        } else {
                            "Service is nearly full"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Status indicator showing service status with color coding
 */
@Composable
private fun ServiceStatusIndicator(
    report: ServiceReport,
    modifier: Modifier = Modifier
) {
    val (color, text) = when {
        report.isAtCapacity() -> Pair(
            MaterialTheme.colorScheme.error,
            "Full"
        )
        report.getCapacityUtilization() >= 0.9f -> Pair(
            Color(0xFFFF9800), // Orange
            "Nearly Full"
        )
        report.currentCheckIns == 0 -> Pair(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Empty"
        )
        else -> Pair(
            Color(0xFF4CAF50), // Green
            "Available"
        )
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Progress bar showing capacity utilization with color coding
 */
@Composable
private fun CapacityProgressBar(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
    val color = getCapacityColor(progress)
    
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

/**
 * Get color based on capacity utilization
 */
private fun getCapacityColor(utilization: Float): Color {
    return when {
        utilization >= 1.0f -> Color(0xFFD32F2F) // Red - Full
        utilization >= 0.9f -> Color(0xFFFF9800) // Orange - Nearly full
        utilization >= 0.7f -> Color(0xFFFFC107) // Yellow - Getting full
        else -> Color(0xFF4CAF50) // Green - Available
    }
}