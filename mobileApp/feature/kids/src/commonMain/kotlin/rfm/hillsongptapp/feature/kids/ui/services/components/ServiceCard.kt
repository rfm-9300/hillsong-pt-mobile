package rfm.hillsongptapp.feature.kids.ui.services.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService

/**
 * Card component displaying service information, capacity, and availability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceCard(
    service: KidsService,
    selectedChild: Child? = null,
    onServiceClick: (KidsService) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isChildEligible = selectedChild?.let { service.isAgeEligible(it.calculateAge()) } ?: true
    val canAcceptCheckIn = service.canAcceptCheckIn()
    val isClickable = selectedChild == null || (isChildEligible && canAcceptCheckIn)
    
    Card(
        onClick = if (isClickable) { { onServiceClick(service) } } else { {} },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !isChildEligible -> MaterialTheme.colorScheme.surfaceVariant
                !canAcceptCheckIn -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
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
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isChildEligible) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (service.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = service.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                ServiceStatusIndicator(
                    service = service,
                    isChildEligible = isChildEligible
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Service details
            ServiceDetailsRow(service = service)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Capacity indicator
            CapacityIndicator(
                service = service,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Child eligibility message if applicable
            if (selectedChild != null && !isChildEligible) {
                Spacer(modifier = Modifier.height(8.dp))
                EligibilityMessage(
                    child = selectedChild,
                    service = service
                )
            }
        }
    }
}

/**
 * Service status indicator showing availability and acceptance status
 */
@Composable
private fun ServiceStatusIndicator(
    service: KidsService,
    isChildEligible: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when {
        !service.isAcceptingCheckIns -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.error,
            "Closed"
        )
        service.isAtCapacity() -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.error,
            "Full"
        )
        !isChildEligible -> Triple(
            Icons.Filled.Warning,
            MaterialTheme.colorScheme.outline,
            "Not Eligible"
        )
        service.canAcceptCheckIn() -> Triple(
            Icons.Filled.CheckCircle,
            Color(0xFF4CAF50), // Green
            "Available"
        )
        else -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.outline,
            "Limited"
        )
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Row showing service details like age range, time, and location
 */
@Composable
private fun ServiceDetailsRow(
    service: KidsService,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ServiceDetailItem(
            icon = Icons.Default.Warning,
            label = "Age Range",
            value = service.getAgeRangeDisplay()
        )
        
        ServiceDetailItem(
            icon = Icons.Default.Warning,
            label = "Time",
            value = "${formatTime(service.startTime)} - ${formatTime(service.endTime)}"
        )
        
        ServiceDetailItem(
            icon = Icons.Filled.LocationOn,
            label = "Location",
            value = service.location
        )
        
        if (service.staffMembers.isNotEmpty()) {
            ServiceDetailItem(
                icon = Icons.Filled.Person,
                label = "Staff",
                value = "${service.staffMembers.size} member${if (service.staffMembers.size != 1) "s" else ""}"
            )
        }
    }
}

/**
 * Individual service detail item
 */
@Composable
private fun ServiceDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Capacity indicator showing current vs maximum capacity
 */
@Composable
private fun CapacityIndicator(
    service: KidsService,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capacity",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = service.getCapacityDisplay(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (service.isAtCapacity()) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { service.currentCapacity.toFloat() / service.maxCapacity.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = when {
                service.isAtCapacity() -> MaterialTheme.colorScheme.error
                service.currentCapacity.toFloat() / service.maxCapacity.toFloat() > 0.8f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        if (service.hasAvailableSpots()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${service.getAvailableSpots()} spot${if (service.getAvailableSpots() != 1) "s" else ""} available",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Message explaining why a child is not eligible for a service
 */
@Composable
private fun EligibilityMessage(
    child: Child,
    service: KidsService,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Information",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${child.name} (age ${child.calculateAge()}) is not eligible for this service (${service.getAgeRangeDisplay()})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Format time string for display
 * This is a simplified implementation - in a real app you'd use proper date/time formatting
 */
private fun formatTime(isoTime: String): String {
    return try {
        val timePart = isoTime.substringAfter('T').substringBefore('.')
        val (hour, minute) = timePart.split(':')
        val hourInt = hour.toInt()
        val amPm = if (hourInt >= 12) "PM" else "AM"
        val displayHour = if (hourInt == 0) 12 else if (hourInt > 12) hourInt - 12 else hourInt
        "$displayHour:$minute $amPm"
    } catch (e: Exception) {
        // Fallback for simple time strings like "09:00" or "14:30"
        try {
            val (hour, minute) = isoTime.split(':')
            val hourInt = hour.toInt()
            val amPm = if (hourInt >= 12) "PM" else "AM"
            val displayHour = if (hourInt == 0) 12 else if (hourInt > 12) hourInt - 12 else hourInt
            "$displayHour:$minute $amPm"
        } catch (e: Exception) {
            isoTime // Fallback to original string
        }
    }
}