package rfm.com.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service as SpringService
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.entity.KidsService
import rfm.com.repository.*
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@SpringService
@Transactional
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val serviceRepository: ServiceRepository,
    private val kidsServiceRepository: KidsServiceRepository
) {

    /**
     * Check in a user to an event, service, or kids service
     */
    fun checkIn(userId: Long, request: CheckInRequest): AttendanceResponse {
        val user = userRepository.findById(userId).getOrNull()
            ?: throw IllegalArgumentException("User not found with ID: $userId")

        // Validate that the target entity exists
        val (event, serviceEntity, kidsService) = when (request.attendanceType) {
            AttendanceType.EVENT -> {
                val event = eventRepository.findById(request.eventId!!).getOrNull()
                    ?: throw IllegalArgumentException("Event not found with ID: ${request.eventId}")
                Triple(event, null, null)
            }
            AttendanceType.SERVICE -> {
                val serviceEntity = serviceRepository.findById(request.serviceId!!).getOrNull()
                    ?: throw IllegalArgumentException("Service not found with ID: ${request.serviceId}")
                Triple(null, serviceEntity, null)
            }
            AttendanceType.KIDS_SERVICE -> {
                val kidsService = kidsServiceRepository.findById(request.kidsServiceId!!).getOrNull()
                    ?: throw IllegalArgumentException("Kids service not found with ID: ${request.kidsServiceId}")
                Triple(null, null, kidsService)
            }
        }

        // Check if user is already checked in for this entity today
        val existingAttendance = findExistingTodayAttendance(user, event, serviceEntity, kidsService)
        if (existingAttendance != null && existingAttendance.status == AttendanceStatus.CHECKED_IN) {
            throw IllegalStateException("User is already checked in for this ${request.attendanceType.name.lowercase()}")
        }

        // Create new attendance record
        val attendance = Attendance(
            user = user,
            event = event,
            service = serviceEntity,
            kidsService = kidsService,
            attendanceType = request.attendanceType,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            notes = request.notes,
            checkedInBy = request.checkedInBy
        )

        val savedAttendance = attendanceRepository.save(attendance)
        return mapToAttendanceResponse(savedAttendance)
    }

    /**
     * Check out a user from their attendance record
     */
    fun checkOut(userId: Long, request: CheckOutRequest): AttendanceResponse {
        val attendance = attendanceRepository.findByIdWithAllRelationships(request.attendanceId)
            ?: throw IllegalArgumentException("Attendance record not found with ID: ${request.attendanceId}")

        // Verify the attendance belongs to the user
        if (attendance.user.id != userId) {
            throw IllegalArgumentException("Attendance record does not belong to the specified user")
        }

        // Check if already checked out
        if (attendance.status == AttendanceStatus.CHECKED_OUT) {
            throw IllegalStateException("User is already checked out")
        }

        // Update attendance record
        val updatedAttendance = attendance.checkOut(
            checkOutTime = LocalDateTime.now(),
            checkedOutBy = request.checkedOutBy
        )

        val savedAttendance = attendanceRepository.save(updatedAttendance)
        return mapToAttendanceResponse(savedAttendance)
    }

    /**
     * Get attendance records for a specific user
     */
    @Transactional(readOnly = true)
    fun getUserAttendance(userId: Long, pageable: Pageable): Page<AttendanceSummaryResponse> {
        val user = userRepository.findById(userId).getOrNull()
            ?: throw IllegalArgumentException("User not found with ID: $userId")

        return attendanceRepository.findByUser(user, pageable)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific event
     */
    @Transactional(readOnly = true)
    fun getEventAttendance(eventId: Long): List<AttendanceSummaryResponse> {
        val event = eventRepository.findById(eventId).getOrNull()
            ?: throw IllegalArgumentException("Event not found with ID: $eventId")

        return attendanceRepository.findByEvent(event)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific service
     */
    @Transactional(readOnly = true)
    fun getServiceAttendance(serviceId: Long): List<AttendanceSummaryResponse> {
        val serviceEntity = serviceRepository.findById(serviceId).getOrNull()
            ?: throw IllegalArgumentException("Service not found with ID: $serviceId")

        return attendanceRepository.findByService(serviceEntity)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific kids service
     */
    @Transactional(readOnly = true)
    fun getKidsServiceAttendance(kidsServiceId: Long): List<AttendanceSummaryResponse> {
        val kidsService = kidsServiceRepository.findById(kidsServiceId).getOrNull()
            ?: throw IllegalArgumentException("Kids service not found with ID: $kidsServiceId")

        return attendanceRepository.findByKidsService(kidsService)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get currently checked-in users
     */
    @Transactional(readOnly = true)
    fun getCurrentlyCheckedIn(): List<AttendanceSummaryResponse> {
        return attendanceRepository.findCurrentlyCheckedIn()
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance statistics for a date range
     */
    @Transactional(readOnly = true)
    fun getAttendanceStats(request: AttendanceReportRequest): AttendanceStatsResponse {
        val attendanceRecords = when {
            request.userId != null -> {
                val user = userRepository.findById(request.userId).getOrNull()
                    ?: throw IllegalArgumentException("User not found with ID: ${request.userId}")
                attendanceRepository.findByUserAndCheckInTimeBetween(user, request.startDate, request.endDate)
            }
            request.eventId != null -> {
                val event = eventRepository.findById(request.eventId).getOrNull()
                    ?: throw IllegalArgumentException("Event not found with ID: ${request.eventId}")
                attendanceRepository.findByEventAndCheckInTimeBetween(event, request.startDate, request.endDate)
            }
            else -> {
                attendanceRepository.findByCheckInTimeBetween(request.startDate, request.endDate)
            }
        }

        // Filter by type and status if specified
        val filteredRecords = attendanceRecords.filter { attendance ->
            (request.attendanceType == null || attendance.attendanceType == request.attendanceType) &&
            (request.status == null || attendance.status == request.status)
        }

        val totalAttendance = filteredRecords.size.toLong()
        val checkedInCount = filteredRecords.count { it.status == AttendanceStatus.CHECKED_IN }.toLong()
        val checkedOutCount = filteredRecords.count { it.status == AttendanceStatus.CHECKED_OUT }.toLong()
        val noShowCount = filteredRecords.count { it.status == AttendanceStatus.NO_SHOW }.toLong()
        val cancelledCount = filteredRecords.count { it.status == AttendanceStatus.CANCELLED }.toLong()

        val attendanceByType = AttendanceType.values().associateWith { type ->
            filteredRecords.count { it.attendanceType == type }.toLong()
        }

        val attendanceByStatus = AttendanceStatus.values().associateWith { status ->
            filteredRecords.count { it.status == status }.toLong()
        }

        val averageDuration = filteredRecords
            .mapNotNull { it.duration }
            .takeIf { it.isNotEmpty() }
            ?.average()

        return AttendanceStatsResponse(
            totalAttendance = totalAttendance,
            checkedInCount = checkedInCount,
            checkedOutCount = checkedOutCount,
            noShowCount = noShowCount,
            cancelledCount = cancelledCount,
            averageDuration = averageDuration,
            attendanceByType = attendanceByType,
            attendanceByStatus = attendanceByStatus,
            dateRange = DateRangeResponse(request.startDate, request.endDate)
        )
    }

    /**
     * Get most frequent attendees
     */
    @Transactional(readOnly = true)
    fun getMostFrequentAttendees(pageable: Pageable): Page<FrequentAttendeesResponse> {
        return attendanceRepository.findMostFrequentAttendees(pageable)
            .map { result ->
                val user = result[0] as User
                val count = result[1] as Long
                
                // Get last attendance for this user
                val lastAttendance = attendanceRepository.findByUser(user)
                    .maxByOrNull { it.checkInTime }
                    ?.checkInTime

                FrequentAttendeesResponse(
                    user = mapToUserResponse(user),
                    attendanceCount = count,
                    lastAttendance = lastAttendance
                )
            }
    }

    /**
     * Bulk check-in multiple users
     */
    fun bulkCheckIn(request: BulkCheckInRequest): BulkAttendanceResponse {
        val successful = mutableListOf<AttendanceResponse>()
        val failed = mutableListOf<BulkAttendanceError>()

        request.userIds.forEach { userId ->
            try {
                val checkInRequest = CheckInRequest(
                    attendanceType = request.attendanceType,
                    eventId = request.eventId,
                    serviceId = request.serviceId,
                    kidsServiceId = request.kidsServiceId,
                    notes = request.notes,
                    checkedInBy = request.checkedInBy
                )
                val attendance = checkIn(userId, checkInRequest)
                successful.add(attendance)
            } catch (e: Exception) {
                failed.add(BulkAttendanceError(userId, e.message ?: "Unknown error"))
            }
        }

        return BulkAttendanceResponse(
            successful = successful,
            failed = failed,
            totalProcessed = request.userIds.size,
            successCount = successful.size,
            failureCount = failed.size
        )
    }

    /**
     * Update attendance status (for admin operations)
     */
    fun updateAttendanceStatus(attendanceId: Long, status: AttendanceStatus, notes: String? = null): AttendanceResponse {
        val attendance = attendanceRepository.findByIdWithAllRelationships(attendanceId)
            ?: throw IllegalArgumentException("Attendance record not found with ID: $attendanceId")

        val updatedAttendance = when (status) {
            AttendanceStatus.CHECKED_OUT -> {
                if (attendance.checkOutTime == null) {
                    attendance.copy(
                        status = status,
                        checkOutTime = LocalDateTime.now(),
                        notes = notes ?: attendance.notes
                    )
                } else {
                    attendance.copy(status = status, notes = notes ?: attendance.notes)
                }
            }
            else -> attendance.copy(status = status, notes = notes ?: attendance.notes)
        }

        val savedAttendance = attendanceRepository.save(updatedAttendance)
        return mapToAttendanceResponse(savedAttendance)
    }

    // Helper methods

    private fun findExistingTodayAttendance(
        user: User, 
        event: Event?, 
        serviceEntity: rfm.com.entity.Service?, 
        kidsService: rfm.com.entity.KidsService?
    ): Attendance? {
        val today = LocalDateTime.now().toLocalDate()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        return when {
            event != null -> attendanceRepository.findByUserAndEvent(user, event)
            serviceEntity != null -> attendanceRepository.findByUserAndService(user, serviceEntity)
            kidsService != null -> attendanceRepository.findByUserAndKidsService(user, kidsService)
            else -> null
        }?.firstOrNull { attendance ->
            attendance.checkInTime.isAfter(startOfDay) && attendance.checkInTime.isBefore(endOfDay)
        }
    }

    private fun mapToAttendanceResponse(attendance: Attendance): AttendanceResponse {
        return AttendanceResponse(
            id = attendance.id!!,
            user = mapToUserResponse(attendance.user),
            attendanceType = attendance.attendanceType,
            status = attendance.status,
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime,
            notes = attendance.notes,
            checkedInBy = attendance.checkedInBy,
            checkedOutBy = attendance.checkedOutBy,
            duration = attendance.duration,
            isCheckedOut = attendance.isCheckedOut,
            event = attendance.event?.let { mapToEventSummaryResponse(it) },
            service = attendance.service?.let { mapToServiceResponse(it) },
            kidsService = attendance.kidsService?.let { mapToKidsServiceResponse(it) }
        )
    }

    private fun mapToAttendanceSummaryResponse(attendance: Attendance): AttendanceSummaryResponse {
        val (entityName, entityId) = when (attendance.attendanceType) {
            AttendanceType.EVENT -> Pair(attendance.event?.title ?: "Unknown Event", attendance.event?.id ?: 0L)
            AttendanceType.SERVICE -> Pair(attendance.service?.name ?: "Unknown Service", attendance.service?.id ?: 0L)
            AttendanceType.KIDS_SERVICE -> Pair(attendance.kidsService?.name ?: "Unknown Kids Service", attendance.kidsService?.id ?: 0L)
        }

        return AttendanceSummaryResponse(
            id = attendance.id!!,
            userId = attendance.user.id!!,
            userName = "${attendance.user.profile?.firstName ?: ""} ${attendance.user.profile?.lastName ?: ""}".trim(),
            attendanceType = attendance.attendanceType,
            status = attendance.status,
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime,
            duration = attendance.duration,
            isCheckedOut = attendance.isCheckedOut,
            entityName = entityName,
            entityId = entityId
        )
    }

    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            email = user.email,
            firstName = user.profile?.firstName ?: "",
            lastName = user.profile?.lastName ?: "",
            verified = user.verified,
            createdAt = user.createdAt,
            authProvider = user.authProvider.name
        )
    }

    private fun mapToEventSummaryResponse(event: Event): EventSummaryResponse {
        return EventSummaryResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = "${event.organizer.firstName} ${event.organizer.lastName}",
            organizerId = event.organizer.id!!,
            attendeeCount = event.attendeeCount,
            maxAttendees = event.maxAttendees,
            availableSpots = event.availableSpots,
            headerImagePath = event.headerImagePath,
            needsApproval = event.needsApproval,
            isAtCapacity = event.isAtCapacity,
            createdAt = event.createdAt
        )
    }

    private fun mapToServiceResponse(serviceEntity: rfm.com.entity.Service): ServiceResponse {
        return ServiceResponse(
            id = serviceEntity.id!!,
            name = serviceEntity.name,
            serviceType = serviceEntity.serviceType.name,
            dayOfWeek = serviceEntity.dayOfWeek.name,
            startTime = serviceEntity.startTime.toString(),
            endTime = serviceEntity.endTime.toString(),
            location = serviceEntity.location,
            leaderName = serviceEntity.leader?.let { "${it.firstName} ${it.lastName}" },
            maxCapacity = serviceEntity.maxCapacity,
            isActive = serviceEntity.isActive
        )
    }

    private fun mapToKidsServiceResponse(kidsService: KidsService): KidsServiceResponse {
        return KidsServiceResponse(
            id = kidsService.id!!,
            name = kidsService.name,
            dayOfWeek = kidsService.dayOfWeek.name,
            serviceDate = kidsService.serviceDate,
            startTime = kidsService.startTime.toString(),
            endTime = kidsService.endTime.toString(),
            location = kidsService.location,
            leaderName = kidsService.leader?.let { "${it.firstName} ${it.lastName}" },
            maxCapacity = kidsService.maxCapacity,
            minAge = kidsService.minAge,
            maxAge = kidsService.maxAge,
            ageGroups = kidsService.ageGroups.map { it.name },
            isActive = kidsService.isActive
        )
    }

    /**
     * Get attendance records by type
     */
    @Transactional(readOnly = true)
    fun getAttendanceByType(type: AttendanceType): List<AttendanceSummaryResponse> {
        return attendanceRepository.findByAttendanceType(type)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records by status
     */
    @Transactional(readOnly = true)
    fun getAttendanceByStatus(status: AttendanceStatus): List<AttendanceSummaryResponse> {
        return attendanceRepository.findByStatus(status)
            .map { mapToAttendanceSummaryResponse(it) }
    }
}