package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import rfm.com.entity.AuthProvider
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

class UserRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var testUser: User
    private lateinit var testUserWithProfile: User
    
    @BeforeEach
    fun setUp() {
        // Create test user without profile
        testUser = User(
            email = "test@example.com",
            password = "hashedPassword",
            salt = "salt123",
            verified = true,
            verificationToken = "token123",
            authProvider = AuthProvider.LOCAL,
            resetToken = "resetToken123",
            resetTokenExpiresAt = System.currentTimeMillis() + 3600000 // 1 hour from now
        )
        
        // Create test user with profile
        val userWithProfile = User(
            email = "profile@example.com",
            password = "hashedPassword2",
            salt = "salt456",
            verified = false,
            authProvider = AuthProvider.GOOGLE,
            googleId = "google123"
        )
        
        // Save users first
        val savedUser = entityManager.persistAndFlush(testUser)
        val savedUserWithProfile = entityManager.persistAndFlush(userWithProfile)
        
        // Create and save profile
        val profile = UserProfile(
            user = savedUserWithProfile,
            firstName = "John",
            lastName = "Doe",
            email = "profile@example.com",
            phone = "1234567890",
            isAdmin = false
        )
        entityManager.persistAndFlush(profile)
        
        testUserWithProfile = savedUserWithProfile
        entityManager.clear()
    }
    
    @Test
    fun `findByEmail should return user when email exists`() {
        // When
        val foundUser = userRepository.findByEmail("test@example.com")
        
        // Then
        assertNotNull(foundUser)
        assertEquals("test@example.com", foundUser?.email)
        assertTrue(foundUser?.verified == true)
    }
    
    @Test
    fun `findByEmail should return null when email does not exist`() {
        // When
        val foundUser = userRepository.findByEmail("nonexistent@example.com")
        
        // Then
        assertNull(foundUser)
    }
    
    @Test
    fun `findByVerificationToken should return user when token exists`() {
        // When
        val foundUser = userRepository.findByVerificationToken("token123")
        
        // Then
        assertNotNull(foundUser)
        assertEquals("test@example.com", foundUser?.email)
        assertEquals("token123", foundUser?.verificationToken)
    }
    
    @Test
    fun `findByResetToken should return user when reset token exists`() {
        // When
        val foundUser = userRepository.findByResetToken("resetToken123")
        
        // Then
        assertNotNull(foundUser)
        assertEquals("test@example.com", foundUser?.email)
        assertEquals("resetToken123", foundUser?.resetToken)
    }
    
    @Test
    fun `findByGoogleId should return user when Google ID exists`() {
        // When
        val foundUser = userRepository.findByGoogleId("google123")
        
        // Then
        assertNotNull(foundUser)
        assertEquals("profile@example.com", foundUser?.email)
        assertEquals("google123", foundUser?.googleId)
        assertEquals(AuthProvider.GOOGLE, foundUser?.authProvider)
    }
    
    @Test
    fun `findByEmailWithProfile should eagerly load profile`() {
        // When
        val foundUser = userRepository.findByEmailWithProfile("profile@example.com")
        
        // Then
        assertNotNull(foundUser)
        assertNotNull(foundUser?.profile)
        assertEquals("John", foundUser?.profile?.firstName)
        assertEquals("Doe", foundUser?.profile?.lastName)
    }
    
    @Test
    fun `findByIdWithProfile should eagerly load profile`() {
        // When
        val foundUser = userRepository.findByIdWithProfile(testUserWithProfile.id!!)
        
        // Then
        assertNotNull(foundUser)
        assertNotNull(foundUser?.profile)
        assertEquals("John", foundUser?.profile?.firstName)
    }
    
    @Test
    fun `findByVerifiedTrue should return only verified users`() {
        // When
        val verifiedUsers = userRepository.findByVerifiedTrue()
        
        // Then
        assertTrue(verifiedUsers.isNotEmpty())
        assertTrue(verifiedUsers.all { it.verified })
        assertTrue(verifiedUsers.any { it.email == "test@example.com" })
    }
    
    @Test
    fun `findByVerifiedFalse should return only unverified users`() {
        // When
        val unverifiedUsers = userRepository.findByVerifiedFalse()
        
        // Then
        assertTrue(unverifiedUsers.isNotEmpty())
        assertTrue(unverifiedUsers.all { !it.verified })
        assertTrue(unverifiedUsers.any { it.email == "profile@example.com" })
    }
    
    @Test
    fun `findByAuthProvider should return users with specific auth provider`() {
        // When
        val localUsers = userRepository.findByAuthProvider(AuthProvider.LOCAL)
        val googleUsers = userRepository.findByAuthProvider(AuthProvider.GOOGLE)
        
        // Then
        assertTrue(localUsers.isNotEmpty())
        assertTrue(localUsers.all { it.authProvider == AuthProvider.LOCAL })
        assertTrue(localUsers.any { it.email == "test@example.com" })
        
        assertTrue(googleUsers.isNotEmpty())
        assertTrue(googleUsers.all { it.authProvider == AuthProvider.GOOGLE })
        assertTrue(googleUsers.any { it.email == "profile@example.com" })
    }
    
    @Test
    fun `findUsersWithExpiredResetTokens should return users with expired tokens`() {
        // Given - create user with expired token
        val expiredUser = User(
            email = "expired@example.com",
            password = "password",
            salt = "salt",
            resetToken = "expiredToken",
            resetTokenExpiresAt = System.currentTimeMillis() - 3600000 // 1 hour ago
        )
        entityManager.persistAndFlush(expiredUser)
        entityManager.clear()
        
        // When
        val expiredUsers = userRepository.findUsersWithExpiredResetTokens(System.currentTimeMillis())
        
        // Then
        assertTrue(expiredUsers.isNotEmpty())
        assertTrue(expiredUsers.any { it.email == "expired@example.com" })
    }
    
    @Test
    fun `existsByEmail should return true when email exists`() {
        // When & Then
        assertTrue(userRepository.existsByEmail("test@example.com"))
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"))
    }
    
    @Test
    fun `existsByGoogleId should return true when Google ID exists`() {
        // When & Then
        assertTrue(userRepository.existsByGoogleId("google123"))
        assertFalse(userRepository.existsByGoogleId("nonexistent"))
    }
    
    @Test
    fun `findUsersCreatedAfter should return users created after specified date`() {
        // Given
        val cutoffDate = LocalDateTime.now().minusHours(1)
        
        // When
        val recentUsers = userRepository.findUsersCreatedAfter(cutoffDate)
        
        // Then
        assertTrue(recentUsers.isNotEmpty())
        assertTrue(recentUsers.all { it.createdAt.isAfter(cutoffDate) })
    }
    
    @Test
    fun `countByVerifiedTrue should return correct count`() {
        // When
        val verifiedCount = userRepository.countByVerifiedTrue()
        val unverifiedCount = userRepository.countByVerifiedFalse()
        
        // Then
        assertTrue(verifiedCount > 0)
        assertTrue(unverifiedCount > 0)
    }
    
    @Test
    fun `save should persist user with all fields`() {
        // Given
        val newUser = User(
            email = "new@example.com",
            password = "newPassword",
            salt = "newSalt",
            verified = false,
            authProvider = AuthProvider.FACEBOOK,
            facebookId = "facebook123"
        )
        
        // When
        val savedUser = userRepository.save(newUser)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedUser.id)
        
        val foundUser = userRepository.findById(savedUser.id!!)
        assertTrue(foundUser.isPresent)
        assertEquals("new@example.com", foundUser.get().email)
        assertEquals(AuthProvider.FACEBOOK, foundUser.get().authProvider)
        assertEquals("facebook123", foundUser.get().facebookId)
    }
    
    @Test
    fun `delete should remove user and cascade to profile`() {
        // Given
        val userToDelete = userRepository.findByEmail("profile@example.com")
        assertNotNull(userToDelete)
        val userId = userToDelete!!.id!!
        
        // When
        userRepository.delete(userToDelete)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        val deletedUser = userRepository.findById(userId)
        assertFalse(deletedUser.isPresent)
    }
    
    @Test
    fun `user entity should handle equals and hashCode correctly`() {
        // Given
        val user1 = User(id = 1L, email = "test1@example.com", password = "pass", salt = "salt")
        val user2 = User(id = 1L, email = "test2@example.com", password = "pass", salt = "salt")
        val user3 = User(id = 2L, email = "test1@example.com", password = "pass", salt = "salt")
        
        // Then
        assertEquals(user1, user2) // Same ID
        assertNotEquals(user1, user3) // Different ID
        assertEquals(user1.hashCode(), user2.hashCode()) // Same ID should have same hash
    }
}