package rfm.com.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entity representing a QR code-based check-in request.
 * 
 * This entity acts as an intermediary between a parent's intent to check in their child
 * and the actual check-in completion by staff. The workflow is:
 * 1. Parent creates a check-in request (generates QR code with token)
 * 2. Staff scans the QR code and retrieves request details
 * 3. Staff approves or rejects the request
 * 4. If approved, a KidAttendance record is created
 * 
 * Security features:
 * - Unique, cryptographically secure token
 * - Time-limited expiration (default 15 minutes)
 * - One-time use (invalidated after approval)
 * - Complete audit trail of who requested and who processed
 */
@Entity
@Table(name = "check_in_request")
data class CheckInRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kid_id", nullable = false)
    val kid: Kid,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kids_service_id", nullable = false)
    val kidsService: KidsService,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    val requestedBy: UserProfile,
    
    @Column(name = "token", nullable = false, unique = true, length = 64)
    val token: String,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: CheckInRequestStatus = CheckInRequestStatus.PENDING,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    val processedBy: UserProfile? = null,
    
    @Column(name = "processed_at")
    val processedAt: LocalDateTime? = null,
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    val rejectionReason: String? = null,
    
    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,
    
    @OneToOne(mappedBy = "checkInRequest", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val attendance: KidAttendance? = null
) {
    /**
     * Checks if the request has expired based on the expiration timestamp.
     * @return true if current time is after expiration time
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    /**
     * Checks if the request is valid for processing.
     * A request is valid if it's in PENDING status and not expired.
     * @return true if request can be displayed to staff
     */
    fun isValid(): Boolean {
        return status == CheckInRequestStatus.PENDING && !isExpired()
    }
    
    /**
     * Checks if the request can be processed (approved or rejected).
     * Same as isValid() - request must be pending and not expired.
     * @return true if staff can approve or reject this request
     */
    fun canBeProcessed(): Boolean {
        return isValid()
    }
    
    /**
     * Gets the number of seconds remaining until expiration.
     * @return seconds until expiration, or 0 if already expired
     */
    fun getSecondsUntilExpiration(): Long {
        if (isExpired()) return 0
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).seconds
    }
    
    /**
     * Checks if this request has medical alerts that staff should be aware of.
     * @return true if child has medical notes, allergies, or special needs
     */
    fun hasMedicalAlerts(): Boolean {
        return !kid.medicalNotes.isNullOrBlank() || 
               !kid.allergies.isNullOrBlank() || 
               !kid.specialNeeds.isNullOrBlank()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CheckInRequest
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
    
    override fun toString(): String {
        return "CheckInRequest(id=$id, kid=${kid.fullName}, service=${kidsService.name}, " +
               "status=$status, createdAt=$createdAt, expiresAt=$expiresAt)"
    }
}
