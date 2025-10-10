package rfm.hillsongptapp.feature.kids.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import rfm.hillsongptapp.core.designsystem.ui.icons.AppIcons

/**
 * Modern, friendly screen showing today's service status and capacity management
 * Focused on current day operations for church staff
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
        viewModel.loadTodaysServices()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Today's Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getCurrentDateFormatted(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshTodaysData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Today's Overview Card
            if (uiState.serviceReports.isNotEmpty()) {
                item {
                    TodaysOverviewCard(
                        totalServices = uiState.serviceReports.size,
                        totalCapacity = uiState.getTotalCapacity(),
                        totalCheckIns = uiState.getTotalCurrentCheckIns(),
                        utilizationPercent = uiState.getOverallCapacityUtilization(),
                        fullServices = uiState.getFullServices().size
                    )
                }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading today's services...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Error State
            uiState.error?.let { error ->
                item {
                    ErrorCard(error = error)
                }
            }
            
            // Service Reports Section
            if (uiState.serviceReports.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Group,
                            contentDescription = "Services",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Service Status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(
                    items = uiState.serviceReports,
                    key = { it.serviceId }
                ) { serviceReport ->
                    ModernServiceCard(
                        report = serviceReport,
                        onViewDetailsClick = { 
                            viewModel.selectService(serviceReport.serviceId)
                        }
                    )
                }
            }
            
            // Empty State
            if (!uiState.isLoading && uiState.serviceReports.isEmpty() && uiState.error == null) {
                item {
                    EmptyStateCard()
                }
            }
        }
    }
}/**
 * Today's overview card showing key metrics in a friendly, visual way
 */
@Composable
private fun TodaysOverviewCard(
    totalServices: Int,
    totalCapacity: Int,
    totalCheckIns: Int,
    utilizationPercent: Int,
    fullServices: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AppIcons.TendingUP,
                        contentDescription = "Overview",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Today's Overview",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverviewMetric(
                        value = totalServices.toString(),
                        label = "Services",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OverviewMetric(
                        value = "$totalCheckIns/$totalCapacity",
                        label = "Attendance",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    OverviewMetric(
                        value = "$utilizationPercent%",
                        label = "Capacity",
                        color = getUtilizationColor(utilizationPercent)
                    )
                    
                    if (fullServices > 0) {
                        OverviewMetric(
                            value = fullServices.toString(),
                            label = "Full",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual metric display for the overview card
 */
@Composable
private fun OverviewMetric(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

/**
 * Modern service card with enhanced visual design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernServiceCard(
    report: rfm.hillsongptapp.core.data.model.ServiceReport,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onViewDetailsClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with service name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.serviceName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live Status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusBadge(report = report)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Capacity visualization
            CapacityVisualization(
                current = report.currentCheckIns,
                total = report.totalCapacity,
                utilization = report.getCapacityUtilization()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    label = "Available",
                    value = "${report.availableSpots} spots",
                    color = if (report.availableSpots > 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                InfoChip(
                    label = "Staff",
                    value = "${report.staffMembers.size} members",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * Enhanced status badge with better visual design
 */
@Composable
private fun StatusBadge(
    report: rfm.hillsongptapp.core.data.model.ServiceReport,
    modifier: Modifier = Modifier
) {
    val (color, text, bgColor) = when {
        report.isAtCapacity() -> Triple(
            Color.White,
            "FULL",
            MaterialTheme.colorScheme.error
        )
        report.getCapacityUtilization() >= 0.9f -> Triple(
            Color.White,
            "NEARLY FULL",
            Color(0xFFFF9800)
        )
        report.currentCheckIns == 0 -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            "EMPTY",
            MaterialTheme.colorScheme.surfaceVariant
        )
        else -> Triple(
            Color.White,
            "AVAILABLE",
            Color(0xFF4CAF50)
        )
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Enhanced capacity visualization with better UX
 */
@Composable
private fun CapacityVisualization(
    current: Int,
    total: Int,
    utilization: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$current / $total",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = getUtilizationColor((utilization * 100).toInt())
            )
            
            Text(
                text = "${(utilization * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { utilization },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getUtilizationColor((utilization * 100).toInt()),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Info chip for displaying additional service information
 */
@Composable
private fun InfoChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Friendly error card
 */
@Composable
private fun ErrorCard(
    error: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Empty state card with friendly messaging
 */
@Composable
private fun EmptyStateCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = AppIcons.CalendarToday,
                contentDescription = "No services",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No services today",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enjoy your day off! Check back tomorrow for service updates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Get color based on utilization percentage
 */
private fun getUtilizationColor(percent: Int): Color {
    return when {
        percent >= 100 -> Color(0xFFD32F2F) // Red - Full
        percent >= 90 -> Color(0xFFFF9800)  // Orange - Nearly full
        percent >= 70 -> Color(0xFFFFC107)  // Yellow - Getting full
        else -> Color(0xFF4CAF50)           // Green - Available
    }
}

/**
 * Get current date formatted for display
 */
private fun getCurrentDateFormatted(): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return "${today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${today.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${today.dayOfMonth}"
}

// MARK: - Preview

@Preview
@Composable
private fun ReportsScreenPreview() {
    MaterialTheme {
        Surface {
            // Preview showing the main components without ViewModel dependency
            ReportsScreenContent(
                uiState = createSampleUiState(),
                onNavigateBack = { },
                onRefresh = { },
                onSelectService = { }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsScreenContent(
    uiState: ReportsUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelectService: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "Today's Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getCurrentDateFormatted(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (uiState.serviceReports.isNotEmpty()) {
                item {
                    TodaysOverviewCard(
                        totalServices = uiState.serviceReports.size,
                        totalCapacity = uiState.getTotalCapacity(),
                        totalCheckIns = uiState.getTotalCurrentCheckIns(),
                        utilizationPercent = uiState.getOverallCapacityUtilization(),
                        fullServices = uiState.getFullServices().size
                    )
                }
                
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Group,
                            contentDescription = "Services",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Service Status",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                items(
                    items = uiState.serviceReports,
                    key = { it.serviceId }
                ) { serviceReport ->
                    ModernServiceCard(
                        report = serviceReport,
                        onViewDetailsClick = { onSelectService(serviceReport.serviceId) }
                    )
                }
            }
        }
    }
}

private fun createSampleUiState() = ReportsUiState(
    serviceReports = listOf(
        createSampleServiceReport(),
        rfm.hillsongptapp.core.data.model.ServiceReport(
            serviceId = "service-2",
            serviceName = "Toddlers (Ages 2-4)",
            totalCapacity = 25,
            currentCheckIns = 25,
            availableSpots = 0,
            checkedInChildren = emptyList(),
            staffMembers = listOf("Jessica Lee", "Tom Brown"),
            generatedAt = "2025-01-15T10:30:00Z"
        ),
        rfm.hillsongptapp.core.data.model.ServiceReport(
            serviceId = "service-3",
            serviceName = "Youth (Ages 13-17)",
            totalCapacity = 75,
            currentCheckIns = 12,
            availableSpots = 63,
            checkedInChildren = emptyList(),
            staffMembers = listOf("Alex Rodriguez", "Katie Smith", "David Kim"),
            generatedAt = "2025-01-15T10:30:00Z"
        )
    ),
    isLoading = false,
    error = null
)

@Preview
@Composable
private fun TodaysOverviewCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            TodaysOverviewCard(
                totalServices = 3,
                totalCapacity = 150,
                totalCheckIns = 127,
                utilizationPercent = 85,
                fullServices = 1
            )
        }
    }
}

@Preview
@Composable
private fun ModernServiceCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            ModernServiceCard(
                report = createSampleServiceReport(),
                onViewDetailsClick = { }
            )
        }
    }
}

@Preview
@Composable
private fun EmptyStateCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EmptyStateCard()
        }
    }
}

