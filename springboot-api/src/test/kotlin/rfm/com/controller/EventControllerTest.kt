package rfm.com.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import rfm.com.dto.CreateEventRequest
import rfm.com.dto.EventResponse
import rfm.com.dto.EventSummaryResponse
import rfm.com.service.EventService
import java.time.LocalDateTime

@WebMvcTest(EventController::class)
@ContextConfiguration(classes = [EventControllerTest.TestConfig::class])
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var eventService: EventService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun eventService(): EventService = mockk()
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET events should return paginated events`() {
        // Given
        val eventSummary = EventSummaryResponse(
            id = 1L,
            title = "Test Event",
            description = "Test Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizerName = "John Doe",
            organizerId = 1L,
            attendeeCount = 5,
            maxAttendees = 10,
            availableSpots = 5,
            headerImagePath = null,
            needsApproval = false,
            isAtCapacity = false,
            createdAt = LocalDateTime.now()
        )
        
        val page = PageImpl(listOf(eventSummary), PageRequest.of(0, 20), 1)
        
        coEvery { eventService.getAllEvents(any()) } returns page

        // When & Then
        mockMvc.perform(get("/api/events"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content[0].title").value("Test Event"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET events upcoming should return upcoming events`() {
        // Given
        val eventSummary = EventSummaryResponse(
            id = 1L,
            title = "Upcoming Event",
            description = "Upcoming Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizerName = "John Doe",
            organizerId = 1L,
            attendeeCount = 3,
            maxAttendees = 10,
            availableSpots = 7,
            headerImagePath = null,
            needsApproval = false,
            isAtCapacity = false,
            createdAt = LocalDateTime.now()
        )
        
        coEvery { eventService.getUpcomingEvents() } returns listOf(eventSummary)

        // When & Then
        mockMvc.perform(get("/api/events/upcoming"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Upcoming events retrieved successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].title").value("Upcoming Event"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET events by id should return event details`() {
        // Given
        val eventResponse = EventResponse(
            id = 1L,
            title = "Test Event",
            description = "Test Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Test Location",
            organizerName = "John Doe",
            organizerId = 1L,
            attendeeCount = 5,
            maxAttendees = 10,
            availableSpots = 5,
            headerImagePath = null,
            needsApproval = false,
            isAtCapacity = false,
            createdAt = LocalDateTime.now()
        )
        
        coEvery { eventService.getEventById(1L, false) } returns eventResponse

        // When & Then
        mockMvc.perform(get("/api/events/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Event retrieved successfully"))
            .andExpect(jsonPath("$.data.title").value("Test Event"))
            .andExpect(jsonPath("$.data.id").value(1))
    }

    @Test
    fun `GET events should require authentication`() {
        // When & Then
        mockMvc.perform(get("/api/events"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET events search should return filtered events`() {
        // Given
        val eventSummary = EventSummaryResponse(
            id = 1L,
            title = "Christmas Event",
            description = "Christmas Description",
            date = LocalDateTime.now().plusDays(1),
            location = "Church Hall",
            organizerName = "John Doe",
            organizerId = 1L,
            attendeeCount = 2,
            maxAttendees = 10,
            availableSpots = 8,
            headerImagePath = null,
            needsApproval = false,
            isAtCapacity = false,
            createdAt = LocalDateTime.now()
        )
        
        coEvery { eventService.searchEvents("Christmas") } returns listOf(eventSummary)

        // When & Then
        mockMvc.perform(get("/api/events/search").param("query", "Christmas"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Search completed successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].title").value("Christmas Event"))
    }

    @Test
    @WithMockUser(username = "1", roles = ["USER"])
    fun `GET events search with empty query should return bad request`() {
        // When & Then
        mockMvc.perform(get("/api/events/search").param("query", ""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Search query cannot be empty"))
    }
}