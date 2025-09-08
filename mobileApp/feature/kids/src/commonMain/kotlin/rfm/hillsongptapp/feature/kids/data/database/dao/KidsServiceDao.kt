package rfm.hillsongptapp.feature.kids.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Data Access Object for KidsService entities
 */
@Dao
interface KidsServiceDao {
    
    /**
     * Get all available services
     */
    @Query("SELECT * FROM kids_services ORDER BY startTime ASC")
    suspend fun getAllServices(): List<KidsServiceEntity>
    
    /**
     * Get all available services as Flow for reactive updates
     */
    @Query("SELECT * FROM kids_services ORDER BY startTime ASC")
    fun getAllServicesFlow(): Flow<List<KidsServiceEntity>>
    
    /**
     * Get a specific service by ID
     */
    @Query("SELECT * FROM kids_services WHERE id = :serviceId")
    suspend fun getServiceById(serviceId: String): KidsServiceEntity?
    
    /**
     * Get a specific service by ID as Flow
     */
    @Query("SELECT * FROM kids_services WHERE id = :serviceId")
    fun getServiceByIdFlow(serviceId: String): Flow<KidsServiceEntity?>
    
    /**
     * Get services that accept a specific age
     */
    @Query("SELECT * FROM kids_services WHERE minAge <= :age AND maxAge >= :age ORDER BY startTime ASC")
    suspend fun getServicesForAge(age: Int): List<KidsServiceEntity>
    
    /**
     * Get services within an age range
     */
    @Query("SELECT * FROM kids_services WHERE minAge <= :maxAge AND maxAge >= :minAge ORDER BY startTime ASC")
    suspend fun getServicesForAgeRange(minAge: Int, maxAge: Int): List<KidsServiceEntity>
    
    /**
     * Get services that are currently accepting check-ins
     */
    @Query("SELECT * FROM kids_services WHERE isAcceptingCheckIns = 1 ORDER BY startTime ASC")
    suspend fun getServicesAcceptingCheckIns(): List<KidsServiceEntity>
    
    /**
     * Get services that are currently accepting check-ins as Flow
     */
    @Query("SELECT * FROM kids_services WHERE isAcceptingCheckIns = 1 ORDER BY startTime ASC")
    fun getServicesAcceptingCheckInsFlow(): Flow<List<KidsServiceEntity>>
    
    /**
     * Get services that have available capacity
     */
    @Query("SELECT * FROM kids_services WHERE currentCapacity < maxCapacity ORDER BY startTime ASC")
    suspend fun getServicesWithAvailableCapacity(): List<KidsServiceEntity>
    
    /**
     * Get services that are both accepting check-ins and have available capacity
     */
    @Query("""
        SELECT * FROM kids_services 
        WHERE isAcceptingCheckIns = 1 AND currentCapacity < maxCapacity 
        ORDER BY startTime ASC
    """)
    suspend fun getAvailableServicesForCheckIn(): List<KidsServiceEntity>
    
    /**
     * Get services that are both accepting check-ins and have available capacity as Flow
     */
    @Query("""
        SELECT * FROM kids_services 
        WHERE isAcceptingCheckIns = 1 AND currentCapacity < maxCapacity 
        ORDER BY startTime ASC
    """)
    fun getAvailableServicesForCheckInFlow(): Flow<List<KidsServiceEntity>>
    
    /**
     * Get services for a specific age that are available for check-in
     */
    @Query("""
        SELECT * FROM kids_services 
        WHERE minAge <= :age AND maxAge >= :age 
        AND isAcceptingCheckIns = 1 AND currentCapacity < maxCapacity 
        ORDER BY startTime ASC
    """)
    suspend fun getAvailableServicesForAgeAndCheckIn(age: Int): List<KidsServiceEntity>
    
