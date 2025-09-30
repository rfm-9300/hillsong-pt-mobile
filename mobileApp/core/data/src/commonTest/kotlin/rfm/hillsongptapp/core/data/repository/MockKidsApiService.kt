package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.network.api.KidsApiService
import rfm.hillsongptapp.core.network.ktor.requests.AttendanceReportRequest
import rfm.hillsongptapp.core.network.ktor.requests.CheckInRequest
import rfm.hillsongptapp.core.network.ktor.requests.CheckOutRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildRegistrationRequest
import rfm.hillsongptapp.core.network.ktor.requests.ChildUpdateRequest
import rfm.hillsongptapp.core.network.ktor.requests.PaginationRequest
import rfm.hillsongptapp.core.network.ktor.requests.ServiceFilterRequest
import rfm.hillsongptapp.core.network.ktor.responses.AttendanceReportApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInHistoryApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckOutApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ChildrenApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CurrentCheckInsApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServiceApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.ServicesApiResponse
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Mock implementation of KidsApiService for testing
 */
class MockKidsApiService : KidsApiService {
    
    var shouldThrowError = false
    
    // Configurable results for different operations
    var registerChildResult: NetworkResult<ChildApiResponse> = NetworkResult.Error("Not configured")
    var getChildResult: NetworkResult<ChildApiResponse> = NetworkResult.Error("Not configured")
    var getChildrenResult: NetworkResult<ChildrenApiResponse> = NetworkResult.Error("Not configured")
    var childrenByParentResult: NetworkResult<ChildrenApiResponse> = NetworkResult.Error("Not configured")
    var updateChildResult: NetworkResult<ChildApiResponse> = NetworkResult.Error("Not configured")
    var deleteChildResult: NetworkResult<ChildApiResponse> = NetworkResult.Error("Not configured")
    
    var getServicesResult: NetworkResult<ServicesApiResponse> = NetworkResult.Error("Not configured")
    var getServiceResult: NetworkResult<ServiceApiResponse> = NetworkResult.Error("Not configured")
    var getAvailableServicesResult: NetworkResult<ServicesApiResponse> = NetworkResult.Error("Not configured")
    
    var checkInChildResult: NetworkResult<CheckInApiResponse> = NetworkResult.Error("Not configured")
    var checkOutChildResult: NetworkResult<CheckOutApiResponse> = NetworkResult.Error("Not configured")
    var getCurrentCheckInsResult: NetworkResult<CurrentCheckInsApiResponse> = NetworkResult.Error("Not configured")
    var getCheckInHistoryResult: NetworkResult<CheckInHistoryApiResponse> = NetworkResult.Error("Not configured")
    
    var getAttendanceReportResult: NetworkResult<AttendanceReportApiResponse> = NetworkResult.Error("Not configured")
    var getChildAttendanceHistoryResult: NetworkResult<CheckInHistoryApiResponse> = NetworkResult.Error("Not configured")
    var getServiceAttendanceHistoryResult: NetworkResult<CheckInHistoryApiResponse> = NetworkResult.Error("Not configured")
    
    // Track method calls for verification
    val registerChildCalls = mutableListOf<ChildRegistrationRequest>()
    val getChildCalls = mutableListOf<String>()
    val getChildrenCalls = mutableListOf<PaginationRequest?>()
    val getChildrenByParentCalls = mutableListOf<Pair<String, PaginationRequest?>>()
    val updateChildCalls = mutableListOf<Pair<String, ChildUpdateRequest>>()
    val deleteChildCalls = mutableListOf<String>()
    
    val getServicesCalls = mutableListOf<ServiceFilterRequest?>()
    val getServiceCalls = mutableListOf<String>()
    val getAvailableServicesCalls = mutableListOf<Int>()
    
