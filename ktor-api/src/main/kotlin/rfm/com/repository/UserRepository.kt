package rfm.com.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.AuthProvider
import rfm.com.entity.User
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * Find user by email address
     */
    fun findByEmail(email: String): User?
    
    /**
     * Find user by verification token
     */
    fun findByVerificationToken(token: String): User?
    
    /**
     * Find user by password reset token
     */
    fun findByResetToken(resetToken: String): User?
    
    /**
     * Find user by Google ID for OAuth authentication
     */
    fun findByGoogleId(googleId: String): User?
    
    /**
     * Find user by Facebook ID for OAuth authentication
     */
    fun findByFacebookId(facebookId: String): User?
    
    /**
     * Find user by email with profile eagerly loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.email = :email")
    fun findByEmailWithProfile(@Param("email") email: String): User?
    
    /**
     * Find user by ID with profile eagerly loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    fun findByIdWithProfile(@Param("id") id: Long): User?
    
    /**
     * Find all verified users
     */
    fun findByVerifiedTrue(): List<User>
    
    /**
     * Find all unverified users
     */
    fun findByVerifiedFalse(): List<User>
    
    /**
     * Find users by authentication provider
     */
    fun findByAuthProvider(authProvider: AuthProvider): List<User>
    
    /**
     * Find users with expired reset tokens
     */
    @Query("SELECT u FROM User u WHERE u.resetToken IS NOT NULL AND u.resetTokenExpiresAt < :currentTime")
    fun findUsersWithExpiredResetTokens(@Param("currentTime") currentTime: Long): List<User>
    
    /**
     * Check if email exists
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * Check if Google ID exists
     */
    fun existsByGoogleId(googleId: String): Boolean
    
    /**
     * Check if Facebook ID exists
     */
    fun existsByFacebookId(facebookId: String): Boolean
    
    /**
     * Find users created after a specific date
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :fromDate ORDER BY u.createdAt DESC")
    fun findUsersCreatedAfter(@Param("fromDate") fromDate: LocalDateTime): List<User>
    
    /**
     * Count verified users
     */
    fun countByVerifiedTrue(): Long
    
    /**
     * Count unverified users
     */
    fun countByVerifiedFalse(): Long
}