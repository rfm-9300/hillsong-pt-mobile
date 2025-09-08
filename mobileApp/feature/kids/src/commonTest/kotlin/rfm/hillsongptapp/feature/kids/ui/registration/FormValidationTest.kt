package rfm.hillsongptapp.feature.kids.ui.registration

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import rfm.hillsongptapp.feature.kids.domain.repository.KidsRepository
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FormValidationTest {
    
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
    
    // Child Name Validation Tests
    
    @Test
    fun `child name validation - valid names should pass`() = runTest {
        val validNames = listOf(
            "John Doe",
            "Mary-Jane Smith",
            "O'Connor",
            "Jean-Luc",
            "Anna Maria",
            "José"
        )
        
        validNames.forEach { name ->
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
            "John123" to "Child name can only contain letters, spaces, hyphens, and apostrophes",
            "John@Doe" to "Child name can only contain letters, spaces, hyphens, and apostrophes",
            "A".repeat(51) to "Child name must be less than 50 characters"
        )
        
        invalidNames.forEach { (name, expectedError) ->
            viewModel.updateChildName(name)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.nameError, "Name '$name' should have error: $expectedError")
        }
    }
    
    // Date of Birth Validation Tests
    
    @Test
    fun `date of birth validation - valid dates should pass`() = runTest {
        val validDates = listOf(
            "2015-05-15",
            "2010-01-01",
            "2020-12-31",
            "2007-02-28",
            "2008-02-29" // Leap year
        )
        
        validDates.forEach { date ->
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertNull(state.dateOfBirthError, "Date '$date' should be valid")
        }
    }
    
    @Test
    fun `date of birth validation - invalid dates should fail`() = runTest {
        val invalidDates = mapOf(
            "" to "Date of birth is required",
            "15/05/2015" to "Please enter date in YYYY-MM-DD format",
            "2015-5-15" to "Please enter date in YYYY-MM-DD format",
            "2015-13-15" to "Please enter a valid date", // Invalid month
            "2015-02-30" to "Please enter a valid date", // Invalid day for February
            "2015-04-31" to "Please enter a valid date", // Invalid day for April
            "1800-01-01" to "Child must be between 0 and 18 years old", // Too old
            "2030-01-01" to "Child must be between 0 and 18 years old" // Future date
        )
        
        invalidDates.forEach { (date, expectedError) ->
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.dateOfBirthError, "Date '$date' should have error: $expectedError")
        }
    }
    
    // Emergency Contact Name Validation Tests
    
    @Test
    fun `emergency contact name validation - valid names should pass`() = runTest {
        val validNames = listOf(
            "Jane Doe",
            "Mary-Jane Smith",
            "O'Connor",
            "Jean-Luc",
            "Anna Maria"
        )
        
        validNames.forEach { name ->
            viewModel.updateEmergencyContactName(name)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactNameError, "Emergency contact name '$name' should be valid")
        }
    }
    
    @Test
    fun `emergency contact name validation - invalid names should fail`() = runTest {
        val invalidNames = mapOf(
            "" to "Emergency contact name is required",
            "J" to "Emergency contact name must be at least 2 characters",
            "Jane123" to "Emergency contact name can only contain letters, spaces, hyphens, and apostrophes",
            "A".repeat(51) to "Emergency contact name must be less than 50 characters"
        )
        
        invalidNames.forEach { (name, expectedError) ->
            viewModel.updateEmergencyContactName(name)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.emergencyContactNameError, "Emergency contact name '$name' should have error: $expectedError")
        }
    }
    
    // Phone Number Validation Tests
    
    @Test
    fun `phone number validation - valid numbers should pass`() = runTest {
        val validNumbers = listOf(
            "(555) 123-4567",
            "555-123-4567",
            "5551234567",
            "+1 555 123 4567",
            "555 123 4567",
            "(555)123-4567"
        )
        
        validNumbers.forEach { number ->
            viewModel.updateEmergencyContactPhone(number)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactPhoneError, "Phone number '$number' should be valid")
        }
    }
    
    @Test
    fun `phone number validation - invalid numbers should fail`() = runTest {
        val invalidNumbers = mapOf(
            "" to "Emergency contact phone is required",
            "123" to "Please enter a valid phone number", // Too short
            "abc-def-ghij" to "Please enter a valid phone number", // Contains letters
            "555@123#4567" to "Please enter a valid phone number" // Invalid characters
        )
        
        invalidNumbers.forEach { (number, expectedError) ->
            viewModel.updateEmergencyContactPhone(number)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.emergencyContactPhoneError, "Phone number '$number' should have error: $expectedError")
        }
    }
    
    // Relationship Validation Tests
    
    @Test
    fun `relationship validation - valid relationships should pass`() = runTest {
        val validRelationships = listOf(
            "Mother",
            "Father",
            "Guardian",
            "Grandparent",
            "Aunt",
            "Uncle",
            "Family Friend"
        )
        
        validRelationships.forEach { relationship ->
            viewModel.updateEmergencyContactRelationship(relationship)
            val state = viewModel.uiState.first()
            assertNull(state.emergencyContactRelationshipError, "Relationship '$relationship' should be valid")
        }
    }
    
    @Test
    fun `relationship validation - invalid relationships should fail`() = runTest {
        val invalidRelationships = mapOf(
            "" to "Emergency contact relationship is required",
            "M" to "Relationship must be at least 2 characters",
            "A".repeat(31) to "Relationship must be less than 30 characters"
        )
        
        invalidRelationships.forEach { (relationship, expectedError) ->
            viewModel.updateEmergencyContactRelationship(relationship)
            val state = viewModel.uiState.first()
            assertEquals(expectedError, state.emergencyContactRelationshipError, "Relationship '$relationship' should have error: $expectedError")
        }
    }
    
    // Age Calculation Tests
    
    @Test
    fun `age calculation should work correctly`() = runTest {
        val testCases = mapOf(
            "2015-01-01" to 10, // 2025 - 2015 = 10
            "2020-01-01" to 5,  // 2025 - 2020 = 5
            "2025-01-01" to 0,  // 2025 - 2025 = 0
            "2010-01-01" to 15  // 2025 - 2010 = 15
        )
        
        testCases.forEach { (date, expectedAge) ->
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertEquals(expectedAge, state.getCalculatedAge(), "Date '$date' should calculate age as $expectedAge")
        }
    }
    
    // Leap Year Validation Tests
    
    @Test
    fun `leap year validation should work correctly`() = runTest {
        // Valid leap year dates
        val validLeapYearDates = listOf(
            "2008-02-29", // Divisible by 4
            "2012-02-29", // Divisible by 4
            "2000-02-29"  // Divisible by 400
        )
        
        validLeapYearDates.forEach { date ->
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertNull(state.dateOfBirthError, "Leap year date '$date' should be valid")
        }
        
        // Invalid leap year dates
        val invalidLeapYearDates = listOf(
            "2009-02-29", // Not a leap year
            "2010-02-29", // Not a leap year
            "1900-02-29"  // Divisible by 100 but not 400
        )
        
        invalidLeapYearDates.forEach { date ->
            viewModel.updateDateOfBirth(date)
            val state = viewModel.uiState.first()
            assertEquals("Please enter a valid date", state.dateOfBirthError, "Invalid leap year date '$date' should fail")
        }
    }
}

/**
 * Mock implementation of KidsRepository for testing
 */
private class MockKidsRepository : KidsRepository {
    var shouldSucceed = true
    var errorMessage = "Test error"
    
    override suspend fun registerChild(child: rfm.hillsongptapp.feature.kids.domain.model.Child): Result<rfm.hillsongptapp.feature.kids.domain.model.Child> {
        return if (shouldSucceed) {
            Result.success(child.copy(id = "test_child_id"))
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Other methods not needed for validation tests
    override suspend fun getChildrenForParent(parentId: String): Result<List<rfm.hillsongptapp.feature.kids.domain.model.Child>> = 
        Result.success(emptyList())
    override suspend fun updateChild(child: rfm.hillsongptapp.feature.kids.domain.model.Child): Result<rfm.hillsongptapp.feature.kids.domain.model.Child> = 
        Result.success(child)
    override suspend fun deleteChild(childId: String): Result<Unit> = 
        Result.success(Unit)
    override suspend fun getChildById(childId: String): Result<rfm.hillsongptapp.feature.kids.domain.model.Child> = 
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
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.Child) -> Unit) {}
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (rfm.hillsongptapp.feature.kids.domain.model.KidsService) -> Unit) {}
    override suspend fun unsubscribeFromUpdates() {}
}