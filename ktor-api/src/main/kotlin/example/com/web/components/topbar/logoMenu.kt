package example.com.web.components.topbar

import kotlinx.html.*

fun HtmlBlockTag.logoMenu() {
    div (classes = "w-20 h-20 absolute top-full left-0 z-10 hidden rounded-lg bg-gray-200 mt-3") {
        id = "logo-menu-dropdown"
        +"test"
    }
}