    /**
     * Search services by name (case-insensitive)
     */
    @Query("SELECT * FROM kids_services WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchServicesByName(searchQuery: String): List<KidsServiceEntity>
    
    /**
     * Search services by location (case-insensitive)
     */
    @Query("SELECT * FROM kids_services WHERE location LIKE '%' || :searchQuery || '%' ORDER BY location ASC")
    suspend fun searchServicesByLocation(searchQuery: String): List<KidsServiceEntity>
    
    /**
     * Get services at full capacity
     */
    @Query("SELECT * FROM kids_services WHERE currentCapacity >= maxCapacity ORDER BY name ASC")
    suspend fun getServicesAtCapacity(): List<KidsServiceEntity>
    
    /**
     * Insert a new service
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertService(service: KidsServiceEntity): Long
    
    /**
     * Insert multiple services
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertServices(services: List<KidsServiceEntity>)
    
    /**
     * Update an existing service
     */
    @Update
    suspend fun updateService(service: KidsServiceEntity)
    
    /**
     * Update multiple services
     */
    @Update
    suspend fun updateServices(services: List<KidsServiceEntity>)
    
    /**
     * Upsert (insert or update) a service
     */
    @Upsert
    suspend fun upsertService(service: KidsServiceEntity)
    
    /**
     * Upsert multiple services
     */
    @Upsert
    suspend fun upsertServices(services: List<KidsServiceEntity>)
    
    /**
     * Delete a service
     */
    @Delete
    suspend fun deleteService(service: KidsServiceEntity)
    
    /**
     * Delete a service by ID
     */
    @Query("DELETE FROM kids_services WHERE id = :serviceId")
    suspend fun deleteServiceById(serviceId: String)
    
    /**
     * Update service capacity
     */
    @Query("UPDATE kids_services SET currentCapacity = :currentCapacity WHERE id = :serviceId")
    suspend fun updateServiceCapacity(serviceId: String, currentCapacity: Int)
    
    /**
     * Increment service capacity (for check-ins)
     */
    @Query("UPDATE kids_services SET currentCapacity = currentCapacity + 1 WHERE id = :serviceId")
    suspend fun incrementServiceCapacity(serviceId: String)
    
    /**
     * Decrement service capacity (for check-outs)
     */
    @Query("UPDATE kids_services SET currentCapacity = currentCapacity - 1 WHERE id = :serviceId AND currentCapacity > 0")
    suspend fun decrementServiceCapacity(serviceId: String)
    
    /**
     * Update service check-in acceptance status
     */
    @Query("UPDATE kids_services SET isAcceptingCheckIns = :isAccepting WHERE id = :serviceId")
    suspend fun updateServiceCheckInAcceptance(serviceId: String, isAccepting: Boolean)
    
    /**
     * Get count of all services
     */
    @Query("SELECT COUNT(*) FROM kids_services")
    suspend fun getServicesCount(): Int
    
    /**
     * Get count of services accepting check-ins
     */
    @Query("SELECT COUNT(*) FROM kids_services WHERE isAcceptingCheckIns = 1")
    suspend fun getServicesAcceptingCheckInsCount(): Int
    
    /**
     * Get count of services with available capacity
     */
    @Query("SELECT COUNT(*) FROM kids_services WHERE currentCapacity < maxCapacity")
    suspend fun getServicesWithCapacityCount(): Int
    
    /**
     * Clear all services (for testing or data reset)
     */
    @Query("DELETE FROM kids_services")
    suspend fun clearAllServices()
    
    /**
     * Get services that need to be synced (have changes since last sync)
     */
    @Query("SELECT * FROM kids_services WHERE lastSyncedAt IS NULL")
    suspend fun getServicesNeedingSync(): List<KidsServiceEntity>
    
    /**
     * Update last synced timestamp for a service
     */
    @Query("UPDATE kids_services SET lastSyncedAt = :timestamp WHERE id = :serviceId")
    suspend fun updateLastSyncedAt(serviceId: String, timestamp: String)
    
    /**
     * Get services by time range
     */
    @Query("SELECT * FROM kids_services WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime ASC")
    suspend fun getServicesByTimeRange(startTime: String, endTime: String): List<KidsServiceEntity>
}