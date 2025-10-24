package rfm.hillsongptapp.feature.kids.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import hillsongptapp.feature.kids.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.navigation.*
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.designsystem.ui.icons.AppIcons
import rfm.hillsongptapp.feature.kids.ui.components.CheckOutDialog
import rfm.hillsongptapp.feature.kids.ui.components.ChildCard
import rfm.hillsongptapp.feature.kids.ui.components.ConnectionStatusBanner
import rfm.hillsongptapp.feature.kids.ui.components.ConnectionStatusIndicator
import rfm.hillsongptapp.feature.kids.ui.components.FloatingNotificationOverlay
import rfm.hillsongptapp.feature.kids.ui.components.ServiceSelectionDialog
import rfm.hillsongptapp.feature.kids.ui.model.*
import rfm.hillsongptapp.feature.kids.ui.model.ConnectionStatus
import rfm.hillsongptapp.feature.kids.ui.model.StatusNotification
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * Main screen for Kids Management displaying list of registered children with current status
 * Supports pull-to-refresh functionality for real-time status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsManagementScreen(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        viewModel: KidsManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // TODO: Re-enable when real-time features are implemented
    // val connectionStatus by viewModel.connectionStatus.collectAsState()
    // val notifications by viewModel.activeNotifications.collectAsState()

    // Temporary defaults until real-time features are implemented
    val connectionStatus = ConnectionStatus.CONNECTED
    val notifications = emptyList<StatusNotification>()
    
    // State for service selection dialog
    var showServiceSelectionDialog by remember { mutableStateOf(false) }
    var selectedChildForQR by remember { mutableStateOf<Child?>(null) }


    val shouldRefresh by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_key", false)
        ?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.loadInitialData()
            // Clear the flag
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh_key", false)
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // In a real app, you'd show this in a SnackbarHost
            // For now, we'll just clear it after showing
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with connection status
            TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(Res.string.kids_management_title), fontWeight = FontWeight.Bold)
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

                        // Staff Dashboard - only show for staff users
                        if (uiState.hasStaffPermissions) {
                            IconButton(onClick = { navController.navigateToStaffDashboard() }) {
                                Icon(
                                        imageVector = AppIcons.AdminPanel,
                                        contentDescription = stringResource(Res.string.staff_dashboard)
                                )
                            }
                        }

                        // Staff Reports - only show for staff users
                        if (uiState.hasStaffPermissions) {
                            IconButton(onClick = { navController.navigateToKidsReports() }) {
                                Icon(
                                        imageVector = AppIcons.Report,
                                        contentDescription = stringResource(Res.string.staff_reports)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.refreshData() }) {
                            Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(Res.string.refresh)
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
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }
                    !uiState.hasChildren -> {
                        EmptyContent(onRegisterClick = { navController.navigateToKidsRegistration() })
                    }
                    else -> {
                        ChildrenListContent(
                                uiState = uiState,
                                connectionStatus = connectionStatus,
                                lastUpdatedTime = stringResource(Res.string.just_now), // TODO: Implement last updated time
                                onRefresh = { viewModel.refreshData() },
                                onCheckInClick = { child -> navController.navigateToKidsCheckIn(child.id) },
                                onCheckOutClick = { child -> viewModel.showCheckOutDialog(child) },
                                onEditClick = { child -> navController.navigateToKidsEditChild(child.id) },
                                onRegisterClick = { navController.navigateToKidsRegistration() },
                                onRetryConnection = { /* TODO: Implement retry connection */},
                                onQRCheckInClick = { child ->
                                    selectedChildForQR = child
                                    showServiceSelectionDialog = true
                                },
                                onCancelCheckInRequest = { requestId ->
                                    viewModel.cancelCheckInRequest(requestId)
                                }
                        )
                    }
                }

                // Floating Action Button for adding new child
                FloatingActionButton(
                        onClick = { navController.navigateToKidsRegistration() },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(Res.string.register_new_child))
                }
            }
        }

        // Floating notification overlay
        FloatingNotificationOverlay(
                notification = notifications.firstOrNull(),
                onDismiss = {
                    // TODO: Implement notification dismissal
                },
                modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Dialogs - Check-in now handled by dedicated screen

    if (uiState.showCheckOutDialog && uiState.selectedChild != null) {
        CheckOutDialog(
                child = uiState.selectedChild!!,
                currentService =
                        uiState.services.find { it.id == uiState.selectedChild!!.currentServiceId },
                onCheckOut = { childId -> viewModel.checkOutChild(childId) },
                onDismiss = { viewModel.hideCheckOutDialog() }
        )
    }

    // Service selection dialog for QR check-in
    if (showServiceSelectionDialog && selectedChildForQR != null) {
        LoggerHelper.logDebug("Showing service selection dialog for child: ${selectedChildForQR!!.name}", "KidsManagementScreen")
        ServiceSelectionDialog(
            services = uiState.services,
            childName = selectedChildForQR!!.name,
            onServiceSelected = { service ->
                LoggerHelper.logDebug("=== ENTERED onServiceSelected CALLBACK ===", "KidsManagementScreen")
                // Convert String IDs to Long for the API call
                LoggerHelper.logDebug("Raw child ID string: '${selectedChildForQR!!.id}'", "KidsManagementScreen")
                LoggerHelper.logDebug("Raw service ID string: '${service.id}'", "KidsManagementScreen")
                val childId = selectedChildForQR!!.id.toLongOrNull() ?: 0L
                val serviceId = service.id.toLongOrNull() ?: 0L
                LoggerHelper.logDebug("Converted IDs - childId=$childId, serviceId=$serviceId", "KidsManagementScreen")
                
                if (childId == 0L || serviceId == 0L) {
                    LoggerHelper.logDebug("ERROR: ID conversion failed! childId=$childId, serviceId=$serviceId", "KidsManagementScreen")
                } else {
                    LoggerHelper.logDebug("Calling navController.navigateToQRCodeDisplay", "KidsManagementScreen")
                    navController.navigateToQRCodeDisplay(childId, serviceId)
                }
                
                showServiceSelectionDialog = false
                selectedChildForQR = null
            },
            onDismiss = {
                LoggerHelper.logDebug("Service selection dialog dismissed", "KidsManagementScreen")
                showServiceSelectionDialog = false
                selectedChildForQR = null
            }
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

/** Loading content displayed while data is being fetched */
@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(Res.string.loading_children), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Empty content displayed when no children are registered */
@Composable
private fun EmptyContent(onRegisterClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
        ) {
            Text(
                    text = stringResource(Res.string.no_children_registered),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                    text = stringResource(Res.string.register_first_child_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.register_child))
            }
        }
    }
}

