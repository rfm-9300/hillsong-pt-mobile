package rfm.com.data.responses

import rfm.com.data.db.post.Post
import rfm.com.data.db.user.User
import rfm.com.data.db.event.Event
import rfm.com.data.db.service.Service
import rfm.com.data.db.kidsservice.KidsService
import rfm.com.data.db.kidsservice.KidsCheckIn
import rfm.com.data.db.attendance.Attendance
import rfm.com.data.db.attendance.AttendanceWithDetails
import rfm.com.data.db.attendance.AttendanceStats
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success : Boolean,
    val message : String = "",
    val data: ApiResponseData? = null
)

@Serializable
sealed class ApiResponseData {
    @Serializable
    data class AuthResponse(val token: String) : ApiResponseData()
    @Serializable
    data class PostListResponse(val postList: List<Post>) : ApiResponseData()
    @Serializable
    data class SinglePostResponse(val post: Post) : ApiResponseData()
    @Serializable
    data class UserListResponse(val users: List<User>) : ApiResponseData()
    @Serializable
    data class EventListResponse(val events: List<Event>) : ApiResponseData()
    @Serializable
    data class SingleEventResponse(val event: Event) : ApiResponseData()
    @Serializable
    data class ServiceListResponse(val services: List<Service>) : ApiResponseData()
    @Serializable
    data class SingleServiceResponse(val service: Service) : ApiResponseData()
    @Serializable
    data class KidsServiceListResponse(val kidsServices: List<KidsService>) : ApiResponseData()
    @Serializable
    data class SingleKidsServiceResponse(val kidsService: KidsService) : ApiResponseData()
    @Serializable
    data class KidsServiceCapacityResponse(val currentCount: Int, val maxCapacity: Int, val kidsServiceId: Int) : ApiResponseData()
    @Serializable
    data class KidsCheckInListResponse(val checkIns: List<KidsCheckIn>) : ApiResponseData()
    @Serializable
    data class SingleKidsCheckInResponse(val checkIn: KidsCheckIn) : ApiResponseData()
    
    // New attendance-related response types
    @Serializable
    data class AttendanceResponse(val attendance: Attendance) : ApiResponseData()
    @Serializable
    data class AttendanceListResponse(val attendances: List<AttendanceWithDetails>) : ApiResponseData()
    @Serializable
    data class AttendanceStatsResponse(val stats: AttendanceStats) : ApiResponseData()
}