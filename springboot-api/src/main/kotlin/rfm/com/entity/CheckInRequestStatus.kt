package rfm.com.entity

/**
 * Status of a check-in request in the QR code-based check-in system.
 * 
 * The lifecycle of a check-in request typically follows:
 * PENDING -> APPROVED (successful check-in)
 * PENDING -> REJECTED (staff denies check-in)
 * PENDING -> EXPIRED (token expires before processing)
 * PENDING -> CANCELLED (parent cancels request)
 */
enum class CheckInRequestStatus {
    /**
     * Request has been created and is awaiting staff verification.
     * QR code is valid and can be scanned.
     */
    PENDING,
    
    /**
     * Request has been approved by staff and check-in is complete.
     * An attendance record has been created.
     */
    APPROVED,
    
    /**
     * Request has been rejected by staff with a reason.
     * No attendance record is created.
     */
    REJECTED,
    
    /**
     * Request has expired due to timeout (default 15 minutes).
     * QR code is no longer valid.
     */
    EXPIRED,
    
    /**
     * Request has been cancelled by the parent before processing.
     * QR code is no longer valid.
     */
    CANCELLED
}
