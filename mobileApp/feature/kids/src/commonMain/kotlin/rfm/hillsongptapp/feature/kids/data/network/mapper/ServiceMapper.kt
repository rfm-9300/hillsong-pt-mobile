package rfm.hillsongptapp.feature.kids.data.network.mapper

import rfm.hillsongptapp.feature.kids.data.network.dto.KidsServiceDto
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

/**
 * Extension function to convert KidsServiceDto to domain KidsService model
 */
fun KidsServiceDto.toDomain(): KidsService {
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

/**
 * Extension function to convert domain KidsService model to KidsServiceDto
 */
fun KidsService.toDto(): KidsServiceDto {
    return KidsServiceDto(
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

/**
 * Extension function to convert list of KidsServiceDto to list of domain KidsService models
 */
fun List<KidsServiceDto>.toDomain(): List<KidsService> {
    return map { it.toDomain() }
}

/**
 * Extension function to convert list of domain KidsService models to list of KidsServiceDto
 */
fun List<KidsService>.toDto(): List<KidsServiceDto> {
    return map { it.toDto() }
}