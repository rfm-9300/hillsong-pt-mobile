package rfm.com.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.CheckInRequest
import rfm.com.entity.CheckInRequestStatus
import java.time.LocalDateTime

@Repository
interface CheckInRequestRepository : MongoRepository<CheckInRequest, String> {
    
    fun findByToken(token: String): CheckInRequest?
    
    fun findByKidIdAndKidsServiceIdAndStatus(
        kidId: String,
        kidsServiceId: String,
        status: CheckInRequestStatus
    ): CheckInRequest?
    
    fun findByStatusAndExpiresAtBefore(
        status: CheckInRequestStatus,
        expiresAt: LocalDateTime
    ): List<CheckInRequest>
    
    fun findByRequestedByIdAndStatusInOrderByCreatedAtDesc(
        requestedById: String,
        statuses: List<CheckInRequestStatus>
    ): List<CheckInRequest>
    
    fun findByKidsServiceIdAndStatusInOrderByCreatedAtDesc(
        kidsServiceId: String,
        statuses: List<CheckInRequestStatus>
    ): List<CheckInRequest>
}
