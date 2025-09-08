package rfm.hillsongptapp.feature.kids.data.network.mapper

import rfm.hillsongptapp.feature.kids.data.network.dto.AttendanceReportDto
import rfm.hillsongptapp.feature.kids.data.network.dto.ServiceReportDto
import rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport
import rfm.hillsongptapp.feature.kids.domain.model.ServiceReport

/**
 * Extension function to convert ServiceReportDto to domain ServiceReport model
 */
fun ServiceReportDto.toDomain(): ServiceReport {
    return ServiceReport(
        serviceId = serviceId,
        serviceName = serviceName,
        totalCapacity = totalCapacity,
        currentCheckIns = currentCheckIns,
        availableSpots = availableSpots,
        checkedInChildren = checkedInChildren.toDomain(),
        staffMembers = staffMembers,
        generatedAt = generatedAt
    )
}

/**
 * Extension function to convert domain ServiceReport model to ServiceReportDto
 */
fun ServiceReport.toDto(): ServiceReportDto {
    return ServiceReportDto(
        serviceId = serviceId,
        serviceName = serviceName,
        totalCapacity = totalCapacity,
        currentCheckIns = currentCheckIns,
        availableSpots = availableSpots,
        checkedInChildren = checkedInChildren.toDto(),
        staffMembers = staffMembers,
        generatedAt = generatedAt
    )
}

/**
 * Extension function to convert AttendanceReportDto to domain AttendanceReport model
 */
fun AttendanceReportDto.toDomain(): AttendanceReport {
    return AttendanceReport(
        startDate = startDate,
        endDate = endDate,
        totalCheckIns = totalCheckIns,
        uniqueChildren = uniqueChildren,
        serviceBreakdown = serviceBreakdown,
        dailyBreakdown = dailyBreakdown,
        generatedAt = generatedAt
    )
}

/**
 * Extension function to convert domain AttendanceReport model to AttendanceReportDto
 */
fun AttendanceReport.toDto(): AttendanceReportDto {
    return AttendanceReportDto(
        startDate = startDate,
        endDate = endDate,
        totalCheckIns = totalCheckIns,
        uniqueChildren = uniqueChildren,
        serviceBreakdown = serviceBreakdown,
        dailyBreakdown = dailyBreakdown,
        generatedAt = generatedAt
    )
}