package rfm.hillsongptapp.feature.kids.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.ui.checkout.components.CheckOutConfirmationDialog
import rfm.hillsongptapp.feature.kids.ui.checkout.components.CheckOutErrorDialog
import rfm.hillsongptapp.feature.kids.ui.checkout.components.CheckOutSuccessDialog
import rfm.hillsongptapp.feature.kids.ui.checkout.components.ParentVerificationDialog

/**
 * Screen for checking out a child from their current service
 * Includes parent verification and confirmation steps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOutScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    viewModel: CheckOutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load child information when screen opens
    LaunchedEffect(childId) {
        viewModel.loadChild(childId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.child != null) {
                            "Check Out ${uiState.child!!.name}"
                        } else {
                            "Check Out Child"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadChild(childId) },
                        onDismiss = { viewModel.clearError() }
                    )
                }
                uiState.child != null -> {
                    CheckOutContent(
                        child = uiState.child!!,
                        currentService = uiState.currentService,
                        eligibilityInfo = uiState.eligibilityInfo,
                        onStartCheckOut = { viewModel.startCheckOutProcess() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    EmptyContent()
                }
            }
        }
    }
    
    // Handle dialogs
    HandleDialogs(
        uiState = uiState,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading child information...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Error Loading Child",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                    
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckOutContent(
    child: Child,
    currentService: rfm.hillsongptapp.feature.kids.domain.model.KidsService?,
    eligibilityInfo: rfm.hillsongptapp.feature.kids.domain.usecase.CheckOutEligibilityInfo?,
    onStartCheckOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Child Information Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Child Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Name:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = child.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Age:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${child.calculateAge()} years",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = child.status.getDisplayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (child.status) {
                            rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus.CHECKED_IN -> 
                                MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current Service Information (if checked in)
        if (currentService != null && child.isCheckedIn()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Currently Checked Into",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = currentService.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currentService.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    child.checkInTime?.let { checkInTime ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Checked in at: ${formatTime(checkInTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Check-out eligibility and action
        if (eligibilityInfo != null) {
            if (eligibilityInfo.canCheckOut) {
                // Check-out button
                Button(
                    onClick = onStartCheckOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Out ${child.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Information card
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
                            text = "Check-Out Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• You will be asked to verify your identity before check-out",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "• A confirmation dialog will appear to prevent accidental check-outs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = "• You'll receive a summary of the service attendance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Cannot check out - show reason
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Cannot check out",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Cannot Check Out",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = eligibilityInfo.reason ?: "Unknown reason",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Child not found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HandleDialogs(
    uiState: CheckOutUiState,
    viewModel: CheckOutViewModel,
    onNavigateBack: () -> Unit
) {
    // Parent verification dialog
    if (uiState.showParentVerification) {
        ParentVerificationDialog(
            onVerified = { viewModel.onParentVerified() },
            onDismiss = { viewModel.hideParentVerification() }
        )
    }
    
    // Check-out confirmation dialog
    if (uiState.showCheckOutConfirmation && uiState.child != null) {
        CheckOutConfirmationDialog(
            child = uiState.child,
            currentService = uiState.currentService,
            onConfirm = { viewModel.confirmCheckOut() },
            onDismiss = { viewModel.hideCheckOutConfirmation() }
        )
    }
    
    // Success dialog
    if (uiState.showSuccessDialog && uiState.checkOutResult != null) {
        CheckOutSuccessDialog(
            result = uiState.checkOutResult,
            onDismiss = { 
                viewModel.hideSuccessDialog()
                onNavigateBack()
            }
        )
    }
    
    // Error dialog
    if (uiState.showErrorDialog && uiState.checkOutError != null) {
        CheckOutErrorDialog(
            error = uiState.checkOutError,
            onDismiss = { viewModel.hideErrorDialog() },
            onRetry = { viewModel.retryCheckOut() }
        )
    }
}

/**
 * Format time string for display
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
        isoTime
    }
}