package rfm.hillsongptapp.core.data.integration

import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity

/**
 * In-memory implementation of ChildDao for integration testing
 * Simulates database behavior without requiring actual database setup
 */
class InMemoryChildDao : ChildDao {
    
    private val children = mutableMapOf<String, ChildEntity>()
    
    override suspend fun insertChild(child: ChildEntity) {
        children[child.id] = child
    }
    
    override suspend fun insertChildren(children: List<ChildEntity>) {
        children.forEach { insertChild(it) }
    }
    
    override suspend fun getChildById(childId: String): ChildEntity? {
        return children[childId]
    }
    
    override suspend fun getChildrenByParentId(parentId: String): List<ChildEntity> {
        return children.values.filter { it.parentId == parentId }.sortedBy { it.name }
    }
    
    override suspend fun getChildrenByStatus(status: CheckInStatus): List<ChildEntity> {
        return children.values.filter { it.status == status }.sortedBy { it.name }
    }
    
    override suspend fun getChildrenByServiceId(serviceId: String): List<ChildEntity> {
        return children.values.filter { it.currentServiceId == serviceId }.sortedBy { it.name }
    }
    
    override suspend fun getAllChildren(): List<ChildEntity> {
        return children.values.sortedBy { it.name }
    }
    
    override suspend fun searchChildrenByName(searchQuery: String): List<ChildEntity> {
        return children.values.filter { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }.sortedBy { it.name }
    }
    
    override suspend fun getChildrenCountByServiceAndStatus(serviceId: String, status: CheckInStatus): Int {
        return children.values.count { it.currentServiceId == serviceId && it.status == status }
    }
    
    override suspend fun getChildrenCountByStatus(status: CheckInStatus): Int {
        return children.values.count { it.status == status }
    }
    
    override suspend fun updateChild(child: ChildEntity) {
        children[child.id] = child
    }
    
    override suspend fun deleteChild(child: ChildEntity) {
        children.remove(child.id)
    }
    
    override suspend fun deleteChildById(childId: String) {
        children.remove(childId)
    }
    
    override suspend fun deleteAllChildren() {
        children.clear()
    }
}

/**
 * In-memory implementation of CheckInRecordDao for integration testing
 */
class InMemoryCheckInRecordDao : CheckInRecordDao {
    
    private val records = mutableMapOf<String, CheckInRecordEntity>()
    
    override suspend fun insertCheckInRecord(record: CheckInRecordEntity) {
        records[record.id] = record
    }
    
    override suspend fun insertCheckInRecords(records: List<CheckInRecordEntity>) {
        records.forEach { insertCheckInRecord(it) }
    }
    
    override suspend fun getCheckInRecordById(recordId: String): CheckInRecordEntity? {
        return records[recordId]
    }
    
