package rfm.hillsongptapp.feature.kids.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.database.entity.CheckInRecordEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.ChildEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.KidsServiceEntity

/**
 * Mock implementation of KidsLocalDataSource for testing
 */
class MockKidsLocalDataSource : KidsLocalDataSource {
    
    // Test data storage
    val childrenByParentId = mutableMapOf<String, List<ChildEntity>>()
    val childrenById = mutableMapOf<String, ChildEntity>()
    val servicesById = mutableMapOf<String, KidsServiceEntity>()
    val checkInRecordsById = mutableMapOf<String, CheckInRecordEntity>()
    val currentCheckInsByService = mutableMapOf<String, List<CheckInRecordEntity>>()
    
    var allServices = listOf<KidsServiceEntity>()
    var allCurrentCheckIns = listOf<CheckInRecordEntity>()
    
    // Track operations for verification
    val insertedChildren = mutableListOf<ChildEntity>()
    val updatedChildren = mutableListOf<ChildEntity>()
    val upsertedChildren = mutableListOf<ChildEntity>()
    val deletedChildIds = mutableListOf<String>()
    val insertedServices = mutableListOf<KidsServiceEntity>()
    val updatedServices = mutableListOf<KidsServiceEntity>()
    val upsertedServices = mutableListOf<KidsServiceEntity>()
    val insertedCheckInRecords = mutableListOf<CheckInRecordEntity>()
    val updatedCheckInRecords = mutableListOf<CheckInRecordEntity>()
    val checkInStatusUpdates = mutableListOf<CheckInStatusUpdate>()
    
    data class CheckInStatusUpdate(
        val childId: String,
        val status: String,
        val serviceId: String?,
        val checkInTime: String?,
        val checkOutTime: String?,
        val updatedAt: String
    )
    
    // Child Operations
    
    override suspend fun getChildrenByParentId(parentId: String): List<ChildEntity> {
        return childrenByParentId[parentId] ?: emptyList()
    }
    
    override fun getChildrenByParentIdFlow(parentId: String): Flow<List<ChildEntity>> {
        return flowOf(getChildrenByParentId(parentId))
    }
    
    override suspend fun getChildById(childId: String): ChildEntity? {
        return childrenById[childId]
    }
    
    override fun getChildByIdFlow(childId: String): Flow<ChildEntity?> {
        return flowOf(getChildById(childId))
    }
    
    override suspend fun insertChild(child: ChildEntity) {
        insertedChildren.add(child)
        childrenById[child.id] = child
        
        // Update parent's children list
        val parentChildren = childrenByParentId[child.parentId]?.toMutableList() ?: mutableListOf()
        parentChildren.add(child)
        childrenByParentId[child.parentId] = parentChildren
    }
    
    override suspend fun updateChild(child: ChildEntity) {
        updatedChildren.add(child)
        childrenById[child.id] = child
        
        // Update in parent's children list
        val parentChildren = childrenByParentId[child.parentId]?.toMutableList() ?: mutableListOf()
        val index = parentChildren.indexOfFirst { it.id == child.id }
        if (index >= 0) {
            parentChildren[index] = child
            childrenByParentId[child.parentId] = parentChildren
        }
    }
    
    override suspend fun upsertChild(child: ChildEntity) {
        upsertedChildren.add(child)
        childrenById[child.id] = child
        
        // Update in parent's children list
        val parentChildren = childrenByParentId[child.parentId]?.toMutableList() ?: mutableListOf()
        val index = parentChildren.indexOfFirst { it.id == child.id }
        if (index >= 0) {
            parentChildren[index] = child
        } else {
            parentChildren.add(child)
        }
        childrenByParentId[child.parentId] = parentChildren
    }
    
    override suspend fun upsertChildren(children: List<ChildEntity>) {
        children.forEach { upsertChild(it) }
    }
    
    override suspend fun deleteChild(childId: String) {
        deletedChildIds.add(childId)
        val child = childrenById.remove(childId)
        
        // Remove from parent's children list
        child?.let {
            val parentChildren = childrenByParentId[it.parentId]?.toMutableList() ?: mutableListOf()
            parentChildren.removeAll { it.id == childId }
            childrenByParentId[it.parentId] = parentChildren
        }
    }
    
    override suspend fun getChildrenNeedingSync(): List<ChildEntity> {
        return childrenById.values.filter { it.lastSyncedAt == null }
    }
    
    override suspend fun updateLastSyncedAt(childId: String, timestamp: String) {
        childrenById[childId]?.let { child ->
            childrenById[childId] = child.copy(lastSyncedAt = timestamp)
        }
    }
    
