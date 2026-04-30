package rfm.com.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service as SpringService
import rfm.com.dto.*
import rfm.com.dto.CheckInRequest as CheckInRequestDto
import rfm.com.entity.*
import rfm.com.entity.KidsService
import rfm.com.repository.*
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@SpringService
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
    fun checkIn(userId: String, request: CheckInRequestDto): AttendanceResponse {
        val user = userRepository.findById(userId).getOrNull()
            ?: throw IllegalArgumentException("User not found with ID: $userId")

        // Validate that the target entity exists
        when (request.attendanceType) {
            AttendanceType.EVENT -> {
                eventRepository.findById(request.eventId!!).getOrNull()
                    ?: throw IllegalArgumentException("Event not found with ID: ${request.eventId}")
            }
            AttendanceType.SERVICE -> {
                serviceRepository.findById(request.serviceId!!).getOrNull()
                    ?: throw IllegalArgumentException("Service not found with ID: ${request.serviceId}")
            }
            AttendanceType.KIDS_SERVICE -> {
                kidsServiceRepository.findById(request.kidsServiceId!!).getOrNull()
                    ?: throw IllegalArgumentException("Kids service not found with ID: ${request.kidsServiceId}")
            }
        }

        // Check if user is already checked in for this entity today
        val existingAttendance = findExistingTodayAttendance(userId, request.eventId, request.serviceId, request.kidsServiceId)
        if (existingAttendance != null && existingAttendance.status == AttendanceStatus.CHECKED_IN) {
            throw IllegalStateException("User is already checked in for this ${request.attendanceType.name.lowercase()}")
        }

        val attendance = Attendance(
            userId = userId,
            eventId = request.eventId,
            serviceId = request.serviceId,
            kidsServiceId = request.kidsServiceId,
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
     * Admin check-in by scanning a user's QR token
     */
    fun checkInByToken(adminUserId: String, request: CheckInByTokenRequest): AttendanceResponse {
        val user = userRepository.findByQrToken(request.qrToken)
            ?: throw IllegalArgumentException("No user found for the provided QR token")

        val userId = user.id!!

        when (request.attendanceType) {
            AttendanceType.EVENT -> {
                val event = eventRepository.findById(request.eventId!!)
                    .orElseThrow { IllegalArgumentException("Event not found with ID: ${request.eventId}") }

                if (!event.attendeeIds.contains(userId)) {
                    if (event.isAtCapacity) {
                        throw IllegalStateException("Event is at capacity. Cannot add walk-in attendee.")
                    }
                    event.attendeeIds.add(userId)
                    event.waitingListIds.remove(userId)
                    eventRepository.save(event)
                }
            }
            AttendanceType.SERVICE -> {
                serviceRepository.findById(request.serviceId!!)
                    .orElseThrow { IllegalArgumentException("Service not found with ID: ${request.serviceId}") }
            }
            AttendanceType.KIDS_SERVICE -> {
                kidsServiceRepository.findById(request.kidsServiceId!!)
                    .orElseThrow { IllegalArgumentException("Kids service not found with ID: ${request.kidsServiceId}") }
            }
        }

        val existing = findExistingTodayAttendance(userId, request.eventId, request.serviceId, request.kidsServiceId)
        if (existing != null && existing.status == AttendanceStatus.CHECKED_IN) {
            return mapToAttendanceResponse(existing)
        }

        val attendance = Attendance(
            userId = userId,
            eventId = request.eventId,
            serviceId = request.serviceId,
            kidsServiceId = request.kidsServiceId,
            attendanceType = request.attendanceType,
            status = AttendanceStatus.CHECKED_IN,
            checkInTime = LocalDateTime.now(),
            notes = request.notes,
            checkedInBy = adminUserId
        )
        return mapToAttendanceResponse(attendanceRepository.save(attendance))
    }

    /**
     * Check out a user from their attendance record
     */
    fun checkOut(userId: String, request: CheckOutRequest): AttendanceResponse {
        val attendance = attendanceRepository.findById(request.attendanceId).getOrNull()
            ?: throw IllegalArgumentException("Attendance record not found with ID: ${request.attendanceId}")

        if (attendance.userId != userId) {
            throw IllegalArgumentException("Attendance record does not belong to the specified user")
        }

        if (attendance.status == AttendanceStatus.CHECKED_OUT) {
            throw IllegalStateException("User is already checked out")
        }

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
    fun getUserAttendance(userId: String, pageable: Pageable): Page<AttendanceSummaryResponse> {
        userRepository.findById(userId).getOrNull()
            ?: throw IllegalArgumentException("User not found with ID: $userId")

        return attendanceRepository.findByUserId(userId, pageable)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific event
     */
    fun getEventAttendance(eventId: String): List<AttendanceSummaryResponse> {
        eventRepository.findById(eventId).getOrNull()
            ?: throw IllegalArgumentException("Event not found with ID: $eventId")

        return attendanceRepository.findByEventId(eventId)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific service
     */
    fun getServiceAttendance(serviceId: String): List<AttendanceSummaryResponse> {
        serviceRepository.findById(serviceId).getOrNull()
            ?: throw IllegalArgumentException("Service not found with ID: $serviceId")

        return attendanceRepository.findByServiceId(serviceId)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records for a specific kids service
     */
    fun getKidsServiceAttendance(kidsServiceId: String): List<AttendanceSummaryResponse> {
        kidsServiceRepository.findById(kidsServiceId).getOrNull()
            ?: throw IllegalArgumentException("Kids service not found with ID: $kidsServiceId")

        return attendanceRepository.findByKidsServiceId(kidsServiceId)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get currently checked-in users
     */
    fun getCurrentlyCheckedIn(): List<AttendanceSummaryResponse> {
        return attendanceRepository.findByCheckOutTimeIsNullOrderByCheckInTimeDesc()
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance statistics for a date range
     */
    fun getAttendanceStats(request: AttendanceReportRequest): AttendanceStatsResponse {
        val attendanceRecords = when {
            request.userId != null -> {
                userRepository.findById(request.userId).getOrNull()
                    ?: throw IllegalArgumentException("User not found with ID: ${request.userId}")
                attendanceRepository.findByUserId(request.userId)
                    .filter { it.checkInTime.isAfter(request.startDate) && it.checkInTime.isBefore(request.endDate) }
            }
            request.eventId != null -> {
                eventRepository.findById(request.eventId).getOrNull()
                    ?: throw IllegalArgumentException("Event not found with ID: ${request.eventId}")
                attendanceRepository.findByEventId(request.eventId)
                    .filter { it.checkInTime.isAfter(request.startDate) && it.checkInTime.isBefore(request.endDate) }
            }
            else -> {
                attendanceRepository.findByCheckInTimeBetween(request.startDate, request.endDate)
            }
        }

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
    fun getMostFrequentAttendees(pageable: Pageable): List<FrequentAttendeesResponse> {
        // Group attendance by userId and count
        val allAttendance = attendanceRepository.findAll()
        val grouped = allAttendance.groupBy { it.userId }
        val sorted = grouped.entries.sortedByDescending { it.value.size }
        
        val page = sorted.drop(pageable.pageNumber * pageable.pageSize).take(pageable.pageSize)
        
        return page.mapNotNull { (userId, records) ->
            val user = userRepository.findById(userId).getOrNull() ?: return@mapNotNull null
            FrequentAttendeesResponse(
                user = mapToUserResponse(user),
                attendanceCount = records.size.toLong(),
                lastAttendance = records.maxByOrNull { it.checkInTime }?.checkInTime
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
                val checkInRequest = CheckInRequestDto(
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
    fun updateAttendanceStatus(attendanceId: String, status: AttendanceStatus, notes: String? = null): AttendanceResponse {
        val attendance = attendanceRepository.findById(attendanceId).getOrNull()
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
        userId: String,
        eventId: String?,
        serviceId: String?,
        kidsServiceId: String?
    ): Attendance? {
        val today = LocalDateTime.now().toLocalDate()
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        val records = when {
            eventId != null -> attendanceRepository.findByUserIdAndEventId(userId, eventId)
            serviceId != null -> attendanceRepository.findByUserIdAndServiceId(userId, serviceId)
            kidsServiceId != null -> attendanceRepository.findByUserIdAndKidsServiceId(userId, kidsServiceId)
            else -> return null
        }
        
        return records.firstOrNull { attendance ->
            attendance.checkInTime.isAfter(startOfDay) && attendance.checkInTime.isBefore(endOfDay)
        }
    }

    private fun mapToAttendanceResponse(attendance: Attendance): AttendanceResponse {
        val user = userRepository.findById(attendance.userId).getOrNull()
        
        return AttendanceResponse(
            id = attendance.id!!,
            user = user?.let { mapToUserResponse(it) } ?: UserResponse(
                id = attendance.userId,
                email = "",
                firstName = "Unknown",
                lastName = "User",
                createdAt = LocalDateTime.now()
            ),
            attendanceType = attendance.attendanceType,
            status = attendance.status,
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime,
            notes = attendance.notes,
            checkedInBy = attendance.checkedInBy,
            checkedOutBy = attendance.checkedOutBy,
            duration = attendance.duration,
            isCheckedOut = attendance.isCheckedOut,
            event = attendance.eventId?.let { loadEventSummary(it) },
            service = attendance.serviceId?.let { loadServiceResponse(it) },
            kidsService = attendance.kidsServiceId?.let { loadKidsServiceResponse(it) }
        )
    }

    private fun mapToAttendanceSummaryResponse(attendance: Attendance): AttendanceSummaryResponse {
        val user = userRepository.findById(attendance.userId).getOrNull()
        
        val (entityName, entityId) = when (attendance.attendanceType) {
            AttendanceType.EVENT -> {
                val event = attendance.eventId?.let { eventRepository.findById(it).getOrNull() }
                Pair(event?.title ?: "Unknown Event", attendance.eventId ?: "")
            }
            AttendanceType.SERVICE -> {
                val service = attendance.serviceId?.let { serviceRepository.findById(it).getOrNull() }
                Pair(service?.name ?: "Unknown Service", attendance.serviceId ?: "")
            }
            AttendanceType.KIDS_SERVICE -> {
                val ks = attendance.kidsServiceId?.let { kidsServiceRepository.findById(it).getOrNull() }
                Pair(ks?.name ?: "Unknown Kids Service", attendance.kidsServiceId ?: "")
            }
        }

        return AttendanceSummaryResponse(
            id = attendance.id!!,
            userId = attendance.userId,
            userName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim(),
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
            firstName = user.firstName,
            lastName = user.lastName,
            createdAt = user.joinedAt
        )
    }

    private fun loadEventSummary(eventId: String): EventSummaryResponse? {
        val event = eventRepository.findById(eventId).getOrNull() ?: return null
        val organizer = event.organizerId?.let { userRepository.findById(it).getOrNull() }
        return EventSummaryResponse(
            id = event.id!!,
            title = event.title,
            description = event.description,
            date = event.date,
            location = event.location,
            organizerName = organizer?.fullName ?: "Unknown",
            organizerId = event.organizerId ?: "",
            attendeeCount = event.attendeeIds.size,
            maxAttendees = event.maxAttendees,
            availableSpots = event.maxAttendees?.let { it - event.attendeeIds.size } ?: 0,
            headerImagePath = event.headerImagePath,
            needsApproval = event.needsApproval,
            isAtCapacity = event.maxAttendees != null && event.attendeeIds.size >= event.maxAttendees,
            createdAt = event.createdAt
        )
    }

    private fun loadServiceResponse(serviceId: String): ServiceResponse? {
        val serviceEntity = serviceRepository.findById(serviceId).getOrNull() ?: return null
        val leader = serviceEntity.leaderId?.let { userRepository.findById(it).getOrNull() }
        return ServiceResponse(
            id = serviceEntity.id!!,
            name = serviceEntity.name,
            serviceType = serviceEntity.serviceType.name,
            dayOfWeek = serviceEntity.dayOfWeek.name,
            startTime = serviceEntity.startTime.toString(),
            endTime = serviceEntity.endTime.toString(),
            location = serviceEntity.location,
            leaderName = leader?.fullName,
            maxCapacity = serviceEntity.maxCapacity,
            isActive = serviceEntity.isActive
        )
    }

    private fun loadKidsServiceResponse(kidsServiceId: String): KidsServiceResponse? {
        val kidsService = kidsServiceRepository.findById(kidsServiceId).getOrNull() ?: return null
        val leader = kidsService.leaderId?.let { userRepository.findById(it).getOrNull() }
        return KidsServiceResponse(
            id = kidsService.id!!,
            name = kidsService.name,
            dayOfWeek = kidsService.dayOfWeek.name,
            serviceDate = kidsService.serviceDate,
            startTime = kidsService.startTime.toString(),
            endTime = kidsService.endTime.toString(),
            location = kidsService.location,
            leaderName = leader?.fullName,
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
    fun getAttendanceByType(type: AttendanceType): List<AttendanceSummaryResponse> {
        return attendanceRepository.findByAttendanceType(type)
            .map { mapToAttendanceSummaryResponse(it) }
    }

    /**
     * Get attendance records by status
     */
    fun getAttendanceByStatus(status: AttendanceStatus): List<AttendanceSummaryResponse> {
        return attendanceRepository.findByStatus(status)
            .map { mapToAttendanceSummaryResponse(it) }
    }
}