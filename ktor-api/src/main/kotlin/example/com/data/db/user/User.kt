package example.com.data.db.user

import example.com.data.db.event.Event
import example.com.data.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

@Serializable
data class User(
    val id: Int? = null,
    val email: String,
    val password: String = "",
    val salt: String = "",
    val verified: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val verificationToken: String? = null,
    val profile: UserProfile? = null,
    val googleId: String? = null,
    val facebookId: String? = null,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val resetToken: String? = null,
    val resetTokenExpiresAt: Long? = null
)

enum class AuthProvider {
    LOCAL, GOOGLE, FACEBOOK
}

@Serializable
data class UserProfile(
    val id: Int? = null,
    val userId : Int? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String,
    val phone: String = "",
    @Serializable(with = LocalDateTimeSerializer::class)
    val joinedAt: LocalDateTime? = null,
    val hostedEvents : List<Event> = emptyList(),
    val attendedEvents : List<Event> = emptyList(),
    val waitingListEvents: List<Event> = emptyList(),
    val attendingEvents: List<Event> = emptyList(),
    val profileImagePath: String = "",
    val isAdmin: Boolean
)

object UserTable : IntIdTable("user") {
    val email = varchar("email", 128)
    val password = varchar("password", 256)
    val salt = varchar("salt", 256)
    val verified = bool("verified").default(false)
    val createdAt = datetime("created_at")
    val verificationToken = varchar("verification_token", 256).nullable()
    val googleId = varchar("google_id", 256).nullable()
    val facebookId = varchar("facebook_id", 256).nullable()
    val authProvider = varchar("auth_provider", 20).default(AuthProvider.LOCAL.name)
    val resetToken = varchar("reset_token", 256).nullable()
    val resetTokenExpiresAt = long("reset_token_expires_at").nullable()
}

// Table for storing password reset tokens
object PasswordResetTable : IntIdTable("password_reset") {
    val userId = reference("user_id", UserTable)
    val token = varchar("token", 256)
    val expiresAt = long("expires_at")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val isUsed = bool("is_used").default(false)
}

// Table for storing access and refresh tokens
object TokenTable : IntIdTable("user_token") {
    val userId = reference("user_id", UserTable)
    val accessToken = varchar("access_token", 512)
    val refreshToken = varchar("refresh_token", 512)
    val accessTokenExpiresAt = long("access_token_expires_at")
    val refreshTokenExpiresAt = long("refresh_token_expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val deviceInfo = varchar("device_info", 256).nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val lastUsedAt = datetime("last_used_at").default(LocalDateTime.now())
}

// UserProfilesTable now extends IntIdTable
object UserProfilesTable : IntIdTable("user_profile") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val firstName = varchar("first_name", 128)
    val lastName = varchar("last_name", 128)
    val email = varchar("email", 128)
    val phone = varchar("phone", 18)
    val joinedAt = datetime("joined_at").default(LocalDateTime.now())
    val imagePath = varchar("image_path", 500)
    val isAdmin = bool("is_admin").default(false)
}


class UserProfileDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserProfileDao>(UserProfilesTable)

    var firstName by UserProfilesTable.firstName
    var lastName by UserProfilesTable.lastName
    var email by UserProfilesTable.email
    var phone by UserProfilesTable.phone
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)



fun ResultRow.toUser() = User(
    id = this[UserTable.id].value,
    email = this[UserTable.email],
    password = this[UserTable.password],
    salt = this[UserTable.salt],
    verified = this[UserTable.verified],
    createdAt = this[UserTable.createdAt],
    verificationToken = this[UserTable.verificationToken],
    googleId = this[UserTable.googleId],
    facebookId = this[UserTable.facebookId],
    authProvider = AuthProvider.valueOf(this[UserTable.authProvider]),
    resetToken = this[UserTable.resetToken],
    resetTokenExpiresAt = this[UserTable.resetTokenExpiresAt]
)

fun ResultRow.toUserProfile() = UserProfile(
    id = this[UserProfilesTable.id].value,
    userId = this[UserProfilesTable.userId].value,
    firstName = this[UserProfilesTable.firstName],
    lastName = this[UserProfilesTable.lastName],
    email = this[UserProfilesTable.email],
    phone = this[UserProfilesTable.phone],
    joinedAt = this[UserProfilesTable.joinedAt],
    profileImagePath = this[UserProfilesTable.imagePath],
    isAdmin = this[UserProfilesTable.isAdmin]
)