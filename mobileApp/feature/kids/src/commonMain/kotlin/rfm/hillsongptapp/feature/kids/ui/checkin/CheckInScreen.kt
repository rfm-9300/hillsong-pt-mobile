package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.feature.kids.ui.model.EligibleServiceInfo
import rfm.hillsongptapp.feature.kids.ui.checkin.components.CheckInConfirmationDialog
import rfm.hillsongptapp.feature.kids.ui.checkin.components.EligibleServiceCard
import rfm.hillsongptapp.feature.kids.ui.checkin.components.CheckInErrorDialog

/**
 * Screen for checking in a child to a service with comprehensive validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    onCheckInSuccess: () -> Unit,
    viewModel: CheckInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(childId) {
        viewModel.loadChildAndEligibleServices(childId)
    }
    
    // Handle successful check-in
    LaunchedEffect(uiState.checkInSuccess) {
        if (uiState.checkInSuccess) {
            onCheckInSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.child != null) {
                            "Check In ${uiState.child!!.name}"
                        } else {
                            "Check In Child"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        onRetry = { viewModel.loadChildAndEligibleServices(childId) },
                        onDismiss = { viewModel.clearError() }
                    )
                }
                uiState.child != null -> {
                    // TODO: Fix type mismatch between KidsService and EligibleServiceInfo
                    Text("Check-in functionality temporarily disabled during migration")
                }
                else -> {
                    EmptyContent()
                }
            }
            
            // Confirmation Dialog
            if (uiState.showConfirmationDialog && uiState.selectedService != null && uiState.child != null) {
                CheckInConfirmationDialog(
                    child = uiState.child!!,
                    service = uiState.selectedService!!,
                    onConfirm = { notes ->
                        viewModel.checkInChild(notes)
                    },
                    onDismiss = viewModel::hideCheckInConfirmation,
                    isLoading = uiState.isCheckingIn
                )
            }
            
            // Error Dialog
            if (uiState.checkInError != null) {
                CheckInErrorDialog(
                    error = uiState.checkInError!!,
                    onDismiss = viewModel::clearCheckInError,
                    onRetry = { viewModel.checkInChild() }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading child information and available services...",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Unable to Load Check-In Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Child Not Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The requested child could not be found. Please check the child ID and try again.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CheckInContent(
    child: Child,
    eligibleServices: List<KidsService>,
    selectedService: KidsService?,
    onServiceSelected: (KidsService) -> Unit,
    onCheckInClicked: () -> Unit,
    isCheckingIn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Child Information Card
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
                    text = child.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Age: ${child.calculateAge()} years",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Current Status: ${child.status.getDisplayName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Services Section
        Text(
            text = "Available Services",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (eligibleServices.isEmpty()) {
            NoServicesAvailable(child = child)
        } else {
            Text(
                text = "Select a service to check ${child.name} into:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eligibleServices.size) { index ->
                    val service = eligibleServices[index]
                    // Convert KidsService to EligibleServiceInfo for the card
                    val serviceInfo = EligibleServiceInfo(
                        service = service,
                        isEligible = true,
                        isRecommended = false,
                        availableSpots = service.getAvailableSpots()
                    )
                    EligibleServiceCard(
                        serviceInfo = serviceInfo,
                        isSelected = selectedService?.id == service.id,
                        onSelect = { onServiceSelected(service) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Check-In Button
            Button(
                onClick = onCheckInClicked,
                enabled = selectedService != null && !isCheckingIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCheckingIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking In...")
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In to Selected Service")
                }
            }
        }
    }
}

@Composable
private fun NoServicesAvailable(child: Child) {
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No Services Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "There are currently no services available for ${child.name} (age ${child.calculateAge()}). " +
                        "This could be because:\n\n" +
                        "• All age-appropriate services are at full capacity\n" +
                        "• No services are currently accepting check-ins\n" +
                        "• No services match the child's age requirements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInContent(
    uiState: CheckInUiState,
    childId: String,
    onNavigateBack: () -> Unit = {},
    onCheckInSuccess: () -> Unit = {},
    onLoadChildAndServices: (String) -> Unit = {},
    onServiceSelected: (EligibleServiceInfo) -> Unit = {},
    onShowCheckInConfirmation: () -> Unit = {},
    onCheckInChild: (String?) -> Unit = {},
    onHideCheckInConfirmation: () -> Unit = {},
    onClearError: () -> Unit = {},
    onClearCheckInError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.child != null) {
                            "Check In ${uiState.child!!.name}"
                        } else {
                            "Check In Child"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                        error = uiState.error!!,
                        onRetry = { onLoadChildAndServices(childId) },
                        onDismiss = onClearError
                    )
                }
                uiState.child != null -> {
                    CheckInContent(
                        child = uiState.child!!,
                        eligibleServices = uiState.eligibleServices,
                        selectedService = uiState.selectedService,
                        onServiceSelected = { service ->
                            // Convert KidsService to EligibleServiceInfo for the callback
                            val serviceInfo = EligibleServiceInfo(
                                service = service,
                                isEligible = true,
                                isRecommended = false,
                                availableSpots = service.getAvailableSpots()
                            )
                            onServiceSelected(serviceInfo)
                        },
                        onCheckInClicked = onShowCheckInConfirmation,
                        isCheckingIn = uiState.isCheckingIn
                    )
                }
                else -> {
                    EmptyContent()
                }
            }
            
            // Dialogs
            if (uiState.showConfirmationDialog && uiState.selectedService != null && uiState.child != null) {
                CheckInConfirmationDialog(
                    child = uiState.child!!,
                    service = uiState.selectedService!!, // TODO: Fix when EligibleServiceInfo is properly migrated
                    onConfirm = onCheckInChild,
                    onDismiss = onHideCheckInConfirmation,
                    isLoading = uiState.isCheckingIn
                )
            }
            
            if (uiState.checkInError != null) {
                CheckInErrorDialog(
                    error = uiState.checkInError!!,
                    onDismiss = onClearCheckInError,
                    onRetry = { onCheckInChild(null) }
                )
            }
        }
    }
}

// MARK: - Preview

@Preview
@Composable
private fun CheckInContentPreview() {
    MaterialTheme {
        Surface {
            CheckInContent(
                uiState = CheckInUiState(
                    child = null,
                    eligibleServices = emptyList(),
                    isLoading = false,
                    error = null
                ),
                childId = "preview-child"
            )
        }
    }
}