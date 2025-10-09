package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.ktor.requests.ApproveCheckInDto
import rfm.hillsongptapp.core.network.ktor.requests.CreateCheckInRequestDto
import rfm.hillsongptapp.core.network.ktor.requests.RejectCheckInDto
import rfm.hillsongptapp.core.network.ktor.responses.ActiveCheckInRequestsApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInApprovalApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRejectionApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestApiResponse
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestDetailsApiResponse
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Check-In Request API service handling QR code-based check-in operations
 * Follows modern Android architecture patterns with proper error handling
 */
interface CheckInRequestApiService {
    
    /**
     * Create a new check-in request for a child
     * @param request Contains childId, serviceId, and optional notes
     * @return NetworkResult containing CheckInRequestApiResponse with QR code token
     */
    suspend fun createCheckInRequest(request: CreateCheckInRequestDto): NetworkResult<CheckInRequestApiResponse>
    
    /**
     * Get check-in request details by token (for staff verification)
     * @param token The unique token from the QR code
     * @return NetworkResult containing CheckInRequestDetailsApiResponse with child and medical info
     */
    suspend fun getRequestByToken(token: String): NetworkResult<CheckInRequestDetailsApiResponse>
    
    /**
     * Approve a check-in request (staff only)
     * @param token The unique token from the QR code
     * @param request Contains optional notes
     * @return NetworkResult containing CheckInApprovalApiResponse with attendance details
     */
    suspend fun approveCheckIn(token: String, request: ApproveCheckInDto): NetworkResult<CheckInApprovalApiResponse>
    
    /**
     * Reject a check-in request (staff only)
     * @param token The unique token from the QR code
     * @param request Contains required rejection reason
     * @return NetworkResult containing CheckInRejectionApiResponse
     */
    suspend fun rejectCheckIn(token: String, request: RejectCheckInDto): NetworkResult<CheckInRejectionApiResponse>
    
    /**
     * Cancel a pending check-in request (parent only)
     * @param requestId The ID of the check-in request to cancel
     * @return NetworkResult containing success response
     */
    suspend fun cancelRequest(requestId: Long): NetworkResult<CheckInRequestApiResponse>
    
    /**
     * Get all active (pending) check-in requests for the current user's children
     * @return NetworkResult containing list of active check-in requests
     */
    suspend fun getActiveRequests(): NetworkResult<ActiveCheckInRequestsApiResponse>
}

/**
 * Implementation of CheckInRequestApiService using Ktor HTTP client
 */
class CheckInRequestApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), CheckInRequestApiService {
    
    override suspend fun createCheckInRequest(request: CreateCheckInRequestDto): NetworkResult<CheckInRequestApiResponse> {
        return safePost("api/kids/checkin-requests", request)
    }
    
    override suspend fun getRequestByToken(token: String): NetworkResult<CheckInRequestDetailsApiResponse> {
        return safeGet("api/kids/checkin-requests/token/$token")
    }
    
    override suspend fun approveCheckIn(token: String, request: ApproveCheckInDto): NetworkResult<CheckInApprovalApiResponse> {
        return safePost("api/kids/checkin-requests/token/$token/approve", request)
    }
    
    override suspend fun rejectCheckIn(token: String, request: RejectCheckInDto): NetworkResult<CheckInRejectionApiResponse> {
        return safePost("api/kids/checkin-requests/token/$token/reject", request)
    }
    
    override suspend fun cancelRequest(requestId: Long): NetworkResult<CheckInRequestApiResponse> {
        return safeDelete("api/kids/checkin-requests/$requestId")
    }
    
    override suspend fun getActiveRequests(): NetworkResult<ActiveCheckInRequestsApiResponse> {
        return safeGet("api/kids/checkin-requests/active")
    }
}
