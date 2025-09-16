package rfm.com.repository

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import rfm.com.entity.*
import java.time.LocalDateTime

class EventRepositoryTest : BaseRepositoryTest() {
    
    @Autowired
    private lateinit var eventRepository: EventRepository
    
    @Autowired
    private lateinit var entityManager: TestEntityManager
    
    private lateinit var organizer: UserProfile
    private lateinit var attendeeUser: User
    private lateinit var waitingUser: User
    private lateinit var upcomingEvent: Event
    private lateinit var pastEvent: Event
    private lateinit var fullEvent: Event
    
    @BeforeEach
    fun setUp() {
        // Create organizer user and profile
        val organizerUser = User(
            email = "organizer@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        val savedOrganizerUser = entityManager.persistAndFlush(organizerUser)
        
        organizer = UserProfile(
            user = savedOrganizerUser,
            firstName = "Event",
            lastName = "Organizer",
            email = "organizer@example.com",
            phone = "1234567890",
            isAdmin = true
        )
        entityManager.persistAndFlush(organizer)
        
        // Create attendee users
        attendeeUser = User(
            email = "attendee@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        entityManager.persistAndFlush(attendeeUser)
        
        waitingUser = User(
            email = "waiting@example.com",
            password = "password",
            salt = "salt",
            verified = true
        )
        entityManager.persistAndFlush(waitingUser)
        
        // Create upcoming event
        upcomingEvent = Event(
            title = "Upcoming Event",
            description = "This is an upcoming event",
            date = LocalDateTime.now().plusDays(7),
            location = "Church Hall",
            organizer = organizer,
            maxAttendees = 50,
            needsApproval = false
        )
        entityManager.persistAndFlush(upcomingEvent)
        
        // Create past event
        pastEvent = Event(
            title = "Past Event",
            description = "This is a past event",
            date = LocalDateTime.now().minusDays(7),
            location = "Community Center",
            organizer = organizer,
            maxAttendees = 30,
            needsApproval = true
        )
        entityManager.persistAndFlush(pastEvent)
        
        // Create full event with attendees
        fullEvent = Event(
            title = "Full Event",
            description = "This event is at capacity",
            date = LocalDateTime.now().plusDays(14),
            location = "Main Sanctuary",
            organizer = organizer,
            maxAttendees = 2,
            needsApproval = false
        )
        val savedFullEvent = entityManager.persistAndFlush(fullEvent)
        
        // Add attendees to full event
        savedFullEvent.attendees.add(attendeeUser)
        savedFullEvent.attendees.add(waitingUser)
        entityManager.persistAndFlush(savedFullEvent)
        
        entityManager.clear()
    }
    
    @Test
    fun `findByIdWithOrganizer should eagerly load organizer`() {
        // When
        val foundEvent = eventRepository.findByIdWithOrganizer(upcomingEvent.id!!)
        
        // Then
        assertNotNull(foundEvent)
        assertNotNull(foundEvent?.organizer)
        assertEquals("Event", foundEvent?.organizer?.firstName)
        assertEquals("Organizer", foundEvent?.organizer?.lastName)
    }
    
    @Test
    fun `findByIdWithAttendees should eagerly load attendees`() {
        // When
        val foundEvent = eventRepository.findByIdWithAttendees(fullEvent.id!!)
        
        // Then
        assertNotNull(foundEvent)
        assertEquals(2, foundEvent?.attendees?.size)
        assertTrue(foundEvent?.attendees?.any { it.email == "attendee@example.com" } == true)
        assertTrue(foundEvent?.attendees?.any { it.email == "waiting@example.com" } == true)
    }
    
    @Test
    fun `findByIdWithAllRelationships should eagerly load all relationships`() {
        // When
        val foundEvent = eventRepository.findByIdWithAllRelationships(fullEvent.id!!)
        
        // Then
        assertNotNull(foundEvent)
        assertNotNull(foundEvent?.organizer)
        assertEquals(2, foundEvent?.attendees?.size)
        assertEquals("Event", foundEvent?.organizer?.firstName)
    }
    
    @Test
    fun `findUpcomingEvents should return events with date after specified time`() {
        // When
        val upcomingEvents = eventRepository.findUpcomingEvents(LocalDateTime.now())
        
        // Then
        assertTrue(upcomingEvents.isNotEmpty())
        assertTrue(upcomingEvents.all { it.date.isAfter(LocalDateTime.now()) })
        assertTrue(upcomingEvents.any { it.title == "Upcoming Event" })
        assertTrue(upcomingEvents.any { it.title == "Full Event" })
        assertFalse(upcomingEvents.any { it.title == "Past Event" })
    }
    
    @Test
    fun `findPastEvents should return events with date before specified time`() {
        // When
        val pastEvents = eventRepository.findPastEvents(LocalDateTime.now())
        
        // Then
        assertTrue(pastEvents.isNotEmpty())
        assertTrue(pastEvents.all { it.date.isBefore(LocalDateTime.now()) })
        assertTrue(pastEvents.any { it.title == "Past Event" })
        assertFalse(pastEvents.any { it.title == "Upcoming Event" })
    }
    
    @Test
    fun `findByOrganizer should return events organized by specific user`() {
        // When
        val organizedEvents = eventRepository.findByOrganizer(organizer)
        
        // Then
        assertEquals(3, organizedEvents.size)
        assertTrue(organizedEvents.all { it.organizer.id == organizer.id })
        assertTrue(organizedEvents.any { it.title == "Upcoming Event" })
        assertTrue(organizedEvents.any { it.title == "Past Event" })
        assertTrue(organizedEvents.any { it.title == "Full Event" })
    }
    
    @Test
    fun `findByOrganizer with pagination should return paginated results`() {
        // When
        val page = eventRepository.findByOrganizer(organizer, PageRequest.of(0, 2))
        
        // Then
        assertEquals(2, page.content.size)
        assertEquals(3, page.totalElements)
        assertEquals(2, page.totalPages)
        assertTrue(page.content.all { it.organizer.id == organizer.id })
    }
    
    @Test
    fun `findEventsByAttendee should return events user is attending`() {
        // When
        val attendedEvents = eventRepository.findEventsByAttendee(attendeeUser)
        
        // Then
        assertTrue(attendedEvents.isNotEmpty())
        assertTrue(attendedEvents.any { it.title == "Full Event" })
        assertTrue(attendedEvents.all { event -> 
            event.attendees.any { it.id == attendeeUser.id }
        })
    }
    
    @Test
    fun `findEventsByDateRange should return events within specified date range`() {
        // Given
        val startDate = LocalDateTime.now().minusDays(1)
        val endDate = LocalDateTime.now().plusDays(10)
        
        // When
        val eventsInRange = eventRepository.findEventsByDateRange(startDate, endDate)
        
        // Then
        assertTrue(eventsInRange.isNotEmpty())
        assertTrue(eventsInRange.all { it.date.isAfter(startDate) && it.date.isBefore(endDate) })
        assertTrue(eventsInRange.any { it.title == "Upcoming Event" })
        assertFalse(eventsInRange.any { it.title == "Past Event" })
    }
    
    @Test
    fun `findEventsByLocationContainingIgnoreCase should find events by location`() {
        // When
        val churchEvents = eventRepository.findEventsByLocationContainingIgnoreCase("church")
        val hallEvents = eventRepository.findEventsByLocationContainingIgnoreCase("HALL")
        
        // Then
        assertTrue(churchEvents.isNotEmpty())
        assertTrue(churchEvents.any { it.title == "Upcoming Event" })
        
        assertTrue(hallEvents.isNotEmpty())
        assertTrue(hallEvents.any { it.title == "Upcoming Event" })
    }
    
    @Test
    fun `findEventsByTitleContainingIgnoreCase should find events by title`() {
        // When
        val upcomingEvents = eventRepository.findEventsByTitleContainingIgnoreCase("upcoming")
        val fullEvents = eventRepository.findEventsByTitleContainingIgnoreCase("FULL")
        
        // Then
        assertTrue(upcomingEvents.isNotEmpty())
        assertTrue(upcomingEvents.any { it.title == "Upcoming Event" })
        
        assertTrue(fullEvents.isNotEmpty())
        assertTrue(fullEvents.any { it.title == "Full Event" })
    }
    
    @Test
    fun `findEventsThatNeedApproval should return events requiring approval`() {
        // When
        val approvalEvents = eventRepository.findEventsThatNeedApproval()
        
        // Then
        assertTrue(approvalEvents.isNotEmpty())
        assertTrue(approvalEvents.all { it.needsApproval })
        assertTrue(approvalEvents.any { it.title == "Past Event" })
        assertFalse(approvalEvents.any { it.title == "Upcoming Event" })
    }
    
    @Test
    fun `findEventsAtCapacity should return events at maximum capacity`() {
        // When
        val fullEvents = eventRepository.findEventsAtCapacity()
        
        // Then
        assertTrue(fullEvents.isNotEmpty())
        assertTrue(fullEvents.any { it.title == "Full Event" })
        assertTrue(fullEvents.all { it.attendees.size >= it.maxAttendees })
    }
    
    @Test
    fun `findEventsWithAvailableSpots should return events with capacity`() {
        // When
        val availableEvents = eventRepository.findEventsWithAvailableSpots()
        
        // Then
        assertTrue(availableEvents.isNotEmpty())
        assertTrue(availableEvents.any { it.title == "Upcoming Event" })
        assertTrue(availableEvents.all { it.attendees.size < it.maxAttendees })
        assertFalse(availableEvents.any { it.title == "Full Event" })
    }
    
    @Test
    fun `countEventsByOrganizer should return correct count`() {
        // When
        val eventCount = eventRepository.countEventsByOrganizer(organizer)
        
        // Then
        assertEquals(3, eventCount)
    }
    
    @Test
    fun `countUpcomingEvents should return correct count`() {
        // When
        val upcomingCount = eventRepository.countUpcomingEvents(LocalDateTime.now())
        
        // Then
        assertEquals(2, upcomingCount) // Upcoming Event and Full Event
    }
    
    @Test
    fun `countPastEvents should return correct count`() {
        // When
        val pastCount = eventRepository.countPastEvents(LocalDateTime.now())
        
        // Then
        assertEquals(1, pastCount) // Past Event
    }
    
    @Test
    fun `findAllWithOrganizer with pagination should return paginated results`() {
        // When
        val page = eventRepository.findAllWithOrganizer(PageRequest.of(0, 2))
        
        // Then
        assertEquals(2, page.content.size)
        assertEquals(3, page.totalElements)
        assertTrue(page.content.all { it.organizer != null })
    }
    
    @Test
    fun `findUpcomingEventsWithPagination should return paginated upcoming events`() {
        // When
        val page = eventRepository.findUpcomingEventsWithPagination(
            LocalDateTime.now(), 
            PageRequest.of(0, 1)
        )
        
        // Then
        assertEquals(1, page.content.size)
        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.date.isAfter(LocalDateTime.now()) })
    }
    
