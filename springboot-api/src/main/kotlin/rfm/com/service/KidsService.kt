package rfm.com.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.repository.*
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class KidsService(
    private val kidRepository: KidRepository,
    private val kidsServiceRepository: KidsServiceRepository,
    private val kidAttendanceRepository: KidAttendanceRepository,
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Get all available kids services with optional filtering
     */
    @Transactional(readOnly = true)
    fun getAvailableServices(
        minAge: Int? = null,
        maxAge: Int? = null,
        acceptingCheckIns: Boolean? = null,
        location: String? = null
    ): List<KidsServiceResponse> {
        val services = kidsServiceRepository.findActiveKidsServices()
        
        return services.filter { service ->
            var matches = true
            
            if (minAge != null) {
                matches = matches && service.minAge >= minAge
            }
            if (maxAge != null) {
                matches = matches && service.maxAge <= maxAge
            }
            if (acceptingCheckIns != null && acceptingCheckIns) {
                matches = matches && service.isCheckInOpen()
            }
            if (location != null) {
                matches = matches && service.location.contains(location, ignoreCase = true)
            }
            
            matches
        }.map { mapToKidsServiceResponse(it) }
    }

    /**
     * Get a specific kids service
     */
    @Transactional(readOnly = true)
    fun getService(serviceId: Long): KidsServiceResponse {
        val service = kidsServiceRepository.findByIdOrNull(serviceId)
            ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
        
        return mapToKidsServiceResponse(service)
    }



    /**
     * Register a new child
     */
    fun registerChild(parentId: Long, request: ChildRegistrationRequest): ChildResponse {
        val primaryParent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val secondaryParent = request.secondaryParentId?.let { 
            userProfileRepository.findByIdOrNull(it)
                ?: throw IllegalArgumentException("Secondary parent not found with ID: $it")
        }
        
        val kid = Kid(
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender,
            primaryParent = primaryParent,
            secondaryParent = secondaryParent,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            medicalNotes = request.medicalNotes,
            allergies = request.allergies,
            specialNeeds = request.specialNeeds,
            pickupAuthorization = request.pickupAuthorization
        )
        
        val savedKid = kidRepository.save(kid)
        return mapToChildResponse(savedKid)
    }

    /**
     * Get children for a specific parent
     */
    @Transactional(readOnly = true)
    fun getChildrenForParent(parentId: Long): List<ChildResponse> {
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val children = kidRepository.findByEitherParent(parent).filter { it.isActive }
        return children.map { mapToChildResponse(it) }
    }

    /**
     * Get a specific child (with parent verification)
     */
    @Transactional(readOnly = true)
    fun getChild(childId: Long, parentId: Long): ChildResponse {
        val child = kidRepository.findByIdOrNull(childId)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to view this child")
        }
        
        return mapToChildResponse(child)
    }

    /**
     * Update a child (with parent verification)
     */
    fun updateChild(childId: Long, parentId: Long, request: ChildUpdateRequest): ChildResponse {
        val child = kidRepository.findByIdOrNull(childId)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to update this child")
        }
        
        val secondaryParent = request.secondaryParentId?.let { 
            userProfileRepository.findByIdOrNull(it)
                ?: throw IllegalArgumentException("Secondary parent not found with ID: $it")
        }
        
        val updatedChild = child.copy(
            firstName = request.firstName ?: child.firstName,
            lastName = request.lastName ?: child.lastName,
            dateOfBirth = request.dateOfBirth ?: child.dateOfBirth,
            gender = request.gender ?: child.gender,
            secondaryParent = secondaryParent ?: child.secondaryParent,
            emergencyContactName = request.emergencyContactName ?: child.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone ?: child.emergencyContactPhone,
            medicalNotes = request.medicalNotes ?: child.medicalNotes,
            allergies = request.allergies ?: child.allergies,
            specialNeeds = request.specialNeeds ?: child.specialNeeds,
            pickupAuthorization = request.pickupAuthorization ?: child.pickupAuthorization,
            isActive = request.isActive ?: child.isActive,
            updatedAt = LocalDateTime.now()
        )
        
        val savedChild = kidRepository.save(updatedChild)
        return mapToChildResponse(savedChild)
    }

    /**
     * Delete a child (with parent verification)
     */
    fun deleteChild(childId: Long, parentId: Long) {
        val child = kidRepository.findByIdOrNull(childId)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to delete this child")
        }
        
        // Soft delete by setting isActive to false
        val updatedChild = child.copy(isActive = false, updatedAt = LocalDateTime.now())
        kidRepository.save(updatedChild)
    }

    /**
     * Check in a child to a service
     * 
     * @deprecated This method is deprecated in favor of the QR code-based check-in system.
     * Use CheckInRequestService to create check-in requests that require staff verification.
     */
    @Deprecated(
        message = "Use QR code-based check-in system via CheckInRequestService",
        level = DeprecationLevel.WARNING
    )
    fun checkInChild(parentId: Long, request: KidsCheckInRequest): KidsCheckInResponse {
        val child = kidRepository.findByIdOrNull(request.childId)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to check in this child")
        }
        
        val service = kidsServiceRepository.findByIdOrNull(request.serviceId)
            ?: throw IllegalArgumentException("Kids service not found with ID: ${request.serviceId}")
        
        if (!service.isActive) {
            throw IllegalStateException("Service is not active")
        }
        
        if (!service.isCheckInOpen()) {
            throw IllegalStateException("Check-in is not currently open for this service")
        }
        
        if (!child.isEligibleForService(service)) {
            throw IllegalArgumentException("Child is not eligible for this service")
        }
        
        // Check if child is already checked in to this service today
        val existingCheckIn = kidAttendanceRepository.findByKidAndCheckInTimeBetween(
            child,
            LocalDateTime.now().toLocalDate().atStartOfDay(),
            LocalDateTime.now().toLocalDate().atTime(23, 59, 59)
        ).firstOrNull { it.status == AttendanceStatus.CHECKED_IN && it.kidsService.id == service.id }
        
        if (existingCheckIn != null) {
            throw IllegalStateException("Child is already checked in to this service today")
        }
        
        if (service.isAtCapacity) {
            throw IllegalStateException("Service is at capacity")
        }
        
        val attendance = KidAttendance(
            kid = child,
            kidsService = service,
            checkedInBy = parent.fullName,
            notes = request.notes,
            status = AttendanceStatus.CHECKED_IN
        )
        
        val savedAttendance = kidAttendanceRepository.save(attendance)
        return mapToKidsCheckInResponse(savedAttendance)
    }

    /**
     * Check out a child from a service
     */
    fun checkOutChild(parentId: Long, request: KidsCheckOutRequest): KidsCheckOutResponse {
        val child = kidRepository.findByIdOrNull(request.childId)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to check out this child")
        }
        
        // Find the current check-in record
        val currentCheckIn = kidAttendanceRepository.findByKid(child)
            .firstOrNull { it.status == AttendanceStatus.CHECKED_IN }
            ?: throw IllegalStateException("Child is not currently checked in")
        
        val checkedOutAttendance = currentCheckIn.checkOut(
            checkOutTime = LocalDateTime.now(),
            checkedOutBy = parent.fullName
        ).copy(notes = request.notes ?: currentCheckIn.notes)
        
        val savedAttendance = kidAttendanceRepository.save(checkedOutAttendance)
        return mapToKidsCheckOutResponse(savedAttendance)
    }

    /**
     * Get current check-ins
     */
    @Transactional(readOnly = true)
    fun getCurrentCheckIns(serviceId: Long? = null): List<KidsCheckInResponse> {
        val checkIns = if (serviceId != null) {
            val service = kidsServiceRepository.findByIdOrNull(serviceId)
                ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
            kidAttendanceRepository.findByKidsService(service).filter { it.status == AttendanceStatus.CHECKED_IN }
        } else {
            kidAttendanceRepository.findByStatus(AttendanceStatus.CHECKED_IN)
        }
        
        return checkIns.map { mapToKidsCheckInResponse(it) }
    }

    /**
     * Get check-in history
     */
    @Transactional(readOnly = true)
    fun getCheckInHistory(
        parentId: Long,
        childId: Long? = null,
        serviceId: Long? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): List<KidsCheckInResponse> {
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val checkIns = when {
            childId != null && serviceId != null -> {
                val child = kidRepository.findByIdOrNull(childId)
                    ?: throw IllegalArgumentException("Child not found with ID: $childId")
                val service = kidsServiceRepository.findByIdOrNull(serviceId)
                    ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
                
                if (!child.hasParent(parent)) {
                    throw SecurityException("Access denied: You are not authorized to view this child's history")
                }
                
                kidAttendanceRepository.findByKidAndKidsService(child, service)
            }
            childId != null -> {
                val child = kidRepository.findByIdOrNull(childId)
                    ?: throw IllegalArgumentException("Child not found with ID: $childId")
                
                if (!child.hasParent(parent)) {
                    throw SecurityException("Access denied: You are not authorized to view this child's history")
                }
                
                kidAttendanceRepository.findByKid(child)
            }
            serviceId != null -> {
                val service = kidsServiceRepository.findByIdOrNull(serviceId)
                    ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
                
                // Get all children for this parent and filter by service
                val children = kidRepository.findByEitherParent(parent).filter { it.isActive }
                kidAttendanceRepository.findByKidsService(service).filter { it.kid in children }
            }
            else -> {
                // Get all check-ins for all children of this parent
                val children = kidRepository.findByEitherParent(parent).filter { it.isActive }
                children.flatMap { kidAttendanceRepository.findByKid(it) }
            }
        }
        
        return checkIns
            .sortedByDescending { it.checkInTime }
            .drop(page * pageSize)
            .take(pageSize)
            .map { mapToKidsCheckInResponse(it) }
    }

    // Mapping functions
    private fun mapToKidsServiceResponse(service: rfm.com.entity.KidsService): KidsServiceResponse {
        return KidsServiceResponse(
            id = service.id!!,
            name = service.name,
            dayOfWeek = service.dayOfWeek.name,
            serviceDate = service.serviceDate,
            startTime = service.startTime.toString(),
            endTime = service.endTime.toString(),
            location = service.location,
            leaderName = service.leader?.let { "${it.firstName} ${it.lastName}" },
            maxCapacity = service.maxCapacity,
            minAge = service.minAge,
            maxAge = service.maxAge,
            ageGroups = service.ageGroups.map { it.name },
            isActive = service.isActive
        )
    }

    private fun mapToChildResponse(child: Kid): ChildResponse {
        val currentCheckIn = kidAttendanceRepository.findByKid(child)
            .firstOrNull { it.status == AttendanceStatus.CHECKED_IN }
        
        return ChildResponse(
            id = child.id!!,
            firstName = child.firstName,
            lastName = child.lastName,
            fullName = child.fullName,
            dateOfBirth = child.dateOfBirth,
            age = child.age,
            ageGroup = child.ageGroup.name,
            gender = child.gender,
            primaryParent = mapToParentResponse(child.primaryParent),
            secondaryParent = child.secondaryParent?.let { mapToParentResponse(it) },
            emergencyContactName = child.emergencyContactName,
            emergencyContactPhone = child.emergencyContactPhone,
            medicalNotes = child.medicalNotes,
            allergies = child.allergies,
            specialNeeds = child.specialNeeds,
            pickupAuthorization = child.pickupAuthorization,
            isActive = child.isActive,
            createdAt = child.createdAt,
            updatedAt = child.updatedAt,
            currentCheckInStatus = currentCheckIn?.let { mapToCheckInStatusResponse(it) }
        )
    }

    private fun mapToParentResponse(parent: UserProfile): ParentResponse {
        return ParentResponse(
            id = parent.id!!,
            firstName = parent.firstName,
            lastName = parent.lastName,
            fullName = parent.fullName,
            email = parent.email,
            phone = parent.phone
        )
    }

    private fun mapToCheckInStatusResponse(attendance: KidAttendance): CheckInStatusResponse {
        return CheckInStatusResponse(
            isCheckedIn = attendance.status == AttendanceStatus.CHECKED_IN,
            serviceName = attendance.kidsService.name,
            serviceId = attendance.kidsService.id,
            checkInTime = attendance.checkInTime,
            checkedInBy = attendance.checkedInBy,
            checkInMethod = if (attendance.checkInRequest != null) CheckInMethod.QR_CODE else CheckInMethod.DIRECT,
            approvedByStaff = attendance.approvedByStaff
        )
    }

    private fun mapToKidsCheckInResponse(attendance: KidAttendance): KidsCheckInResponse {
        return KidsCheckInResponse(
            id = attendance.id!!,
            child = mapToChildSummaryResponse(attendance.kid),
            service = mapToKidsServiceResponse(attendance.kidsService),
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime,
            checkedInBy = attendance.checkedInBy,
            checkedOutBy = attendance.checkedOutBy,
            notes = attendance.notes,
            status = attendance.status.name,
            isCheckedOut = attendance.isCheckedOut,
            duration = attendance.duration,
            checkInMethod = if (attendance.checkInRequest != null) CheckInMethod.QR_CODE else CheckInMethod.DIRECT,
            checkInRequestId = attendance.checkInRequest?.id,
            approvedByStaff = attendance.approvedByStaff
        )
    }

    private fun mapToKidsCheckOutResponse(attendance: KidAttendance): KidsCheckOutResponse {
        return KidsCheckOutResponse(
            id = attendance.id!!,
            childId = attendance.kid.id!!,
            serviceId = attendance.kidsService.id!!,
            child = mapToChildSummaryResponse(attendance.kid),
            service = mapToKidsServiceResponse(attendance.kidsService),
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime!!,
            checkedInBy = attendance.checkedInBy,
            checkedOutBy = attendance.checkedOutBy!!,
            notes = attendance.notes,
            status = attendance.status.name,
            duration = attendance.duration!!
        )
    }

    private fun mapToChildSummaryResponse(child: Kid): ChildSummaryResponse {
        return ChildSummaryResponse(
            id = child.id!!,
            firstName = child.firstName,
            lastName = child.lastName,
            fullName = child.fullName,
            age = child.age,
            ageGroup = child.ageGroup.name,
            isActive = child.isActive
        )
    }
}