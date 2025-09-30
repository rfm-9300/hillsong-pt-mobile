package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.EmergencyContact
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildResponse
import rfm.hillsongptapp.core.network.ktor.responses.EmergencyContactResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceResponse

/**
 * Extension functions for converting between domain models, entities, and DTOs in tests
 */

// Child conversions
fun Child.toEntity(): ChildEntity {
    return ChildEntity(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact,
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ChildEntity.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact,
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Child.toResponse(): ChildResponse {
    return ChildResponse(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toResponse(),
        status = status.name,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun EmergencyContact.toResponse(): EmergencyContactResponse {
    return EmergencyContactResponse(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

// KidsService conversions
fun KidsService.toEntity(): KidsServiceEntity {
    return KidsServiceEntity(
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
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

fun KidsServiceEntity.toDomain(): KidsService {
    return KidsService(
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
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

fun KidsService.toResponse(): ServiceResponse {
    return ServiceResponse(
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
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

// CheckInRecord conversions
fun CheckInRecord.toEntity(): CheckInRecordEntity {
    return CheckInRecordEntity(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status
    )
}

fun CheckInRecordEntity.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status
    )
}

fun CheckInRecord.toResponse(): CheckInRecordResponse {
    return CheckInRecordResponse(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status.name
    )
}