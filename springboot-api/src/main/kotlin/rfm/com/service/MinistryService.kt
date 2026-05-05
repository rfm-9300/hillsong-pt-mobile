package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import rfm.com.dto.*
import rfm.com.entity.GroupLocation
import rfm.com.entity.MembershipStatus
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryRole
import rfm.com.entity.MinistryType
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.MinistryMembershipRepository
import rfm.com.repository.MinistryRepository

@Service
class MinistryService(
    private val ministryRepository: MinistryRepository,
    private val membershipRepository: MinistryMembershipRepository,
    private val fileStorageService: FileStorageService
) {

    private val logger = LoggerFactory.getLogger(MinistryService::class.java)

    suspend fun listMinistries(
        type: MinistryType?,
        city: String?,
        query: String?,
        joinableOnly: Boolean,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<MinistrySummaryResponse> = withContext(Dispatchers.IO) {
        ministryRepository.searchMinistries(type, city, query, joinableOnly, includeInactive, pageable)
            .map { mapToSummary(it) }
    }

    suspend fun getById(id: String, includeInactive: Boolean = false): MinistryResponse = withContext(Dispatchers.IO) {
        val ministry = if (includeInactive) {
            ministryRepository.findById(id).orElse(null)
        } else {
            ministryRepository.findByIdAndIsActiveTrue(id)
        } ?: throw EntityNotFoundException("Ministry", id)
        mapToResponse(ministry)
    }

    suspend fun create(request: CreateMinistryRequest, image: MultipartFile? = null): MinistryResponse = withContext(Dispatchers.IO) {
        logger.info("Creating ministry: ${request.name} (${request.type})")
        val imagePath = image?.let { storeImage(it) }
        val ministry = Ministry(
            name = request.name,
            description = request.description,
            type = request.type,
            imagePath = imagePath,
            isActive = request.isActive,
            isJoinable = request.isJoinable,
            requiresApproval = request.requiresApproval,
            tags = request.tags,
            location = request.location?.toEntity(),
            parentMinistryId = request.parentMinistryId
        )
        val saved = ministryRepository.save(ministry)
        logger.info("Ministry created with id: ${saved.id}")
        mapToResponse(saved)
    }

    suspend fun update(id: String, request: UpdateMinistryRequest, image: MultipartFile? = null): MinistryResponse = withContext(Dispatchers.IO) {
        val existing = ministryRepository.findById(id).orElse(null)
            ?: throw EntityNotFoundException("Ministry", id)

        val newImagePath = image?.let {
            existing.imagePath?.takeIf { p -> p.isNotBlank() }?.let(fileStorageService::deleteFile)
            storeImage(it)
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            description = request.description ?: existing.description,
            type = request.type ?: existing.type,
            isActive = request.isActive ?: existing.isActive,
            isJoinable = request.isJoinable ?: existing.isJoinable,
            requiresApproval = request.requiresApproval ?: existing.requiresApproval,
            tags = request.tags ?: existing.tags,
            location = request.location?.toEntity() ?: existing.location,
            imagePath = newImagePath ?: existing.imagePath,
            parentMinistryId = request.parentMinistryId ?: existing.parentMinistryId
        )
        val saved = ministryRepository.save(updated)
        logger.info("Ministry updated: ${saved.id}")
        mapToResponse(saved)
    }

    suspend fun softDelete(id: String): Boolean = withContext(Dispatchers.IO) {
        val existing = ministryRepository.findById(id).orElse(null)
            ?: throw EntityNotFoundException("Ministry", id)
        if (!existing.isActive) return@withContext true
        ministryRepository.save(existing.copy(isActive = false))
        logger.info("Ministry soft-deleted: $id")
        true
    }

    private fun storeImage(image: MultipartFile): String = try {
        fileStorageService.storeMinistryImage(image)
    } catch (ex: Exception) {
        logger.error("Failed to store ministry image", ex)
        throw RuntimeException("Failed to upload ministry image: ${ex.message}", ex)
    }

    fun mapToResponse(ministry: Ministry): MinistryResponse {
        val id = ministry.id!!
        val memberCount = membershipRepository.countByMinistryIdAndStatus(id, MembershipStatus.ACTIVE)
        val pendingCount = membershipRepository.countByMinistryIdAndStatus(id, MembershipStatus.PENDING)
        return MinistryResponse(
            id = id,
            name = ministry.name,
            description = ministry.description,
            type = ministry.type,
            imagePath = ministry.imagePath,
            isActive = ministry.isActive,
            isJoinable = ministry.isJoinable,
            requiresApproval = ministry.requiresApproval,
            tags = ministry.tags,
            location = ministry.location?.toDto(),
            leaderUserIds = ministry.leaderUserIds,
            coordinatorUserIds = ministry.coordinatorUserIds,
            parentMinistryId = ministry.parentMinistryId,
            memberCount = memberCount,
            pendingCount = pendingCount,
            createdAt = ministry.createdAt,
            updatedAt = ministry.updatedAt
        )
    }

    fun mapToSummary(ministry: Ministry): MinistrySummaryResponse {
        val id = ministry.id!!
        val memberCount = membershipRepository.countByMinistryIdAndStatus(id, MembershipStatus.ACTIVE)
        return MinistrySummaryResponse(
            id = id,
            name = ministry.name,
            description = ministry.description,
            type = ministry.type,
            imagePath = ministry.imagePath,
            city = ministry.location?.city,
            isActive = ministry.isActive,
            isJoinable = ministry.isJoinable,
            requiresApproval = ministry.requiresApproval,
            memberCount = memberCount,
            leaderCount = ministry.leaderUserIds.size + ministry.coordinatorUserIds.size
        )
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
}
