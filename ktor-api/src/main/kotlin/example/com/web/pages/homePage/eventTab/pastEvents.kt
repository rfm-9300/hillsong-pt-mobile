package example.com.web.pages.homePage.eventTab

import example.com.data.db.event.EventRepository
import example.com.plugins.Logger
import example.com.routes.Routes
import example.com.web.components.eventFilterTag
import example.com.web.components.post.event
import example.com.web.components.projectButton
import example.com.web.loadJs
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

fun HtmlBlockTag.pastEvents(
    eventRepository: EventRepository,
    isAdminRequest: Boolean
) {
    // Fetch events using runBlocking
    val events = runBlocking {
        try {
            eventRepository.getAllEvents()
        } catch (e: Exception) {
            Logger.d("Error fetching events: $e")
            emptyList()
        }
    }
    // Container for the "Create Event" button
    div(classes = "w-full py-4 flex flex-col justify-center items-center") {
        // Add a button to create a new event
        div(classes = "w-[80%] flex justify-end mb-4") {
            projectButton("Create Event", hxGet = Routes.Ui.Event.CREATE, hxTarget = "#main-content")
        }

        // container for filter tags
        div(classes = "w-[80%] justify-end flex flex-row gap-1 mb-2") {
            // Filter tags
            eventFilterTag("Upcoming", Routes.Ui.Event.LIST_UPCOMING, active = false)
            eventFilterTag("Past", Routes.Ui.Event.LIST_PAST, active = true)
        }

        // Render each event
        events.forEach { event ->
            event(event, isAdminRequest)
        }
        loadJs("event/all-events")
    }
}