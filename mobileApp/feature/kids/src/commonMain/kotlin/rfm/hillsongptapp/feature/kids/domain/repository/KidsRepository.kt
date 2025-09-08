package rfm.hillsongptapp.feature.kids.domain.repository

import rfm.hillsongptapp.feature.kids.domain.model.AttendanceReport
import rfm.hillsongptapp.feature.kids.domain.model.Child
import rfm.hillsongptapp.feature.kids.domain.model.CheckInRecord
import rfm.hillsongptapp.feature.kids.domain.model.KidsService
import rfm.hillsongptapp.feature.kids.domain.model.ServiceReport

/**
 * Repository interface for kids management operations
 * Provides access to child, service, and check-in/check-out data
 */
interface KidsRepository {
    
    // Child Management Operations
    
    /**
     * Get all children for a specific parent
     * @param parentId The ID of the parent
     * @return Result containing list of children or error
     */
    suspend fun getChildrenForParent(parentId: String): Result<List<Child>>
    
    /**
     * Register a new child in the system
     * @param child The child to register
     * @return Result containing the registered child with assigned ID or error
     */
    suspend fun registerChild(child: Child): Result<Child>
    
    /**
     * Update an existing child's information
     * @param child The child with updated information
     * @return Result containing the updated child or error
     */
    suspend fun updateChild(child: Child): Result<Child>
    
    /**
     * Delete a child from the system
     * @param childId The ID of the child to delete
     * @return Result indicating success or error
     */
    suspend fun deleteChild(childId: String): Result<Unit>
    
    /**
     * Get a specific child by ID
     * @param childId The ID of the child
     * @return Result containing the child or error
     */
    suspend fun getChildById(childId: String): Result<Child>
    
    // Service Management Operations
    
    /**
     * Get all available kids services
     * @return Result containing list of services or error
     */
    suspend fun getAvailableServices(): Result<List<KidsService>>
    
    /**
     * Get services appropriate for a specific age
     * @param age The age to filter services by
     * @return Result containing list of age-appropriate services or error
     */
    suspend fun getServicesForAge(age: Int): Result<List<KidsService>>
    
    /**
     * Get a specific service by ID
     * @param serviceId The ID of the service
     * @return Result containing the service or error
     */
    suspend fun getServiceById(serviceId: String): Result<KidsService>
    
    /**
     * Get services that are currently accepting check-ins
     * @return Result containing list of services accepting check-ins or error
     */
    suspend fun getServicesAcceptingCheckIns(): Result<List<KidsService>>
    
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
    ): Result<CheckInRecord>
    
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
    ): Result<CheckInRecord>
    
    /**
     * Get check-in history for a specific child
     * @param childId The ID of the child
     * @param limit Optional limit on number of records to return
     * @return Result containing list of check-in records or error
     */
    suspend fun getCheckInHistory(childId: String, limit: Int? = null): Result<List<CheckInRecord>>
    
    /**
     * Get current check-ins for a specific service
     * @param serviceId The ID of the service
     * @return Result containing list of current check-in records or error
     */
    suspend fun getCurrentCheckIns(serviceId: String): Result<List<CheckInRecord>>
    
    /**
     * Get all current check-ins across all services
     * @return Result containing list of all current check-in records or error
     */
    suspend fun getAllCurrentCheckIns(): Result<List<CheckInRecord>>
    
    /**
     * Get a specific check-in record by ID
     * @param recordId The ID of the check-in record
     * @return Result containing the check-in record or error
     */
    suspend fun getCheckInRecord(recordId: String): Result<CheckInRecord>
    
    // Staff/Reporting Operations
    
    /**
     * Get service report with attendance and capacity information
     * @param serviceId The ID of the service
     * @return Result containing service report data or error
     */
    suspend fun getServiceReport(serviceId: String): Result<ServiceReport>
    
    /**
     * Get attendance report for a date range
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     * @return Result containing attendance report or error
     */
    suspend fun getAttendanceReport(startDate: String, endDate: String): Result<AttendanceReport>
    
    // Real-time Operations
    
    /**
     * Subscribe to real-time updates for child status changes
     * @param childId The ID of the child to monitor
     * @param onUpdate Callback function for status updates
     */
    suspend fun subscribeToChildUpdates(childId: String, onUpdate: (Child) -> Unit)
    
    /**
     * Subscribe to real-time updates for service capacity changes
     * @param serviceId The ID of the service to monitor
     * @param onUpdate Callback function for capacity updates
     */
    suspend fun subscribeToServiceUpdates(serviceId: String, onUpdate: (KidsService) -> Unit)
    
    /**
     * Unsubscribe from real-time updates
     */
    suspend fun unsubscribeFromUpdates()
}



