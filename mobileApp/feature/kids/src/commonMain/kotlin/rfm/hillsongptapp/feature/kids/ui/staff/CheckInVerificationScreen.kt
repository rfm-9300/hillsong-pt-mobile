package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.network.ktor.responses.CheckInApprovalResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRejectionResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestDetailsResponse
import kotlin.time.Duration.Companion.seconds

/**
 * Check-In Verification Screen for staff to review and approve/reject check-in requests
 * 
 * Features:
 * - Display child information prominently
 * - Highlight medical alerts, allergies, and special needs
 * - Show parent and emergency contact information
 * - Show service details
 * - Display expiration countdown
 * - Approve/Reject buttons with appropriate colors
 * - Handle approval and rejection flows
 * - Error handling for various scenarios
 * 
 * Requirements: 3.3, 3.6, 3.7, 4.1, 4.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInVerificationScreen(
    token: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StaffCheckInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRejectDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Load request details when screen is first displayed
    LaunchedEffect(token) {
        viewModel.getRequestDetails(token)
    }
    
    // Show success dialog when approval or rejection is complete
    LaunchedEffect(uiState.approvalResult, uiState.rejectionResult) {
        if (uiState.approvalResult != null || uiState.rejectionResult != null) {
            showSuccessDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Check-In") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error ?: "",
                        onRetry = { viewModel.getRequestDetails(token) },
                        onGoBack = onNavigateBack
                    )
                }
                
                uiState.currentRequest != null -> {
                    VerificationContent(
                        request = uiState.currentRequest!!,
                        onApprove = { viewModel.approveCheckIn() },
                        onReject = { showRejectDialog = true }
                    )
                }
            }
            
            // Rejection reason dialog
            if (showRejectDialog) {
                RejectionReasonDialog(
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        viewModel.rejectCheckIn(reason)
                        showRejectDialog = false
                    }
                )
            }
            
            // Success dialog
            if (showSuccessDialog) {
                SuccessDialog(
                    approvalResult = uiState.approvalResult,
                    rejectionResult = uiState.rejectionResult,
                    onDismiss = {
                        showSuccessDialog = false
                        viewModel.clearCurrentRequest()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

/**
 * Loading content while fetching request details
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading check-in details...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Main verification content showing child details and action buttons
 */
