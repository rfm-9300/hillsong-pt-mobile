package rfm.hillsongptapp.feature.kids.integration

import kotlinx.coroutines.test.runTest
import rfm.hillsongptapp.feature.kids.data.network.error.KidsManagementError
import rfm.hillsongptapp.feature.kids.domain.error.ErrorHandler
import rfm.hillsongptapp.feature.kids.domain.error.ErrorRecoveryManager
import rfm.hillsongptapp.feature.kids.domain.error.RecoveryContext
import rfm.hillsongptapp.feature.kids.domain.error.RecoveryResult
import rfm.hillsongptapp.feature.kids.domain.offline.OfflineHandler
import rfm.hillsongptapp.feature.kids.domain.validation.BusinessRuleValidator
import rfm.hillsongptapp.feature.kids.domain.validation.ChildValidator
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import co.touchlab.kermit.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ErrorHandlingIntegrationTest {
    
    private val logger = Logger.withTag("ErrorHandlingIntegrationTest")
    private val errorHandler = ErrorHandler(logger)
    private val offlineHandler = OfflineHandler(logger)
    private val errorRecoveryManager = ErrorRecoveryManager(errorHandler, offlineHandler, logger)
    private val childValidator = ChildValidator()
    private val businessRuleValidator = BusinessRuleValidator()
    
    private val sampleChild = Child(
        id = "child123",
        parentId = "parent123",
        name = "John Doe",
        dateOfBirth = "2015-01-01",
        medicalInfo = null,
        dietaryRestrictions = null,
        emergencyContact = EmergencyContact(
            name = "Jane Doe",
            phoneNumber = "1234567890",
            relationship = "parent"
        ),
        status = CheckInStatus.NOT_IN_SERVICE,
        currentServiceId = null,
        checkInTime = null,
        checkOutTime = null,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )
    
    private val sampleService = KidsService(
        id = "service123",
        name = "Kids Church",
        description = "Sunday service for kids",
        minAge = 5,
        maxAge = 12,
        startTime = "09:00",
        endTime = "10:30",
        location = "Kids Room",
        maxCapacity = 20,
        currentCapacity = 10,
        isAcceptingCheckIns = true,
        staffMembers = listOf("staff1", "staff2"),
        createdAt = "2025-01-01T00:00:00Z"
    )
    
    @Test
    fun `complete error handling flow for network error with retry`() = runTest {
        var attemptCount = 0
        val operation: suspend () -> Result<String> = {
            attemptCount++
            if (attemptCount < 3) {
                Result.failure(KidsManagementError.NetworkError)
            } else {
                Result.success("Operation succeeded")
            }
        }
        
        val context = RecoveryContext(
            operationType = "checkIn",
            userId = "parent123",
            childId = "child123",
            serviceId = "service123"
        )
        
        val result = errorRecoveryManager.startRecovery(
            error = KidsManagementError.NetworkError,
            operation = operation,
            context = context
        )
        
        assertTrue(result is RecoveryResult.Success)
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `complete error handling flow for validation error requiring user action`() = runTest {
        val validationError = KidsManagementError.ValidationError("name", "Name is required")
        
        val operation: suspend () -> Result<String> = {
            Result.failure(validationError)
        }
        
        val context = RecoveryContext(
            operationType = "registerChild",
            userId = "parent123",
            childId = null,
            serviceId = null
        )
        
        val result = errorRecoveryManager.startRecovery(
            error = validationError,
            operation = operation,
            context = context
        )
        
        assertTrue(result is RecoveryResult.RequiresUserAction)
        assertTrue(result.message.contains("review and correct"))
    }
    
    @Test
    fun `complete error handling flow for business rule violation`() = runTest {
        val businessRuleError = KidsManagementError.ServiceAtCapacity
        
        val operation: suspend () -> Result<String> = {
            Result.failure(businessRuleError)
        }
        
        val context = RecoveryContext(
            operationType = "checkIn",
            userId = "parent123",
            childId = "child123",
            serviceId = "service123"
        )
        
        val result = errorRecoveryManager.startRecovery(
            error = businessRuleError,
            operation = operation,
            context = context
        )
        
        assertTrue(result is RecoveryResult.RequiresUserAction)
        assertTrue(result.message.contains("full capacity") || result.actions.isNotEmpty())
    }
    
    @Test
    fun `offline handling integration with error recovery`() = runTest {
        // Set offline mode
        offlineHandler.setOfflineStatus(true)
        
        val networkError = KidsManagementError.NetworkError
        val operation: suspend () -> Result<String> = {
            Result.failure(networkError)
        }
        
        val context = RecoveryContext(
            operationType = "checkIn",
            userId = "parent123",
            childId = "child123",
            serviceId = "service123"
        )
        
        val result = errorRecoveryManager.startRecovery(
            error = networkError,
            operation = operation,
            context = context
        )
        
        // Should suggest offline mode
        assertTrue(result is RecoveryResult.RequiresUserAction)
        assertTrue(result.message.contains("offline") || result.actions.contains("Continue Offline"))
    }
    
    @Test
    fun `validation integration with business rules`() {
        // Test child validation
        val invalidChildValidation = childValidator.validateChildForRegistration(
            name = "",
            dateOfBirth = "invalid-date",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact(
                name = "",
                phoneNumber = "123",
                relationship = "friend"
            )
        )
        
        assertFalse(invalidChildValidation.isValid)
        assertTrue(invalidChildValidation.allErrorMessages.size >= 5)
        
        // Test business rule validation
        val checkedInChild = sampleChild.copy(status = CheckInStatus.CHECKED_IN)
        val fullService = sampleService.copy(currentCapacity = 20, maxCapacity = 20)
        
        val businessRuleValidation = businessRuleValidator.getComprehensiveCheckInValidation(
            child = checkedInChild,
            service = fullService,
            parentId = "parent123"
        )
        
        assertFalse(businessRuleValidation.isValid)
        assertTrue(businessRuleValidation.allErrorMessages.size >= 2)
    }
    
    @Test
    fun `error categorization and user message generation`() {
        val errors = listOf(
            KidsManagementError.NetworkError,
            KidsManagementError.ChildNotFound,
            KidsManagementError.ServiceAtCapacity,
            KidsManagementError.ValidationError("name", "Required"),
            KidsManagementError.Unauthorized,
            KidsManagementError.ServerError
        )
        
        errors.forEach { error ->
            val errorInfo = errorHandler.handleError(error)
            
            // Verify error info is properly categorized
            assertTrue(errorInfo.userMessage.isNotBlank())
            assertTrue(errorInfo.technicalMessage.isNotBlank())
            assertTrue(errorInfo.suggestedAction.isNotBlank())
            assertTrue(errorInfo.summary.isNotBlank())
            
            // Verify retryability is correctly set
            when (error) {
                is KidsManagementError.NetworkError,
                is KidsManagementError.ServerError,
                is KidsManagementError.ServiceAtCapacity -> assertTrue(errorInfo.isRetryable)
                
                is KidsManagementError.ChildNotFound,
                is KidsManagementError.ValidationError,
                is KidsManagementError.Unauthorized -> assertFalse(errorInfo.isRetryable)
                
                else -> {} // Other cases handled individually
            }
        }
    }
    
    @Test
    fun `error recovery suggestions based on history`() = runTest {
        // Simulate multiple network errors
        repeat(5) {
            val operation: suspend () -> Result<String> = {
                Result.failure(KidsManagementError.NetworkError)
            }
            
            val context = RecoveryContext(
                operationType = "checkIn",
                userId = "parent123",
                childId = "child123",
                serviceId = "service123"
            )
            
            errorRecoveryManager.startRecovery(
                error = KidsManagementError.NetworkError,
                operation = operation,
                context = context
            )
        }
        
        val suggestions = errorRecoveryManager.getRecoverySuggestions(rfm.hillsongptapp.feature.kids.domain.error.ErrorType.NETWORK)
        
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.contains("network provider") || it.contains("switching between WiFi") })
    }
    
    @Test
    fun `comprehensive validation error handling`() {
        // Test multiple validation errors
        val invalidData = mapOf(
            "name" to "",
            "dateOfBirth" to "2030-01-01", // Future date
            "emergencyContactName" to "A", // Too short
            "emergencyContactPhone" to "123", // Too short
            "emergencyContactRelationship" to "stranger" // Invalid relationship
        )
        
        val nameValidation = childValidator.validateName(invalidData["name"]!!)
        val dateValidation = childValidator.validateDateOfBirth(invalidData["dateOfBirth"]!!)
        val contactNameValidation = childValidator.validateEmergencyContactName(invalidData["emergencyContactName"]!!)
        val contactPhoneValidation = childValidator.validateEmergencyContactPhone(invalidData["emergencyContactPhone"]!!)
        val contactRelationshipValidation = childValidator.validateEmergencyContactRelationship(invalidData["emergencyContactRelationship"]!!)
        
        // All validations should fail
        assertFalse(nameValidation.isValid)
        assertFalse(dateValidation.isValid)
        assertFalse(contactNameValidation.isValid)
        assertFalse(contactPhoneValidation.isValid)
        assertFalse(contactRelationshipValidation.isValid)
        
        // Each should have appropriate error messages
        assertTrue(nameValidation.errorMessage!!.contains("required"))
        assertTrue(dateValidation.errorMessage!!.contains("future"))
        assertTrue(contactNameValidation.errorMessage!!.contains("at least"))
        assertTrue(contactPhoneValidation.errorMessage!!.contains("at least"))
        assertTrue(contactRelationshipValidation.errorMessage!!.contains("valid relationship"))
    }
    
    @Test
    fun `business rule validation with multiple violations`() {
        // Create scenario with multiple business rule violations
        val unauthorizedParent = "other-parent"
        val checkedInChild = sampleChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "other-service"
        )
        val fullService = sampleService.copy(
            currentCapacity = 20,
            maxCapacity = 20,
            isAcceptingCheckIns = false,
            staffMembers = emptyList()
        )
        val youngChild = sampleChild.copy(dateOfBirth = "2022-01-01") // 3 years old
        
        val validation = businessRuleValidator.getComprehensiveCheckInValidation(
            child = youngChild.copy(status = CheckInStatus.CHECKED_IN),
            service = fullService,
            parentId = unauthorizedParent
        )
        
        assertFalse(validation.isValid)
        
        // Should have multiple violations
        val errors = validation.allErrorMessages
        assertTrue(errors.size >= 4)
        
        // Check for specific violations
        assertTrue(errors.any { it.contains("not authorized") })
        assertTrue(errors.any { it.contains("already checked") })
        assertTrue(errors.any { it.contains("full capacity") })
        assertTrue(errors.any { it.contains("not currently accepting") })
        assertTrue(errors.any { it.contains("age") })
        assertTrue(errors.any { it.contains("no assigned staff") })
    }
    
    @Test
    fun `offline operation handling with pending operations`() = runTest {
        offlineHandler.setOfflineStatus(true)
        
        // Try to perform operations that require network
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.CHECK_IN))
        assertFalse(offlineHandler.isOperationAvailableOffline(OfflineOperation.REGISTER_CHILD))
        
        // Add pending operations
        val pendingCheckIn = rfm.hillsongptapp.feature.kids.domain.offline.PendingOperation(
            id = "checkin1",
            type = rfm.hillsongptapp.feature.kids.domain.offline.PendingOperationType.CHECK_IN_CHILD,
            data = mapOf("childId" to "child123", "serviceId" to "service123"),
            timestamp = System.currentTimeMillis(),
            description = "Check in John Doe"
        )
        
        val pendingRegistration = rfm.hillsongptapp.feature.kids.domain.offline.PendingOperation(
            id = "register1",
            type = rfm.hillsongptapp.feature.kids.domain.offline.PendingOperationType.REGISTER_CHILD,
            data = mapOf("childName" to "Jane Doe"),
            timestamp = System.currentTimeMillis(),
            description = "Register Jane Doe"
        )
        
        offlineHandler.addPendingOperation(pendingCheckIn)
        offlineHandler.addPendingOperation(pendingRegistration)
        
        assertEquals(2, offlineHandler.pendingOperations.value.size)
        
        // Simulate going back online
        offlineHandler.setOfflineStatus(false)
        
        val reconnectionMessage = offlineHandler.getReconnectionMessage()
        assertTrue(reconnectionMessage.contains("2 pending operations"))
    }
}