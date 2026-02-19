package rfm.com.service

import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import rfm.com.dto.CheckInStatusNotification
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
     */
    fun notifyCheckInRequestStatus(
        parentUserId: String,
        requestId: String,
        kidId: String,
        kidsServiceId: String,
        status: CheckInRequestStatus,
        message: String? = null,
        rejectionReason: String? = null,
        approvedBy: String? = null,
        attendanceId: String? = null
    ) {
        try {
            val notification = CheckInStatusNotification(
                requestId = requestId,
                childId = kidId,
                serviceId = kidsServiceId,
                status = status.name,
                timestamp = LocalDateTime.now(),
                message = message,
                rejectionReason = rejectionReason,
                approvedBy = approvedBy,
                attendanceId = attendanceId
            )
            
            messagingTemplate.convertAndSendToUser(
                parentUserId,
                "/queue/checkin-status",
                notification
            )
            
            logger.info(
                "Sent check-in status notification to user {}: request={}, status={}",
                parentUserId,
                requestId,
                status.name
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to send check-in status notification to user {}: request={}, status={}",
                parentUserId,
                requestId,
                status.name,
                e
            )
        }
    }
    
    /**
     * Convenience method to notify check-in approval.
     */
    fun notifyCheckInApproved(
        parentUserId: String,
        requestId: String,
        kidId: String,
        kidsServiceId: String,
        approvedBy: String,
        attendanceId: String
    ) {
        notifyCheckInRequestStatus(
            parentUserId = parentUserId,
            requestId = requestId,
            kidId = kidId,
            kidsServiceId = kidsServiceId,
            status = CheckInRequestStatus.APPROVED,
            message = "Your check-in request has been approved",
            approvedBy = approvedBy,
            attendanceId = attendanceId
        )
    }
    
    /**
     * Convenience method to notify check-in rejection.
     */
    fun notifyCheckInRejected(
        parentUserId: String,
        requestId: String,
        kidId: String,
        kidsServiceId: String,
        rejectedBy: String,
        reason: String
    ) {
        notifyCheckInRequestStatus(
            parentUserId = parentUserId,
            requestId = requestId,
            kidId = kidId,
            kidsServiceId = kidsServiceId,
            status = CheckInRequestStatus.REJECTED,
            message = "Your check-in request has been rejected",
            rejectionReason = reason
        )
    }
}
