package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.User
import rfm.com.entity.UserToken
import java.time.LocalDateTime

@Repository
interface UserTokenRepository : JpaRepository<UserToken, Long> {
    
    /**
     * Find token by access token
     */
    fun findByAccessToken(accessToken: String): UserToken?
    
    /**
     * Find token by refresh token
     */
    fun findByRefreshToken(refreshToken: String): UserToken?
    
    /**
     * Find token by ID with user eagerly loaded
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.id = :id")
    fun findByIdWithUser(@Param("id") id: Long): UserToken?
    
    /**
     * Find all tokens for a specific user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user ORDER BY ut.createdAt DESC")
    fun findByUser(@Param("user") user: User): List<UserToken>
    
    /**
     * Find all tokens for a specific user with pagination
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user ORDER BY ut.createdAt DESC")
    fun findByUser(@Param("user") user: User, pageable: Pageable): Page<UserToken>
    
    /**
     * Find all active (non-revoked) tokens for a user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = false ORDER BY ut.createdAt DESC")
    fun findActiveTokensByUser(@Param("user") user: User): List<UserToken>
    
    /**
     * Find all revoked tokens for a user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = true ORDER BY ut.createdAt DESC")
    fun findRevokedTokensByUser(@Param("user") user: User): List<UserToken>
    
    /**
     * Find all expired access tokens
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.accessTokenExpiresAt < :currentTime")
    fun findExpiredAccessTokens(@Param("currentTime") currentTime: Long): List<UserToken>
    
    /**
     * Find all expired refresh tokens
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.refreshTokenExpiresAt < :currentTime")
    fun findExpiredRefreshTokens(@Param("currentTime") currentTime: Long): List<UserToken>
    
    /**
     * Find all expired tokens (both access and refresh)
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.accessTokenExpiresAt < :currentTime OR ut.refreshTokenExpiresAt < :currentTime")
    fun findAllExpiredTokens(@Param("currentTime") currentTime: Long): List<UserToken>
    
    /**
     * Find valid (non-revoked and non-expired) tokens for a user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = false AND ut.accessTokenExpiresAt > :currentTime ORDER BY ut.createdAt DESC")
    fun findValidTokensByUser(@Param("user") user: User, @Param("currentTime") currentTime: Long): List<UserToken>
    
    /**
     * Find tokens by device info
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.deviceInfo = :deviceInfo ORDER BY ut.createdAt DESC")
    fun findByDeviceInfo(@Param("deviceInfo") deviceInfo: String): List<UserToken>
    
    /**
     * Find tokens by device info for a specific user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user AND ut.deviceInfo = :deviceInfo ORDER BY ut.createdAt DESC")
    fun findByUserAndDeviceInfo(@Param("user") user: User, @Param("deviceInfo") deviceInfo: String): List<UserToken>
    
    /**
     * Find tokens created within a date range
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.createdAt BETWEEN :startDate AND :endDate ORDER BY ut.createdAt DESC")
    fun findByCreatedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<UserToken>
    
    /**
     * Find tokens last used within a date range
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.lastUsedAt BETWEEN :startDate AND :endDate ORDER BY ut.lastUsedAt DESC")
    fun findByLastUsedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<UserToken>
    
    /**
     * Find recently used tokens (used within the last N days)
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.lastUsedAt >= :fromDate ORDER BY ut.lastUsedAt DESC")
    fun findRecentlyUsedTokens(@Param("fromDate") fromDate: LocalDateTime): List<UserToken>
    
    /**
     * Find inactive tokens (not used for a long time)
     */
    @Query("SELECT ut FROM UserToken ut LEFT JOIN FETCH ut.user WHERE ut.lastUsedAt < :cutoffDate ORDER BY ut.lastUsedAt ASC")
    fun findInactiveTokens(@Param("cutoffDate") cutoffDate: LocalDateTime): List<UserToken>
    
