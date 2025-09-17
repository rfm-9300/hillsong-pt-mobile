package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import java.time.LocalDateTime

class UserProfileRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var userProfileRepository: UserProfileRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var testUser: User
    private lateinit var adminUser: User
    private lateinit var testProfile: UserProfile
    private lateinit var adminProfile: UserProfile
    
    @BeforeEach
    fun setUp() {
        // Create regular user and profile
        testUser = User(
            email = "user@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedTestUser = entityManager.persistAndFlush(testUser)
        
        testProfile = UserProfile(
            user = savedTestUser,
            firstName = "John",
            lastName = "Doe",
            email = "user@example.com",
            phone = "1234567890",
            isAdmin = false,
            imagePath = "/images/john.jpg"
        )
        entityManager.persistAndFlush(testProfile)
        
        // Create admin user and profile
        adminUser = User(
            email = "admin@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedAdminUser = entityManager.persistAndFlush(adminUser)
        
        adminProfile = UserProfile(
            user = savedAdminUser,
            firstName = "Jane",
            lastName = "Admin",
            email = "admin@example.com",
            phone = "0987654321",
            isAdmin = true
        )
        entityManager.persistAndFlush(adminProfile)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByUser should return profile for specific user`() {
        // When
        val foundProfile = userProfileRepository.findByUser(testUser)
        
        // Then
        assertNotNull(foundProfile)
        assertEquals("John", foundProfile?.firstName)
        assertEquals("Doe", foundProfile?.lastName)
        assertEquals("user@example.com", foundProfile?.email)
    }
    
    @Test
    fun `findByUserId should return profile for specific user ID`() {
        // When
        val foundProfile = userProfileRepository.findByUserId(testUser.id!!)
        
        // Then
        assertNotNull(foundProfile)
        assertEquals("John", foundProfile?.firstName)
        assertEquals("Doe", foundProfile?.lastName)
    }
    
    @Test
    fun `findByEmail should return profile with specific email`() {
        // When
        val foundProfile = userProfileRepository.findByEmail("user@example.com")
        
        // Then
        assertNotNull(foundProfile)
        assertEquals("John", foundProfile?.firstName)
        assertEquals("user@example.com", foundProfile?.email)
    }
    
    @Test
    fun `findByPhone should return profile with specific phone`() {
        // When
        val foundProfile = userProfileRepository.findByPhone("1234567890")
        
        // Then
        assertNotNull(foundProfile)
        assertEquals("John", foundProfile?.firstName)
        assertEquals("1234567890", foundProfile?.phone)
    }
    
    @Test
    fun `findByIdWithUser should eagerly load user`() {
        // When
        val foundProfile = userProfileRepository.findByIdWithUser(testProfile.id!!)
        
        // Then
        assertNotNull(foundProfile)
        assertNotNull(foundProfile?.user)
        assertEquals("user@example.com", foundProfile?.user?.email)
        assertEquals("John", foundProfile?.firstName)
    }
    
    @Test
    fun `findByFirstNameContainingIgnoreCase should find profiles by first name`() {
        // When
        val johnProfiles = userProfileRepository.findByFirstNameContainingIgnoreCase("john")
        val janeProfiles = userProfileRepository.findByFirstNameContainingIgnoreCase("JANE")
        
        // Then
        assertTrue(johnProfiles.isNotEmpty())
        assertTrue(johnProfiles.any { it.firstName == "John" })
        
        assertTrue(janeProfiles.isNotEmpty())
        assertTrue(janeProfiles.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByLastNameContainingIgnoreCase should find profiles by last name`() {
        // When
        val doeProfiles = userProfileRepository.findByLastNameContainingIgnoreCase("doe")
        val adminProfiles = userProfileRepository.findByLastNameContainingIgnoreCase("ADMIN")
        
        // Then
        assertTrue(doeProfiles.isNotEmpty())
        assertTrue(doeProfiles.any { it.lastName == "Doe" })
        
        assertTrue(adminProfiles.isNotEmpty())
        assertTrue(adminProfiles.any { it.lastName == "Admin" })
    }
    
    @Test
    fun `findByFullNameContainingIgnoreCase should find profiles by full name`() {
        // When
        val johnDoeProfiles = userProfileRepository.findByFullNameContainingIgnoreCase("john doe")
        val janeAdminProfiles = userProfileRepository.findByFullNameContainingIgnoreCase("JANE ADMIN")
        
        // Then
        assertTrue(johnDoeProfiles.isNotEmpty())
        assertTrue(johnDoeProfiles.any { it.firstName == "John" && it.lastName == "Doe" })
        
        assertTrue(janeAdminProfiles.isNotEmpty())
        assertTrue(janeAdminProfiles.any { it.firstName == "Jane" && it.lastName == "Admin" })
    }
    
    @Test
    fun `findAdminProfiles should return only admin profiles`() {
        // When
        val adminProfiles = userProfileRepository.findAdminProfiles()
        
        // Then
        assertTrue(adminProfiles.isNotEmpty())
        assertTrue(adminProfiles.all { it.isAdmin })
        assertTrue(adminProfiles.any { it.firstName == "Jane" })
        assertFalse(adminProfiles.any { it.firstName == "John" })
    }
    
    @Test
    fun `findNonAdminProfiles should return only non-admin profiles`() {
        // When
        val nonAdminProfiles = userProfileRepository.findNonAdminProfiles()
        
        // Then
        assertTrue(nonAdminProfiles.isNotEmpty())
        assertTrue(nonAdminProfiles.all { !it.isAdmin })
        assertTrue(nonAdminProfiles.any { it.firstName == "John" })
        assertFalse(nonAdminProfiles.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByJoinedAtBetween should return profiles joined within date range`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(1)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val profilesInRange = userProfileRepository.findByJoinedAtBetween(startDate, endDate)
        
        // Then
        assertTrue(profilesInRange.isNotEmpty())
        assertTrue(profilesInRange.all { 
            it.joinedAt.isAfter(startDate) && it.joinedAt.isBefore(endDate) 
        })
        assertEquals(2, profilesInRange.size)
    }
    
    @Test
    fun `findByJoinedAtBetween with pagination should return paginated results`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(1)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val page = userProfileRepository.findByJoinedAtBetween(startDate, endDate, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
    }
    
    @Test
    fun `findRecentlyJoinedProfiles should return profiles joined after specified date`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val recentProfiles = userProfileRepository.findRecentlyJoinedProfiles(fromDate)
        
        // Then
        assertTrue(recentProfiles.isNotEmpty())
        assertTrue(recentProfiles.all { it.joinedAt.isAfter(fromDate) })
        assertEquals(2, recentProfiles.size)
    }
    
    @Test
    fun `findRecentlyJoinedProfiles with pagination should return paginated results`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val page = userProfileRepository.findRecentlyJoinedProfiles(fromDate, PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
    }
    
    @Test
    fun `findProfilesWithImages should return profiles that have image paths`() {
        // When
        val profilesWithImages = userProfileRepository.findProfilesWithImages()
        
        // Then
        assertTrue(profilesWithImages.isNotEmpty())
        assertTrue(profilesWithImages.all { !it.imagePath.isNullOrBlank() })
        assertTrue(profilesWithImages.any { it.firstName == "John" })
        assertFalse(profilesWithImages.any { it.firstName == "Jane" }) // Jane has no image
    }
    
    @Test
    fun `findProfilesWithoutImages should return profiles without image paths`() {
        // When
        val profilesWithoutImages = userProfileRepository.findProfilesWithoutImages()
        
        // Then
        assertTrue(profilesWithoutImages.isNotEmpty())
        assertTrue(profilesWithoutImages.all { it.imagePath.isNullOrBlank() })
        assertTrue(profilesWithoutImages.any { it.firstName == "Jane" })
        assertFalse(profilesWithoutImages.any { it.firstName == "John" }) // John has an image
    }
    
    @Test
    fun `findByPhoneContaining should find profiles by partial phone match`() {
        // When
        val profiles1234 = userProfileRepository.findByPhoneContaining("1234")
        val profiles0987 = userProfileRepository.findByPhoneContaining("0987")
        
        // Then
        assertTrue(profiles1234.isNotEmpty())
        assertTrue(profiles1234.any { it.firstName == "John" })
        
        assertTrue(profiles0987.isNotEmpty())
        assertTrue(profiles0987.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findAllOrderByJoinedAtDesc should return profiles ordered by join date`() {
        // When
        val orderedProfiles = userProfileRepository.findAllOrderByJoinedAtDesc()
        
        // Then
        assertEquals(2, orderedProfiles.size)
        // Should be ordered by joinedAt descending (newest first)
        assertTrue(orderedProfiles[0].joinedAt.isAfter(orderedProfiles[1].joinedAt) || 
                  orderedProfiles[0].joinedAt.isEqual(orderedProfiles[1].joinedAt))
    }
    
    @Test
    fun `findAllOrderByName should return profiles ordered by name`() {
        // When
        val orderedProfiles = userProfileRepository.findAllOrderByName()
        
        // Then
        assertEquals(2, orderedProfiles.size)
        // Should be ordered by firstName, lastName
        assertEquals("Jane", orderedProfiles[0].firstName) // Jane Admin comes before John Doe
        assertEquals("John", orderedProfiles[1].firstName)
    }
    
    @Test
    fun `countAdminProfiles should return correct count`() {
        // When
        val adminCount = userProfileRepository.countAdminProfiles()
        
        // Then
        assertEquals(1, adminCount)
    }
    
    @Test
    fun `countNonAdminProfiles should return correct count`() {
        // When
        val nonAdminCount = userProfileRepository.countNonAdminProfiles()
        
        // Then
        assertEquals(1, nonAdminCount)
    }
    
    @Test
    fun `countByJoinedAtBetween should return correct count`() {
        // Given
        val startDate = LocalDateTime.now().minusHours(1)
        val endDate = LocalDateTime.now().plusHours(1)
        
        // When
        val countInRange = userProfileRepository.countByJoinedAtBetween(startDate, endDate)
        
        // Then
        assertEquals(2, countInRange)
    }
    
    @Test
    fun `countRecentlyJoinedProfiles should return correct count`() {
        // Given
        val fromDate = LocalDateTime.now().minusHours(1)
        
        // When
        val recentCount = userProfileRepository.countRecentlyJoinedProfiles(fromDate)
        
        // Then
        assertEquals(2, recentCount)
    }
    
    @Test
    fun `countProfilesWithImages should return correct count`() {
        // When
        val withImagesCount = userProfileRepository.countProfilesWithImages()
        val withoutImagesCount = userProfileRepository.countProfilesWithoutImages()
        
        // Then
        assertEquals(1, withImagesCount) // Only John has an image
        assertEquals(1, withoutImagesCount) // Only Jane has no image
    }
    
    @Test
    fun `existsByEmailAndIdNot should check email uniqueness correctly`() {
        // When & Then
        assertTrue(userProfileRepository.existsByEmailAndIdNot("user@example.com", null))
        assertTrue(userProfileRepository.existsByEmailAndIdNot("user@example.com", adminProfile.id))
        assertFalse(userProfileRepository.existsByEmailAndIdNot("user@example.com", testProfile.id))
        assertFalse(userProfileRepository.existsByEmailAndIdNot("nonexistent@example.com", null))
    }
    
    @Test
    fun `existsByPhoneAndIdNot should check phone uniqueness correctly`() {
        // When & Then
        assertTrue(userProfileRepository.existsByPhoneAndIdNot("1234567890", null))
        assertTrue(userProfileRepository.existsByPhoneAndIdNot("1234567890", adminProfile.id))
        assertFalse(userProfileRepository.existsByPhoneAndIdNot("1234567890", testProfile.id))
        assertFalse(userProfileRepository.existsByPhoneAndIdNot("9999999999", null))
    }
    
    @Test
    fun `findAllWithUser with pagination should return paginated results`() {
        // When
        val page = userProfileRepository.findAllWithUser(PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.user != null })
    }
    
    @Test
    fun `findAdminProfilesWithPagination should return paginated admin profiles`() {
        // When
        val page = userProfileRepository.findAdminProfilesWithPagination(PageRequest.of(0, 10))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(1, page.totalElements)
        assertTrue(page.content.all { it.isAdmin })
    }
    
    @Test
    fun `findNonAdminProfilesWithPagination should return paginated non-admin profiles`() {
        // When
        val page = userProfileRepository.findNonAdminProfilesWithPagination(PageRequest.of(0, 10))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(1, page.totalElements)
        assertTrue(page.content.all { !it.isAdmin })
    }
    
    @Test
    fun `searchProfiles should find profiles by name or email`() {
        // When
        val johnResults = userProfileRepository.searchProfiles("john")
        val emailResults = userProfileRepository.searchProfiles("admin@example.com")
        val fullNameResults = userProfileRepository.searchProfiles("jane admin")
        
        // Then
        assertTrue(johnResults.isNotEmpty())
        assertTrue(johnResults.any { it.firstName == "John" })
        
        assertTrue(emailResults.isNotEmpty())
        assertTrue(emailResults.any { it.email == "admin@example.com" })
        
        assertTrue(fullNameResults.isNotEmpty())
        assertTrue(fullNameResults.any { it.firstName == "Jane" && it.lastName == "Admin" })
    }
    
    @Test
    fun `searchProfiles with pagination should return paginated search results`() {
        // When
        val page = userProfileRepository.searchProfiles("example.com", PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements) // Both profiles have example.com in email
    }
    
    @Test
    fun `save should persist profile with all fields`() {
        // Given
        val newUser = User(
            email = "new@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedNewUser = entityManager.persistAndFlush(newUser)
        
        val newProfile = UserProfile(
            user = savedNewUser,
            firstName = "New",
            lastName = "User",
            email = "new@example.com",
            phone = "5555555555",
            isAdmin = false,
            imagePath = "/images/new.jpg"
        )
        
        // When
        val savedProfile = userProfileRepository.save(newProfile)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedProfile.id)
        
        val foundProfile = userProfileRepository.findById(savedProfile.id!!)
        assertTrue(foundProfile.isPresent)
        assertEquals("New", foundProfile.get().firstName)
        assertEquals("User", foundProfile.get().lastName)
        assertEquals("new@example.com", foundProfile.get().email)
        assertEquals("5555555555", foundProfile.get().phone)
        assertEquals("/images/new.jpg", foundProfile.get().imagePath)
    }
    
    @Test
    fun `delete should remove profile but not cascade to user`() {
        // Given
        val profileToDelete = userProfileRepository.findByEmail("user@example.com")
        assertNotNull(profileToDelete)
        val profileId = profileToDelete!!.id!!
        val userId = profileToDelete.user.id!!
        
        // When
        userProfileRepository.delete(profileToDelete)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        val deletedProfile = userProfileRepository.findById(profileId)
        assertFalse(deletedProfile.isPresent)
        
        // User should still exist (profile deletion doesn't cascade to user)
        val user = entityManager.find(User::class.java, userId)
        assertNotNull(user)
    }
    
    @Test
    fun `userProfile entity should handle equals and hashCode correctly`() {
        // Given
        val profile1 = UserProfile(
            id = 1L,
            user = testUser,
            firstName = "Test1",
            lastName = "User1",
            email = "test1@example.com"
        )
        val profile2 = UserProfile(
            id = 1L,
            user = adminUser,
            firstName = "Test2",
            lastName = "User2",
            email = "test2@example.com"
        )
        val profile3 = UserProfile(
            id = 2L,
            user = testUser,
            firstName = "Test1",
            lastName = "User1",
            email = "test1@example.com"
        )
        
        // Then
        assertEquals(profile1, profile2) // Same ID
        assertNotEquals(profile1, profile3) // Different ID
        assertEquals(profile1.hashCode(), profile2.hashCode()) // Same ID should have same hash
    }
}