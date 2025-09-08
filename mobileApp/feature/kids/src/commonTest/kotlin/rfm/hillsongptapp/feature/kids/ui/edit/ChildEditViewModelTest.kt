package rfm.hillsongptapp.feature.kids.ui.edit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChildEditViewModelTest {
    
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var viewModel: ChildEditViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    private val testChild = Child(
        id = "child_123",
        parentId = "parent_123",
        name = "John Doe",
        dateOfBirth = "2015-05-15",
        medicalInfo = "No allergies",
        dietaryRestrictions = "Vegetarian",
        emergencyContact = EmergencyContact(
            name = "Jane Doe",
            phoneNumber = "(555) 123-4567",
            relationship = "Mother"
        ),
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        viewModel = ChildEditViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be empty`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertNull(initialState.originalChild)
        assertEquals("", initialState.childName)
        assertEquals("", initialState.dateOfBirth)
        assertEquals("", initialState.medicalInfo)
        assertEquals("", initialState.dietaryRestrictions)
        assertEquals("", initialState.emergencyContactName)
        assertEquals("", initialState.emergencyContactPhone)
        assertEquals("", initialState.emergencyContactRelationship)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isSaving)
        assertFalse(initialState.isUpdateSuccessful)
        assertNull(initialState.error)
        assertFalse(initialState.showDatePicker)
        assertFalse(initialState.showDiscardChangesDialog)
        assertFalse(initialState.showSuccessDialog)
    }
    
    @Test
    fun `initializeWithChild should populate form with child data`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        val state = viewModel.uiState.first()
        assertEquals(testChild, state.originalChild)
        assertEquals("John Doe", state.childName)
        assertEquals("2015-05-15", state.dateOfBirth)
        assertEquals("No allergies", state.medicalInfo)
        assertEquals("Vegetarian", state.dietaryRestrictions)
        assertEquals("Jane Doe", state.emergencyContactName)
        assertEquals("(555) 123-4567", state.emergencyContactPhone)
        assertEquals("Mother", state.emergencyContactRelationship)
    }
    
    @Test
    fun `updateChildName should update name and validate in real-time`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateChildName("Jane Smith")
        
        val state = viewModel.uiState.first()
        assertEquals("Jane Smith", state.childName)
        assertNull(state.nameError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateChildName with invalid name should show error immediately`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateChildName("J") // Too short
        
        val state = viewModel.uiState.first()
        assertEquals("J", state.childName)
        assertEquals("Child name must be at least 2 characters", state.nameError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateChildName with empty name should show error immediately`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateChildName("")
        
        val state = viewModel.uiState.first()
        assertEquals("", state.childName)
        assertEquals("Child name is required", state.nameError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateDateOfBirth should update date and validate in real-time`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateDateOfBirth("2016-06-20")
        
        val state = viewModel.uiState.first()
        assertEquals("2016-06-20", state.dateOfBirth)
        assertNull(state.dateOfBirthError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateDateOfBirth with invalid format should show error immediately`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateDateOfBirth("20/06/2016") // Wrong format
        
        val state = viewModel.uiState.first()
        assertEquals("20/06/2016", state.dateOfBirth)
        assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateMedicalInfo should update field and track changes`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateMedicalInfo("Peanut allergy")
        
        val state = viewModel.uiState.first()
        assertEquals("Peanut allergy", state.medicalInfo)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateDietaryRestrictions should update field and track changes`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateDietaryRestrictions("Vegan")
        
        val state = viewModel.uiState.first()
        assertEquals("Vegan", state.dietaryRestrictions)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateEmergencyContactName should update and validate in real-time`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateEmergencyContactName("John Smith")
        
        val state = viewModel.uiState.first()
        assertEquals("John Smith", state.emergencyContactName)
        assertNull(state.emergencyContactNameError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateEmergencyContactPhone should update and validate in real-time`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateEmergencyContactPhone("(555) 987-6543")
        
        val state = viewModel.uiState.first()
        assertEquals("(555) 987-6543", state.emergencyContactPhone)
        assertNull(state.emergencyContactPhoneError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `updateEmergencyContactRelationship should update and validate in real-time`() = runTest {
        viewModel.initializeWithChild(testChild)
        viewModel.updateEmergencyContactRelationship("Father")
        
        val state = viewModel.uiState.first()
        assertEquals("Father", state.emergencyContactRelationship)
        assertNull(state.emergencyContactRelationshipError)
        assertTrue(state.hasChanges)
    }
    
    @Test
    fun `showDatePicker should update state`() = runTest {
        viewModel.showDatePicker()
        
        val state = viewModel.uiState.first()
        assertTrue(state.showDatePicker)
    }
    
    @Test
    fun `hideDatePicker should update state`() = runTest {
        viewModel.showDatePicker()
        viewModel.hideDatePicker()
        
        val state = viewModel.uiState.first()
        assertFalse(state.showDatePicker)
    }
    
    @Test
    fun `showDiscardChangesDialog should update state`() = runTest {
        viewModel.showDiscardChangesDialog()
        
        val state = viewModel.uiState.first()
        assertTrue(state.showDiscardChangesDialog)
    }
    
    @Test
    fun `hideDiscardChangesDialog should update state`() = runTest {
        viewModel.showDiscardChangesDialog()
        viewModel.hideDiscardChangesDialog()
        
        val state = viewModel.uiState.first()
        assertFalse(state.showDiscardChangesDialog)
    }
    
    @Test
    fun `showSuccessDialog should update state`() = runTest {
        viewModel.showSuccessDialog()
        
        val state = viewModel.uiState.first()
        assertTrue(state.showSuccessDialog)
    }
    
    @Test
    fun `hideSuccessDialog should update state`() = runTest {
        viewModel.showSuccessDialog()
        viewModel.hideSuccessDialog()
        
        val state = viewModel.uiState.first()
        assertFalse(state.showSuccessDialog)
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        viewModel.initializeWithChild(testChild)
        // Trigger an error by trying to save with invalid data
        viewModel.updateChildName("")
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Clear error
        viewModel.clearError()
        
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
    
    @Test
    fun `resetForm should restore original child data`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make changes
        viewModel.updateChildName("Changed Name")
        viewModel.updateMedicalInfo("Changed Medical Info")
        
        // Verify changes were made
        var state = viewModel.uiState.first()
        assertEquals("Changed Name", state.childName)
        assertEquals("Changed Medical Info", state.medicalInfo)
        assertTrue(state.hasChanges)
        
        // Reset form
        viewModel.resetForm()
        
        // Verify form was reset to original data
        state = viewModel.uiState.first()
        assertEquals("John Doe", state.childName)
        assertEquals("No allergies", state.medicalInfo)
        assertFalse(state.hasChanges)
    }
    
    @Test
    fun `saveChildInformation should succeed with valid data and optimistic updates`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make valid changes
        viewModel.updateChildName("Jane Smith")
        viewModel.updateMedicalInfo("Peanut allergy")
        
        // Mock successful update
        mockRepository.shouldSucceed = true
        
        viewModel.saveChildInformation()
        
        // Check immediate optimistic update
        var state = viewModel.uiState.first()
        assertTrue(state.isSaving)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check final state
        state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertTrue(state.isUpdateSuccessful)
        assertTrue(state.showSuccessDialog)
        assertNull(state.error)
        
        // Verify optimistic update was applied
        assertEquals("Jane Smith", state.originalChild?.name)
        assertEquals("Peanut allergy", state.originalChild?.medicalInfo)
    }
    
    @Test
    fun `saveChildInformation should fail with invalid data`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make invalid changes
        viewModel.updateChildName("") // Invalid
        
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertEquals("Child name is required", state.nameError)
    }
    
    @Test
    fun `saveChildInformation should handle repository failure and revert optimistic update`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make valid changes
        viewModel.updateChildName("Jane Smith")
        
        // Mock repository failure
        mockRepository.shouldSucceed = false
        mockRepository.errorMessage = "Network error"
        
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertEquals("Failed to save changes: Network error", state.error)
        
        // Verify optimistic update was reverted
        assertEquals("John Doe", state.originalChild?.name) // Should be reverted to original
    }
    
    @Test
    fun `saveChildInformation should not proceed without changes`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Don't make any changes
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertFalse(state.hasChanges)
        
        // Repository should not have been called
        assertFalse(mockRepository.updateChildCalled)
    }
    
    @Test
    fun `saveChildInformation should validate all fields before submission`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make changes with multiple validation errors
        viewModel.updateChildName("") // Invalid
        viewModel.updateDateOfBirth("invalid-date") // Invalid
        viewModel.updateEmergencyContactPhone("123") // Invalid
        
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        
        // All validation errors should be shown
        assertEquals("Child name is required", state.nameError)
        assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError)
        assertEquals("Please enter a valid phone number", state.emergencyContactPhoneError)
    }
    
    @Test
    fun `saveChildInformation should handle exception and revert optimistic update`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Make valid changes
        viewModel.updateChildName("Jane Smith")
        
        // Mock repository exception
        mockRepository.shouldThrowException = true
        
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertTrue(state.error?.contains("Failed to save changes") == true)
        
        // Verify optimistic update was reverted
        assertEquals("John Doe", state.originalChild?.name) // Should be reverted to original
    }
    
    @Test
    fun `validation should work correctly for all fields`() = runTest {
        viewModel.initializeWithChild(testChild)
        
        // Test child name validation
        viewModel.updateChildName("A") // Too short
        var state = viewModel.uiState.first()
        assertEquals("Child name must be at least 2 characters", state.nameError)
        
        viewModel.updateChildName("A".repeat(51)) // Too long
        state = viewModel.uiState.first()
        assertEquals("Child name must be less than 50 characters", state.nameError)
        
        viewModel.updateChildName("John123") // Invalid characters
        state = viewModel.uiState.first()
        assertEquals("Child name can only contain letters, spaces, hyphens, and apostrophes", state.nameError)
        
        // Test date validation
        viewModel.updateDateOfBirth("2025-13-32") // Invalid date
        state = viewModel.uiState.first()
        assertEquals("Please enter a valid date", state.dateOfBirthError)
        
        viewModel.updateDateOfBirth("1800-01-01") // Too old
        state = viewModel.uiState.first()
        assertEquals("Child must be between 0 and 18 years old", state.dateOfBirthError)
        
        // Test phone validation
        viewModel.updateEmergencyContactPhone("123") // Too short
        state = viewModel.uiState.first()
        assertEquals("Please enter a valid phone number", state.emergencyContactPhoneError)
        
        // Test valid inputs clear errors
        viewModel.updateChildName("John Doe")
        viewModel.updateDateOfBirth("2015-05-15")
        viewModel.updateEmergencyContactPhone("(555) 123-4567")
        
        state = viewModel.uiState.first()
        assertNull(state.nameError)
        assertNull(state.dateOfBirthError)
        assertNull(state.emergencyContactPhoneError)
    }
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    var shouldSucceed = true
    var shouldThrowException = false
    var errorMessage = "Test error"
    var updateChildCalled = false
    
    override suspend fun updateChild(child: Child): Result<Child> {
        updateChildCalled = true
        
        if (shouldThrowException) {
            throw Exception("Test exception")
        }
        
        return if (shouldSucceed) {
            Result.success(child.copy(updatedAt = System.currentTimeMillis().toString()))
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Other methods not needed for edit tests
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = 
        Result.success(emptyList())
    override suspend fun registerChild(child: Child): Result<Child> = 
        Result.success(child.copy(id = "test_child_id"))
    override suspend fun deleteChild(childId: String): Result<Unit> = 
        Result.success(Unit)
    override suspend fun getChildById(childId: String): Result<Child> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getAvailableServices(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = 
        Result.success(emptyList())
    override suspend fun getServicesForAge(age: Int): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = 
        Result.success(emptyList())
    override suspend fun getServiceById(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.KidsService> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getServicesAcceptingCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = 
        Result.success(emptyList())
    override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
        Result.success(emptyList())
    override suspend fun getCurrentCheckIns(serviceId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
        Result.success(emptyList())
    override suspend fun getAllCurrentCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = 
        Result.success(emptyList())
    override suspend fun getCheckInRecord(recordId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.ServiceReport> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {}
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.KidsService) -> Unit) {}
    override suspend fun unsubscribeFromUpdates() {}
}