package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity

/**
 * Mock implementation of ChildDao for testing
 */
class MockChildDao : ChildDao {
    
    var shouldThrowError = false
    val insertedChildren = mutableListOf<ChildEntity>()
    val updatedChildren = mutableListOf<ChildEntity>()
    val deletedChildIds = mutableListOf<String>()
    val childrenById = mutableMapOf<String, ChildEntity>()
    val childrenByParent = mutableMapOf<String, List<ChildEntity>>()
    val childrenByStatus = mutableMapOf<CheckInStatus, List<ChildEntity>>()
    val childrenByService = mutableMapOf<String, List<ChildEntity>>()
    
    private fun checkError() {
        if (shouldThrowError) throw RuntimeException("Mock DAO error")
    }
    
    override suspend fun insertChild(child: ChildEntity) {
        checkError()
        insertedChildren.add(child)
        childrenById[child.id] = child
    }
    
    override suspend fun insertChildren(children: List<ChildEntity>) {
        checkError()
        children.forEach { insertChild(it) }
    }
    
    override suspend fun getChildById(childId: String): ChildEntity? {
        checkError()
        return childrenById[childId]
    }
    
    override suspend fun getChildrenByParentId(parentId: String): List<ChildEntity> {
        checkError()
        return childrenByParent[parentId] ?: emptyList()
    }
    
    override suspend fun getChildrenByStatus(status: CheckInStatus): List<ChildEntity> {
        checkError()
        return childrenByStatus[status] ?: emptyList()
    }
    
    override suspend fun getChildrenByServiceId(serviceId: String): List<ChildEntity> {
        checkError()
        return childrenByService[serviceId] ?: emptyList()
    }
    
    override suspend fun getAllChildren(): List<ChildEntity> {
        checkError()
        return childrenById.values.toList()
    }
    
    override suspend fun searchChildrenByName(searchQuery: String): List<ChildEntity> {
        checkError()
        return childrenById.values.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    override suspend fun getChildrenCountByServiceAndStatus(serviceId: String, status: CheckInStatus): Int {
        checkError()
        return childrenById.values.count { it.currentServiceId == serviceId && it.status == status }
    }
    
    override suspend fun getChildrenCountByStatus(status: CheckInStatus): Int {
        checkError()
        return childrenById.values.count { it.status == status }
    }
    
    override suspend fun updateChild(child: ChildEntity) {
        checkError()
        updatedChildren.add(child)
        childrenById[child.id] = child
    }
    
    override suspend fun deleteChild(child: ChildEntity) {
        checkError()
        deletedChildIds.add(child.id)
        childrenById.remove(child.id)
    }
    
    override suspend fun deleteChildById(childId: String) {
        checkError()
        deletedChildIds.add(childId)
        childrenById.remove(childId)
    }
    
    override suspend fun deleteAllChildren() {
        checkError()
        childrenById.clear()
    }
}

/**
 * Mock implementation of CheckInRecordDao for testing
 */
class MockCheckInRecordDao : CheckInRecordDao {
    
    var shouldThrowError = false
    val insertedRecords = mutableListOf<CheckInRecordEntity>()
    val updatedRecords = mutableListOf<CheckInRecordEntity>()
    val deletedRecordIds = mutableListOf<String>()
    val recordsById = mutableMapOf<String, CheckInRecordEntity>()
    val recordsByChild = mutableMapOf<String, List<CheckInRecordEntity>>()
    val recordsByService = mutableMapOf<String, List<CheckInRecordEntity>>()
    val recordsByStatus = mutableMapOf<CheckInStatus, List<CheckInRecordEntity>>()
    val currentRecordsByChild = mutableMapOf<String, CheckInRecordEntity>()
    val activeRecordsByService = mutableMapOf<String, List<CheckInRecordEntity>>()
    val recordsByDateRange = mutableMapOf<String, List<CheckInRecordEntity>>()
    
    private fun checkError() {
        if (shouldThrowError) throw RuntimeException("Mock DAO error")
    }
    
    override suspend fun insertCheckInRecord(record: CheckInRecordEntity) {
        checkError()
        insertedRecords.add(record)
        recordsById[record.id] = record
    }
    
