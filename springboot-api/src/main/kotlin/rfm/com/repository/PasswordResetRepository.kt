package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.PasswordReset
import rfm.com.entity.User
import java.time.LocalDateTime

@Repository
interface PasswordResetRepository : JpaRepository<PasswordReset, Long> {
    
    /**
     * Find password reset by token
     */
    fun findByToken(token: String): PasswordReset?
    
    /**
     * Find password reset by ID with user eagerly loaded
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.id = :id")
    fun findByIdWithUser(@Param("id") id: Long): PasswordReset?
    
    /**
     * Find password reset by token with user eagerly loaded
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.token = :token")
    fun findByTokenWithUser(@Param("token") token: String): PasswordReset?
    
    /**
     * Find all password reset requests for a specific user
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user ORDER BY pr.createdAt DESC")
    fun findByUser(@Param("user") user: User): List<PasswordReset>
    
    /**
     * Find all password reset requests for a specific user with pagination
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user ORDER BY pr.createdAt DESC")
    fun findByUser(@Param("user") user: User, pageable: Pageable): Page<PasswordReset>
    
    /**
     * Find valid (unused and non-expired) password reset requests for a user
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = false AND pr.expiresAt > :currentTime ORDER BY pr.createdAt DESC")
    fun findValidPasswordResetsByUser(@Param("user") user: User, @Param("currentTime") currentTime: Long): List<PasswordReset>
    
    /**
     * Find used password reset requests
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.isUsed = true ORDER BY pr.createdAt DESC")
    fun findUsedPasswordResets(): List<PasswordReset>
    
    /**
     * Find unused password reset requests
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.isUsed = false ORDER BY pr.createdAt DESC")
    fun findUnusedPasswordResets(): List<PasswordReset>
    
    /**
     * Find expired password reset requests
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.expiresAt < :currentTime ORDER BY pr.createdAt DESC")
    fun findExpiredPasswordResets(@Param("currentTime") currentTime: Long): List<PasswordReset>
    
    /**
     * Find valid (unused and non-expired) password reset requests
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.isUsed = false AND pr.expiresAt > :currentTime ORDER BY pr.createdAt DESC")
    fun findValidPasswordResets(@Param("currentTime") currentTime: Long): List<PasswordReset>
    
    /**
     * Find password reset requests created within a date range
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.createdAt BETWEEN :startDate AND :endDate ORDER BY pr.createdAt DESC")
    fun findByCreatedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PasswordReset>
    
    /**
     * Find password reset requests created within a date range with pagination
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.createdAt BETWEEN :startDate AND :endDate ORDER BY pr.createdAt DESC")
    fun findByCreatedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime, pageable: Pageable): Page<PasswordReset>
    
    /**
     * Find recent password reset requests (created within the last N days)
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.createdAt >= :fromDate ORDER BY pr.createdAt DESC")
    fun findRecentPasswordResets(@Param("fromDate") fromDate: LocalDateTime): List<PasswordReset>
    
    /**
     * Find recent password reset requests with pagination
     */
    @Query("SELECT pr FROM PasswordReset pr LEFT JOIN FETCH pr.user WHERE pr.createdAt >= :fromDate ORDER BY pr.createdAt DESC")
    fun findRecentPasswordResets(@Param("fromDate") fromDate: LocalDateTime, pageable: Pageable): Page<PasswordReset>
    
    /**
     * Find password reset requests for a user within a date range
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.createdAt BETWEEN :startDate AND :endDate ORDER BY pr.createdAt DESC")
    fun findByUserAndCreatedAtBetween(@Param("user") user: User, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<PasswordReset>
    
    /**
     * Find the most recent password reset request for a user
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user ORDER BY pr.createdAt DESC LIMIT 1")
    fun findMostRecentPasswordResetForUser(@Param("user") user: User): PasswordReset?
    
    /**
     * Find the most recent valid password reset request for a user
     */
    @Query("SELECT pr FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = false AND pr.expiresAt > :currentTime ORDER BY pr.createdAt DESC LIMIT 1")
    fun findMostRecentValidPasswordResetForUser(@Param("user") user: User, @Param("currentTime") currentTime: Long): PasswordReset?
    
