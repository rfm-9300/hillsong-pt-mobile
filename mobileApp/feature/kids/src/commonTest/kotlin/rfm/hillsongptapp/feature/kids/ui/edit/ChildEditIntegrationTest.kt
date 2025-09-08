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
class ChildEditIntegrationTest {
    
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
    fun `complete child edit flow - successful update`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Verify initial state
        var state = viewModel.uiState.first()
        assertEquals(testChild, state.originalChild)
        assertEquals("John Doe", state.childName)
        assertFalse(state.hasChanges)
        
        // Make changes to multiple fields
        viewModel.updateChildName("Jane Smith")
        viewModel.updateMedicalInfo("Peanut allergy")
        viewModel.updateEmergencyContactPhone("(555) 987-6543")
        
        // Verify changes are tracked
        state = viewModel.uiState.first()
        assertTrue(state.hasChanges)
        assertTrue(state.isFormValid)
        assertEquals("Jane Smith", state.childName)
        assertEquals("Peanut allergy", state.medicalInfo)
        assertEquals("(555) 987-6543", state.emergencyContactPhone)
        
        // Mock successful repository update
        mockRepository.shouldSucceed = true
        
        // Save changes
        viewModel.saveChildInformation()
        
        // Verify saving state
        state = viewModel.uiState.first()
        assertTrue(state.isSaving)
        
        // Complete the async operation
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify successful completion
        state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertTrue(state.isUpdateSuccessful)
        assertTrue(state.showSuccessDialog)
        assertNull(state.error)
        
        // Verify optimistic update was applied
        assertEquals("Jane Smith", state.originalChild?.name)
        assertEquals("Peanut allergy", state.originalChild?.medicalInfo)
        assertEquals("(555) 987-6543", state.originalChild?.emergencyContact?.phoneNumber)
        
        // Verify repository was called with correct data
        assertTrue(mockRepository.updateChildCalled)
        val updatedChild = mockRepository.lastUpdatedChild
        assertNotNull(updatedChild)
        assertEquals("Jane Smith", updatedChild.name)
        assertEquals("Peanut allergy", updatedChild.medicalInfo)
        assertEquals("(555) 987-6543", updatedChild.emergencyContact.phoneNumber)
    }
    
    @Test
    fun `complete child edit flow - validation failure`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Make invalid changes
        viewModel.updateChildName("") // Invalid - required field
        viewModel.updateDateOfBirth("invalid-date") // Invalid format
        viewModel.updateEmergencyContactPhone("123") // Invalid - too short
        
        // Verify validation errors
        var state = viewModel.uiState.first()
        assertTrue(state.hasChanges)
        assertFalse(state.isFormValid)
        assertTrue(state.hasValidationErrors)
        assertEquals("Child name is required", state.nameError)
        assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError)
        assertEquals("Please enter a valid phone number", state.emergencyContactPhoneError)
        
        // Attempt to save
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify save was not attempted
        state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertFalse(mockRepository.updateChildCalled)
        
        // Validation errors should still be present
        assertEquals("Child name is required", state.nameError)
        assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError)
        assertEquals("Please enter a valid phone number", state.emergencyContactPhoneError)
    }
    
    @Test
    fun `complete child edit flow - repository failure with optimistic update rollback`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Make valid changes
        viewModel.updateChildName("Jane Smith")
        viewModel.updateMedicalInfo("Peanut allergy")
        
        // Verify changes
        var state = viewModel.uiState.first()
        assertTrue(state.hasChanges)
        assertTrue(state.isFormValid)
        
        // Mock repository failure
        mockRepository.shouldSucceed = false
        mockRepository.errorMessage = "Network connection failed"
        
        // Save changes
        viewModel.saveChildInformation()
        
        // Verify optimistic update was applied immediately
        state = viewModel.uiState.first()
        assertTrue(state.isSaving)
        assertEquals("Jane Smith", state.originalChild?.name) // Optimistic update
        
        // Complete the async operation
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify failure handling
        state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertEquals("Failed to save changes: Network connection failed", state.error)
        
        // Verify optimistic update was rolled back
        assertEquals("John Doe", state.originalChild?.name) // Reverted to original
        assertEquals("No allergies", state.originalChild?.medicalInfo) // Reverted to original
        
        // Form should still show the user's changes
        assertEquals("Jane Smith", state.childName)
        assertEquals("Peanut allergy", state.medicalInfo)
        assertTrue(state.hasChanges) // Changes still present for user to retry
    }
    
    @Test
    fun `complete child edit flow - reset form functionality`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Make changes
        viewModel.updateChildName("Jane Smith")
        viewModel.updateMedicalInfo("Peanut allergy")
        viewModel.updateDietaryRestrictions("Vegan")
        
        // Verify changes
        var state = viewModel.uiState.first()
        assertTrue(state.hasChanges)
        assertEquals("Jane Smith", state.childName)
        assertEquals("Peanut allergy", state.medicalInfo)
        assertEquals("Vegan", state.dietaryRestrictions)
        
        // Reset form
        viewModel.resetForm()
        
        // Verify form was reset to original data
        state = viewModel.uiState.first()
        assertFalse(state.hasChanges)
        assertEquals("John Doe", state.childName)
        assertEquals("No allergies", state.medicalInfo)
        assertEquals("Vegetarian", state.dietaryRestrictions)
        assertEquals("Jane Doe", state.emergencyContactName)
        assertEquals("(555) 123-4567", state.emergencyContactPhone)
        assertEquals("Mother", state.emergencyContactRelationship)
    }
    
    @Test
    fun `complete child edit flow - dialog state management`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Test date picker dialog
        viewModel.showDatePicker()
        var state = viewModel.uiState.first()
        assertTrue(state.showDatePicker)
        
        viewModel.hideDatePicker()
        state = viewModel.uiState.first()
        assertFalse(state.showDatePicker)
        
        // Test discard changes dialog
        viewModel.showDiscardChangesDialog()
        state = viewModel.uiState.first()
        assertTrue(state.showDiscardChangesDialog)
        
        viewModel.hideDiscardChangesDialog()
        state = viewModel.uiState.first()
        assertFalse(state.showDiscardChangesDialog)
        
        // Test success dialog
        viewModel.showSuccessDialog()
        state = viewModel.uiState.first()
        assertTrue(state.showSuccessDialog)
        
        viewModel.hideSuccessDialog()
        state = viewModel.uiState.first()
        assertFalse(state.showSuccessDialog)
    }
    
    @Test
    fun `complete child edit flow - error handling and recovery`() = runTest {
        // Initialize with child data
        viewModel.initializeWithChild(testChild)
        
        // Make valid changes
        viewModel.updateChildName("Jane Smith")
        
        // Mock repository exception
        mockRepository.shouldThrowException = true
        
        // Save changes
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify error handling
        var state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertFalse(state.isUpdateSuccessful)
        assertNotNull(state.error)
        assertTrue(state.error?.contains("Failed to save changes") == true)
        
        // Clear error
        viewModel.clearError()
        state = viewModel.uiState.first()
        assertNull(state.error)
        
        // Fix repository and retry
        mockRepository.shouldThrowException = false
        mockRepository.shouldSucceed = true
        
        viewModel.saveChildInformation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify successful retry
        state = viewModel.uiState.first()
        assertFalse(state.isSaving)
        assertTrue(state.isUpdateSuccessful)
        assertNull(state.error)
    }
}

