package rfm.hillsongptapp.feature.kids.ui.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.ui.services.components.ServiceCard
import rfm.hillsongptapp.feature.kids.ui.services.components.ServiceFilterDialog

/**
 * Screen displaying available kids services with filtering and management capabilities
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onNavigateBack: () -> Unit,
    onServiceSelected: (KidsService) -> Unit = {},
    selectedChildId: String? = null,
    selectedChild: Child? = null,
    modifier: Modifier = Modifier,
    viewModel: ServicesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }


    
    LaunchedEffect(selectedChild) {
        if (selectedChild != null) {
            viewModel.setSelectedChild(selectedChild)
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (selectedChild != null) {
                        "Services for ${selectedChild.name}"
                    } else {
                        "Kids Services"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Filter services"
                    )
                }
            }
        )
        
        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadServices() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.filteredServices.isEmpty() -> {
                    EmptyServicesContent(
                        hasFilters = uiState.hasActiveFilters,
                        onClearFilters = { viewModel.clearFilters() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    ServicesContent(
                        services = uiState.filteredServices,
                        selectedChild = selectedChild,
                        onServiceSelected = onServiceSelected,
                        onRefresh = { viewModel.refreshServices() },
                        isRefreshing = uiState.isRefreshing,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        ServiceFilterDialog(
            currentFilters = uiState.filters,
            onFiltersChanged = { filters ->
                viewModel.updateFilters(filters)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar for errors
            // In a real implementation, you'd use SnackbarHost
        }
    }
}

/**
 * Main content showing the list of services
 */
@Composable
private fun ServicesContent(
    services: List<KidsService>,
    selectedChild: Child?,
    onServiceSelected: (KidsService) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary header if child is selected
        if (selectedChild != null) {
            item {
                ServicesSummaryCard(
                    child = selectedChild,
                    totalServices = services.size,
                    eligibleServices = services.count { it.isAgeEligible(selectedChild.calculateAge()) },
                    availableServices = services.count { it.canAcceptCheckIn() }
                )
            }
        }
        
        items(
            items = services,
            key = { it.id }
        ) { service ->
            ServiceCard(
                service = service,
                selectedChild = selectedChild,
                onServiceClick = { onServiceSelected(service) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Summary card showing service statistics for a selected child
 */
@Composable
private fun ServicesSummaryCard(
    child: Child,
    totalServices: Int,
    eligibleServices: Int,
    availableServices: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Service Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Total Services",
                    value = totalServices.toString()
                )
                SummaryItem(
                    label = "Age Eligible",
                    value = eligibleServices.toString()
                )
                SummaryItem(
                    label = "Available Now",
                    value = availableServices.toString()
                )
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
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Error content with retry option
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "Unable to load services",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty state content
 */
@Composable
private fun EmptyServicesContent(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = if (hasFilters) "No services match your filters" else "No services available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasFilters) {
                "Try adjusting your filter criteria to see more services."
            } else {
                "There are currently no kids services available."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (hasFilters) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(onClick = onClearFilters) {
                Text("Clear Filters")
            }
        }
    }
}/**

 * Pure UI content for Services that doesn't depend on ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreenContent(
    uiState: ServicesUiState,
    selectedChild: Child? = null,
    onNavigateBack: () -> Unit = {},
    onServiceSelected: (KidsService) -> Unit = {},
    onSetSelectedChild: (Child) -> Unit = {},
    onLoadServices: () -> Unit = {},
    onRefreshServices: () -> Unit = {},
    onUpdateFilters: (ServiceFilters) -> Unit = {},
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (selectedChild != null) {
                        "Services for ${selectedChild.name}"
                    } else {
                        "Kids Services"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Filter services"
                    )
                }
            }
        )
        
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.error != null -> {
                    ErrorContent(
                        error = uiState.error!!,
                        onRetry = onLoadServices,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.filteredServices.isEmpty() -> {
                    EmptyServicesContent(
                        hasFilters = uiState.hasActiveFilters,
                        onClearFilters = onClearFilters,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    ServicesContent(
                        services = uiState.filteredServices,
                        selectedChild = selectedChild,
                        onServiceSelected = onServiceSelected,
                        onRefresh = onRefreshServices,
                        isRefreshing = uiState.isRefreshing,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        ServiceFilterDialog(
            currentFilters = uiState.filters,
            onFiltersChanged = { filters ->
                onUpdateFilters(filters)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ServicesContentPreview() {
    MaterialTheme {
        Surface {
            ServicesScreenContent(
                uiState = ServicesUiState(
                    services = emptyList(),
                    filteredServices = emptyList(),
                    filters = ServiceFilters(),
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                    // hasActiveFilters is computed property
                )
            )
        }
    }
}