package example.com.web.pages.homePage.eventTab

import example.com.web.loadJs
import kotlinx.html.*

fun HTML.createEvent() {
    body {
        div(classes = "w-full py-4") {
            // Container for the form with consistent width
            div(classes = "w-[80%] mx-auto") {
                // Header section
                div(classes = "flex justify-between items-center mb-6") {
                    p(classes = "text-2xl font-bold text-blue-600") { +"Create New Event" }
                }

                // Form container with consistent styling
                form(
                    action = "#",
                    method = FormMethod.post,
                    classes = "bg-white bg-opacity-80 shadow-lg rounded-xl border border-blue-200 p-6 transition-all duration-300",
                    encType = FormEncType.multipartFormData
                ) {
                    id = "event-form"
                    attributes["name"] = "eventForm"

                    // Title
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            attributes["for"] = "title"
                            +"Event Title"
                        }
                        input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                            attributes["type"] = "text"
                            attributes["name"] = "title"
                            attributes["id"] = "title"
                            attributes["required"] = "true"
                            attributes["placeholder"] = "Enter event title"
                        }
                    }

                    // Description
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            attributes["for"] = "description"
                            +"Description"
                        }
                        textArea(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                            attributes["name"] = "description"
                            attributes["id"] = "description"
                            attributes["rows"] = "4"
                            attributes["placeholder"] = "Describe your event"
                        }
                    }

                    // Event date and time
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            +"Event Date & Time"
                        }
                        
                        // Date and time container
                        div(classes = "grid grid-cols-1 md:grid-cols-2 gap-4") {
                            // Date selector
                            div {
                                label(classes = "block text-sm text-gray-600 mb-1") {
                                    attributes["for"] = "date"
                                    +"Date"
                                }
                                input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                                    attributes["type"] = "date"
                                    attributes["name"] = "date"
                                    attributes["id"] = "date"
                                    attributes["required"] = "true"
                                }
                            }
                            
                            // Time selector with hour and minute inputs
                            div {
                                label(classes = "block text-sm text-gray-600 mb-1") {
                                    +"Time"
                                }
                                div(classes = "flex items-center gap-2") {
                                    // Hour input
                                    div(classes = "flex-1") {
                                        input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                                            attributes["type"] = "number"
                                            attributes["name"] = "hour"
                                            attributes["id"] = "hour"
                                            attributes["required"] = "true"
                                            attributes["min"] = "0"
                                            attributes["max"] = "23"
                                            attributes["placeholder"] = "HH"
                                        }
                                    }
                                    
                                    // Separator
                                    span(classes = "text-gray-500") { +":" }
                                    
                                    // Minute input
                                    div(classes = "flex-1") {
                                        input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                                            attributes["type"] = "number"
                                            attributes["name"] = "minute"
                                            attributes["id"] = "minute"
                                            attributes["required"] = "true"
                                            attributes["min"] = "0"
                                            attributes["max"] = "59"
                                            attributes["placeholder"] = "MM"
                                        }
                                    }
                                }
                                // Quick select buttons
                                div(classes = "mt-2 flex flex-wrap gap-2") {
                                    // Common times
                                    listOf("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00").forEach { time ->
                                        button(classes = "text-xs px-2 py-1 rounded bg-gray-100 hover:bg-gray-200 text-gray-700 transition-colors duration-200") {
                                            attributes["type"] = "button"
                                            attributes["onclick"] = "setTime('$time')"
                                            +time
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Location
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            attributes["for"] = "location"
                            +"Location"
                        }
                        input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                            attributes["type"] = "text"
                            attributes["name"] = "location"
                            attributes["id"] = "location"
                            attributes["placeholder"] = "Enter event location"
                        }
                    }

                    // Max attendees
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            attributes["for"] = "maxAttendees"
                            +"Max Attendees"
                        }
                        input(classes = "shadow appearance-none border rounded-lg w-full py-2 px-3 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent") {
                            attributes["type"] = "number"
                            attributes["name"] = "maxAttendees"
                            attributes["id"] = "maxAttendees"
                            attributes["required"] = "true"
                            attributes["min"] = "1"
                            attributes["placeholder"] = "Enter maximum number of attendees"
                        }
                    }

                    // Image upload with improved styling
                    div(classes = "mb-6") {
                        label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                            attributes["for"] = "image"
                            +"Event Image"
                        }
                        div(classes = "mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-blue-500 transition-colors duration-300") {
                            div(classes = "space-y-2 text-center") {
                                div(classes = "flex text-sm text-gray-600 justify-center items-center") {
                                    label(classes = "relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500") {
                                        input(classes = "sr-only") {
                                            attributes["type"] = "file"
                                            attributes["name"] = "image"
                                            attributes["id"] = "image"
                                            attributes["accept"] = "image/*"
                                        }
                                        div(classes = "flex items-center") {
                                            span { +"Upload a file" }
                                            span(classes = "pl-1") { +" or drag and drop" }
                                        }
                                    }
                                }
                                p(classes = "text-xs text-gray-500") {
                                    id = "image-upload-text"
                                    +"PNG, JPG, GIF up to 10MB"
                                }
                                p(classes = "text-sm text-green-600 mt-2") {
                                    id = "upload-status"
                                    +""
                                }
                            }
                        }
                        
                        // Image preview and cropper container
                        div(classes = "mt-4 hidden") {
                            id = "image-cropper-container"
                            div(classes = "max-w-2xl mx-auto") {
                                img(classes = "max-w-full") {
                                    id = "image-preview"
                                    alt = "Event image preview"
                                }
                            }
                        }
                    }

                    // Approval requirement checkbox
                    div(classes = "mb-6") {
                        div(classes = "flex items-center") {
                            input(classes = "h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded") {
                                attributes["type"] = "checkbox"
                                attributes["name"] = "needsApproval"
                                attributes["id"] = "needsApproval"
                            }
                            label(classes = "ml-2 block text-sm text-gray-700") {
                                attributes["for"] = "needsApproval"
                                +"Require approval for attendees"
                            }
                        }
                        p(classes = "mt-1 text-sm text-gray-500") {
                            +"When enabled, attendees will need your approval to join the event"
                        }
                    }

                    // Action buttons with consistent styling
                    div(classes = "flex items-center justify-between gap-4") {
                        button(classes = "bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg shadow-md transition-colors duration-300") {
                            attributes["type"] = "button"
                            attributes["hx-get"] = "/events/upcoming"
                            attributes["hx-target"] = "#main-content"
                            +"Cancel"
                        }
                        button(classes = "bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg shadow-md transition-colors duration-300") {
                            attributes["type"] = "button"
                            attributes["id"] = "submit-btn"
                            +"Create Event"
                        }
                    }
                }
            }
            // load scripts
            loadJs("create-event")
        }
    }
}