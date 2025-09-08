package rfm.hillsongptapp.feature.kids.data.network.mapper

import rfm.hillsongptapp.feature.kids.data.network.dto.ChildDto
import rfm.hillsongptapp.feature.kids.data.network.dto.ChildRegistrationRequest
import rfm.hillsongptapp.feature.kids.data.network.dto.ChildUpdateRequest
import rfm.hillsongptapp.feature.kids.data.network.dto.EmergencyContactDto
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact

/**
 * Extension function to convert ChildDto to domain Child model
 */
fun ChildDto.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDomain(),
        status = status.toCheckInStatus(),
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert domain Child model to ChildDto
 */
fun Child.toDto(): ChildDto {
    return ChildDto(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDto(),
        status = status.toApiString(),
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert EmergencyContactDto to domain EmergencyContact model
 */
fun EmergencyContactDto.toDomain(): EmergencyContact {
    return EmergencyContact(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

/**
 * Extension function to convert domain EmergencyContact model to EmergencyContactDto
 */
fun EmergencyContact.toDto(): EmergencyContactDto {
    return EmergencyContactDto(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

/**
 * Extension function to convert domain Child model to ChildRegistrationRequest
 */
fun Child.toRegistrationRequest(): ChildRegistrationRequest {
    return ChildRegistrationRequest(
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDto()
    )
}

/**
 * Extension function to convert domain Child model to ChildUpdateRequest
 */
fun Child.toUpdateRequest(): ChildUpdateRequest {
    return ChildUpdateRequest(
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDto()
    )
}

/**
 * Extension function to convert string status to CheckInStatus enum
 */
fun String.toCheckInStatus(): CheckInStatus {
    return when (this.uppercase()) {
        "CHECKED_IN" -> CheckInStatus.CHECKED_IN
        "CHECKED_OUT" -> CheckInStatus.CHECKED_OUT
        "NOT_IN_SERVICE" -> CheckInStatus.NOT_IN_SERVICE
        else -> CheckInStatus.NOT_IN_SERVICE // Default fallback
    }
}

/**
 * Extension function to convert CheckInStatus enum to API string
 */
fun CheckInStatus.toApiString(): String {
    return when (this) {
        CheckInStatus.CHECKED_IN -> "CHECKED_IN"
        CheckInStatus.CHECKED_OUT -> "CHECKED_OUT"
        CheckInStatus.NOT_IN_SERVICE -> "NOT_IN_SERVICE"
    }
}

/**
 * Extension function to convert list of ChildDto to list of domain Child models
 */
fun List<ChildDto>.toDomain(): List<Child> {
    return map { it.toDomain() }
}

/**
 * Extension function to convert list of domain Child models to list of ChildDto
 */
fun List<Child>.toDto(): List<ChildDto> {
    return map { it.toDto() }
}