package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import rfm.hillsongptapp.core.data.model.Child
import hillsongptapp.feature.kids.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import rfm.hillsongptapp.core.data.model.KidsService

/**
 * Dialog for checking out a child from their current service
 * This is a simplified version - for full check-out flow with verification, use CheckOutScreen
 */
@Composable
fun CheckOutDialog(
    child: Child,
    currentService: KidsService?,
    onCheckOut: (childId: String) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToFullCheckOut: ((childId: String) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(Res.string.warning),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = stringResource(Res.string.check_out_child, child.name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current service info
                if (currentService != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.currently_in),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = currentService.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = currentService.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            child.checkInTime?.let { checkInTime ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Checked in at: ${formatTime(checkInTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Confirmation message
                Text(
                    text = "Are you sure you want to check out ${child.name}? This will remove them from their current service.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Show full check-out button if navigation is available
                    if (onNavigateToFullCheckOut != null) {
                        OutlinedButton(
                            onClick = { 
                                onDismiss()
                                onNavigateToFullCheckOut(child.id) 
                            }
                        ) {
                            Text("Full Check-Out")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Button(
                        onClick = { onCheckOut(child.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Quick Check-Out")
                    }
                }
            }
        }
    }
}

/**
 * Format time string for display
 * This is a simplified implementation - in a real app you'd use proper date/time formatting
 */
private fun formatTime(isoTime: String): String {
    // Simplified time formatting - extract time portion from ISO string
    return try {
        val timePart = isoTime.substringAfter('T').substringBefore('.')
        val (hour, minute) = timePart.split(':')
        val hourInt = hour.toInt()
        val amPm = if (hourInt >= 12) "PM" else "AM"
        val displayHour = if (hourInt == 0) 12 else if (hourInt > 12) hourInt - 12 else hourInt
        "$displayHour:$minute $amPm"
    } catch (e: Exception) {
        isoTime // Fallback to original string
    }
}