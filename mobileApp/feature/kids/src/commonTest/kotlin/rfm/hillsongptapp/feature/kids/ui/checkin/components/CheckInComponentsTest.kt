package rfm.hillsongptapp.feature.kids.ui.checkin.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.usecase.EligibleServiceInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for check-in components
 * Tests individual component behavior and interactions
 */
@OptIn(ExperimentalTestApi::class)
class CheckInComponentsTest {
    
    @Test
    fun `EligibleServiceCard should display service information correctly`() = runComposeUiTest {
        // Given
        val service = createTestService(
            name = "Elementary Service",
            description = "Service for elementary age children",
            location = "Room 101",
            minAge = 5,
            maxAge = 10,
            startTime = "09:00:00",
            endTime = "10:30:00",
            currentCapacity = 8,
            maxCapacity = 20
        )
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 12,
            isRecommended = true
        )
        
        // When
        setContent {
            EligibleServiceCard(
                serviceInfo = serviceInfo,
                isSelected = false,
                onSelect = {}
            )
        }
        
        // Then
        onNodeWithText("Elementary Service").assertIsDisplayed()
        onNodeWithText("Service for elementary age children").assertIsDisplayed()
        onNodeWithText("Age Range: 5-10 years").assertIsDisplayed()
        onNodeWithText("Location: Room 101").assertIsDisplayed()
        onNodeWithText("Time: 09:00:00 - 10:30:00").assertIsDisplayed()
        onNodeWithText("12 spots left").assertIsDisplayed()
        onNodeWithText("8 / 20 children").assertIsDisplayed()
        onNodeWithText("Recommended").assertIsDisplayed()
    }
    
    @Test
    fun `EligibleServiceCard should show warning for limited availability`() = runComposeUiTest {
        // Given
        val service = createTestService(
            currentCapacity = 18,
            maxCapacity = 20
        )
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 2,
            isRecommended = false
        )
        
        // When
        setContent {
            EligibleServiceCard(
                serviceInfo = serviceInfo,
                isSelected = false,
                onSelect = {}
            )
        }
        
        // Then
        onNodeWithText("2 spots left").assertIsDisplayed()
        onNodeWithText("18 / 20 children").assertIsDisplayed()
        // Should not show recommended badge
        onNodeWithText("Recommended").assertDoesNotExist()
    }
    
    @Test
    fun `EligibleServiceCard should call onSelect when clicked`() = runComposeUiTest {
        // Given
        var selectCalled = false
        val service = createTestService()
        val serviceInfo = EligibleServiceInfo(
            service = service,
            availableSpots = 10,
            isRecommended = true
        )
        
        // When
        setContent {
            EligibleServiceCard(
                serviceInfo = serviceInfo,
                isSelected = false,
                onSelect = { selectCalled = true }
            )
        }
        
        onNodeWithText("Test Service").performClick()
        
        // Then
        assertTrue(selectCalled)
    }
    
    @Test
    fun `CheckInConfirmationDialog should display child and service information`() = runComposeUiTest {
        // Given
        val child = createTestChild(name = "Johnny Smith")
        val service = createTestService(
            name = "Elementary Service",
            description = "Service for elementary children",
            location = "Room 101",
            startTime = "09:00:00",
            endTime = "10:30:00",
            currentCapacity = 8,
            maxCapacity = 20
        )
        
        // When
        setContent {
            CheckInConfirmationDialog(
                child = child,
                service = service,
                onConfirm = {},
                onDismiss = {},
                isLoading = false
            )
        }
        
        // Then
        onNodeWithText("Confirm Check-In").assertIsDisplayed()
        onNodeWithText("Please confirm that you want to check in:").assertIsDisplayed()
        onNodeWithText("Johnny Smith").assertIsDisplayed()
        onNodeWithText("Age: 7 years").assertIsDisplayed()
        onNodeWithText("Elementary Service").assertIsDisplayed()
        onNodeWithText("Service for elementary children").assertIsDisplayed()
        onNodeWithText("Room 101").assertIsDisplayed()
        onNodeWithText("09:00:00 - 10:30:00").assertIsDisplayed()
        onNodeWithText("12 spots remaining (8/20)").assertIsDisplayed()
    }
    
    @Test
    fun `CheckInConfirmationDialog should call onConfirm when confirm button clicked`() = runComposeUiTest {
        // Given
        var confirmCalled = false
        var notesReceived: String? = null
        val child = createTestChild()
        val service = createTestService()
        
        // When
        setContent {
            CheckInConfirmationDialog(
                child = child,
                service = service,
                onConfirm = { notes ->
                    confirmCalled = true
                    notesReceived = notes
                },
                onDismiss = {},
                isLoading = false
            )
        }
        
        onNodeWithText("Confirm Check-In").performClick()
        
        // Then
        assertTrue(confirmCalled)
        assertEquals(null, notesReceived) // No notes entered
    }
    
    @Test
    fun `CheckInConfirmationDialog should call onDismiss when cancel button clicked`() = runComposeUiTest {
        // Given
        var dismissCalled = false
        val child = createTestChild()
        val service = createTestService()
        
        // When
        setContent {
            CheckInConfirmationDialog(
                child = child,
                service = service,
                onConfirm = {},
                onDismiss = { dismissCalled = true },
                isLoading = false
            )
        }
        
        onNodeWithText("Cancel").performClick()
        
        // Then
        assertTrue(dismissCalled)
    }
    
    @Test
    fun `CheckInConfirmationDialog should disable buttons when loading`() = runComposeUiTest {
        // Given
        val child = createTestChild()
        val service = createTestService()
        
        // When
        setContent {
            CheckInConfirmationDialog(
                child = child,
                service = service,
                onConfirm = {},
                onDismiss = {},
                isLoading = true
            )
        }
        
        // Then
        onNodeWithText("Checking In...").assertIsDisplayed()
        onNodeWithText("Cancel").assertIsNotEnabled()
    }
    
    @Test
    fun `CheckInErrorDialog should display error message`() = runComposeUiTest {
        // Given
        val errorMessage = "This service is at full capacity. Please try another service."
        
        // When
        setContent {
            CheckInErrorDialog(
                error = errorMessage,
                onDismiss = {},
                onRetry = {}
            )
        }
        
        // Then
        onNodeWithText("Check-In Failed").assertIsDisplayed()
        onNodeWithText(errorMessage).assertIsDisplayed()
        onNodeWithText("OK").assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
    }
    
    @Test
    fun `CheckInErrorDialog should call onDismiss when OK button clicked`() = runComposeUiTest {
        // Given
        var dismissCalled = false
        
        // When
        setContent {
            CheckInErrorDialog(
                error = "Test error",
                onDismiss = { dismissCalled = true },
                onRetry = {}
            )
        }
        
        onNodeWithText("OK").performClick()
        
        // Then
        assertTrue(dismissCalled)
    }
    
    @Test
    fun `CheckInErrorDialog should call onRetry when retry button clicked`() = runComposeUiTest {
        // Given
        var retryCalled = false
        
        // When
        setContent {
            CheckInErrorDialog(
                error = "Test error",
                onDismiss = {},
                onRetry = { retryCalled = true }
            )
        }
        
        onNodeWithText("Retry").performClick()
        
        // Then
        assertTrue(retryCalled)
    }
    
    @Test
    fun `CheckInErrorDialog should not show retry button when onRetry is null`() = runComposeUiTest {
        // Given
        // When
        setContent {
            CheckInErrorDialog(
                error = "Test error",
                onDismiss = {},
                onRetry = null
            )
        }
        
        // Then
        onNodeWithText("OK").assertIsDisplayed()
        onNodeWithText("Retry").assertDoesNotExist()
    }
    
    // Helper functions
    
    private fun createTestChild(
        id: String = "child_1",
        name: String = "Test Child",
        status: CheckInStatus = CheckInStatus.CHECKED_OUT
    ) = Child(
        id = id,
        parentId = "parent_1",
        name = name,
        dateOfBirth = "2018-01-01", // 7 years old
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "+1234567890",
            relationship = "Parent"
        ),
        status = status,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private fun createTestService(
        id: String = "service_1",
        name: String = "Test Service",
        description: String = "Test Description",
        minAge: Int = 3,
        maxAge: Int = 12,
        startTime: String = "09:00:00",
        endTime: String = "10:30:00",
        location: String = "Room 101",
        maxCapacity: Int = 20,
        currentCapacity: Int = 10
    ) = KidsService(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff_1"),
        createdAt = "2025-01-01T00:00:00Z"
    )
}