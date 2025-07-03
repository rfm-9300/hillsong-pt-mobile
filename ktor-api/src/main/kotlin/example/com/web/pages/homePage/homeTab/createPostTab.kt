package example.com.web.pages.homePage.homeTab

import kotlinx.html.*

fun HTML.createPostTab(){
    body {
        div(classes = "flex flex-col justify-center items-center w-3/4 w-full") {
            div(classes = "flex justify-between items-center mb-8") {
                div {
                    h1(classes = "text-3xl font-bold") { +"Create New Post" }
                }
            }

                // Title
                div(classes = "mb-4 w-full") {
                    label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                        attributes["for"] = "title"
                        +"Post Title"
                    }
                    input(classes = "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700") {
                        attributes["type"] = "text"
                        attributes["name"] = "title"
                        attributes["id"] = "post-title"
                        attributes["required"] = "true"
                    }
                }

                // Content
                div(classes = "mb-4 w-full") {
                    label(classes = "block text-gray-700 text-sm font-bold mb-2") {
                        attributes["for"] = "content"
                        +"Content"
                    }
                    textArea(classes = "shadow appearance-none border rounded w-full py-2 px-3 text-gray-700") {
                        attributes["name"] = "content"
                        attributes["id"] = "post-content"
                        attributes["rows"] = "4"
                    }
                }


                // Submit button
                div(classes = "flex items-center justify-between") {
                    button(classes = "bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700") {
                        onClick = "submitPost()"
                        +"Create Post"
                    }
                }
            }
        }
    }