    /**
     * Count tokens for a specific user
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.user = :user")
    fun countByUser(@Param("user") user: User): Long
    
    /**
     * Count active tokens for a specific user
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = false")
    fun countActiveTokensByUser(@Param("user") user: User): Long
    
    /**
     * Count revoked tokens for a specific user
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = true")
    fun countRevokedTokensByUser(@Param("user") user: User): Long
    
    /**
     * Count expired access tokens
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.accessTokenExpiresAt < :currentTime")
    fun countExpiredAccessTokens(@Param("currentTime") currentTime: Long): Long
    
    /**
     * Count expired refresh tokens
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.refreshTokenExpiresAt < :currentTime")
    fun countExpiredRefreshTokens(@Param("currentTime") currentTime: Long): Long
    
    /**
     * Count valid tokens for a user
     */
    @Query("SELECT COUNT(ut) FROM UserToken ut WHERE ut.user = :user AND ut.isRevoked = false AND ut.accessTokenExpiresAt > :currentTime")
    fun countValidTokensByUser(@Param("user") user: User, @Param("currentTime") currentTime: Long): Long
    
    /**
     * Check if access token exists and is valid
     */
    @Query("SELECT COUNT(ut) > 0 FROM UserToken ut WHERE ut.accessToken = :accessToken AND ut.isRevoked = false AND ut.accessTokenExpiresAt > :currentTime")
    fun isAccessTokenValid(@Param("accessToken") accessToken: String, @Param("currentTime") currentTime: Long): Boolean
    
    /**
     * Check if refresh token exists and is valid
     */
    @Query("SELECT COUNT(ut) > 0 FROM UserToken ut WHERE ut.refreshToken = :refreshToken AND ut.isRevoked = false AND ut.refreshTokenExpiresAt > :currentTime")
    fun isRefreshTokenValid(@Param("refreshToken") refreshToken: String, @Param("currentTime") currentTime: Long): Boolean
    
    /**
     * Revoke all tokens for a specific user
     */
    @Modifying
    @Query("UPDATE UserToken ut SET ut.isRevoked = true WHERE ut.user = :user")
    fun revokeAllTokensForUser(@Param("user") user: User): Int
    
    /**
     * Revoke specific token by access token
     */
    @Modifying
    @Query("UPDATE UserToken ut SET ut.isRevoked = true WHERE ut.accessToken = :accessToken")
    fun revokeTokenByAccessToken(@Param("accessToken") accessToken: String): Int
    
    /**
     * Revoke specific token by refresh token
     */
    @Modifying
    @Query("UPDATE UserToken ut SET ut.isRevoked = true WHERE ut.refreshToken = :refreshToken")
    fun revokeTokenByRefreshToken(@Param("refreshToken") refreshToken: String): Int
    
    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.accessTokenExpiresAt < :currentTime AND ut.refreshTokenExpiresAt < :currentTime")
    fun deleteExpiredTokens(@Param("currentTime") currentTime: Long): Int
    
    /**
     * Delete revoked tokens older than a specific date
     */
    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.isRevoked = true AND ut.createdAt < :cutoffDate")
    fun deleteOldRevokedTokens(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    /**
     * Update last used time for a token
     */
    @Modifying
    @Query("UPDATE UserToken ut SET ut.lastUsedAt = :lastUsedAt WHERE ut.accessToken = :accessToken")
    fun updateLastUsedTime(@Param("accessToken") accessToken: String, @Param("lastUsedAt") lastUsedAt: LocalDateTime): Int
    
    /**
     * Find most recently created token for a user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user ORDER BY ut.createdAt DESC LIMIT 1")
    fun findMostRecentTokenForUser(@Param("user") user: User): UserToken?
    
    /**
     * Find most recently used token for a user
     */
    @Query("SELECT ut FROM UserToken ut WHERE ut.user = :user ORDER BY ut.lastUsedAt DESC LIMIT 1")
    fun findMostRecentlyUsedTokenForUser(@Param("user") user: User): UserToken?
}