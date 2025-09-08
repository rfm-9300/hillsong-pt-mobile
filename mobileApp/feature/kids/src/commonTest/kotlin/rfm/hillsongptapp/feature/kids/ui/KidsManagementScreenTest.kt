package rfm.hillsongptapp.feature.kids.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

class KidsManagementScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val mockChild = Child(
        id = "child1",
        parentId = "parent1",
        name = "John Doe",
        dateOfBirth = "2018-05-15",
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Jane Doe",
            phoneNumber = "123-456-7890",
            relationship = "Mother"
        ),
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T10:00:00Z",
        updatedAt = "2025-01-01T10:00:00Z"
    )
    
    private val mockService = KidsService(
        id = "service1",
        name = "Sunday School",
        description = "Fun learning for kids",
        minAge = 5,
        maxAge = 12,
        startTime = "09:00:00",
        endTime = "10:30:00",
        location = "Room A",
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("Staff1", "Staff2"),
        createdAt = "2025-01-01T08:00:00Z"
    )
    
    @Test
    fun kidsManagementScreen_showsLoadingState() {
        val loadingState = KidsManagementUiState(isLoading = true)
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = loadingState,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Loading children...")
            .assertIsDisplayed()
        
        composeTestRule
            .onNode(hasTestTag("loading_indicator") or hasContentDescription("Loading"))
            .assertIsDisplayed()
    }
    
    @Test
    fun kidsManagementScreen_showsEmptyState() {
        val emptyState = KidsManagementUiState(
            children = emptyList(),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = emptyState,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("No Children Registered")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Register your first child to get started with kids services")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Register Child")
            .assertIsDisplayed()
    }
    
    @Test
    fun kidsManagementScreen_showsChildrenList() {
        val stateWithChildren = KidsManagementUiState(
            children = listOf(mockChild),
            services = listOf(mockService),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = stateWithChildren,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        // Check if child name is displayed
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        // Check if age is displayed
        composeTestRule
            .onNodeWithText("Age: 7 years")
            .assertIsDisplayed()
        
        // Check if status is displayed
        composeTestRule
            .onNodeWithText("Not in Service")
            .assertIsDisplayed()
        
        // Check if check-in button is displayed for available child
        composeTestRule
            .onNodeWithText("Check In")
            .assertIsDisplayed()
        
        // Check if edit button is displayed
        composeTestRule
            .onNodeWithText("Edit")
            .assertIsDisplayed()
    }
    
    @Test
    fun kidsManagementScreen_showsSummaryCard() {
        val stateWithChildren = KidsManagementUiState(
            children = listOf(
                mockChild,
                mockChild.copy(
                    id = "child2",
                    name = "Jane Doe",
                    status = CheckInStatus.CHECKED_IN
                )
            ),
            services = listOf(mockService),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = stateWithChildren,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        // Check summary statistics
        composeTestRule
            .onNodeWithText("2") // Total children
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("1") // Checked in count
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Total")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Checked In")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Available")
            .assertIsDisplayed()
    }
    
    @Test
    fun kidsManagementScreen_checkInButtonTriggersCallback() {
        var checkInClicked = false
        var clickedChild: Child? = null
        
        val stateWithChildren = KidsManagementUiState(
            children = listOf(mockChild),
            services = listOf(mockService),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = stateWithChildren,
                onRefresh = {},
                onCheckInClick = { child ->
                    checkInClicked = true
                    clickedChild = child
                },
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Check In")
            .performClick()
        
        assert(checkInClicked)
        assert(clickedChild?.id == mockChild.id)
    }
    
    @Test
    fun kidsManagementScreen_editButtonTriggersCallback() {
        var editClicked = false
        var clickedChild: Child? = null
        
        val stateWithChildren = KidsManagementUiState(
            children = listOf(mockChild),
            services = listOf(mockService),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = stateWithChildren,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = { child ->
                    editClicked = true
                    clickedChild = child
                },
                onRegisterClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Edit")
            .performClick()
        
        assert(editClicked)
        assert(clickedChild?.id == mockChild.id)
    }
    
    @Test
    fun kidsManagementScreen_showsCheckedInChild() {
        val checkedInChild = mockChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = mockService.id,
            checkInTime = "2025-01-29T10:00:00Z"
        )
        
        val stateWithCheckedInChild = KidsManagementUiState(
            children = listOf(checkedInChild),
            services = listOf(mockService),
            isLoading = false
        )
        
        composeTestRule.setContent {
            KidsManagementScreenContent(
                uiState = stateWithCheckedInChild,
                onRefresh = {},
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {},
                onRegisterClick = {}
            )
        }
        
        // Check if checked in status is displayed
        composeTestRule
            .onNodeWithText("Checked In")
            .assertIsDisplayed()
        
        // Check if current service is displayed
        composeTestRule
            .onNodeWithText("Currently in: Sunday School")
            .assertIsDisplayed()
        
        // Check if check-out button is displayed
        composeTestRule
            .onNodeWithText("Check Out")
            .assertIsDisplayed()
    }
}

/**
 * Helper composable for testing the content without the full screen structure
 */
@Composable
private fun KidsManagementScreenContent(
    uiState: KidsManagementUiState,
    onRefresh: () -> Unit,
    onCheckInClick: (Child) -> Unit,
    onCheckOutClick: (Child) -> Unit,
    onEditClick: (Child) -> Unit,
    onRegisterClick: () -> Unit
) {
    when {
        uiState.isLoading -> {
            LoadingContent()
        }
        
        !uiState.hasChildren -> {
            EmptyContent(onRegisterClick = onRegisterClick)
        }
        
        else -> {
            ChildrenListContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onCheckInClick = onCheckInClick,
                onCheckOutClick = onCheckOutClick,
                onEditClick = onEditClick,
                onRegisterClick = onRegisterClick
            )
        }
    }
}