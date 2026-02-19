package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "check_in_requests")
data class CheckInRequest(
    @Id
    val id: String? = null,

    val kidId: String,

    val kidsServiceId: String,

    val requestedById: String,

    @Indexed(unique = true)
    val token: String,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val expiresAt: LocalDateTime,

    val status: CheckInRequestStatus = CheckInRequestStatus.PENDING,

    val processedById: String? = null,

    val processedAt: LocalDateTime? = null,

    val rejectionReason: String? = null,

    val notes: String? = null,

    val attendanceId: String? = null
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    fun isValid(): Boolean {
        return status == CheckInRequestStatus.PENDING && !isExpired()
    }

    fun canBeProcessed(): Boolean {
        return isValid()
    }

    fun getSecondsUntilExpiration(): Long {
        if (isExpired()) return 0
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).seconds
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CheckInRequest
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String {
        return "CheckInRequest(id=$id, kidId=$kidId, kidsServiceId=$kidsServiceId, " +
               "status=$status, createdAt=$createdAt, expiresAt=$expiresAt)"
    }
}
