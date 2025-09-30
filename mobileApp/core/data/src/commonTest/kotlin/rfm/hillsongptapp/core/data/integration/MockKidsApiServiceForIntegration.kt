package rfm.hillsongptapp.core.data.integration

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
 * Mock implementation of KidsApiService for integration testing
 * Simulates network behavior and can be configured to test different scenarios
 */
class MockKidsApiServiceForIntegration : KidsApiService {
    
    var shouldFailAllRequests = false
    var networkDelay = 0L // Simulate network delay in milliseconds
    
    // Configurable responses
    var childRegistrationResponse: ChildApiResponse? = null
    var childUpdateResponse: ChildApiResponse? = null
    var childDeleteResponse: ChildApiResponse? = null
    var childGetResponse: ChildApiResponse? = null
    var childrenGetResponse: ChildrenApiResponse? = null
    
    var servicesGetResponse: ServicesApiResponse? = null
    var serviceGetResponse: ServiceApiResponse? = null
    var availableServicesResponse: ServicesApiResponse? = null
    
    var checkInResponse: CheckInApiResponse? = null
    var checkOutResponse: CheckOutApiResponse? = null
    var currentCheckInsResponse: CurrentCheckInsApiResponse? = null
    var checkInHistoryResponse: CheckInHistoryApiResponse? = null
    
    var attendanceReportResponse: AttendanceReportApiResponse? = null
    
    private suspend fun simulateNetworkDelay() {
        if (networkDelay > 0) {
            kotlinx.coroutines.delay(networkDelay)
        }
    }
    
    private fun <T> handleRequest(response: T?): NetworkResult<T> {
        return when {
            shouldFailAllRequests -> NetworkResult.Error("Network unavailable")
            response != null -> NetworkResult.Success(response)
            else -> NetworkResult.Error("No response configured")
        }
    }
    
    // Child Management Operations
    override suspend fun registerChild(request: ChildRegistrationRequest): NetworkResult<ChildApiResponse> {
        simulateNetworkDelay()
        
        // Auto-generate a successful response if none configured
        val response = childRegistrationResponse ?: ChildApiResponse(
            success = true,
            child = createMockChildResponse(
                id = "generated-${System.currentTimeMillis()}",
                parentId = request.parentId,
                name = request.name,
                dateOfBirth = request.dateOfBirth,
                medicalInfo = request.medicalInfo,
                dietaryRestrictions = request.dietaryRestrictions,
                emergencyContact = request.emergencyContact
            ),
            message = "Child registered successfully"
        )
        
        return handleRequest(response)
    }
    
    override suspend fun getChild(childId: String): NetworkResult<ChildApiResponse> {
        simulateNetworkDelay()
        return handleRequest(childGetResponse)
    }
    
    override suspend fun getChildren(pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        simulateNetworkDelay()
        return handleRequest(childrenGetResponse)
    }
    
    override suspend fun getChildrenByParent(parentId: String, pagination: PaginationRequest?): NetworkResult<ChildrenApiResponse> {
        simulateNetworkDelay()
        return handleRequest(childrenGetResponse)
    }
    
    override suspend fun updateChild(childId: String, request: ChildUpdateRequest): NetworkResult<ChildApiResponse> {
        simulateNetworkDelay()
        
        val response = childUpdateResponse ?: ChildApiResponse(
            success = true,
            child = createMockChildResponse(
                id = childId,
                parentId = "parent-1", // Default parent ID
                name = request.name,
                dateOfBirth = request.dateOfBirth,
                medicalInfo = request.medicalInfo,
                dietaryRestrictions = request.dietaryRestrictions,
                emergencyContact = request.emergencyContact
            ),
            message = "Child updated successfully"
        )
        
        return handleRequest(response)
    }
    
    override suspend fun deleteChild(childId: String): NetworkResult<ChildApiResponse> {
        simulateNetworkDelay()
        
        val response = childDeleteResponse ?: ChildApiResponse(
            success = true,
            child = null,
            message = "Child deleted successfully"
        )
        
        return handleRequest(response)
    }
    
    // Service Management Operations
    override suspend fun getServices(filter: ServiceFilterRequest?): NetworkResult<ServicesApiResponse> {
        simulateNetworkDelay()
        return handleRequest(servicesGetResponse)
    }
    
    override suspend fun getService(serviceId: String): NetworkResult<ServiceApiResponse> {
        simulateNetworkDelay()
        return handleRequest(serviceGetResponse)
    }
    