// Sample data for previews
private fun createSampleServiceReport() = rfm.hillsongptapp.core.data.model.ServiceReport(
    serviceId = "service-1",
    serviceName = "Kids Church (Ages 5-12)",
    totalCapacity = 50,
    currentCheckIns = 42,
    availableSpots = 8,
    checkedInChildren = createSampleChildren(),
    staffMembers = listOf("Sarah Johnson", "Mike Chen", "Emma Wilson"),
    generatedAt = "2025-01-15T10:30:00Z"
)

private fun createSampleChildren() = listOf(
    rfm.hillsongptapp.core.data.model.Child(
        id = "child-1",
        parentId = "parent-1",
        name = "Emma Thompson",
        dateOfBirth = "2018-03-15",
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = rfm.hillsongptapp.core.data.model.EmergencyContact(
            name = "Sarah Thompson",
            phoneNumber = "+1-555-0123",
            relationship = "Mother"
        ),
        status = rfm.hillsongptapp.core.data.model.CheckInStatus.CHECKED_IN,
        currentServiceId = "service-1",
        checkInTime = "2025-01-15T09:45:00Z",
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-15T09:45:00Z"
    ),
    rfm.hillsongptapp.core.data.model.Child(
        id = "child-2",
        parentId = "parent-2",
        name = "Lucas Martinez",
        dateOfBirth = "2019-07-22",
        medicalInfo = "Mild peanut allergy",
        dietaryRestrictions = "No nuts",
        emergencyContact = rfm.hillsongptapp.core.data.model.EmergencyContact(
            name = "Maria Martinez",
            phoneNumber = "+1-555-0456",
            relationship = "Mother"
        ),
        status = rfm.hillsongptapp.core.data.model.CheckInStatus.CHECKED_IN,
        currentServiceId = "service-1",
        checkInTime = "2025-01-15T09:50:00Z",
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-15T09:50:00Z"
    )
)

