package example.com.web.components

import kotlinx.html.HtmlBlockTag
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.id

fun HtmlBlockTag.projectButton(
    text: String = "Join Event",
    onClick: String? = null,
    hxGet: String? = null, // Optional htmx hx-get attribute
    hxTarget: String? = null, // Optional htmx hx-target attribute
    extraClasses: String = "py-3",
    disabled: Boolean = false,
    buttonId: String = "button-id"
) {
    button(classes = "bg-gradient-to-r from-yellow-500  to-black hover:from-yellow-600 hover:to-gray-900 text-white font-semibold px-5 rounded-lg shadow-md focus:outline-none focus:ring-2 focus:ring-yellow-300 transition-all duration-200 $extraClasses") {
        id = buttonId
        if (onClick != null) {
            attributes["onclick"] = onClick
        }
        if (hxGet != null) {
            attributes["hx-get"] = hxGet
        }
        if (hxTarget != null) {
            attributes["hx-target"] = hxTarget
        }
        if (disabled) {
            attributes["disabled"] = "true"
            classes = classes + " opacity-50 cursor-not-allowed"
        }
        +text
    }
}