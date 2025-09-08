package rfm.hillsongptapp.feature.kids.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

class ChildCardTest {
    
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
    fun childCard_displaysChildInformation() {
        composeTestRule.setContent {
            ChildCard(
                child = mockChild,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Check child name is displayed
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        // Check age is displayed
        composeTestRule
            .onNodeWithText("Age: 7 years")
            .assertIsDisplayed()
        
        // Check status is displayed
        composeTestRule
            .onNodeWithText("Not in Service")
            .assertIsDisplayed()
    }
    
    @Test
    fun childCard_showsCheckInButtonForAvailableChild() {
        composeTestRule.setContent {
            ChildCard(
                child = mockChild.copy(status = CheckInStatus.NOT_IN_SERVICE),
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Check In button should be visible
        composeTestRule
            .onNodeWithText("Check In")
            .assertIsDisplayed()
        
        // Check Out button should not be visible
        composeTestRule
            .onNodeWithText("Check Out")
            .assertDoesNotExist()
    }
    
    @Test
    fun childCard_showsCheckOutButtonForCheckedInChild() {
        val checkedInChild = mockChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = mockService.id,
            checkInTime = "2025-01-29T10:00:00Z"
        )
        
        composeTestRule.setContent {
            ChildCard(
                child = checkedInChild,
                currentService = mockService,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Check Out button should be visible
        composeTestRule
            .onNodeWithText("Check Out")
            .assertIsDisplayed()
        
        // Check In button should not be visible
        composeTestRule
            .onNodeWithText("Check In")
            .assertDoesNotExist()
        
        // Should show current service
        composeTestRule
            .onNodeWithText("Currently in: Sunday School")
            .assertIsDisplayed()
        
        // Should show checked in status
        composeTestRule
            .onNodeWithText("Checked In")
            .assertIsDisplayed()
    }
    
    @Test
    fun childCard_showsEditButtonAlways() {
        composeTestRule.setContent {
            ChildCard(
                child = mockChild,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Edit button should always be visible
        composeTestRule
            .onNodeWithText("Edit")
            .assertIsDisplayed()
    }
    
    @Test
    fun childCard_checkInButtonTriggersCallback() {
        var checkInClicked = false
        var clickedChild: Child? = null
        
        composeTestRule.setContent {
            ChildCard(
                child = mockChild,
                onCheckInClick = { child ->
                    checkInClicked = true
                    clickedChild = child
                },
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Check In")
            .performClick()
        
        assert(checkInClicked)
        assert(clickedChild?.id == mockChild.id)
    }
    
    @Test
    fun childCard_checkOutButtonTriggersCallback() {
        var checkOutClicked = false
        var clickedChild: Child? = null
        
        val checkedInChild = mockChild.copy(status = CheckInStatus.CHECKED_IN)
        
        composeTestRule.setContent {
            ChildCard(
                child = checkedInChild,
                onCheckInClick = {},
                onCheckOutClick = { child ->
                    checkOutClicked = true
                    clickedChild = child
                },
                onEditClick = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Check Out")
            .performClick()
        
        assert(checkOutClicked)
        assert(clickedChild?.id == checkedInChild.id)
    }
    
    @Test
    fun childCard_editButtonTriggersCallback() {
        var editClicked = false
        var clickedChild: Child? = null
        
        composeTestRule.setContent {
            ChildCard(
                child = mockChild,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = { child ->
                    editClicked = true
                    clickedChild = child
                }
            )
        }
        
        composeTestRule
            .onNodeWithText("Edit")
            .performClick()
        
        assert(editClicked)
        assert(clickedChild?.id == mockChild.id)
    }
    
    @Test
    fun childCard_showsCheckedOutStatus() {
        val checkedOutChild = mockChild.copy(
            status = CheckInStatus.CHECKED_OUT,
            checkOutTime = "2025-01-29T11:00:00Z"
        )
        
        composeTestRule.setContent {
            ChildCard(
                child = checkedOutChild,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Should show checked out status
        composeTestRule
            .onNodeWithText("Checked Out")
            .assertIsDisplayed()
        
        // Should show last checkout time
        composeTestRule
            .onNodeWithText("Last checked out: 11:00 AM")
            .assertIsDisplayed()
        
        // Should show check in button (can check in again)
        composeTestRule
            .onNodeWithText("Check In")
            .assertIsDisplayed()
    }
    
    @Test
    fun childCard_showsCheckInTimeForCheckedInChild() {
        val checkedInChild = mockChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = mockService.id,
            checkInTime = "2025-01-29T09:30:00Z"
        )
        
        composeTestRule.setContent {
            ChildCard(
                child = checkedInChild,
                currentService = mockService,
                onCheckInClick = {},
                onCheckOutClick = {},
                onEditClick = {}
            )
        }
        
        // Should show check-in time
        composeTestRule
            .onNodeWithText("Checked in at: 9:30 AM")
            .assertIsDisplayed()
    }
}