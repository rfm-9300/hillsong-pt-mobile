package rfm.hillsongptapp.feature.kids.data.database.datasource

import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Interface for local database operations for kids management
 * Provides abstraction over Room database operations
 */
interface KidsLocalDataSource {
    
    // Child Operations
    suspend fun getChildrenByParentId(parentId: String): List<ChildEntity>
    fun getChildrenByParentIdFlow(parentId: String): Flow<List<ChildEntity>>
    suspend fun getChildById(childId: String): ChildEntity?
    fun getChildByIdFlow(childId: String): Flow<ChildEntity?>
    suspend fun insertChild(child: ChildEntity)
    suspend fun updateChild(child: ChildEntity)
    suspend fun upsertChild(child: ChildEntity)
    suspend fun upsertChildren(children: List<ChildEntity>)
    suspend fun deleteChild(childId: String)
    suspend fun getChildrenNeedingSync(): List<ChildEntity>
    suspend fun updateLastSyncedAt(childId: String, timestamp: String)
    suspend fun updateChildCheckInStatus(
        childId: String,
        status: String,
        serviceId: String?,
        checkInTime: String?,
        checkOutTime: String?,
        updatedAt: String
    )
    
    // Service Operations
    suspend fun getAllServices(): List<KidsServiceEntity>
    fun getAllServicesFlow(): Flow<List<KidsServiceEntity>>
    suspend fun getServiceById(serviceId: String): KidsServiceEntity?
    suspend fun getServicesAcceptingCheckIns(): List<KidsServiceEntity>
    suspend fun insertService(service: KidsServiceEntity)
    suspend fun updateService(service: KidsServiceEntity)
    suspend fun upsertService(service: KidsServiceEntity)
    suspend fun upsertServices(services: List<KidsServiceEntity>)
    suspend fun deleteService(serviceId: String)
    suspend fun getServicesNeedingSync(): List<KidsServiceEntity>
    suspend fun updateServiceLastSyncedAt(serviceId: String, timestamp: String)
    
    // Check-in Record Operations
    suspend fun getCheckInHistory(childId: String, limit: Int?): List<CheckInRecordEntity>
    suspend fun getCurrentCheckIns(serviceId: String): List<CheckInRecordEntity>
    suspend fun getAllCurrentCheckIns(): List<CheckInRecordEntity>
    suspend fun getCheckInRecord(recordId: String): CheckInRecordEntity?
    suspend fun insertCheckInRecord(record: CheckInRecordEntity)
    suspend fun updateCheckInRecord(record: CheckInRecordEntity)
    suspend fun upsertCheckInRecord(record: CheckInRecordEntity)
    suspend fun upsertCheckInRecords(records: List<CheckInRecordEntity>)
    suspend fun deleteCheckInRecord(recordId: String)
    suspend fun getCheckInRecordsNeedingSync(): List<CheckInRecordEntity>
    suspend fun updateCheckInRecordLastSyncedAt(recordId: String, timestamp: String)
    
    // Utility Operations
    suspend fun clearAllData()
    suspend fun getLastSyncTimestamp(): String?
    suspend fun updateLastSyncTimestamp(timestamp: String)
}