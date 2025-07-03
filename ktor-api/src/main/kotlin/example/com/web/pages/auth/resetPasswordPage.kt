package example.com.web.pages.auth

import example.com.routes.Routes
import example.com.web.components.layout.layout
import example.com.web.components.svgIcon
import example.com.web.components.SvgIcon
import example.com.web.loadJs
import kotlinx.html.*
import kotlinx.html.InputType.*

fun HTML.resetPasswordPage(resetToken: String? = null) {
    layout {
        // Alert Box
        div(classes = "fixed z-20 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 hidden p-4 rounded-lg shadow-lg bg-white bg-opacity-90 border border-blue-200 min-w-[300px]") {
            id = "alert-box"
            div(classes = "flex items-center justify-between") {
                span(classes = "text-gray-800 font-semibold flex-grow") {
                    id = "alert-message"
                }
                span(classes = "ml-3 cursor-pointer text-red-500 hover:text-red-700 transition-colors duration-300 p-1 rounded-full hover:bg-red-100") {
                    onClick = "closeAlert()"
                    svgIcon(SvgIcon.CLOSE, classes = "w-5 h-5")
                }
            }
        }
        div(classes = "flex items-center justify-center min-h-[80vh]") {
            div(classes = "bg-white bg-opacity-90 p-8 rounded-xl shadow-lg border border-slate-200 w-[28rem] max-w-full transform transition-all duration-300") {
                if (resetToken == null) {
                    // Request password reset form
                    requestResetForm()
                } else {
                    // Set new password form
                    newPasswordForm(resetToken)
                }
            }
        }
        loadJs("auth/reset-password")
    }
}

private fun FlowContent.requestResetForm() {
    form(
        action = "#",
        method = FormMethod.post,
        classes = "space-y-6"
    ) {
        id = "reset-request-form"
        
        // Title with icon
        div(classes = "text-center mb-6") {
            div(classes = "inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-100 mb-4") {
                svgIcon(SvgIcon.LOCK, "w-8 h-8 text-purple-600")
            }
            h2(classes = "text-2xl font-bold text-slate-800") {
                +"Reset Your Password"
            }
            p(classes = "text-slate-500 mt-1") {
                +"Enter your email to receive a password reset link"
            }
        }

        // Email field
        div {
            label(classes = "block text-sm font-medium text-slate-700 mb-1") {
                attributes["for"] = "email"
                +"Email"
            }
            div(classes = "relative") {
                // Email icon
                div(classes = "absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none") {
                    svgIcon(SvgIcon.EMAIL, "w-5 h-5 text-slate-400")
                }
                
                input(classes = "pl-10 block w-full rounded-lg border-slate-300 bg-slate-50 py-3 shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:outline-none") {
                    id = "email"
                    type = text
                    name = "email"
                    required = true
                    placeholder = "Enter your email"
                }
            }
        }

        // Submit button
        div {
            button(classes = "w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-base font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors duration-300") {
                type = ButtonType.submit
                id = "reset-request-btn"
                
                svgIcon(SvgIcon.EMAIL, "w-5 h-5 mr-2")
                +"Send Reset Link"
            }
        }

        // Back to login link
        div(classes = "text-center mt-6") {
            p(classes = "text-sm text-slate-600") {
                +"Remember your password? "
                span(classes = "font-medium text-purple-600 hover:text-purple-500 cursor-pointer transition-colors duration-300") {
                    attributes["hx-get"] = Routes.Ui.Auth.LOGIN
                    attributes["hx-target"] = "#main-content"
                    +"Back to Login"
                }
            }
        }
    }
}

private fun FlowContent.newPasswordForm(resetToken: String) {
    form(
        action = "#",
        method = FormMethod.post,
        classes = "space-y-6"
    ) {
        id = "reset-password-form"
        
        // Hidden token field
        input(type = InputType.hidden, name = "token") {
            value = resetToken
        }
        
        // Title with icon
        div(classes = "text-center mb-6") {
            div(classes = "inline-flex items-center justify-center w-16 h-16 rounded-full bg-purple-100 mb-4") {
                svgIcon(SvgIcon.LOCK, "w-8 h-8 text-purple-600")
            }
            h2(classes = "text-2xl font-bold text-slate-800") {
                +"Create New Password"
            }
            p(classes = "text-slate-500 mt-1") {
                +"Please enter your new password"
            }
        }

        // New Password field
        div {
            label(classes = "block text-sm font-medium text-slate-700 mb-1") {
                attributes["for"] = "new-password"
                +"New Password"
            }
            div(classes = "relative") {
                // Lock icon
                div(classes = "absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none") {
                    svgIcon(SvgIcon.LOCK, "w-5 h-5 text-slate-400")
                }
                
                input(classes = "pl-10 block w-full rounded-lg border-slate-300 bg-slate-50 py-3 shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:outline-none") {
                    id = "new-password"
                    type = password
                    name = "new-password"
                    required = true
                    placeholder = "Enter new password"
                    attributes["minlength"] = "8"
                }
            }
            p(classes = "mt-1 text-xs text-slate-500") {
                +"Must be at least 8 characters"
            }
        }
        
        // Confirm Password field
        div {
            label(classes = "block text-sm font-medium text-slate-700 mb-1") {
                attributes["for"] = "confirm-password"
                +"Confirm Password"
            }
            div(classes = "relative") {
                // Lock icon
                div(classes = "absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none") {
                    svgIcon(SvgIcon.LOCK, "w-5 h-5 text-slate-400")
                }
                
                input(classes = "pl-10 block w-full rounded-lg border-slate-300 bg-slate-50 py-3 shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:outline-none") {
                    id = "confirm-password"
                    type = password
                    name = "confirm-password"
                    required = true
                    placeholder = "Confirm new password"
                }
            }
        }

        // Submit button
        div {
            button(classes = "w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-base font-medium text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-colors duration-300") {
                type = ButtonType.submit
                id = "reset-password-btn"
                
                svgIcon(SvgIcon.CHECK_CIRCLE, "w-5 h-5 mr-2")
                +"Reset Password"
            }
        }

        // Back to login link
        div(classes = "text-center mt-6") {
            p(classes = "text-sm text-slate-600") {
                +"Remember your password? "
                span(classes = "font-medium text-purple-600 hover:text-purple-500 cursor-pointer transition-colors duration-300") {
                    attributes["hx-get"] = Routes.Ui.Auth.LOGIN
                    attributes["hx-target"] = "#main-content"
                    +"Back to Login"
                }
            }
        }
    }
} 