    @Test
    fun `save should persist event with all relationships`() {
        // Given
        val newEvent = Event(
            title = "New Test Event",
            description = "A new test event",
            date = LocalDateTime.now().plusDays(30),
            location = "Test Location",
            organizer = organizer,
            maxAttendees = 100,
            needsApproval = true
        )
        
        // When
        val savedEvent = eventRepository.save(newEvent)
        entityManager.flush()
        entityManager.clear()
        
        // Then
        assertNotNull(savedEvent.id)
        
        val foundEvent = eventRepository.findById(savedEvent.id!!)
        assertTrue(foundEvent.isPresent)
        assertEquals("New Test Event", foundEvent.get().title)
        assertEquals(100, foundEvent.get().maxAttendees)
        assertTrue(foundEvent.get().needsApproval)
    }
    
    @Test
    fun `event entity should handle business logic methods correctly`() {
        // Given
        val event = Event(
            title = "Test Event",
            description = "Test",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizer = organizer,
            maxAttendees = 2
        )
        
        // Test adding attendees
        assertTrue(event.addAttendee(attendeeUser))
        assertEquals(1, event.attendeeCount)
        assertFalse(event.isAtCapacity)
        assertEquals(1, event.availableSpots)
        
        // Fill to capacity
        assertTrue(event.addAttendee(waitingUser))
        assertEquals(2, event.attendeeCount)
        assertTrue(event.isAtCapacity)
        assertEquals(0, event.availableSpots)
        
        // Test waiting list
        val newUser = User(email = "new@example.com", password = "pass", salt = "salt")
        assertFalse(event.addAttendee(newUser)) // Should fail - at capacity
        assertTrue(event.addToWaitingList(newUser))
        
        // Test removal
        assertTrue(event.removeAttendee(attendeeUser))
        assertEquals(1, event.attendeeCount)
        assertFalse(event.isAtCapacity)
    }
    
    @Test
    fun `event entity should handle equals and hashCode correctly`() {
        // Given
        val event1 = Event(
            id = 1L,
            title = "Event 1",
            description = "Description",
            date = LocalDateTime.now(),
            location = "Location",
            organizer = organizer,
            maxAttendees = 10
        )
        val event2 = Event(
            id = 1L,
            title = "Event 2",
            description = "Different Description",
            date = LocalDateTime.now(),
            location = "Different Location",
            organizer = organizer,
            maxAttendees = 20
        )
        val event3 = Event(
            id = 2L,
            title = "Event 1",
            description = "Description",
            date = LocalDateTime.now(),
            location = "Location",
            organizer = organizer,
            maxAttendees = 10
        )
        
        // Then
        assertEquals(event1, event2) // Same ID
        assertNotEquals(event1, event3) // Different ID
        assertEquals(event1.hashCode(), event2.hashCode()) // Same ID should have same hash
    }
}