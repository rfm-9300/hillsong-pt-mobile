package rfm.hillsongptapp.core.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.repository.database.ChildDao
import rfm.hillsongptapp.core.data.repository.database.CheckInRecordDao
import rfm.hillsongptapp.core.data.repository.database.KidsServiceDao
import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.result.NetworkResult
import rfm.hillsongptapp.core.network.ktor.responses.ChildResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceResponse
import kotlinx.coroutines.Job
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * Manages real-time updates for the Kids feature
 * Provides Flow-based reactive updates and handles connection management
 */
class KidsRealTimeManager(
    private val childDao: ChildDao,
    private val checkInRecordDao: CheckInRecordDao,
    private val kidsServiceDao: KidsServiceDao,
    private val kidsApiService: KidsApiService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    // Shared flows for broadcasting updates
    private val _childUpdates = MutableSharedFlow<KidsResult<Child>>(replay = 1)
    private val _serviceUpdates = MutableSharedFlow<KidsResult<KidsService>>(replay = 1)
    private val _checkInUpdates = MutableSharedFlow<KidsResult<List<CheckInRecord>>>(replay = 1)
    
    // Public flows for subscribers
    val childUpdates: Flow<KidsResult<Child>> = _childUpdates.asSharedFlow()
    val serviceUpdates: Flow<KidsResult<KidsService>> = _serviceUpdates.asSharedFlow()
    val checkInUpdates: Flow<KidsResult<List<CheckInRecord>>> = _checkInUpdates.asSharedFlow()
    
    // Active subscriptions tracking
    private val activeChildSubscriptions = mutableSetOf<String>()
    private val activeServiceSubscriptions = mutableSetOf<String>()
    
    // Polling intervals (in milliseconds)
    private val childPollingInterval = 30_000L // 30 seconds
    private val servicePollingInterval = 15_000L // 15 seconds
    private val checkInPollingInterval = 10_000L // 10 seconds
    
    /**
     * Subscribe to real-time updates for a specific child
     */
    fun subscribeToChildUpdates(childId: String): Flow<KidsResult<Child>> = flow {
        LoggerHelper.logDebug("Subscribing to child updates for $childId")
        
        // Add to active subscriptions
        activeChildSubscriptions.add(childId)
        
        try {
            // Emit initial data
            val initialChild = childDao.getChildById(childId)
            if (initialChild != null) {
                emit(KidsResult.Success(initialChild.toDomain()))
            } else {
                emit(KidsResult.Error("Child not found"))
                return@flow
            }
            
            // Start polling for updates
            while (activeChildSubscriptions.contains(childId)) {
                delay(childPollingInterval)
                
                try {
                    // Fetch latest data from API
                    val remoteResult = kidsApiService.getChild(childId)
                    when (remoteResult) {
                        is NetworkResult.Success -> {
                            val response = remoteResult.data
                            if (response.success && response.child != null) {
                                val childResponse = response.child!!
                                val updatedChild = childResponse.toDomain()
                                
                                // Update local cache
                                childDao.insertChild(updatedChild.toEntity())
                                
                                // Emit update
                                emit(KidsResult.Success(updatedChild))
                                
                                // Broadcast to other subscribers
                                _childUpdates.tryEmit(KidsResult.Success(updatedChild))
                            }
                        }
                        is NetworkResult.Error -> {
                            LoggerHelper.logError("Failed to fetch child updates: ${remoteResult.exception.message}")
                            // Emit local data as fallback
                            val localChild = childDao.getChildById(childId)
                            if (localChild != null) {
                                emit(KidsResult.Success(localChild.toDomain()))
                            }
                        }
                        is NetworkResult.Loading -> {
                            // Continue polling
                        }
                    }
                } catch (e: Exception) {
                    LoggerHelper.logError("Error during child polling for $childId", e)
                    // Continue polling despite errors
                }
            }
        } finally {
            // Clean up subscription
            activeChildSubscriptions.remove(childId)
            LoggerHelper.logDebug("Unsubscribed from child updates for $childId")
        }
    }.catch { e ->
        LoggerHelper.logError("Error in child updates stream for $childId", e)
        emit(KidsResult.Error("Stream error: ${e.message}"))
    }
    
    /**
     * Subscribe to real-time updates for a specific service
     */
    fun subscribeToServiceUpdates(serviceId: String): Flow<KidsResult<KidsService>> = flow {
        LoggerHelper.logDebug("Subscribing to service updates for $serviceId")
        
        // Add to active subscriptions
        activeServiceSubscriptions.add(serviceId)
        
        try {
            // Emit initial data
            val initialService = kidsServiceDao.getKidsServiceById(serviceId)
            if (initialService != null) {
                emit(KidsResult.Success(initialService.toDomain()))
            } else {
                emit(KidsResult.Error("Service not found"))
                return@flow
            }
            
            // Start polling for updates
            while (activeServiceSubscriptions.contains(serviceId)) {
                delay(servicePollingInterval)
                
                try {
                    // Fetch latest data from API
                    val remoteResult = kidsApiService.getService(serviceId)
                    when (remoteResult) {
                        is NetworkResult.Success -> {
                            val response = remoteResult.data
                            if (response.success && response.service != null) {
                                val serviceResponse = response.service!!
                                val updatedService = serviceResponse.toDomain()
                                
                                // Update local cache
                                kidsServiceDao.insertKidsService(updatedService.toEntity())
                                
                                // Emit update
                                emit(KidsResult.Success(updatedService))
                                
                                // Broadcast to other subscribers
                                _serviceUpdates.tryEmit(KidsResult.Success(updatedService))
                            }
                        }
                        is NetworkResult.Error -> {
                            LoggerHelper.logError("Failed to fetch service updates: ${remoteResult.exception.message}")
                            // Emit local data as fallback
                            val localService = kidsServiceDao.getKidsServiceById(serviceId)
                            if (localService != null) {
                                emit(KidsResult.Success(localService.toDomain()))
                            }
                        }
                        is NetworkResult.Loading -> {
                            // Continue polling
                        }
                    }
                } catch (e: Exception) {
                    LoggerHelper.logError("Error during service polling for $serviceId", e)
                    // Continue polling despite errors
                }
            }
        } finally {
            // Clean up subscription
            activeServiceSubscriptions.remove(serviceId)
            LoggerHelper.logDebug("Unsubscribed from service updates for $serviceId")
        }
    }.catch { e ->
        LoggerHelper.logError("Error in service updates stream for $serviceId", e)
        emit(KidsResult.Error("Stream error: ${e.message}"))
    }
    
    /**
     * Subscribe to real-time updates for current check-ins
     */
    fun subscribeToCurrentCheckInsUpdates(serviceId: String? = null): Flow<KidsResult<List<CheckInRecord>>> = flow {
        LoggerHelper.logDebug("Subscribing to check-in updates for service: ${serviceId ?: "all"}")
        
        try {
            // Emit initial data
            val initialRecords = if (serviceId != null) {
                checkInRecordDao.getActiveCheckInRecordsForService(serviceId).map { it.toDomain() }
            } else {
                checkInRecordDao.getCheckInRecordsByStatus(rfm.hillsongptapp.core.data.model.CheckInStatus.CHECKED_IN).map { it.toDomain() }
            }
            emit(KidsResult.Success(initialRecords))
            
            // Start polling for updates
            while (true) {
                delay(checkInPollingInterval)
                
                try {
                    // Fetch latest data from API
                    val remoteResult = kidsApiService.getCurrentCheckIns(serviceId)
                    when (remoteResult) {
                        is NetworkResult.Success -> {
                            val response = remoteResult.data
                            if (response.success) {
                                val updatedRecords = response.records.map { it.toDomain() }
                                
                                // Update local cache
                                updatedRecords.forEach { record ->
                                    checkInRecordDao.insertCheckInRecord(record.toEntity())
                                }
                                
                                // Emit update
                                emit(KidsResult.Success(updatedRecords))
                                
                                // Broadcast to other subscribers
                                _checkInUpdates.tryEmit(KidsResult.Success(updatedRecords))
                            }
                        }
                        is NetworkResult.Error -> {
                            LoggerHelper.logError("Failed to fetch check-in updates: ${remoteResult.exception.message}")
                            // Emit local data as fallback
                            val localRecords = if (serviceId != null) {
                                checkInRecordDao.getActiveCheckInRecordsForService(serviceId).map { it.toDomain() }
                            } else {
                                checkInRecordDao.getCheckInRecordsByStatus(rfm.hillsongptapp.core.data.model.CheckInStatus.CHECKED_IN).map { it.toDomain() }
                            }
                            emit(KidsResult.Success(localRecords))
                        }
                        is NetworkResult.Loading -> {
                            // Continue polling
                        }
                    }
                } catch (e: Exception) {
                    LoggerHelper.logError("Error during check-in polling", e)
                    // Continue polling despite errors
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error in check-in updates stream", e)
            emit(KidsResult.Error("Stream error: ${e.message}"))
        }
    }.catch { e ->
        LoggerHelper.logError("Error in check-in updates stream", e)
        emit(KidsResult.Error("Stream error: ${e.message}"))
    }
    
    /**
     * Manually trigger an update for a specific child
     */
    suspend fun triggerChildUpdate(childId: String) {
        try {
            val remoteResult = kidsApiService.getChild(childId)
            if (remoteResult is NetworkResult.Success) {
                val response = remoteResult.data
                if (response.success && response.child != null) {
                    val childResponse = response.child!!
                    val updatedChild = childResponse.toDomain()
                    childDao.insertChild(updatedChild.toEntity())
                    _childUpdates.tryEmit(KidsResult.Success(updatedChild))
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error triggering child update for $childId", e)
        }
    }
    
    /**
     * Manually trigger an update for a specific service
     */
    suspend fun triggerServiceUpdate(serviceId: String) {
        try {
            val remoteResult = kidsApiService.getService(serviceId)
            if (remoteResult is NetworkResult.Success) {
                val response = remoteResult.data
                if (response.success && response.service != null) {
                    val serviceResponse = response.service!!
                    val updatedService = serviceResponse.toDomain()
                    kidsServiceDao.insertKidsService(updatedService.toEntity())
                    _serviceUpdates.tryEmit(KidsResult.Success(updatedService))
                }
            }
        } catch (e: Exception) {
            LoggerHelper.logError("Error triggering service update for $serviceId", e)
        }
    }
    
    /**
     * Stop all active subscriptions and clean up resources
     */
    fun cleanup() {
        LoggerHelper.logDebug("Cleaning up KidsRealTimeManager")
        activeChildSubscriptions.clear()
        activeServiceSubscriptions.clear()
        // Note: coroutineScope.cancel() method doesn't exist, need to use job.cancel()
        // This should be handled by the parent scope
    }
    
    /**
     * Get the number of active subscriptions (for monitoring)
     */
    fun getActiveSubscriptionsCount(): Pair<Int, Int> {
        return Pair(activeChildSubscriptions.size, activeServiceSubscriptions.size)
    }
}

// Extension functions for converting between domain models and entities
// (These are duplicated from KidsRepositoryImpl for now - in a real implementation, 
// these would be moved to a shared mapper file)

private fun rfm.hillsongptapp.core.data.repository.database.ChildEntity.toDomain(): Child {
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

private fun Child.toEntity(): rfm.hillsongptapp.core.data.repository.database.ChildEntity {
    return rfm.hillsongptapp.core.data.repository.database.ChildEntity(
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

private fun rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity.toDomain(): KidsService {
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

private fun KidsService.toEntity(): rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity {
    return rfm.hillsongptapp.core.data.repository.database.KidsServiceEntity(
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

private fun rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity.toDomain(): CheckInRecord {
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

private fun CheckInRecord.toEntity(): rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity {
    return rfm.hillsongptapp.core.data.repository.database.CheckInRecordEntity(
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

// Extension functions for converting from response DTOs to domain models

private fun rfm.hillsongptapp.core.network.ktor.responses.ChildResponse.toDomain(): Child {
    return Child(
        id = id,
        parentId = parentId,
        name = name,
        dateOfBirth = dateOfBirth,
        medicalInfo = medicalInfo,
        dietaryRestrictions = dietaryRestrictions,
        emergencyContact = emergencyContact.toDomain(),
        status = rfm.hillsongptapp.core.data.model.CheckInStatus.valueOf(status),
        currentServiceId = currentServiceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun rfm.hillsongptapp.core.network.ktor.responses.EmergencyContactResponse.toDomain(): rfm.hillsongptapp.core.data.model.EmergencyContact {
    return rfm.hillsongptapp.core.data.model.EmergencyContact(
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship
    )
}

private fun rfm.hillsongptapp.core.network.ktor.responses.ServiceResponse.toDomain(): KidsService {
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

private fun rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse.toDomain(): CheckInRecord {
    return CheckInRecord(
        id = id,
        childId = childId,
        serviceId = serviceId,
        checkInTime = checkInTime,
        checkOutTime = checkOutTime,
        checkedInBy = checkedInBy,
        checkedOutBy = checkedOutBy,
        notes = notes,
        status = rfm.hillsongptapp.core.data.model.CheckInStatus.valueOf(status)
    )
}