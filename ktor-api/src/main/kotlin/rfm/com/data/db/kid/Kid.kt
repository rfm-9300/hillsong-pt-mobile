
package rfm.com.data.db.kid

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.date
import rfm.com.data.db.user.UserTable

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

object KidTable : IntIdTable("kid") {
    val familyId = reference("family_id", UserTable)
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val dateOfBirth = date("date_of_birth")
    val allergies = text("allergies").nullable()
    val notes = text("notes").nullable()
}

fun ResultRow.toKid() = Kid(
    id = this[KidTable.id].value,
    familyId = this[KidTable.familyId].value,
    firstName = this[KidTable.firstName],
    lastName = this[KidTable.lastName],
    dateOfBirth = this[KidTable.dateOfBirth].toString(),
    allergies = this[KidTable.allergies],
    notes = this[KidTable.notes]
)
