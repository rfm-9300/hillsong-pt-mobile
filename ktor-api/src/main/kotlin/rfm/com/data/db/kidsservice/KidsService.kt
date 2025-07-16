package rfm.com.data.db.kidsservice


import rfm.com.data.db.service.ServiceTable
import rfm.com.data.db.user.UserTable
import rfm.com.data.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import rfm.com.data.db.kid.KidTable
import java.time.LocalDateTime

@Serializable
data class KidsService(
    val id: Int? = null,
    val serviceId: Int,
    val name: String,
    val description: String = "",
    val ageGroupMin: Int,
    val ageGroupMax: Int,
    val maxCapacity: Int,
    val location: String,
    val isActive: Boolean = true,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class KidsCheckIn(
    val id: Int? = null,
    val kidsServiceId: Int,
    val kidId: Int,
    val checkedInBy: Int, // User ID of the person who checked in the kid
    @Serializable(with = LocalDateTimeSerializer::class)
    val checkInTime: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val checkOutTime: LocalDateTime? = null,
    val checkedOutBy: Int? = null, // User ID of the person who checked out the kid
    val notes: String = "",
    val status: CheckInStatus = CheckInStatus.CHECKED_IN
)

enum class CheckInStatus {
    CHECKED_IN,
    CHECKED_OUT,
    EMERGENCY
}

object KidsServiceTable : IntIdTable("kids_service") {
    val serviceId = reference("service_id", ServiceTable)
    val name = varchar("name", 255)
    val description = text("description")
    val ageGroupMin = integer("age_group_min")
    val ageGroupMax = integer("age_group_max")
    val maxCapacity = integer("max_capacity")
    val location = varchar("location", 255)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

object KidsCheckInTable : IntIdTable("kids_check_in") {
    val kidsServiceId = reference("kids_service_id", KidsServiceTable)
    val kidId = reference("kid_id", KidTable)
    val checkedInBy = reference("checked_in_by", UserTable)
    val checkInTime = datetime("check_in_time").default(LocalDateTime.now())
    val checkOutTime = datetime("check_out_time").nullable()
    val checkedOutBy = reference("checked_out_by", UserTable).nullable()
    val notes = text("notes")
    val status = varchar("status", 20).default(CheckInStatus.CHECKED_IN.name)
}

fun ResultRow.toKidsService() = KidsService(
    id = this[KidsServiceTable.id].value,
    serviceId = this[KidsServiceTable.serviceId].value,
    name = this[KidsServiceTable.name],
    description = this[KidsServiceTable.description],
    ageGroupMin = this[KidsServiceTable.ageGroupMin],
    ageGroupMax = this[KidsServiceTable.ageGroupMax],
    maxCapacity = this[KidsServiceTable.maxCapacity],
    location = this[KidsServiceTable.location],
    isActive = this[KidsServiceTable.isActive],
    createdAt = this[KidsServiceTable.createdAt]
)

fun ResultRow.toKidsCheckIn() = KidsCheckIn(
    id = this[KidsCheckInTable.id].value,
    kidsServiceId = this[KidsCheckInTable.kidsServiceId].value,
    kidId = this[KidsCheckInTable.kidId].value,
    checkedInBy = this[KidsCheckInTable.checkedInBy].value,
    checkInTime = this[KidsCheckInTable.checkInTime],
    checkOutTime = this[KidsCheckInTable.checkOutTime],
    checkedOutBy = this[KidsCheckInTable.checkedOutBy]?.value,
    notes = this[KidsCheckInTable.notes],
    status = CheckInStatus.valueOf(this[KidsCheckInTable.status])
)