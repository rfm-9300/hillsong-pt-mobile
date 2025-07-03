package example.com.data.db.user

import example.com.security.token.RefreshResult
import example.com.security.token.TokenClaim
import example.com.security.token.TokenConfig
import example.com.security.token.TokenPair

interface UserRepository {
    suspend fun getUser(email: String): User?
    suspend fun getUserById(userId: Int): User?
    suspend fun addUser(user: User) : Boolean
    suspend fun getUserProfile(userId: Int): UserProfile?
    suspend fun updateUserProfile(userProfile: UserProfile): Boolean
    
    // Google authentication methods
    suspend fun getUserByGoogleId(googleId: String): User?
    suspend fun createOrUpdateGoogleUser(
        email: String, 
        googleId: String, 
        firstName: String = "", 
        lastName: String = "", 
        profileImageUrl: String = ""
    ): User?
    
    // Facebook authentication methods
    suspend fun getUserByFacebookId(facebookId: String): User?
    suspend fun createOrUpdateFacebookUser(
        email: String, 
        facebookId: String, 
        firstName: String = "", 
        lastName: String = "", 
        profileImageUrl: String = ""
    ): User?
    
    // Password reset methods
    suspend fun saveResetToken(email: String, token: String, expiresAt: Long): Boolean
    suspend fun getUserByResetToken(token: String): User?
    suspend fun updatePassword(userId: Int, newPasswordHash: String, newSalt: String): Boolean
    suspend fun deleteResetToken(userId: Int): Boolean

    // token management methods
    suspend fun saveTokenPair(userId: Int, accessToken: String, refreshToken: String, deviceInfo: String?): Int
    suspend fun revokeTokenById(tokenId: Int): Boolean
    suspend fun revokeAllUserTokens(userId: Int): Boolean
    suspend fun findTokenByRefreshToken(refreshToken: String): TokenPair?
    suspend fun refreshAccessToken(refreshToken: String, config: TokenConfig, vararg claims: TokenClaim): RefreshResult?
}