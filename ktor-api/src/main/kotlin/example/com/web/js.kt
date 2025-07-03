package example.com.web

import example.com.web.utils.Strings
import kotlinx.html.HEAD
import kotlinx.html.HtmlBlockTag
import kotlinx.html.script
import java.io.File

fun HtmlBlockTag.loadJs(folderName: String = "") {
    val directory = "${Strings.RESOURCES_DIR}/js/$folderName"

    // get all files in the directory
    val files = File(directory).listFiles()?.toList()?.filter { it.name.endsWith(".js") } ?: emptyList()
    val jsFileNames = files.map { it.name }

    // return script tags for each file
    jsFileNames.forEach { fileName ->
        script(src = "/resources/js/$folderName/$fileName") {}
    }
}

fun HEAD.loadHeaderScripts(folderName: String = "main") {
    val directory = "${Strings.RESOURCES_DIR}/js/$folderName"

    // get all files in the directory
    val files = File(directory).listFiles()?.toList()?.filter { it.name.endsWith(".js") } ?: emptyList()
    val jsFileNames = files.map { it.name }

    // return script tags for each file
    jsFileNames.forEach { fileName ->
        script(src = "/resources/js/$folderName/$fileName") {}
    }
}