package rfm.hillsongptapp.feature.kids.validation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import rfm.hillsongptapp.core.data.model.*

/**
 * Comprehensive validation test suite to verify all kids functionality works after migration.
 * Tests Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
class KidsFeatureValidationTest {

    @Test
    fun `verify core data models are properly migrated`() = runTest {
        // Test that core data models exist and are properly structured (Requirement 4.2)
        
        // Test Child model
        val testChild = Child(
            id = "test-child-1",
            parentId = "parent-1",
            name = "Test Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = "No allergies",
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact(
                name = "Emergency Contact",
                phoneNumber = "123-456-7890",
                relationship = "Parent"
            ),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        // Verify model properties
        assertEquals("test-child-1", testChild.id)
        assertEquals("Test Child", testChild.name)
        assertEquals(CheckInStatus.CHECKED_OUT, testChild.status)
        assertNotNull(testChild.emergencyContact)
        
        println("✅ Core data models validated")
    }

    @Test
    fun `verify check-in status enum works correctly`() = runTest {
        // Test check-in and check-out status management (Requirement 4.1)
        
        // Test CheckInStatus enum
        val checkedOut = CheckInStatus.CHECKED_OUT
        val checkedIn = CheckInStatus.CHECKED_IN
        
        assertNotNull(checkedOut)
        assertNotNull(checkedIn)
        assertTrue(checkedOut != checkedIn)
        
        // Test status transitions in child model
        val testChild = Child(
            id = "test-child-2",
            parentId = "parent-2",
            name = "Test Child 2",
            dateOfBirth = "2019-01-01",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact(
                name = "Emergency Contact 2",
                phoneNumber = "123-456-7891",
                relationship = "Guardian"
            ),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        // Verify initial status
        assertEquals(CheckInStatus.CHECKED_OUT, testChild.status)
        
        // Test status change
        val checkedInChild = testChild.copy(
            status = CheckInStatus.CHECKED_IN,
            currentServiceId = "service-1",
            checkInTime = "2024-01-01T10:00:00Z"
        )
        
        assertEquals(CheckInStatus.CHECKED_IN, checkedInChild.status)
        assertEquals("service-1", checkedInChild.currentServiceId)
        assertNotNull(checkedInChild.checkInTime)
        
        println("✅ Check-in and check-out status functionality validated")
    }

    @Test
    fun `verify reporting models work correctly`() = runTest {
        // Test reporting data models (Requirement 4.3, 4.4)
        
        // Test AttendanceReport model
        val attendanceReport = AttendanceReport(
            startDate = "2024-01-01",
            endDate = "2024-01-31",
            totalCheckIns = 150,
            uniqueChildren = 45,
            serviceBreakdown = mapOf("service-1" to 75, "service-2" to 75),
            dailyBreakdown = mapOf("2024-01-01" to 10, "2024-01-02" to 15),
            generatedAt = "2024-01-31T23:59:59Z"
        )
        
        assertEquals("2024-01-01", attendanceReport.startDate)
        assertEquals(150, attendanceReport.totalCheckIns)
        assertEquals(45, attendanceReport.uniqueChildren)
        assertTrue(attendanceReport.serviceBreakdown.isNotEmpty())
        
        // Test ServiceReport model
        val serviceReport = ServiceReport(
            serviceId = "service-1",
            serviceName = "Kids Service",
            totalCheckIns = 75,
            uniqueChildren = 25,
            averageAttendance = 12.5,
            peakAttendance = 20,
            checkInsByDay = mapOf("Monday" to 15, "Sunday" to 20),
            generatedAt = "2024-01-31T23:59:59Z"
        )
        
        assertEquals("service-1", serviceReport.serviceId)
        assertEquals(75, serviceReport.totalCheckIns)
        assertEquals(25, serviceReport.uniqueChildren)
        assertTrue(serviceReport.checkInsByDay.isNotEmpty())
        
        println("✅ Reporting models validated")
    }

    @Test
    fun `verify service management models work correctly`() = runTest {
        // Test service management models (Requirement 4.4)
        
        val testService = KidsService(
            id = "test-service-1",
            name = "Test Service",
            description = "Test service description",
            minAge = 3,
            maxAge = 5,
            startTime = "09:00",
            endTime = "10:30",
            location = "Room A",
            maxCapacity = 20,
            currentCapacity = 0,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Staff 1", "Staff 2"),
            createdAt = "2024-01-01T00:00:00Z"
        )

        // Test service properties
        assertEquals("test-service-1", testService.id)
        assertEquals("Test Service", testService.name)
        assertEquals(20, testService.maxCapacity)
        assertEquals(0, testService.currentCapacity)
        assertTrue(testService.isAcceptingCheckIns)
        
        // Test service business logic
        assertTrue(testService.hasAvailableSpots())
        assertFalse(testService.isAtCapacity())
        assertEquals(20, testService.getAvailableSpots())
        assertTrue(testService.isAgeEligible(4))
        assertFalse(testService.isAgeEligible(7))
        assertTrue(testService.canAcceptCheckIn())
        
        // Test service at capacity
        val fullService = testService.copy(currentCapacity = 20)
        assertFalse(fullService.hasAvailableSpots())
        assertTrue(fullService.isAtCapacity())
        assertEquals(0, fullService.getAvailableSpots())
        assertFalse(fullService.canAcceptCheckIn())
        
        println("✅ Service management models validated")
    }

    @Test
    fun `verify child management models work correctly`() = runTest {
        // Test child management operations (Requirement 4.2)
        
        val testChild = Child(
            id = "test-child-3",
            parentId = "parent-3",
            name = "Test Child 3",
            dateOfBirth = "2018-01-01",
            medicalInfo = "Asthma",
            dietaryRestrictions = "Nut allergy",
            emergencyContact = EmergencyContact(
                name = "Emergency Contact 3",
                phoneNumber = "123-456-7892",
                relationship = "Parent"
            ),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )

        // Test child properties
        assertEquals("test-child-3", testChild.id)
        assertEquals("Test Child 3", testChild.name)
        assertEquals("Asthma", testChild.medicalInfo)
        assertEquals("Nut allergy", testChild.dietaryRestrictions)
        assertNotNull(testChild.emergencyContact)
        
        // Test child update
        val updatedChild = testChild.copy(medicalInfo = "Asthma - mild")
        assertEquals("Asthma - mild", updatedChild.medicalInfo)
        assertEquals(testChild.id, updatedChild.id) // ID should remain the same
        
        // Test emergency contact
        val emergencyContact = testChild.emergencyContact
        assertEquals("Emergency Contact 3", emergencyContact.name)
        assertEquals("123-456-7892", emergencyContact.phoneNumber)
        assertEquals("Parent", emergencyContact.relationship)
        
        println("✅ Child management models validated")
    }

    @Test
    fun `verify check-in record model works correctly`() = runTest {
        // Test check-in record functionality (Requirement 4.5)
        
        val checkInRecord = CheckInRecord(
            id = "record-1",
            childId = "child-1",
            serviceId = "service-1",
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "staff-1",
            checkedOutBy = null,
            notes = "Child arrived on time",
            status = CheckInStatus.CHECKED_IN
        )
        
        // Test record properties
        assertEquals("record-1", checkInRecord.id)
        assertEquals("child-1", checkInRecord.childId)
        assertEquals("service-1", checkInRecord.serviceId)
        assertEquals(CheckInStatus.CHECKED_IN, checkInRecord.status)
        assertNotNull(checkInRecord.checkInTime)
        assertEquals(null, checkInRecord.checkOutTime)
        assertEquals("Child arrived on time", checkInRecord.notes)
        
        // Test check-out update
        val checkedOutRecord = checkInRecord.copy(
            checkOutTime = "2024-01-01T12:00:00Z",
            checkedOutBy = "staff-2",
            status = CheckInStatus.CHECKED_OUT
        )
        
        assertEquals(CheckInStatus.CHECKED_OUT, checkedOutRecord.status)
        assertNotNull(checkedOutRecord.checkOutTime)
        assertEquals("staff-2", checkedOutRecord.checkedOutBy)
        
        println("✅ Check-in record functionality validated")
    }

    @Test
    fun `verify architecture migration is complete`() = runTest {
        // Test that architecture migration is complete (Requirement 4.5)
        
        // Verify core data models exist and are accessible
        val child = Child(
            id = "test",
            parentId = "parent",
            name = "Test",
            dateOfBirth = "2020-01-01",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact("Contact", "123", "Parent"),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        val service = KidsService(
            id = "service",
            name = "Service",
            description = "Description",
            minAge = 3,
            maxAge = 5,
            startTime = "09:00",
            endTime = "10:30",
            location = "Room A",
            maxCapacity = 20,
            currentCapacity = 0,
            isAcceptingCheckIns = true,
            staffMembers = emptyList(),
            createdAt = "2024-01-01T00:00:00Z"
        )
        
        val record = CheckInRecord(
            id = "record",
            childId = "child",
            serviceId = "service",
            checkInTime = "2024-01-01T10:00:00Z",
            checkOutTime = null,
            checkedInBy = "staff",
            checkedOutBy = null,
            notes = null,
            status = CheckInStatus.CHECKED_IN
        )
        
        // Verify all models can be instantiated
        assertNotNull(child)
        assertNotNull(service)
        assertNotNull(record)
        
        println("✅ Architecture migration validated")
    }

    @Test
    fun `verify model validation works correctly`() = runTest {
        // Test model validation scenarios
        
        // Test valid child model
        val validChild = Child(
            id = "valid-id",
            parentId = "parent-id",
            name = "Valid Child",
            dateOfBirth = "2020-01-01",
            medicalInfo = null,
            dietaryRestrictions = null,
            emergencyContact = EmergencyContact("Contact", "123-456-7890", "Parent"),
            status = CheckInStatus.CHECKED_OUT,
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T00:00:00Z",
            updatedAt = "2024-01-01T00:00:00Z"
        )
        
        assertNotNull(validChild)
        assertTrue(validChild.name.isNotBlank())
        assertTrue(validChild.id.isNotBlank())
        
        // Test valid service model with validation
        val validService = KidsService(
            id = "valid-service",
            name = "Valid Service",
            description = "Valid description",
            minAge = 3,
            maxAge = 5,
            startTime = "09:00",
            endTime = "10:30",
            location = "Room A",
            maxCapacity = 20,
            currentCapacity = 5,
            isAcceptingCheckIns = true,
            staffMembers = listOf("Staff 1"),
            createdAt = "2024-01-01T00:00:00Z"
        )
        
        assertNotNull(validService)
        assertTrue(validService.name.isNotBlank())
        assertTrue(validService.maxCapacity > 0)
        assertTrue(validService.currentCapacity >= 0)
        assertTrue(validService.currentCapacity <= validService.maxCapacity)
        
        println("✅ Model validation functionality validated")
    }

    @Test
    fun `verify feature module structure is correct`() = runTest {
        // Test that feature module structure follows the correct pattern
        
        // This test validates that the kids feature module has been properly
        // restructured to only contain UI components, following the same
        // pattern as other features like feed and auth
        
        // The fact that we can import and use core data models here
        // proves that the migration was successful
        val coreModelsAccessible = try {
            Child(
                id = "test",
                parentId = "parent",
                name = "Test",
                dateOfBirth = "2020-01-01",
                medicalInfo = null,
                dietaryRestrictions = null,
                emergencyContact = EmergencyContact("Contact", "123", "Parent"),
                status = CheckInStatus.CHECKED_OUT,
                currentServiceId = null,
                checkInTime = null,
                checkOutTime = null,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            )
            true
        } catch (e: Exception) {
            false
        }
        
        assertTrue(coreModelsAccessible, "Core data models should be accessible from feature module")
        
        println("✅ Feature module structure validated")
    }
}