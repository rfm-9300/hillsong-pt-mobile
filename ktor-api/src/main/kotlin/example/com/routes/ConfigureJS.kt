package example.com.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.io.files.FileNotFoundException
import java.io.File

fun Route.dynamicJsProcessing() {
    get("/js/{filename}") {
        val directory = "app/files/js/dynamic"
        try {
            // Get the filename from the request
            val filename = call.parameters["filename"] ?: throw IllegalArgumentException("Filename not provided")
            val files = File(directory).listFiles()?.toList()?.filter { it.name.endsWith(".js") } ?: emptyList()
            val jsFile = files.find { it.name == filename.lowercase() } ?: throw FileNotFoundException("File not found: $filename")

            // Read the JS file from your resources directory
            val jsContent = jsFile.readText()

            // Replace placeholders with actual values
            val processedContent = jsContent.replace(
                Routes.Placeholder.PLACEHOLDERS
            )

            call.respondText(
                contentType = ContentType.Application.JavaScript,
                text = processedContent
            )
        } catch (e: FileNotFoundException) {
            call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}


// Extension function to handle replacements
private fun String.replace(replacements: Map<String, String>): String {
    var result = this
    replacements.forEach { (placeholder, value) ->
        result = result.replace(placeholder, value)
    }
    return result
}