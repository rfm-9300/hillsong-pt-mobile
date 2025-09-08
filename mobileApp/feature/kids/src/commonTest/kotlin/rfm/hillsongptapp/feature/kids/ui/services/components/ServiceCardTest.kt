package rfm.hillsongptapp.feature.kids.ui.services.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import kotlin.test.*

class ServiceCardTest {
    
    private val composeTestRule = createComposeRule()
    
    private val availableService = KidsService(
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
    
    private val fullService = KidsService(
        id = "service-2",
        name = "Toddler Time",
        description = "Service for toddlers",
        minAge = 2,
        maxAge = 4,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room B",
        maxCapacity = 10,
        currentCapacity = 10, // Full
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff-3"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val closedService = KidsService(
        id = "service-3",
        name = "Youth Group",
        description = "Service for teenagers",
        minAge = 13,
        maxAge = 17,
        startTime = "2025-01-01T10:00:00Z",
        endTime = "2025-01-01T11:00:00Z",
        location = "Room C",
        maxCapacity = 25,
        currentCapacity = 5,
        isAcceptingCheckIns = false, // Not accepting
        staffMembers = listOf("staff-4"),
        createdAt = "2025-01-01T09:00:00Z"
    )
    
    private val eligibleChild = Child(
        id = "child-1",
        parentId = "parent-1",
        name = "Eligible Child",
        dateOfBirth = "2018-01-01", // 7 years old - eligible for Kids Church
        emergencyContact = EmergencyContact(
            name = "Parent",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = CheckInStatus.CHECKED_OUT,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z"
    )
    
    private val ineligibleChild = Child(
        id = "child-2",
        parentId = "parent-1",
        name = "Ineligible Child",
        dateOfBirth = "2022-01-01", // 3 years old - not eligible for Kids Church
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
    fun `displays service basic information`() {
        composeTestRule.setContent {
            ServiceCard(
                service = availableService,
                onServiceClick = {}
            )
        }
        
        // Check service name and description
        composeTestRule.onNodeWithText("Kids Church").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunday service for children").assertIsDisplayed()
        
        // Check age range
        composeTestRule.onNodeWithText("Age Range: 5-12 years").assertIsDisplayed()
        
        // Check location
        composeTestRule.onNodeWithText("Location: Room A").assertIsDisplayed()
        
        // Check staff count
        composeTestRule.onNodeWithText("Staff: 2 members").assertIsDisplayed()
    }
    
    @Test
    fun `displays capacity information correctly`() {
        composeTestRule.setContent {
            ServiceCard(
                service = availableService,
                onServiceClick = {}
            )
        }
        
        // Check capacity display
        composeTestRule.onNodeWithText("15/20").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 spots available").assertIsDisplayed()
    }
    
    @Test
    fun `shows available status for available service`() {
        composeTestRule.setContent {
            ServiceCard(
                service = availableService,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Available").assertIsDisplayed()
    }
    
    @Test
    fun `shows full status for service at capacity`() {
        composeTestRule.setContent {
            ServiceCard(
                service = fullService,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Full").assertIsDisplayed()
    }
    
    @Test
    fun `shows closed status for service not accepting check-ins`() {
        composeTestRule.setContent {
            ServiceCard(
                service = closedService,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Closed").assertIsDisplayed()
    }
    
    @Test
    fun `shows eligibility message for ineligible child`() {
        composeTestRule.setContent {
            ServiceCard(
                service = availableService, // Ages 5-12
                selectedChild = ineligibleChild, // 3 years old
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Not Eligible").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Ineligible Child (age 3) is not eligible for this service (5-12 years)"
        ).assertIsDisplayed()
    }
    
    @Test
    fun `does not show eligibility message for eligible child`() {
        composeTestRule.setContent {
            ServiceCard(
                service = availableService, // Ages 5-12
                selectedChild = eligibleChild, // 7 years old
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Available").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Eligible Child (age 7) is not eligible for this service (5-12 years)"
        ).assertDoesNotExist()
    }
    
    @Test
    fun `handles service click for eligible child`() {
        var clickedService: KidsService? = null
        
        composeTestRule.setContent {
            ServiceCard(
                service = availableService,
                selectedChild = eligibleChild,
                onServiceClick = { clickedService = it }
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").performClick()
        
        assertEquals(availableService, clickedService)
    }
    
    @Test
    fun `does not handle click for ineligible child`() {
        var clickedService: KidsService? = null
        
        composeTestRule.setContent {
            ServiceCard(
                service = availableService,
                selectedChild = ineligibleChild,
                onServiceClick = { clickedService = it }
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").performClick()
        
        assertNull(clickedService)
    }
    
    @Test
    fun `does not handle click for full service`() {
        var clickedService: KidsService? = null
        
        composeTestRule.setContent {
            ServiceCard(
                service = fullService,
                selectedChild = eligibleChild,
                onServiceClick = { clickedService = it }
            )
        }
        
        composeTestRule.onNodeWithText("Toddler Time").performClick()
        
        assertNull(clickedService)
    }
    
    @Test
    fun `displays single staff member correctly`() {
        composeTestRule.setContent {
            ServiceCard(
                service = fullService, // Has 1 staff member
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Staff: 1 member").assertIsDisplayed()
    }
    
    @Test
    fun `displays service without description`() {
        val serviceWithoutDescription = availableService.copy(description = "")
        
        composeTestRule.setContent {
            ServiceCard(
                service = serviceWithoutDescription,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sunday service for children").assertDoesNotExist()
    }
    
    @Test
    fun `displays service without staff members`() {
        val serviceWithoutStaff = availableService.copy(staffMembers = emptyList())
        
        composeTestRule.setContent {
            ServiceCard(
                service = serviceWithoutStaff,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Kids Church").assertIsDisplayed()
        composeTestRule.onNodeWithText("Staff:", substring = true).assertDoesNotExist()
    }
    
    @Test
    fun `displays capacity progress correctly for different fill levels`() {
        val almostFullService = availableService.copy(currentCapacity = 18) // 18/20 = 90%
        
        composeTestRule.setContent {
            ServiceCard(
                service = almostFullService,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("18/20").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 spots available").assertIsDisplayed()
    }
    
    @Test
    fun `handles service with single age range`() {
        val singleAgeService = availableService.copy(minAge = 8, maxAge = 8)
        
        composeTestRule.setContent {
            ServiceCard(
                service = singleAgeService,
                onServiceClick = {}
            )
        }
        
        composeTestRule.onNodeWithText("Age Range: 8 years").assertIsDisplayed()
    }
}