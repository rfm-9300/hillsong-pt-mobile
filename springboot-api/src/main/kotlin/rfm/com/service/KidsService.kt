package rfm.com.service

import org.springframework.stereotype.Service
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.repository.*
import java.time.LocalDateTime

@Service
class KidsService(
    private val kidRepository: KidRepository,
    private val kidsServiceRepository: KidsServiceRepository,
    private val kidAttendanceRepository: KidAttendanceRepository,
    private val userRepository: UserRepository
) {

    /**
     * Get all available kids services with optional filtering
     */
    fun getAvailableServices(
        minAge: Int? = null,
        maxAge: Int? = null,
        acceptingCheckIns: Boolean? = null,
        location: String? = null
    ): List<KidsServiceResponse> {
        val services = kidsServiceRepository.findByIsActiveTrue()
        
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
    fun getService(serviceId: String): KidsServiceResponse {
        val service = kidsServiceRepository.findById(serviceId).orElse(null)
            ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
        
        return mapToKidsServiceResponse(service)
    }

    /**
     * Register a new child
     */
    fun registerChild(parentId: String, request: ChildRegistrationRequest): ChildResponse {
        userRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        request.secondaryParentId?.let {
            userRepository.findById(it).orElse(null)
                ?: throw IllegalArgumentException("Secondary parent not found with ID: $it")
        }
        
        val kid = Kid(
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender,
            primaryParentId = parentId,
            secondaryParentId = request.secondaryParentId,
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
    fun getChildrenForParent(parentId: String): List<ChildResponse> {
        userRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val children = kidRepository.findByEitherParent(parentId).filter { it.isActive }
        return children.map { mapToChildResponse(it) }
    }

    /**
     * Get a specific child (with parent verification)
     */
    fun getChild(childId: String, parentId: String): ChildResponse {
        val child = kidRepository.findById(childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        if (!child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to view this child")
        }
        
        return mapToChildResponse(child)
    }

    /**
     * Update a child (with parent verification)
     */
    fun updateChild(childId: String, parentId: String, request: ChildUpdateRequest): ChildResponse {
        val child = kidRepository.findById(childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        if (!child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to update this child")
        }
        
        request.secondaryParentId?.let {
            userRepository.findById(it).orElse(null)
                ?: throw IllegalArgumentException("Secondary parent not found with ID: $it")
        }
        
        val updatedChild = child.copy(
            firstName = request.firstName ?: child.firstName,
            lastName = request.lastName ?: child.lastName,
            dateOfBirth = request.dateOfBirth ?: child.dateOfBirth,
            gender = request.gender ?: child.gender,
            secondaryParentId = request.secondaryParentId ?: child.secondaryParentId,
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
    fun deleteChild(childId: String, parentId: String) {
        val child = kidRepository.findById(childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: $childId")
        
        if (!child.hasParent(parentId)) {
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
    fun checkInChild(parentId: String, request: KidsCheckInRequest): KidsCheckInResponse {
        val child = kidRepository.findById(request.childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        if (!child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to check in this child")
        }
        
        val service = kidsServiceRepository.findById(request.serviceId).orElse(null)
            ?: throw IllegalArgumentException("Kids service not found with ID: ${request.serviceId}")
        
        if (!service.isActive) {
            throw IllegalStateException("Service is not active")
        }
        
        if (!service.isCheckInOpen()) {
            throw IllegalStateException("Check-in is not currently open for this service")
        }
        
        if (child.age < service.minAge || child.age > service.maxAge) {
            throw IllegalArgumentException("Child is not eligible for this service")
        }
        
        // Check if child is already checked in to this service today
        val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
        val todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59)
        val todayAttendances = kidAttendanceRepository.findByCheckInTimeBetween(todayStart, todayEnd)
        val existingCheckIn = todayAttendances.firstOrNull { 
            it.kidId == child.id && it.status == AttendanceStatus.CHECKED_IN && it.kidsServiceId == service.id 
        }
        
        if (existingCheckIn != null) {
            throw IllegalStateException("Child is already checked in to this service today")
        }
        
        if (service.isAtCapacity) {
            throw IllegalStateException("Service is at capacity")
        }
        
        val parent = userRepository.findById(parentId).orElse(null)
        
        val attendance = KidAttendance(
            kidId = child.id!!,
            kidsServiceId = service.id!!,
            checkedInBy = parent?.fullName ?: "Unknown",
            notes = request.notes,
            status = AttendanceStatus.CHECKED_IN
        )
        
        val savedAttendance = kidAttendanceRepository.save(attendance)
        return mapToKidsCheckInResponse(savedAttendance)
    }

    /**
     * Check out a child from a service
     */
    fun checkOutChild(parentId: String, request: KidsCheckOutRequest): KidsCheckOutResponse {
        val child = kidRepository.findById(request.childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        if (!child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to check out this child")
        }
        
        // Find the current check-in record
        val currentCheckIn = kidAttendanceRepository.findByKidId(child.id!!)
            .firstOrNull { it.status == AttendanceStatus.CHECKED_IN }
            ?: throw IllegalStateException("Child is not currently checked in")
        
        val parent = userRepository.findById(parentId).orElse(null)
        
        val checkedOutAttendance = currentCheckIn.checkOut(
            checkOutTime = LocalDateTime.now(),
            checkedOutBy = parent?.fullName ?: "Unknown"
        ).copy(notes = request.notes ?: currentCheckIn.notes)
        
        val savedAttendance = kidAttendanceRepository.save(checkedOutAttendance)
        return mapToKidsCheckOutResponse(savedAttendance)
    }

    /**
     * Get current check-ins
     */
    fun getCurrentCheckIns(serviceId: String? = null): List<KidsCheckInResponse> {
        val checkIns = if (serviceId != null) {
            kidsServiceRepository.findById(serviceId).orElse(null)
                ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
            kidAttendanceRepository.findByKidsServiceId(serviceId).filter { it.status == AttendanceStatus.CHECKED_IN }
        } else {
            kidAttendanceRepository.findByStatus(AttendanceStatus.CHECKED_IN)
        }
        
        return checkIns.map { mapToKidsCheckInResponse(it) }
    }

    /**
     * Get check-in history
     */
    fun getCheckInHistory(
        parentId: String,
        childId: String? = null,
        serviceId: String? = null,
        page: Int = 0,
        pageSize: Int = 20
    ): List<KidsCheckInResponse> {
        userRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val checkIns = when {
            childId != null && serviceId != null -> {
                val child = kidRepository.findById(childId).orElse(null)
                    ?: throw IllegalArgumentException("Child not found with ID: $childId")
                kidsServiceRepository.findById(serviceId).orElse(null)
                    ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
                
                if (!child.hasParent(parentId)) {
                    throw SecurityException("Access denied: You are not authorized to view this child's history")
                }
                
                kidAttendanceRepository.findByKidIdAndKidsServiceId(childId, serviceId)
            }
            childId != null -> {
                val child = kidRepository.findById(childId).orElse(null)
                    ?: throw IllegalArgumentException("Child not found with ID: $childId")
                
                if (!child.hasParent(parentId)) {
                    throw SecurityException("Access denied: You are not authorized to view this child's history")
                }
                
                kidAttendanceRepository.findByKidId(childId)
            }
            serviceId != null -> {
                kidsServiceRepository.findById(serviceId).orElse(null)
                    ?: throw IllegalArgumentException("Kids service not found with ID: $serviceId")
                
                // Get all children for this parent and filter by service
                val children = kidRepository.findByEitherParent(parentId).filter { it.isActive }
                val childIds = children.mapNotNull { it.id }
                kidAttendanceRepository.findByKidsServiceId(serviceId)
                    .filter { it.kidId in childIds }
            }
            else -> {
                // Get all check-ins for all children of this parent
                val children = kidRepository.findByEitherParent(parentId).filter { it.isActive }
                children.flatMap { kidAttendanceRepository.findByKidId(it.id!!) }
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
        val leader = service.leaderId?.let { userRepository.findById(it).orElse(null) }
        return KidsServiceResponse(
            id = service.id!!,
            name = service.name,
            dayOfWeek = service.dayOfWeek.name,
            serviceDate = service.serviceDate,
            startTime = service.startTime.toString(),
            endTime = service.endTime.toString(),
            location = service.location,
            leaderName = leader?.fullName,
            maxCapacity = service.maxCapacity,
            minAge = service.minAge,
            maxAge = service.maxAge,
            ageGroups = service.ageGroups.map { it.name },
            isActive = service.isActive
        )
    }

    private fun mapToChildResponse(child: Kid): ChildResponse {
        val currentCheckIn = kidAttendanceRepository.findByKidId(child.id!!)
            .firstOrNull { it.status == AttendanceStatus.CHECKED_IN }
        
        val primaryParent = userRepository.findById(child.primaryParentId).orElse(null)
        val secondaryParent = child.secondaryParentId?.let { userRepository.findById(it).orElse(null) }
        
        return ChildResponse(
            id = child.id,
            firstName = child.firstName,
            lastName = child.lastName,
            fullName = child.fullName,
            dateOfBirth = child.dateOfBirth,
            age = child.age,
            ageGroup = child.ageGroup.name,
            gender = child.gender,
            primaryParent = mapToParentResponse(child.primaryParentId, primaryParent),
            secondaryParent = secondaryParent?.let { mapToParentResponse(child.secondaryParentId!!, it) },
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

    private fun mapToParentResponse(parentId: String, user: User?): ParentResponse {
        return ParentResponse(
            id = parentId,
            firstName = user?.firstName ?: "Unknown",
            lastName = user?.lastName ?: "",
            fullName = user?.fullName ?: "Unknown",
            email = user?.email,
            phone = user?.phone
        )
    }

    private fun mapToCheckInStatusResponse(attendance: KidAttendance): CheckInStatusResponse {
        val service = kidsServiceRepository.findById(attendance.kidsServiceId).orElse(null)
        return CheckInStatusResponse(
            isCheckedIn = attendance.status == AttendanceStatus.CHECKED_IN,
            serviceName = service?.name,
            serviceId = attendance.kidsServiceId,
            checkInTime = attendance.checkInTime,
            checkedInBy = attendance.checkedInBy,
            checkInMethod = if (attendance.checkInRequestId != null) CheckInMethod.QR_CODE else CheckInMethod.DIRECT,
            approvedByStaff = attendance.approvedByStaff
        )
    }

    private fun mapToKidsCheckInResponse(attendance: KidAttendance): KidsCheckInResponse {
        val child = kidRepository.findById(attendance.kidId).orElse(null)
        val service = kidsServiceRepository.findById(attendance.kidsServiceId).orElse(null)
        
        return KidsCheckInResponse(
            id = attendance.id!!,
            child = child?.let { mapToChildSummaryResponse(it) } ?: ChildSummaryResponse(
                id = attendance.kidId, firstName = "Unknown", lastName = "", fullName = "Unknown",
                age = 0, ageGroup = "UNKNOWN", isActive = false
            ),
            service = service?.let { mapToKidsServiceResponse(it) } ?: throw IllegalStateException("Service not found"),
            checkInTime = attendance.checkInTime,
            checkOutTime = attendance.checkOutTime,
            checkedInBy = attendance.checkedInBy,
            checkedOutBy = attendance.checkedOutBy,
            notes = attendance.notes,
            status = attendance.status.name,
            isCheckedOut = attendance.isCheckedOut,
            duration = attendance.duration,
            checkInMethod = if (attendance.checkInRequestId != null) CheckInMethod.QR_CODE else CheckInMethod.DIRECT,
            checkInRequestId = attendance.checkInRequestId,
            approvedByStaff = attendance.approvedByStaff
        )
    }

    private fun mapToKidsCheckOutResponse(attendance: KidAttendance): KidsCheckOutResponse {
        val child = kidRepository.findById(attendance.kidId).orElse(null)
        val service = kidsServiceRepository.findById(attendance.kidsServiceId).orElse(null)
        
        return KidsCheckOutResponse(
            id = attendance.id!!,
            childId = attendance.kidId,
            serviceId = attendance.kidsServiceId,
            child = child?.let { mapToChildSummaryResponse(it) } ?: ChildSummaryResponse(
                id = attendance.kidId, firstName = "Unknown", lastName = "", fullName = "Unknown",
                age = 0, ageGroup = "UNKNOWN", isActive = false
            ),
            service = service?.let { mapToKidsServiceResponse(it) } ?: throw IllegalStateException("Service not found"),
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