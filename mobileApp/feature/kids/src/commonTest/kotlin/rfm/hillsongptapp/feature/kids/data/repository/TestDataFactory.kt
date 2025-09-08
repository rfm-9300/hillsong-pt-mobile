package rfm.hillsongptapp.feature.kids.data.repository

import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.domain.model.*

/**
 * Factory for creating test data objects
 */
object TestDataFactory {
    
    fun createTestChild(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        dateOfBirth: String = "2020-01-01"
    ): Child {
        return Child(
            id = id,
            parentId = parentId,
            name = "Test Child $id",
            dateOfBirth = dateOfBirth,
            emergencyContact = EmergencyContact("Emergency Contact", "123-456-7890", "Parent"),
            status = status,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestChildEntity(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE,
        dateOfBirth: String = "2020-01-01"
    ): ChildEntity {
        return ChildEntity(
            id = id,
            parentId = parentId,
            name = "Test Child $id",
            dateOfBirth = dateOfBirth,
            emergencyContactName = "Emergency Contact",
            emergencyContactPhone = "123-456-7890",
            emergencyContactRelationship = "Parent",
            status = status.name,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestChildDto(
        id: String,
        parentId: String,
        status: CheckInStatus = CheckInStatus.NOT_IN_SERVICE
    ): ChildDto {
        return ChildDto(
            id = id,
            parentId = parentId,
            name = "Test Child $id",
            dateOfBirth = "2020-01-01",
            emergencyContact = EmergencyContactDto("Emergency Contact", "123-456-7890", "Parent"),
            status = status.name,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestKidsService(
        id: String,
        minAge: Int = 3,
        maxAge: Int = 12,
        currentCapacity: Int = 0,
        maxCapacity: Int = 20
    ): KidsService {
        return KidsService(
            id = id,
            name = "Test Service $id",
            description = "Test Description",
            minAge = minAge,
            maxAge = maxAge,
            startTime = "09:00:00",
            endTime = "11:00:00",
            location = "Room 1",
            maxCapacity = maxCapacity,
            currentCapacity = currentCapacity,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff1", "staff2"),
            createdAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestServiceEntity(
        id: String,
        minAge: Int = 3,
        maxAge: Int = 12,
        currentCapacity: Int = 0,
        maxCapacity: Int = 20
    ): KidsServiceEntity {
        return KidsServiceEntity(
            id = id,
            name = "Test Service $id",
            description = "Test Description",
            minAge = minAge,
            maxAge = maxAge,
            startTime = "09:00:00",
            endTime = "11:00:00",
            location = "Room 1",
            maxCapacity = maxCapacity,
            currentCapacity = currentCapacity,
            isAcceptingCheckIns = true,
            staffMembers = """["staff1", "staff2"]""",
            createdAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestServiceDto(
        id: String,
        currentCapacity: Int = 0
    ): KidsServiceDto {
        return KidsServiceDto(
            id = id,
            name = "Test Service $id",
            description = "Test Description",
            minAge = 3,
            maxAge = 12,
            startTime = "09:00:00",
            endTime = "11:00:00",
            location = "Room 1",
            maxCapacity = 20,
            currentCapacity = currentCapacity,
            isAcceptingCheckIns = true,
            staffMembers = listOf("staff1", "staff2"),
            createdAt = "2025-01-01T00:00:00Z"
        )
    }
    
    fun createTestCheckInRecord(
        id: String,
        childId: String,
        serviceId: String,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ): CheckInRecord {
        return CheckInRecord(
            id = id,
            childId = childId,
            serviceId = serviceId,
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = if (status == CheckInStatus.CHECKED_OUT) "2025-01-01T12:00:00Z" else null,
            checkedInBy = "parent123",
            checkedOutBy = if (status == CheckInStatus.CHECKED_OUT) "parent123" else null,
            status = status
        )
    }
    
    fun createTestCheckInRecordEntity(
        id: String,
        childId: String,
        serviceId: String,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ): CheckInRecordEntity {
        return CheckInRecordEntity(
            id = id,
            childId = childId,
            serviceId = serviceId,
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = if (status == CheckInStatus.CHECKED_OUT) "2025-01-01T12:00:00Z" else null,
            checkedInBy = "parent123",
            checkedOutBy = if (status == CheckInStatus.CHECKED_OUT) "parent123" else null,
            status = status.name
        )
    }
    
    fun createTestCheckInRecordDto(
        id: String,
        childId: String,
        serviceId: String,
        status: CheckInStatus = CheckInStatus.CHECKED_IN
    ): CheckInRecordDto {
        return CheckInRecordDto(
            id = id,
            childId = childId,
            serviceId = serviceId,
            checkInTime = "2025-01-01T10:00:00Z",
            checkOutTime = if (status == CheckInStatus.CHECKED_OUT) "2025-01-01T12:00:00Z" else null,
            checkedInBy = "parent123",
            checkedOutBy = if (status == CheckInStatus.CHECKED_OUT) "parent123" else null,
            status = status.name
        )
    }
    
    fun createTestEmergencyContact(): EmergencyContact {
        return EmergencyContact(
            name = "Emergency Contact",
            phoneNumber = "123-456-7890",
            relationship = "Parent"
        )
    }
    
    fun createTestEmergencyContactDto(): EmergencyContactDto {
        return EmergencyContactDto(
            name = "Emergency Contact",
            phoneNumber = "123-456-7890",
            relationship = "Parent"
        )
    }
}