    override suspend fun getCheckInRecordsByChildId(childId: String): List<CheckInRecordEntity> {
        return records.values.filter { it.childId == childId }.sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCheckInRecordsByServiceId(serviceId: String): List<CheckInRecordEntity> {
        return records.values.filter { it.serviceId == serviceId }.sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCheckInRecordsByStatus(status: CheckInStatus): List<CheckInRecordEntity> {
        return records.values.filter { it.status == status }.sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCurrentCheckInRecordForChild(childId: String, status: CheckInStatus): CheckInRecordEntity? {
        return records.values
            .filter { it.childId == childId && it.status == status }
            .maxByOrNull { it.checkInTime }
    }
    
    override suspend fun getActiveCheckInRecordsForService(serviceId: String, status: CheckInStatus): List<CheckInRecordEntity> {
        return records.values
            .filter { it.serviceId == serviceId && it.status == status }
            .sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCheckInRecordsByDateRange(startDate: String, endDate: String): List<CheckInRecordEntity> {
        return records.values
            .filter { it.checkInTime >= startDate && it.checkInTime <= endDate }
            .sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCheckInRecordsByChildAndDateRange(childId: String, startDate: String, endDate: String): List<CheckInRecordEntity> {
        return records.values
            .filter { 
                it.childId == childId && 
                it.checkInTime >= startDate && 
                it.checkInTime <= endDate 
            }
            .sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getCheckInRecordsByServiceAndDateRange(serviceId: String, startDate: String, endDate: String): List<CheckInRecordEntity> {
        return records.values
            .filter { 
                it.serviceId == serviceId && 
                it.checkInTime >= startDate && 
                it.checkInTime <= endDate 
            }
            .sortedByDescending { it.checkInTime }
    }
    
    override suspend fun getActiveCheckInCountForService(serviceId: String, status: CheckInStatus): Int {
        return records.values.count { it.serviceId == serviceId && it.status == status }
    }
    
    override suspend fun getCheckInCountByDateRange(startDate: String, endDate: String): Int {
        return records.values.count { it.checkInTime >= startDate && it.checkInTime <= endDate }
    }
    
    override suspend fun getAllCheckInRecords(): List<CheckInRecordEntity> {
        return records.values.sortedByDescending { it.checkInTime }
    }
    
    override suspend fun updateCheckInRecord(record: CheckInRecordEntity) {
        records[record.id] = record
    }
    
    override suspend fun deleteCheckInRecord(record: CheckInRecordEntity) {
        records.remove(record.id)
    }
    
    override suspend fun deleteCheckInRecordById(recordId: String) {
        records.remove(recordId)
    }
    
    override suspend fun deleteCheckInRecordsByChildId(childId: String) {
        records.values.filter { it.childId == childId }.forEach { record ->
            records.remove(record.id)
        }
    }
    
    override suspend fun deleteCheckInRecordsByServiceId(serviceId: String) {
        records.values.filter { it.serviceId == serviceId }.forEach { record ->
            records.remove(record.id)
        }
    }
    
    override suspend fun deleteAllCheckInRecords() {
        records.clear()
    }
}

/**
 * In-memory implementation of KidsServiceDao for integration testing
 */
class InMemoryKidsServiceDao : KidsServiceDao {
    
    private val services = mutableMapOf<String, KidsServiceEntity>()
    
    override suspend fun insertKidsService(service: KidsServiceEntity) {
        services[service.id] = service
    }
    
    override suspend fun insertKidsServices(services: List<KidsServiceEntity>) {
        services.forEach { insertKidsService(it) }
    }
    
    override suspend fun getKidsServiceById(serviceId: String): KidsServiceEntity? {
        return services[serviceId]
    }
    
    override suspend fun getAllKidsServices(): List<KidsServiceEntity> {
        return services.values.sortedBy { it.startTime }
    }
    
    override suspend fun getActiveKidsServices(): List<KidsServiceEntity> {
        return services.values.filter { it.isAcceptingCheckIns }.sortedBy { it.startTime }
    }
    
    override suspend fun searchKidsServicesByName(searchQuery: String): List<KidsServiceEntity> {
        return services.values
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .sortedBy { it.name }
    }
    
    override suspend fun getKidsServicesByAgeRange(age: Int): List<KidsServiceEntity> {
        return services.values
            .filter { it.minAge <= age && it.maxAge >= age }
            .sortedBy { it.startTime }
    }
    
    override suspend fun getKidsServicesWithAvailableSpots(): List<KidsServiceEntity> {
        return services.values
            .filter { it.currentCapacity < it.maxCapacity }
            .sortedBy { it.startTime }
    }
    
    override suspend fun getAvailableKidsServicesForCheckIn(): List<KidsServiceEntity> {
        return services.values
            .filter { it.isAcceptingCheckIns && it.currentCapacity < it.maxCapacity }
            .sortedBy { it.startTime }
    }
    
    override suspend fun getKidsServicesByTimeRange(startTime: String, endTime: String): List<KidsServiceEntity> {
        return services.values
            .filter { it.startTime >= startTime && it.endTime <= endTime }
            .sortedBy { it.startTime }
    }
    
    override suspend fun getKidsServicesByLocation(location: String): List<KidsServiceEntity> {
        return services.values
            .filter { it.location == location }
            .sortedBy { it.startTime }
    }
    
    override suspend fun getActiveKidsServicesCount(): Int {
        return services.values.count { it.isAcceptingCheckIns }
    }
    
    override suspend fun getTotalCurrentCapacity(): Int? {
        return services.values.sumOf { it.currentCapacity }
    }
    
    override suspend fun getTotalMaxCapacity(): Int? {
        return services.values.sumOf { it.maxCapacity }
    }
    
    override suspend fun updateKidsService(service: KidsServiceEntity) {
        services[service.id] = service
    }
    
    override suspend fun updateServiceCapacity(serviceId: String, newCapacity: Int) {
        services[serviceId]?.let { service ->
            services[serviceId] = service.copy(currentCapacity = newCapacity)
        }
    }
    
    override suspend fun updateServiceCheckInStatus(serviceId: String, isAccepting: Boolean) {
        services[serviceId]?.let { service ->
            services[serviceId] = service.copy(isAcceptingCheckIns = isAccepting)
        }
    }
    
    override suspend fun deleteKidsService(service: KidsServiceEntity) {
        services.remove(service.id)
    }
    
    override suspend fun deleteKidsServiceById(serviceId: String) {
        services.remove(serviceId)
    }
    
    override suspend fun deleteAllKidsServices() {
        services.clear()
    }
}