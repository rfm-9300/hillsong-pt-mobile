package example.com.web.pages.auth

import example.com.web.components.layout.layout
import example.com.web.loadJs
import kotlinx.html.*
import kotlinx.html.InputType.*

fun HTML.signupPage() {
    layout {
        div(classes = "bg-white p-8 rounded-lg shadow-md w-96") {
            form(
                action = "#",
                method = FormMethod.post,
                classes = "space-y-6"
            ) {
                id = "signup-form"
                h2(classes = "text-center text-2xl font-bold text-gray-800") {
                    +"Create Your Account"
                }

                div {
                    //profile inputs
                    label(classes = "block text-sm font-medium text-gray-700") {
                        attributes["for"] = "first-name"
                        +"First Name"
                    }
                    input(classes = "mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50") {
                        id = "first-name"
                        type = text
                        name = "first-name"
                        required = true
                        placeholder = "Enter your first name"
                    }
                }
                div {
                    label(classes = "block text-sm font-medium text-gray-700") {
                        attributes["for"] = "last-name"
                        +"Last Name"
                    }
                    input(classes = "mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50") {
                        id = "last-name"
                        type = text
                        name = "last-name"
                        required = true
                        placeholder = "Enter your last name"
                    }
                }

                div {
                    label(classes = "block text-sm font-medium text-gray-700") {
                        attributes["for"] = "email"
                        +"Email"
                    }
                    input(classes = "mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50") {
                        id = "email"
                        type = email
                        name = "email"
                        required = true
                        placeholder = "Enter your email"
                    }
                }

                div {
                    label(classes = "block text-sm font-medium text-gray-700") {
                        attributes["for"] = "password"
                        +"Password"
                    }
                    input(classes = "mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50") {
                        id = "password"
                        type = password
                        name = "password"
                        required = true
                        placeholder = "Enter your password"
                    }
                }

                div {
                    label(classes = "block text-sm font-medium text-gray-700") {
                        attributes["for"] = "confirm-password"
                        +"Confirm Password"
                    }
                    input(classes = "mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-300 focus:ring focus:ring-indigo-200 focus:ring-opacity-50") {
                        id = "confirm-password"
                        type = password
                        name = "confirm-password"
                        required = true
                        placeholder = "Confirm your password"
                    }
                }

                div {
                    button(classes = "w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500") {
                        type = ButtonType.submit
                        +"Sign Up"
                    }
                }

                div(classes = "text-center") {
                    p(classes = "mt-2 text-sm text-gray-600") {
                        +"Already have an account? "
                        a(href = "/login", classes = "font-medium text-indigo-600 hover:text-indigo-500") {
                            +"Login"
                        }
                    }
                }
            }

        }
        loadJs("auth/sign-up")
    }
}