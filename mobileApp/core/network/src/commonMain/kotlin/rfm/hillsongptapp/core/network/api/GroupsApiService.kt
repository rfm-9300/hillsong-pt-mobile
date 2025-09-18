package rfm.hillsongptapp.core.network.api

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import rfm.hillsongptapp.core.network.base.BaseApiService
import rfm.hillsongptapp.core.network.result.NetworkResult

/**
 * Data classes for Groups API
 */
@kotlinx.serialization.Serializable
data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val meetingDay: String,
    val meetingTime: String,
    val location: String,
    val leaderName: String,
    val leaderContact: String,
    val imageUrl: String? = null,
    val maxMembers: Int? = null,
    val currentMembers: Int = 0,
    val isJoinable: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@kotlinx.serialization.Serializable
data class GroupsResponse(
    val groups: List<Group>,
    val totalCount: Int,
    val hasMore: Boolean
)

@kotlinx.serialization.Serializable
data class JoinGroupRequest(
    val groupId: Int,
    val message: String? = null
)

@kotlinx.serialization.Serializable
data class GroupMembershipResponse(
    val success: Boolean,
    val message: String,
    val membershipId: Int? = null
)

/**
 * Groups API service handling all group-related network operations
 */
interface GroupsApiService {
    suspend fun getGroups(page: Int = 0, limit: Int = 20): NetworkResult<GroupsResponse>
    suspend fun getGroup(groupId: Int): NetworkResult<Group>
    suspend fun getGroupsByCategory(category: String): NetworkResult<List<Group>>
    suspend fun joinGroup(request: JoinGroupRequest): NetworkResult<GroupMembershipResponse>
    suspend fun leaveGroup(groupId: Int): NetworkResult<GroupMembershipResponse>
    suspend fun getMyGroups(): NetworkResult<List<Group>>
    suspend fun searchGroups(query: String): NetworkResult<List<Group>>
    
    // Reactive streams
    fun getGroupsStream(): Flow<NetworkResult<GroupsResponse>>
}

/**
 * Implementation of GroupsApiService using Ktor HTTP client
 */
class GroupsApiServiceImpl(
    httpClient: HttpClient,
    baseUrl: String
) : BaseApiService(httpClient, baseUrl), GroupsApiService {
    
    override suspend fun getGroups(page: Int, limit: Int): NetworkResult<GroupsResponse> {
        return safeGet("api/groups") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
        }
    }
    
    override suspend fun getGroup(groupId: Int): NetworkResult<Group> {
        return safeGet("api/groups/$groupId")
    }
    
    override suspend fun getGroupsByCategory(category: String): NetworkResult<List<Group>> {
        return safeGet("api/groups/category/$category")
    }
    
    override suspend fun joinGroup(request: JoinGroupRequest): NetworkResult<GroupMembershipResponse> {
        return safePost("api/groups/${request.groupId}/join", request)
    }
    
    override suspend fun leaveGroup(groupId: Int): NetworkResult<GroupMembershipResponse> {
        return safeDelete("api/groups/$groupId/leave")
    }
    
    override suspend fun getMyGroups(): NetworkResult<List<Group>> {
        return safeGet("api/groups/my-groups")
    }
    
    override suspend fun searchGroups(query: String): NetworkResult<List<Group>> {
        return safeGet("api/groups/search") {
            url {
                parameters.append("q", query)
            }
        }
    }
    
    override fun getGroupsStream(): Flow<NetworkResult<GroupsResponse>> = flow {
        emit(NetworkResult.Loading)
        emit(getGroups())
    }
}