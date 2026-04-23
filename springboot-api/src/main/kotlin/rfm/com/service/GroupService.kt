package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.Group
import rfm.com.entity.GroupLocation
import rfm.com.entity.Ministry
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.GroupRepository

/**
 * Service for connection groups (SisterHood, Jovens YxYa, Mens, Casais,
 * 30+, Geral). All endpoints are read-only for the mobile app; writes
 * happen via the admin panel.
 */
@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val fileStorageService: FileStorageService
) {

    private val logger = LoggerFactory.getLogger(GroupService::class.java)

    /**
     * List groups with optional filters. If [latitude] and [longitude] are
     * both non-null, pagination is bypassed and results come back ordered
     * by distance from that point within [radiusKm].
     */
    suspend fun listGroups(
        ministry: Ministry?,
        city: String?,
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double?,
        query: String?,
        pageable: Pageable
    ): Page<GroupSummaryResponse> = withContext(Dispatchers.IO) {
        val hasGeo = latitude != null && longitude != null
        if (hasGeo) {
            val meters = (radiusKm ?: DEFAULT_RADIUS_KM) * 1000.0
            val results = if (ministry != null) {
                groupRepository.findActiveNearByMinistry(ministry, longitude!!, latitude!!, meters)
            } else {
                groupRepository.findActiveNear(longitude!!, latitude!!, meters)
            }
            val filtered = if (!city.isNullOrBlank()) {
                results.filter { it.location.city.equals(city, ignoreCase = true) }
            } else {
                results
            }
            val searched = if (query.isNullOrBlank()) {
                filtered
            } else {
                filtered.filter { it.matchesQuery(query) }
            }
            PageImpl(searched.map { mapToSummary(it) }, pageable, searched.size.toLong())
        } else {
            val page = groupRepository.searchActiveGroups(
                ministry = ministry,
                city = city,
                query = query,
                pageable = pageable
            )
            page.map { mapToSummary(it) }
        }
    }

    suspend fun listAdminGroups(
        ministry: Ministry?,
        city: String?,
        query: String?,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<GroupSummaryResponse> = withContext(Dispatchers.IO) {
        groupRepository.searchGroupsForAdmin(
            ministry = ministry,
            city = city,
            query = query,
            includeInactive = includeInactive,
            pageable = pageable
        ).map { mapToSummary(it) }
    }

    suspend fun getById(id: String, includeInactive: Boolean = false): GroupResponse = withContext(Dispatchers.IO) {
        val group = if (includeInactive) {
            groupRepository.findById(id).orElse(null)
        } else {
            groupRepository.findByIdAndIsActiveTrue(id)
        } ?: throw EntityNotFoundException("Group", id)
        mapToResponse(group)
    }

    suspend fun create(
        request: CreateGroupRequest,
        image: MultipartFile? = null
    ): GroupResponse = withContext(Dispatchers.IO) {
        logger.info("Creating group: ${request.name} (${request.ministry})")

        val imagePath = image?.let { storeImage(it) }

        val group = Group(
            name = request.name,
            ministry = request.ministry,
            description = request.description,
            leaderName = request.leaderName,
            leaderContact = request.leaderContact,
            meetingDay = request.meetingDay,
            meetingTime = request.meetingTime,
            frequency = request.frequency,
            location = request.location.toEntity(),
            imagePath = imagePath,
            maxMembers = request.maxMembers,
            currentMembers = request.currentMembers,
            isActive = request.isActive,
            isJoinable = request.isJoinable,
            tags = request.tags
        )

        val saved = groupRepository.save(group)
        logger.info("Group created with id: ${saved.id}")
        mapToResponse(saved)
    }

    suspend fun update(
        id: String,
        request: UpdateGroupRequest,
        image: MultipartFile? = null
    ): GroupResponse = withContext(Dispatchers.IO) {
        val existing = groupRepository.findById(id).orElse(null)
            ?: throw EntityNotFoundException("Group", id)

        val newImagePath = image?.let {
            existing.imagePath?.takeIf { p -> p.isNotBlank() }?.let(fileStorageService::deleteFile)
            storeImage(it)
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            ministry = request.ministry ?: existing.ministry,
            description = request.description ?: existing.description,
            leaderName = request.leaderName ?: existing.leaderName,
            leaderContact = request.leaderContact ?: existing.leaderContact,
            meetingDay = request.meetingDay ?: existing.meetingDay,
            meetingTime = request.meetingTime ?: existing.meetingTime,
            frequency = request.frequency ?: existing.frequency,
            location = request.location?.toEntity() ?: existing.location,
            imagePath = newImagePath ?: existing.imagePath,
            maxMembers = request.maxMembers ?: existing.maxMembers,
            currentMembers = request.currentMembers ?: existing.currentMembers,
            isActive = request.isActive ?: existing.isActive,
            isJoinable = request.isJoinable ?: existing.isJoinable,
            tags = request.tags ?: existing.tags
        )

        val saved = groupRepository.save(updated)
        logger.info("Group updated: ${saved.id}")
        mapToResponse(saved)
    }

    /**
     * Soft delete: flip isActive to false so the mobile app stops
     * surfacing the group but historical data is preserved.
     */
    suspend fun softDelete(id: String): Boolean = withContext(Dispatchers.IO) {
        val existing = groupRepository.findById(id).orElse(null)
            ?: throw EntityNotFoundException("Group", id)
        if (!existing.isActive) return@withContext true
        groupRepository.save(existing.copy(isActive = false))
        logger.info("Group soft-deleted: $id")
        true
    }

    private fun storeImage(image: MultipartFile): String = try {
        fileStorageService.storeGroupImage(image)
    } catch (ex: Exception) {
        logger.error("Failed to store group image", ex)
        throw RuntimeException("Failed to upload group image: ${ex.message}", ex)
    }

    private fun GroupLocationDto.toEntity() = GroupLocation(
        addressLine = addressLine,
        city = city,
        region = region,
        postalCode = postalCode,
        country = country,
        coordinates = doubleArrayOf(longitude, latitude)
    )

    private fun GroupLocation.toDto() = GroupLocationResponse(
        addressLine = addressLine,
        city = city,
        region = region,
        postalCode = postalCode,
        country = country,
        latitude = latitude,
        longitude = longitude
    )

    private fun mapToResponse(group: Group) = GroupResponse(
        id = group.id!!,
        name = group.name,
        ministry = group.ministry,
        description = group.description,
        leaderName = group.leaderName,
        leaderContact = group.leaderContact,
        meetingDay = group.meetingDay,
        meetingTime = group.meetingTime,
        frequency = group.frequency,
        location = group.location.toDto(),
        imagePath = group.imagePath,
        maxMembers = group.maxMembers,
        currentMembers = group.currentMembers,
        isActive = group.isActive,
        isJoinable = group.isJoinable,
        tags = group.tags,
        createdAt = group.createdAt,
        updatedAt = group.updatedAt
    )

    private fun mapToSummary(group: Group) = GroupSummaryResponse(
        id = group.id!!,
        name = group.name,
        ministry = group.ministry,
        description = group.description,
        leaderName = group.leaderName,
        meetingDay = group.meetingDay,
        meetingTime = group.meetingTime,
        frequency = group.frequency,
        city = group.location.city,
        latitude = group.location.latitude,
        longitude = group.location.longitude,
        imagePath = group.imagePath,
        currentMembers = group.currentMembers,
        maxMembers = group.maxMembers,
        isJoinable = group.isJoinable,
        isActive = group.isActive
    )

    private fun Group.matchesQuery(rawQuery: String): Boolean {
        val query = rawQuery.trim()
        if (query.isBlank()) return true
        return listOf(
            name,
            description,
            leaderName,
            location.addressLine,
            location.city,
            location.region.orEmpty(),
            tags.joinToString(" ")
        ).any { it.contains(query, ignoreCase = true) }
    }

    companion object {
        private const val DEFAULT_RADIUS_KM = 15.0
    }
}
