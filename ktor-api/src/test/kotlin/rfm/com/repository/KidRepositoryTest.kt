package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.*
import java.time.LocalDate

class KidRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var kidRepository: KidRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var primaryParent: UserProfile
    private lateinit var secondaryParent: UserProfile
    private lateinit var testKid: Kid
    private lateinit var testKidsService: KidsService
    
    @BeforeEach
    fun setUp() {
        // Create primary parent user and profile
        val primaryParentUser = User(
            email = "parent1@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedPrimaryParentUser = entityManager.persistAndFlush(primaryParentUser)
        
        primaryParent = UserProfile(
            user = savedPrimaryParentUser,
            firstName = "Primary",
            lastName = "Parent",
            email = "parent1@example.com",
            phone = "1234567890",
            isAdmin = false
        )
        entityManager.persistAndFlush(primaryParent)
        
        // Create secondary parent user and profile
        val secondaryParentUser = User(
            email = "parent2@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedSecondaryParentUser = entityManager.persistAndFlush(secondaryParentUser)
        
        secondaryParent = UserProfile(
            user = savedSecondaryParentUser,
            firstName = "Secondary",
            lastName = "Parent",
            email = "parent2@example.com",
            phone = "0987654321",
            isAdmin = false
        )
        entityManager.persistAndFlush(secondaryParent)
        
        // Create kids service
        testKidsService = KidsService(
            name = "Elementary Kids",
            description = "Elementary age kids service",
            ageGroup = AgeGroup.ELEMENTARY,
            dayOfWeek = 0,
            startTime = java.time.LocalTime.of(10, 0),
            endTime = java.time.LocalTime.of(11, 0),
            maxCapacity = 20,
            isActive = true
        )
        entityManager.persistAndFlush(testKidsService)
        
        // Create test kid
        testKid = Kid(
            firstName = "Johnny",
            lastName = "Test",
            dateOfBirth = LocalDate.now().minusYears(8),
            gender = Gender.MALE,
            primaryParent = primaryParent,
            secondaryParent = secondaryParent,
            isActive = true,
            medicalNotes = "No known allergies",
            allergies = "None",
            emergencyContactName = "Emergency Contact",
            emergencyContactPhone = "5555555555"
        )
        val savedKid = entityManager.persistAndFlush(testKid)
        
        // Add kid to kids service
        savedKid.kidsServices.add(testKidsService)
        entityManager.persistAndFlush(savedKid)
        
        // Create another kid for testing
        val anotherKid = Kid(
            firstName = "Jane",
            lastName = "Test",
            dateOfBirth = LocalDate.now().minusYears(6),
            gender = Gender.FEMALE,
            primaryParent = primaryParent,
            isActive = false, // Inactive kid
            specialNeeds = "Requires assistance"
        )
        entityManager.persistAndFlush(anotherKid)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByIdWithPrimaryParent should eagerly load primary parent`() {
        // When
        val foundKid = kidRepository.findByIdWithPrimaryParent(testKid.id!!)
        
        // Then
        assertNotNull(foundKid)
        assertNotNull(foundKid?.primaryParent)
        assertEquals("Primary", foundKid?.primaryParent?.firstName)
        assertEquals("Parent", foundKid?.primaryParent?.lastName)
    }
    
    @Test
    fun `findByIdWithParents should eagerly load both parents`() {
        // When
        val foundKid = kidRepository.findByIdWithParents(testKid.id!!)
        
        // Then
        assertNotNull(foundKid)
        assertNotNull(foundKid?.primaryParent)
        assertNotNull(foundKid?.secondaryParent)
        assertEquals("Primary", foundKid?.primaryParent?.firstName)
        assertEquals("Secondary", foundKid?.secondaryParent?.firstName)
    }
    
    @Test
    fun `findByIdWithKidsServices should eagerly load kids services`() {
        // When
        val foundKid = kidRepository.findByIdWithKidsServices(testKid.id!!)
        
        // Then
        assertNotNull(foundKid)
        assertEquals(1, foundKid?.kidsServices?.size)
        assertTrue(foundKid?.kidsServices?.any { it.name == "Elementary Kids" } == true)
    }
    
    @Test
    fun `findActiveKids should return only active kids`() {
        // When
        val activeKids = kidRepository.findActiveKids()
        
        // Then
        assertTrue(activeKids.isNotEmpty())
        assertTrue(activeKids.all { it.isActive })
        assertTrue(activeKids.any { it.firstName == "Johnny" })
        assertFalse(activeKids.any { it.firstName == "Jane" }) // Jane is inactive
    }
    
    @Test
    fun `findInactiveKids should return only inactive kids`() {
        // When
        val inactiveKids = kidRepository.findInactiveKids()
        
        // Then
        assertTrue(inactiveKids.isNotEmpty())
        assertTrue(inactiveKids.all { !it.isActive })
        assertTrue(inactiveKids.any { it.firstName == "Jane" })
        assertFalse(inactiveKids.any { it.firstName == "Johnny" }) // Johnny is active
    }
    
    @Test
    fun `findByPrimaryParent should return kids with specific primary parent`() {
        // When
        val primaryParentKids = kidRepository.findByPrimaryParent(primaryParent)
        
        // Then
        assertEquals(2, primaryParentKids.size) // Johnny and Jane
        assertTrue(primaryParentKids.all { it.primaryParent.id == primaryParent.id })
        assertTrue(primaryParentKids.any { it.firstName == "Johnny" })
        assertTrue(primaryParentKids.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findBySecondaryParent should return kids with specific secondary parent`() {
        // When
        val secondaryParentKids = kidRepository.findBySecondaryParent(secondaryParent)
        
        // Then
        assertEquals(1, secondaryParentKids.size) // Only Johnny has secondary parent
        assertTrue(secondaryParentKids.all { it.secondaryParent?.id == secondaryParent.id })
        assertTrue(secondaryParentKids.any { it.firstName == "Johnny" })
    }
    
    @Test
    fun `findByEitherParent should return kids with parent as primary or secondary`() {
        // When
        val primaryParentKids = kidRepository.findByEitherParent(primaryParent)
        val secondaryParentKids = kidRepository.findByEitherParent(secondaryParent)
        
        // Then
        assertEquals(2, primaryParentKids.size) // Johnny and Jane (both have primary parent)
        assertEquals(1, secondaryParentKids.size) // Only Johnny has secondary parent
    }
    
    @Test
    fun `findByFirstNameContainingIgnoreCase should find kids by first name`() {
        // When
        val johnnyKids = kidRepository.findByFirstNameContainingIgnoreCase("johnny")
        val janeKids = kidRepository.findByFirstNameContainingIgnoreCase("JANE")
        
        // Then
        assertTrue(johnnyKids.isNotEmpty())
        assertTrue(johnnyKids.any { it.firstName == "Johnny" })
        
        assertTrue(janeKids.isNotEmpty())
        assertTrue(janeKids.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByLastNameContainingIgnoreCase should find kids by last name`() {
        // When
        val testKids = kidRepository.findByLastNameContainingIgnoreCase("test")
        
        // Then
        assertEquals(2, testKids.size)
        assertTrue(testKids.all { it.lastName == "Test" })
    }
    
    @Test
    fun `findByFullNameContainingIgnoreCase should find kids by full name`() {
        // When
        val johnnyTestKids = kidRepository.findByFullNameContainingIgnoreCase("johnny test")
        val janeTestKids = kidRepository.findByFullNameContainingIgnoreCase("JANE TEST")
        
        // Then
        assertTrue(johnnyTestKids.isNotEmpty())
        assertTrue(johnnyTestKids.any { it.firstName == "Johnny" && it.lastName == "Test" })
        
        assertTrue(janeTestKids.isNotEmpty())
        assertTrue(janeTestKids.any { it.firstName == "Jane" && it.lastName == "Test" })
    }
    
    @Test
    fun `findByGender should return kids of specific gender`() {
        // When
        val maleKids = kidRepository.findByGender(Gender.MALE)
        val femaleKids = kidRepository.findByGender(Gender.FEMALE)
        
        // Then
        assertTrue(maleKids.isNotEmpty())
        assertTrue(maleKids.all { it.gender == Gender.MALE })
        assertTrue(maleKids.any { it.firstName == "Johnny" })
        
        assertTrue(femaleKids.isNotEmpty())
        assertTrue(femaleKids.all { it.gender == Gender.FEMALE })
        assertTrue(femaleKids.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByAgeRange should return kids within age range`() {
        // Given - kids aged 6-8, so we test for age range 5-10
        val minAgeDate = LocalDate.now().minusYears(10) // 10 years ago (max age)
        val maxAgeDate = LocalDate.now().minusYears(5)  // 5 years ago (min age)
        
        // When
        val kidsInRange = kidRepository.findByAgeRange(minAgeDate, maxAgeDate)
        
        // Then
        assertEquals(2, kidsInRange.size)
        assertTrue(kidsInRange.all { 
            it.dateOfBirth.isAfter(minAgeDate) && it.dateOfBirth.isBefore(maxAgeDate) 
        })
    }
    
    @Test
    fun `findByDateOfBirthBetween should return kids born within date range`() {
        // Given
        val startDate = LocalDate.now().minusYears(10)
        val endDate = LocalDate.now().minusYears(5)
        
        // When
        val kidsBornInRange = kidRepository.findByDateOfBirthBetween(startDate, endDate)
        
        // Then
        assertEquals(2, kidsBornInRange.size)
        assertTrue(kidsBornInRange.all { 
            it.dateOfBirth.isAfter(startDate) && it.dateOfBirth.isBefore(endDate) 
        })
    }
    
    @Test
    fun `findByKidsService should return kids enrolled in specific service`() {
        // When
        val kidsInService = kidRepository.findByKidsService(testKidsService)
        
        // Then
        assertTrue(kidsInService.isNotEmpty())
        assertTrue(kidsInService.any { it.firstName == "Johnny" })
        assertTrue(kidsInService.all { kid -> 
            kid.kidsServices.any { it.id == testKidsService.id }
        })
    }
    
    @Test
    fun `findKidsWithMedicalNotes should return kids with medical notes`() {
        // When
        val kidsWithMedicalNotes = kidRepository.findKidsWithMedicalNotes()
        
        // Then
        assertTrue(kidsWithMedicalNotes.isNotEmpty())
        assertTrue(kidsWithMedicalNotes.all { !it.medicalNotes.isNullOrBlank() })
        assertTrue(kidsWithMedicalNotes.any { it.firstName == "Johnny" })
    }
    
    @Test
    fun `findKidsWithAllergies should return kids with allergies`() {
        // When
        val kidsWithAllergies = kidRepository.findKidsWithAllergies()
        
        // Then
        assertTrue(kidsWithAllergies.isNotEmpty())
        assertTrue(kidsWithAllergies.all { !it.allergies.isNullOrBlank() })
        assertTrue(kidsWithAllergies.any { it.firstName == "Johnny" })
    }
    
    @Test
    fun `findKidsWithSpecialNeeds should return kids with special needs`() {
        // When
        val kidsWithSpecialNeeds = kidRepository.findKidsWithSpecialNeeds()
        
        // Then
        assertTrue(kidsWithSpecialNeeds.isNotEmpty())
        assertTrue(kidsWithSpecialNeeds.all { !it.specialNeeds.isNullOrBlank() })
        assertTrue(kidsWithSpecialNeeds.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findKidsWithEmergencyContact should return kids with emergency contact info`() {
        // When
        val kidsWithEmergencyContact = kidRepository.findKidsWithEmergencyContact()
        
        // Then
        assertTrue(kidsWithEmergencyContact.isNotEmpty())
        assertTrue(kidsWithEmergencyContact.all { 
            !it.emergencyContactName.isNullOrBlank() && !it.emergencyContactPhone.isNullOrBlank() 
        })
        assertTrue(kidsWithEmergencyContact.any { it.firstName == "Johnny" })
    }
    
    @Test
    fun `findKidsWithoutEmergencyContact should return kids without emergency contact info`() {
        // When
        val kidsWithoutEmergencyContact = kidRepository.findKidsWithoutEmergencyContact()
        
        // Then
        assertTrue(kidsWithoutEmergencyContact.isNotEmpty())
        assertTrue(kidsWithoutEmergencyContact.all { 
            it.emergencyContactName.isNullOrBlank() || it.emergencyContactPhone.isNullOrBlank() 
        })
        assertTrue(kidsWithoutEmergencyContact.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByAge should return kids of specific age`() {
        // Given - Johnny is 8 years old, Jane is 6 years old
        
        // When
        val eightYearOlds = kidRepository.findByAge(8)
        val sixYearOlds = kidRepository.findByAge(6)
        
        // Then
        assertTrue(eightYearOlds.any { it.firstName == "Johnny" })
        assertTrue(sixYearOlds.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `findByBirthYear should return kids born in specific year`() {
        // Given
        val johnnyBirthYear = LocalDate.now().minusYears(8).year
        val janeBirthYear = LocalDate.now().minusYears(6).year
        
        // When
        val johnnyYearKids = kidRepository.findByBirthYear(johnnyBirthYear)
        val janeYearKids = kidRepository.findByBirthYear(janeBirthYear)
        
        // Then
        assertTrue(johnnyYearKids.any { it.firstName == "Johnny" })
        assertTrue(janeYearKids.any { it.firstName == "Jane" })
    }
    
    @Test
    fun `countByPrimaryParent should return correct count`() {
        // When
        val primaryParentCount = kidRepository.countByPrimaryParent(primaryParent)
        
        // Then
        assertEquals(2, primaryParentCount) // Johnny and Jane
    }
    
    @Test
    fun `countByEitherParent should return correct count`() {
        // When
        val primaryParentCount = kidRepository.countByEitherParent(primaryParent)
        val secondaryParentCount = kidRepository.countByEitherParent(secondaryParent)
        
        // Then
        assertEquals(2, primaryParentCount) // Johnny and Jane
        assertEquals(1, secondaryParentCount) // Only Johnny
    }
    
    @Test
    fun `countActiveKids should return correct count`() {
        // When
        val activeCount = kidRepository.countActiveKids()
        
        // Then
        assertEquals(1, activeCount) // Only Johnny is active
    }
    
    @Test
    fun `countByGender should return correct count`() {
        // When
        val maleCount = kidRepository.countByGender(Gender.MALE)
        val femaleCount = kidRepository.countByGender(Gender.FEMALE)
        
        // Then
        assertEquals(1, maleCount) // Johnny
        assertEquals(1, femaleCount) // Jane
    }
    
    @Test
    fun `countByKidsService should return correct count`() {
        // When
        val serviceCount = kidRepository.countByKidsService(testKidsService)
        
        // Then
        assertEquals(1, serviceCount) // Only Johnny is enrolled
    }
    
    @Test
    fun `findAllWithPrimaryParent with pagination should return paginated results`() {
        // When
        val page = kidRepository.findAllWithPrimaryParent(PageRequest.of(0, 1))
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.primaryParent != null })
    }
    
    @Test
    fun `findActiveKidsWithPagination should return paginated active kids`() {
        // When
        val page = kidRepository.findActiveKidsWithPagination(PageRequest.of(0, 10))
        
        // Then
        assertEquals(1, page.content.size) // Only Johnny is active
        assertEquals(1, page.totalElements)
        assertTrue(page.content.all { it.isActive })
    }
    
    @Test
    fun `existsByFirstNameAndLastNameAndDateOfBirthAndIdNot should check for duplicates`() {
        // When & Then
        assertTrue(kidRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
            "Johnny", "Test", testKid.dateOfBirth, null
        ))
        assertFalse(kidRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
            "Johnny", "Test", testKid.dateOfBirth, testKid.id
        ))
        assertFalse(kidRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
            "NonExistent", "Kid", LocalDate.now(), null
        ))
    }
    
    @Test
    fun `save should persist kid with all fields`() {
        // Given
        val newKid = Kid(
            firstName = "New",
            lastName = "Kid",
            dateOfBirth = LocalDate.now().minusYears(5),
            gender = Gender.MALE,
            primaryParent = primaryParent,
            isActive = true,
            medicalNotes = "Test medical notes",
            allergies = "Test allergies",
            specialNeeds = "Test special needs",
            emergencyContactName = "Test Emergency",
            emergencyContactPhone = "1111111111"
        )
        
        // When
        val savedKid = kidRepository.save(newKid)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedKid.id)
        
        val foundKid = kidRepository.findById(savedKid.id!!)
        assertTrue(foundKid.isPresent)
        assertEquals("New", foundKid.get().firstName)
        assertEquals("Kid", foundKid.get().lastName)
        assertEquals(Gender.MALE, foundKid.get().gender)
        assertEquals("Test medical notes", foundKid.get().medicalNotes)
        assertEquals("Test allergies", foundKid.get().allergies)
        assertEquals("Test special needs", foundKid.get().specialNeeds)
        assertEquals("Test Emergency", foundKid.get().emergencyContactName)
        assertEquals("1111111111", foundKid.get().emergencyContactPhone)
    }
    
    @Test
    fun `delete should remove kid and handle relationships`() {
        // Given
        val kidToDelete = kidRepository.findById(testKid.id!!)
        assertTrue(kidToDelete.isPresent)
        val kidId = kidToDelete.get().id!!
        
        // When
        kidRepository.delete(kidToDelete.get())
        entityManager.flush()
        entityManager.clear()
        
        // Then
        val deletedKid = kidRepository.findById(kidId)
        assertFalse(deletedKid.isPresent)
    }
    
    @Test
    fun `kid entity should handle equals and hashCode correctly`() {
        // Given
        val kid1 = Kid(
            id = 1L,
            firstName = "Test1",
            lastName = "Kid1",
            dateOfBirth = LocalDate.now().minusYears(5),
            gender = Gender.MALE,
            primaryParent = primaryParent
        )
        val kid2 = Kid(
            id = 1L,
            firstName = "Test2",
            lastName = "Kid2",
            dateOfBirth = LocalDate.now().minusYears(6),
            gender = Gender.FEMALE,
            primaryParent = secondaryParent
        )
        val kid3 = Kid(
            id = 2L,
            firstName = "Test1",
            lastName = "Kid1",
            dateOfBirth = LocalDate.now().minusYears(5),
            gender = Gender.MALE,
            primaryParent = primaryParent
        )
        
        // Then
        assertEquals(kid1, kid2) // Same ID
        assertNotEquals(kid1, kid3) // Different ID
        assertEquals(kid1.hashCode(), kid2.hashCode()) // Same ID should have same hash
    }
}