package example.com.data.db.image

import example.com.data.db.user.suspendTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

interface ImageHashRepository {
    suspend fun findByHash(hash: Int): ImageHash?
    suspend fun save(imagePath: String, hash: Int): ImageHash
    suspend fun delete(imagePath: String): Int
}

class ImageHashRepositoryImpl : ImageHashRepository {
    override suspend fun findByHash(hash: Int): ImageHash? = suspendTransaction {
        ImageHashTable.select { ImageHashTable.hash eq hash }
            .map { it.toImageHash() }
            .firstOrNull()
    }

    override suspend fun save(imagePath: String, hash: Int): ImageHash = suspendTransaction {
        val id = ImageHashTable.insert {
            it[ImageHashTable.imagePath] = imagePath
            it[ImageHashTable.hash] = hash
        }[ImageHashTable.id].value

        ImageHash(id, imagePath, hash)
    }

    override suspend fun delete(imagePath: String): Int = suspendTransaction {
        ImageHashTable.deleteWhere { ImageHashTable.imagePath eq imagePath }
    }
} 