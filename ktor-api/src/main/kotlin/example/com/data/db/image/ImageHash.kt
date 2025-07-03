package example.com.data.db.image

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow

data class ImageHash(
    val id: Int? = null,
    val imagePath: String,
    val hash: Int
)

object ImageHashTable : IntIdTable("image_hash") {
    val imagePath = varchar("image_path", 255)
    val hash = integer("hash")
}

fun ResultRow.toImageHash() = ImageHash(
    id = this[ImageHashTable.id].value,
    imagePath = this[ImageHashTable.imagePath],
    hash = this[ImageHashTable.hash]
) 