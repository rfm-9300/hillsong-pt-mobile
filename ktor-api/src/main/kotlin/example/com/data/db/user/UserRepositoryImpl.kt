package example.com.data.db.user

import example.com.data.db.event.EventAttendeeTable
import example.com.data.db.event.EventTable
import example.com.data.db.event.toEvent
import example.com.security.token.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class UserRepositoryImpl (
    private val tokenService: TokenService
): UserRepository {
    override suspend fun getUser(email: String): User? = suspendTransaction {
        UserTable
            .select { UserTable.email eq email }
            .singleOrNull()?.let {
                User(
                    id = it[UserTable.id].value,
                    email = it[UserTable.email],
                    password = it[UserTable.password],
                    salt = it[UserTable.salt],
                    verified = it[UserTable.verified],
                    googleId = it[UserTable.googleId],
                    authProvider = AuthProvider.valueOf(it[UserTable.authProvider])
                )
            }
    }

    override suspend fun getUserById(userId: Int): User? = suspendTransaction {
        UserTable
            .select { UserTable.id eq userId }
            .singleOrNull()?.let {
                User(
                    id = it[UserTable.id].value,
                    email = it[UserTable.email],
                    password = it[UserTable.password],
                    salt = it[UserTable.salt],
                    verified = it[UserTable.verified],
                    verificationToken = it[UserTable.verificationToken],
                    googleId = it[UserTable.googleId],
                    authProvider = AuthProvider.valueOf(it[UserTable.authProvider])
                )
            }
    }

    override suspend fun getUserByGoogleId(googleId: String): User? = suspendTransaction {
        UserTable
            .select { UserTable.googleId eq googleId }
            .singleOrNull()?.let {
                User(
                    id = it[UserTable.id].value,
                    email = it[UserTable.email],
                    password = "",
                    salt = "",
                    verified = true,
                    googleId = it[UserTable.googleId],
                    authProvider = AuthProvider.valueOf(it[UserTable.authProvider])
                )
            }
    }

    override suspend fun getUserByFacebookId(facebookId: String): User? = suspendTransaction {
        UserTable
            .select { UserTable.facebookId eq facebookId }
            .singleOrNull()?.let {
                User(
                    id = it[UserTable.id].value,
                    email = it[UserTable.email],
                    password = "",
                    salt = "",
                    verified = true,
                    facebookId = it[UserTable.facebookId],
                    authProvider = AuthProvider.valueOf(it[UserTable.authProvider])
                )
            }
    }

    override suspend fun createOrUpdateGoogleUser(
        email: String, 
        googleId: String, 
        firstName: String, 
        lastName: String, 
        profileImageUrl: String
    ): User? = suspendTransaction {
        try {
            // Check if user with this Google ID already exists
            val existingUser = UserTable
                .select { UserTable.googleId eq googleId }
                .singleOrNull()
                
            if (existingUser != null) {
                // Update existing user
                UserTable.update({ UserTable.id eq existingUser[UserTable.id] }) {
                    it[UserTable.email] = email
                    it[UserTable.verified] = true
                }
                
                // Update profile if it exists
                val userProfileId = existingUser[UserTable.id]
                val userProfile = UserProfilesTable
                    .select { UserProfilesTable.userId eq userProfileId }
                    .singleOrNull()
                    
                if (userProfile != null) {
                    UserProfilesTable.update({ UserProfilesTable.userId eq userProfileId }) {
                        it[UserProfilesTable.firstName] = firstName
                        it[UserProfilesTable.lastName] = lastName
                        it[UserProfilesTable.email] = email
                        // For Google users, store the complete URL
                        if (profileImageUrl.isNotEmpty()) {
                            it[UserProfilesTable.imagePath] = profileImageUrl
                        }
                    }
                }
                
                return@suspendTransaction User(
                    id = existingUser[UserTable.id].value,
                    email = email,
                    verified = true,
                    googleId = googleId,
                    authProvider = AuthProvider.GOOGLE
                )
            } else {
                // Check if user with same email exists
                val existingEmailUser = UserTable
                    .select { UserTable.email eq email }
                    .singleOrNull()
                    
                if (existingEmailUser != null) {
                    // Update the existing user to link with Google
                    UserTable.update({ UserTable.id eq existingEmailUser[UserTable.id] }) {
                        it[UserTable.googleId] = googleId
                        it[UserTable.authProvider] = AuthProvider.GOOGLE.name
                        it[UserTable.verified] = true
                    }

                    return@suspendTransaction User(
                        id = existingEmailUser[UserTable.id].value,
                        email = email,
                        verified = true,
                        googleId = googleId,
                        authProvider = AuthProvider.GOOGLE
                    )
                } else {
                    // Create new user
                    val userId = UserTable.insert {
                        it[UserTable.email] = email
                        it[UserTable.password] = ""
                        it[UserTable.salt] = ""
                        it[UserTable.verified] = true
                        it[UserTable.googleId] = googleId
                        it[UserTable.authProvider] = AuthProvider.GOOGLE.name
                        it[UserTable.createdAt] = LocalDateTime.now()
                    } get UserTable.id
                    
                    // Create user profile with Google profile image URL
                    UserProfilesTable.insert {
                        it[UserProfilesTable.userId] = userId
                        it[UserProfilesTable.firstName] = firstName
                        it[UserProfilesTable.lastName] = lastName
                        it[UserProfilesTable.email] = email
                        it[UserProfilesTable.phone] = ""
                        it[UserProfilesTable.joinedAt] = LocalDateTime.now()
                        // Store the full URL for Google profile images
                        it[UserProfilesTable.imagePath] = profileImageUrl.ifEmpty { "profile" }
                    }
                    
                    return@suspendTransaction User(
                        id = userId.value,
                        email = email,
                        verified = true,
                        googleId = googleId,
                        authProvider = AuthProvider.GOOGLE
                    )
                }
            }
        } catch (e: Exception) {
            println("Error creating/updating Google user: ${e.message}")
            null
        }
    }

    override suspend fun createOrUpdateFacebookUser(
        email: String, 
        facebookId: String, 
        firstName: String, 
        lastName: String, 
        profileImageUrl: String
    ): User? = suspendTransaction {
        try {
            // Check if user with this Facebook ID already exists
            val existingUser = UserTable
                .select { UserTable.facebookId eq facebookId }
                .singleOrNull()
                
            if (existingUser != null) {
                // Update existing user
                UserTable.update({ UserTable.id eq existingUser[UserTable.id] }) {
                    it[UserTable.email] = email
                    it[UserTable.verified] = true
                }
                
                // Update profile if it exists
                val userProfileId = existingUser[UserTable.id]
                val userProfile = UserProfilesTable
                    .select { UserProfilesTable.userId eq userProfileId }
                    .singleOrNull()
                    
                if (userProfile != null) {
                    UserProfilesTable.update({ UserProfilesTable.userId eq userProfileId }) {
                        it[UserProfilesTable.firstName] = firstName
                        it[UserProfilesTable.lastName] = lastName
                        it[UserProfilesTable.email] = email
                        // For Facebook users, store the complete URL
                        if (profileImageUrl.isNotEmpty()) {
                            it[UserProfilesTable.imagePath] = profileImageUrl
                        }
                    }
                }
                
                return@suspendTransaction User(
                    id = existingUser[UserTable.id].value,
                    email = email,
                    verified = true,
                    facebookId = facebookId,
                    authProvider = AuthProvider.FACEBOOK
                )
            } else {
                // Check if user with same email exists
                val existingEmailUser = UserTable
                    .select { UserTable.email eq email }
                    .singleOrNull()
                    
                if (existingEmailUser != null) {
                    // Update the existing user to link with Facebook
                    UserTable.update({ UserTable.id eq existingEmailUser[UserTable.id] }) {
                        it[UserTable.facebookId] = facebookId
                        it[UserTable.authProvider] = AuthProvider.FACEBOOK.name
                        it[UserTable.verified] = true
                    }
                    
                    // Update profile image to use Facebook profile image
                    val userProfileId = existingEmailUser[UserTable.id]
                    UserProfilesTable.update({ UserProfilesTable.userId eq userProfileId }) {
                        if (profileImageUrl.isNotEmpty()) {
                            it[UserProfilesTable.imagePath] = profileImageUrl
                        }
                    }
                    
                    return@suspendTransaction User(
                        id = existingEmailUser[UserTable.id].value,
                        email = email,
                        verified = true,
                        facebookId = facebookId,
                        authProvider = AuthProvider.FACEBOOK
                    )
                } else {
                    // Create new user
                    val userId = UserTable.insert {
                        it[UserTable.email] = email
                        it[UserTable.password] = ""
                        it[UserTable.salt] = ""
                        it[UserTable.verified] = true
                        it[UserTable.facebookId] = facebookId
                        it[UserTable.authProvider] = AuthProvider.FACEBOOK.name
                        it[UserTable.createdAt] = LocalDateTime.now()
                    } get UserTable.id
                    
                    // Create user profile with Facebook profile image URL
                    UserProfilesTable.insert {
                        it[UserProfilesTable.userId] = userId
                        it[UserProfilesTable.firstName] = firstName
                        it[UserProfilesTable.lastName] = lastName
                        it[UserProfilesTable.email] = email
                        it[UserProfilesTable.phone] = ""
                        it[UserProfilesTable.joinedAt] = LocalDateTime.now()
                        // Store the full URL for Facebook profile images
                        it[UserProfilesTable.imagePath] = profileImageUrl.ifEmpty { "profile" }
                    }
                    
                    return@suspendTransaction User(
                        id = userId.value,
                        email = email,
                        verified = true,
                        facebookId = facebookId,
                        authProvider = AuthProvider.FACEBOOK
                    )
                }
            }
        } catch (e: Exception) {
            println("Error creating/updating Facebook user: ${e.message}")
            null
            }
    }

    override suspend fun addUser(user: User): Boolean = suspendTransaction {
        try {
            UserTable.insert {
                it[email] = user.email
                it[password] = user.password
                it[salt] = user.salt
                it[verified] = user.verified
                it[verificationToken] = user.verificationToken
                it[createdAt] = user.createdAt
            }
            UserProfilesTable.insert {
                it[userId] = UserTable.select { UserTable.email eq user.email }.single()[UserTable.id]
                it[firstName] = user.profile?.firstName ?: ""
                it[lastName] = user.profile?.lastName ?: ""
                it[email] = user.profile?.email ?: ""
                it[phone] = user.profile?.phone ?: ""
                it[joinedAt] = user.profile?.joinedAt ?: LocalDateTime.now()
                it[imagePath] = user.profile?.profileImagePath ?: "profile"
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserProfile(userId: Int): UserProfile? = suspendTransaction {
        try {
            val userProfile = UserProfilesTable.select { UserProfilesTable.userId eq userId }.single().toUserProfile()
            val now = LocalDateTime.now()
            
            // Get all events where the user is the organizer
            val hostedEvents = EventTable
                .select { EventTable.organizerId eq userId }
                .map { it.toEvent() }

            // Get all events the user is registered for
            val userEvents = EventAttendeeTable
                .join(EventTable, JoinType.INNER) { EventTable.id eq EventAttendeeTable.eventId }
                .select { EventAttendeeTable.userId eq userId }
                .map { it.toEvent() }
            
            // Split events into past (attended) and future (attending)
            val attendedEvents = userEvents.filter { it.date.isBefore(now) }
            val attendingEvents = userEvents.filter { it.date.isAfter(now) }
            
            // For waiting list events, we need logic from EventWaitingListTable
            // This would be implemented if we had that table
            val waitingListEvents = emptyList<example.com.data.db.event.Event>()

            userProfile.copy(
                hostedEvents = hostedEvents,
                attendedEvents = attendedEvents,
                attendingEvents = attendingEvents,
                waitingListEvents = waitingListEvents
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean = suspendTransaction {
        try {
            UserProfilesTable.update({ UserProfilesTable.userId eq userProfile.userId }) {
                it[firstName] = userProfile.firstName
                it[lastName] = userProfile.lastName
                it[email] = userProfile.email
                it[phone] = userProfile.phone
                it[imagePath] = userProfile.profileImagePath
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun saveResetToken(email: String, token: String, expiresAt: Long): Boolean = suspendTransaction {
        try {
            val user = UserTable.select { UserTable.email eq email }.singleOrNull() ?: return@suspendTransaction false
            
            // Update user record with reset token
            UserTable.update({ UserTable.id eq user[UserTable.id] }) {
                it[resetToken] = token
                it[resetTokenExpiresAt] = expiresAt
            }
            
            // Also create a record in the password reset table for tracking
            PasswordResetTable.insert {
                it[userId] = user[UserTable.id]
                it[this.token] = token
                it[this.expiresAt] = expiresAt
                it[createdAt] = LocalDateTime.now()
                it[isUsed] = false
            }
            
            true
        } catch (e: Exception) {
            println("Error saving reset token: ${e.message}")
            false
        }
    }
    
    override suspend fun getUserByResetToken(token: String): User? = suspendTransaction {
        try {
            val now = System.currentTimeMillis()
            UserTable
                .select { 
                    (UserTable.resetToken eq token) and 
                    (UserTable.resetTokenExpiresAt greater now) 
                }
                .singleOrNull()?.toUser()
        } catch (e: Exception) {
            println("Error getting user by reset token: ${e.message}")
            null
        }
    }
    
    override suspend fun updatePassword(userId: Int, newPasswordHash: String, newSalt: String): Boolean = suspendTransaction {
        try {
            UserTable.update({ UserTable.id eq userId }) {
                it[password] = newPasswordHash
                it[salt] = newSalt
            }
            
            // Also mark the token as used in the password reset table
            val userResetToken = UserTable.select { UserTable.id eq userId }
                .singleOrNull()?.get(UserTable.resetToken)
                
            if (userResetToken != null) {
                PasswordResetTable.update({ (PasswordResetTable.userId eq userId) and (PasswordResetTable.token eq userResetToken) }) {
                    it[isUsed] = true
                }
            }
            
            true
        } catch (e: Exception) {
            println("Error updating password: ${e.message}")
            false
        }
    }
    
    override suspend fun deleteResetToken(userId: Int): Boolean = suspendTransaction {
        try {
            UserTable.update({ UserTable.id eq userId }) {
                it[resetToken] = null
                it[resetTokenExpiresAt] = null
            }
            true
        } catch (e: Exception) {
            println("Error deleting reset token: ${e.message}")
            false
        }
    }

    override suspend fun saveTokenPair(userId: Int, accessToken: String, refreshToken: String, deviceInfo: String?): Int {
        val accessTokenDecoded = tokenService.decodeToken(accessToken)
        val refreshTokenDecoded = tokenService.decodeToken(refreshToken)

        if (accessTokenDecoded == null || refreshTokenDecoded == null) {
            throw IllegalArgumentException("Invalid tokens provided")
        }

        val accessTokenExpires = accessTokenDecoded.expiresAt.time
        val refreshTokenExpires = refreshTokenDecoded.expiresAt.time

        return dbQuery {
            TokenTable.insert {
                it[TokenTable.userId] = userId
                it[TokenTable.accessToken] = accessToken
                it[TokenTable.refreshToken] = refreshToken
                it[TokenTable.accessTokenExpiresAt] = accessTokenExpires
                it[TokenTable.refreshTokenExpiresAt] = refreshTokenExpires
                it[TokenTable.deviceInfo] = deviceInfo
            }.resultedValues?.firstOrNull()?.get(TokenTable.id)?.value ?: -1
        }
    }

    override suspend fun revokeTokenById(tokenId: Int): Boolean {
        return dbQuery {
            TokenTable.update({ TokenTable.id eq tokenId }) {
                it[isRevoked] = true
            } > 0
        }
    }

    override suspend fun revokeAllUserTokens(userId: Int): Boolean {
        return dbQuery {
            TokenTable.update({ TokenTable.userId eq userId }) {
                it[isRevoked] = true
            } > 0
        }
    }

    override suspend fun findTokenByRefreshToken(refreshToken: String): TokenPair? {
        return dbQuery {
            TokenTable.select { TokenTable.refreshToken eq refreshToken and (TokenTable.isRevoked eq false) }
                .mapNotNull { row ->
                    TokenPair(
                        id = row[TokenTable.id].value,
                        userId = row[TokenTable.userId].value,
                        accessToken = row[TokenTable.accessToken],
                        refreshToken = row[TokenTable.refreshToken],
                        accessTokenExpiresAt = row[TokenTable.accessTokenExpiresAt],
                        refreshTokenExpiresAt = row[TokenTable.refreshTokenExpiresAt],
                        isRevoked = row[TokenTable.isRevoked],
                        deviceInfo = row[TokenTable.deviceInfo],
                        createdAt = row[TokenTable.createdAt],
                        lastUsedAt = row[TokenTable.lastUsedAt]
                    )
                }.singleOrNull()
        }
    }

    override suspend fun refreshAccessToken(refreshToken: String, config: TokenConfig, vararg claims: TokenClaim): RefreshResult? {
        val tokenPair = findTokenByRefreshToken(refreshToken) ?: return null

        // Check if refresh token is expired
        if (System.currentTimeMillis() > tokenPair.refreshTokenExpiresAt) {
            revokeTokenById(tokenPair.id)
            return null
        }

        // Generate a new access token
        val newAccessToken = tokenService.generateAuthToken(config, *claims)
        val accessTokenDecoded = tokenService.decodeToken(newAccessToken)
        val accessTokenExpires = accessTokenDecoded?.expiresAt?.time ?: 0

        // Update the token in the database
        dbQuery {
            TokenTable.update({ TokenTable.id eq tokenPair.id }) {
                it[TokenTable.accessToken] = newAccessToken
                it[TokenTable.accessTokenExpiresAt] = accessTokenExpires
                it[TokenTable.lastUsedAt] = LocalDateTime.now()
            }
        }

        // Return new tokens
        return RefreshResult(
            userId = tokenPair.userId,
            newAccessToken = newAccessToken,
            accessTokenExpiresAt = accessTokenExpires,
            refreshToken = refreshToken,
            refreshTokenExpiresAt = tokenPair.refreshTokenExpiresAt
        )
    }
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}