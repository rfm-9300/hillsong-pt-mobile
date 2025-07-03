package example.com.web.components.post

import example.com.data.db.event.Event
import example.com.data.db.user.UserProfile
import example.com.data.db.user.UserRepository
import example.com.routes.Routes
import example.com.web.components.SvgIcon
import example.com.web.components.svgIcon
import example.com.web.components.userProfileImage
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import org.koin.java.KoinJavaComponent.getKoin
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun HtmlBlockTag.event(event: Event, isAdminRequest: Boolean = false) {
    val userRepository = getKoin().get<UserRepository>()
    val user = runBlocking {
        userRepository.getUserProfile(event.organizerId)
    }

    val date = LocalDateTime.parse(event.date.toString()).format(DateTimeFormatter.ofPattern("dd MMM"))
    val dayOfWeek = LocalDateTime.parse(event.date.toString()).dayOfWeek.toString()
    val time = LocalDateTime.parse(event.date.toString()).format(DateTimeFormatter.ofPattern("HH:mm"))
    val url = Routes.Ui.Event.DETAILS.replace("{eventId}", event.id.toString())

    // Calculate if event is today
    val isToday = LocalDateTime.parse(event.date.toString()).toLocalDate() == LocalDateTime.now().toLocalDate()
    val todayClass = if (isToday) "border-yellow-500 bg-yellow-50/80" else "border-yellow-300 bg-yellow-50/80"

    // Determine occupancy status
    val occupancyPercentage = (event.attendees.size.toFloat() / event.maxAttendees) * 100
    val occupancyColor = when {
        occupancyPercentage >= 90 -> "text-red-600"
        occupancyPercentage >= 75 -> "text-yellow-600"
        occupancyPercentage >= 50 -> "text-yellow-800"
        else -> "text-black"
    }

    // Padding for vertical layout
    val verticalPadding = if (isAdminRequest) "" else "sm:py-6"

    div(classes = "flex flex-col sm:flex-row items-center w-full space-y-3 sm:space-y-0 sm:space-x-4 group mb-4") {
        // Date container - horizontal on mobile, vertical on desktop
        div(classes = "flex flex-row sm:flex-col items-center justify-between sm:justify-center w-full sm:w-24 sm:min-w-[6rem] px-3 py-2 sm:p-3 shadow-md backdrop-blur-sm rounded-xl border transition-all duration-300 $verticalPadding " + 
             (if (isToday) "border-yellow-500 bg-yellow-50/80" else "border-yellow-300 bg-yellow-50/80")) {
            
            // Date and day container - row on mobile, column on desktop
            div(classes = "flex flex-row sm:flex-col items-center") {
                // Calendar icon on mobile only
                span(classes = "text-yellow-600 sm:hidden mr-1") {
                    svgIcon(SvgIcon.CALENDAR, "w-3 h-3")
                }
                
                // Date
                p(classes = "text-base sm:text-lg font-semibold text-black") { +date }
                
                // Day of week - with dot on mobile, block on desktop
                div(classes = "flex items-center sm:mt-1") {
                    // Dot separator visible only on mobile
                    span(classes = "text-yellow-500 mx-1 sm:hidden") { +"•" }
                    p(classes = "text-sm text-yellow-800 capitalize") { +dayOfWeek.take(3).lowercase() }
                }
                
                // TODAY badge for mobile only
                if (isToday) {
                    span(classes = "ml-1 sm:hidden px-1.5 py-0.5 bg-yellow-500 text-black rounded-full text-[10px] font-bold") {
                        +"TODAY"
                    }
                }
            }
            
            // Time badge - right aligned on mobile, centered and below on desktop
            div(classes = "flex items-center gap-1 px-2 py-1 bg-white/80 rounded-full text-xs font-medium text-black backdrop-blur-sm ml-auto sm:ml-0 sm:mt-2") {
                span(classes = "text-yellow-600") {
                    svgIcon(SvgIcon.TIME, "w-3 h-3")
                }
                +time
            }

            // Admin controls - horizontal on mobile, centered and below on desktop
            if (isAdminRequest) {
                div(classes = "flex flex-row justify-end sm:justify-center gap-1 ml-2 pl-2 sm:pl-0 sm:ml-0 sm:mt-2 border-l sm:border-l-0 sm:border-t border-yellow-200 sm:pt-2") {
                    span(classes = "p-1 rounded-full bg-yellow-100/50 hover:bg-red-100 transition-colors cursor-pointer") {
                        attributes["data-event-id"] = event.id.toString()
                        attributes["onclick"] = "deleteEvent(${event.id})"
                        svgIcon(SvgIcon.DELETE, classes = "w-4 h-4 text-red-600")
                    }
                    span(classes = "p-1 rounded-full bg-yellow-100/50 hover:bg-yellow-200/80 transition-colors cursor-pointer") {
                        attributes["hx-get"] = Routes.Ui.Event.UPDATE.replace("{eventId}", event.id.toString())
                        attributes["hx-target"] = "#main-content"
                        svgIcon(SvgIcon.EDIT, classes = "w-4 h-4 text-black")
                    }
                }
            }
        }

        // Event card
        div(classes = "flex-1 p-3 sm:p-4 bg-white backdrop-blur-sm rounded-xl border border-yellow-200 shadow-md hover:shadow-lg hover:border-yellow-400 hover:bg-yellow-50 transition-all duration-300 cursor-pointer group-hover:translate-x-1 w-full") {
            attributes["hx-get"] = url
            attributes["hx-target"] = "#main-content"

            div(classes = "flex flex-col items-center justify-between gap-3") {
                div(classes = "w-full flex flex-col md:flex-row justify-start") {
                    // Text content
                    div(classes = "flex flex-col w-full align-start order-2 md:order-1") {
                        p(classes = "text-xl font-bold text-black mb-1 tracking-tight mt-2 md:mt-0") { +event.title }

                        // Location with icon
                        div(classes = "flex items-center text-sm text-gray-700 mb-2") {
                            svgIcon(SvgIcon.LOCATION, classes = "w-4 h-4 text-gray-600 mr-1")
                            +event.location
                        }

                        div(classes = "flex items-center") {
                            // Updated to use userProfileImage component
                            div(classes = "w-4 h-4 rounded-full overflow-hidden mr-2") {
                                user?.profileImagePath?.let { 
                                    userProfileImage(
                                        it,
                                        "Profile picture of ${event.organizerName}",
                                        "w-full h-full object-cover"
                                    )
                                }
                            }
                            p(classes = "text-sm text-yellow-700") {
                                +"Hosted by "
                                span(classes = "font-medium text-black") { +event.organizerName }
                            }
                        }
                        // Capacity indicator
                        div(classes = "mt-2 flex flex-col w-full") {
                            div(classes = "flex justify-between items-center text-xs mb-1") {
                                span { +"Capacity" }
                                span(classes = occupancyColor) { +"${event.attendees.size}/${event.maxAttendees}" }
                            }
                            // Progress bar
                            div(classes = "w-full bg-gray-200 rounded-full h-1.5") {
                                div(classes = "h-1.5 rounded-full bg-yellow-500") {
                                    style = "width: ${(event.attendees.size.toFloat() / event.maxAttendees * 100).coerceAtMost(
                                        100F
                                    )}%"
                                }
                            }
                        }
                    }
                    
                    // Image with status indicator
                    div(classes = "relative flex-shrink-0 mx-auto md:ml-4 order-1 md:order-2 w-full sm:w-auto flex justify-center") {
                        if (isToday) {
                            div(classes = "absolute -top-1 -right-1 sm:-right-1 z-10 px-2 py-0.5 bg-yellow-500 text-black rounded-full text-xs font-bold hidden sm:block") {
                                +"TODAY"
                            }
                        }
                        img(classes = "w-full max-w-[280px] h-48 sm:w-32 sm:h-32 rounded-xl border-2 border-yellow-100 object-cover shadow-inner") {
                            src = "/resources/uploads/images/${event.headerImagePath}"
                            alt = event.title
                        }
                    }
                }
            }
        }
    }
}