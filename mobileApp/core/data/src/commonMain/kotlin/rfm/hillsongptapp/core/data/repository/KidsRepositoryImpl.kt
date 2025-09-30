package rfm.hillsongptapp.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.CheckInStatus
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.ChildEntity
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.ktor.requests.CheckInRequest
import rfm.hillsongptapp.core.network.ktor.requests.CheckOutRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildRegistrationRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildUpdateRequest
import rfm.hillsongptapp.core.network.ktor.requests.EmergencyContactRequest
import rfm.hillsongptapp.core.network.ktor.responses.ChildResponse
import rfm.hillsongptapp.core.network.ktor.responses.EmergencyContactResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * Implementation of KidsRepository with real-time synchronization and offline support
 * Follows the same pattern as other repository implementations in the core module
 */
class KidsRepositoryImpl(
    private val childDao: ChildDao,
    private val checkInRecordDao: CheckInRecordDao,
    private val kidsServiceDao: KidsServiceDao,
    private val kidsApiService: KidsApiService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : KidsRepository {
    
    private val logger = LoggerHelper
    
    // Real-time manager for handling WebSocket-like updates
    private val realTimeManager = KidsRealTimeManager(
        childDao = childDao,
        checkInRecordDao = checkInRecordDao,
        kidsServiceDao = kidsServiceDao,
        kidsApiService = kidsApiService,
        coroutineScope = coroutineScope
    )
    
    // Child Management Operations
    
    override suspend fun getChildrenForParent(parentId: String): KidsResult<List<Child>> {
        return try {
            // First, get from local database
            val localChildren = childDao.getChildrenByParentId(parentId).map { it.toDomain() }
            
            // Try to sync with remote if possible
            syncChildrenFromRemote(parentId)
            
            // Return updated local data
            val updatedChildren = childDao.getChildrenByParentId(parentId).map { it.toDomain() }
            KidsResult.Success(updatedChildren)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting children for parent $parentId", e)
            // Return local data as fallback
            try {
                val localChildren = childDao.getChildrenByParentId(parentId).map { it.toDomain() }
                KidsResult.Success(localChildren)
            } catch (localError: Exception) {
                KidsResult.Error("Failed to get children: ${localError.message}")
            }
        }
    }
    
    override suspend fun registerChild(child: Child): KidsResult<Child> {
        return try {
            // Generate ID if not provided
            val childWithId = if (child.id.isBlank()) {
                child.copy(id = generateId())
            } else child
            
            // Save locally first for offline support
            val currentTime = getCurrentTimestamp()
            val childEntity = childWithId.copy(
                createdAt = currentTime,
                updatedAt = currentTime
            ).toEntity()
            
            childDao.insertChild(childEntity)
            
            // Try to sync with remote
            val request = childWithId.toRegistrationRequest()
            val remoteResult = kidsApiService.registerChild(request)
            
            when (remoteResult) {
                is NetworkResult.Success -> {
                    val response = remoteResult.data
                    if (response.success && response.child != null) {
                        // Update local with server response
                        val childResponse = response.child!!
                        val serverChild = childResponse.toDomain()
                        childDao.insertChild(serverChild.toEntity())
                        KidsResult.Success(serverChild)
                    } else {
                        // Server returned error, but we have local copy
                        LoggerHelper.logError("Server registration failed: ${response.message}")
                        KidsResult.Success(childWithId)
                    }
                }
                is NetworkResult.Error -> {
                    // Network error, return local copy
                    LoggerHelper.logError("Network error during registration: ${remoteResult.exception.message}")
                    KidsResult.Success(childWithId)
                }
                is NetworkResult.Loading -> {
                    KidsResult.Loading
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error registering child", e)
            KidsResult.Error("Failed to register child: ${e.message}")
        }
    }
    
    override suspend fun updateChild(child: Child): KidsResult<Child> {
        return try {
            val currentTime = getCurrentTimestamp()
            val updatedChild = child.copy(updatedAt = currentTime)
            
            // Update locally first
            childDao.updateChild(updatedChild.toEntity())
            
            // Try to sync with remote
            val request = child.toUpdateRequest()
            val remoteResult = kidsApiService.updateChild(child.id, request)
            
            when (remoteResult) {
                is NetworkResult.Success -> {
                    val response = remoteResult.data
                    if (response.success && response.child != null) {
                        val childResponse = response.child!!
                        val serverChild = childResponse.toDomain()
                        childDao.insertChild(serverChild.toEntity())
                        KidsResult.Success(serverChild)
                    } else {
                        KidsResult.Success(updatedChild)
                    }
                }
                is NetworkResult.Error -> {
                    KidsResult.Success(updatedChild)
                }
                is NetworkResult.Loading -> {
                    KidsResult.Loading
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error updating child ${child.id}", e)
            KidsResult.Error("Failed to update child: ${e.message}")
        }
    }
    
    override suspend fun deleteChild(childId: String): KidsResult<Unit> {
        return try {
            // Delete locally first
            childDao.deleteChildById(childId)
            
            // Try to delete from remote
            val remoteResult = kidsApiService.deleteChild(childId)
            
            when (remoteResult) {
                is NetworkResult.Error -> {
                    LoggerHelper.logError("Failed to delete child from remote: ${remoteResult.exception.message}")
                }
                else -> {
                    // Success or loading - we've already deleted locally
                }
            }
            
            KidsResult.Success(Unit)
        } catch (e: Exception) {
            LoggerHelper.logError("Error deleting child $childId", e)
            KidsResult.Error("Failed to delete child: ${e.message}")
        }
    }
    
    override suspend fun getChildById(childId: String): KidsResult<Child> {
        return try {
            val localChild = childDao.getChildById(childId)
            if (localChild != null) {
                KidsResult.Success(localChild.toDomain())
            } else {
                // Try to get from remote
                val remoteResult = kidsApiService.getChild(childId)
                when (remoteResult) {
                    is NetworkResult.Success -> {
                        val response = remoteResult.data
                        if (response.success && response.child != null) {
                            val childResponse = response.child!!
                            val child = childResponse.toDomain()
                            // Cache locally
                            childDao.insertChild(child.toEntity())
                            KidsResult.Success(child)
                        } else {
                            KidsResult.Error("Child not found")
                        }
                    }
                    is NetworkResult.Error -> {
                        KidsResult.Error("Child not found: ${remoteResult.exception.message}")
                    }
                    is NetworkResult.Loading -> {
                        KidsResult.Loading
                    }
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting child $childId", e)
            KidsResult.Error("Failed to get child: ${e.message}")
        }
    }
    
    // Service Management Operations
    
    override suspend fun getAvailableServices(): KidsResult<List<KidsService>> {
        return try {
            // Get from local first
            val localServices = kidsServiceDao.getAllKidsServices().map { it.toDomain() }
            
            // Try to sync with remote
            syncServicesFromRemote()
            
            // Return updated local data
            val updatedServices = kidsServiceDao.getAllKidsServices().map { it.toDomain() }
            KidsResult.Success(updatedServices)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting available services", e)
            // Return local data as fallback
            try {
                val localServices = kidsServiceDao.getAllKidsServices().map { it.toDomain() }
                KidsResult.Success(localServices)
            } catch (localError: Exception) {
                KidsResult.Error("Failed to get services: ${localError.message}")
            }
        }
    }
    
    override suspend fun getServicesForAge(age: Int): KidsResult<List<KidsService>> {
        return try {
            val services = kidsServiceDao.getKidsServicesByAgeRange(age).map { it.toDomain() }
            KidsResult.Success(services)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting services for age $age", e)
            KidsResult.Error("Failed to get services for age: ${e.message}")
        }
    }
    
    override suspend fun getServiceById(serviceId: String): KidsResult<KidsService> {
        return try {
            val localService = kidsServiceDao.getKidsServiceById(serviceId)
            if (localService != null) {
                KidsResult.Success(localService.toDomain())
            } else {
                // Try to get from remote
                val remoteResult = kidsApiService.getService(serviceId)
                when (remoteResult) {
                    is NetworkResult.Success -> {
                        val response = remoteResult.data
                        if (response.success && response.service != null) {
                            val serviceResponse = response.service!!
                            val service = serviceResponse.toDomain()
                            // Cache locally
                            kidsServiceDao.insertKidsService(service.toEntity())
                            KidsResult.Success(service)
                        } else {
                            KidsResult.Error("Service not found")
                        }
                    }
                    is NetworkResult.Error -> {
                        KidsResult.Error("Service not found: ${remoteResult.exception.message}")
                    }
                    is NetworkResult.Loading -> {
                        KidsResult.Loading
                    }
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting service $serviceId", e)
            KidsResult.Error("Failed to get service: ${e.message}")
        }
    }
    
    override suspend fun getServicesAcceptingCheckIns(): KidsResult<List<KidsService>> {
        return try {
            val services = kidsServiceDao.getAvailableKidsServicesForCheckIn().map { it.toDomain() }
            KidsResult.Success(services)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting services accepting check-ins", e)
            KidsResult.Error("Failed to get services accepting check-ins: ${e.message}")
        }
    } 
   
    // Check-in/Check-out Operations
    
    override suspend fun checkInChild(
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String?
    ): KidsResult<CheckInRecord> {
        return try {
            // First, validate the check-in locally
            val child = childDao.getChildById(childId)
                ?: return KidsResult.Error("Child not found")
            
            val service = kidsServiceDao.getKidsServiceById(serviceId)
                ?: return KidsResult.Error("Service not found")
            
            // Check if child is already checked in
            if (child.status == CheckInStatus.CHECKED_IN) {
                return KidsResult.Error("Child is already checked in")
            }
            
            // Check service capacity
            if (service.currentCapacity >= service.maxCapacity) {
                return KidsResult.Error("Service is at full capacity")
            }
            
            // Check age eligibility
            val childDomain = child.toDomain()
            val serviceDomain = service.toDomain()
            if (!childDomain.isEligibleForService(serviceDomain)) {
                return KidsResult.Error("Child is not eligible for this service")
            }
            
            val currentTime = getCurrentTimestamp()
            val recordId = generateId()
            
            // Create check-in record
            val checkInRecord = CheckInRecord(
                id = recordId,
                childId = childId,
                serviceId = serviceId,
                checkInTime = currentTime,
                checkedInBy = checkedInBy,
                notes = notes,
                status = CheckInStatus.CHECKED_IN
            )
            
            // Perform optimistic local update
            val updatedChild = child.copy(
                status = CheckInStatus.CHECKED_IN,
                currentServiceId = serviceId,
                checkInTime = currentTime,
                checkOutTime = null,
                updatedAt = currentTime
            )
            
            val updatedService = service.copy(
                currentCapacity = service.currentCapacity + 1
            )
            
            childDao.updateChild(updatedChild)
            kidsServiceDao.updateKidsService(updatedService)
            checkInRecordDao.insertCheckInRecord(checkInRecord.toEntity())
            
            // Try to sync with remote
            val request = CheckInRequest(
                childId = childId,
                serviceId = serviceId,
                checkedInBy = checkedInBy,
                notes = notes
            )
            
            val remoteResult = kidsApiService.checkInChild(request)
            
            when (remoteResult) {
                is NetworkResult.Success -> {
                    val response = remoteResult.data
                    if (response.success && response.record != null) {
                        // Update with server response
                        val recordResponse = response.record!!
                        val serverRecord = recordResponse.toDomain()
                        checkInRecordDao.insertCheckInRecord(serverRecord.toEntity())
                        
                        // Update child and service from server response if provided
                        response.updatedChild?.let { childDto ->
                            childDao.insertChild(childDto.toDomain().toEntity())
                        }
                        response.updatedService?.let { serviceDto ->
                            kidsServiceDao.insertKidsService(serviceDto.toDomain().toEntity())
                        }
                        
                        KidsResult.Success(serverRecord)
                    } else {
                        // Server returned error - keep local changes for now
                        LoggerHelper.logError("Server check-in failed: ${response.message}")
                        KidsResult.Success(checkInRecord)
                    }
                }
                is NetworkResult.Error -> {
                    // Network error - keep local changes
                    LoggerHelper.logError("Network error during check-in: ${remoteResult.exception.message}")
                    KidsResult.Success(checkInRecord)
                }
                is NetworkResult.Loading -> {
                    KidsResult.Loading
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error checking in child $childId to service $serviceId", e)
            KidsResult.Error("Failed to check in child: ${e.message}")
        }
    }
    
    override suspend fun checkOutChild(
        childId: String,
        checkedOutBy: String,
        notes: String?
    ): KidsResult<CheckInRecord> {
        return try {
            // Find the current check-in record
            val currentRecord = checkInRecordDao.getCurrentCheckInRecordForChild(childId)
                ?: return KidsResult.Error("Child is not currently checked in")
            
            val child = childDao.getChildById(childId)
                ?: return KidsResult.Error("Child not found")
            
            val service = kidsServiceDao.getKidsServiceById(currentRecord.serviceId)
                ?: return KidsResult.Error("Service not found")
            
            val currentTime = getCurrentTimestamp()
            
            // Update the record with check-out information
            val updatedRecord = currentRecord.copy(
                checkOutTime = currentTime,
                checkedOutBy = checkedOutBy,
                notes = if (notes != null) "${currentRecord.notes ?: ""}\n$notes".trim() else currentRecord.notes,
                status = CheckInStatus.CHECKED_OUT
            )
            
            // Perform optimistic local update
            val updatedChild = child.copy(
                status = CheckInStatus.CHECKED_OUT,
                currentServiceId = null,
                checkInTime = null,
                checkOutTime = currentTime,
                updatedAt = currentTime
            )
            
            val updatedService = service.copy(
                currentCapacity = maxOf(0, service.currentCapacity - 1)
            )
            
            childDao.updateChild(updatedChild)
            kidsServiceDao.updateKidsService(updatedService)
            checkInRecordDao.updateCheckInRecord(updatedRecord)
            
            // Try to sync with remote
            val request = CheckOutRequest(
                childId = childId,
                checkedOutBy = checkedOutBy,
                notes = notes
            )
            
            val remoteResult = kidsApiService.checkOutChild(request)
            
            when (remoteResult) {
                is NetworkResult.Success -> {
                    val response = remoteResult.data
                    if (response.success && response.record != null) {
                        // Update with server response
                        val recordResponse = response.record!!
                        val serverRecord = recordResponse.toDomain()
                        checkInRecordDao.insertCheckInRecord(serverRecord.toEntity())
                        
                        // Update child and service from server response if provided
                        response.updatedChild?.let { childDto ->
                            childDao.insertChild(childDto.toDomain().toEntity())
                        }
                        response.updatedService?.let { serviceDto ->
                            kidsServiceDao.insertKidsService(serviceDto.toDomain().toEntity())
                        }
                        
                        KidsResult.Success(serverRecord)
                    } else {
                        // Server returned error - keep local changes
                        LoggerHelper.logError("Server check-out failed: ${response.message}")
                        KidsResult.Success(updatedRecord.toDomain())
                    }
                }
                is NetworkResult.Error -> {
                    // Network error - keep local changes
                    LoggerHelper.logError("Network error during check-out: ${remoteResult.exception.message}")
                    KidsResult.Success(updatedRecord.toDomain())
                }
                is NetworkResult.Loading -> {
                    KidsResult.Loading
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error checking out child $childId", e)
            KidsResult.Error("Failed to check out child: ${e.message}")
        }
    }
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): KidsResult<List<CheckInRecord>> {
        return try {
            val records = if (limit != null) {
                checkInRecordDao.getCheckInRecordsByChildId(childId).take(limit)
            } else {
                checkInRecordDao.getCheckInRecordsByChildId(childId)
            }.map { it.toDomain() }
            
            KidsResult.Success(records)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting check-in history for child $childId", e)
            KidsResult.Error("Failed to get check-in history: ${e.message}")
        }
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): KidsResult<List<CheckInRecord>> {
        return try {
            val records = checkInRecordDao.getActiveCheckInRecordsForService(serviceId).map { it.toDomain() }
            KidsResult.Success(records)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting current check-ins for service $serviceId", e)
            KidsResult.Error("Failed to get current check-ins: ${e.message}")
        }
    }
    
    override suspend fun getAllCurrentCheckIns(): KidsResult<List<CheckInRecord>> {
        return try {
            val records = checkInRecordDao.getCheckInRecordsByStatus(CheckInStatus.CHECKED_IN).map { it.toDomain() }
            KidsResult.Success(records)
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting all current check-ins", e)
            KidsResult.Error("Failed to get all current check-ins: ${e.message}")
        }
    }
    
    override suspend fun getCheckInRecord(recordId: String): KidsResult<CheckInRecord> {
        return try {
            val record = checkInRecordDao.getCheckInRecordById(recordId)
            if (record != null) {
                KidsResult.Success(record.toDomain())
            } else {
                KidsResult.Error("Check-in record not found")
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error getting check-in record $recordId", e)
            KidsResult.Error("Failed to get check-in record: ${e.message}")
        }
    }
    
    // Staff/Reporting Operations
    
    override suspend fun getServiceReport(serviceId: String): KidsResult<ServiceReport> {
        return try {
            val service = kidsServiceDao.getKidsServiceById(serviceId)?.toDomain()
                ?: return KidsResult.Error("Service not found")
            
            val currentCheckIns = checkInRecordDao.getActiveCheckInRecordsForService(serviceId).map { it.toDomain() }
            val checkedInChildren = currentCheckIns.mapNotNull { record ->
                childDao.getChildById(record.childId)?.toDomain()
            }
            
            val report = ServiceReport(
                serviceId = service.id,
                serviceName = service.name,
                totalCapacity = service.maxCapacity,
                currentCheckIns = service.currentCapacity,
                availableSpots = service.maxCapacity - service.currentCapacity,
                checkedInChildren = checkedInChildren,
                staffMembers = service.staffMembers,
                generatedAt = getCurrentTimestamp()
            )
            
            KidsResult.Success(report)
        } catch (e: Exception) {
            LoggerHelper.logError("Error generating service report for $serviceId", e)
            KidsResult.Error("Failed to generate service report: ${e.message}")
        }
    }
    
    override suspend fun getAttendanceReport(startDate: String, endDate: String): KidsResult<AttendanceReport> {
        return try {
            val records = checkInRecordDao.getCheckInRecordsByDateRange(startDate, endDate)
            val uniqueChildren = records.map { it.childId }.toSet().size
            val serviceBreakdown = records.groupBy { it.serviceId }.mapValues { it.value.size }
            
            // Simplified daily breakdown - in real implementation would parse dates properly
            val dailyBreakdown = records.groupBy { it.checkInTime.substring(0, 10) }.mapValues { it.value.size }
            
            val report = AttendanceReport(
                startDate = startDate,
                endDate = endDate,
                totalCheckIns = records.size,
                uniqueChildren = uniqueChildren,
                serviceBreakdown = serviceBreakdown,
                dailyBreakdown = dailyBreakdown,
                generatedAt = getCurrentTimestamp()
            )
            
            KidsResult.Success(report)
        } catch (e: Exception) {
            LoggerHelper.logError("Error generating attendance report", e)
            KidsResult.Error("Failed to generate attendance report: ${e.message}")
        }
    }  
  
    // Real-time Operations (Flow-based for reactive updates)
    
    override fun getChildrenForParentStream(parentId: String): Flow<KidsResult<List<Child>>> = flow {
        emit(KidsResult.Loading)
        try {
            // Emit initial data
            val children = childDao.getChildrenByParentId(parentId).map { it.toDomain() }
            emit(KidsResult.Success(children))
            
            // Try to sync with remote
            syncChildrenFromRemote(parentId)
            
            // Emit updated data
            val updatedChildren = childDao.getChildrenByParentId(parentId).map { it.toDomain() }
            emit(KidsResult.Success(updatedChildren))
        } catch (e: Exception) {
            LoggerHelper.logError("Error in children stream for parent $parentId", e)
            emit(KidsResult.Error("Failed to get children stream: ${e.message}"))
        }
    }.catch { e ->
        emit(KidsResult.Error("Stream error: ${e.message}"))
    }
    
    override fun getAvailableServicesStream(): Flow<KidsResult<List<KidsService>>> = flow {
        emit(KidsResult.Loading)
        try {
            // Emit initial data
            val services = kidsServiceDao.getAllKidsServices().map { it.toDomain() }
            emit(KidsResult.Success(services))
            
            // Try to sync with remote
            syncServicesFromRemote()
            
            // Emit updated data
            val updatedServices = kidsServiceDao.getAllKidsServices().map { it.toDomain() }
            emit(KidsResult.Success(updatedServices))
        } catch (e: Exception) {
            LoggerHelper.logError("Error in services stream", e)
            emit(KidsResult.Error("Failed to get services stream: ${e.message}"))
        }
    }.catch { e ->
        emit(KidsResult.Error("Stream error: ${e.message}"))
    }
    
    override fun getCurrentCheckInsStream(serviceId: String): Flow<KidsResult<List<CheckInRecord>>> {
        return realTimeManager.subscribeToCurrentCheckInsUpdates(serviceId)
    }
    
    override fun getAllCurrentCheckInsStream(): Flow<KidsResult<List<CheckInRecord>>> {
        return realTimeManager.subscribeToCurrentCheckInsUpdates()
    }
    
    override fun subscribeToChildUpdates(childId: String): Flow<KidsResult<Child>> {
        return realTimeManager.subscribeToChildUpdates(childId)
    }
    
    override fun subscribeToServiceUpdates(serviceId: String): Flow<KidsResult<KidsService>> {
        return realTimeManager.subscribeToServiceUpdates(serviceId)
    }
    
    // Private helper methods
    
    private suspend fun syncChildrenFromRemote(parentId: String) {
        try {
            val remoteResult = kidsApiService.getChildrenByParent(parentId)
            if (remoteResult is NetworkResult.Success) {
                val response = remoteResult.data
                if (response.success) {
                    val children = response.children.map { it.toDomain().toEntity() }
                    children.forEach { child ->
                        childDao.insertChild(child)
                    }
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Failed to sync children from remote", e)
        }
    }
    
    private suspend fun syncServicesFromRemote() {
        try {
            val remoteResult = kidsApiService.getServices()
            if (remoteResult is NetworkResult.Success) {
                val response = remoteResult.data
                if (response.success) {
                    val services = response.services.map { it.toDomain().toEntity() }
                    services.forEach { service ->
                        kidsServiceDao.insertKidsService(service)
                    }
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Failed to sync services from remote", e)
        }
    }
    
    private fun generateId(): String {
        return "local_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
    
    private fun getCurrentTimestamp(): String {
        return Clock.System.now().toString()
    }
    
    // Cleanup
    fun cleanup() {
        realTimeManager.cleanup()
        // Note: coroutineScope.cancel() method doesn't exist
        // The scope should be managed by the parent component
    }
    
    // Additional methods for triggering manual updates
    suspend fun triggerChildUpdate(childId: String) {
        realTimeManager.triggerChildUpdate(childId)
    }
    
    suspend fun triggerServiceUpdate(serviceId: String) {
        realTimeManager.triggerServiceUpdate(serviceId)
    }
}

// Extension functions for converting between domain models and entities

private fun Child.toEntity(): ChildEntity {
    return ChildEntity(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact,
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun ChildEntity.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact,
        status = status,
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun KidsService.toEntity(): KidsServiceEntity {
    return KidsServiceEntity(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

private fun KidsServiceEntity.toDomain(): KidsService {
    return KidsService(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

private fun CheckInRecord.toEntity(): CheckInRecordEntity {
    return CheckInRecordEntity(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status
    )
}

private fun CheckInRecordEntity.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = status
    )
}

// Extension functions for converting to request DTOs

private fun Child.toRegistrationRequest(): ChildRegistrationRequest {
    return ChildRegistrationRequest(
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toRequest()
    )
}

private fun Child.toUpdateRequest(): ChildUpdateRequest {
    return ChildUpdateRequest(
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toRequest()
    )
}

private fun rfm.hillsongptapp.core.data.model.EmergencyContact.toRequest(): EmergencyContactRequest {
    return EmergencyContactRequest(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

// Extension functions for converting from response DTOs to domain models

private fun ChildResponse.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDomain(),
        status = CheckInStatus.valueOf(status),
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun EmergencyContactResponse.toDomain(): rfm.hillsongptapp.core.data.model.EmergencyContact {
    return rfm.hillsongptapp.core.data.model.EmergencyContact(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

private fun ServiceResponse.toDomain(): KidsService {
    return KidsService(
        id = id,
        name = name,
        description = description,
        minAge = minAge,
        maxAge = maxAge,
        startTime = startTime,
        endTime = endTime,
        location = location,
        maxCapacity = maxCapacity,
        currentCapacity = currentCapacity,
        isAcceptingCheckIns = isAcceptingCheckIns,
        staffMembers = staffMembers,
        createdAt = createdAt
    )
}

private fun CheckInRecordResponse.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = CheckInStatus.valueOf(status)
    )
}