package rfm.com.auth.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,

    @Indexed(unique = true)
    val email: String,

    val password: String? = null, 

    val firstName: String,
    val lastName: String,
    
    val imagePath: String? = null,

    val verified: Boolean = false,
    val verificationToken: String? = null,

    val resetToken: String? = null,
    val resetTokenExpiresAt: Long? = null,

    val authProvider: AuthProvider = AuthProvider.LOCAL,
    
    @Indexed
    val googleId: String? = null,
    
    @Indexed
    val facebookId: String? = null,

    @DBRef
    val roles: MutableSet<Role> = mutableSetOf(),
    
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun getRoleNames(): List<String> {
        return roles.map { it.name }
    }
}
