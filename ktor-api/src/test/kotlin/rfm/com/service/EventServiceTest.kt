package rfm.com.service

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.CreateEventRequest
import rfm.com.dto.EventUserStatus
import rfm.com.entity.AuthProvider
import rfm.com.entity.Event
import rfm.com.entity.User
import rfm.com.entity.UserProfile
import rfm.com.repository.EventRepository
import rfm.com.repository.UserProfileRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.EntityNotFoundException

class EventServiceTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var userRepository: UserRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var fileStorageService: FileStorageService
    private lateinit var eventService: EventService

    private lateinit var testUser: User
    private lateinit var testUserProfile: UserProfile
    private lateinit var testEvent: Event

    @BeforeEach
    fun setUp() {
        eventRepository = mockk()
        userRepository = mockk()
        userProfileRepository = mockk()
        fileStorageService = mockk()
        
        eventService = EventService(
            eventRepository,
            userRepository,
            userProfileRepository,
            fileStorageService
        )

        // Create test data
        testUser = User(
            id = 1L,
            email = "test@example.com",
            password = "hashedPassword",
            salt = "salt",
            verified = true,
            authProvider = AuthProvider.LOCAL
        )

        testUserProfile = UserProfile(
            id = 1L,
            user = testUser,
            firstName = "John",
            lastName = "Doe",
            email = "test@example.com"
        )

        testEvent = Event(
            id = 1L,
            title = "Test Event",
            description = "Test Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizer = testUserProfile,
            maxAttendees = 10,
            needsApproval = false
        )
    }

    @Test
    fun `createEvent should create event successfully`() = runBlocking {
        // Given
        val createRequest = CreateEventRequest(
            title = "New Event",
            description = "New Description",
            date = LocalDateTime.now().plusDays(1),
            location = "New Location",
            maxAttendees = 20,
            needsApproval = false
        )
        
        val mockFile: MultipartFile = mockk()
        
        every { userProfileRepository.findById(1L) } returns Optional.of(testUserProfile)
        every { fileStorageService.storeEventImage(mockFile) } returns "events/test-image.jpg"
        every { eventRepository.save(any<Event>()) } returns testEvent.copy(
            title = createRequest.title,
            description = createRequest.description,
            headerImagePath = "events/test-image.jpg"
        )

        // When
        val result = eventService.createEvent(createRequest, 1L, mockFile)

        // Then
        assertNotNull(result)
        assertEquals(createRequest.title, result.title)
        assertEquals(createRequest.description, result.description)
        assertEquals("events/test-image.jpg", result.headerImagePath)
        
        verify { eventRepository.save(any<Event>()) }
        verify { fileStorageService.storeEventImage(mockFile) }
    }

    @Test
    fun `createEvent should throw exception when organizer not found`() = runBlocking {
        // Given
        val createRequest = CreateEventRequest(
            title = "New Event",
            description = "New Description",
            date = LocalDateTime.now().plusDays(1),
            location = "New Location",
            maxAttendees = 20,
            needsApproval = false
        )
        
        every { userProfileRepository.findById(999L) } returns Optional.empty()

        // When & Then
        assertThrows<EntityNotFoundException> {
            runBlocking {
                eventService.createEvent(createRequest, 999L)
            }
        }
    }

    @Test
    fun `joinEvent should add user as attendee when event has capacity`() = runBlocking {
        // Given
        val eventWithCapacity = testEvent.copy(maxAttendees = 10, attendees = mutableSetOf())
        
        every { eventRepository.findByIdWithAllRelationships(1L) } returns eventWithCapacity
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { eventRepository.save(any<Event>()) } returns eventWithCapacity

        // When
        val result = eventService.joinEvent(1L, 1L)

        // Then
        assertTrue(result.success)
        assertEquals("Successfully joined the event", result.message)
        assertEquals(EventUserStatus.ATTENDEE, result.currentStatus)
        
        verify { eventRepository.save(any<Event>()) }
    }

    @Test
    fun `joinEvent should add user to waiting list when event is at capacity`() = runBlocking {
        // Given
        val fullEvent = testEvent.copy(
            maxAttendees = 1,
            attendees = mutableSetOf(testUser),
            waitingListUsers = mutableSetOf()
        )
        val anotherUser = testUser.copy(id = 2L, email = "another@example.com")
        
        every { eventRepository.findByIdWithAllRelationships(1L) } returns fullEvent
        every { userRepository.findById(2L) } returns Optional.of(anotherUser)
        every { eventRepository.save(any<Event>()) } returns fullEvent

        // When
        val result = eventService.joinEvent(1L, 2L)

        // Then
        assertTrue(result.success)
        assertEquals("Event is at capacity. Added to waiting list.", result.message)
        assertEquals(EventUserStatus.WAITING_LIST, result.currentStatus)
        
        verify { eventRepository.save(any<Event>()) }
    }

    @Test
    fun `joinEvent should add user to waiting list when event needs approval`() = runBlocking {
        // Given
        val approvalEvent = testEvent.copy(
            needsApproval = true,
            attendees = mutableSetOf(),
            waitingListUsers = mutableSetOf()
        )
        
        every { eventRepository.findByIdWithAllRelationships(1L) } returns approvalEvent
        every { userRepository.findById(1L) } returns Optional.of(testUser)
        every { eventRepository.save(any<Event>()) } returns approvalEvent

        // When
        val result = eventService.joinEvent(1L, 1L)

        // Then
        assertTrue(result.success)
        assertEquals("Added to waiting list. Awaiting organizer approval.", result.message)
        assertEquals(EventUserStatus.PENDING_APPROVAL, result.currentStatus)
        
        verify { eventRepository.save(any<Event>()) }
    }

    @Test
    fun `getUpcomingEvents should return events after current time`() = runBlocking {
        // Given
        val upcomingEvents = listOf(testEvent)
        every { eventRepository.findUpcomingEvents(any()) } returns upcomingEvents

        // When
        val result = eventService.getUpcomingEvents()

        // Then
        assertEquals(1, result.size)
        assertEquals(testEvent.title, result[0].title)
        
        verify { eventRepository.findUpcomingEvents(any()) }
    }

    @Test
    fun `getAllEvents should return paginated events`() = runBlocking {
        // Given
        val pageable = PageRequest.of(0, 10)
        val eventPage = PageImpl(listOf(testEvent), pageable, 1)
        
        every { eventRepository.findAllWithOrganizer(pageable) } returns eventPage

        // When
        val result = eventService.getAllEvents(pageable)

        // Then
        assertEquals(1, result.content.size)
        assertEquals(testEvent.title, result.content[0].title)
        
        verify { eventRepository.findAllWithOrganizer(pageable) }
    }

    @Test
    fun `getEventById should return event when found`() = runBlocking {
        // Given
        every { eventRepository.findByIdWithOrganizer(1L) } returns testEvent

        // When
        val result = eventService.getEventById(1L)

        // Then
        assertEquals(testEvent.title, result.title)
        assertEquals(testEvent.description, result.description)
        
        verify { eventRepository.findByIdWithOrganizer(1L) }
    }

    @Test
    fun `getEventById should throw exception when event not found`() = runBlocking {
        // Given
        every { eventRepository.findByIdWithOrganizer(999L) } returns null

        // When & Then
        assertThrows<EntityNotFoundException> {
            runBlocking {
                eventService.getEventById(999L)
            }
        }
    }
}