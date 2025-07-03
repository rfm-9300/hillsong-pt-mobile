package example.com.web.pages.profilePage

import example.com.data.db.event.Event
import example.com.data.db.user.UserProfile
import example.com.data.utils.monthYear
import example.com.web.components.layout.layout
import example.com.web.components.svgIcon
import example.com.web.components.SvgIcon
import example.com.web.components.userProfileImage
import example.com.web.loadJs
import kotlinx.html.*
import java.time.format.DateTimeFormatter

fun HTML.profilePage(user: UserProfile) {
    val userName = "${user.firstName} ${user.lastName}"
    val joinString = "Joined on ${user.joinedAt?.monthYear()}"
    val hostedEvents = user.hostedEvents.size
    val attendedEvents = user.attendedEvents.size
    val waitingEvents = user.waitingListEvents.size
    val attendingEvents = user.attendingEvents.size
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")

    layout {
        div(classes = "flex flex-col w-full items-center justify-center relative max-w-4xl mx-auto px-3 sm:px-4") {
            // hidden floating box change profile picture
            div(classes = "relative w-full flex justify-center") {
                profileEditBox()
            }
            // Profile Header Section
            div(classes = "w-full bg-white rounded-xl shadow-sm p-4 sm:p-6 mb-4 sm:mb-8 border border-yellow-200") {
                div(classes = "flex flex-col md:flex-row items-center md:items-start gap-4 sm:gap-6") {
                    // Profile Image Section
                    div(classes = "relative group") {
                        userProfileImage(
                            user.profileImagePath,
                            "Profile picture of $userName",
                            "w-28 h-28 sm:w-32 sm:h-32 rounded-full object-cover border-4 border-yellow-100 shadow-lg"
                        )

                        // Edit button (hidden by default, shown on hover)
                        button(classes = "absolute inset-0 flex items-center justify-center w-28 h-28 sm:w-32 sm:h-32 " +
                                "rounded-full bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 " +
                                "transition-opacity duration-200 text-white font-medium") {
                            onClick = "showEditProfile()"
                            attributes["aria-label"] = "Edit profile picture"
                            +"Edit"
                        }
                    }

                    // User Info Section
                    div(classes = "flex-1 text-center md:text-left") {
                        h1(classes = "text-xl sm:text-2xl font-bold text-black mb-1 sm:mb-2") { +userName }
                        p(classes = "text-xs sm:text-sm text-yellow-800 mb-3 sm:mb-4") { +joinString }
                        
                        // Stats Grid
                        div(classes = "grid grid-cols-2 md:grid-cols-4 gap-2 sm:gap-4") {
                            statItem("Hosted", hostedEvents, SvgIcon.CALENDAR)
                            statItem("Attended", attendedEvents, SvgIcon.PROFILE)
                            statItem("Waiting", waitingEvents, SvgIcon.TIME)
                            statItem("Attending", attendingEvents, SvgIcon.CHECK_CIRCLE)
                        }
                    }
                }
            }

            // Events Section
            div(classes = "w-full bg-white rounded-xl shadow-sm p-4 sm:p-6 border border-yellow-200") {
                // Tabs Navigation
                div(classes = "flex flex-nowrap overflow-x-auto hide-scrollbar border-b border-yellow-300 mb-4 sm:mb-6") {
                    tabButton("hosted", "Hosted Events", hostedEvents, true)
                    tabButton("attended", "Attended Events", attendedEvents, false)
                    tabButton("waiting", "Waiting List", waitingEvents, false)
                    tabButton("attending", "Attending Events", attendingEvents, false)
                }

                // Tab Content
                div(classes = "mt-3 sm:mt-4") {
                    // Hosted Events
                    tabContent("hosted", user.hostedEvents, dateFormatter, "No hosted events yet")

                    // Attended Events
                    tabContent("attended", user.attendedEvents, dateFormatter, "No attended events yet")

                    // Waiting List Events
                    tabContent("waiting", user.waitingListEvents, dateFormatter, "No events in waiting list")

                    // Attending Events
                    tabContent("attending", user.attendingEvents, dateFormatter, "No attending events yet")
                }
            }
        }
        loadJs("profile-page")
    }
}

