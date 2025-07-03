package example.com.web.pages.auth

import example.com.routes.Routes
import example.com.web.components.layout.layout
import example.com.web.components.svgIcon
import example.com.web.components.SvgIcon
import example.com.web.loadJs
import kotlinx.html.*
import kotlinx.html.InputType.*

fun HTML.loginPage(googleClientId: String = "", facebookAppId: String = "") {
    layout {
        div(classes = "flex items-center justify-center min-h-[80vh]") {
            div(classes = "bg-white bg-opacity-90 p-8 rounded-xl shadow-lg border border-yellow-300 w-[28rem] max-w-full transform transition-all duration-300") {
                form(
                    action = "#",
                    method = FormMethod.post,
                    classes = "space-y-6"
                ) {
                    id = "login-form"
                    
                    // Hidden inputs for social login configurations
                    input(type = InputType.hidden, name = "google-client-id") {
                        id = "google-client-id"
                        value = googleClientId
                    }
                    
                    input(type = InputType.hidden, name = "facebook-app-id") {
                        id = "facebook-app-id"
                        value = facebookAppId
                    }
                    
                    // Title with icon
                    div(classes = "text-center mb-6") {
                        div(classes = "inline-flex items-center justify-center w-16 h-16 rounded-full bg-yellow-100 mb-4") {
                            svgIcon(SvgIcon.PROFILE, "w-8 h-8 text-yellow-600")
                        }
                        h2(classes = "text-2xl font-bold text-black") {
                            +"Welcome to Active Hive"
                        }
                        p(classes = "text-gray-700 mt-1") {
                            +"Sign in to your account to continue"
                        }
                    }

                    // Email field
                    div {
                        label(classes = "block text-sm font-medium text-gray-700 mb-1") {
                            attributes["for"] = "email"
                            +"Email"
                        }
                        div(classes = "relative") {
                            // Email icon
                            div(classes = "absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none") {
                                svgIcon(SvgIcon.EMAIL, "w-5 h-5 text-yellow-500")
                            }
                            
                            input(classes = "pl-10 block w-full rounded-lg border-yellow-300 bg-yellow-50 py-3 shadow-sm focus:border-yellow-500 focus:ring-yellow-500 focus:outline-none") {
                                id = "email"
                                type = text
                                name = "email"
                                required = true
                                placeholder = "Enter your email"
                            }
                        }
                    }

                    // Password field
                    div {
                        label(classes = "block text-sm font-medium text-gray-700 mb-1") {
                            attributes["for"] = "password"
                            +"Password"
                        }
                        div(classes = "relative") {
                            // Lock icon
                            div(classes = "absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none") {
                                svgIcon(SvgIcon.LOCK, "w-5 h-5 text-yellow-500")
                            }
                            
                            input(classes = "pl-10 block w-full rounded-lg border-yellow-300 bg-yellow-50 py-3 shadow-sm focus:border-yellow-500 focus:ring-yellow-500 focus:outline-none") {
                                id = "password"
                                type = password
                                name = "password"
                                required = true
                                placeholder = "Enter your password"
                            }
                        }
                        // Forgot password link
                        div(classes = "text-right mt-1") {
                            span(classes = "text-sm font-medium text-yellow-700 hover:text-yellow-600 cursor-pointer transition-colors duration-300") {
                                attributes["hx-get"] = Routes.Ui.Auth.FORGOT_PASSWORD
                                attributes["hx-target"] = "#main-content"
                                +"Forgot Password?"
                            }
                        }
                    }

                    div {
                        button(classes = "w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-base font-medium text-white bg-gradient-to-r from-yellow-500 to-black hover:from-yellow-600 hover:to-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors duration-300") {
                            type = ButtonType.submit
                            
                            // Login icon
                            svgIcon(SvgIcon.LOGIN, "w-5 h-5 mr-2")
                            +"Sign In"
                        }
                    }
                    
                    // Divider
                    div(classes = "relative my-4") {
                        div(classes = "absolute inset-0 flex items-center") {
                            div(classes = "w-full border-t border-yellow-300") {}
                        }
                        div(classes = "relative flex justify-center text-sm") {
                            span(classes = "px-2 bg-white text-gray-600") {
                                +"OR"
                            }
                        }
                    }
                    
                    // Google Sign-In Button
                    div {
                        button(classes = "w-full flex justify-center items-center py-3 px-4 border border-yellow-300 rounded-lg shadow-sm text-base font-medium text-gray-700 bg-white hover:bg-yellow-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors duration-300") {
                            type = ButtonType.button
                            id = "google-login-btn"
                            
                            // Google icon (simple G icon)
                            div(classes = "w-5 h-5 mr-2 flex items-center justify-center") {
                                unsafe {
                                    +"""
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                                    </svg>
                                    """
                                }
                            }
                            +"Sign in with Google"
                        }
                    }

                    // Facebook Sign-In Button
                    div(classes = "mt-2") {
                        button(classes = "w-full flex justify-center items-center py-3 px-4 border border-yellow-300 rounded-lg shadow-sm text-base font-medium text-gray-700 bg-white hover:bg-yellow-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 transition-colors duration-300") {
                            type = ButtonType.button
                            id = "facebook-login-btn"
                            
                            // Facebook icon
                            div(classes = "w-5 h-5 mr-2 flex items-center justify-center") {
                                unsafe {
                                    +"""
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                                        <path fill="#1877F2" d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                                    </svg>
                                    """
                                }
                            }
                            +"Sign in with Facebook"
                        }
                    }

                    div(classes = "text-center mt-6") {
                        p(classes = "text-sm text-gray-600") {
                            +"Don't have an account? "
                            span(classes = "font-medium text-yellow-700 hover:text-yellow-600 cursor-pointer transition-colors duration-300") {
                                attributes["hx-get"] = Routes.Ui.Auth.SIGNUP
                                attributes["hx-target"] = "#main-content"
                                +"Register"
                            }
                        }
                    }
                }
            }
        }
        loadJs("auth/login")
    }
}
