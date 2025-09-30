package rfm.hillsongptapp.core.data.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import rfm.hillsongptapp.core.data.model.CheckInStatus

/**
 * Room entity representing a check-in record in the database
 */
@Entity(
    tableName = "check_in_records",
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
        Index(value = ["status"])
    ]
)
@TypeConverters(CheckInRecordTypeConverters::class)
data class CheckInRecordEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val serviceId: String,
    val checkInTime: String, // ISO 8601 format
    val checkOutTime: String? = null, // ISO 8601 format, null if still checked in
    val checkedInBy: String, // User ID of the person who checked in the child
    val checkedOutBy: String? = null, // User ID of the person who checked out the child
    val notes: String? = null,
    val status: CheckInStatus
)

/**
 * Type converters for complex types in CheckInRecordEntity
 */
class CheckInRecordTypeConverters {
    
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
 * Data Access Object for CheckInRecord entities
 */
@Dao
interface CheckInRecordDao {
    
    @Upsert
    suspend fun insertCheckInRecord(record: CheckInRecordEntity)
    
    @Upsert
    suspend fun insertCheckInRecords(records: List<CheckInRecordEntity>)
    
    @Query("SELECT * FROM check_in_records WHERE id = :recordId")
    suspend fun getCheckInRecordById(recordId: String): CheckInRecordEntity?
    
    @Query("SELECT * FROM check_in_records WHERE childId = :childId ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByChildId(childId: String): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE serviceId = :serviceId ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByServiceId(serviceId: String): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE status = :status ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByStatus(status: CheckInStatus): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE childId = :childId AND status = :status ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getCurrentCheckInRecordForChild(childId: String, status: CheckInStatus = CheckInStatus.CHECKED_IN): CheckInRecordEntity?
    
    @Query("SELECT * FROM check_in_records WHERE serviceId = :serviceId AND status = :status ORDER BY checkInTime DESC")
    suspend fun getActiveCheckInRecordsForService(serviceId: String, status: CheckInStatus = CheckInStatus.CHECKED_IN): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE checkInTime >= :startDate AND checkInTime <= :endDate ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByDateRange(startDate: String, endDate: String): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE childId = :childId AND checkInTime >= :startDate AND checkInTime <= :endDate ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByChildAndDateRange(childId: String, startDate: String, endDate: String): List<CheckInRecordEntity>
    
    @Query("SELECT * FROM check_in_records WHERE serviceId = :serviceId AND checkInTime >= :startDate AND checkInTime <= :endDate ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByServiceAndDateRange(serviceId: String, startDate: String, endDate: String): List<CheckInRecordEntity>
    
    @Query("SELECT COUNT(*) FROM check_in_records WHERE serviceId = :serviceId AND status = :status")
    suspend fun getActiveCheckInCountForService(serviceId: String, status: CheckInStatus = CheckInStatus.CHECKED_IN): Int
    
    @Query("SELECT COUNT(*) FROM check_in_records WHERE checkInTime >= :startDate AND checkInTime <= :endDate")
    suspend fun getCheckInCountByDateRange(startDate: String, endDate: String): Int
    
    @Query("SELECT * FROM check_in_records ORDER BY checkInTime DESC")
    suspend fun getAllCheckInRecords(): List<CheckInRecordEntity>
    
    @Update
    suspend fun updateCheckInRecord(record: CheckInRecordEntity)
    
    @Delete
    suspend fun deleteCheckInRecord(record: CheckInRecordEntity)
    
    @Query("DELETE FROM check_in_records WHERE id = :recordId")
    suspend fun deleteCheckInRecordById(recordId: String)
    
    @Query("DELETE FROM check_in_records WHERE childId = :childId")
    suspend fun deleteCheckInRecordsByChildId(childId: String)
    
    @Query("DELETE FROM check_in_records WHERE serviceId = :serviceId")
    suspend fun deleteCheckInRecordsByServiceId(serviceId: String)
    
    @Query("DELETE FROM check_in_records")
    suspend fun deleteAllCheckInRecords()
}