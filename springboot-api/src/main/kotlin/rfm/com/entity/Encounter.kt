package rfm.com.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "encounters")
data class Encounter(
    @Id
    val id: String? = null,

    val title: String,

    val description: String,

    val date: LocalDateTime,

    val location: String,

    val organizerId: String,

    val imagePath: String? = null,

    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Encounter
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String = "Encounter(id=$id, title='$title', date=$date, location='$location')"
}
