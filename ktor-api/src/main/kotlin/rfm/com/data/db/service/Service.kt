package rfm.com.data.db.service

import rfm.com.data.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

@Serializable
data class Service(
    val id: Int? = null,
    val name: String,
    val description: String = "",
    @Serializable(with = LocalDateTimeSerializer::class)
    val startTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endTime: LocalDateTime,
    val location: String,
    val serviceType: ServiceType = ServiceType.REGULAR,
    val isActive: Boolean = true,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ServiceType {
    REGULAR,
    SPECIAL,
    YOUTH,
    KIDS,
    PRAYER,
    WORSHIP
}

object ServiceTable : IntIdTable("service") {
    val name = varchar("name", 255)
    val description = text("description")
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val location = varchar("location", 255)
    val serviceType = varchar("service_type", 50).default(ServiceType.REGULAR.name)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

fun ResultRow.toService() = Service(
    id = this[ServiceTable.id].value,
    name = this[ServiceTable.name],
    description = this[ServiceTable.description],
    startTime = this[ServiceTable.startTime],
    endTime = this[ServiceTable.endTime],
    location = this[ServiceTable.location],
    serviceType = ServiceType.valueOf(this[ServiceTable.serviceType]),
    isActive = this[ServiceTable.isActive],
    createdAt = this[ServiceTable.createdAt]
)