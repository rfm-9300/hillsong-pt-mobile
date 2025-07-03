package example.com.web.pages.homePage.navbar

import example.com.routes.Routes
import kotlinx.html.*

fun HtmlBlockTag.navbar() {
    nav(classes = "bg-red w-full shadow px-4 py-4 top-0 left-0 right-0 z-10 rounded-t-2xl") {
        div(classes = "mx-auto max-w-6xl w-full flex justify-between items-center") {
            ul(classes = "hidden md:flex space-x-8 text-gray-600 font-medium mx-auto") {
                val tabs = mapOf(
                    "Home" to Routes.Ui.Home.HOME,
                    "Events" to Routes.Ui.Event.LIST_UPCOMING,
                )
                tabs.forEach { (label, url) ->
                    li(classes = "nav-tab cursor-pointer hover:text-blue-600 transition text-gray-600") {
                        attributes["hx-get"] = url
                        attributes["hx-target"] = "#main-content"
                        attributes["onclick"] = "setActiveTab(this)"
                        // If this is the initial active tab
                        if (label == "Home") {  // or whatever logic you use to determine active tab
                            attributes["class"] = attributes["class"] + " text-blue-600 font-bold"
                            attributes["id"] = "home-tab"
                        }
                        +label
                    }
                }
            }
        }
    }

    script (src = "/resources/js/navBar.js"){}
}