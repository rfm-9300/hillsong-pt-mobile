package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkException
import rfm.hillsongptapp.core.network.result.NetworkResult

@kotlinx.serialization.Serializable
enum class Ministry {
    SISTERHOOD,
    JOVENS_YXYA,
    MENS,
    CASAIS,
    THIRTY_PLUS,
    GERAL,
}

@kotlinx.serialization.Serializable
enum class MeetingFrequency {
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
}

@kotlinx.serialization.Serializable
enum class GroupDayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
}

@kotlinx.serialization.Serializable
data class GroupLocation(
    val addressLine: String,
    val city: String,
    val region: String? = null,
    val postalCode: String? = null,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)

@kotlinx.serialization.Serializable
data class GroupSummary(
    val id: String,
    val name: String,
    val description: String,
    val ministry: Ministry,
    val leaderName: String,
    val meetingDay: GroupDayOfWeek,
    val meetingTime: String,
    val frequency: MeetingFrequency,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val imagePath: String? = null,
    val currentMembers: Int = 0,
    val maxMembers: Int? = null,
    val isJoinable: Boolean = true,
    val isActive: Boolean = true,
)

@kotlinx.serialization.Serializable
data class Group(
    val id: String,
    val name: String,
    val ministry: Ministry,
    val description: String,
    val leaderName: String,
    val leaderContact: String,
    val meetingDay: GroupDayOfWeek,
    val meetingTime: String,
    val frequency: MeetingFrequency,
    val location: GroupLocation,
    val imagePath: String? = null,
    val maxMembers: Int? = null,
    val currentMembers: Int = 0,
    val isActive: Boolean = true,
    val isJoinable: Boolean = true,
    val tags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

@kotlinx.serialization.Serializable
data class GroupsPageResponse(
    val content: List<GroupSummary>,
    val totalPages: Int,
    val totalElements: Long,
    val number: Int,
    val size: Int,
)

@kotlinx.serialization.Serializable
data class MinistryOption(
    val value: Ministry,
    val labelEn: String,
    val labelPt: String,
)

interface GroupsApiService {
    suspend fun getGroups(
        page: Int = 0,
        size: Int = 50,
        ministry: Ministry? = null,
        city: String? = null,
        query: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
    ): NetworkResult<GroupsPageResponse>

    suspend fun getGroup(groupId: String): NetworkResult<Group>
    suspend fun getMinistries(): NetworkResult<List<MinistryOption>>
}

class GroupsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), GroupsApiService {

    override suspend fun getGroups(
        page: Int,
        size: Int,
        ministry: Ministry?,
        city: String?,
        query: String?,
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double?,
    ): NetworkResult<GroupsPageResponse> {
        return safeGet<ApiResponse<GroupsPageResponse>>("api/groups") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
                parameters.append("sortBy", "name")
                parameters.append("sortDir", "asc")
                ministry?.let { parameters.append("ministry", it.name) }
                city?.takeIf(String::isNotBlank)?.let { parameters.append("city", it) }
                query?.takeIf(String::isNotBlank)?.let { parameters.append("q", it) }
                latitude?.let { parameters.append("lat", it.toString()) }
                longitude?.let { parameters.append("lng", it.toString()) }
                radiusKm?.let { parameters.append("radiusKm", it.toString()) }
            }
        }.unwrap()
    }

    override suspend fun getGroup(groupId: String): NetworkResult<Group> {
        return safeGet<ApiResponse<Group>>("api/groups/$groupId").unwrap()
    }

    override suspend fun getMinistries(): NetworkResult<List<MinistryOption>> {
        return safeGet<ApiResponse<List<MinistryOption>>>("api/groups/ministries").unwrap()
    }

    private fun <T> NetworkResult<ApiResponse<T>>.unwrap(): NetworkResult<T> {
        return when (this) {
            is NetworkResult.Success -> data.data?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error(NetworkException.UnknownError("No data"))
            is NetworkResult.Error -> this
            is NetworkResult.Loading -> this
        }
    }
}
