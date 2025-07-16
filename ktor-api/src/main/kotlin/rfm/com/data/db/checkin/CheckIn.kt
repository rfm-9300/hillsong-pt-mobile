
package rfm.com.data.db.checkin

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import rfm.com.data.db.kid.KidsTable
import java.time.LocalDateTime

@Serializable
data class CheckIn(
    val id: Int? = null,
    val childId: Int,
    val checkInTime: String,
    val checkOutTime: String? = null,
    val serviceId: Int,
    val guardianCode: String,
    val status: String
)

object CheckInsTable : IntIdTable("check_in") {
    val childId = reference("child_id", KidsTable)
    val checkInTime = datetime("check_in_time").default(LocalDateTime.now())
    val checkOutTime = datetime("check_out_time").nullable()
    val serviceId = integer("service_id")
    val guardianCode = varchar("guardian_code", 64)
    val status = varchar("status", 16)
}

fun ResultRow.toCheckIn() = CheckIn(
    id = this[CheckInsTable.id].value,
    childId = this[CheckInsTable.childId].value,
    checkInTime = this[CheckInsTable.checkInTime].toString(),
    checkOutTime = this[CheckInsTable.checkOutTime]?.toString(),
    serviceId = this[CheckInsTable.serviceId],
    guardianCode = this[CheckInsTable.guardianCode],
    status = this[CheckInsTable.status]
)
