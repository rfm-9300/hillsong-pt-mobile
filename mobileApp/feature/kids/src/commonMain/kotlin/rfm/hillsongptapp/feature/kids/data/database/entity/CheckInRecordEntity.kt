package rfm.hillsongptapp.feature.kids.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus

/**
 * Room entity representing a check-in record in the local database
 */
@Entity(
    tableName = "checkin_records",
    foreignKeys = [
        ForeignKey(
            entity = ChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = KidsServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["childId"]),
        Index(value = ["serviceId"]),
        Index(value = ["checkInTime"]),
        Index(value = ["checkOutTime"]),
        Index(value = ["status"]),
        Index(value = ["checkedInBy"]),
        Index(value = ["checkedOutBy"])
    ]
)
data class CheckInRecordEntity(
    @PrimaryKey 
    val id: String,
    val childId: String,
    val serviceId: String,
    val checkInTime: String, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format, null if still checked in
    val checkedInBy: String, // User ID of the person who checked in the child
    val checkedOutBy: String? = null, // User ID of the person who checked out the child
    val notes: String? = null,
    val status: String, // CheckInStatus enum as string
    val lastSyncedAt: String? = null // For offline sync tracking
)

/**
 * Extension function to convert CheckInRecordEntity to domain CheckInRecord model
 */
fun CheckInRecordEntity.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = CheckInStatus.valueOf(status)
    )
}

/**
 * Extension function to convert domain CheckInRecord model to CheckInRecordEntity
 */
fun CheckInRecord.toEntity(lastSyncedAt: String? = null): CheckInRecordEntity {
    return CheckInRecordEntity(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status.name,
        lastSyncedAt = lastSyncedAt
    )
}