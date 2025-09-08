package rfm.hillsongptapp.feature.kids.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rfm.hillsongptapp.feature.kids.domain.model.KidsService

/**
 * Room entity representing a kids service in the local database
 */
@Entity(
    tableName = "kids_services",
    indices = [
        Index(value = ["minAge", "maxAge"]),
        Index(value = ["isAcceptingCheckIns"]),
        Index(value = ["currentCapacity", "maxCapacity"]),
        Index(value = ["startTime", "endTime"])
    ]
)
data class KidsServiceEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    val description: String,
    val minAge: Int,
    val maxAge: Int,
    val startTime: String, // ISO 8601 format
    val endTime: String, // ISO 8601 format
    val location: String,
    val maxCapacity: Int,
    val currentCapacity: Int,
    val isAcceptingCheckIns: Boolean,
    val staffMembers: String, // JSON serialized list of staff member IDs
    val createdAt: String, // ISO 8601 format
    val lastSyncedAt: String? = null // For offline sync tracking
)

/**
 * Extension function to convert KidsServiceEntity to domain KidsService model
 */
fun KidsServiceEntity.toDomain(): KidsService {
    val staffMembersList = try {
        Json.decodeFromString<List<String>>(staffMembers)
    } catch (e: Exception) {
        emptyList()
    }
    
    return KidsService(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembersList,
        createdAt = createdAt
    )
}

/**
 * Extension function to convert domain KidsService model to KidsServiceEntity
 */
fun KidsService.toEntity(lastSyncedAt: String? = null): KidsServiceEntity {
    val staffMembersJson = try {
        Json.encodeToString(staffMembers)
    } catch (e: Exception) {
        "[]"
    }
    
    return KidsServiceEntity(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembersJson,
        createdAt = createdAt,
        lastSyncedAt = lastSyncedAt
    )
}