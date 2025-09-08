package rfm.hillsongptapp.feature.kids.ui.registration

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
class ChildRegistrationViewModelTest {
    
    private lateinit var mockRepository: MockKidsRepository
    private lateinit var viewModel: ChildRegistrationViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockKidsRepository()
        viewModel = ChildRegistrationViewModel(mockRepository)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be empty`() = runTest {
        val initialState = viewModel.uiState.first()
        
        assertEquals("", initialState.childName)
        assertEquals("", initialState.dateOfBirth)
        assertEquals("", initialState.medicalInfo)
        assertEquals("", initialState.dietaryRestrictions)
        assertEquals("", initialState.emergencyContactName)
        assertEquals("", initialState.emergencyContactPhone)
        assertEquals("", initialState.emergencyContactRelationship)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isRegistrationSuccessful)
        assertNull(initialState.error)
        assertFalse(initialState.showDatePicker)
    }
    
    @Test
    fun `updateChildName should update name and validate`() = runTest {
        viewModel.updateChildName("John Doe")
        
        val state = viewModel.uiState.first()
        assertEquals("John Doe", state.childName)
        assertNull(state.nameError)
    }
    
    @Test
    fun `updateChildName with invalid name should show error`() = runTest {
        viewModel.updateChildName("J") // Too short
        
        val state = viewModel.uiState.first()
        assertEquals("J", state.childName)
        assertEquals("Child name must be at least 2 characters", state.nameError)
    }
    
    @Test
    fun `updateChildName with empty name should show error`() = runTest {
        viewModel.updateChildName("")
        
        val state = viewModel.uiState.first()
        assertEquals("", state.childName)
        assertEquals("Child name is required", state.nameError)
    }
    
    @Test
    fun `updateChildName with invalid characters should show error`() = runTest {
        viewModel.updateChildName("John123") // Contains numbers
        
        val state = viewModel.uiState.first()
        assertEquals("John123", state.childName)
        assertEquals("Child name can only contain letters, spaces, hyphens, and apostrophes", state.nameError)
    }
    
    @Test
    fun `updateDateOfBirth should update date and validate`() = runTest {
        viewModel.updateDateOfBirth("2015-05-15")
        
        val state = viewModel.uiState.first()
        assertEquals("2015-05-15", state.dateOfBirth)
        assertNull(state.dateOfBirthError)
    }
    
    @Test
    fun `updateDateOfBirth with invalid format should show error`() = runTest {
        viewModel.updateDateOfBirth("15/05/2015") // Wrong format
        
        val state = viewModel.uiState.first()
        assertEquals("15/05/2015", state.dateOfBirth)
        assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError)
    }
    
    @Test
    fun `updateDateOfBirth with empty date should show error`() = runTest {
        viewModel.updateDateOfBirth("")
        
        val state = viewModel.uiState.first()
        assertEquals("", state.dateOfBirth)
        assertEquals("Date of birth is required", state.dateOfBirthError)
    }
    
    @Test
    fun `updateEmergencyContactPhone should validate phone number`() = runTest {
        viewModel.updateEmergencyContactPhone("(555) 123-4567")
        
        val state = viewModel.uiState.first()
        assertEquals("(555) 123-4567", state.emergencyContactPhone)
        assertNull(state.emergencyContactPhoneError)
    }
    
    @Test
    fun `updateEmergencyContactPhone with invalid phone should show error`() = runTest {
        viewModel.updateEmergencyContactPhone("123") // Too short
        
        val state = viewModel.uiState.first()
        assertEquals("123", state.emergencyContactPhone)
        assertEquals("Please enter a valid phone number", state.emergencyContactPhoneError)
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
    fun `getCalculatedAge should return correct age`() = runTest {
        viewModel.updateDateOfBirth("2015-05-15")
        
        val state = viewModel.uiState.first()
        val age = state.getCalculatedAge()
        assertEquals(10, age) // 2025 - 2015 = 10
    }
    
    @Test
    fun `getCalculatedAge with invalid date should return null`() = runTest {
        viewModel.updateDateOfBirth("invalid-date")
        
        val state = viewModel.uiState.first()
        val age = state.getCalculatedAge()
        assertNull(age)
    }
    
    @Test
    fun `isFormValid should return true when all required fields are filled and valid`() = runTest {
        viewModel.updateChildName("John Doe")
        viewModel.updateDateOfBirth("2015-05-15")
        viewModel.updateEmergencyContactName("Jane Doe")
        viewModel.updateEmergencyContactPhone("(555) 123-4567")
        viewModel.updateEmergencyContactRelationship("Mother")
        
        val state = viewModel.uiState.first()
        assertTrue(state.isFormValid)
    }
    
    @Test
    fun `isFormValid should return false when required fields are missing`() = runTest {
        viewModel.updateChildName("John Doe")
        // Missing other required fields
        
        val state = viewModel.uiState.first()
        assertFalse(state.isFormValid)
    }
    
    @Test
    fun `registerChild should succeed with valid data`() = runTest {
        // Setup valid form data
        viewModel.updateChildName("John Doe")
        viewModel.updateDateOfBirth("2015-05-15")
        viewModel.updateEmergencyContactName("Jane Doe")
        viewModel.updateEmergencyContactPhone("(555) 123-4567")
        viewModel.updateEmergencyContactRelationship("Mother")
        
        // Mock successful registration
        mockRepository.shouldSucceed = true
        
        viewModel.registerChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertTrue(state.isRegistrationSuccessful)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `registerChild should fail with invalid data`() = runTest {
        // Setup invalid form data (missing required fields)
        viewModel.updateChildName("") // Invalid
        
        viewModel.registerChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isRegistrationSuccessful)
        assertFalse(state.isLoading)
        assertEquals("Child name is required", state.nameError)
    }
    
    @Test
    fun `registerChild should handle repository failure`() = runTest {
        // Setup valid form data
        viewModel.updateChildName("John Doe")
        viewModel.updateDateOfBirth("2015-05-15")
        viewModel.updateEmergencyContactName("Jane Doe")
        viewModel.updateEmergencyContactPhone("(555) 123-4567")
        viewModel.updateEmergencyContactRelationship("Mother")
        
        // Mock repository failure
        mockRepository.shouldSucceed = false
        mockRepository.errorMessage = "Network error"
        
        viewModel.registerChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isRegistrationSuccessful)
        assertFalse(state.isLoading)
        assertEquals("Registration failed: Network error", state.error)
    }
    
    @Test
    fun `resetForm should clear all fields`() = runTest {
        // Fill form with data
        viewModel.updateChildName("John Doe")
        viewModel.updateDateOfBirth("2015-05-15")
        viewModel.updateMedicalInfo("No allergies")
        
        // Reset form
        viewModel.resetForm()
        
        val state = viewModel.uiState.first()
        assertEquals("", state.childName)
        assertEquals("", state.dateOfBirth)
        assertEquals("", state.medicalInfo)
        assertNull(state.nameError)
        assertNull(state.dateOfBirthError)
    }
    
    @Test
    fun `clearError should clear error message`() = runTest {
        // Trigger an error
        viewModel.updateChildName("")
        viewModel.registerChild()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Clear error
        viewModel.clearError()
        
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    
    override suspend fun registerChild(child: Child): Result<Child> {
        return if (shouldSucceed) {
            Result.success(child.copy(id = "test_child_id"))
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Other methods not needed for registration tests
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> = 
        Result.success(emptyList())
    override suspend fun updateChild(child: Child): Result<Child> = 
        Result.success(child)
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
    override suspend fun getServiceReport(serviceId: String): Result<rfm.hillsongptapp.feature.kids.domain.repository.ServiceReport> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<rfm.hillsongptapp.feature.kids.domain.repository.AttendanceReport> = 
        Result.failure(Exception("Not implemented"))
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {}
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.KidsService) -> Unit) {}
    override suspend fun unsubscribeFromUpdates() {}
}