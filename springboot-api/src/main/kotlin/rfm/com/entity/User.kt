package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,

    @Indexed(unique = true, sparse = true)
    val authId: String? = null,

    @Indexed(unique = true)
    val email: String,

    // Embedded profile fields (previously in UserProfile)
    val firstName: String = "",

    val lastName: String = "",

    val phone: String = "",

    val imagePath: String = "",

    val isAdmin: Boolean = false,

    // Roles stored as DBRefs to match auth-service
    @org.springframework.data.mongodb.core.mapping.DBRef
    val roles: MutableList<Role> = mutableListOf(),

    @Indexed(unique = true, sparse = true)
    val qrToken: String? = null,

    @CreatedDate
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "User(id=$id, authId=$authId, email='$email')"
}