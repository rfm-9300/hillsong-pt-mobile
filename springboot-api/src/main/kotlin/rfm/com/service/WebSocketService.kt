package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import rfm.com.dto.CheckInStatusNotification
import rfm.com.entity.CheckInRequest
import rfm.com.entity.CheckInRequestStatus
import java.time.LocalDateTime

/**
 * Service for sending real-time WebSocket notifications to users.
 * 
 * This service handles sending check-in status updates to parents via WebSocket
 * when their check-in requests are processed by staff.
 * 
 * Notifications are sent to user-specific queues at:
 * /user/{userId}/queue/checkin-status
 */
@Service
class WebSocketService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    
    private val logger = LoggerFactory.getLogger(WebSocketService::class.java)
    
    /**
     * Sends a check-in request status notification to a specific parent user.
     * 
     * The notification is sent to the user's personal queue and includes:
     * - Request ID and status
     * - Child and service information
     * - Timestamp of the status change
     * - Additional details based on status (approval info, rejection reason, etc.)
     * 
     * @param parentUserId The ID of the parent user to notify
     * @param request The check-in request that was processed
     * @param status The new status of the request
     * @param message Optional message to include in the notification
     * @param rejectionReason Optional rejection reason (for REJECTED status)
     * @param approvedBy Optional staff name who approved (for APPROVED status)
     * @param attendanceId Optional attendance record ID (for APPROVED status)
     */
    fun notifyCheckInRequestStatus(
        parentUserId: Long,
        request: CheckInRequest,
        status: CheckInRequestStatus,
        message: String? = null,
        rejectionReason: String? = null,
        approvedBy: String? = null,
        attendanceId: Long? = null
    ) {
        try {
            val notification = CheckInStatusNotification(
                requestId = request.id!!,
                childId = request.kid.id!!,
                serviceId = request.kidsService.id!!,
                status = status.name,
                timestamp = LocalDateTime.now(),
                message = message,
                rejectionReason = rejectionReason,
                approvedBy = approvedBy,
                attendanceId = attendanceId
            )
            
            // Send notification to user-specific queue
            // The destination will be: /user/{parentUserId}/queue/checkin-status
            messagingTemplate.convertAndSendToUser(
                parentUserId.toString(),
                "/queue/checkin-status",
                notification
            )
            
            logger.info(
                "Sent check-in status notification to user {}: request={}, status={}, child={}, service={}",
                parentUserId,
                request.id,
                status.name,
                request.kid.fullName,
                request.kidsService.name
            )
        } catch (e: Exception) {
            // Log error but don't throw - WebSocket notification failure shouldn't break the flow
            logger.error(
                "Failed to send check-in status notification to user {}: request={}, status={}",
                parentUserId,
                request.id,
                status.name,
                e
            )
        }
    }
    
    /**
     * Convenience method to notify check-in approval.
     * 
     * @param parentUserId The ID of the parent user to notify
     * @param request The approved check-in request
     * @param approvedBy The name of the staff member who approved
     * @param attendanceId The ID of the created attendance record
     */
    fun notifyCheckInApproved(
        parentUserId: Long,
        request: CheckInRequest,
        approvedBy: String,
        attendanceId: Long
    ) {
        notifyCheckInRequestStatus(
            parentUserId = parentUserId,
            request = request,
            status = CheckInRequestStatus.APPROVED,
            message = "Your check-in request has been approved",
            approvedBy = approvedBy,
            attendanceId = attendanceId
        )
    }
    
    /**
     * Convenience method to notify check-in rejection.
     * 
     * @param parentUserId The ID of the parent user to notify
     * @param request The rejected check-in request
     * @param rejectedBy The name of the staff member who rejected
     * @param reason The reason for rejection
     */
    fun notifyCheckInRejected(
        parentUserId: Long,
        request: CheckInRequest,
        rejectedBy: String,
        reason: String
    ) {
        notifyCheckInRequestStatus(
            parentUserId = parentUserId,
            request = request,
            status = CheckInRequestStatus.REJECTED,
            message = "Your check-in request has been rejected",
            rejectionReason = reason
        )
    }
}
