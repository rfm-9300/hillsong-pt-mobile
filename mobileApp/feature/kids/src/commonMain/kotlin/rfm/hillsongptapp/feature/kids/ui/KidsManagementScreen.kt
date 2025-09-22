package rfm.hillsongptapp.feature.kids.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.ui.components.ChildCard
import rfm.hillsongptapp.feature.kids.ui.components.CheckOutDialog
import rfm.hillsongptapp.feature.kids.ui.components.ConnectionStatusIndicator
import rfm.hillsongptapp.feature.kids.ui.components.ConnectionStatusBanner
import rfm.hillsongptapp.feature.kids.ui.components.FloatingNotificationOverlay
import rfm.hillsongptapp.feature.kids.data.network.websocket.ConnectionStatus
import rfm.hillsongptapp.feature.kids.data.network.websocket.StatusNotification

/**
 * Main screen for Kids Management displaying list of registered children with current status
 * Supports pull-to-refresh functionality for real-time status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsManagementScreen(
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToServicesForChild: (String) -> Unit = {},
    onNavigateToCheckIn: (String) -> Unit = {},
    onNavigateToCheckOut: (String) -> Unit = {},
    onNavigateToChildEdit: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: KidsManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val notifications by viewModel.activeNotifications.collectAsState()
    
    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // In a real app, you'd show this in a SnackbarHost
            // For now, we'll just clear it after showing
            viewModel.clearError()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar with connection status
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kids Management",
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.showConnectionStatus) {
                            ConnectionStatusIndicator(
                                connectionStatus = connectionStatus,
                                showText = true,
                                compact = true
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToServices
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View Services"
                        )
                    }
                    
                    // Staff Reports - only show for staff users
                    if (uiState.hasStaffPermissions) {
                        IconButton(
                            onClick = onNavigateToReports
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Staff Reports"
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
            
            // Connection status banner for non-connected states
            ConnectionStatusBanner(
                connectionStatus = connectionStatus,
                onDismiss = null // Don't allow dismissing for now
            )
            
            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }
                    
                    !uiState.hasChildren -> {
                        EmptyContent(
                            onRegisterClick = onNavigateToRegistration
                        )
                    }
                    
                    else -> {
                        ChildrenListContent(
                            uiState = uiState,
                            connectionStatus = connectionStatus,
                            lastUpdatedTime = viewModel.getLastUpdatedTime(),
                            onRefresh = { viewModel.refreshData() },
                            onCheckInClick = { child -> onNavigateToCheckIn(child.id) },
                            onCheckOutClick = { child -> viewModel.showCheckOutDialog(child) },
                            onEditClick = onNavigateToChildEdit as (Child) -> Unit,
                            onRegisterClick = onNavigateToRegistration,
                            onRetryConnection = { viewModel.retryConnection() }
                        )
                    }
                }
                
                // Floating Action Button for adding new child
                FloatingActionButton(
                    onClick = onNavigateToRegistration,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Register New Child"
                    )
                }
            }
        }
        
        // Floating notification overlay
        FloatingNotificationOverlay(
            notification = notifications.firstOrNull(),
            onDismiss = { 
                notifications.firstOrNull()?.let { notification ->
                    viewModel.dismissNotification(notification)
                }
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    
    // Dialogs - Check-in now handled by dedicated screen
    
    if (uiState.showCheckOutDialog && uiState.selectedChild != null) {
        CheckOutDialog(
            child = uiState.selectedChild!!,
            currentService = uiState.services.find { it.id == uiState.selectedChild!!.currentServiceId },
            onCheckOut = { childId ->
                viewModel.checkOutChild(childId)
            },
            onDismiss = { viewModel.hideCheckOutDialog() }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, show snackbar here
            // Error occurred
        }
    }
}

/**
 * Loading content displayed while data is being fetched
 */
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
                text = "Loading children...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Empty content displayed when no children are registered
 */
@Composable
private fun EmptyContent(
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No Children Registered",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Register your first child to get started with kids services",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Child")
            }
        }
    }
}

/**
 * Content displaying list of children with pull-to-refresh and real-time status
 */
@Composable
private fun ChildrenListContent(
    uiState: KidsManagementUiState,
    connectionStatus: ConnectionStatus,
    lastUpdatedTime: String,
    onRefresh: () -> Unit,
    onCheckInClick: (Child) -> Unit,
    onCheckOutClick: (Child) -> Unit,
    onEditClick: (Child) -> Unit,
    onRegisterClick: () -> Unit,
    onRetryConnection: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary section with real-time status
        item {
            SummaryCard(
                totalChildren = uiState.children.size,
                checkedInCount = uiState.checkedInChildren.size,
                availableCount = uiState.availableChildren.size,
                connectionStatus = connectionStatus,
                lastUpdatedTime = lastUpdatedTime,
                onRetryConnection = onRetryConnection
            )
        }
        
        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Children",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onRegisterClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Child")
                }
            }
        }
        
        // Children list
        items(
            items = uiState.children,
            key = { child -> child.id }
        ) { child ->
            val currentService = uiState.services.find { it.id == child.currentServiceId }
            
            ChildCard(
                child = child,
                currentService = currentService,
                onCheckInClick = onCheckInClick,
                onCheckOutClick = onCheckOutClick,
                onEditClick = onEditClick
            )
        }
        
        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Summary card showing overview statistics with real-time status
 */