private fun FlowContent.statItem(label: String, count: Int, icon: SvgIcon) {
    div(classes = "bg-yellow-50/80 rounded-lg p-2 sm:p-3 text-center border border-yellow-200") {
        div(classes = "flex items-center justify-center text-yellow-600 mb-1") {
            svgIcon(icon, "w-4 h-4 sm:w-5 sm:h-5")
        }
        div(classes = "text-base sm:text-lg font-semibold text-black") { +count.toString() }
        div(classes = "text-xs sm:text-sm text-yellow-800") { +label }
    }
}

private fun FlowContent.tabButton(id: String, label: String, count: Int, isActive: Boolean) {
    button(classes = "px-3 sm:px-4 py-2 text-xs sm:text-sm font-medium whitespace-nowrap transition-colors duration-200 flex-shrink-0 " +
            if (isActive) "text-yellow-600 border-b-2 border-yellow-500" else "text-gray-500 hover:text-yellow-700") {
        attributes["onclick"] = "switchTab('$id')"
        attributes["aria-selected"] = isActive.toString()
        this.id = "$id-tab"
        +"$label ($count)"
    }
}

private fun FlowContent.tabContent(id2: String, events: List<Event>, dateFormatter: DateTimeFormatter, emptyMessage: String) {
    div(classes = if (id2 == "hosted") "" else "hidden") {
        id = "$id2-content"
        if (events.isEmpty()) {
            emptyState(emptyMessage)
        } else {
            div(classes = "grid gap-3 sm:gap-4 grid-cols-1 md:grid-cols-2") {
                events.forEach { event ->
                    eventCard(event, dateFormatter)
                }
            }
        }
    }
}

private fun FlowContent.emptyState(message: String) {
    div(classes = "text-center py-8 sm:py-12") {
        svgIcon(SvgIcon.CALENDAR, "w-10 h-10 sm:w-12 sm:h-12 mx-auto text-yellow-400 mb-3 sm:mb-4")
        p(classes = "text-yellow-800 text-base sm:text-lg") { +message }
    }
}

private fun FlowContent.eventCard(event: Event, dateFormatter: DateTimeFormatter) {
    div(classes = "bg-white rounded-lg shadow-sm hover:shadow-md transition-all duration-200 border border-yellow-200 hover:border-yellow-400 hover:bg-yellow-50/80") {
        div(classes = "p-3 sm:p-4") {
            // Event Header
            div(classes = "flex justify-between items-start mb-2 sm:mb-3") {
                h3(classes = "font-semibold text-base sm:text-lg text-black") {
                    +event.title
                }
                a(classes = "px-2 sm:px-3 py-1 text-xs sm:text-sm bg-yellow-50 hover:bg-yellow-100 text-yellow-700 rounded-lg transition-colors") {
                    href = "/events/${event.id}"
                    +"View"
                }
            }

            // Event Description
            p(classes = "text-xs sm:text-sm text-yellow-800 mb-3 sm:mb-4 line-clamp-2") {
                +event.description
            }

            // Event Details
            div(classes = "space-y-1 sm:space-y-2") {
                // Date and Attendees
                div(classes = "flex items-center text-xs sm:text-sm text-yellow-700") {
                    svgIcon(SvgIcon.CALENDAR, "w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2 text-yellow-600")
                    +event.date.format(dateFormatter)
                }
                
                // Attendee Count
                div(classes = "flex items-center text-xs sm:text-sm text-yellow-700") {
                    svgIcon(SvgIcon.PROFILE, "w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2 text-yellow-600")
                    +"${event.attendees.size} attendees"
                }
                
                // Location
                div(classes = "flex items-center text-xs sm:text-sm text-yellow-700") {
                    svgIcon(SvgIcon.HOME, "w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2 text-yellow-600")
                    +event.location
                }
            }
        }
    }
}