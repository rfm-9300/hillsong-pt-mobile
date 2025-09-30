package rfm.hillsongptapp.core.data.repository

import kotlinx.coroutines.flow.Flow
import rfm.hillsongptapp.core.data.model.AttendanceReport
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.CheckInRecord
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.core.data.model.ServiceReport

/**
 * Repository interface for kids management operations
 * Provides access to child, service, and check-in/check-out data
 * Following the same pattern as PostRepository for consistency
 */
interface KidsRepository {
    
    // Child Management Operations
    
    /**
     * Get all children for a specific parent
     * @param parentId The ID of the parent
     * @return Result containing list of children or error
     */
    suspend fun getChildrenForParent(parentId: String): KidsResult<List<Child>>
    
    /**
     * Register a new child in the system
     * @param child The child to register
     * @return Result containing the registered child with assigned ID or error
     */
    suspend fun registerChild(child: Child): KidsResult<Child>
    
    /**
     * Update an existing child's information
     * @param child The child with updated information
     * @return Result containing the updated child or error
     */
    suspend fun updateChild(child: Child): KidsResult<Child>
    
    /**
     * Delete a child from the system
     * @param childId The ID of the child to delete
     * @return Result indicating success or error
     */
    suspend fun deleteChild(childId: String): KidsResult<Unit>
    
    /**
     * Get a specific child by ID
     * @param childId The ID of the child
     * @return Result containing the child or error
     */
    suspend fun getChildById(childId: String): KidsResult<Child>
    
    // Service Management Operations
    
    /**
     * Get all available kids services
     * @return Result containing list of services or error
     */
    suspend fun getAvailableServices(): KidsResult<List<KidsService>>
    
    /**
     * Get services appropriate for a specific age
     * @param age The age to filter services by
     * @return Result containing list of age-appropriate services or error
     */
    suspend fun getServicesForAge(age: Int): KidsResult<List<KidsService>>
    
    /**
     * Get a specific service by ID
     * @param serviceId The ID of the service
     * @return Result containing the service or error
     */
    suspend fun getServiceById(serviceId: String): KidsResult<KidsService>
    
    /**
     * Get services that are currently accepting check-ins
     * @return Result containing list of services accepting check-ins or error
     */
    suspend fun getServicesAcceptingCheckIns(): KidsResult<List<KidsService>>
    
    // Check-in/Check-out Operations
    
    /**
     * Check a child into a service
     * @param childId The ID of the child to check in
     * @param serviceId The ID of the service to check into
     * @param checkedInBy The ID of the user performing the check-in
     * @param notes Optional notes for the check-in
     * @return Result containing the check-in record or error
     */
    suspend fun checkInChild(
        childId: String, 
        serviceId: String, 
        checkedInBy: String,
        notes: String? = null
    ): KidsResult<CheckInRecord>
    
    /**
     * Check a child out of their current service
     * @param childId The ID of the child to check out
     * @param checkedOutBy The ID of the user performing the check-out
     * @param notes Optional notes for the check-out
     * @return Result containing the updated check-in record or error
     */
    suspend fun checkOutChild(
        childId: String, 
        checkedOutBy: String,
        notes: String? = null
    ): KidsResult<CheckInRecord>
    
    /**
     * Get check-in history for a specific child
     * @param childId The ID of the child
     * @param limit Optional limit on number of records to return
     * @return Result containing list of check-in records or error
     */
    suspend fun getCheckInHistory(childId: String, limit: Int? = null): KidsResult<List<CheckInRecord>>
    
    /**
     * Get current check-ins for a specific service
     * @param serviceId The ID of the service
     * @return Result containing list of current check-in records or error
     */
    suspend fun getCurrentCheckIns(serviceId: String): KidsResult<List<CheckInRecord>>
    
    /**
     * Get all current check-ins across all services
     * @return Result containing list of all current check-in records or error
     */
    suspend fun getAllCurrentCheckIns(): KidsResult<List<CheckInRecord>>
    
    /**
     * Get a specific check-in record by ID
     * @param recordId The ID of the check-in record
     * @return Result containing the check-in record or error
     */
    suspend fun getCheckInRecord(recordId: String): KidsResult<CheckInRecord>
    
    // Staff/Reporting Operations
    
    /**
     * Get service report with attendance and capacity information
     * @param serviceId The ID of the service
     * @return Result containing service report data or error
     */
    suspend fun getServiceReport(serviceId: String): KidsResult<ServiceReport>
    
    /**
     * Get attendance report for a date range
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     * @return Result containing attendance report or error
     */
    suspend fun getAttendanceReport(startDate: String, endDate: String): KidsResult<AttendanceReport>
    
    // Real-time Operations (Flow-based for reactive updates)
    
    /**
     * Get children for a parent as a reactive stream for real-time updates
     * @param parentId The ID of the parent
     * @return Flow of results containing list of children
     */
    fun getChildrenForParentStream(parentId: String): Flow<KidsResult<List<Child>>>
    
    /**
     * Get available services as a reactive stream for real-time updates
     * @return Flow of results containing list of services
     */
    fun getAvailableServicesStream(): Flow<KidsResult<List<KidsService>>>
    
    /**
     * Get current check-ins for a service as a reactive stream
     * @param serviceId The ID of the service
     * @return Flow of results containing list of current check-in records
     */
    fun getCurrentCheckInsStream(serviceId: String): Flow<KidsResult<List<CheckInRecord>>>
    
    /**
     * Get all current check-ins as a reactive stream
     * @return Flow of results containing list of all current check-in records
     */
    fun getAllCurrentCheckInsStream(): Flow<KidsResult<List<CheckInRecord>>>
    
    /**
     * Subscribe to real-time updates for child status changes
     * @param childId The ID of the child to monitor
     * @return Flow of child updates
     */
    fun subscribeToChildUpdates(childId: String): Flow<KidsResult<Child>>
    
    /**
     * Subscribe to real-time updates for service capacity changes
     * @param serviceId The ID of the service to monitor
     * @return Flow of service updates
     */
    fun subscribeToServiceUpdates(serviceId: String): Flow<KidsResult<KidsService>>
}