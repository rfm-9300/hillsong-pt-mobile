package rfm.hillsongptapp.feature.kids.data.network.mapper

import rfm.hillsongptapp.feature.kids.data.network.dto.CheckInRecordDto
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord

/**
 * Extension function to convert CheckInRecordDto to domain CheckInRecord model
 */
fun CheckInRecordDto.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status.toCheckInStatus()
    )
}

/**
 * Extension function to convert domain CheckInRecord model to CheckInRecordDto
 */
fun CheckInRecord.toDto(): CheckInRecordDto {
    return CheckInRecordDto(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status.toApiString()
    )
}

/**
 * Extension function to convert list of CheckInRecordDto to list of domain CheckInRecord models
 */
fun List<CheckInRecordDto>.toDomain(): List<CheckInRecord> {
    return map { it.toDomain() }
}

