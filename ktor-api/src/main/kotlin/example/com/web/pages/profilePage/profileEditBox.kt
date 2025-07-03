package example.com.web.pages.profilePage

import example.com.web.components.SvgIcon
import example.com.web.components.svgIcon
import kotlinx.html.*

fun FlowContent.profileEditBox() {
    div(classes = "absolute bg-white rounded-xl shadow-lg p-4 sm:p-6 z-10 top-24 sm:top-32 transform hidden transition-transform duration-300 border border-yellow-200 w-[95%] sm:w-auto max-w-[400px]") {
        id = "profile-edit-box"
        
        // Close button
        div(classes = "absolute top-2 right-2") {
            button(classes = "text-yellow-600 hover:text-yellow-800") {
                onClick = "hideEditProfile()"
                attributes["aria-label"] = "Close"
                svgIcon(SvgIcon.CLOSE, "w-4 h-4 sm:w-5 sm:h-5")
            }
        }
        
        // Title
        h3(classes = "text-base sm:text-lg font-medium text-black mb-3 sm:mb-4") {
            +"Change Profile Picture"
        }
        
        // Form
        form(classes = "space-y-3 sm:space-y-4") {
            encType = FormEncType.multipartFormData
            method = FormMethod.post
            action = "#"

            
            // File input
            div {
                label(classes = "block text-xs sm:text-sm font-medium text-yellow-800 mb-1") {
                    attributes["for"] = "profile-picture"
                    +"Select Image"
                }
                input(classes = "w-full text-xs sm:text-sm text-yellow-800 file:mr-3 sm:file:mr-4 file:py-1 sm:file:py-2 file:px-3 sm:file:px-4 file:rounded-md file:border-0 file:text-xs sm:file:text-sm file:font-semibold file:bg-yellow-50 file:text-yellow-700 hover:file:bg-yellow-100") {
                    type = InputType.file
                    id = "profile-picture"
                    name = "profile-picture"
                    accept = "image/*"
                }
            }
            
            // Preview (hidden initially)
            div(classes = "hidden") {
                id = "image-preview-container"
                label(classes = "block text-xs sm:text-sm font-medium text-yellow-800 mb-1") {
                    +"Preview"
                }
                div(classes = "w-32 h-32 sm:w-48 sm:h-48 md:w-64 md:h-64 rounded-full overflow-hidden border border-yellow-200 mx-auto") {
                    img(classes = "w-full h-full object-cover") {
                        id = "image-preview"
                        src = ""
                        alt = "Preview"
                    }
                }
            }
            
            // Submit button
            div(classes = "pt-1 sm:pt-2") {
                button(classes = "w-full py-1.5 sm:py-2 px-3 sm:px-4 border border-transparent rounded-md shadow-sm text-xs sm:text-sm font-medium text-white bg-gradient-to-r from-yellow-500 to-black hover:from-yellow-600 hover:to-gray-900 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500") {
                    attributes["type"] = "button"
                    onClick = "submitProfileEdit()"
                    +"Upload Picture"
                }
            }
        }
    }
}