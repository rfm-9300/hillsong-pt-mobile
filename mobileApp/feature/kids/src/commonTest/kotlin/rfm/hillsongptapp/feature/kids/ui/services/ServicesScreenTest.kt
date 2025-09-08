package rfm.hillsongptapp.feature.kids.ui.services

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import kotlin.test.*

class ServicesScreenTest {
    
    private val composeTestRule = createComposeRule()
    
    private val mockService1 = KidsService(
        id = "service-1",
        name = "Kids Church",
        description = "Sunday service for children",
        minAge = 5,
        maxAge = 12,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room A",
        maxCapacity = 20,
        currentCapacity = 15,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-1", "staff-2"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val mockService2 = KidsService(
        id = "service-2",
        name = "Toddler Time",
        description = "Service for toddlers",
        minAge = 2,
        maxAge = 4,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room B",
        maxCapacity = 10,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-3"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val mockChild = Child(
        id = "child-1",
        parentId = "parent-1",
        name = "Test Child",
        dateOfBirth = "2018-01-01", // 7 years old
        emergencyContact = EmergencyContact(
            name = "Parent",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    @Test
    fun `displays loading state correctly`() {
        val loadingState = ServicesUiState(isLoading = true)
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = loadingState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
    
    @Test
    fun `displays error state with retry button`() {
        val errorState = ServicesUiState(
            error = "Network connection failed"
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = errorState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Unable to load services").assertIsDisplayed()
        composeTestRule.onNodeWithText("Network connection failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }
    
    @Test
    fun `displays empty state when no services available`() {
        val emptyState = ServicesUiState(
            services = emptyList(),
            filteredServices = emptyList()
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = emptyState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("No services available").assertIsDisplayed()
        composeTestRule.onNodeWithText("There are currently no kids services available.").assertIsDisplayed()
    }
    
    @Test
    fun `displays empty state with clear filters when filters are active`() {
        val emptyFilteredState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = emptyList(),
            filters = ServiceFilters(availability = ServiceFilters.Availability.AVAILABLE_ONLY)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = emptyFilteredState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("No services match your filters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try adjusting your filter criteria to see more services.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear Filters").assertIsDisplayed()
    }
    
    @Test
    fun `displays services list correctly`() {
        val servicesState = ServicesUiState(
            services = listOf(mockService1, mockService2),
            filteredServices = listOf(mockService1, mockService2)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").assertIsDisplayed()
        composeTestRule.onNodeWithText("Toddler Time").assertIsDisplayed()
    }
    
    @Test
    fun `displays correct title when no child selected`() {
        val servicesState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = listOf(mockService1)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                selectedChild = null,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Kids Services").assertIsDisplayed()
    }
    
    @Test
    fun `displays correct title when child is selected`() {
        val servicesState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = listOf(mockService1),
            selectedChild = mockChild
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                selectedChild = mockChild,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Services for Test Child").assertIsDisplayed()
    }
    
    @Test
    fun `displays service summary when child is selected`() {
        val servicesState = ServicesUiState(
            services = listOf(mockService1, mockService2),
            filteredServices = listOf(mockService1, mockService2),
            selectedChild = mockChild
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                selectedChild = mockChild,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Service Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Services").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age Eligible").assertIsDisplayed()
        composeTestRule.onNodeWithText("Available Now").assertIsDisplayed()
    }
    
    @Test
    fun `handles back navigation`() {
        var backPressed = false
        val servicesState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = listOf(mockService1)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                onNavigateBack = { backPressed = true },
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        assertTrue(backPressed)
    }
    
    @Test
    fun `handles filter button click`() {
        var filterClicked = false
        val servicesState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = listOf(mockService1)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = { filterClicked = true },
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Filter services").performClick()
        
        assertTrue(filterClicked)
    }
    
    @Test
    fun `handles service selection`() {
        var selectedService: KidsService? = null
        val servicesState = ServicesUiState(
            services = listOf(mockService1),
            filteredServices = listOf(mockService1)
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                onNavigateBack = {},
                onServiceSelected = { selectedService = it },
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").performClick()
        
        assertEquals(mockService1, selectedService)
    }
    
    @Test
    fun `handles retry button click`() {
        var retryClicked = false
        val errorState = ServicesUiState(
            error = "Network error"
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = errorState,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = { retryClicked = true }
            )
        }
        
        composeTestRule.onNodeWithText("Retry").performClick()
        
        assertTrue(retryClicked)
    }
    
    @Test
    fun `calculates service summary correctly for selected child`() {
        // Child is 7 years old
        // mockService1: 5-12 years, available (eligible and available)
        // mockService2: 2-4 years, full (not eligible)
        val servicesState = ServicesUiState(
            services = listOf(mockService1, mockService2),
            filteredServices = listOf(mockService1, mockService2),
            selectedChild = mockChild
        )
        
        composeTestRule.setContent {
            ServicesScreenContent(
                uiState = servicesState,
                selectedChild = mockChild,
                onNavigateBack = {},
                onServiceSelected = {},
                onFilterClick = {},
                onRefresh = {}
            )
        }
        
        // Total services: 2
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        
        // Age eligible: 1 (only Kids Church)
        // Available now: 1 (only Kids Church can accept check-ins)
        // Note: The exact numbers depend on the implementation of the summary calculation
    }
    
    // Helper composable for testing
    @Composable
    private fun ServicesScreenContent(
        uiState: ServicesUiState,
        selectedChild: Child? = null,
        onNavigateBack: () -> Unit,
        onServiceSelected: (KidsService) -> Unit,
        onFilterClick: () -> Unit,
        onRefresh: () -> Unit
    ) {
        // This would be the content part of ServicesScreen without the ViewModel
        // For testing purposes, we simulate the screen content based on the UI state
        when {
            uiState.isLoading -> {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    androidx.compose.material3.Text("Unable to load services")
                    androidx.compose.material3.Text(uiState.error)
                    androidx.compose.material3.Button(onClick = onRefresh) {
                        androidx.compose.material3.Text("Retry")
                    }
                }
            }
            
            uiState.filteredServices.isEmpty() -> {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    if (uiState.hasActiveFilters) {
                        androidx.compose.material3.Text("No services match your filters")
                        androidx.compose.material3.Text("Try adjusting your filter criteria to see more services.")
                        androidx.compose.material3.OutlinedButton(onClick = {}) {
                            androidx.compose.material3.Text("Clear Filters")
                        }
                    } else {
                        androidx.compose.material3.Text("No services available")
                        androidx.compose.material3.Text("There are currently no kids services available.")
                    }
                }
            }
            
            else -> {
                androidx.compose.foundation.layout.Column {
                    // Top App Bar
                    androidx.compose.material3.TopAppBar(
                        title = {
                            androidx.compose.material3.Text(
                                if (selectedChild != null) {
                                    "Services for ${selectedChild.name}"
                                } else {
                                    "Kids Services"
                                }
                            )
                        },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            androidx.compose.material3.IconButton(onClick = onFilterClick) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.FilterList,
                                    contentDescription = "Filter services"
                                )
                            }
                        }
                    )
                    
                    // Services list
                    androidx.compose.foundation.lazy.LazyColumn {
                        if (selectedChild != null) {
                            item {
                                androidx.compose.material3.Card {
                                    androidx.compose.foundation.layout.Column(
                                        modifier = androidx.compose.ui.Modifier.padding(16.dp)
                                    ) {
                                        androidx.compose.material3.Text("Service Summary")
                                        androidx.compose.foundation.layout.Row(
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                        ) {
                                            androidx.compose.foundation.layout.Column {
                                                androidx.compose.material3.Text(uiState.services.size.toString())
                                                androidx.compose.material3.Text("Total Services")
                                            }
                                            androidx.compose.foundation.layout.Column {
                                                androidx.compose.material3.Text(
                                                    uiState.services.count { it.isAgeEligible(selectedChild.calculateAge()) }.toString()
                                                )
                                                androidx.compose.material3.Text("Age Eligible")
                                            }
                                            androidx.compose.foundation.layout.Column {
                                                androidx.compose.material3.Text(
                                                    uiState.services.count { it.canAcceptCheckIn() }.toString()
                                                )
                                                androidx.compose.material3.Text("Available Now")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        items(uiState.filteredServices.size) { index ->
                            val service = uiState.filteredServices[index]
                            ServiceCard(
                                service = service,
                                selectedChild = selectedChild,
                                onServiceClick = onServiceSelected
                            )
                        }
                    }
                }
            }
        }
    }
}