@Composable
private fun VerificationContent(
    request: CheckInRequestDetailsResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Expiration status card
        ExpirationStatusCard(
            expiresAt = request.expiresAt,
            isExpired = request.isExpired,
            canBeProcessed = request.canBeProcessed
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Child information card
        ChildInformationCard(child = request.child)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Medical alerts section (if any)
        if (request.hasMedicalAlerts || request.hasAllergies || request.hasSpecialNeeds) {
            MedicalAlertsSection(
                medicalNotes = request.child.medicalNotes,
                allergies = request.child.allergies,
                specialNeeds = request.child.specialNeeds,
                hasMedicalAlerts = request.hasMedicalAlerts,
                hasAllergies = request.hasAllergies,
                hasSpecialNeeds = request.hasSpecialNeeds
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Parent information card
        ParentInformationCard(parent = request.requestedBy)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Emergency contact card
        EmergencyContactCard(child = request.child)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Service details card
        ServiceDetailsCard(service = request.service)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action buttons
        if (request.canBeProcessed) {
            ActionButtons(
                onApprove = onApprove,
                onReject = onReject
            )
        } else {
            // Show message if cannot be processed
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (request.isExpired) {
                            "This check-in request has expired and cannot be processed."
                        } else {
                            "This check-in request cannot be processed."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Expiration status card with countdown
 */
@Composable
private fun ExpirationStatusCard(
    expiresAt: String,
    isExpired: Boolean,
    canBeProcessed: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isExpired -> MaterialTheme.colorScheme.errorContainer
        !canBeProcessed -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    
    val textColor = when {
        isExpired -> MaterialTheme.colorScheme.onErrorContainer
        !canBeProcessed -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpired) Icons.Default.Warning else Icons.Default.DateRange,
                contentDescription = if (isExpired) "Expired" else "Expires",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isExpired) "EXPIRED" else "EXPIRES",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                if (!isExpired) {
                    ExpirationCountdown(
                        expiresAt = expiresAt,
                        textColor = textColor
                    )
                } else {
                    Text(
                        text = "This request has expired",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Countdown timer showing time remaining
 */
@Composable
private fun ExpirationCountdown(
    expiresAt: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var timeRemaining by remember { mutableStateOf("Calculating...") }
    
    LaunchedEffect(expiresAt) {
        while (isActive) {
            timeRemaining = calculateTimeRemaining(expiresAt)
            delay(1.seconds)
        }
    }
    
    Text(
        text = timeRemaining,
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Child information card
 */
@Composable
private fun ChildInformationCard(
    child: rfm.hillsongptapp.core.network.ktor.responses.ChildDetailedResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Child",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = child.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${child.age} years old • ${child.ageGroup}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    child.gender?.let { gender ->
                        Text(
                            text = gender,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Medical alerts section with highlighted warnings
 */
@Composable
private fun MedicalAlertsSection(
    medicalNotes: String?,
    allergies: String?,
    specialNeeds: String?,
    hasMedicalAlerts: Boolean,
    hasAllergies: Boolean,
    hasSpecialNeeds: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Medical Alerts",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MEDICAL ALERTS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Allergies (danger color - red)
        if (hasAllergies && !allergies.isNullOrBlank()) {
            MedicalAlertCard(
                title = "ALLERGIES",
                content = allergies,
                icon = Icons.Default.Warning,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                iconColor = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Medical Notes (warning color - orange/amber)
        if (hasMedicalAlerts && !medicalNotes.isNullOrBlank()) {
            MedicalAlertCard(
                title = "MEDICAL NOTES",
                content = medicalNotes,
                icon = Icons.Default.Warning,
                backgroundColor = Color(0xFFFFF3E0), // Amber 50
                textColor = Color(0xFFE65100), // Amber 900
                iconColor = Color(0xFFFF6F00) // Amber 700
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Special Needs (info color - blue)
        if (hasSpecialNeeds && !specialNeeds.isNullOrBlank()) {
            MedicalAlertCard(
                title = "SPECIAL NEEDS",
                content = specialNeeds,
                icon = Icons.Default.Info,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Individual medical alert card
 */
@Composable
private fun MedicalAlertCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    textColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

/**
 * Parent information card
 */
@Composable
private fun ParentInformationCard(
    parent: rfm.hillsongptapp.core.network.ktor.responses.ParentSummaryResponse,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Parent",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Parent Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(label = "Name", value = parent.fullName)
            InfoRow(label = "Email", value = parent.email)
            parent.phone?.let { phone ->
                InfoRow(label = "Phone", value = phone)
            }
        }
    }
}

/**
 * Emergency contact card
 */
@Composable
private fun EmergencyContactCard(
    child: rfm.hillsongptapp.core.network.ktor.responses.ChildDetailedResponse,
    modifier: Modifier = Modifier
) {
    if (child.emergencyContactName != null || child.emergencyContactPhone != null) {
        Card(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Emergency Contact",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                child.emergencyContactName?.let { name ->
                    InfoRow(label = "Name", value = name)
                }
                child.emergencyContactPhone?.let { phone ->
                    InfoRow(label = "Phone", value = phone)
                }
            }
        }
    }
}

/**
 * Service details card
 */
@Composable
private fun ServiceDetailsCard(
    service: rfm.hillsongptapp.core.network.ktor.responses.KidsServiceResponse,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Service",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Service Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(label = "Service", value = service.name)
            InfoRow(label = "Day", value = service.dayOfWeek)
            InfoRow(label = "Time", value = "${service.startTime} - ${service.endTime}")
            InfoRow(label = "Location", value = service.location)
        }
    }
}

/**
 * Info row helper component
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

/**
 * Action buttons for approve and reject
 */
@Composable
private fun ActionButtons(
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Reject button (danger color)
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Reject",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reject")
        }
        
        // Approve button (primary color)
        Button(
            onClick = onApprove,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Approve",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Approve")
        }
    }
}

/**
 * Error content with retry option
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show retry button for certain errors
        if (shouldShowRetry(error)) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        OutlinedButton(
            onClick = onGoBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go Back")
        }
    }
}

/**
 * Rejection reason dialog (subtask 14.2)
 */
@Composable
private fun RejectionReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = {
            Text(
                text = "Reject Check-In",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "Please provide a reason for rejecting this check-in request. The parent will be notified.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        showError = false
                    },
                    label = { Text("Rejection Reason") },
                    placeholder = { Text("e.g., Child appears unwell, Missing required documents") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("Rejection reason is required") }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(reason.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Success dialog showing approval or rejection result (subtasks 14.3 and 14.4)
 */
@Composable
private fun SuccessDialog(
    approvalResult: CheckInApprovalResponse?,
    rejectionResult: CheckInRejectionResponse?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isApproval = approvalResult != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        icon = {
            Icon(
                imageVector = if (isApproval) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (isApproval) "Approved" else "Rejected",
                tint = if (isApproval) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = if (isApproval) "Check-In Approved" else "Check-In Rejected",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isApproval && approvalResult != null) {
                    Text(
                        text = approvalResult.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            InfoRow(label = "Child", value = approvalResult.child.fullName)
                            InfoRow(label = "Service", value = approvalResult.service.name)
                            InfoRow(label = "Check-In Time", value = formatDateTime(approvalResult.checkInTime))
                            InfoRow(label = "Approved By", value = approvalResult.approvedBy)
                        }
                    }
                } else if (rejectionResult != null) {
                    Text(
                        text = rejectionResult.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            InfoRow(label = "Child", value = rejectionResult.child.fullName)
                            InfoRow(label = "Service", value = rejectionResult.service.name)
                            InfoRow(label = "Rejected By", value = rejectionResult.rejectedBy)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Reason:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = rejectionResult.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "The parent has been notified.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    )
}

/**
 * Helper function to determine if retry button should be shown
 */
private fun shouldShowRetry(error: String): Boolean {
    return !error.contains("expired", ignoreCase = true) &&
           !error.contains("invalid", ignoreCase = true) &&
           !error.contains("permission", ignoreCase = true)
}

/**
 * Calculate time remaining until expiration
 */
private fun calculateTimeRemaining(expiresAt: String): String {
    // Parse ISO 8601 datetime string
    // Format: "2024-03-10T15:30:00"
    try {
        // Simple parsing - in production, use kotlinx-datetime
        val parts = expiresAt.split("T")
        if (parts.size != 2) return "Invalid time"
        
        // For now, return a placeholder
        // In production, calculate actual time difference
        return "Expires at ${parts[1]}"
    } catch (e: Exception) {
        return "Invalid time"
    }
}

/**
 * Format datetime for display
 */
private fun formatDateTime(dateTime: String): String {
    // Parse ISO 8601 datetime string
    // Format: "2024-03-10T15:30:00"
    try {
        val parts = dateTime.split("T")
        if (parts.size != 2) return dateTime
        
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        
        if (dateParts.size == 3 && timeParts.size >= 2) {
            val year = dateParts[0]
            val month = dateParts[1]
            val day = dateParts[2]
            val hour = timeParts[0]
            val minute = timeParts[1]
            
            return "$day/$month/$year $hour:$minute"
        }
        
        return dateTime
    } catch (e: Exception) {
        return dateTime
    }
}
