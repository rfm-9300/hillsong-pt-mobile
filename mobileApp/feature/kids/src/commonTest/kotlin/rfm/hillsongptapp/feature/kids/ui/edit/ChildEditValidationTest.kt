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
class ChildEditValidationTest {
    
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
        viewModel.initializeWithChild(testChild)
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // Child Name Validation Tests
    
    @Test
    fun `child name validation - valid names should pass`() = runTest {
        val validNames = listOf(
            "John Doe",
            "Mary-Jane Smith",
            "O'Connor",
            "Jean-Luc",
            "Anna Maria",
            "José",
            "François"
        )
        
        for (name in validNames) {
            viewModel.updateChildName(name)
            val state = viewModel.uiState.first()
            assertNull(state.nameError, "Name '$name' should be valid")
        }
    }
    
    @Test
    fun `child name validation - invalid names should fail`() = runTest {
        val invalidNames = mapOf(
            "" to "Child name is required",
            "J" to "Child name must be at least 2 characters",
            "A".repeat(51) to "Child name must be less than 50 characters",
            "John123" to "Child name can only contain letters, spaces, hyphens, and apostrophes",
            "John@Doe" to "Child name can only contain letters, spaces, hyphens, and apostrophes",
            "John.Doe" to "Child name can only contain letters, spaces, hyphens, and apostrophes"
        )
        
        for ((name, expectedError) in invalidNames) {
            viewModel.updateChildName(name)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.nameError, "Name '$name' should fail with correct error")
        }
    }
    
    // Date of Birth Validation Tests
    
    @Test
    fun `date of birth validation - valid dates should pass`() = runTest {
        val validDates = listOf(
            "2015-05-15",
            "2020-01-01",
            "2010-12-31",
            "2025-01-01", // Current year
            "2007-02-29" // Leap year
        )
        
        for (date in validDates) {
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertNull(state.dateOfBirthError, "Date '$date' should be valid")
        }
    }
    
    @Test
    fun `date of birth validation - invalid format should fail`() = runTest {
        val invalidFormats = listOf(
            "15/05/2015",
            "2015-5-15",
            "2015/05/15",
            "15-05-2015",
            "May 15, 2015",
            "2015-05",
            "05-15"
        )
        
        for (date in invalidFormats) {
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertEquals("Please enter date in YYYY-MM-DD format", state.dateOfBirthError, 
                "Date '$date' should fail with format error")
        }
    }
    
    @Test
    fun `date of birth validation - invalid dates should fail`() = runTest {
        val invalidDates = listOf(
            "2015-13-15", // Invalid month
            "2015-05-32", // Invalid day
            "2015-02-30", // February doesn't have 30 days
            "2015-04-31", // April doesn't have 31 days
            "2021-02-29", // Not a leap year
            "1899-05-15", // Too old
            "2026-05-15"  // Future year beyond reasonable range
        )
        
        for (date in invalidDates) {
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertNotNull(state.dateOfBirthError, "Date '$date' should be invalid")
        }
    }
    
    @Test
    fun `date of birth validation - age range should be enforced`() = runTest {
        // Test reasonable age range (0-18 years)
        viewModel.updateDateOfBirth("1900-01-01") // Too old
        var state = viewModel.uiState.first()
        assertEquals("Child must be between 0 and 18 years old", state.dateOfBirthError)
        
        viewModel.updateDateOfBirth("2025-01-01") // Current year - should be valid
        state = viewModel.uiState.first()
        assertNull(state.dateOfBirthError)
        
        viewModel.updateDateOfBirth("2007-01-01") // 18 years old - should be valid
        state = viewModel.uiState.first()
        assertNull(state.dateOfBirthError)
    }
    
    @Test
    fun `date of birth validation - leap year handling`() = runTest {
        // Valid leap year date
        viewModel.updateDateOfBirth("2020-02-29")
        var state = viewModel.uiState.first()
        assertNull(state.dateOfBirthError)
        
        // Invalid leap year date
        viewModel.updateDateOfBirth("2021-02-29")
        state = viewModel.uiState.first()
        assertEquals("Please enter a valid date", state.dateOfBirthError)
    }
    
    // Emergency Contact Name Validation Tests
    
    @Test
    fun `emergency contact name validation - valid names should pass`() = runTest {
        val validNames = listOf(
            "Jane Doe",
            "Mary-Jane Smith",
            "O'Connor",
            "Jean-Luc Picard",
            "Anna Maria"
        )
        
        for (name in validNames) {
            viewModel.updateEmergencyContactName(name)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactNameError, "Contact name '$name' should be valid")
        }
    }
    
    @Test
    fun `emergency contact name validation - invalid names should fail`() = runTest {
        val invalidNames = mapOf(
            "" to "Emergency contact name is required",
            "J" to "Emergency contact name must be at least 2 characters",
            "A".repeat(51) to "Emergency contact name must be less than 50 characters",
            "Jane123" to "Emergency contact name can only contain letters, spaces, hyphens, and apostrophes"
        )
        
        for ((name, expectedError) in invalidNames) {
            viewModel.updateEmergencyContactName(name)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.emergencyContactNameError, 
                "Contact name '$name' should fail with correct error")
        }
    }
    
    // Emergency Contact Phone Validation Tests
    
    @Test
    fun `emergency contact phone validation - valid phones should pass`() = runTest {
        val validPhones = listOf(
            "(555) 123-4567",
            "555-123-4567",
            "555 123 4567",
            "5551234567",
            "+1 555 123 4567",
            "+1-555-123-4567",
            "(555)123-4567"
        )
        
        for (phone in validPhones) {
            viewModel.updateEmergencyContactPhone(phone)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactPhoneError, "Phone '$phone' should be valid")
        }
    }
    
    @Test
    fun `emergency contact phone validation - invalid phones should fail`() = runTest {
        val invalidPhones = listOf(
            "",
            "123",
            "555-123",
            "abc-def-ghij",
            "555@123@4567",
            "555.123.4567" // Dots not allowed in basic validation
        )
        
        for (phone in invalidPhones) {
            viewModel.updateEmergencyContactPhone(phone)
            val state = viewModel.uiState.first()
            assertNotNull(state.emergencyContactPhoneError, "Phone '$phone' should be invalid")
        }
    }
    
    // Emergency Contact Relationship Validation Tests
    
    @Test
    fun `emergency contact relationship validation - valid relationships should pass`() = runTest {
        val validRelationships = listOf(
            "Mother",
            "Father",
            "Guardian",
            "Grandmother",
            "Grandfather",
            "Aunt",
            "Uncle",
            "Family Friend"
        )
        
        for (relationship in validRelationships) {
            viewModel.updateEmergencyContactRelationship(relationship)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactRelationshipError, 
                "Relationship '$relationship' should be valid")
        }
    }
    
    @Test
    fun `emergency contact relationship validation - invalid relationships should fail`() = runTest {
        val invalidRelationships = mapOf(
            "" to "Emergency contact relationship is required",
            "M" to "Relationship must be at least 2 characters",
            "A".repeat(31) to "Relationship must be less than 30 characters"
        )
        
        for ((relationship, expectedError) in invalidRelationships) {
            viewModel.updateEmergencyContactRelationship(relationship)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.emergencyContactRelationshipError, 
                "Relationship '$relationship' should fail with correct error")
        }
    }
    
    // Form Validation Integration Tests
    
    @Test
    fun `form validation - all valid fields should enable save`() = runTest {
        viewModel.updateChildName("Jane Smith")
        viewModel.updateDateOfBirth("2016-06-20")
        viewModel.updateEmergencyContactName("John Smith")
        viewModel.updateEmergencyContactPhone("(555) 987-6543")
        viewModel.updateEmergencyContactRelationship("Father")
        
        val state = viewModel.uiState.first()
        assertTrue(state.isFormValid)
        assertTrue(state.hasChanges)
        assertFalse(state.hasValidationErrors)
    }
    
    @Test
    fun `form validation - any invalid field should disable save`() = runTest {
        viewModel.updateChildName("Jane Smith")
        viewModel.updateDateOfBirth("2016-06-20")
        viewModel.updateEmergencyContactName("John Smith")
        viewModel.updateEmergencyContactPhone("123") // Invalid
        viewModel.updateEmergencyContactRelationship("Father")
        
        val state = viewModel.uiState.first()
        assertFalse(state.isFormValid)
        assertTrue(state.hasChanges)
        assertTrue(state.hasValidationErrors)
    }
    
    @Test
    fun `form validation - missing required fields should disable save`() = runTest {
        viewModel.updateChildName("Jane Smith")
        viewModel.updateDateOfBirth("") // Missing required field
        viewModel.updateEmergencyContactName("John Smith")
        viewModel.updateEmergencyContactPhone("(555) 987-6543")
        viewModel.updateEmergencyContactRelationship("Father")
        
        val state = viewModel.uiState.first()
        assertFalse(state.isFormValid)
        assertFalse(state.areRequiredFieldsFilled)
    }
    
    @Test
    fun `form validation - no changes should disable save`() = runTest {
        // Don't make any changes to the form
        val state = viewModel.uiState.first()
        
        assertTrue(state.areRequiredFieldsFilled) // Original data fills required fields
        assertFalse(state.hasValidationErrors) // Original data is valid
        assertFalse(state.hasChanges) // No changes made
        assertFalse(state.isFormValid) // Should be false because no changes
    }
    
    @Test
    fun `real-time validation - errors should appear immediately on field change`() = runTest {
        // Test that validation happens immediately when field changes
        viewModel.updateChildName("") // Should immediately show error
        
        var state = viewModel.uiState.first()
        assertEquals("Child name is required", state.nameError)
        
        viewModel.updateChildName("Valid Name") // Should immediately clear error
        
        state = viewModel.uiState.first()
        assertNull(state.nameError)
    }
    
    @Test
    fun `validation should handle edge cases`() = runTest {
        // Test whitespace handling
        viewModel.updateChildName("  John Doe  ") // Leading/trailing spaces
        var state = viewModel.uiState.first()
        assertNull(state.nameError) // Should be valid (will be trimmed)
        
        // Test empty vs whitespace
        viewModel.updateChildName("   ") // Only whitespace
        state = viewModel.uiState.first()
        assertEquals("Child name is required", state.nameError) // Should be treated as empty
        
        // Test special characters in names
        viewModel.updateChildName("Mary-Jane O'Connor") // Hyphens and apostrophes
        state = viewModel.uiState.first()
        assertNull(state.nameError) // Should be valid
    }
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    override suspend fun updateChild(child: Child): Result<Child> = Result.success(child)
    
    // Other methods not needed for validation tests
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