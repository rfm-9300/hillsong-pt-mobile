package rfm.hillsongptapp.feature.kids.data.database.datasource

import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.feature.kids.data.database.KidsDatabase
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Implementation of KidsLocalDataSource using Room database
 */
class KidsLocalDataSourceImpl(
    private val database: KidsDatabase
) : KidsLocalDataSource {
    
    private val childDao = database.childDao()
    private val serviceDao = database.kidsServiceDao()
    private val checkInDao = database.checkInRecordDao()
    
    // Child Operations
    
    override suspend fun getChildrenByParentId(parentId: String): List<ChildEntity> {
        return childDao.getChildrenByParentId(parentId)
    }
    
    override fun getChildrenByParentIdFlow(parentId: String): Flow<List<ChildEntity>> {
        return childDao.getChildrenByParentIdFlow(parentId)
    }
    
    override suspend fun getChildById(childId: String): ChildEntity? {
        return childDao.getChildById(childId)
    }
    
    override fun getChildByIdFlow(childId: String): Flow<ChildEntity?> {
        return childDao.getChildByIdFlow(childId)
    }
    
    override suspend fun insertChild(child: ChildEntity) {
        childDao.insertChild(child)
    }
    
    override suspend fun updateChild(child: ChildEntity) {
        childDao.updateChild(child)
    }
    
    override suspend fun upsertChild(child: ChildEntity) {
        childDao.upsertChild(child)
    }
    
    override suspend fun upsertChildren(children: List<ChildEntity>) {
        childDao.upsertChildren(children)
    }
    
    override suspend fun deleteChild(childId: String) {
        childDao.deleteChildById(childId)
    }
    
    override suspend fun getChildrenNeedingSync(): List<ChildEntity> {
        return childDao.getChildrenNeedingSync()
    }
    
    override suspend fun updateLastSyncedAt(childId: String, timestamp: String) {
        childDao.updateLastSyncedAt(childId, timestamp)
    }
    
    override suspend fun updateChildCheckInStatus(
        childId: String,
        status: String,
        serviceId: String?,
        checkInTime: String?,
        checkOutTime: String?,
        updatedAt: String
    ) {
        childDao.updateChildCheckInStatus(
            childId = childId,
            status = status,
            serviceId = serviceId,
            checkInTime = checkInTime,
            checkOutTime = checkOutTime,
            updatedAt = updatedAt
        )
    }
    
    // Service Operations
    
    override suspend fun getAllServices(): List<KidsServiceEntity> {
        return serviceDao.getAllServices()
    }
    
    override fun getAllServicesFlow(): Flow<List<KidsServiceEntity>> {
        return serviceDao.getAllServicesFlow()
    }
    
    override suspend fun getServiceById(serviceId: String): KidsServiceEntity? {
        return serviceDao.getServiceById(serviceId)
    }
    
    override suspend fun getServicesAcceptingCheckIns(): List<KidsServiceEntity> {
        return serviceDao.getServicesAcceptingCheckIns()
    }
    
    override suspend fun insertService(service: KidsServiceEntity) {
        serviceDao.insertService(service)
    }
    
    override suspend fun updateService(service: KidsServiceEntity) {
        serviceDao.updateService(service)
    }
    
    override suspend fun upsertService(service: KidsServiceEntity) {
        serviceDao.upsertService(service)
    }
    
    override suspend fun upsertServices(services: List<KidsServiceEntity>) {
        serviceDao.upsertServices(services)
    }
    
    override suspend fun deleteService(serviceId: String) {
        serviceDao.deleteServiceById(serviceId)
    }
    
    override suspend fun getServicesNeedingSync(): List<KidsServiceEntity> {
        return serviceDao.getServicesNeedingSync()
    }
    
    override suspend fun updateServiceLastSyncedAt(serviceId: String, timestamp: String) {
        serviceDao.updateLastSyncedAt(serviceId, timestamp)
    }
    
    // Check-in Record Operations
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): List<CheckInRecordEntity> {
        return if (limit != null) {
            checkInDao.getCheckInHistoryWithLimit(childId, limit)
        } else {
            checkInDao.getCheckInHistory(childId)
        }
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): List<CheckInRecordEntity> {
        return checkInDao.getCurrentCheckInsForService(serviceId)
    }
    
    override suspend fun getAllCurrentCheckIns(): List<CheckInRecordEntity> {
        return checkInDao.getAllCurrentCheckIns()
    }
    
    override suspend fun getCheckInRecord(recordId: String): CheckInRecordEntity? {
        return checkInDao.getCheckInRecordById(recordId)
    }
    
    override suspend fun insertCheckInRecord(record: CheckInRecordEntity) {
        checkInDao.insertCheckInRecord(record)
    }
    
    override suspend fun updateCheckInRecord(record: CheckInRecordEntity) {
        checkInDao.updateCheckInRecord(record)
    }
    
    override suspend fun upsertCheckInRecord(record: CheckInRecordEntity) {
        checkInDao.upsertCheckInRecord(record)
    }
    
    override suspend fun upsertCheckInRecords(records: List<CheckInRecordEntity>) {
        checkInDao.upsertCheckInRecords(records)
    }
    
    override suspend fun deleteCheckInRecord(recordId: String) {
        checkInDao.deleteCheckInRecordById(recordId)
    }
    
    override suspend fun getCheckInRecordsNeedingSync(): List<CheckInRecordEntity> {
        return checkInDao.getCheckInRecordsNeedingSync()
    }
    
    override suspend fun updateCheckInRecordLastSyncedAt(recordId: String, timestamp: String) {
        checkInDao.updateLastSyncedAt(recordId, timestamp)
    }
    
    // Utility Operations
    
    override suspend fun clearAllData() {
        database.clearAllTables()
    }
    
    override suspend fun getLastSyncTimestamp(): String? {
        // This would typically be stored in a settings/metadata table
        // For now, we'll return null and implement this later if needed
        return null
    }
    
    override suspend fun updateLastSyncTimestamp(timestamp: String) {
        // This would typically be stored in a settings/metadata table
        // For now, we'll do nothing and implement this later if needed
    }
}