/**
 * Mock implementation of KidsRepository for integration testing
 */
private class MockKidsRepository : KidsRepository {
    var shouldSucceed = true
    var shouldThrowException = false
    var errorMessage = "Test error"
    var updateChildCalled = false
    var lastUpdatedChild: Child? = null
    
    override suspend fun updateChild(child: Child): Result<Child> {
        updateChildCalled = true
        lastUpdatedChild = child
        
        if (shouldThrowException) {
            throw Exception("Test exception")
        }
        
        return if (shouldSucceed) {
            Result.success(child.copy(updatedAt = System.currentTimeMillis().toString()))
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Other methods not needed for integration tests
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = Result.success(emptyList())
    override suspend fun registerChild(child: Child): Result<Child> = Result.success(child)
    override suspend fun deleteChild(childId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getChildById(childId: String): Result<Child> = Result.failure(Exception("Not implemented"))
    override suspend fun getAvailableServices(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = Result.success(emptyList())
    override suspend fun getServicesForAge(age: Int): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = Result.success(emptyList())
    override suspend fun getServiceById(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.KidsService> = Result.failure(Exception("Not implemented"))
    override suspend fun getServicesAcceptingCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.KidsService>> = Result.success(emptyList())
    override suspend fun checkInChild(childId: String, serviceId: String, checkedInBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun checkOutChild(childId: String, checkedOutBy: String, notes: String?): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = Result.success(emptyList())
    override suspend fun getCurrentCheckIns(serviceId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = Result.success(emptyList())
    override suspend fun getAllCurrentCheckIns(): Result<List<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord>> = Result.success(emptyList())
    override suspend fun getCheckInRecord(recordId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord> = Result.failure(Exception("Not implemented"))
    override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.ServiceReport> = Result.failure(Exception("Not implemented"))
    override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport> = Result.failure(Exception("Not implemented"))
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {}
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.KidsService) -> Unit) {}
    override suspend fun unsubscribeFromUpdates() {}
}