package example.com.web.pages.homePage.eventTab

import example.com.data.db.event.Event
import example.com.data.db.user.UserProfile
import example.com.routes.Routes
import example.com.web.components.SvgIcon
import example.com.web.components.projectButton
import example.com.web.components.svgIcon
import example.com.web.components.userProfileImage
import example.com.web.loadJs
import kotlinx.html.*
import java.net.URLEncoder
import java.time.*
import java.time.format.*

fun HTML.eventDetail(event: Event, requestUser: UserProfile?) {
    val now = LocalDateTime.now()
    val eventDate = LocalDateTime.parse(event.date.toString())
    val isToday = eventDate.toLocalDate() == now.toLocalDate()
    val isTomorrow = eventDate.toLocalDate() == now.toLocalDate().plusDays(1)
    val formattedDate = when {
        isToday -> "Today at " + eventDate.format(TimeFormatter)
        isTomorrow -> "Tomorrow at " + eventDate.format(TimeFormatter)
        else -> eventDate.format(DateAndTimeFormatter)
    }
    val isAdmin = (requestUser?.userId == event.organizerId || requestUser?.isAdmin == true)

    val isParticipating = requestUser?.let { user -> event.attendees.any { it.userId == user.userId } } ?: false
    val isWaiting = requestUser?.let { user -> event.waitingList.any { it.user.userId == user.userId } } ?: false

    body {
        div(classes = "w-[90%] mx-auto py-6") {
            div(classes = "bg-gradient-to-br from-yellow-50/80 to-black/5 shadow-lg p-6 rounded-xl border border-yellow-300 transition-all duration-300") {
                img(src = "/resources/uploads/images/${event.headerImagePath}", classes = "w-full h-56 object-cover rounded-xl mb-6 shadow-md", alt = event.title)
                
                // Title and essential info section
                div(classes = "mb-6") {
                    h1(classes = "text-3xl font-bold text-black mb-3") { +event.title }
                    
                    // Date information with icon
                    div(classes = "flex items-center text-yellow-800 mb-3") {
                        svgIcon(SvgIcon.CALENDAR, classes = "w-5 h-5 mr-2 text-yellow-600")
                        p(classes = "font-medium") { +formattedDate }
                    }
                    
                    // Location with icon - moved up as requested
                    div(classes = "flex items-center text-yellow-800 mb-3") {
                        svgIcon(SvgIcon.LOCATION, classes = "w-5 h-5 mr-2 text-yellow-600") 
                        a(href = "https://www.google.com/maps/place/${event.location}", target = "_blank", classes = "hover:underline hover:text-yellow-900 transition-colors") { +event.location }
                    }
                    
                    // Organizer with icon - moved up as requested
                    div(classes = "flex items-center text-yellow-800") {
                        svgIcon(SvgIcon.PROFILE, classes = "w-5 h-5 mr-2 text-yellow-600")
                        span { +"Organized by: " }
                        span(classes = "font-medium ml-1") { +event.organizerName }
                    }
                }
                
                // Description section with improved styling
                div(classes = "mb-6 bg-white/60 p-4 rounded-lg border border-yellow-200 shadow-sm") {
                    h3(classes = "text-lg font-semibold text-black mb-2") { +"About This Event" }
                    p(classes = "text-gray-800 leading-relaxed whitespace-pre-line") { +event.description }
                }
                
                // Countdown section
                div(classes = "space-y-3 mb-6") {
                    if (eventDate > now) {
                        div(classes = "flex items-center bg-yellow-100/70 p-3 rounded-lg shadow-sm") {
                            svgIcon(SvgIcon.TIME, classes = "w-5 h-5 mr-2 text-yellow-700")
                            p(classes = "text-yellow-900 font-medium") {
                                +"Starts in "
                                span(classes = "text-black") { id = "countdown" }
                            }
                        }
                    } else {
                        div(classes = "flex items-center bg-green-100/70 p-3 rounded-lg shadow-sm") {
                            svgIcon(SvgIcon.CHECK_CIRCLE, classes = "w-5 h-5 mr-2 text-green-600")
                            p(classes = "text-green-700 font-medium") { +"Event has started!" }
                        }
                    }
                }

                // Enhanced Participants Section
                div(classes = "mb-8 bg-yellow-50 border border-yellow-200 rounded-lg p-4 transition-all duration-300 hover:shadow-md") {
                    div(classes = "cursor-pointer flex items-center justify-between") {
                        attributes["onclick"] = "toggleParticipants()"

                        div {
                            h3(classes = "text-lg font-bold text-black") { +"Participants" }
                            div(classes = "flex items-center") {
                                span(classes = "text-yellow-700 mr-2") {
                                    +"${event.attendees.size}/${event.maxAttendees}"
                                }
                                if (event.attendees.size >= event.maxAttendees) {
                                    span(classes = "text-red-500 text-sm font-semibold") { +"(Sold out)" }
                                }

                                // Tooltip to indicate clickable
                                span(classes = "text-xs text-yellow-600 ml-2 italic") { +"(click to view all)" }
                            }
                        }

                        div(classes = "flex items-center gap-2") {
                            // User Status Badge
                            requestUser?.let {
                                when {
                                    isParticipating -> span(classes = "inline-block bg-yellow-500 text-black text-xs font-bold px-3 py-1 rounded-full shadow-sm") { +"You're going!" }
                                    isWaiting -> span(classes = "inline-block bg-yellow-400 text-gray-800 text-xs font-bold px-3 py-1 rounded-full shadow-sm") { +"Waiting list" }
                                    event.needsApproval -> span(classes = "inline-block bg-gray-300 text-gray-800 text-xs font-bold px-3 py-1 rounded-full shadow-sm") { +"Needs approval" }
                                }
                            }
                            div(classes = "bg-yellow-100 hover:bg-yellow-200 rounded-full p-1 transition-colors duration-300") {
                                svgIcon(SvgIcon.CHEVRON_DOWN, classes = "w-5 h-5 text-yellow-700 participants-toggle-icon")
                            }
                        }
                    }

                    // Participants List (Hidden by Default)
                    div(classes = "hidden mt-4 pt-3 border-t border-yellow-200") {
                        id = "participants-list"
                        if (event.attendees.isEmpty()) {
                            p(classes = "text-gray-600 text-center py-4 italic") { +"Be the first to join this event!" }
                        } else {
                            div(classes = "grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2") {
                                event.attendees.forEach { attendee ->
                                    div(classes = "flex items-center p-2 rounded-md ${if (requestUser?.userId == attendee.userId) "bg-yellow-100" else ""}") {
                                        // Profile image - UPDATED
                                        div(classes = "w-8 h-8 rounded-full overflow-hidden mr-2") {
                                            userProfileImage(
                                                attendee.profileImagePath,
                                                "${attendee.firstName}'s profile",
                                                "w-full h-full object-cover"
                                            )
                                        }

                                        // User name with flex-grow to push remove button to right
                                        div(classes = "flex-grow") {
                                            span { +"${attendee.firstName} ${attendee.lastName}" }

                                            if (requestUser?.userId == attendee.userId) {
                                                span(classes = "ml-1 text-yellow-700 text-xs font-bold") { +"(You)" }
                                            }
                                        }
                                        
                                        // Remove button - only visible to organizer/admin
                                        if (isAdmin && requestUser?.userId != attendee.userId) {
                                            button(classes = "text-slate-400 hover:text-red-500 transition-colors ml-2") {
                                                attributes["type"] = "button"
                                                attributes["title"] = "Remove user"
                                                attributes["onclick"] = "removeUser(${event.id}, ${attendee.userId})"
                                                svgIcon(SvgIcon.CLOSE, "w-4 h-4")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                }

                // Action Buttons (Social Sharing, Calendar, Back)
                div(classes = "flex flex-row justify-start gap-4") {
                    a(classes = "flex items-center gap-1 text-yellow-700 hover:text-yellow-900 transition-colors duration-300",
                        href = "webcal://yourdomain.com/events/${event.id}.ics") {
                        svgIcon(SvgIcon.CALENDAR, classes = "w-4 h-4")
                        +"Add to Calendar"
                    }
                    p(classes = "flex items-center gap-1 text-yellow-700 hover:text-yellow-900 transition-colors duration-300 cursor-pointer") {
                        attributes["hx-get"] = Routes.Ui.Event.LIST_UPCOMING
                        attributes["hx-target"] = "#main-content"
                        svgIcon(SvgIcon.ARROW_LEFT, classes = "w-4 h-4")
                        +"Back to Events"
                    }
                    // Join Button Section
                    div(classes = "ml-auto flex justify-end") {
                        if (eventDate <= now) {
                            span(classes = "text-gray-500 text-base") { +"Event has started." }
                        } else if (event.attendees.size >= event.maxAttendees && !isParticipating) {
                            button(classes = "bg-yellow-500 hover:bg-yellow-600 text-black px-4 py-2 rounded-lg shadow transition-colors duration-300") {
                                attributes["onclick"] = "joinWaitingList(${event.id})"
                                +"Join Waiting List"
                            }
                        } else if (!isParticipating && !isWaiting) {
                            projectButton(
                                text = "Join Event",
                                onClick = "joinEvent(${event.id})",
                                extraClasses = "px-4 py-3 rounded-lg shadow-md transition-colors duration-300"
                            )
                        }
                    }
                }
            }

            // Admin Waiting List Section (Collapsible)
            if (isAdmin) {
                div(classes = "mt-6 bg-white bg-opacity-80 shadow-lg px-6 py-2 rounded-xl border border-yellow-200") {
                    id = "waiting-list-container"
                    div(classes = "flex justify-between items-center mb-2 cursor-pointer") {
                        attributes["onclick"] = "toggleWaitingList()"
                        h2(classes = "text-2xl font-semibold text-black") { +"Waiting List" }
                        button(classes = "text-yellow-600 hover:text-yellow-800 focus:outline-none") {
                            svgIcon(SvgIcon.CHEVRON_DOWN, classes = "w-6 h-6 waiting-list-toggle-icon")
                        }
                    }
                    div(classes = "hidden") {
                        id = "waiting-list"
                        if (event.waitingList.isEmpty()) {
                            p(classes = "text-gray-600 text-base py-3") { +"No users on the waiting list." }
                        } else {
                            div {
                                div(classes = "flex flex-row justify-between border-b border-gray-200 pb-2 font-medium") {
                                    p(classes = "text-gray-700 w-1/3") { +"User" }
                                    p(classes = "text-gray-700 w-1/3 text-center") { +"Joined At" }
                                    p(classes = "text-gray-700 w-1/3 text-right") { +"Actions" }
                                }
                                event.waitingList.forEach { waitingList ->
                                    div(classes = "flex flex-row justify-between py-2 border-b border-gray-200 text-sm items-center") {
                                        div(classes = "flex items-center w-1/3") {
                                            // UPDATED: Use userProfileImage
                                            div(classes = "w-8 h-8 rounded-full overflow-hidden mr-2") {
                                                userProfileImage(
                                                    waitingList.user.profileImagePath,
                                                    "${waitingList.user.firstName}'s profile",
                                                    "w-full h-full object-cover"
                                                )
                                            }
                                            p { +"${waitingList.user.firstName} ${waitingList.user.lastName}" }
                                        }
                                        p(classes = "w-1/3 text-center") {
                                            +waitingList.joinedAt.format(DateAndTimeFormatter)
                                        }
                                        div(classes = "w-1/3 flex justify-end") {
                                            button(classes = "bg-yellow-500 text-black px-4 py-1 rounded hover:bg-yellow-600 transition-colors duration-300") {
                                                attributes["onclick"] = "approveUser(${waitingList.eventId}, ${waitingList.user.userId})"
                                                +"Approve"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Set the event date variable for the JavaScript to use
            script { +"const eventDate = '${event.date}'; const eventId = ${event.id};" }
            script { +"setNavigateUrl('${Routes.Ui.Event}');" }

            // Load the external JS file that contains the functions
            loadJs("event/event-detail")
        }
    }
}

val DateAndTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")