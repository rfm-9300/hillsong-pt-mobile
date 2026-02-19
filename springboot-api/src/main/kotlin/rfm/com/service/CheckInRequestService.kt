package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import rfm.com.dto.*
import rfm.com.entity.*
import rfm.com.repository.*
import rfm.com.util.TokenGenerator
import java.time.LocalDateTime

/**
 * Service for managing QR code-based check-in requests.
 * 
 * This service handles the complete lifecycle of check-in requests:
 * - Creating requests with secure tokens
 * - Retrieving request details for staff verification
 * - Approving requests to create attendance records
 * - Rejecting requests with reasons
 * - Cancelling pending requests
 * - Expiring old requests automatically
 */
@Service
class CheckInRequestService(
    private val checkInRequestRepository: CheckInRequestRepository,
    private val kidRepository: KidRepository,
    private val kidsServiceRepository: KidsServiceRepository,
    private val userRepository: UserRepository,
    private val kidAttendanceRepository: KidAttendanceRepository,
    private val webSocketService: WebSocketService
) {
    
    private val logger = LoggerFactory.getLogger(CheckInRequestService::class.java)
    
    companion object {
        const val TOKEN_EXPIRATION_MINUTES = 15L
    }
    
    /**
     * Creates a new check-in request for a child.
     */
    fun createCheckInRequest(
        parentId: String,
        request: CreateCheckInRequestDto
    ): CheckInRequestResponse {
        // Validate parent exists
        val parent = userRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        // Validate child exists
        val child = kidRepository.findById(request.childId).orElse(null)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        // Validate parent owns the child
        if (!child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to create check-in requests for this child")
        }
        
        // Validate service exists
        val service = kidsServiceRepository.findById(request.serviceId).orElse(null)
            ?: throw IllegalArgumentException("Kids service not found with ID: ${request.serviceId}")
        
        // Validate service is active
        if (!service.isActive) {
            throw IllegalStateException("Service is not active")
        }
        
        // Validate service is accepting check-ins
        if (!service.isCheckInOpen()) {
            throw IllegalStateException("Check-in is not currently open for this service. Check-in opens ${service.checkInStartsMinutesBefore} minutes before service starts.")
        }
        
        // Validate child is eligible for service (age requirements)
        val childAge = child.age
        if (childAge < service.minAge || childAge > service.maxAge) {
            throw IllegalArgumentException("Child does not meet age requirements for this service. Service accepts ages ${service.minAge}-${service.maxAge}.")
        }
        
        // Check for existing pending request for same child and service
        val existingRequest = checkInRequestRepository.findByKidIdAndKidsServiceIdAndStatus(
            child.id!!,
            service.id!!,
            CheckInRequestStatus.PENDING
        )
        
        if (existingRequest != null && !existingRequest.isExpired()) {
            // Return existing request instead of creating a duplicate
            return mapToCheckInRequestResponse(existingRequest, child, service, parent)
        }
        
        // Generate secure token
        val token = TokenGenerator.generateSecureToken()
        val expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES)
        
        // Create check-in request
        val checkInRequest = rfm.com.entity.CheckInRequest(
            kidId = child.id,
            kidsServiceId = service.id,
            requestedById = parentId,
            token = token,
            expiresAt = expiresAt,
            notes = request.notes
        )
        
        val savedRequest = checkInRequestRepository.save(checkInRequest)
        
        return mapToCheckInRequestResponse(savedRequest, child, service, parent)
    }
    
    // Mapping function for CheckInRequestResponse
    private fun mapToCheckInRequestResponse(
        request: rfm.com.entity.CheckInRequest,
        child: Kid? = null,
        service: rfm.com.entity.KidsService? = null,
        parent: User? = null
    ): CheckInRequestResponse {
        val resolvedChild = child ?: kidRepository.findById(request.kidId).orElse(null)
        val resolvedService = service ?: kidsServiceRepository.findById(request.kidsServiceId).orElse(null)
        val resolvedParent = parent ?: userRepository.findById(request.requestedById).orElse(null)
        
        return CheckInRequestResponse(
            id = request.id!!,
            token = request.token,
            qrCodeData = request.token,
            child = resolvedChild?.let { mapToChildSummaryResponse(it) }
                ?: ChildSummaryResponse(id = request.kidId, firstName = "Unknown", lastName = "", fullName = "Unknown", age = 0, ageGroup = "UNKNOWN", isActive = false),
            service = resolvedService?.let { mapToKidsServiceResponse(it) }
                ?: throw IllegalStateException("Service not found"),
            requestedBy = mapToParentResponse(request.requestedById, resolvedParent),
            status = request.status.name,
            createdAt = request.createdAt,
            expiresAt = request.expiresAt,
            expiresInSeconds = request.getSecondsUntilExpiration(),
            isExpired = request.isExpired()
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
    
    /**
     * Approves a check-in request and creates an attendance record.
     */
    fun approveCheckInRequest(
        token: String,
        staffUserId: String,
        notes: String?
    ): CheckInApprovalResponse {
        // Validate token exists
        val request = checkInRequestRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid check-in request token")
        
        // Validate token is valid and not expired
        if (!request.isValid()) {
            if (request.isExpired()) {
                throw IllegalStateException("Check-in request has expired. Please generate a new QR code.")
            } else {
                throw IllegalStateException("Check-in request cannot be processed. Current status: ${request.status}")
            }
        }
        
        // Validate staff exists
        val staff = userRepository.findById(staffUserId).orElse(null)
            ?: throw IllegalArgumentException("Staff member not found with ID: $staffUserId")
        
        val child = kidRepository.findById(request.kidId).orElse(null)
            ?: throw IllegalArgumentException("Child not found")
        val service = kidsServiceRepository.findById(request.kidsServiceId).orElse(null)
            ?: throw IllegalArgumentException("Service not found")
        
        // Check service capacity
        if (service.isAtCapacity) {
            throw IllegalStateException("Service is at capacity. Cannot complete check-in.")
        }
        
        // Check for duplicate check-in (child already checked in to this service today)
        val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
        val todayEnd = LocalDateTime.now().toLocalDate().atTime(23, 59, 59)
        val todayAttendances = kidAttendanceRepository.findByCheckInTimeBetween(todayStart, todayEnd)
        val existingCheckIn = todayAttendances.firstOrNull { 
            it.kidId == request.kidId &&
            it.status == AttendanceStatus.CHECKED_IN && 
            it.kidsServiceId == request.kidsServiceId
        }
        
        if (existingCheckIn != null) {
            throw IllegalStateException("Child is already checked in to this service today")
        }
        
        val checkInTime = LocalDateTime.now()
        val requestedByUser = userRepository.findById(request.requestedById).orElse(null)
        
        // Create KidAttendance record with reference to CheckInRequest
        val attendance = KidAttendance(
            kidId = request.kidId,
            kidsServiceId = request.kidsServiceId,
            checkedInBy = requestedByUser?.fullName ?: "Unknown",
            notes = notes ?: request.notes,
            status = AttendanceStatus.CHECKED_IN,
            checkInRequestId = request.id,
            approvedByStaff = staff.fullName
        )
        
        val savedAttendance = kidAttendanceRepository.save(attendance)
        
        // Update CheckInRequest status to APPROVED
        val updatedRequest = request.copy(
            status = CheckInRequestStatus.APPROVED,
            processedById = staffUserId,
            processedAt = checkInTime,
            attendanceId = savedAttendance.id
        )
        
        checkInRequestRepository.save(updatedRequest)
        
        // Send real-time notification to parent via WebSocket
        webSocketService.notifyCheckInApproved(
            parentUserId = request.requestedById,
            requestId = request.id!!,
            kidId = request.kidId,
            kidsServiceId = request.kidsServiceId,
            approvedBy = staff.fullName,
            attendanceId = savedAttendance.id!!
        )
        
        return CheckInApprovalResponse(
            requestId = request.id,
            attendanceId = savedAttendance.id,
            child = mapToChildSummaryResponse(child),
            service = mapToKidsServiceResponse(service),
            checkInTime = checkInTime,
            approvedBy = staff.fullName,
            message = "Check-in approved successfully"
        )
    }
    
    /**
     * Rejects a check-in request with a reason.
     */
    fun rejectCheckInRequest(
        token: String,
        staffUserId: String,
        reason: String
    ): CheckInRejectionResponse {
        // Validate token exists
        val request = checkInRequestRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid check-in request token")
        
        // Validate token is valid and not expired
        if (!request.isValid()) {
            if (request.isExpired()) {
                throw IllegalStateException("Check-in request has expired. Please generate a new QR code.")
            } else {
                throw IllegalStateException("Check-in request cannot be processed. Current status: ${request.status}")
            }
        }
        
        // Validate staff exists
        val staff = userRepository.findById(staffUserId).orElse(null)
            ?: throw IllegalArgumentException("Staff member not found with ID: $staffUserId")
        
        val child = kidRepository.findById(request.kidId).orElse(null)
        val service = kidsServiceRepository.findById(request.kidsServiceId).orElse(null)
        val rejectionTime = LocalDateTime.now()
        
        // Update CheckInRequest status to REJECTED
        val updatedRequest = request.copy(
            status = CheckInRequestStatus.REJECTED,
            processedById = staffUserId,
            processedAt = rejectionTime,
            rejectionReason = reason
        )
        
        checkInRequestRepository.save(updatedRequest)
        
        // Send real-time notification to parent with reason via WebSocket
        webSocketService.notifyCheckInRejected(
            parentUserId = request.requestedById,
            requestId = request.id!!,
            kidId = request.kidId,
            kidsServiceId = request.kidsServiceId,
            rejectedBy = staff.fullName,
            reason = reason
        )
        
        return CheckInRejectionResponse(
            requestId = request.id,
            child = child?.let { mapToChildSummaryResponse(it) }
                ?: ChildSummaryResponse(id = request.kidId, firstName = "Unknown", lastName = "", fullName = "Unknown", age = 0, ageGroup = "UNKNOWN", isActive = false),
            service = service?.let { mapToKidsServiceResponse(it) }
                ?: throw IllegalStateException("Service not found"),
            rejectedBy = staff.fullName,
            reason = reason,
            message = "Check-in request has been rejected"
        )
    }
    
    /**
     * Cancels a pending check-in request.
     */
    fun cancelCheckInRequest(requestId: String, parentId: String) {
        // Validate request exists
        val request = checkInRequestRepository.findById(requestId).orElse(null)
            ?: throw IllegalArgumentException("Check-in request not found with ID: $requestId")
        
        // Validate parent owns the child
        val child = kidRepository.findById(request.kidId).orElse(null)
        if (child != null && !child.hasParent(parentId)) {
            throw SecurityException("Access denied: You are not authorized to cancel this check-in request")
        }
        
        // Validate request is in PENDING status
        if (request.status != CheckInRequestStatus.PENDING) {
            throw IllegalStateException("Cannot cancel check-in request. Current status: ${request.status}")
        }
        
        // Update status to CANCELLED
        val updatedRequest = request.copy(
            status = CheckInRequestStatus.CANCELLED,
            processedAt = LocalDateTime.now()
        )
        
        checkInRequestRepository.save(updatedRequest)
    }
    
    /**
     * Scheduled job to expire old check-in requests.
     * Runs every 5 minutes to mark PENDING requests that have passed their expiration time as EXPIRED.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    fun expireOldRequests() {
        val now = LocalDateTime.now()
        
        val expiredRequests = checkInRequestRepository.findByStatusAndExpiresAtBefore(
            CheckInRequestStatus.PENDING,
            now
        )
        
        if (expiredRequests.isNotEmpty()) {
            val updatedRequests = expiredRequests.map { request ->
                request.copy(
                    status = CheckInRequestStatus.EXPIRED,
                    processedAt = now
                )
            }
            
            checkInRequestRepository.saveAll(updatedRequests)
            logger.info("Expired ${expiredRequests.size} check-in requests at $now")
        }
    }
    
    /**
     * Retrieves all active (PENDING) check-in requests for a parent's children.
     */
    fun getActiveRequestsForParent(parentId: String): List<CheckInRequestResponse> {
        userRepository.findById(parentId).orElse(null)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        val activeRequests = checkInRequestRepository.findByRequestedByIdAndStatusInOrderByCreatedAtDesc(
            parentId,
            listOf(CheckInRequestStatus.PENDING)
        )
        
        return activeRequests
            .filter { !it.isExpired() }
            .map { mapToCheckInRequestResponse(it) }
    }
    
    /**
     * Retrieves check-in request details by token.
     * Used by staff when scanning a QR code.
     */
    fun getCheckInRequestByToken(token: String): CheckInRequestDetailsResponse {
        val request = checkInRequestRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid check-in request token")
        
        if (request.isExpired()) {
            throw IllegalStateException("Check-in request has expired. Please generate a new QR code.")
        }
        
        return mapToCheckInRequestDetailsResponse(request)
    }
    
    private fun mapToCheckInRequestDetailsResponse(request: rfm.com.entity.CheckInRequest): CheckInRequestDetailsResponse {
        val child = kidRepository.findById(request.kidId).orElse(null)
        val service = kidsServiceRepository.findById(request.kidsServiceId).orElse(null)
        val parent = userRepository.findById(request.requestedById).orElse(null)
        
        return CheckInRequestDetailsResponse(
            id = request.id!!,
            child = child?.let { mapToChildDetailedResponse(it) }
                ?: ChildDetailedResponse(id = request.kidId, firstName = "Unknown", lastName = "", fullName = "Unknown", age = 0, ageGroup = "UNKNOWN", gender = null, emergencyContactName = null, emergencyContactPhone = null, medicalNotes = null, allergies = null, specialNeeds = null, pickupAuthorization = null),
            service = service?.let { mapToKidsServiceResponse(it) }
                ?: throw IllegalStateException("Service not found"),
            requestedBy = mapToParentResponse(request.requestedById, parent),
            status = request.status.name,
            createdAt = request.createdAt,
            expiresAt = request.expiresAt,
            notes = request.notes,
            isExpired = request.isExpired(),
            canBeProcessed = request.canBeProcessed(),
            hasMedicalAlerts = child?.medicalNotes?.isNotBlank() == true,
            hasAllergies = child?.allergies?.isNotBlank() == true,
            hasSpecialNeeds = child?.specialNeeds?.isNotBlank() == true
        )
    }
    
    private fun mapToChildDetailedResponse(child: Kid): ChildDetailedResponse {
        return ChildDetailedResponse(
            id = child.id!!,
            firstName = child.firstName,
            lastName = child.lastName,
            fullName = child.fullName,
            age = child.age,
            ageGroup = child.ageGroup.name,
            gender = child.gender,
            emergencyContactName = child.emergencyContactName,
            emergencyContactPhone = child.emergencyContactPhone,
            medicalNotes = child.medicalNotes,
            allergies = child.allergies,
            specialNeeds = child.specialNeeds,
            pickupAuthorization = child.pickupAuthorization
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
}
