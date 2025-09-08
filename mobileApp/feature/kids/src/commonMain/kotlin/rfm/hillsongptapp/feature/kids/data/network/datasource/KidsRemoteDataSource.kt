package rfm.hillsongptapp.feature.kids.data.network.datasource

import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.WebSocketMessage
import kotlinx.coroutines.flow.Flow

/**
 * Interface for remote data source operations for kids management
 * Handles all network communication including REST API calls and WebSocket connections
 */
interface KidsRemoteDataSource {
    
    // Child Management Operations
    
    /**
     * Get all children for a specific parent from the remote server
     * @param parentId The ID of the parent
     * @return Result containing ChildrenResponse or error
     */
    suspend fun getChildrenForParent(parentId: String): Result<ChildrenResponse>
    
    /**
     * Register a new child on the remote server
     * @param request The child registration request
     * @return Result containing ChildResponse or error
     */
    suspend fun registerChild(request: ChildRegistrationRequest): Result<ChildResponse>
    
    /**
     * Update an existing child's information on the remote server
     * @param childId The ID of the child to update
     * @param request The child update request
     * @return Result containing ChildResponse or error
     */
    suspend fun updateChild(childId: String, request: ChildUpdateRequest): Result<ChildResponse>
    
    /**
     * Delete a child from the remote server
     * @param childId The ID of the child to delete
     * @return Result indicating success or error
     */
    suspend fun deleteChild(childId: String): Result<Unit>
    
    /**
     * Get a specific child by ID from the remote server
     * @param childId The ID of the child
     * @return Result containing ChildResponse or error
     */
    suspend fun getChildById(childId: String): Result<ChildResponse>
    
    // Service Management Operations
    
    /**
     * Get all available kids services from the remote server
     * @return Result containing ServicesResponse or error
     */
    suspend fun getAvailableServices(): Result<ServicesResponse>
    
    /**
     * Get services appropriate for a specific age from the remote server
     * @param age The age to filter services by
     * @return Result containing ServicesResponse or error
     */
    suspend fun getServicesForAge(age: Int): Result<ServicesResponse>
    
    /**
     * Get a specific service by ID from the remote server
     * @param serviceId The ID of the service
     * @return Result containing ServiceResponse or error
     */
    suspend fun getServiceById(serviceId: String): Result<ServiceResponse>
    
    /**
     * Get services that are currently accepting check-ins from the remote server
     * @return Result containing ServicesResponse or error
     */
    suspend fun getServicesAcceptingCheckIns(): Result<ServicesResponse>
    
    // Check-in/Check-out Operations
    
    /**
     * Check a child into a service on the remote server
     * @param request The check-in request
     * @return Result containing CheckInResponse or error
     */
    suspend fun checkInChild(request: CheckInRequest): Result<CheckInResponse>
    
    /**
     * Check a child out of their current service on the remote server
     * @param request The check-out request
     * @return Result containing CheckOutResponse or error
     */
    suspend fun checkOutChild(request: CheckOutRequest): Result<CheckOutResponse>
    
    /**
     * Get check-in history for a specific child from the remote server
     * @param childId The ID of the child
     * @param limit Optional limit on number of records to return
     * @return Result containing CheckInHistoryResponse or error
     */
    suspend fun getCheckInHistory(childId: String, limit: Int? = null): Result<CheckInHistoryResponse>
    
    /**
     * Get current check-ins for a specific service from the remote server
     * @param serviceId The ID of the service
     * @return Result containing CurrentCheckInsResponse or error
     */
    suspend fun getCurrentCheckIns(serviceId: String): Result<CurrentCheckInsResponse>
    
    /**
     * Get all current check-ins across all services from the remote server
     * @return Result containing CurrentCheckInsResponse or error
     */
    suspend fun getAllCurrentCheckIns(): Result<CurrentCheckInsResponse>
    
    // Staff/Reporting Operations
    
    /**
     * Get service report with attendance and capacity information from the remote server
     * @param serviceId The ID of the service
     * @return Result containing ServiceReportResponse or error
     */
    suspend fun getServiceReport(serviceId: String): Result<ServiceReportResponse>
    
    /**
     * Get attendance report for a date range from the remote server
     * @param request The attendance report request
     * @return Result containing AttendanceReportResponse or error
     */
    suspend fun getAttendanceReport(request: AttendanceReportRequest): Result<AttendanceReportResponse>
    
    // Real-time WebSocket Operations
    
    /**
     * Connect to the WebSocket for real-time updates
     * @return Result indicating success or error
     */
    suspend fun connectWebSocket(): Result<Unit>
    
    /**
     * Disconnect from the WebSocket
     */
    suspend fun disconnectWebSocket()
    
    /**
     * Subscribe to real-time updates for a specific child
     * @param childId The ID of the child to monitor
     * @return Result indicating success or error
     */
    suspend fun subscribeToChildUpdates(childId: String): Result<Unit>
    
    /**
     * Subscribe to real-time updates for a specific service
     * @param serviceId The ID of the service to monitor
     * @return Result indicating success or error
     */
    suspend fun subscribeToServiceUpdates(serviceId: String): Result<Unit>
    
    /**
     * Unsubscribe from all real-time updates
     * @return Result indicating success or error
     */
    suspend fun unsubscribeFromUpdates(): Result<Unit>
    
    /**
     * Get a flow of WebSocket messages for real-time updates
     * @return Flow of WebSocketMessage objects
     */
    fun getWebSocketMessages(): Flow<WebSocketMessage>
    
    /**
     * Check if WebSocket is currently connected
     * @return Boolean indicating connection status
     */
    fun isWebSocketConnected(): Boolean
}