    val checkInChildCalls = mutableListOf<CheckInRequest>()
    val checkOutChildCalls = mutableListOf<CheckOutRequest>()
    val getCurrentCheckInsCalls = mutableListOf<String?>()
    val getCheckInHistoryCalls = mutableListOf<Triple<String?, String?, PaginationRequest?>>()
    
    val getAttendanceReportCalls = mutableListOf<AttendanceReportRequest>()
    val getChildAttendanceHistoryCalls = mutableListOf<Pair<String, PaginationRequest?>>()
    val getServiceAttendanceHistoryCalls = mutableListOf<Pair<String, PaginationRequest?>>()
    
    private fun checkError() {
        if (shouldThrowError) throw RuntimeException("Mock API service error")
    }
    
    // Child Management Operations
    override suspend fun registerChild(request: ChildRegistrationRequest): NetworkResult<ChildApiResponse> {
        checkError()
        registerChildCalls.add(request)
        return registerChildResult
    }
    
    override suspend fun getChild(childId: String): NetworkResult<ChildApiResponse> {
        checkError()
        getChildCalls.add(childId)
        return getChildResult
    }
    
    override suspend fun getChildren(pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        checkError()
        getChildrenCalls.add(pagination)
        return getChildrenResult
    }
    
    override suspend fun getChildrenByParent(parentId: String, pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        checkError()
        getChildrenByParentCalls.add(parentId to pagination)
        return childrenByParentResult
    }
    
    override suspend fun updateChild(childId: String, request: ChildUpdateRequest): NetworkResult<ChildApiResponse> {
        checkError()
        updateChildCalls.add(childId to request)
        return updateChildResult
    }
    
    override suspend fun deleteChild(childId: String): NetworkResult<ChildApiResponse> {
        checkError()
        deleteChildCalls.add(childId)
        return deleteChildResult
    }
    
    // Service Management Operations
    override suspend fun getServices(filter: ServiceFilterRequest?): NetworkResult<ServicesApiResponse> {
        checkError()
        getServicesCalls.add(filter)
        return getServicesResult
    }
    
    override suspend fun getService(serviceId: String): NetworkResult<ServiceApiResponse> {
        checkError()
        getServiceCalls.add(serviceId)
        return getServiceResult
    }
    
    override suspend fun getAvailableServices(childAge: Int): NetworkResult<ServicesApiResponse> {
        checkError()
        getAvailableServicesCalls.add(childAge)
        return getAvailableServicesResult
    }
    
    // Check-in/Check-out Operations
    override suspend fun checkInChild(request: CheckInRequest): NetworkResult<CheckInApiResponse> {
        checkError()
        checkInChildCalls.add(request)
        return checkInChildResult
    }
    
    override suspend fun checkOutChild(request: CheckOutRequest): NetworkResult<CheckOutApiResponse> {
        checkError()
        checkOutChildCalls.add(request)
        return checkOutChildResult
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String?): NetworkResult<CurrentCheckInsApiResponse> {
        checkError()
        getCurrentCheckInsCalls.add(serviceId)
        return getCurrentCheckInsResult
    }
    
    override suspend fun getCheckInHistory(
        childId: String?,
        serviceId: String?,
        pagination: PaginationRequest?
    ): NetworkResult<CheckInHistoryApiResponse> {
        checkError()
        getCheckInHistoryCalls.add(Triple(childId, serviceId, pagination))
        return getCheckInHistoryResult
    }
    
    // Reporting Operations
    override suspend fun getAttendanceReport(request: AttendanceReportRequest): NetworkResult<AttendanceReportApiResponse> {
        checkError()
        getAttendanceReportCalls.add(request)
        return getAttendanceReportResult
    }
    
    override suspend fun getChildAttendanceHistory(childId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        checkError()
        getChildAttendanceHistoryCalls.add(childId to pagination)
        return getChildAttendanceHistoryResult
    }
    
    override suspend fun getServiceAttendanceHistory(serviceId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        checkError()
        getServiceAttendanceHistoryCalls.add(serviceId to pagination)
        return getServiceAttendanceHistoryResult
    }
}