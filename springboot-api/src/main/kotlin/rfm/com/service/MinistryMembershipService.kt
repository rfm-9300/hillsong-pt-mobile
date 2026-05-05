package rfm.com.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import rfm.com.dto.*
import rfm.com.entity.MembershipStatus
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryMembership
import rfm.com.entity.MinistryRole
import rfm.com.exception.BusinessRuleViolationException
import rfm.com.exception.EntityAlreadyExistsException
import rfm.com.exception.EntityNotFoundException
import rfm.com.repository.MinistryMembershipRepository
import rfm.com.repository.MinistryRepository
import rfm.com.repository.UserRepository
import java.time.LocalDateTime

@Service
class MinistryMembershipService(
    private val membershipRepository: MinistryMembershipRepository,
    private val ministryRepository: MinistryRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(MinistryMembershipService::class.java)

    suspend fun listMembers(
        ministryId: String,
        status: MembershipStatus?,
        role: MinistryRole?,
        query: String?,
        pageable: Pageable
    ): Page<MemberResponse> = withContext(Dispatchers.IO) {
        ministryRepository.findById(ministryId).orElseThrow { EntityNotFoundException("Ministry", ministryId) }

        val effectiveStatus = status ?: MembershipStatus.ACTIVE
        val rawPage = membershipRepository.findByMinistryIdAndStatus(ministryId, effectiveStatus, pageable)

        val filtered = rawPage.content.filter { m ->
            (role == null || m.role == role)
        }

        val users = userRepository.findAllById(filtered.map { it.userId }).associateBy { it.id }
        val mapped = filtered.mapNotNull { m ->
            val u = users[m.userId] ?: return@mapNotNull null
            if (!query.isNullOrBlank()) {
                val q = query.trim()
                val match = listOf(u.firstName, u.lastName, u.email, u.fullName)
                    .any { it.contains(q, ignoreCase = true) }
                if (!match) return@mapNotNull null
            }
            MemberResponse(
                id = m.id!!,
                userId = m.userId,
                ministryId = m.ministryId,
                role = m.role,
                status = m.status,
                firstName = u.firstName,
                lastName = u.lastName,
                fullName = u.fullName,
                email = u.email,
                phone = u.phone.ifBlank { null },
                imagePath = u.imagePath.ifBlank { null },
                joinedAt = m.joinedAt,
                notes = m.notes
            )
        }
        PageImpl(mapped, pageable, rawPage.totalElements)
    }

    suspend fun selfJoin(userId: String, ministryId: String, message: String?): MembershipResponse = withContext(Dispatchers.IO) {
        val ministry = ministryRepository.findByIdAndIsActiveTrue(ministryId)
            ?: throw EntityNotFoundException("Ministry", ministryId)
        if (!ministry.isJoinable) {
            throw BusinessRuleViolationException("Ministry not joinable", "Ministry '${ministry.name}' is not open for self-registration")
        }
        userRepository.findById(userId).orElseThrow { EntityNotFoundException("User", userId) }

        val existing = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
        if (existing != null && existing.status in listOf(MembershipStatus.ACTIVE, MembershipStatus.PENDING)) {
            throw EntityAlreadyExistsException("Membership", "$userId/$ministryId")
        }

        val targetStatus = if (ministry.requiresApproval) MembershipStatus.PENDING else MembershipStatus.ACTIVE
        val membership = (existing?.copy(
            role = MinistryRole.VOLUNTEER,
            status = targetStatus,
            joinedAt = LocalDateTime.now(),
            leftAt = null,
            notes = message ?: existing.notes,
            addedBy = null
        ) ?: MinistryMembership(
            userId = userId,
            ministryId = ministryId,
            role = MinistryRole.VOLUNTEER,
            status = targetStatus,
            notes = message
        )).let(membershipRepository::save)

        toMembershipResponse(membership)
    }

    suspend fun leave(userId: String, ministryId: String): Boolean = withContext(Dispatchers.IO) {
        val existing = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
            ?: return@withContext false
        if (existing.status == MembershipStatus.INACTIVE) return@withContext true

        membershipRepository.save(existing.copy(
            status = MembershipStatus.INACTIVE,
            role = MinistryRole.VOLUNTEER,
            leftAt = LocalDateTime.now()
        ))
        syncDenormalizedLeaderLists(ministryId)
        true
    }

    suspend fun listMyMinistries(userId: String): List<MyMinistryResponse> = withContext(Dispatchers.IO) {
        val memberships = membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE) +
            membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.PENDING)
        if (memberships.isEmpty()) return@withContext emptyList<MyMinistryResponse>()

        val ministries = ministryRepository.findAllById(memberships.map { it.ministryId }).associateBy { it.id }
        memberships.mapNotNull { m ->
            val ministry = ministries[m.ministryId] ?: return@mapNotNull null
            MyMinistryResponse(
                membershipId = m.id!!,
                ministry = ministrySummary(ministry),
                role = m.role,
                status = m.status,
                joinedAt = m.joinedAt
            )
        }
    }

    suspend fun addMember(ministryId: String, request: AddMemberRequest, addedBy: String): MemberResponse = withContext(Dispatchers.IO) {
        val ministry = ministryRepository.findById(ministryId).orElseThrow { EntityNotFoundException("Ministry", ministryId) }
        val user = userRepository.findById(request.userId).orElseThrow { EntityNotFoundException("User", request.userId) }

        val existing = membershipRepository.findByUserIdAndMinistryId(request.userId, ministryId)
        val saved = if (existing != null) {
            membershipRepository.save(existing.copy(
                role = request.role,
                status = MembershipStatus.ACTIVE,
                notes = request.notes ?: existing.notes,
                addedBy = addedBy,
                approvedBy = addedBy,
                leftAt = null
            ))
        } else {
            membershipRepository.save(MinistryMembership(
                userId = request.userId,
                ministryId = ministryId,
                role = request.role,
                status = MembershipStatus.ACTIVE,
                notes = request.notes,
                addedBy = addedBy,
                approvedBy = addedBy
            ))
        }

        syncDenormalizedLeaderLists(ministry.id!!)

        toMemberResponse(saved, user)
    }

    suspend fun updateMember(
        ministryId: String,
        userId: String,
        request: UpdateMemberRequest
    ): MemberResponse = withContext(Dispatchers.IO) {
        val ministry = ministryRepository.findById(ministryId).orElseThrow { EntityNotFoundException("Ministry", ministryId) }
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
            ?: throw EntityNotFoundException("Membership", "$userId/$ministryId")
        val user = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User", userId) }

        val newRole = request.role ?: membership.role
        val newStatus = request.status ?: membership.status

        val saved = membershipRepository.save(membership.copy(
            role = newRole,
            status = newStatus,
            notes = request.notes ?: membership.notes,
            leftAt = if (newStatus == MembershipStatus.INACTIVE) (membership.leftAt ?: LocalDateTime.now()) else null
        ))
        syncDenormalizedLeaderLists(ministry.id!!)

        toMemberResponse(saved, user)
    }

    suspend fun removeMember(ministryId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId) ?: return@withContext false
        membershipRepository.save(membership.copy(
            status = MembershipStatus.INACTIVE,
            role = MinistryRole.VOLUNTEER,
            leftAt = LocalDateTime.now()
        ))
        syncDenormalizedLeaderLists(ministryId)
        true
    }

    suspend fun approve(ministryId: String, userId: String, approver: String): MemberResponse = withContext(Dispatchers.IO) {
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
            ?: throw EntityNotFoundException("Membership", "$userId/$ministryId")
        val user = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User", userId) }

        val saved = membershipRepository.save(membership.copy(
            status = MembershipStatus.ACTIVE,
            approvedBy = approver
        ))
        syncDenormalizedLeaderLists(ministryId)
        toMemberResponse(saved, user)
    }

    suspend fun reject(ministryId: String, userId: String, reason: String?, processedBy: String): MemberResponse = withContext(Dispatchers.IO) {
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
            ?: throw EntityNotFoundException("Membership", "$userId/$ministryId")
        val user = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User", userId) }

        val saved = membershipRepository.save(membership.copy(
            status = MembershipStatus.REJECTED,
            approvedBy = processedBy,
            notes = listOfNotNull(membership.notes, reason?.let { "Rejected: $it" }).joinToString(" | ").ifBlank { null }
        ))
        syncDenormalizedLeaderLists(ministryId)
        toMemberResponse(saved, user)
    }

    suspend fun assignLeader(ministryId: String, request: AssignLeaderRequest, assignedBy: String): MemberResponse =
        addMember(ministryId, AddMemberRequest(userId = request.userId, role = request.role), assignedBy)

    suspend fun revokeLeader(ministryId: String, userId: String): MemberResponse = withContext(Dispatchers.IO) {
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId)
            ?: throw EntityNotFoundException("Membership", "$userId/$ministryId")
        val user = userRepository.findById(userId).orElseThrow { EntityNotFoundException("User", userId) }

        val saved = membershipRepository.save(membership.copy(role = MinistryRole.VOLUNTEER))
        syncDenormalizedLeaderLists(ministryId)
        toMemberResponse(saved, user)
    }

    private fun syncDenormalizedLeaderLists(ministryId: String) {
        val ministry = ministryRepository.findById(ministryId).orElse(null) ?: return
        val activeLeaders = membershipRepository.findByMinistryIdAndRoleInAndStatus(
            ministryId,
            listOf(MinistryRole.LEADER, MinistryRole.COORDINATOR),
            MembershipStatus.ACTIVE
        )
        val leaderUserIds = activeLeaders
            .filter { it.role == MinistryRole.LEADER }
            .map { it.userId }
            .distinct()
        val coordinatorUserIds = activeLeaders
            .filter { it.role == MinistryRole.COORDINATOR }
            .map { it.userId }
            .distinct()

        if (ministry.leaderUserIds != leaderUserIds || ministry.coordinatorUserIds != coordinatorUserIds) {
            ministryRepository.save(ministry.copy(
                leaderUserIds = leaderUserIds,
                coordinatorUserIds = coordinatorUserIds
            ))
        }
    }

    private fun ministrySummary(ministry: Ministry) = MinistrySummaryResponse(
        id = ministry.id!!,
        name = ministry.name,
        description = ministry.description,
        type = ministry.type,
        imagePath = ministry.imagePath,
        city = ministry.location?.city,
        isActive = ministry.isActive,
        isJoinable = ministry.isJoinable,
        requiresApproval = ministry.requiresApproval,
        memberCount = membershipRepository.countByMinistryIdAndStatus(ministry.id, MembershipStatus.ACTIVE),
        leaderCount = ministry.leaderUserIds.size + ministry.coordinatorUserIds.size
    )

    private fun toMembershipResponse(m: MinistryMembership) = MembershipResponse(
        id = m.id!!,
        userId = m.userId,
        ministryId = m.ministryId,
        role = m.role,
        status = m.status,
        joinedAt = m.joinedAt,
        approvedBy = m.approvedBy,
        addedBy = m.addedBy,
        notes = m.notes
    )

    private fun toMemberResponse(m: MinistryMembership, user: rfm.com.entity.User) = MemberResponse(
        id = m.id!!,
        userId = m.userId,
        ministryId = m.ministryId,
        role = m.role,
        status = m.status,
        firstName = user.firstName,
        lastName = user.lastName,
        fullName = user.fullName,
        email = user.email,
        phone = user.phone.ifBlank { null },
        imagePath = user.imagePath.ifBlank { null },
        joinedAt = m.joinedAt,
        notes = m.notes
    )
}
