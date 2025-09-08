package rfm.hillsongptapp.feature.kids.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import rfm.hillsongptapp.feature.kids.domain.model.CheckInStatus
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.EmergencyContact

/**
 * Room entity representing a child in the local database
 */
@Entity(
    tableName = "children",
    indices = [
        Index(value = ["parentId"]),
        Index(value = ["status"]),
        Index(value = ["currentServiceId"]),
        Index(value = ["dateOfBirth"])
    ]
)
data class ChildEntity(
    @PrimaryKey 
    val id: String,
    val parentId: String,
    val name: String,
    val dateOfBirth: String, // ISO 8601 format (YYYY-MM-DD)
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val emergencyContactRelationship: String,
    val status: String, // CheckInStatus enum as string
    val currentServiceId: String? = null,
    val checkInTime: String? = null, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format
    val createdAt: String, // ISO 8601 format
    val updatedAt: String, // ISO 8601 format
    val lastSyncedAt: String? = null // For offline sync tracking
)

/**
 * Extension function to convert ChildEntity to domain Child model
 */
fun ChildEntity.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = EmergencyContact(
            name = emergencyContactName,
            phoneNumber = emergencyContactPhone,
            relationship = emergencyContactRelationship
        ),
        status = CheckInStatus.valueOf(status),
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Extension function to convert domain Child model to ChildEntity
 */
fun Child.toEntity(lastSyncedAt: String? = null): ChildEntity {
    return ChildEntity(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContactName = emergencyContact.name,
        emergencyContactPhone = emergencyContact.phoneNumber,
        emergencyContactRelationship = emergencyContact.relationship,
        status = status.name,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt
    )
}