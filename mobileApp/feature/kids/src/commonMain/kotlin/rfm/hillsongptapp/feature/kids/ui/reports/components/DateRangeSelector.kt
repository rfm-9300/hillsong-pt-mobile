package rfm.hillsongptapp.feature.kids.ui.reports.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*

/**
 * Component for selecting date ranges for reports
 * Provides quick preset options and custom date selection
 */
@Composable
fun DateRangeSelector(
    startDate: String,
    endDate: String,
    onDateRangeChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick preset buttons
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateButton(
                text = "Today",
                onClick = {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    onDateRangeChanged(today, today)
                },
                modifier = Modifier.weight(1f)
            )
            
            QuickDateButton(
                text = "This Week",
                onClick = {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    val endOfWeek = startOfWeek.plus(6, DateTimeUnit.DAY)
                    onDateRangeChanged(startOfWeek.toString(), endOfWeek.toString())
                },
                modifier = Modifier.weight(1f)
            )
            
            QuickDateButton(
                text = "This Month",
                onClick = {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val startOfMonth = LocalDate(today.year, today.month, 1)
                    val endOfMonth = startOfMonth.plus(1, DateTimeUnit.MONTH)
                    onDateRangeChanged(startOfMonth.toString(), endOfMonth.toString())
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Custom date selection
        Text(
            text = "Custom Range",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start date
            DateField(
                label = "Start Date",
                date = startDate,
                onDateChanged = { newStartDate ->
                    onDateRangeChanged(newStartDate, endDate)
                },
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "to",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // End date
            DateField(
                label = "End Date",
                date = endDate,
                onDateChanged = { newEndDate ->
                    onDateRangeChanged(startDate, newEndDate)
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Selected range display
        if (startDate.isNotBlank() && endDate.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Selected Range",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Selected: ${formatDateRange(startDate, endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Quick date selection button
 */
@Composable
private fun QuickDateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * Date input field with validation
 */
@Composable
private fun DateField(
    label: String,
    date: String,
    onDateChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isError by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = date,
        onValueChange = { newValue ->
            isError = !isValidDateFormat(newValue) && newValue.isNotBlank()
            if (!isError || newValue.isBlank()) {
                onDateChanged(newValue)
            }
        },
        label = { Text(label) },
        placeholder = { Text("YYYY-MM-DD") },
        isError = isError,
        supportingText = if (isError) {
            { Text("Invalid date format (use YYYY-MM-DD)") }
        } else null,
        singleLine = true,
        modifier = modifier
    )
}

/**
 * Validate date format (YYYY-MM-DD)
 */
private fun isValidDateFormat(date: String): Boolean {
    return try {
        LocalDate.parse(date)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Format date range for display
 */
private fun formatDateRange(startDate: String, endDate: String): String {
    return try {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        
        if (start == end) {
            formatDate(start)
        } else {
            "${formatDate(start)} - ${formatDate(end)}"
        }
    } catch (e: Exception) {
        "$startDate - $endDate"
    }
}

/**
 * Format a single date for display
 */
private fun formatDate(date: LocalDate): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    
    return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}