    override suspend fun getAvailableServices(childAge: Int): NetworkResult<ServicesApiResponse> {
        simulateNetworkDelay()
        return handleRequest(availableServicesResponse)
    }
    
    // Check-in/Check-out Operations
    override suspend fun checkInChild(request: CheckInRequest): NetworkResult<CheckInApiResponse> {
        simulateNetworkDelay()
        
        val response = checkInResponse ?: CheckInApiResponse(
            success = true,
            record = createMockCheckInRecordResponse(
                id = "record-${System.currentTimeMillis()}",
                childId = request.childId,
                serviceId = request.serviceId,
                checkedInBy = request.checkedInBy,
                notes = request.notes
            ),
            updatedChild = null,
            updatedService = null,
            message = "Check-in successful"
        )
        
        return handleRequest(response)
    }
    
    override suspend fun checkOutChild(request: CheckOutRequest): NetworkResult<CheckOutApiResponse> {
        simulateNetworkDelay()
        
        val response = checkOutResponse ?: CheckOutApiResponse(
            success = true,
            record = createMockCheckInRecordResponse(
                id = "record-${System.currentTimeMillis()}",
                childId = request.childId,
                serviceId = "service-1", // Default service
                checkedInBy = "staff-1", // Default staff
                notes = request.notes,
                checkOutTime = "2024-01-07T11:30:00Z",
                checkedOutBy = request.checkedOutBy,
                status = "CHECKED_OUT"
            ),
            updatedChild = null,
            updatedService = null,
            message = "Check-out successful"
        )
        
        return handleRequest(response)
    }
    
    override suspend fun getCurrentCheckIns(serviceId: String?): NetworkResult<CurrentCheckInsApiResponse> {
        simulateNetworkDelay()
        return handleRequest(currentCheckInsResponse)
    }
    
    override suspend fun getCheckInHistory(
        childId: String?,
        serviceId: String?,
        pagination: PaginationRequest?
    ): NetworkResult<CheckInHistoryApiResponse> {
        simulateNetworkDelay()
        return handleRequest(checkInHistoryResponse)
    }
    
    // Reporting Operations
    override suspend fun getAttendanceReport(request: AttendanceReportRequest): NetworkResult<AttendanceReportApiResponse> {
        simulateNetworkDelay()
        return handleRequest(attendanceReportResponse)
    }
    
    override suspend fun getChildAttendanceHistory(childId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        simulateNetworkDelay()
        return handleRequest(checkInHistoryResponse)
    }
    
    override suspend fun getServiceAttendanceHistory(serviceId: String, pagination: PaginationRequest?): NetworkResult<CheckInHistoryApiResponse> {
        simulateNetworkDelay()
        return handleRequest(checkInHistoryResponse)
    }
    
    // Helper methods to create mock responses
    private fun createMockChildResponse(
        id: String,
        parentId: String,
        name: String,
        dateOfBirth: String,
        medicalInfo: String?,
        dietaryRestrictions: String?,
        emergencyContact: rfm.hillsongptapp.core.data.model.EmergencyContact
    ): rfm.hillsongptapp.core.network.ktor.responses.ChildResponse {
        return rfm.hillsongptapp.core.network.ktor.responses.ChildResponse(
            id = id,
            parentId = parentId,
            name = name,
            dateOfBirth = dateOfBirth,
            medicalInfo = medicalInfo,
            dietaryRestrictions = dietaryRestrictions,
            emergencyContact = rfm.hillsongptapp.core.network.ktor.responses.EmergencyContactResponse(
                name = emergencyContact.name,
                phoneNumber = emergencyContact.phoneNumber,
                relationship = emergencyContact.relationship
            ),
            status = "CHECKED_OUT",
            currentServiceId = null,
            checkInTime = null,
            checkOutTime = null,
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-01T10:00:00Z"
        )
    }
    
    private fun createMockCheckInRecordResponse(
        id: String,
        childId: String,
        serviceId: String,
        checkedInBy: String,
        notes: String?,
        checkOutTime: String? = null,
        checkedOutBy: String? = null,
        status: String = "CHECKED_IN"
    ): rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse {
        return rfm.hillsongptapp.core.network.ktor.responses.CheckInRecordResponse(
            id = id,
            childId = childId,
            serviceId = serviceId,
            checkInTime = "2024-01-07T10:00:00Z",
            checkOutTime = checkOutTime,
            checkedInBy = checkedInBy,
            checkedOutBy = checkedOutBy,
            notes = notes,
            status = status
        )
    }
}