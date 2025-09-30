package rfm.hillsongptapp.core.data.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.EmergencyContact

/**
 * Room entity representing a child in the database
 */
@Entity(tableName = "children")
@TypeConverters(ChildTypeConverters::class)
data class ChildEntity(
    @PrimaryKey val id: String,
    val parentId: String,
    val name: String,
    val dateOfBirth: String, // ISO 8601 format (YYYY-MM-DD)
    val medicalInfo: String? = null,
    val dietaryRestrictions: String? = null,
    val emergencyContact: EmergencyContact,
    val status: CheckInStatus,
    val currentServiceId: String? = null,
    val checkInTime: String? = null, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format
    val createdAt: String, // ISO 8601 format
    val updatedAt: String // ISO 8601 format
)

/**
 * Type converters for complex types in ChildEntity
 */
class ChildTypeConverters {
    
    @TypeConverter
    fun fromEmergencyContact(contact: EmergencyContact): String {
        return Json.encodeToString(contact)
    }
    
    @TypeConverter
    fun toEmergencyContact(contactJson: String): EmergencyContact {
        return Json.decodeFromString(contactJson)
    }
    
    @TypeConverter
    fun fromCheckInStatus(status: CheckInStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toCheckInStatus(statusName: String): CheckInStatus {
        return CheckInStatus.valueOf(statusName)
    }
}

/**
 * Data Access Object for Child entities
 */
@Dao
interface ChildDao {
    
    @Upsert
    suspend fun insertChild(child: ChildEntity)
    
    @Upsert
    suspend fun insertChildren(children: List<ChildEntity>)
    
    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): ChildEntity?
    
    @Query("SELECT * FROM children WHERE parentId = :parentId ORDER BY name ASC")
    suspend fun getChildrenByParentId(parentId: String): List<ChildEntity>
    
    @Query("SELECT * FROM children WHERE status = :status ORDER BY name ASC")
    suspend fun getChildrenByStatus(status: CheckInStatus): List<ChildEntity>
    
    @Query("SELECT * FROM children WHERE currentServiceId = :serviceId ORDER BY name ASC")
    suspend fun getChildrenByServiceId(serviceId: String): List<ChildEntity>
    
    @Query("SELECT * FROM children ORDER BY name ASC")
    suspend fun getAllChildren(): List<ChildEntity>
    
    @Query("SELECT * FROM children WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchChildrenByName(searchQuery: String): List<ChildEntity>
    
    @Query("SELECT COUNT(*) FROM children WHERE currentServiceId = :serviceId AND status = :status")
    suspend fun getChildrenCountByServiceAndStatus(serviceId: String, status: CheckInStatus): Int
    
    @Query("SELECT COUNT(*) FROM children WHERE status = :status")
    suspend fun getChildrenCountByStatus(status: CheckInStatus): Int
    
    @Update
    suspend fun updateChild(child: ChildEntity)
    
    @Delete
    suspend fun deleteChild(child: ChildEntity)
    
    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChildById(childId: String)
    
    @Query("DELETE FROM children")
    suspend fun deleteAllChildren()
}