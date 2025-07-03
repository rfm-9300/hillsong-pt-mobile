package example.com.web.pages.eventsPage

import example.com.data.db.event.Event
import example.com.data.db.event.EventRepository
import example.com.web.components.layout.layout
import example.com.web.components.svgIcon
import example.com.web.components.SvgIcon
import example.com.web.loadJs
import kotlinx.html.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val Logger = LoggerFactory.getLogger("EventsPage")

fun HtmlBlockTag.eventsPage(eventRepository: EventRepository, isAdminRequest: Boolean) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
    
    // Fetch events using runBlocking
    val (upcomingEvents, pastEvents) = runBlocking {
        try {
            val allEvents = eventRepository.getAllEvents()
            val now = LocalDateTime.now()
            
            val upcoming = allEvents.filter { it.date.isAfter(now) }.sortedBy { it.date }
            val past = allEvents.filter { it.date.isBefore(now) }.sortedByDescending { it.date }
            
            Pair(upcoming, past)
        } catch (e: Exception) {
            Logger.error("Error fetching events: $e")
            Pair(emptyList(), emptyList())
        }
    }

    div(classes = "flex flex-col w-full items-center justify-center relative max-w-7xl mx-auto px-4") {
            // Header Section
            div(classes = "w-full bg-white rounded-xl shadow-sm p-6 mb-8") {
                div(classes = "flex flex-col md:flex-row items-center justify-between gap-6") {
                    // Title and Description
                    div(classes = "flex-1 text-center md:text-left") {
                        h1(classes = "text-3xl font-bold text-gray-900 mb-2") { +"Events" }
                        p(classes = "text-gray-600") { +"Discover and join exciting events in your community" }
                    }
                    
                    // Create Event Button
                    a(classes = "px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 flex items-center gap-2") {
                        href = "/events/create"
                        svgIcon(SvgIcon.CALENDAR, "w-5 h-5")
                        +"Create Event"
                    }
                }
            }

            // Events Section
            div(classes = "w-full bg-white rounded-xl shadow-sm p-6") {
                // Tabs Navigation
                div(classes = "flex flex-wrap border-b border-gray-200 mb-6") {
                    tabButton("upcoming", "Upcoming Events", upcomingEvents.size, true)
                    tabButton("past", "Past Events", pastEvents.size, false)
                }

                // Tab Content
                div(classes = "mt-4") {
                    // Upcoming Events
                    tabContent("upcoming", upcomingEvents, dateFormatter, "No upcoming events")

                    // Past Events
                    tabContent("past", pastEvents, dateFormatter, "No past events")
                }
            }
        }
    loadJs("events-page")

}

private fun FlowContent.tabButton(id: String, label: String, count: Int, isActive: Boolean) {
    button(classes = "px-4 py-2 text-sm font-medium transition-colors duration-200 " +
            if (isActive) "text-blue-600 border-b-2 border-blue-600" else "text-gray-500 hover:text-gray-700") {
        attributes["onclick"] = "switchTab('$id')"
        attributes["aria-selected"] = isActive.toString()
        this.id = "$id-tab"
        +"$label ($count)"
    }
}

private fun FlowContent.tabContent(TagId: String, events: List<Event>, dateFormatter: DateTimeFormatter, emptyMessage: String) {
    div(classes = if (TagId == "upcoming") "" else "hidden") {
        id = "$TagId-content"
        if (events.isEmpty()) {
            emptyState(emptyMessage)
        } else {
            div(classes = "grid gap-6 md:grid-cols-2 lg:grid-cols-3") {
                events.forEach { event ->
                    eventCard(event, dateFormatter)
                }
            }
        }
    }
}

private fun FlowContent.emptyState(message: String) {
    div(classes = "text-center py-12") {
        svgIcon(SvgIcon.CALENDAR, "w-12 h-12 mx-auto text-gray-400 mb-4")
        p(classes = "text-gray-500 text-lg") { +message }
    }
}

private fun FlowContent.eventCard(event: Event, dateFormatter: DateTimeFormatter) {
    val isToday = LocalDateTime.parse(event.date.toString()).toLocalDate() == LocalDateTime.now().toLocalDate()
    val occupancyPercentage = (event.attendees.size.toFloat() / event.maxAttendees) * 100
    val occupancyColor = when {
        occupancyPercentage >= 90 -> "text-red-600"
        occupancyPercentage >= 75 -> "text-orange-500"
        occupancyPercentage >= 50 -> "text-blue-600"
        else -> "text-green-600"
    }

    div(classes = "bg-white rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-gray-100 overflow-hidden") {
        // Event Image
        div(classes = "relative h-48") {
            img(
                src = "/resources/uploads/images/${event.headerImagePath}",
                alt = event.title,
                classes = "w-full h-full object-cover"
            )
            if (isToday) {
                div(classes = "absolute top-2 right-2 px-2 py-1 bg-blue-600 text-white rounded-full text-xs font-bold") {
                    +"TODAY"
                }
            }
        }

        // Event Content
        div(classes = "p-4") {
            // Event Header
            div(classes = "flex justify-between items-start mb-3") {
                h3(classes = "font-semibold text-lg text-gray-900") {
                    +event.title
                }
                a(classes = "px-3 py-1 text-sm bg-blue-50 hover:bg-blue-100 text-blue-700 rounded-lg transition-colors") {
                    href = "/events/${event.id}"
                    +"View"
                }
            }

            // Event Description
            p(classes = "text-sm text-gray-600 mb-4 line-clamp-2") {
                +event.description
            }

            // Event Details
            div(classes = "space-y-2") {
                // Date
                div(classes = "flex items-center text-sm text-gray-500") {
                    svgIcon(SvgIcon.CALENDAR, "w-4 h-4 mr-2")
                    +event.date.format(dateFormatter)
                }
                
                // Location
                div(classes = "flex items-center text-sm text-gray-500") {
                    svgIcon(SvgIcon.HOME, "w-4 h-4 mr-2")
                    +event.location
                }

                // Capacity
                div(classes = "flex items-center text-sm text-gray-500") {
                    svgIcon(SvgIcon.PROFILE, "w-4 h-4 mr-2")
                    +"${event.attendees.size}/${event.maxAttendees} attendees"
                }

                // Progress Bar
                div(classes = "w-full bg-gray-200 rounded-full h-1.5 mt-2") {
                    div(classes = "h-1.5 rounded-full bg-blue-600") {
                        style = "width: ${occupancyPercentage.coerceAtMost(100F)}%"
                    }
                }
            }
        }
    }
} 