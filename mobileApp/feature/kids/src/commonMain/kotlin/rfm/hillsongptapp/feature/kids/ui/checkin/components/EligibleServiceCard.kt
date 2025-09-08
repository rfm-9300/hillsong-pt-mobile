package rfm.hillsongptapp.feature.kids.ui.checkin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo

/**
 * Card component for displaying an eligible service with selection capability
 */
@Composable
fun EligibleServiceCard(
    serviceInfo: EligibleServiceInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val service = serviceInfo.service
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                serviceInfo.isRecommended -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
            )
        } else {
            CardDefaults.outlinedCardBorder()
        },
        elevation = if (isSelected) {
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        } else {
            CardDefaults.cardElevation()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with service name and recommendation badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (service.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (serviceInfo.isRecommended) {
                    RecommendedBadge()
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Service details
            ServiceDetailRow(
                icon = Icons.Default.Warning,
                label = "Age Range",
                value = service.getAgeRangeDisplay()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ServiceDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Location",
                value = service.location
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ServiceDetailRow(
                icon = Icons.Default.Clear,
                label = "Time",
                value = "${service.startTime} - ${service.endTime}"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Capacity information
            CapacityIndicator(
                availableSpots = serviceInfo.availableSpots,
                totalCapacity = service.maxCapacity,
                isRecommended = serviceInfo.isRecommended
            )
        }
    }
}

@Composable
private fun RecommendedBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Recommended",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ServiceDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CapacityIndicator(
    availableSpots: Int,
    totalCapacity: Int,
    isRecommended: Boolean
) {
    val currentCapacity = totalCapacity - availableSpots
    val fillPercentage = currentCapacity.toFloat() / totalCapacity.toFloat()
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capacity",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isRecommended && availableSpots <= 3) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Limited availability",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    text = "$availableSpots spots left",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        availableSpots > 5 -> MaterialTheme.colorScheme.primary
                        availableSpots > 2 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Progress bar showing capacity
        LinearProgressIndicator(
            progress = { fillPercentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = when {
                fillPercentage < 0.7f -> MaterialTheme.colorScheme.primary
                fillPercentage < 0.9f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$currentCapacity / $totalCapacity children",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}