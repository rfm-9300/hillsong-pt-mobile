package rfm.com.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import rfm.com.entity.MembershipStatus
import rfm.com.entity.MinistryRole
import rfm.com.repository.MinistryMembershipRepository
import rfm.com.security.jwt.UserPrincipal

/**
 * SpEL-friendly permission helper used in @PreAuthorize annotations to
 * decide if the current authenticated user can act as a leader for a
 * given ministry.
 *
 * Bean name `ministryPerms` keeps the annotation expressions short:
 *   @PreAuthorize("@ministryPerms.canManageMinistry(authentication, #id)")
 */
@Component("ministryPerms")
class MinistryPermissionEvaluator(
    private val membershipRepository: MinistryMembershipRepository
) {

    private val logger = LoggerFactory.getLogger(MinistryPermissionEvaluator::class.java)

    fun isLeaderOf(authentication: Authentication, ministryId: String): Boolean {
        val userId = currentUserId(authentication) ?: return false
        return membershipRepository.existsByUserIdAndMinistryIdAndRoleInAndStatus(
            userId,
            ministryId,
            LEADER_ROLES,
            MembershipStatus.ACTIVE
        )
    }

    fun canManageMinistry(authentication: Authentication, ministryId: String): Boolean {
        if (hasGlobalRole(authentication, "ADMIN")) return true
        return isLeaderOf(authentication, ministryId)
    }

    fun canViewMinistryStats(authentication: Authentication, ministryId: String): Boolean {
        if (hasGlobalRole(authentication, "ADMIN") || hasGlobalRole(authentication, "STAFF")) return true
        return isLeaderOf(authentication, ministryId)
    }

    fun isMemberOf(authentication: Authentication, ministryId: String): Boolean {
        val userId = currentUserId(authentication) ?: return false
        val membership = membershipRepository.findByUserIdAndMinistryId(userId, ministryId) ?: return false
        return membership.status == MembershipStatus.ACTIVE
    }

    private fun currentUserId(authentication: Authentication): String? = when (val p = authentication.principal) {
        is UserPrincipal -> p.id
        else -> {
            logger.debug("Unsupported principal type for ministry permission check: {}", p?.javaClass?.simpleName)
            null
        }
    }

    private fun hasGlobalRole(authentication: Authentication, role: String): Boolean =
        authentication.authorities?.any { it.authority == "ROLE_$role" } ?: false

    companion object {
        private val LEADER_ROLES = listOf(MinistryRole.LEADER, MinistryRole.COORDINATOR)
    }
}
