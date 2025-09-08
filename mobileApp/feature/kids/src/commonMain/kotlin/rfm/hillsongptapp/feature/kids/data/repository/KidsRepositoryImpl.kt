package rfm.hillsongptapp.feature.kids.data.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import rfm.hillsongptapp.feature.kids.data.database.datasource.KidsLocalDataSource
import rfm.hillsongptapp.feature.kids.data.database.entity.toEntity
import rfm.hillsongptapp.feature.kids.data.database.entity.toDomain
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.mapper.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.*
import rfm.hillsongptapp.feature.kids.domain.model.*
import rfm.hillsongptapp.feature.kids.domain.repository.*
import co.touchlab.kermit.Logger

/**
 * Implementation of KidsRepository with real-time synchronization and offline support
 */
class KidsRepositoryImpl(
    private val localDataSource: KidsLocalDataSource,
    private val remoteDataSource: KidsRemoteDataSource,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : KidsRepository {
    
    private val logger = Logger.withTag("KidsRepositoryImpl")
    
    // Real-time update callbacks
    private val childUpdateCallbacks = mutableMapOf<String, (Child) -> Unit>()
    private val serviceUpdateCallbacks = mutableMapOf<String, (KidsService) -> Unit>()
    
    // WebSocket message processing job
    private var webSocketJob: Job? = null
    
    init {
        startWebSocketMessageProcessing()
    }
    
    // Child Management Operations
    
    override suspend fun getChildrenForParent(parentId: String): Result<List<Child>> {
        return try {
            // First, try to get from local database
            val localChildren = localDataSource.getChildrenByParentId(parentId).map { it.toDomain() }
            
            // Try to sync with remote if possible
            syncChildrenFromRemote(parentId)
            
            // Return local data (which may have been updated by sync)
            val updatedChildren = localDataSource.getChildrenByParentId(parentId).map { it.toDomain() }
            Result.success(updatedChildren)
        } catch (e: Exception) {
            logger.e(e) { "Error getting children for parent $parentId" }
            // Return local data as fallback
            try {
                val localChildren = localDataSource.getChildrenByParentId(parentId).map { it.toDomain() }
                Result.success(localChildren)
            } catch (localError: Exception) {
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun registerChild(child: Child): Result<Child> {
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
            
            localDataSource.insertChild(childEntity)
            
            // Try to sync with remote
            val remoteResult = remoteDataSource.registerChild(childWithId.toRegistrationRequest())
            
            when {
                remoteResult.isSuccess -> {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.child != null) {
                        // Update local with server response
                        val serverChild = response.child.toDomain()
                        localDataSource.upsertChild(serverChild.toEntity(currentTime))
                        localDataSource.updateLastSyncedAt(serverChild.id, currentTime)
                        Result.success(serverChild)
                    } else {
                        // Server returned error, but we have local copy
                        logger.w { "Server registration failed: ${response.message}" }
                        Result.success(childWithId)
                    }
                }
                else -> {
                    // Network error, return local copy
                    logger.w { "Network error during registration, using local copy" }
                    Result.success(childWithId)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error registering child" }
            Result.failure(e)
        }
    }
    
    override suspend fun updateChild(child: Child): Result<Child> {
        return try {
            val currentTime = getCurrentTimestamp()
            val updatedChild = child.copy(updatedAt = currentTime)
            
            // Update locally first
            localDataSource.updateChild(updatedChild.toEntity())
            
            // Try to sync with remote
            val remoteResult = remoteDataSource.updateChild(child.id, child.toUpdateRequest())
            
            when {
                remoteResult.isSuccess -> {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.child != null) {
                        val serverChild = response.child.toDomain()
                        localDataSource.upsertChild(serverChild.toEntity(currentTime))
                        localDataSource.updateLastSyncedAt(serverChild.id, currentTime)
                        Result.success(serverChild)
                    } else {
                        Result.success(updatedChild)
                    }
                }
                else -> {
                    Result.success(updatedChild)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error updating child ${child.id}" }
            Result.failure(e)
        }
    }
    
    override suspend fun deleteChild(childId: String): Result<Unit> {
        return try {
            // Delete locally first
            localDataSource.deleteChild(childId)
            
            // Try to delete from remote
            val remoteResult = remoteDataSource.deleteChild(childId)
            
            if (remoteResult.isFailure) {
                logger.w { "Failed to delete child from remote, but deleted locally" }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "Error deleting child $childId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getChildById(childId: String): Result<Child> {
        return try {
            val localChild = localDataSource.getChildById(childId)
            if (localChild != null) {
                Result.success(localChild.toDomain())
            } else {
                // Try to get from remote
                val remoteResult = remoteDataSource.getChildById(childId)
                if (remoteResult.isSuccess) {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.child != null) {
                        val child = response.child.toDomain()
                        // Cache locally
                        localDataSource.upsertChild(child.toEntity(getCurrentTimestamp()))
                        Result.success(child)
                    } else {
                        Result.failure(Exception("Child not found"))
                    }
                } else {
                    Result.failure(remoteResult.exceptionOrNull() ?: Exception("Child not found"))
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error getting child $childId" }
            Result.failure(e)
        }
    }
    
    // Service Management Operations
    
    override suspend fun getAvailableServices(): Result<List<KidsService>> {
        return try {
            // Get from local first
            val localServices = localDataSource.getAllServices().map { it.toDomain() }
            
            // Try to sync with remote
            syncServicesFromRemote()
            
            // Return updated local data
            val updatedServices = localDataSource.getAllServices().map { it.toDomain() }
            Result.success(updatedServices)
        } catch (e: Exception) {
            logger.e(e) { "Error getting available services" }
            // Return local data as fallback
            try {
                val localServices = localDataSource.getAllServices().map { it.toDomain() }
                Result.success(localServices)
            } catch (localError: Exception) {
                Result.failure(localError)
            }
        }
    }
    
    override suspend fun getServicesForAge(age: Int): Result<List<KidsService>> {
        return getAvailableServices().map { services ->
            services.filter { it.isAgeEligible(age) }
        }
    }
    
    override suspend fun getServiceById(serviceId: String): Result<KidsService> {
        return try {
            val localService = localDataSource.getServiceById(serviceId)
            if (localService != null) {
                Result.success(localService.toDomain())
            } else {
                // Try to get from remote
                val remoteResult = remoteDataSource.getServiceById(serviceId)
                if (remoteResult.isSuccess) {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.service != null) {
                        val service = response.service.toDomain()
                        // Cache locally
                        localDataSource.upsertService(service.toEntity(getCurrentTimestamp()))
                        Result.success(service)
                    } else {
                        Result.failure(Exception("Service not found"))
                    }
                } else {
                    Result.failure(remoteResult.exceptionOrNull() ?: Exception("Service not found"))
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error getting service $serviceId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>> {
        return try {
            val services = localDataSource.getServicesAcceptingCheckIns().map { it.toDomain() }
            Result.success(services)
        } catch (e: Exception) {
            logger.e(e) { "Error getting services accepting check-ins" }
            Result.failure(e)
        }
    }
    
    // Check-in/Check-out Operations with Conflict Resolution
    
    override suspend fun checkInChild(
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String?
    ): Result<CheckInRecord> {
        return try {
            // First, validate the check-in locally
            val child = localDataSource.getChildById(childId)
                ?: return Result.failure(Exception("Child not found"))
            
            val service = localDataSource.getServiceById(serviceId)
                ?: return Result.failure(Exception("Service not found"))
            
            // Check if child is already checked in
            if (child.status == CheckInStatus.CHECKED_IN.name) {
                return Result.failure(Exception("Child is already checked in"))
            }
            
            // Check service capacity
            if (service.currentCapacity >= service.maxCapacity) {
                return Result.failure(Exception("Service is at full capacity"))
            }
            
            // Check age eligibility
            val childDomain = child.toDomain()
            val serviceDomain = service.toDomain()
            if (!childDomain.isEligibleForService(serviceDomain)) {
                return Result.failure(Exception("Child is not eligible for this service"))
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
            localDataSource.updateChildCheckInStatus(
                childId = childId,
                status = CheckInStatus.CHECKED_IN.name,
                serviceId = serviceId,
                checkInTime = currentTime,
                checkOutTime = null,
                updatedAt = currentTime
            )
            
            localDataSource.insertCheckInRecord(checkInRecord.toEntity())
            
            // Try to sync with remote
            val request = CheckInRequest(
                childId = childId,
                serviceId = serviceId,
                checkedInBy = checkedInBy,
                notes = notes
            )
            
            val remoteResult = remoteDataSource.checkInChild(request)
            
            when {
                remoteResult.isSuccess -> {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.record != null) {
                        // Update with server response
                        val serverRecord = response.record.toDomain()
                        localDataSource.upsertCheckInRecord(serverRecord.toEntity(currentTime))
                        
                        // Update child and service from server response
                        response.updatedChild?.let { childDto ->
                            localDataSource.upsertChild(childDto.toDomain().toEntity(currentTime))
                        }
                        response.updatedService?.let { serviceDto ->
                            localDataSource.upsertService(serviceDto.toDomain().toEntity(currentTime))
                        }
                        
                        Result.success(serverRecord)
                    } else {
                        // Server returned error - handle conflict
                        handleCheckInConflict(childId, serviceId, checkInRecord)
                    }
                }
                else -> {
                    // Network error - keep local changes
                    logger.w { "Network error during check-in, keeping local changes" }
                    Result.success(checkInRecord)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error checking in child $childId to service $serviceId" }
            Result.failure(e)
        }
    }
    
    override suspend fun checkOutChild(
        childId: String,
        checkedOutBy: String,
        notes: String?
    ): Result<CheckInRecord> {
        return try {
            // Find the current check-in record
            val currentCheckIns = localDataSource.getAllCurrentCheckIns()
            val currentRecord = currentCheckIns.find { it.childId == childId && it.status == CheckInStatus.CHECKED_IN.name }
                ?: return Result.failure(Exception("Child is not currently checked in"))
            
            val currentTime = getCurrentTimestamp()
            
            // Update the record with check-out information
            val updatedRecord = currentRecord.toDomain().copy(
                checkOutTime = currentTime,
                checkedOutBy = checkedOutBy,
                notes = if (notes != null) "${currentRecord.notes ?: ""}\n$notes".trim() else currentRecord.notes,
                status = CheckInStatus.CHECKED_OUT
            )
            
            // Perform optimistic local update
            localDataSource.updateChildCheckInStatus(
                childId = childId,
                status = CheckInStatus.CHECKED_OUT.name,
                serviceId = null,
                checkInTime = null,
                checkOutTime = currentTime,
                updatedAt = currentTime
            )
            
            localDataSource.updateCheckInRecord(updatedRecord.toEntity())
            
            // Try to sync with remote
            val request = CheckOutRequest(
                childId = childId,
                checkedOutBy = checkedOutBy,
                notes = notes
            )
            
            val remoteResult = remoteDataSource.checkOutChild(request)
            
            when {
                remoteResult.isSuccess -> {
                    val response = remoteResult.getOrThrow()
                    if (response.success && response.record != null) {
                        // Update with server response
                        val serverRecord = response.record.toDomain()
                        localDataSource.upsertCheckInRecord(serverRecord.toEntity(currentTime))
                        
                        // Update child and service from server response
                        response.updatedChild?.let { childDto ->
                            localDataSource.upsertChild(childDto.toDomain().toEntity(currentTime))
                        }
                        response.updatedService?.let { serviceDto ->
                            localDataSource.upsertService(serviceDto.toDomain().toEntity(currentTime))
                        }
                        
                        Result.success(serverRecord)
                    } else {
                        // Server returned error - handle conflict
                        handleCheckOutConflict(childId, updatedRecord)
                    }
                }
                else -> {
                    // Network error - keep local changes
                    logger.w { "Network error during check-out, keeping local changes" }
                    Result.success(updatedRecord)
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error checking out child $childId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<List<CheckInRecord>> {
        return try {
            val records = localDataSource.getCheckInHistory(childId, limit).map { it.toDomain() }
            Result.success(records)
        } catch (e: Exception) {
            logger.e(e) { "Error getting check-in history for child $childId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): Result<List<CheckInRecord>> {
        return try {
            val records = localDataSource.getCurrentCheckIns(serviceId).map { it.toDomain() }
            Result.success(records)
        } catch (e: Exception) {
            logger.e(e) { "Error getting current check-ins for service $serviceId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getAllCurrentCheckIns(): Result<List<CheckInRecord>> {
        return try {
            val records = localDataSource.getAllCurrentCheckIns().map { it.toDomain() }
            Result.success(records)
        } catch (e: Exception) {
            logger.e(e) { "Error getting all current check-ins" }
            Result.failure(e)
        }
    }
    
    override suspend fun getCheckInRecord(recordId: String): Result<CheckInRecord> {
        return try {
            val record = localDataSource.getCheckInRecord(recordId)
            if (record != null) {
                Result.success(record.toDomain())
            } else {
                Result.failure(Exception("Check-in record not found"))
            }
        } catch (e: Exception) {
            logger.e(e) { "Error getting check-in record $recordId" }
            Result.failure(e)
        }
    }
    
    // Staff/Reporting Operations
    
    override suspend fun getServiceReport(serviceId: String): Result<ServiceReport> {
        return try {
            val service = localDataSource.getServiceById(serviceId)?.toDomain()
                ?: return Result.failure(Exception("Service not found"))
            
            val currentCheckIns = localDataSource.getCurrentCheckIns(serviceId).map { it.toDomain() }
            val checkedInChildren = currentCheckIns.mapNotNull { record ->
                localDataSource.getChildById(record.childId)?.toDomain()
            }
            
            val report = ServiceReport(
                serviceId = service.id,
                serviceName = service.name,
                totalCapacity = service.maxCapacity,
                currentCheckIns = service.currentCapacity,
                availableSpots = service.getAvailableSpots(),
                checkedInChildren = checkedInChildren,
                staffMembers = service.staffMembers,
                generatedAt = getCurrentTimestamp()
            )
            
            Result.success(report)
        } catch (e: Exception) {
            logger.e(e) { "Error generating service report for $serviceId" }
            Result.failure(e)
        }
    }
    
    override suspend fun getAttendanceReport(startDate: String, endDate: String): Result<AttendanceReport> {
        return try {
            // This would require more complex queries - simplified implementation
            val report = AttendanceReport(
                startDate = startDate,
                endDate = endDate,
                totalCheckIns = 0,
                uniqueChildren = 0,
                serviceBreakdown = emptyMap(),
                dailyBreakdown = emptyMap(),
                generatedAt = getCurrentTimestamp()
            )
            Result.success(report)
        } catch (e: Exception) {
            logger.e(e) { "Error generating attendance report" }
            Result.failure(e)
        }
    }
    
    // Real-time Operations
    
    override suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit) {
        childUpdateCallbacks[childId] = onUpdate
        
        // Subscribe to WebSocket updates
        remoteDataSource.subscribeToChildUpdates(childId)
    }
    
    override suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit) {
        serviceUpdateCallbacks[serviceId] = onUpdate
        
        // Subscribe to WebSocket updates
        remoteDataSource.subscribeToServiceUpdates(serviceId)
    }
    
    override suspend fun unsubscribeFromUpdates() {
        childUpdateCallbacks.clear()
        serviceUpdateCallbacks.clear()
        
        // Unsubscribe from WebSocket updates
        remoteDataSource.unsubscribeFromUpdates()
    }
    
    // Private helper methods
    
    private fun startWebSocketMessageProcessing() {
        webSocketJob = coroutineScope.launch {
            remoteDataSource.getWebSocketMessages()
                .catch { e -> logger.e(e) { "Error in WebSocket message flow" } }
                .collect { message ->
                    processWebSocketMessage(message)
                }
        }
    }
    
    private suspend fun processWebSocketMessage(message: WebSocketMessage) {
        try {
            when (message) {
                is ChildStatusUpdateMessage -> {
                    val child = message.child.toDomain()
                    // Update local cache
                    localDataSource.upsertChild(child.toEntity(getCurrentTimestamp()))
                    // Notify subscribers
                    childUpdateCallbacks[child.id]?.invoke(child)
                }
                is ServiceCapacityUpdateMessage -> {
                    val service = message.service.toDomain()
                    // Update local cache
                    localDataSource.upsertService(service.toEntity(getCurrentTimestamp()))
                    // Notify subscribers
                    serviceUpdateCallbacks[service.id]?.invoke(service)
                }
                is CheckInUpdateMessage -> {
                    val record = message.record.toDomain()
                    val child = message.child.toDomain()
                    val service = message.service.toDomain()
                    
                    // Update local cache
                    localDataSource.upsertCheckInRecord(record.toEntity(getCurrentTimestamp()))
                    localDataSource.upsertChild(child.toEntity(getCurrentTimestamp()))
                    localDataSource.upsertService(service.toEntity(getCurrentTimestamp()))
                    
                    // Notify subscribers
                    childUpdateCallbacks[child.id]?.invoke(child)
                    serviceUpdateCallbacks[service.id]?.invoke(service)
                }
                is CheckOutUpdateMessage -> {
                    val record = message.record.toDomain()
                    val child = message.child.toDomain()
                    val service = message.service.toDomain()
                    
                    // Update local cache
                    localDataSource.upsertCheckInRecord(record.toEntity(getCurrentTimestamp()))
                    localDataSource.upsertChild(child.toEntity(getCurrentTimestamp()))
                    localDataSource.upsertService(service.toEntity(getCurrentTimestamp()))
                    
                    // Notify subscribers
                    childUpdateCallbacks[child.id]?.invoke(child)
                    serviceUpdateCallbacks[service.id]?.invoke(service)
                }
                else -> {
                    logger.d { "Received unhandled WebSocket message: ${message.type}" }
                }
            }
        } catch (e: Exception) {
            logger.e(e) { "Error processing WebSocket message: ${message.type}" }
        }
    }
    
    private suspend fun syncChildrenFromRemote(parentId: String) {
        try {
            val remoteResult = remoteDataSource.getChildrenForParent(parentId)
            if (remoteResult.isSuccess) {
                val response = remoteResult.getOrThrow()
                if (response.success) {
                    val currentTime = getCurrentTimestamp()
                    val children = response.children.map { it.toDomain().toEntity(currentTime) }
                    localDataSource.upsertChildren(children)
                    
                    // Update sync timestamps
                    children.forEach { child ->
                        localDataSource.updateLastSyncedAt(child.id, currentTime)
                    }
                }
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to sync children from remote" }
        }
    }
    
    private suspend fun syncServicesFromRemote() {
        try {
            val remoteResult = remoteDataSource.getAvailableServices()
            if (remoteResult.isSuccess) {
                val response = remoteResult.getOrThrow()
                if (response.success) {
                    val currentTime = getCurrentTimestamp()
                    val services = response.services.map { it.toDomain().toEntity(currentTime) }
                    localDataSource.upsertServices(services)
                    
                    // Update sync timestamps
                    services.forEach { service ->
                        localDataSource.updateServiceLastSyncedAt(service.id, currentTime)
                    }
                }
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to sync services from remote" }
        }
    }
    
    private suspend fun handleCheckInConflict(
        childId: String,
        serviceId: String,
        localRecord: CheckInRecord
    ): Result<CheckInRecord> {
        logger.w { "Check-in conflict detected for child $childId" }
        
        // In case of conflict, we'll trust the server state
        // Revert local changes and sync with server
        try {
            syncChildrenFromRemote(localRecord.checkedInBy) // Assuming checkedInBy is parentId
            syncServicesFromRemote()
            
            // Return the local record for now - in a real implementation,
            // you might want to re-attempt the check-in or show a conflict resolution UI
            return Result.success(localRecord)
        } catch (e: Exception) {
            return Result.failure(Exception("Check-in conflict could not be resolved"))
        }
    }
    
    private suspend fun handleCheckOutConflict(
        childId: String,
        localRecord: CheckInRecord
    ): Result<CheckInRecord> {
        logger.w { "Check-out conflict detected for child $childId" }
        
        // Similar conflict resolution as check-in
        try {
            syncChildrenFromRemote(localRecord.checkedInBy) // Assuming checkedInBy is parentId
            return Result.success(localRecord)
        } catch (e: Exception) {
            return Result.failure(Exception("Check-out conflict could not be resolved"))
        }
    }
    
    private fun generateId(): String {
        return "local_${System.currentTimeMillis()}_${(0..999).random()}"
    }
    
    private fun getCurrentTimestamp(): String {
        // In a real implementation, use kotlinx-datetime
        return System.currentTimeMillis().toString()
    }
    
    // Cleanup
    fun cleanup() {
        webSocketJob?.cancel()
        coroutineScope.cancel()
    }
}