    override suspend fun updateChildCheckInStatus(
        childId: String,
        status: String,
        serviceId: String?,
        checkInTime: String?,
        checkOutTime: String?,
        updatedAt: String
    ) {
        checkInStatusUpdates.add(
            CheckInStatusUpdate(childId, status, serviceId, checkInTime, checkOutTime, updatedAt)
        )
        
        childrenById[childId]?.let { child ->
            childrenById[childId] = child.copy(
                status = status,
                currentServiceId = serviceId,
                checkInTime = checkInTime,
                checkOutTime = checkOutTime,
                updatedAt = updatedAt
            )
        }
    }
    
    // Service Operations
    
    override suspend fun getAllServices(): List<KidsServiceEntity> {
        return allServices
    }
    
    override fun getAllServicesFlow(): Flow<List<KidsServiceEntity>> {
        return flowOf(getAllServices())
    }
    
    override suspend fun getServiceById(serviceId: String): KidsServiceEntity? {
        return servicesById[serviceId]
    }
    
    override suspend fun getServicesAcceptingCheckIns(): List<KidsServiceEntity> {
        return allServices.filter { it.isAcceptingCheckIns }
    }
    
    override suspend fun insertService(service: KidsServiceEntity) {
        insertedServices.add(service)
        servicesById[service.id] = service
    }
    
    override suspend fun updateService(service: KidsServiceEntity) {
        updatedServices.add(service)
        servicesById[service.id] = service
    }
    
    override suspend fun upsertService(service: KidsServiceEntity) {
        upsertedServices.add(service)
        servicesById[service.id] = service
    }
    
    override suspend fun upsertServices(services: List<KidsServiceEntity>) {
        services.forEach { upsertService(it) }
    }
    
    override suspend fun deleteService(serviceId: String) {
        servicesById.remove(serviceId)
    }
    
    override suspend fun getServicesNeedingSync(): List<KidsServiceEntity> {
        return servicesById.values.filter { it.lastSyncedAt == null }
    }
    
    override suspend fun updateServiceLastSyncedAt(serviceId: String, timestamp: String) {
        servicesById[serviceId]?.let { service ->
            servicesById[serviceId] = service.copy(lastSyncedAt = timestamp)
        }
    }
    
    // Check-in Record Operations
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): List<CheckInRecordEntity> {
        val records = checkInRecordsById.values.filter { it.childId == childId }
            .sortedByDescending { it.checkInTime }
        return if (limit != null) records.take(limit) else records
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): List<CheckInRecordEntity> {
        return currentCheckInsByService[serviceId] ?: emptyList()
    }
    
    override suspend fun getAllCurrentCheckIns(): List<CheckInRecordEntity> {
        return allCurrentCheckIns
    }
    
    override suspend fun getCheckInRecord(recordId: String): CheckInRecordEntity? {
        return checkInRecordsById[recordId]
    }
    
    override suspend fun insertCheckInRecord(record: CheckInRecordEntity) {
        insertedCheckInRecords.add(record)
        checkInRecordsById[record.id] = record
    }
    
    override suspend fun updateCheckInRecord(record: CheckInRecordEntity) {
        updatedCheckInRecords.add(record)
        checkInRecordsById[record.id] = record
    }
    
    override suspend fun upsertCheckInRecord(record: CheckInRecordEntity) {
        checkInRecordsById[record.id] = record
    }
    
    override suspend fun upsertCheckInRecords(records: List<CheckInRecordEntity>) {
        records.forEach { upsertCheckInRecord(it) }
    }
    
    override suspend fun deleteCheckInRecord(recordId: String) {
        checkInRecordsById.remove(recordId)
    }
    
    override suspend fun getCheckInRecordsNeedingSync(): List<CheckInRecordEntity> {
        return checkInRecordsById.values.filter { it.lastSyncedAt == null }
    }
    
    override suspend fun updateCheckInRecordLastSyncedAt(recordId: String, timestamp: String) {
        checkInRecordsById[recordId]?.let { record ->
            checkInRecordsById[recordId] = record.copy(lastSyncedAt = timestamp)
        }
    }
    
    // Utility Operations
    
    override suspend fun clearAllData() {
        childrenByParentId.clear()
        childrenById.clear()
        servicesById.clear()
        checkInRecordsById.clear()
        currentCheckInsByService.clear()
        allServices = emptyList()
        allCurrentCheckIns = emptyList()
    }
    
    override suspend fun getLastSyncTimestamp(): String? {
        return null
    }
    
    override suspend fun updateLastSyncTimestamp(timestamp: String) {
        // No-op for mock
    }
}