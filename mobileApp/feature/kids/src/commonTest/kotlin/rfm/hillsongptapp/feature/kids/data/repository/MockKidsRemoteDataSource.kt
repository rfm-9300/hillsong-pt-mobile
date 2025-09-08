package rfm.hillsongptapp.feature.kids.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import rfm.hillsongptapp.feature.kids.data.network.datasource.KidsRemoteDataSource
import rfm.hillsongptapp.feature.kids.data.network.dto.*
import rfm.hillsongptapp.feature.kids.data.network.websocket.WebSocketMessage

/**
 * Mock implementation of KidsRemoteDataSource for testing
 */
class MockKidsRemoteDataSource : KidsRemoteDataSource {
    
    // Test configuration flags
    var shouldFailGetChildren = false
    var shouldFailRegisterChild = false
    var shouldFailUpdateChild = false
    var shouldFailCheckIn = false
    var shouldFailCheckOut = false
    
    // Mock responses
    var childrenResponse: ChildrenResponse? = null
    var childResponse: ChildResponse? = null
    var servicesResponse: ServicesResponse? = null
    var serviceResponse: ServiceResponse? = null
    var checkInResponse: CheckInResponse? = null
    var checkOutResponse: CheckOutResponse? = null
    var serviceReportResponse: ServiceReportResponse? = null
    var attendanceReportResponse: AttendanceReportResponse? = null
    var deleteChildResult: Result<Unit>? = null
    
    // Track operations for verification
    val subscribedChildIds = mutableSetOf<String>()
    val subscribedServiceIds = mutableSetOf<String>()
    var unsubscribeCalled = false
    var webSocketConnected = false
    
    // Child Management Operations
    
    override suspend fun getChildrenForParent(parentId: String): Result<ChildrenResponse> {
        return if (shouldFailGetChildren) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(childrenResponse ?: ChildrenResponse(success = true, children = emptyList()))
        }
    }
    
    override suspend fun registerChild(request: ChildRegistrationRequest): Result<ChildResponse> {
        return if (shouldFailRegisterChild) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(childResponse ?: ChildResponse(success = true))
        }
    }
    
    override suspend fun updateChild(childId: String, request: ChildUpdateRequest): Result<ChildResponse> {
        return if (shouldFailUpdateChild) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(childResponse ?: ChildResponse(success = true))
        }
    }
    
    override suspend fun deleteChild(childId: String): Result<Unit> {
        return deleteChildResult ?: Result.success(Unit)
    }
    
    override suspend fun getChildById(childId: String): Result<ChildResponse> {
        return Result.success(childResponse ?: ChildResponse(success = true))
    }
    
    // Service Management Operations
    
    override suspend fun getAvailableServices(): Result<ServicesResponse> {
        return Result.success(servicesResponse ?: ServicesResponse(success = true, services = emptyList()))
    }
    
    override suspend fun getServicesForAge(age: Int): Result<ServicesResponse> {
        return Result.success(servicesResponse ?: ServicesResponse(success = true, services = emptyList()))
    }
    
    override suspend fun getServiceById(serviceId: String): Result<ServiceResponse> {
        return Result.success(serviceResponse ?: ServiceResponse(success = true))
    }
    
    override suspend fun getServicesAcceptingCheckIns(): Result<ServicesResponse> {
        return Result.success(servicesResponse ?: ServicesResponse(success = true, services = emptyList()))
    }
    
    // Check-in/Check-out Operations
    
    override suspend fun checkInChild(request: CheckInRequest): Result<CheckInResponse> {
        return if (shouldFailCheckIn) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(checkInResponse ?: CheckInResponse(success = true))
        }
    }
    
    override suspend fun checkOutChild(request: CheckOutRequest): Result<CheckOutResponse> {
        return if (shouldFailCheckOut) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(checkOutResponse ?: CheckOutResponse(success = true))
        }
    }
    
    override suspend fun getCheckInHistory(childId: String, limit: Int?): Result<CheckInHistoryResponse> {
        return Result.success(CheckInHistoryResponse(success = true, records = emptyList()))
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String): Result<CurrentCheckInsResponse> {
        return Result.success(CurrentCheckInsResponse(success = true, records = emptyList()))
    }
    
    override suspend fun getAllCurrentCheckIns(): Result<CurrentCheckInsResponse> {
        return Result.success(CurrentCheckInsResponse(success = true, records = emptyList()))
    }
    
    // Staff/Reporting Operations
    
    override suspend fun getServiceReport(serviceId: String): Result<ServiceReportResponse> {
        return Result.success(serviceReportResponse ?: ServiceReportResponse(success = true))
    }
    
    override suspend fun getAttendanceReport(request: AttendanceReportRequest): Result<AttendanceReportResponse> {
        return Result.success(attendanceReportResponse ?: AttendanceReportResponse(success = true))
    }
    
    // Real-time WebSocket Operations
    
    override suspend fun connectWebSocket(): Result<Unit> {
        webSocketConnected = true
        return Result.success(Unit)
    }
    
    override suspend fun disconnectWebSocket() {
        webSocketConnected = false
    }
    
    override suspend fun subscribeToChildUpdates(childId: String): Result<Unit> {
        subscribedChildIds.add(childId)
        return Result.success(Unit)
    }
    
    override suspend fun subscribeToServiceUpdates(serviceId: String): Result<Unit> {
        subscribedServiceIds.add(serviceId)
        return Result.success(Unit)
    }
    
    override suspend fun unsubscribeFromUpdates(): Result<Unit> {
        unsubscribeCalled = true
        subscribedChildIds.clear()
        subscribedServiceIds.clear()
        return Result.success(Unit)
    }
    
    override fun getWebSocketMessages(): Flow<WebSocketMessage> {
        return flowOf() // Empty flow for testing
    }
    
    override fun isWebSocketConnected(): Boolean {
        return webSocketConnected
    }
}