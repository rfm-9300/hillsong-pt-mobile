package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rfm.com.entity.MembershipStatus
import rfm.com.entity.MinistryMembership
import rfm.com.entity.MinistryRole

@Repository
interface MinistryMembershipRepository : MongoRepository<MinistryMembership, String> {

    fun findByUserIdAndMinistryId(userId: String, ministryId: String): MinistryMembership?

    fun findByUserIdAndStatus(userId: String, status: MembershipStatus): List<MinistryMembership>

    fun findByMinistryIdAndStatus(ministryId: String, status: MembershipStatus, pageable: Pageable): Page<MinistryMembership>

    fun findByMinistryIdAndRoleInAndStatus(
        ministryId: String,
        roles: List<MinistryRole>,
        status: MembershipStatus
    ): List<MinistryMembership>

    fun existsByUserIdAndMinistryIdAndRoleInAndStatus(
        userId: String,
        ministryId: String,
        roles: List<MinistryRole>,
        status: MembershipStatus
    ): Boolean

    fun countByMinistryIdAndStatus(ministryId: String, status: MembershipStatus): Long

    fun countByMinistryIdAndRoleAndStatus(
        ministryId: String,
        role: MinistryRole,
        status: MembershipStatus
    ): Long

    fun findByMinistryId(ministryId: String): List<MinistryMembership>
}
