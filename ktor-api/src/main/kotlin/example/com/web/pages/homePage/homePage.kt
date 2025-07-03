package example.com.web.pages.homePage

import example.com.data.db.event.EventRepository
import example.com.routes.Routes
import example.com.web.components.layout.layout
import example.com.web.components.topbar.topbar
import example.com.web.loadJs
import example.com.web.pages.homePage.eventTab.upcomingEvents
import example.com.web.components.SvgIcon
import example.com.web.components.svgIcon
import kotlinx.html.*

fun HTML.homePage(eventRepository: EventRepository, isAdminRequest: Boolean) {
    layout {
        div(classes = "flex flex-col w-full bg-gradient-to-br from-yellow-50 via-yellow-100 to-black-50 min-h-screen") {
            id = "root-container"
            // Sticky topbar container
            div(classes = "sticky top-0 z-20 w-full") {
                topbar()
            }
            // Navigation Bar
            //navbar()

            // Alert Box
            div(classes = "fixed z-20 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 hidden p-4 rounded-lg shadow-lg bg-white bg-opacity-90 border border-yellow-400 w-[90%] max-w-[400px] sm:min-w-[300px]") {
                id = "alert-box"
                div(classes = "flex items-center justify-between") {
                    span(classes = "text-gray-800 font-semibold flex-grow") {
                        id = "alert-message"
                    }
                    span(classes = "ml-3 cursor-pointer text-yellow-600 hover:text-yellow-800 transition-colors duration-300 p-1 rounded-full hover:bg-yellow-100") {
                        onClick = "closeAlert()"
                        svgIcon(SvgIcon.CLOSE, classes = "w-5 h-5")
                    }
                }
            }

            div (classes = "w-full flex flex-col justify-center items-center relative px-2 sm:px-0") {
                id = "main-content-bg"

                // Back to Home Icon - improved mobile positioning
                div(classes = "z-50 fixed top-[75px] left-2 sm:top-[10%] sm:left-[5%] md:left-[10%] lg:left-[15%] text-yellow-500 hover:text-yellow-700 hover:bg-yellow-300 transition-colors duration-300 bg-yellow-100 rounded-full p-1.5 sm:p-2 shadow-md cursor-pointer") {
                    attributes["onclick"] = "navigate()"
                    svgIcon(SvgIcon.ARROW_LEFT, classes = "w-4 h-4 sm:w-5 sm:h-5")
                }
                
                // Content - responsive width
                div(classes = "flex flex-col justify-center items-center w-full sm:w-[90%] md:w-[85%] lg:w-[70%] xl:w-[60%] mt-1 py-2") {
                    id = "main-content"
                    upcomingEvents(eventRepository, isAdminRequest)
                }
            }

        }
        loadJs("home")
    }
}