    /**
     * Count password reset requests for a specific user
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.user = :user")
    fun countByUser(@Param("user") user: User): Long
    
    /**
     * Count used password reset requests for a specific user
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = true")
    fun countUsedPasswordResetsByUser(@Param("user") user: User): Long
    
    /**
     * Count valid password reset requests for a specific user
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = false AND pr.expiresAt > :currentTime")
    fun countValidPasswordResetsByUser(@Param("user") user: User, @Param("currentTime") currentTime: Long): Long
    
    /**
     * Count expired password reset requests
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.expiresAt < :currentTime")
    fun countExpiredPasswordResets(@Param("currentTime") currentTime: Long): Long
    
    /**
     * Count used password reset requests
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.isUsed = true")
    fun countUsedPasswordResets(): Long
    
    /**
     * Count unused password reset requests
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.isUsed = false")
    fun countUnusedPasswordResets(): Long
    
    /**
     * Count valid password reset requests
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.isUsed = false AND pr.expiresAt > :currentTime")
    fun countValidPasswordResets(@Param("currentTime") currentTime: Long): Long
    
    /**
     * Count password reset requests created within a date range
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.createdAt BETWEEN :startDate AND :endDate")
    fun countByCreatedAtBetween(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): Long
    
    /**
     * Count recent password reset requests (created within the last N days)
     */
    @Query("SELECT COUNT(pr) FROM PasswordReset pr WHERE pr.createdAt >= :fromDate")
    fun countRecentPasswordResets(@Param("fromDate") fromDate: LocalDateTime): Long
    
    /**
     * Check if token exists and is valid
     */
    @Query("SELECT COUNT(pr) > 0 FROM PasswordReset pr WHERE pr.token = :token AND pr.isUsed = false AND pr.expiresAt > :currentTime")
    fun isTokenValid(@Param("token") token: String, @Param("currentTime") currentTime: Long): Boolean
    
    /**
     * Check if user has any valid password reset requests
     */
    @Query("SELECT COUNT(pr) > 0 FROM PasswordReset pr WHERE pr.user = :user AND pr.isUsed = false AND pr.expiresAt > :currentTime")
    fun hasValidPasswordResetRequest(@Param("user") user: User, @Param("currentTime") currentTime: Long): Boolean
    
    /**
     * Mark password reset as used
     */
    @Modifying
    @Query("UPDATE PasswordReset pr SET pr.isUsed = true WHERE pr.token = :token")
    fun markAsUsed(@Param("token") token: String): Int
    
    /**
     * Mark all password reset requests for a user as used
     */
    @Modifying
    @Query("UPDATE PasswordReset pr SET pr.isUsed = true WHERE pr.user = :user")
    fun markAllAsUsedForUser(@Param("user") user: User): Int
    
    /**
     * Delete expired password reset requests
     */
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.expiresAt < :currentTime")
    fun deleteExpiredPasswordResets(@Param("currentTime") currentTime: Long): Int
    
    /**
     * Delete used password reset requests older than a specific date
     */
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.isUsed = true AND pr.createdAt < :cutoffDate")
    fun deleteOldUsedPasswordResets(@Param("cutoffDate") cutoffDate: LocalDateTime): Int
    
    /**
     * Delete all password reset requests for a user
     */
    @Modifying
    @Query("DELETE FROM PasswordReset pr WHERE pr.user = :user")
    fun deleteAllForUser(@Param("user") user: User): Int
    
    /**
     * Find users with most password reset requests
     */
    @Query("SELECT pr.user, COUNT(pr) as resetCount FROM PasswordReset pr GROUP BY pr.user ORDER BY resetCount DESC")
    fun findUsersWithMostPasswordResets(pageable: Pageable): Page<Array<Any>>
    
    /**
     * Find password reset statistics by date
     */
    @Query("SELECT DATE(pr.createdAt), COUNT(pr) FROM PasswordReset pr WHERE pr.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(pr.createdAt) ORDER BY DATE(pr.createdAt)")
    fun findPasswordResetStatsByDate(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Array<Any>>
}