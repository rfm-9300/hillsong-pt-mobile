package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestResponse
import rfm.hillsongptapp.feature.kids.ui.theme.KidsColors

/** Card component displaying child information with status indicators and action buttons */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildCard(
        child: Child,
        currentService: KidsService? = null,
        checkInRequest: CheckInRequestResponse? = null,
        onCheckOutClick: (Child) -> Unit,
        onEditClick: (Child) -> Unit,
        onViewServicesClick: ((Child) -> Unit)? = null,
        onQRCheckInClick: ((Child) -> Unit)? = null,
        onCancelCheckInRequest: ((Long) -> Unit)? = null,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header with child name and status
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Child",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = child.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                    )
                }

                StatusIndicator(status = child.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Child details
            Text(
                    text = "Age: ${child.calculateAge()} years",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Show current service if checked in
            if (child.status == CheckInStatus.CHECKED_IN && currentService != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = "Currently in: ${currentService.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                )

                child.checkInTime?.let { checkInTime ->
                    Text(
                            text = "Checked in at: ${formatTime(checkInTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Show last checkout time if recently checked out
            child.checkOutTime?.let { checkOutTime ->
                if (child.status == CheckInStatus.CHECKED_OUT) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text = "Last checked out: ${formatTime(checkOutTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Check-in status widget
            CheckInStatusWidget(
                    checkInRequest = checkInRequest,
                    isCheckedIn = child.status == CheckInStatus.CHECKED_IN,
                    checkInTime = child.checkInTime?.let { formatTime(it) },
                    approvedByStaff = null, // TODO: Add approvedByStaff to Child model
                    onCancelRequest =
                            if (checkInRequest != null && onCancelCheckInRequest != null) {
                                { onCancelCheckInRequest(checkInRequest.id) }
                            } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Primary action row
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (child.status) {
                        CheckInStatus.CHECKED_OUT, CheckInStatus.NOT_IN_SERVICE -> {
                            if (onQRCheckInClick != null) {
                                OutlinedButton(
                                        onClick = { onQRCheckInClick?.invoke(child) },
                                        modifier = Modifier.weight(1f)
                                ) { Text("Check-In") }
                            }
                        }
                        CheckInStatus.CHECKED_IN -> {
                            Button(
                                    onClick = { onCheckOutClick(child) },
                                    modifier = Modifier.weight(1f),
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.secondary
                                            )
                            ) { Text("Check Out") }
                        }
                    }

                    OutlinedButton(
                            onClick = { onEditClick(child) },
                            modifier = Modifier.weight(1f)
                    ) { Text("Edit") }
                }

                // Tertiary action row
                if (onViewServicesClick != null) {
                    OutlinedButton(
                            onClick = { onViewServicesClick(child) },
                            modifier = Modifier.fillMaxWidth()
                    ) { Text("View Services for ${child.name}") }
                }
            }
        }
    }
}

/** Status indicator component showing visual status of child */
@Composable
private fun StatusIndicator(status: CheckInStatus, modifier: Modifier = Modifier) {
    val statusInfo =
            when (status) {
                CheckInStatus.CHECKED_IN ->
                        Triple(Icons.Filled.CheckCircle, KidsColors.CheckedInColor, "Checked In")
                CheckInStatus.CHECKED_OUT ->
                        Triple(Icons.Outlined.Warning, KidsColors.CheckedOutColor, "Checked Out")
                CheckInStatus.NOT_IN_SERVICE ->
                        Triple(
                                Icons.Outlined.Warning,
                                KidsColors.NotInServiceColor,
                                "Not in Service"
                        )
                else -> Triple(Icons.Outlined.Warning, KidsColors.NotInServiceColor, "Unknown")
            }

    val (icon, color, text) = statusInfo

    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(16.dp)
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

/**
 * Format time string for display This is a simplified implementation - in a real app you'd use
 * proper date/time formatting
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

@Preview
@Composable
private fun ChildCardPreview() {
    MaterialTheme {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Checked out child
            ChildCard(
                    child =
                            Child(
                                    id = "1",
                                    parentId = "parent-1",
                                    name = "Emma Johnson",
                                    dateOfBirth = "2018-03-15",
                                    medicalInfo = "No known allergies",
                                    dietaryRestrictions = "Vegetarian",
                                    emergencyContact =
                                            EmergencyContact(
                                                    name = "Sarah Johnson",
                                                    phoneNumber = "+1-555-123-4567",
                                                    relationship = "Mother"
                                            ),
                                    status = CheckInStatus.CHECKED_OUT,
                                    currentServiceId = null,
                                    checkInTime = null,
                                    checkOutTime = "2024-01-15T11:30:00.000Z",
                                    createdAt = "2024-01-01T00:00:00.000Z",
                                    updatedAt = "2024-01-15T11:30:00.000Z"
                            ),
                    onCheckOutClick = {},
                    onEditClick = {}
            )

            // Checked in child
            ChildCard(
                    child =
                            Child(
                                    id = "2",
                                    parentId = "parent-2",
                                    name = "Liam Smith",
                                    dateOfBirth = "2016-08-22",
                                    medicalInfo = null,
                                    dietaryRestrictions = null,
                                    emergencyContact =
                                            EmergencyContact(
                                                    name = "Michael Smith",
                                                    phoneNumber = "+1-555-987-6543",
                                                    relationship = "Father"
                                            ),
                                    status = CheckInStatus.CHECKED_IN,
                                    currentServiceId = "service-1",
                                    checkInTime = "2024-01-15T09:15:00.000Z",
                                    checkOutTime = null,
                                    createdAt = "2024-01-01T00:00:00.000Z",
                                    updatedAt = "2024-01-15T09:15:00.000Z"
                            ),
                    currentService =
                            KidsService(
                                    id = "service-1",
                                    name = "Kids Church",
                                    description = "Main kids service",
                                    minAge = 5,
                                    maxAge = 12,
                                    startTime = "09:00:00",
                                    endTime = "10:30:00",
                                    location = "Kids Hall A",
                                    maxCapacity = 50,
                                    currentCapacity = 25,
                                    isAcceptingCheckIns = true,
                                    staffMembers = listOf("staff-1", "staff-2"),
                                    createdAt = "2024-01-01T00:00:00.000Z"
                            ),
                    onCheckOutClick = {},
                    onEditClick = {}
            )
        }
    }
}
