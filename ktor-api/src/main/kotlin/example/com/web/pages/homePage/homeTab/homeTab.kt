package example.com.web.pages.homePage.homeTab

import example.com.data.db.post.Post
import example.com.data.db.post.PostRepositoryImpl
import example.com.data.utils.dayMonthTime
import example.com.routes.Routes
import example.com.web.components.layout.layout
import example.com.web.components.post.post
import example.com.web.models.PostUi
import kotlinx.coroutines.*
import kotlinx.html.*

fun HtmlBlockTag.homeTab(isAdminRequest: Boolean = false) {
    // Run blocking to get posts synchronously
    val posts = runBlocking {
        try {
            PostRepositoryImpl().getAllPosts().map { post ->
                PostUi(
                    postId = post.id!!,
                    title = post.title,
                    content = post.content,
                    likes = post.likes,
                    date = post.date.dayMonthTime()
                )
            }
        } catch (e: Exception) {
            println("Error fetching posts: ${e.message}")
            emptyList()
        }
    }
    div {
        //banner div
        div(classes = "flex flex-row items-center") {
            img(classes = "w-[1000px] h-[300px] rounded-2xl") {
                src = "/resources/images/banner.jpeg"
            }
        }

        // Container for the "Create Post" button
        if (isAdminRequest){
            div(classes = "w-full py-4") {
                // Add a button to create a new post
                div(classes = "flex justify-end mb-4") {
                    button(classes = "bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700") {
                        attributes["hx-get"] = Routes.Ui.Home.CREATE_POST
                        attributes["hx-target"] = "#main-content"
                        +"Create Post"
                    }
                }
            }
        }

        div(classes = "w-[70%] py-4") {
            posts.forEach { post ->
                post(post)
            }
            script(src = "/resources/js/post.js") {}
        }
    }


}