/** Content displaying list of children with pull-to-refresh and real-time status */
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
        onRetryConnection: () -> Unit,
        onQRCheckInClick: (Child) -> Unit,
        onCancelCheckInRequest: (Long) -> Unit
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
                        text = stringResource(Res.string.your_children),
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
                    Text(stringResource(Res.string.add_child))
                }
            }
        }

        // Children list
        items(items = uiState.children, key = { child -> child.id }) { child ->
            val currentService = uiState.services.find { it.id == child.currentServiceId }
            val checkInRequest = uiState.checkInRequests[child.id]

            ChildCard(
                    child = child,
                    currentService = currentService,
                    checkInRequest = checkInRequest,
                    onCheckOutClick = onCheckOutClick,
                    onEditClick = onEditClick,
                    onQRCheckInClick = onQRCheckInClick,
                    onCancelCheckInRequest = onCancelCheckInRequest
            )
        }

        // Bottom spacing for FAB
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/** Summary card showing overview statistics with real-time status */
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
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
    ) {
        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Statistics row
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(label = stringResource(Res.string.total), value = totalChildren.toString())

                SummaryItem(label = stringResource(Res.string.checked_in), value = checkedInCount.toString())

                SummaryItem(label = stringResource(Res.string.available), value = availableCount.toString())
            }

            // Real-time status row
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                            text = stringResource(Res.string.last_updated, lastUpdatedTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    if (connectionStatus == ConnectionStatus.ERROR) {
                        TextButton(onClick = onRetryConnection, modifier = Modifier.padding(0.dp)) {
                            Text(text = stringResource(Res.string.retry), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

/** Individual summary item */
@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
 * Pure UI content for Kids Management that doesn't depend on ViewModel This can be used for
 * previews and testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsManagementContent(
        uiState: KidsManagementUiState,
        connectionStatus: ConnectionStatus,
        notifications: List<Any> = emptyList(), // Simplified for preview
        onNavigateToRegistration: () -> Unit = {},
        onNavigateToCheckIn: (String) -> Unit = {},
        onNavigateToChildEdit: (String) -> Unit = {},
        onRefreshData: () -> Unit = {},
        onRetryConnection: () -> Unit = {},
        onShowCheckOutDialog: (Child) -> Unit = {},
        onCheckOutChild: (String) -> Unit = {},
        onHideCheckOutDialog: () -> Unit = {},
        onDismissNotification: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar with connection status
            TopAppBar(
                    title = {
                        Column {
                            Text(text = stringResource(Res.string.kids_management_title), fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {

                        IconButton(onClick = onRefreshData) {
                            Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(Res.string.refresh)
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
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        LoadingContent()
                    }
                    !uiState.hasChildren -> {
                        EmptyContent(onRegisterClick = onNavigateToRegistration)
                    }
                    else -> {
                        ChildrenListContent(
                                uiState = uiState,
                                connectionStatus = connectionStatus,
                                lastUpdatedTime = stringResource(Res.string.just_now), // Simplified for preview
                                onRefresh = onRefreshData,
                                onCheckInClick = { child -> onNavigateToCheckIn(child.id) },
                                onCheckOutClick = onShowCheckOutDialog,
                                onEditClick = { child -> onNavigateToChildEdit(child.id) },
                                onRegisterClick = onNavigateToRegistration,
                                onRetryConnection = onRetryConnection,
                                onQRCheckInClick = { },
                                onCancelCheckInRequest = { }
                        )
                    }
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
                currentService =
                        uiState.services.find { it.id == uiState.selectedChild!!.currentServiceId },
                onCheckOut = onCheckOutChild,
                onDismiss = onHideCheckOutDialog
        )
    }
}

// MARK: - Preview

@Preview
@Composable
private fun KidsManagementContentPreview() {
    // Sample children data
    val sampleChildren = listOf(
        Child(
            id = "1",
            parentId = "parent-1",
            name = "Emma Smith",
            dateOfBirth = "2018-05-15",
            medicalInfo = "Mild asthma",
            dietaryRestrictions = "Peanuts",
            emergencyContact = EmergencyContact(
                name = "Jane Smith",
                phoneNumber = "+1234567890",
                relationship = "Mother"
            ),
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service-1",
            checkInTime = "2025-01-10T09:30:00",
            checkOutTime = null,
            createdAt = "2025-01-01T00:00:00",
            updatedAt = "2025-01-10T09:30:00"
        ),
        Child(
            id = "2",
            parentId = "parent-1",
            name = "Lucas Johnson",
            dateOfBirth = "2020-03-22",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact(
                name = "Mike Johnson",
                phoneNumber = "+1234567891",
                relationship = "Father"
            ),
            status = CheckInStatus.NOT_IN_SERVICE,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2025-01-01T00:00:00",
            updatedAt = "2025-01-05T10:00:00"
        ),
        Child(
            id = "3",
            parentId = "parent-1",
            name = "Sofia Martinez",
            dateOfBirth = "2016-11-08",
            medicalInfo = null,
            dietaryRestrictions = "Dairy",
            emergencyContact = EmergencyContact(
                name = "Maria Martinez",
                phoneNumber = "+1234567892",
                relationship = "Mother"
            ),
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service-2",
            checkInTime = "2025-01-10T11:30:00",
            checkOutTime = null,
            createdAt = "2025-01-01T00:00:00",
            updatedAt = "2025-01-10T11:30:00"
        )
    )
    
    // Sample services data
    val sampleServices = listOf(
        rfm.hillsongptapp.core.data.model.KidsService(
            id = "service-1",
            name = "Morning 09h:30",
            description = "Sunday morning service for preschool and elementary children",
            minAge = 3,
            maxAge = 10,
            startTime = "2025-01-12T09:30:00",
            endTime = "2025-01-12T11:30:00",
            location = "Main Hall",
            maxCapacity = 50,
            currentCapacity = 15,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Sarah Johnson", "Mike Davis"),
            createdAt = "2025-01-01T00:00:00"
        ),
        rfm.hillsongptapp.core.data.model.KidsService(
            id = "service-2",
            name = "Morning 11h:30",
            description = "Sunday late morning service for elementary children",
            minAge = 6,
            maxAge = 12,
            startTime = "2025-01-12T11:30:00",
            endTime = "2025-01-12T13:30:00",
            location = "Main Hall",
            maxCapacity = 50,
            currentCapacity = 22,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Emily Brown", "Tom Wilson"),
            createdAt = "2025-01-01T00:00:00"
        ),
        rfm.hillsongptapp.core.data.model.KidsService(
            id = "service-3",
            name = "Evening 17h:30",
            description = "Sunday evening service for all ages",
            minAge = 1,
            maxAge = 12,
            startTime = "2025-01-12T17:30:00",
            endTime = "2025-01-12T19:30:00",
            location = "Main Hall",
            maxCapacity = 50,
            currentCapacity = 8,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Lisa Anderson", "John Smith"),
            createdAt = "2025-01-01T00:00:00"
        )
    )
    
    MaterialTheme {
        Surface {
            KidsManagementContent(
                    uiState =
                            KidsManagementUiState(
                                    children = sampleChildren,
                                    services = sampleServices,
                                    isLoading = false,
                                    hasStaffPermissions = false
                            ),
                    connectionStatus = ConnectionStatus.CONNECTED
            )
        }
    }
}

@Preview
@Composable
private fun KidsManagementContentLoadingPreview() {
    MaterialTheme {
        Surface {
            KidsManagementContent(
                    uiState =
                            KidsManagementUiState(
                                    children = emptyList(),
                                    services = emptyList(),
                                    isLoading = true,
                                    hasStaffPermissions = false
                            ),
                    connectionStatus = ConnectionStatus.CONNECTING
            )
        }
    }
}

@Preview
@Composable
private fun KidsManagementContentEmptyPreview() {
    MaterialTheme {
        Surface {
            KidsManagementContent(
                    uiState =
                            KidsManagementUiState(
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
