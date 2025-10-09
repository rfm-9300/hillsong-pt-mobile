package rfm.com.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import rfm.com.dto.*
import rfm.com.service.CheckInRequestService
import rfm.com.util.getCurrentUserId

/**
 * REST Controller for QR code-based check-in requests.
 * 
 * This controller handles:
 * - Parent creation of check-in requests with QR codes
 * - Staff scanning and verification of QR codes
 * - Staff approval and rejection of check-in requests
 * - Parent cancellation of pending requests
 * - Viewing active check-in requests
 */
@RestController
@RequestMapping("/api/kids/checkin-requests")
class CheckInRequestController(
    private val checkInRequestService: CheckInRequestService
) {
    
    /**
     * Create a new check-in request for a child.
     * 
     * Parents use this endpoint to generate a QR code for check-in.
     * The QR code contains a secure token that expires after 15 minutes.
     * 
     * @param request CreateCheckInRequestDto with childId and serviceId
     * @param authentication Current authenticated user
     * @return CheckInRequestResponse with token and QR code data
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    fun createCheckInRequest(
        @Valid @RequestBody request: CreateCheckInRequestDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInRequestResponse>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val response = checkInRequestService.createCheckInRequest(userId, request)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in request created successfully",
                    data = response
                )
            )
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Access denied"
                    )
                )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid request"
                    )
                )
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Service is not accepting check-ins"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while creating the check-in request"
                    )
                )
        }
    }
    
    /**
     * Get check-in request details by token.
     * 
     * Staff use this endpoint after scanning a QR code to view the check-in request details,
     * including child information, medical alerts, and service details.
     * 
     * @param token The token from the scanned QR code
     * @return CheckInRequestDetailsResponse with child medical information
     */
    @GetMapping("/token/{token}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    fun getRequestByToken(
        @PathVariable token: String
    ): ResponseEntity<ApiResponse<CheckInRequestDetailsResponse>> {
        return try {
            val response = checkInRequestService.getCheckInRequestByToken(token)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in request retrieved successfully",
                    data = response
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Check-in request not found"
                    )
                )
        } catch (e: IllegalStateException) {
            // Token expired
            ResponseEntity.status(HttpStatus.GONE)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Check-in request has expired"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving the check-in request"
                    )
                )
        }
    }
    
    /**
     * Approve a check-in request.
     * 
     * Staff use this endpoint after verifying the child's identity and information
     * to complete the check-in process. This creates an attendance record.
     * 
     * @param token The token from the scanned QR code
     * @param request ApproveCheckInDto with optional notes
     * @param authentication Current authenticated staff member
     * @return CheckInApprovalResponse with attendance details
     */
    @PostMapping("/token/{token}/approve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    fun approveCheckIn(
        @PathVariable token: String,
        @Valid @RequestBody request: ApproveCheckInDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInApprovalResponse>> {
        return try {
            val staffUserId = authentication.getCurrentUserId()
            val response = checkInRequestService.approveCheckInRequest(
                token = token,
                staffUserId = staffUserId,
                notes = request.notes
            )
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in approved successfully",
                    data = response
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid check-in request"
                    )
                )
        } catch (e: IllegalStateException) {
            // Handle capacity issues, duplicate check-ins, and expired tokens
            val statusCode = when {
                e.message?.contains("capacity", ignoreCase = true) == true -> HttpStatus.CONFLICT
                e.message?.contains("already checked in", ignoreCase = true) == true -> HttpStatus.CONFLICT
                e.message?.contains("expired", ignoreCase = true) == true -> HttpStatus.GONE
                else -> HttpStatus.BAD_REQUEST
            }
            
            ResponseEntity.status(statusCode)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Cannot approve check-in"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while approving the check-in"
                    )
                )
        }
    }
    
    /**
     * Reject a check-in request.
     * 
     * Staff use this endpoint when they cannot approve a check-in.
     * A reason must be provided which will be sent to the parent.
     * 
     * @param token The token from the scanned QR code
     * @param request RejectCheckInDto with required rejection reason
     * @param authentication Current authenticated staff member
     * @return CheckInRejectionResponse with rejection details
     */
    @PostMapping("/token/{token}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    fun rejectCheckIn(
        @PathVariable token: String,
        @Valid @RequestBody request: RejectCheckInDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CheckInRejectionResponse>> {
        return try {
            val staffUserId = authentication.getCurrentUserId()
            val response = checkInRequestService.rejectCheckInRequest(
                token = token,
                staffUserId = staffUserId,
                reason = request.reason
            )
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in rejected",
                    data = response
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid check-in request"
                    )
                )
        } catch (e: IllegalStateException) {
            val statusCode = when {
                e.message?.contains("expired", ignoreCase = true) == true -> HttpStatus.GONE
                else -> HttpStatus.BAD_REQUEST
            }
            
            ResponseEntity.status(statusCode)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Cannot reject check-in"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while rejecting the check-in"
                    )
                )
        }
    }
    
    /**
     * Cancel a pending check-in request.
     * 
     * Parents use this endpoint to cancel a check-in request they no longer need.
     * Only pending requests can be cancelled.
     * 
     * @param requestId The ID of the check-in request to cancel
     * @param authentication Current authenticated user (parent)
     * @return Success message
     */
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasRole('USER')")
    fun cancelRequest(
        @PathVariable requestId: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val userId = authentication.getCurrentUserId()
            checkInRequestService.cancelCheckInRequest(requestId, userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Check-in request cancelled successfully",
                    data = Unit
                )
            )
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Access denied"
                    )
                )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid request"
                    )
                )
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Cannot cancel check-in request"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while cancelling the check-in request"
                    )
                )
        }
    }
    
    /**
     * Get all active check-in requests for the current user.
     * 
     * Parents use this endpoint to view all their pending check-in requests
     * across all their children.
     * 
     * @param authentication Current authenticated user (parent)
     * @return List of active CheckInRequestResponse
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    fun getActiveRequests(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<CheckInRequestResponse>>> {
        return try {
            val userId = authentication.getCurrentUserId()
            val requests = checkInRequestService.getActiveRequestsForParent(userId)
            
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Active check-in requests retrieved successfully",
                    data = requests
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(
                    ApiResponse(
                        success = false,
                        message = e.message ?: "Invalid request"
                    )
                )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                    ApiResponse(
                        success = false,
                        message = "An error occurred while retrieving active check-in requests"
                    )
                )
        }
    }
}