@Composable
private fun SummaryCard(
    totalChildren: Int,
    checkedInCount: Int,
    availableCount: Int,
    connectionStatus: ConnectionStatus,
    lastUpdatedTime: String,
    onRetryConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total",
                    value = totalChildren.toString()
                )
                
                SummaryItem(
                    label = "Checked In",
                    value = checkedInCount.toString()
                )
                
                SummaryItem(
                    label = "Available",
                    value = availableCount.toString()
                )
            }
            
            // Real-time status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionStatusIndicator(
                    connectionStatus = connectionStatus,
                    showText = true,
                    compact = true
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Last updated: $lastUpdatedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    if (connectionStatus == ConnectionStatus.FAILED) {
                        TextButton(
                            onClick = onRetryConnection,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual summary item
 */
@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Pure UI content for Kids Management that doesn't depend on ViewModel
 * This can be used for previews and testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsManagementContent(
    uiState: KidsManagementUiState,
    connectionStatus: ConnectionStatus,
    notifications: List<Any> = emptyList(), // Simplified for preview
    onNavigateToRegistration: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToServicesForChild: (String) -> Unit = {},
    onNavigateToCheckIn: (String) -> Unit = {},
    onNavigateToCheckOut: (String) -> Unit = {},
    onNavigateToChildEdit: (String) -> Unit = {},
    onRefreshData: () -> Unit = {},
    onRetryConnection: () -> Unit = {},
    onShowCheckOutDialog: (Child) -> Unit = {},
    onCheckOutChild: (String) -> Unit = {},
    onHideCheckOutDialog: () -> Unit = {},
    onDismissNotification: () -> Unit = {},
    onClearError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar with connection status
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kids Management",
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.showConnectionStatus) {
                            ConnectionStatusIndicator(
                                connectionStatus = connectionStatus,
                                showText = true,
                                compact = true
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToServices
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "View Services"
                        )
                    }
                    
                    // Staff Reports - only show for staff users
                    if (uiState.hasStaffPermissions) {
                        IconButton(
                            onClick = onNavigateToReports
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Staff Reports"
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onRefreshData
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
            
            // Connection status banner for non-connected states
            ConnectionStatusBanner(
                connectionStatus = connectionStatus,
                onDismiss = null // Don't allow dismissing for now
            )
            
            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }
                    
                    !uiState.hasChildren -> {
                        EmptyContent(
                            onRegisterClick = onNavigateToRegistration
                        )
                    }
                    
                    else -> {
                        ChildrenListContent(
                            uiState = uiState,
                            connectionStatus = connectionStatus,
                            lastUpdatedTime = "Just now", // Simplified for preview
                            onRefresh = onRefreshData,
                            onCheckInClick = { child -> onNavigateToCheckIn(child.id) },
                            onCheckOutClick = onShowCheckOutDialog,
                            onEditClick = { child -> onNavigateToChildEdit(child.id) },
                            onRegisterClick = onNavigateToRegistration,
                            onRetryConnection = onRetryConnection
                        )
                    }
                }
                
                // Floating Action Button for adding new child
                FloatingActionButton(
                    onClick = onNavigateToRegistration,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Register New Child"
                    )
                }
            }
        }
        
        // Floating notification overlay (simplified for preview)
        if (notifications.isNotEmpty()) {
            FloatingNotificationOverlay(
                notification = notifications.firstOrNull() as StatusNotification?,
                onDismiss = onDismissNotification,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
    
    // Dialogs
    if (uiState.showCheckOutDialog && uiState.selectedChild != null) {
        CheckOutDialog(
            child = uiState.selectedChild!!,
            currentService = uiState.services.find { it.id == uiState.selectedChild!!.currentServiceId },
            onCheckOut = onCheckOutChild,
            onDismiss = onHideCheckOutDialog
        )
    }
}


// MARK: - Preview

@Preview
@Composable
private fun KidsManagementContentPreview() {
    MaterialTheme {
        Surface {
            KidsManagementContent(
                uiState = KidsManagementUiState(
                    children = emptyList(),
                    services = emptyList(),
                    isLoading = false,
                    hasStaffPermissions = false
                ),
                connectionStatus = ConnectionStatus.CONNECTED
            )
        }
    }
}