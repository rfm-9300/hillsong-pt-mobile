package rfm.hillsongptapp.feature.kids.ui.services.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import rfm.hillsongptapp.feature.kids.ui.services.ServiceFilters

/**
 * Dialog for filtering services by various criteria
 */
@Composable
fun ServiceFilterDialog(
    currentFilters: ServiceFilters,
    onFiltersChanged: (ServiceFilters) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filters by remember { mutableStateOf(currentFilters) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Filter Services",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Availability Filter
                AvailabilityFilterSection(
                    selectedAvailability = filters.availability,
                    onAvailabilityChanged = { availability ->
                        filters = filters.copy(availability = availability)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Age Range Filter
                AgeRangeFilterSection(
                    minAge = filters.minAge,
                    maxAge = filters.maxAge,
                    onAgeRangeChanged = { min, max ->
                        filters = filters.copy(minAge = min, maxAge = max)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Capacity Filter
                CapacityFilterSection(
                    showFullServices = filters.showFullServices,
                    onShowFullServicesChanged = { showFull ->
                        filters = filters.copy(showFullServices = showFull)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            filters = ServiceFilters() // Reset to default
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                    
                    Button(
                        onClick = { onFiltersChanged(filters) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Section for filtering by service availability
 */
@Composable
private fun AvailabilityFilterSection(
    selectedAvailability: ServiceFilters.Availability,
    onAvailabilityChanged: (ServiceFilters.Availability) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Availability",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.selectableGroup()
        ) {
            ServiceFilters.Availability.entries.forEach { availability ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedAvailability == availability,
                            onClick = { onAvailabilityChanged(availability) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedAvailability == availability,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = availability.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Section for filtering by age range
 */
@Composable
private fun AgeRangeFilterSection(
    minAge: Int?,
    maxAge: Int?,
    onAgeRangeChanged: (Int?, Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Age Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Min Age
            OutlinedTextField(
                value = minAge?.toString() ?: "",
                onValueChange = { value ->
                    val age = value.toIntOrNull()
                    onAgeRangeChanged(age, maxAge)
                },
                label = { Text("Min Age") },
                placeholder = { Text("Any") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // Max Age
            OutlinedTextField(
                value = maxAge?.toString() ?: "",
                onValueChange = { value ->
                    val age = value.toIntOrNull()
                    onAgeRangeChanged(minAge, age)
                },
                label = { Text("Max Age") },
                placeholder = { Text("Any") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        if (minAge != null || maxAge != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    minAge != null && maxAge != null -> "Showing services for ages $minAge-$maxAge"
                    minAge != null -> "Showing services for ages $minAge and up"
                    maxAge != null -> "Showing services for ages up to $maxAge"
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Section for filtering by capacity
 */
@Composable
private fun CapacityFilterSection(
    showFullServices: Boolean,
    onShowFullServicesChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Capacity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = showFullServices,
                    onClick = { onShowFullServicesChanged(!showFullServices) },
                    role = Role.Checkbox
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showFullServices,
                onCheckedChange = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Show full services",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Include services that are at maximum capacity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}