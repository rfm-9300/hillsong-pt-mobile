package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.Service
import java.time.LocalTime

class ServiceRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var serviceRepository: ServiceRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var sundayService: Service
    private lateinit var wednesdayService: Service
    private lateinit var inactiveService: Service
    
    @BeforeEach
    fun setUp() {
        // Create Sunday service
        sundayService = Service(
            name = "Sunday Morning Service",
            description = "Main Sunday worship service",
            dayOfWeek = 0, // Sunday
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(11, 30),
            isActive = true
        )
        entityManager.persistAndFlush(sundayService)
        
        // Create Wednesday service
        wednesdayService = Service(
            name = "Wednesday Prayer Meeting",
            description = "Midweek prayer and Bible study",
            dayOfWeek = 3, // Wednesday
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(20, 30),
            isActive = true
        )
        entityManager.persistAndFlush(wednesdayService)
        
        // Create inactive service
        inactiveService = Service(
            name = "Old Service",
            description = "This service is no longer active",
            dayOfWeek = 6, // Saturday
            startTime = LocalTime.of(18, 0),
            endTime = LocalTime.of(19, 0),
            isActive = false
        )
        entityManager.persistAndFlush(inactiveService)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByName should return service with specific name`() {
        // When
        val foundService = serviceRepository.findByName("Sunday Morning Service")
        
        // Then
        assertNotNull(foundService)
        assertEquals("Sunday Morning Service", foundService?.name)
        assertEquals("Main Sunday worship service", foundService?.description)
    }
    
    @Test
    fun `findByName should return null when service not found`() {
        // When
        val foundService = serviceRepository.findByName("Non-existent Service")
        
        // Then
        assertNull(foundService)
    }
    
    @Test
    fun `findByNameContainingIgnoreCase should find services by partial name`() {
        // When
        val sundayServices = serviceRepository.findByNameContainingIgnoreCase("sunday")
        val prayerServices = serviceRepository.findByNameContainingIgnoreCase("PRAYER")
        
        // Then
        assertTrue(sundayServices.isNotEmpty())
        assertTrue(sundayServices.any { it.name == "Sunday Morning Service" })
        
        assertTrue(prayerServices.isNotEmpty())
        assertTrue(prayerServices.any { it.name == "Wednesday Prayer Meeting" })
    }
    
    @Test
    fun `findByDayOfWeek should return services on specific day`() {
        // When
        val sundayServices = serviceRepository.findByDayOfWeek(0) // Sunday
        val wednesdayServices = serviceRepository.findByDayOfWeek(3) // Wednesday
        val saturdayServices = serviceRepository.findByDayOfWeek(6) // Saturday
        
        // Then
        assertTrue(sundayServices.isNotEmpty())
        assertTrue(sundayServices.all { it.dayOfWeek == 0 })
        assertTrue(sundayServices.any { it.name == "Sunday Morning Service" })
        
        assertTrue(wednesdayServices.isNotEmpty())
        assertTrue(wednesdayServices.all { it.dayOfWeek == 3 })
        assertTrue(wednesdayServices.any { it.name == "Wednesday Prayer Meeting" })
        
        assertTrue(saturdayServices.isNotEmpty())
        assertTrue(saturdayServices.all { it.dayOfWeek == 6 })
        assertTrue(saturdayServices.any { it.name == "Old Service" })
    }
    
    @Test
    fun `findByIsActiveTrue should return only active services`() {
        // When
        val activeServices = serviceRepository.findByIsActiveTrue()
        
        // Then
        assertEquals(2, activeServices.size)
        assertTrue(activeServices.all { it.isActive })
        assertTrue(activeServices.any { it.name == "Sunday Morning Service" })
        assertTrue(activeServices.any { it.name == "Wednesday Prayer Meeting" })
        assertFalse(activeServices.any { it.name == "Old Service" })
    }
    
    @Test
    fun `findByIsActiveFalse should return only inactive services`() {
        // When
        val inactiveServices = serviceRepository.findByIsActiveFalse()
        
        // Then
        assertEquals(1, inactiveServices.size)
        assertTrue(inactiveServices.all { !it.isActive })
        assertTrue(inactiveServices.any { it.name == "Old Service" })
        assertFalse(inactiveServices.any { it.name == "Sunday Morning Service" })
    }
    
    @Test
    fun `findByDayOfWeekAndIsActiveTrue should return active services on specific day`() {
        // When
        val activeSundayServices = serviceRepository.findByDayOfWeekAndIsActiveTrue(0)
        val activeWednesdayServices = serviceRepository.findByDayOfWeekAndIsActiveTrue(3)
        val activeSaturdayServices = serviceRepository.findByDayOfWeekAndIsActiveTrue(6)
        
        // Then
        assertEquals(1, activeSundayServices.size)
        assertTrue(activeSundayServices.all { it.dayOfWeek == 0 && it.isActive })
        
        assertEquals(1, activeWednesdayServices.size)
        assertTrue(activeWednesdayServices.all { it.dayOfWeek == 3 && it.isActive })
        
        assertEquals(0, activeSaturdayServices.size) // Saturday service is inactive
    }
    
    @Test
    fun `findByStartTimeBetween should return services starting within time range`() {
        // Given
        val morningStart = LocalTime.of(8, 0)
        val morningEnd = LocalTime.of(12, 0)
        val eveningStart = LocalTime.of(18, 0)
        val eveningEnd = LocalTime.of(21, 0)
        
        // When
        val morningServices = serviceRepository.findByStartTimeBetween(morningStart, morningEnd)
        val eveningServices = serviceRepository.findByStartTimeBetween(eveningStart, eveningEnd)
        
        // Then
        assertTrue(morningServices.isNotEmpty())
        assertTrue(morningServices.any { it.name == "Sunday Morning Service" })
        assertFalse(morningServices.any { it.name == "Wednesday Prayer Meeting" })
        
        assertTrue(eveningServices.isNotEmpty())
        assertTrue(eveningServices.any { it.name == "Wednesday Prayer Meeting" })
        assertTrue(eveningServices.any { it.name == "Old Service" })
        assertFalse(eveningServices.any { it.name == "Sunday Morning Service" })
    }
    
    @Test
    fun `findByEndTimeBetween should return services ending within time range`() {
        // Given
        val morningStart = LocalTime.of(11, 0)
        val morningEnd = LocalTime.of(12, 0)
        val eveningStart = LocalTime.of(19, 0)
        val eveningEnd = LocalTime.of(21, 0)
        
        // When
        val morningServices = serviceRepository.findByEndTimeBetween(morningStart, morningEnd)
        val eveningServices = serviceRepository.findByEndTimeBetween(eveningStart, eveningEnd)
        
        // Then
        assertTrue(morningServices.isNotEmpty())
        assertTrue(morningServices.any { it.name == "Sunday Morning Service" })
        
        assertTrue(eveningServices.isNotEmpty())
        assertTrue(eveningServices.any { it.name == "Wednesday Prayer Meeting" })
        assertTrue(eveningServices.any { it.name == "Old Service" })
    }
    
    @Test
    fun `findServicesRunningAtTime should return services running at specific time`() {
        // Given
        val sundayMorningTime = LocalTime.of(10, 30) // During Sunday service
        val wednesdayEveningTime = LocalTime.of(19, 30) // During Wednesday service
        val offTime = LocalTime.of(15, 0) // No services running
        
        // When
        val sundayRunningServices = serviceRepository.findServicesRunningAtTime(0, sundayMorningTime)
        val wednesdayRunningServices = serviceRepository.findServicesRunningAtTime(3, wednesdayEveningTime)
        val offTimeServices = serviceRepository.findServicesRunningAtTime(0, offTime)
        
        // Then
        assertTrue(sundayRunningServices.isNotEmpty())
        assertTrue(sundayRunningServices.any { it.name == "Sunday Morning Service" })
        
        assertTrue(wednesdayRunningServices.isNotEmpty())
        assertTrue(wednesdayRunningServices.any { it.name == "Wednesday Prayer Meeting" })
        
        assertTrue(offTimeServices.isEmpty())
    }
    
    @Test
    fun `findActiveServicesRunningAtTime should return only act