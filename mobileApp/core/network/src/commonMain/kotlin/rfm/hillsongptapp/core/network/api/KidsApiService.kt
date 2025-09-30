package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
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
 * Kids API service handling all kids-related network operations
 * Follows modern Android architecture patterns with proper error handling
 */
interface KidsApiService {
    
    // Child Management Operations
    suspend fun registerChild(request: ChildRegistrationRequest): NetworkResult<ChildApiResponse>
    suspend fun getChild(childId: String): NetworkResult<ChildApiResponse>
    suspend fun getChildren(pagination: PaginationRequest? = null): NetworkResult<ChildrenApiResponse>
    suspend fun getChildrenByParent(parentId: String, pagination: PaginationRequest? = null): NetworkResult<ChildrenApiResponse>
    suspend fun updateChild(childId: String, request: ChildUpdateRequest): NetworkResult<ChildApiResponse>
    suspend fun deleteChild(childId: String): NetworkResult<ChildApiResponse>
    
    // Service Management Operations
    suspend fun getServices(filter: ServiceFilterRequest? = null): NetworkResult<ServicesApiResponse>
    suspend fun getService(serviceId: String): NetworkResult<ServiceApiResponse>
    suspend fun getAvailableServices(childAge: Int): NetworkResult<ServicesApiResponse>
    
    // Check-in/Check-out Operations
    suspend fun checkInChild(request: CheckInRequest): NetworkResult<CheckInApiResponse>
    suspend fun checkOutChild(request: CheckOutRequest): NetworkResult<CheckOutApiResponse>
    suspend fun getCurrentCheckIns(serviceId: String? = null): NetworkResult<CurrentCheckInsApiResponse>
    suspend fun getCheckInHistory(
        childId: String? = null,
        serviceId: String? = null,
        pagination: PaginationRequest? = null
    ): NetworkResult<CheckInHistoryApiResponse>
    
    // Reporting Operations
    suspend fun getAttendanceReport(request: AttendanceReportRequest): NetworkResult<AttendanceReportApiResponse>
    suspend fun getChildAttendanceHistory(childId: String, pagination: PaginationRequest? = null): NetworkResult<CheckInHistoryApiResponse>
    suspend fun getServiceAttendanceHistory(serviceId: String, pagination: PaginationRequest? = null): NetworkResult<CheckInHistoryApiResponse>
}

/**
 * Implementation of KidsApiService using Ktor HTTP client
 */
class KidsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), KidsApiService {
    
    // Child Management Operations
    override suspend fun registerChild(request: ChildRegistrationRequest): NetworkResult<ChildApiResponse> {
        return safePost("api/kids/children", request)
    }
    
    override suspend fun getChild(childId: String): NetworkResult<ChildApiResponse> {
        return safeGet("api/kids/children/$childId")
    }
    
    override suspend fun getChildren(pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        return safeGet("api/kids/children") {
            pagination?.let {
                url.parameters.append("page", it.page.toString())
                url.parameters.append("pageSize", it.pageSize.toString())
            }
        }
    }
    
    override suspend fun getChildrenByParent(parentId: String, pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        return safeGet("api/kids/children/parent/$parentId") {
            pagination?.let {
                url.parameters.append("page", it.page.toString())
                url.parameters.append("pageSize", it.pageSize.toString())
            }
        }
    }
    
    override suspend fun updateChild(childId: String, request: ChildUpdateRequest): NetworkResult<ChildApiResponse> {
        return safePut("api/kids/children/$childId", request)
    }
    
    override suspend fun deleteChild(childId: String): NetworkResult<ChildApiResponse> {
        return safeDelete("api/kids/children/$childId")
    }
    
    // Service Management Operations
    override suspend fun getServices(filter: ServiceFilterRequest?): NetworkResult<ServicesApiResponse> {
        return safeGet("api/kids/services") {
            filter?.let { f ->
                f.minAge?.let { url.parameters.append("minAge", it.toString()) }
                f.maxAge?.let { url.parameters.append("maxAge", it.toString()) }
                f.acceptingCheckIns?.let { url.parameters.append("acceptingCheckIns", it.toString()) }
                f.location?.let { url.parameters.append("location", it) }
            }
        }
    }
    
    override suspend fun getService(serviceId: String): NetworkResult<ServiceApiResponse> {
        return safeGet("api/kids/services/$serviceId")
    }
    
    override suspend fun getAvailableServices(childAge: Int): NetworkResult<ServicesApiResponse> {
        return safeGet("api/kids/services/available") {
            url.parameters.append("age", childAge.toString())
        }
    }
    
    // Check-in/Check-out Operations
    override suspend fun checkInChild(request: CheckInRequest): NetworkResult<CheckInApiResponse> {
        return safePost("api/kids/checkin", request)
    }
    
    override suspend fun checkOutChild(request: CheckOutRequest): NetworkResult<CheckOutApiResponse> {
        return safePost("api/kids/checkout", request)
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String?): NetworkResult<CurrentCheckInsApiResponse> {
        return safeGet("api/kids/checkins/current") {
            serviceId?.let { url.parameters.append("serviceId", it) }
        }
    }
    
    override suspend fun getCheckInHistory(
        childId: String?,
        serviceId: String?,
        pagination: PaginationRequest?
    ): NetworkResult<CheckInHistoryApiResponse> {
        return safeGet("api/kids/checkins/history") {
            childId?.let { url.parameters.append("childId", it) }
            serviceId?.let { url.parameters.append("serviceId", it) }
            pagination?.let {
                url.parameters.append("page", it.page.toString())
                url.parameters.append("pageSize", it.pageSize.toString())
            }
        }
    }
    
    // Reporting Operations
    override suspend fun getAttendanceReport(request: AttendanceReportRequest): NetworkResult<AttendanceReportApiResponse> {
        return safePost("api/kids/reports/attendance", request)
    }
    
    override suspend fun getChildAttendanceHistory(childId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        return safeGet("api/kids/children/$childId/attendance") {
            pagination?.let {
                url.parameters.append("page", it.page.toString())
                url.parameters.append("pageSize", it.pageSize.toString())
            }
        }
    }
    
    override suspend fun getServiceAttendanceHistory(serviceId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        return safeGet("api/kids/services/$serviceId/attendance") {
            pagination?.let {
                url.parameters.append("page", it.page.toString())
                url.parameters.append("pageSize", it.pageSize.toString())
            }
        }
    }
}