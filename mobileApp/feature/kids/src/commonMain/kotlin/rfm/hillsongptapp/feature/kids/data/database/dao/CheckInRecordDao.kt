package rfm.hillsongptapp.feature.kids.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity

/**
 * Data Access Object for CheckInRecord entities
 */
@Dao
interface CheckInRecordDao {
    
    /**
     * Get all check-in records
     */
    @Query("SELECT * FROM checkin_records ORDER BY checkInTime DESC")
    suspend fun getAllCheckInRecords(): List<CheckInRecordEntity>
    
    /**
     * Get all check-in records as Flow for reactive updates
     */
    @Query("SELECT * FROM checkin_records ORDER BY checkInTime DESC")
    fun getAllCheckInRecordsFlow(): Flow<List<CheckInRecordEntity>>
    
    /**
     * Get a specific check-in record by ID
     */
    @Query("SELECT * FROM checkin_records WHERE id = :recordId")
    suspend fun getCheckInRecordById(recordId: String): CheckInRecordEntity?
    
    /**
     * Get check-in history for a specific child
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId ORDER BY checkInTime DESC")
    suspend fun getCheckInHistory(childId: String): List<CheckInRecordEntity>
    
    /**
     * Get check-in history for a specific child with limit
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId ORDER BY checkInTime DESC LIMIT :limit")
    suspend fun getCheckInHistoryWithLimit(childId: String, limit: Int): List<CheckInRecordEntity>
    
    /**
     * Get check-in history for a specific child
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId ORDER BY checkInTime DESC")
    suspend fun getCheckInHistoryForChild(childId: String): List<CheckInRecordEntity>
    
    /**
     * Get check-in history for a specific child as Flow
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId ORDER BY checkInTime DESC")
    fun getCheckInHistoryForChildFlow(childId: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * Get all check-in records for a specific service
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsForService(serviceId: String): List<CheckInRecordEntity>
    
    /**
     * Get all check-in records for a specific service as Flow
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId ORDER BY checkInTime DESC")
    fun getCheckInRecordsForServiceFlow(serviceId: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * Get current check-in records (children still checked in)
     */
    @Query("SELECT * FROM checkin_records WHERE status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getCurrentCheckInRecords(): List<CheckInRecordEntity>
    
    /**
     * Get all current check-ins across all services
     */
    @Query("SELECT * FROM checkin_records WHERE status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getAllCurrentCheckIns(): List<CheckInRecordEntity>
    
    /**
     * Get current check-ins for a specific service
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId AND status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getCurrentCheckIns(serviceId: String): List<CheckInRecordEntity>
    
    /**
     * Get current check-in records as Flow
     */
    @Query("SELECT * FROM checkin_records WHERE status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    fun getCurrentCheckInRecordsFlow(): Flow<List<CheckInRecordEntity>>
    
    /**
     * Get current check-in records for a specific service
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId AND status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getCurrentCheckInsForService(serviceId: String): List<CheckInRecordEntity>
    
    /**
     * Get current check-in records for a specific service
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId AND status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getCurrentCheckInRecordsForService(serviceId: String): List<CheckInRecordEntity>
    
    /**
     * Get current check-in records for a specific service as Flow
     */
    @Query("SELECT * FROM checkin_records WHERE serviceId = :serviceId AND status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    fun getCurrentCheckInRecordsForServiceFlow(serviceId: String): Flow<List<CheckInRecordEntity>>
    
    /**
     * Get the current active check-in record for a child (if any)
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId AND status = 'CHECKED_IN' LIMIT 1")
    suspend fun getCurrentCheckInForChild(childId: String): CheckInRecordEntity?
    
    /**
     * Get completed check-in records (children who have been checked out)
     */
    @Query("SELECT * FROM checkin_records WHERE status = 'CHECKED_OUT' ORDER BY checkOutTime DESC")
    suspend fun getCompletedCheckInRecords(): List<CheckInRecordEntity>
    
    /**
     * Get check-in records by status
     */
    @Query("SELECT * FROM checkin_records WHERE status = :status ORDER BY checkInTime DESC")
    suspend fun getCheckInRecordsByStatus(status: String): List<CheckInRecordEntity>
    
    /**
     * Get check-in records by date range
     */
    @Query("""
        SELECT * FROM checkin_records 
        WHERE checkInTime >= :startDate AND checkInTime <= :endDate 
        ORDER BY checkInTime DESC
    """)
    suspend fun getCheckInRecordsByDateRange(startDate: String, endDate: String): List<CheckInRecordEntity>
    
    /**
     * Get check-in records for a specific child by date range
     */
    @Query("""
        SELECT * FROM checkin_records 
        WHERE childId = :childId AND checkInTime >= :startDate AND checkInTime <= :endDate 
        ORDER BY checkInTime DESC
    """)
    suspend fun getCheckInRecordsForChildByDateRange(
        childId: String, 
        startDate: String, 
        endDate: String
    ): List<CheckInRecordEntity>
    
    /**
     * Get check-in records for a specific service by date range
     */
    @Query("""
        SELECT * FROM checkin_records 
        WHERE serviceId = :serviceId AND checkInTime >= :startDate AND checkInTime <= :endDate 
        ORDER BY checkInTime DESC
    """)
    suspend fun getCheckInRecordsForServiceByDateRange(
        serviceId: String, 
        startDate: String, 
        endDate: String
    ): List<CheckInRecordEntity>
    
    /**
     * Insert a new check-in record
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckInRecord(record: CheckInRecordEntity): Long
    
    /**
     * Insert multiple check-in records
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckInRecords(records: List<CheckInRecordEntity>)
    
    /**
     * Update an existing check-in record
     */
    @Update
    suspend fun updateCheckInRecord(record: CheckInRecordEntity)
    
    /**
     * Update multiple check-in records
     */
    @Update
    suspend fun updateCheckInRecords(records: List<CheckInRecordEntity>)
    
    /**
     * Upsert (insert or update) a check-in record
     */
    @Upsert
    suspend fun upsertCheckInRecord(record: CheckInRecordEntity)
    
    /**
     * Upsert multiple check-in records
     */
    @Upsert
    suspend fun upsertCheckInRecords(records: List<CheckInRecordEntity>)
    
    /**
     * Delete a check-in record
     */
    @Delete
    suspend fun deleteCheckInRecord(record: CheckInRecordEntity)
    
    /**
     * Delete a check-in record by ID
     */
    @Query("DELETE FROM checkin_records WHERE id = :recordId")
    suspend fun deleteCheckInRecordById(recordId: String)
    
    /**
     * Delete all check-in records for a specific child
     */
    @Query("DELETE FROM checkin_records WHERE childId = :childId")
    suspend fun deleteCheckInRecordsForChild(childId: String)
    
    /**
     * Delete all check-in records for a specific service
     */
    @Query("DELETE FROM checkin_records WHERE serviceId = :serviceId")
    suspend fun deleteCheckInRecordsForService(serviceId: String)
    
    /**
     * Update check-out information for a record
     */
    @Query("""
        UPDATE checkin_records 
        SET checkOutTime = :checkOutTime, 
            checkedOutBy = :checkedOutBy, 
            status = 'CHECKED_OUT',
            notes = :notes
        WHERE id = :recordId
    """)
    suspend fun updateCheckOut(
        recordId: String,
        checkOutTime: String,
        checkedOutBy: String,
        notes: String?
    )
    
    /**
     * Get count of check-in records for a child
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE childId = :childId")
    suspend fun getCheckInCountForChild(childId: String): Int
    
    /**
     * Get count of check-in records for a service
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE serviceId = :serviceId")
    suspend fun getCheckInCountForService(serviceId: String): Int
    
    /**
     * Get count of current check-ins for a service
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE serviceId = :serviceId AND status = 'CHECKED_IN'")
    suspend fun getCurrentCheckInCountForService(serviceId: String): Int
    
    /**
     * Get count of all current check-ins
     */
    @Query("SELECT COUNT(*) FROM checkin_records WHERE status = 'CHECKED_IN'")
    suspend fun getTotalCurrentCheckInCount(): Int
    
    /**
     * Clear all check-in records (for testing or data reset)
     */
    @Query("DELETE FROM checkin_records")
    suspend fun clearAllCheckInRecords()
    
    /**
     * Get check-in records that need to be synced (have changes since last sync)
     */
    @Query("SELECT * FROM checkin_records WHERE lastSyncedAt IS NULL")
    suspend fun getCheckInRecordsNeedingSync(): List<CheckInRecordEntity>
    
    /**
     * Update last synced timestamp for a check-in record
     */
    @Query("UPDATE checkin_records SET lastSyncedAt = :timestamp WHERE id = :recordId")
    suspend fun updateLastSyncedAt(recordId: String, timestamp: String)
    
    /**
     * Get the most recent check-in record for a child
     */
    @Query("SELECT * FROM checkin_records WHERE childId = :childId ORDER BY checkInTime DESC LIMIT 1")
    suspend fun getMostRecentCheckInForChild(childId: String): CheckInRecordEntity?
}