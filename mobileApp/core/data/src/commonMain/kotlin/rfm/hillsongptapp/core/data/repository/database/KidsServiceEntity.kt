package rfm.hillsongptapp.core.data.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Room entity representing a kids service in the database
 */
@Entity(
    tableName = "kids_services",
    indices = [
        Index(value = ["name"]),
        Index(value = ["startTime"]),
        Index(value = ["endTime"]),
        Index(value = ["isAcceptingCheckIns"])
    ]
)
@TypeConverters(KidsServiceTypeConverters::class)
data class KidsServiceEntity(
    @PrimaryKey val id: String,
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
    val staffMembers: List<String>,
    val createdAt: String // ISO 8601 format
)

/**
 * Type converters for complex types in KidsServiceEntity
 */
class KidsServiceTypeConverters {
    
    @TypeConverter
    fun fromStringList(staffMembers: List<String>): String {
        return Json.encodeToString(staffMembers)
    }
    
    @TypeConverter
    fun toStringList(staffMembersJson: String): List<String> {
        return Json.decodeFromString(staffMembersJson)
    }
}

/**
 * Data Access Object for KidsService entities
 */
@Dao
interface KidsServiceDao {
    
    @Upsert
    suspend fun insertKidsService(service: KidsServiceEntity)
    
    @Upsert
    suspend fun insertKidsServices(services: List<KidsServiceEntity>)
    
    @Query("SELECT * FROM kids_services WHERE id = :serviceId")
    suspend fun getKidsServiceById(serviceId: String): KidsServiceEntity?
    
    @Query("SELECT * FROM kids_services ORDER BY startTime ASC")
    suspend fun getAllKidsServices(): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE isAcceptingCheckIns = 1 ORDER BY startTime ASC")
    suspend fun getActiveKidsServices(): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchKidsServicesByName(searchQuery: String): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE minAge <= :age AND maxAge >= :age ORDER BY startTime ASC")
    suspend fun getKidsServicesByAgeRange(age: Int): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE currentCapacity < maxCapacity ORDER BY startTime ASC")
    suspend fun getKidsServicesWithAvailableSpots(): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE isAcceptingCheckIns = 1 AND currentCapacity < maxCapacity ORDER BY startTime ASC")
    suspend fun getAvailableKidsServicesForCheckIn(): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime ASC")
    suspend fun getKidsServicesByTimeRange(startTime: String, endTime: String): List<KidsServiceEntity>
    
    @Query("SELECT * FROM kids_services WHERE location = :location ORDER BY startTime ASC")
    suspend fun getKidsServicesByLocation(location: String): List<KidsServiceEntity>
    
    @Query("SELECT COUNT(*) FROM kids_services WHERE isAcceptingCheckIns = 1")
    suspend fun getActiveKidsServicesCount(): Int
    
    @Query("SELECT SUM(currentCapacity) FROM kids_services")
    suspend fun getTotalCurrentCapacity(): Int?
    
    @Query("SELECT SUM(maxCapacity) FROM kids_services")
    suspend fun getTotalMaxCapacity(): Int?
    
    @Update
    suspend fun updateKidsService(service: KidsServiceEntity)
    
    @Query("UPDATE kids_services SET currentCapacity = :newCapacity WHERE id = :serviceId")
    suspend fun updateServiceCapacity(serviceId: String, newCapacity: Int)
    
    @Query("UPDATE kids_services SET isAcceptingCheckIns = :isAccepting WHERE id = :serviceId")
    suspend fun updateServiceCheckInStatus(serviceId: String, isAccepting: Boolean)
    
    @Delete
    suspend fun deleteKidsService(service: KidsServiceEntity)
    
    @Query("DELETE FROM kids_services WHERE id = :serviceId")
    suspend fun deleteKidsServiceById(serviceId: String)
    
    @Query("DELETE FROM kids_services")
    suspend fun deleteAllKidsServices()
}