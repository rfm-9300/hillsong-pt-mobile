package rfm.com.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.CheckInRequest
import rfm.com.entity.CheckInRequestStatus
import rfm.com.entity.Kid
import rfm.com.entity.KidsService
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

/**
 * Repository interface for CheckInRequest entity.
 * 
 * Provides data access methods for managing QR code-based check-in requests.
 * Supports queries for token lookup, status filtering, expiration cleanup,
 * and retrieving active requests for parents and services.
 */
@Repository
interface CheckInRequestRepository : JpaRepository<CheckInRequest, Long> {
    
    /**
     * Find check-in request by token.
     * Used when staff scans a QR code to retrieve request details.
     * 
     * @param token The unique token from the QR code
     * @return CheckInRequest if found, null otherwise
     */
    @Query("SELECT cir FROM CheckInRequest cir " +
           "LEFT JOIN FETCH cir.kid " +
           "LEFT JOIN FETCH cir.kidsService " +
           "LEFT JOIN FETCH cir.requestedBy " +
           "WHERE cir.token = :token")
    fun findByToken(@Param("token") token: String): CheckInRequest?
    
    /**
     * Find check-in request by kid, service, and status.
     * Used to check for existing pending requests before creating a new one.
     * 
     * @param kid The child for the check-in request
     * @param kidsService The service for the check-in request
     * @param status The status to filter by
     * @return CheckInRequest if found, null otherwise
     */
    @Query("SELECT cir FROM CheckInRequest cir " +
           "WHERE cir.kid = :kid " +
           "AND cir.kidsService = :kidsService " +
           "AND cir.status = :status")
    fun findByKidAndKidsServiceAndStatus(
        @Param("kid") kid: Kid,
        @Param("kidsService") kidsService: KidsService,
        @Param("status") status: CheckInRequestStatus
    ): CheckInRequest?
    
    /**
     * Find check-in requests by status that expired before a given time.
     * Used by scheduled job to mark expired requests.
     * 
     * @param status The status to filter by (typically PENDING)
     * @param expiresAt The expiration timestamp to compare against
     * @return List of expired check-in requests
     */
    @Query("SELECT cir FROM CheckInRequest cir " +
           "WHERE cir.status = :status " +
           "AND cir.expiresAt < :expiresAt")
    fun findByStatusAndExpiresAtBefore(
        @Param("status") status: CheckInRequestStatus,
        @Param("expiresAt") expiresAt: LocalDateTime
    ): List<CheckInRequest>
    
    /**
     * Find check-in requests by parent and status list.
     * Used to retrieve all active (pending) requests for a parent's children.
     * 
     * @param requestedBy The parent who created the requests
     * @param statuses List of statuses to filter by
     * @return List of check-in requests matching the criteria
     */
    @Query("SELECT cir FROM CheckInRequest cir " +
           "LEFT JOIN FETCH cir.kid " +
           "LEFT JOIN FETCH cir.kidsService " +
           "WHERE cir.requestedBy = :requestedBy " +
           "AND cir.status IN :statuses " +
           "ORDER BY cir.createdAt DESC")
    fun findByRequestedByAndStatusIn(
        @Param("requestedBy") requestedBy: UserProfile,
        @Param("statuses") statuses: List<CheckInRequestStatus>
    ): List<CheckInRequest>
    
    /**
     * Find check-in requests by service and status list.
     * Used to retrieve all pending requests for a specific service.
     * 
     * @param kidsService The service to filter by
     * @param statuses List of statuses to filter by
     * @return List of check-in requests matching the criteria
     */
    @Query("SELECT cir FROM CheckInRequest cir " +
           "LEFT JOIN FETCH cir.kid " +
           "LEFT JOIN FETCH cir.requestedBy " +
           "WHERE cir.kidsService = :kidsService " +
           "AND cir.status IN :statuses " +
           "ORDER BY cir.createdAt DESC")
    fun findByKidsServiceAndStatusIn(
        @Param("kidsService") kidsService: KidsService,
        @Param("statuses") statuses: List<CheckInRequestStatus>
    ): List<CheckInRequest>
}
