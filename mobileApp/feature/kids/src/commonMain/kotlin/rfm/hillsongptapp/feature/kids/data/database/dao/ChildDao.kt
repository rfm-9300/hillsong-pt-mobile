package rfm.hillsongptapp.feature.kids.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity

/**
 * Data Access Object for Child entities
 */
@Dao
interface ChildDao {
    
    /**
     * Get all children for a specific parent
     */
    @Query("SELECT * FROM children WHERE parentId = :parentId ORDER BY name ASC")
    suspend fun getChildrenByParentId(parentId: String): List<ChildEntity>
    
    /**
     * Get all children for a specific parent as Flow for reactive updates
     */
    @Query("SELECT * FROM children WHERE parentId = :parentId ORDER BY name ASC")
    fun getChildrenByParentIdFlow(parentId: String): Flow<List<ChildEntity>>
    
    /**
     * Get a specific child by ID
     */
    @Query("SELECT * FROM children WHERE id = :childId")
    suspend fun getChildById(childId: String): ChildEntity?
    
    /**
     * Get a specific child by ID as Flow
     */
    @Query("SELECT * FROM children WHERE id = :childId")
    fun getChildByIdFlow(childId: String): Flow<ChildEntity?>
    
    /**
     * Get all children currently checked into a specific service
     */
    @Query("SELECT * FROM children WHERE currentServiceId = :serviceId AND status = 'CHECKED_IN'")
    suspend fun getChildrenCheckedIntoService(serviceId: String): List<ChildEntity>
    
    /**
     * Get all children with a specific status
     */
    @Query("SELECT * FROM children WHERE status = :status ORDER BY name ASC")
    suspend fun getChildrenByStatus(status: String): List<ChildEntity>
    
    /**
     * Get all currently checked-in children
     */
    @Query("SELECT * FROM children WHERE status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    suspend fun getAllCheckedInChildren(): List<ChildEntity>
    
    /**
     * Get all currently checked-in children as Flow
     */
    @Query("SELECT * FROM children WHERE status = 'CHECKED_IN' ORDER BY checkInTime DESC")
    fun getAllCheckedInChildrenFlow(): Flow<List<ChildEntity>>
    
    /**
     * Search children by name (case-insensitive)
     */
    @Query("SELECT * FROM children WHERE parentId = :parentId AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchChildrenByName(parentId: String, searchQuery: String): List<ChildEntity>
    
    /**
     * Get children by age range (calculated from date of birth)
     * Note: This is a simplified query - in production, you'd want proper date calculations
     */
    @Query("SELECT * FROM children WHERE parentId = :parentId ORDER BY dateOfBirth DESC")
    suspend fun getChildrenByParentIdOrderedByAge(parentId: String): List<ChildEntity>
    
    /**
     * Insert a new child
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChild(child: ChildEntity): Long
    
    /**
     * Insert multiple children
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChildren(children: List<ChildEntity>)
    
    /**
     * Update an existing child
     */
    @Update
    suspend fun updateChild(child: ChildEntity)
    
    /**
     * Update multiple children
     */
    @Update
    suspend fun updateChildren(children: List<ChildEntity>)
    
    /**
     * Upsert (insert or update) a child
     */
    @Upsert
    suspend fun upsertChild(child: ChildEntity)
    
    /**
     * Upsert multiple children
     */
    @Upsert
    suspend fun upsertChildren(children: List<ChildEntity>)
    
    /**
     * Delete a child
     */
    @Delete
    suspend fun deleteChild(child: ChildEntity)
    
    /**
     * Delete a child by ID
     */
    @Query("DELETE FROM children WHERE id = :childId")
    suspend fun deleteChildById(childId: String)
    
    /**
     * Delete all children for a specific parent
     */
    @Query("DELETE FROM children WHERE parentId = :parentId")
    suspend fun deleteChildrenByParentId(parentId: String)
    
    /**
     * Update child's check-in status and related fields
     */
    @Query("""
        UPDATE children 
        SET status = :status, 
            currentServiceId = :serviceId, 
            checkInTime = :checkInTime,
            checkOutTime = :checkOutTime,
            updatedAt = :updatedAt
        WHERE id = :childId
    """)
    suspend fun updateChildCheckInStatus(
        childId: String,
        status: String,
        serviceId: String?,
        checkInTime: String?,
        checkOutTime: String?,
        updatedAt: String
    )
    
    /**
     * Get count of children for a parent
     */
    @Query("SELECT COUNT(*) FROM children WHERE parentId = :parentId")
    suspend fun getChildrenCountByParentId(parentId: String): Int
    
    /**
     * Get count of children currently checked into a service
     */
    @Query("SELECT COUNT(*) FROM children WHERE currentServiceId = :serviceId AND status = 'CHECKED_IN'")
    suspend fun getCheckedInCountForService(serviceId: String): Int
    
    /**
     * Clear all children (for testing or data reset)
     */
    @Query("DELETE FROM children")
    suspend fun clearAllChildren()
    
    /**
     * Get children that need to be synced (have changes since last sync)
     */
    @Query("SELECT * FROM children WHERE lastSyncedAt IS NULL OR updatedAt > lastSyncedAt")
    suspend fun getChildrenNeedingSync(): List<ChildEntity>
    
    /**
     * Update last synced timestamp for a child
     */
    @Query("UPDATE children SET lastSyncedAt = :timestamp WHERE id = :childId")
    suspend fun updateLastSyncedAt(childId: String, timestamp: String)
}