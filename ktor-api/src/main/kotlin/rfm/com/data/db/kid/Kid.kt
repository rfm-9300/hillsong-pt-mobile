
package rfm.com.data.db.kid

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import rfm.com.data.db.user.UserTable
import java.time.LocalDate

@Serializable
data class Kid(
    val id: Int? = null,
    val familyId: Int,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val allergies: String? = null,
    val notes: String? = null
)

object KidsTable : IntIdTable("kid") {
    val familyId = reference("family_id", UserTable)
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val dateOfBirth = date("date_of_birth")
    val allergies = text("allergies").nullable()
    val notes = text("notes").nullable()
}

fun ResultRow.toKid() = Kid(
    id = this[KidsTable.id].value,
    familyId = this[KidsTable.familyId].value,
    firstName = this[KidsTable.firstName],
    lastName = this[KidsTable.lastName],
    dateOfBirth = this[KidsTable.dateOfBirth].toString(),
    allergies = this[KidsTable.allergies],
    notes = this[KidsTable.notes]
)
