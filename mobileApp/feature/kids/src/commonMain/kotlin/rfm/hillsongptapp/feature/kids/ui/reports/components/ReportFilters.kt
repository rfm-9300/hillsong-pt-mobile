package rfm.hillsongptapp.feature.kids.ui.reports.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import rfm.hillsongptapp.core.data.model.KidsService

/**
 * Component for filtering reports by services and other criteria
 */
@Composable
fun ReportFilters(
    availableServices: List<KidsService>,
    selectedServices: Set<String>,
    onServiceSelectionChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Clear all button
                if (selectedServices.size < availableServices.size) {
                    TextButton(
                        onClick = {
                            onServiceSelectionChanged(availableServices.map { it.id }.toSet())
                        }
                    ) {
                        Text("Select All")
                    }
                } else {
                    TextButton(
                        onClick = {
                            onServiceSelectionChanged(emptySet())
                        }
                    ) {
                        Text("Clear All")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Service filters
            if (availableServices.isNotEmpty()) {
                Text(
                    text = "Services (${selectedServices.size}/${availableServices.size} selected)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(availableServices) { service ->
                        ServiceFilterChip(
                            service = service,
                            isSelected = selectedServices.contains(service.id),
                            onSelectionChanged = { isSelected ->
                                val newSelection = if (isSelected) {
                                    selectedServices + service.id
                                } else {
                                    selectedServices - service.id
                                }
                                onServiceSelectionChanged(newSelection)
                            }
                        )
                    }
                }
            }
            
            // Filter summary
            if (selectedServices.size < availableServices.size && selectedServices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Filtering by ${selectedServices.size} of ${availableServices.size} services",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Filter chip for individual service selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceFilterChip(
    service: KidsService,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelectionChanged(!isSelected) },
        label = {
            Text(
                text = service.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    )
}