    override suspend fun insertCheckInRecords(records: List<CheckInRecordEntity>) {
        checkError()
        records.forEach { insertCheckInRecord(it) }
    }
    
    override suspend fun getCheckInRecordById(recordId: String): CheckInRecordEntity? {
        checkError()
        return recordsById[recordId]
    }
    
    override suspend fun getCheckInRecordsByChildId(childId: String): List<CheckInRecordEntity> {
        checkError()
        return recordsByChild[childId] ?: emptyList()
    }
    
    override suspend fun getCheckInRecordsByServiceId(serviceId: String): List<CheckInRecordEntity> {
        checkError()
        return recordsByService[serviceId] ?: emptyList()
    }
    
    override suspend fun getCheckInRecordsByStatus(status: CheckInStatus): List<CheckInRecordEntity> {
        checkError()
        return recordsByStatus[status] ?: emptyList()
    }
    
    override suspend fun getCurrentCheckInRecordForChild(childId: String, status: CheckInStatus): CheckInRecordEntity? {
        checkError()
        return currentRecordsByChild[childId]
    }
    
    override suspend fun getActiveCheckInRecordsForService(serviceId: String, status: CheckInStatus): List<CheckInRecordEntity> {
        checkError()
        return activeRecordsByService[serviceId] ?: emptyList()
    }
    
    override suspend fun getCheckInRecordsByDateRange(startDate: String, endDate: String): List<CheckInRecordEntity> {
        checkError()
        return recordsByDateRange["$startDate-$endDate"] ?: emptyList()
    }
    
    override suspend fun getCheckInRecordsByChildAndDateRange(childId: String, startDate: String, endDate: String): List<CheckInRecordEntity> {
        checkError()
        return recordsByChild[childId]?.filter { 
            it.checkInTime >= startDate && it.checkInTime <= endDate 
        } ?: emptyList()
    }
    
    override suspend fun getCheckInRecordsByServiceAndDateRange(serviceId: String, startDate: String, endDate: String): List<CheckInRecordEntity> {
        checkError()
        return recordsByService[serviceId]?.filter { 
            it.checkInTime >= startDate && it.checkInTime <= endDate 
        } ?: emptyList()
    }
    
    override suspend fun getActiveCheckInCountForService(serviceId: String, status: CheckInStatus): Int {
        checkError()
        return activeRecordsByService[serviceId]?.size ?: 0
    }
    
    override suspend fun getCheckInCountByDateRange(startDate: String, endDate: String): Int {
        checkError()
        return recordsByDateRange["$startDate-$endDate"]?.size ?: 0
    }
    
    override suspend fun getAllCheckInRecords(): List<CheckInRecordEntity> {
        checkError()
        return recordsById.values.toList()
    }
    
    override suspend fun updateCheckInRecord(record: CheckInRecordEntity) {
        checkError()
        updatedRecords.add(record)
        recordsById[record.id] = record
    }
    
    override suspend fun deleteCheckInRecord(record: CheckInRecordEntity) {
        checkError()
        deletedRecordIds.add(record.id)
        recordsById.remove(record.id)
    }
    
    override suspend fun deleteCheckInRecordById(recordId: String) {
        checkError()
        deletedRecordIds.add(recordId)
        recordsById.remove(recordId)
    }
    
    override suspend fun deleteCheckInRecordsByChildId(childId: String) {
        checkError()
        recordsByChild[childId]?.forEach { record ->
            deletedRecordIds.add(record.id)
            recordsById.remove(record.id)
        }
    }
    
    override suspend fun deleteCheckInRecordsByServiceId(serviceId: String) {
        checkError()
        recordsByService[serviceId]?.forEach { record ->
            deletedRecordIds.add(record.id)
            recordsById.remove(record.id)
        }
    }
    
    override suspend fun deleteAllCheckInRecords() {
        checkError()
        recordsById.clear()
    }
}

/**
 * Mock implementation of KidsServiceDao for testing
 */
class MockKidsServiceDao : KidsServiceDao {
    
