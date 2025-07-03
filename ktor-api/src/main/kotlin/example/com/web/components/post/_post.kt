package example.com.web.components.post

import example.com.web.components.SvgIcon
import example.com.web.components.svgIcon
import example.com.web.models.PostUi
import kotlinx.html.*

fun HtmlBlockTag.post(post: PostUi) {
    div(classes = "flex flex-col rounded-2xl bg-white shadow-lg p-4 my-4") {

        // user div
        div(classes = "flex items-center gap-2") {
            div(classes = "w-8 h-8 rounded-full overflow-hidden cursor-pointer") {
                img(classes = "object-cover w-full h-full", src = post.imgUrl, alt = "Active Hive Logo")
            }
            div(classes = "flex flex-col") {
                span(classes = "font-bold") {
                    +post.userName
                }
                span(classes = "text-xs") {
                    +"${post.date.day} ${post.date.month}, ${post.date.time}"
                }
            }
        }

        // post content div
        div(classes = "flex flex-col my-4") {
            p(classes = "text-lg") {
                +post.title
            }
            p(classes = "text-sm") {
                +post.content
            }
        }

        // post icons div
        div(classes = "flex justify-between") {
            div(classes = "flex gap-2") {
                div(classes = "w-6 h-6 rounded-full overflow-hidden cursor-pointer like-icon") {
                    attributes["data-post-id"] = post.postId.toString()
                    svgIcon(SvgIcon.LIKE)
                }
                +post.likes.toString()
            }
            span(classes = "delete-icon rounded-full overflow-hidden cursor-pointer"){
                attributes["onclick"] = "deletePost(${post.postId})"
                svgIcon(SvgIcon.DELETE)
            }
        }
    }
}