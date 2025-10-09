package rfm.com.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
@Transactional
class CheckInRequestService(
    private val checkInRequestRepository: CheckInRequestRepository,
    private val kidRepository: KidRepository,
    private val kidsServiceRepository: KidsServiceRepository,
    private val userProfileRepository: UserProfileRepository,
    private val kidAttendanceRepository: KidAttendanceRepository,
    private val webSocketService: WebSocketService
) {
    
    companion object {
        const val TOKEN_EXPIRATION_MINUTES = 15L
    }
    
    /**
     * Creates a new check-in request for a child.
     * 
     * Validates:
     * - Parent owns the child
     * - Service is accepting check-ins
     * - Child is eligible for service (age requirements)
     * - No existing pending request for same child and service
     * 
     * @param parentId The ID of the parent creating the request
     * @param request The check-in request details
     * @return CheckInRequestResponse with token and QR code data
     * @throws IllegalArgumentException if validation fails
     * @throws SecurityException if parent doesn't own the child
     * @throws IllegalStateException if service is not accepting check-ins
     */
    fun createCheckInRequest(
        parentId: Long,
        request: CreateCheckInRequestDto
    ): CheckInRequestResponse {
        // Validate parent exists
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        // Validate child exists
        val child = kidRepository.findByIdOrNull(request.childId)
            ?: throw IllegalArgumentException("Child not found with ID: ${request.childId}")
        
        // Validate parent owns the child
        if (!child.hasParent(parent)) {
            throw SecurityException("Access denied: You are not authorized to create check-in requests for this child")
        }
        
        // Validate service exists
        val service = kidsServiceRepository.findByIdOrNull(request.serviceId)
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
        if (!child.isEligibleForService(service)) {
            throw IllegalArgumentException("Child does not meet age requirements for this service. Service accepts ages ${service.minAge}-${service.maxAge}.")
        }
        
        // Check for existing pending request for same child and service
        val existingRequest = checkInRequestRepository.findByKidAndKidsServiceAndStatus(
            child,
            service,
            CheckInRequestStatus.PENDING
        )
        
        if (existingRequest != null && !existingRequest.isExpired()) {
            // Return existing request instead of creating a duplicate
            return mapToCheckInRequestResponse(existingRequest)
        }
        
        // Generate secure token
        val token = TokenGenerator.generateSecureToken()
        val expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES)
        
        // Create check-in request
        val checkInRequest = rfm.com.entity.CheckInRequest(
            kid = child,
            kidsService = service,
            requestedBy = parent,
            token = token,
            expiresAt = expiresAt,
            notes = request.notes
        )
        
        val savedRequest = checkInRequestRepository.save(checkInRequest)
        
        return mapToCheckInRequestResponse(savedRequest)
    }
    
    // Mapping function for CheckInRequestResponse
    private fun mapToCheckInRequestResponse(request: rfm.com.entity.CheckInRequest): CheckInRequestResponse {
        return CheckInRequestResponse(
            id = request.id!!,
            token = request.token,
            qrCodeData = request.token, // Token is the QR code data
            child = mapToChildSummaryResponse(request.kid),
            service = mapToKidsServiceResponse(request.kidsService),
            requestedBy = mapToParentResponse(request.requestedBy),
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
    
    /**
     * Approves a check-in request and creates an attendance record.
     * Used by staff after verifying the child's identity and information.
     * 
     * @param token The token from the scanned QR code
     * @param staffUserId The ID of the staff member approving the request
     * @param notes Optional notes from staff
     * @return CheckInApprovalResponse with attendance details
     * @throws IllegalArgumentException if token not found or staff not found
     * @throws IllegalStateException if token is expired, service at capacity, or duplicate check-in
     */
    fun approveCheckInRequest(
        token: String,
        staffUserId: Long,
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
        val staff = userProfileRepository.findByIdOrNull(staffUserId)
            ?: throw IllegalArgumentException("Staff member not found with ID: $staffUserId")
        
        val child = request.kid
        val service = request.kidsService
        
        // Check service capacity
        if (service.isAtCapacity) {
            throw IllegalStateException("Service is at capacity. Cannot complete check-in.")
        }
        
        // Check for duplicate check-in (child already checked in to this service today)
        val existingCheckIn = kidAttendanceRepository.findByKidAndCheckInTimeBetween(
            child,
            LocalDateTime.now().toLocalDate().atStartOfDay(),
            LocalDateTime.now().toLocalDate().atTime(23, 59, 59)
        ).firstOrNull { 
            it.status == AttendanceStatus.CHECKED_IN && 
            it.kidsService.id == service.id 
        }
        
        if (existingCheckIn != null) {
            throw IllegalStateException("Child is already checked in to this service today")
        }
        
        val checkInTime = LocalDateTime.now()
        
        // Create KidAttendance record with reference to CheckInRequest
        val attendance = KidAttendance(
            kid = child,
            kidsService = service,
            checkedInBy = request.requestedBy.fullName,
            notes = notes ?: request.notes,
            status = AttendanceStatus.CHECKED_IN,
            checkInRequest = request,
            approvedByStaff = staff.fullName
        )
        
        val savedAttendance = kidAttendanceRepository.save(attendance)
        
        // Update CheckInRequest status to APPROVED
        val updatedRequest = request.copy(
            status = CheckInRequestStatus.APPROVED,
            processedBy = staff,
            processedAt = checkInTime
        )
        
        checkInRequestRepository.save(updatedRequest)
        
        // Send real-time notification to parent via WebSocket
        webSocketService.notifyCheckInApproved(
            parentUserId = request.requestedBy.id!!,
            request = updatedRequest,
            approvedBy = staff.fullName,
            attendanceId = savedAttendance.id!!
        )
        
        return CheckInApprovalResponse(
            requestId = request.id!!,
            attendanceId = savedAttendance.id!!,
            child = mapToChildSummaryResponse(child),
            service = mapToKidsServiceResponse(service),
            checkInTime = checkInTime,
            approvedBy = staff.fullName,
            message = "Check-in approved successfully"
        )
    }
    
    /**
     * Rejects a check-in request with a reason.
     * Used by staff when they cannot approve a check-in.
     * 
     * @param token The token from the scanned QR code
     * @param staffUserId The ID of the staff member rejecting the request
     * @param reason The reason for rejection
     * @return CheckInRejectionResponse with rejection details
     * @throws IllegalArgumentException if token not found or staff not found
     * @throws IllegalStateException if token is expired or already processed
     */
    fun rejectCheckInRequest(
        token: String,
        staffUserId: Long,
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
        val staff = userProfileRepository.findByIdOrNull(staffUserId)
            ?: throw IllegalArgumentException("Staff member not found with ID: $staffUserId")
        
        val child = request.kid
        val service = request.kidsService
        val rejectionTime = LocalDateTime.now()
        
        // Update CheckInRequest status to REJECTED
        val updatedRequest = request.copy(
            status = CheckInRequestStatus.REJECTED,
            processedBy = staff,
            processedAt = rejectionTime,
            rejectionReason = reason
        )
        
        checkInRequestRepository.save(updatedRequest)
        
        // Send real-time notification to parent with reason via WebSocket
        webSocketService.notifyCheckInRejected(
            parentUserId = request.requestedBy.id!!,
            request = updatedRequest,
            rejectedBy = staff.fullName,
            reason = reason
        )
        
        return CheckInRejectionResponse(
            requestId = request.id!!,
            child = mapToChildSummaryResponse(child),
            service = mapToKidsServiceResponse(service),
            rejectedBy = staff.fullName,
            reason = reason,
            message = "Check-in request has been rejected"
        )
    }
    
    /**
     * Cancels a pending check-in request.
     * Used by parents when they no longer need the check-in request.
     * 
     * @param requestId The ID of the check-in request to cancel
     * @param parentId The ID of the parent cancelling the request
     * @throws IllegalArgumentException if request not found or parent not found
     * @throws SecurityException if parent doesn't own the child
     * @throws IllegalStateException if request is not in PENDING status
     */
    fun cancelCheckInRequest(requestId: Long, parentId: Long) {
        // Validate request exists
        val request = checkInRequestRepository.findByIdOrNull(requestId)
            ?: throw IllegalArgumentException("Check-in request not found with ID: $requestId")
        
        // Validate parent exists
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        // Validate parent owns the child
        if (!request.kid.hasParent(parent)) {
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
     * 
     * This ensures that expired QR codes cannot be used for check-in.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    fun expireOldRequests() {
        val now = LocalDateTime.now()
        
        // Query all PENDING requests with expires_at before current time
        val expiredRequests = checkInRequestRepository.findByStatusAndExpiresAtBefore(
            CheckInRequestStatus.PENDING,
            now
        )
        
        if (expiredRequests.isNotEmpty()) {
            // Update status to EXPIRED
            val updatedRequests = expiredRequests.map { request ->
                request.copy(
                    status = CheckInRequestStatus.EXPIRED,
                    processedAt = now
                )
            }
            
            checkInRequestRepository.saveAll(updatedRequests)
            
            // Log the number of expired requests
            println("Expired ${expiredRequests.size} check-in requests at $now")
        }
    }
    
    /**
     * Retrieves all active (PENDING) check-in requests for a parent's children.
     * Used by parents to view their pending check-in requests.
     * 
     * @param parentId The ID of the parent
     * @return List of CheckInRequestResponse for all pending requests
     * @throws IllegalArgumentException if parent not found
     */
    @Transactional(readOnly = true)
    fun getActiveRequestsForParent(parentId: Long): List<CheckInRequestResponse> {
        // Validate parent exists
        val parent = userProfileRepository.findByIdOrNull(parentId)
            ?: throw IllegalArgumentException("Parent not found with ID: $parentId")
        
        // Query all PENDING requests for parent's children
        val activeRequests = checkInRequestRepository.findByRequestedByAndStatusIn(
            parent,
            listOf(CheckInRequestStatus.PENDING)
        )
        
        // Filter out expired requests and map to response
        return activeRequests
            .filter { !it.isExpired() }
            .map { mapToCheckInRequestResponse(it) }
    }
    
    /**
     * Retrieves check-in request details by token.
     * Used by staff when scanning a QR code.
     * 
     * @param token The token from the scanned QR code
     * @return CheckInRequestDetailsResponse with child medical information
     * @throws IllegalArgumentException if token not found
     * @throws IllegalStateException if token is expired
     */
    @Transactional(readOnly = true)
    fun getCheckInRequestByToken(token: String): CheckInRequestDetailsResponse {
        // Validate token exists
        val request = checkInRequestRepository.findByToken(token)
            ?: throw IllegalArgumentException("Invalid check-in request token")
        
        // Check if token is expired
        if (request.isExpired()) {
            throw IllegalStateException("Check-in request has expired. Please generate a new QR code.")
        }
        
        return mapToCheckInRequestDetailsResponse(request)
    }
    
    private fun mapToCheckInRequestDetailsResponse(request: rfm.com.entity.CheckInRequest): CheckInRequestDetailsResponse {
        val child = request.kid
        
        return CheckInRequestDetailsResponse(
            id = request.id!!,
            child = mapToChildDetailedResponse(child),
            service = mapToKidsServiceResponse(request.kidsService),
            requestedBy = mapToParentResponse(request.requestedBy),
            status = request.status.name,
            createdAt = request.createdAt,
            expiresAt = request.expiresAt,
            notes = request.notes,
            isExpired = request.isExpired(),
            canBeProcessed = request.canBeProcessed(),
            hasMedicalAlerts = !child.medicalNotes.isNullOrBlank(),
            hasAllergies = !child.allergies.isNullOrBlank(),
            hasSpecialNeeds = !child.specialNeeds.isNullOrBlank()
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
}