    var shouldThrowError = false
    val insertedServices = mutableListOf<KidsServiceEntity>()
    val updatedServices = mutableListOf<KidsServiceEntity>()
    val deletedServiceIds = mutableListOf<String>()
    val servicesById = mutableMapOf<String, KidsServiceEntity>()
    var allServices = listOf<KidsServiceEntity>()
    var activeServices = listOf<KidsServiceEntity>()
    val servicesByAge = mutableMapOf<Int, List<KidsServiceEntity>>()
    var servicesWithSpots = listOf<KidsServiceEntity>()
    var acceptingCheckInServices = listOf<KidsServiceEntity>()
    val servicesByTimeRange = mutableMapOf<String, List<KidsServiceEntity>>()
    val servicesByLocation = mutableMapOf<String, List<KidsServiceEntity>>()
    
    private fun checkError() {
        if (shouldThrowError) throw RuntimeException("Mock DAO error")
    }
    
    override suspend fun insertKidsService(service: KidsServiceEntity) {
        checkError()
        insertedServices.add(service)
        servicesById[service.id] = service
    }
    
    override suspend fun insertKidsServices(services: List<KidsServiceEntity>) {
        checkError()
        services.forEach { insertKidsService(it) }
    }
    
    override suspend fun getKidsServiceById(serviceId: String): KidsServiceEntity? {
        checkError()
        return servicesById[serviceId]
    }
    
    override suspend fun getAllKidsServices(): List<KidsServiceEntity> {
        checkError()
        return allServices
    }
    
    override suspend fun getActiveKidsServices(): List<KidsServiceEntity> {
        checkError()
        return activeServices
    }
    
    override suspend fun searchKidsServicesByName(searchQuery: String): List<KidsServiceEntity> {
        checkError()
        return allServices.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    override suspend fun getKidsServicesByAgeRange(age: Int): List<KidsServiceEntity> {
        checkError()
        return servicesByAge[age] ?: emptyList()
    }
    
    override suspend fun getKidsServicesWithAvailableSpots(): List<KidsServiceEntity> {
        checkError()
        return servicesWithSpots
    }
    
    override suspend fun getAvailableKidsServicesForCheckIn(): List<KidsServiceEntity> {
        checkError()
        return acceptingCheckInServices
    }
    
    override suspend fun getKidsServicesByTimeRange(startTime: String, endTime: String): List<KidsServiceEntity> {
        checkError()
        return servicesByTimeRange["$startTime-$endTime"] ?: emptyList()
    }
    
    override suspend fun getKidsServicesByLocation(location: String): List<KidsServiceEntity> {
        checkError()
        return servicesByLocation[location] ?: emptyList()
    }
    
    override suspend fun getActiveKidsServicesCount(): Int {
        checkError()
        return activeServices.size
    }
    
    override suspend fun getTotalCurrentCapacity(): Int? {
        checkError()
        return allServices.sumOf { it.currentCapacity }
    }
    
    override suspend fun getTotalMaxCapacity(): Int? {
        checkError()
        return allServices.sumOf { it.maxCapacity }
    }
    
    override suspend fun updateKidsService(service: KidsServiceEntity) {
        checkError()
        updatedServices.add(service)
        servicesById[service.id] = service
    }
    
    override suspend fun updateServiceCapacity(serviceId: String, newCapacity: Int) {
        checkError()
        servicesById[serviceId]?.let { service ->
            val updated = service.copy(currentCapacity = newCapacity)
            servicesById[serviceId] = updated
            updatedServices.add(updated)
        }
    }
    
    override suspend fun updateServiceCheckInStatus(serviceId: String, isAccepting: Boolean) {
        checkError()
        servicesById[serviceId]?.let { service ->
            val updated = service.copy(isAcceptingCheckIns = isAccepting)
            servicesById[serviceId] = updated
            updatedServices.add(updated)
        }
    }
    
    override suspend fun deleteKidsService(service: KidsServiceEntity) {
        checkError()
        deletedServiceIds.add(service.id)
        servicesById.remove(service.id)
    }
    
    override suspend fun deleteKidsServiceById(serviceId: String) {
        checkError()
        deletedServiceIds.add(serviceId)
        servicesById.remove(serviceId)
    }
    
    override suspend fun deleteAllKidsServices() {
        checkError()
        servicesById.clear()
    }
}