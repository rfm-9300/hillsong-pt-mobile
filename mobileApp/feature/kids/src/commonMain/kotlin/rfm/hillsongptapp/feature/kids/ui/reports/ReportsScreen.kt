package rfm.hillsongptapp.feature.kids.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.feature.kids.ui.reports.components.AttendanceReportCard
import rfm.hillsongptapp.feature.kids.ui.reports.components.DateRangeSelector
import rfm.hillsongptapp.feature.kids.ui.reports.components.ServiceReportCard
import rfm.hillsongptapp.feature.kids.ui.reports.components.ReportFilters

/**
 * Screen for church staff to view attendance reports and service management
 * Provides overview of service attendance, capacity management, and reporting features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Reports") },
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
                        onClick = { viewModel.refreshData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    
                    if (uiState.attendanceReport != null) {
                        IconButton(
                            onClick = { viewModel.exportReport() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Export Report"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Date Range Selector
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Date Range",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Report Period",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DateRangeSelector(
                            startDate = uiState.selectedStartDate,
                            endDate = uiState.selectedEndDate,
                            onDateRangeChanged = { startDate, endDate ->
                                viewModel.updateDateRange(startDate, endDate)
                            }
                        )
                    }
                }
            }
            
            // Report Filters
            item {
                ReportFilters(
                    availableServices = uiState.availableServices,
                    selectedServices = uiState.selectedServices,
                    onServiceSelectionChanged = { services ->
                        viewModel.updateServiceFilter(services)
                    }
                )
            }
            
            // Loading State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error State
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Attendance Report Summary
            uiState.attendanceReport?.let { report ->
                item {
                    AttendanceReportCard(
                        report = report,
                        onExportClick = { viewModel.exportReport() }
                    )
                }
            }
            
            // Service Reports Section
            if (uiState.serviceReports.isNotEmpty()) {
                item {
                    Text(
                        text = "Current Service Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(
                    items = uiState.serviceReports,
                    key = { it.serviceId }
                ) { serviceReport ->
                    ServiceReportCard(
                        report = serviceReport,
                        onViewDetailsClick = { 
                            viewModel.selectService(serviceReport.serviceId)
                        }
                    )
                }
            }
            
            // Empty State
            if (!uiState.isLoading && uiState.serviceReports.isEmpty() && uiState.attendanceReport == null && uiState.error == null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No data available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a date range to view reports",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}/**
 * 
Pure UI content for Reports that doesn't depend on ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsContent(
    uiState: ReportsUiState,
    onNavigateBack: () -> Unit = {},
    onLoadInitialData: () -> Unit = {},
    onRefreshData: () -> Unit = {},
    onUpdateDateRange: (String, String) -> Unit = { _, _ -> },
    onUpdateServiceFilter: (Set<String>) -> Unit = {},
    onExportReport: () -> Unit = {},
    onSelectService: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports",
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
                    IconButton(onClick = onRefreshData) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Selection
            item {
                Card {
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
                                contentDescription = "Date Range",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Report Period",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DateRangeSelector(
                            startDate = uiState.selectedStartDate,
                            endDate = uiState.selectedEndDate,
                            onDateRangeChanged = onUpdateDateRange
                        )
                    }
                }
            }
            
            // Report Filters
            item {
                ReportFilters(
                    availableServices = uiState.availableServices,
                    selectedServices = uiState.selectedServices,
                    onServiceSelectionChanged = onUpdateServiceFilter
                )
            }
            
            // Loading State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error State
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $error",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Attendance Report Summary
            uiState.attendanceReport?.let { report ->
                item {
                    AttendanceReportCard(
                        report = report,
                        onExportClick = onExportReport
                    )
                }
            }
            
            // Service Reports Section
            if (uiState.serviceReports.isNotEmpty()) {
                item {
                    Text(
                        text = "Current Service Status",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(
                    items = uiState.serviceReports,
                    key = { it.serviceId }
                ) { serviceReport ->
                    ServiceReportCard(
                        report = serviceReport,
                        onViewDetailsClick = { 
                            onSelectService(serviceReport.serviceId)
                        }
                    )
                }
            }
            
            // Empty State
            if (!uiState.isLoading && uiState.serviceReports.isEmpty() && uiState.attendanceReport == null && uiState.error == null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No data available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a date range to view reports",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Preview

@Preview
@Composable
private fun ReportsContentPreview() {
    MaterialTheme {
        Surface {
            ReportsContent(
                uiState = ReportsUiState(
                    selectedStartDate = "2024-01-01",
                    selectedEndDate = "2024-01-31",
                    availableServices = emptyList(),
                    selectedServices = emptySet(),
                    serviceReports = emptyList(),
                    attendanceReport = null,
                    isLoading = false,
                    error = null
                )
            )
        }
    }
}