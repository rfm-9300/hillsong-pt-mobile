package rfm.com.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import rfm.com.entity.MembershipStatus
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryMembership
import rfm.com.entity.MinistryRole
import rfm.com.entity.MinistryType
import rfm.com.entity.Role
import rfm.com.repository.GroupRepository
import rfm.com.repository.MinistryMembershipRepository
import rfm.com.repository.MinistryRepository
import rfm.com.repository.RoleRepository
import rfm.com.repository.UserRepository

/**
 * Bootstraps the Ministry domain:
 *   1) Inserts the STAFF role (idempotent).
 *   2) Creates one default Ministry per MinistryType so connection groups
 *      can be backfilled with a parent ministry id.
 *   3) Backfills `Group.ministryId` from `Group.ministry` enum.
 *   4) For every Group with a leaderName, attempts to match a User by
 *      name; if matched, sets `Group.leaderUserId` and creates a
 *      LEADER membership in the parent ministry.
 *
 * Runs once on application start. Safe to re-run.
 */
@Component
@ConditionalOnProperty(name = ["app.ministries.seed.enabled"], havingValue = "true", matchIfMissing = true)
class MinistrySeeder(
    private val ministryRepository: MinistryRepository,
    private val membershipRepository: MinistryMembershipRepository,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {

    private val logger = LoggerFactory.getLogger(MinistrySeeder::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun seed() {
        ensureStaffRole()
        val byType = ensureDefaultMinistries()
        backfillGroupsToMinistries(byType)
        backfillGroupLeaders(byType)
    }

    private fun ensureStaffRole() {
        if (roleRepository.findByName("STAFF").isEmpty) {
            roleRepository.save(Role(name = "STAFF"))
            logger.info("STAFF role created")
        }
    }

    private fun ensureDefaultMinistries(): Map<MinistryType, Ministry> {
        val labels = mapOf(
            MinistryType.SISTERHOOD to ("Sisterhood" to "Comunidade de mulheres."),
            MinistryType.JOVENS_YXYA to ("Jovens YxYa" to "Jovens adultos."),
            MinistryType.MENS to ("Homens" to "Comunidade dos homens."),
            MinistryType.CASAIS to ("Casais" to "Comunidade dos casais."),
            MinistryType.THIRTY_PLUS to ("30+" to "Comunidade dos 30+."),
            MinistryType.GERAL to ("Geral" to "Comunidade geral."),
            MinistryType.WORSHIP to ("Equipa de Louvor" to "Equipa de louvor e adoração."),
            MinistryType.KIDS to ("Hillsong Kids" to "Ministério das crianças."),
            MinistryType.USHERS to ("Recepção" to "Equipa de recepção e ushers."),
            MinistryType.HOSPITALITY to ("Hospitalidade" to "Equipa de hospitalidade e café."),
            MinistryType.MEDIA to ("Multimédia" to "Equipa de multimédia."),
            MinistryType.PRODUCTION to ("Produção" to "Som, luz e produção técnica."),
            MinistryType.CONNECT to ("Connect" to "Equipa de integração."),
            MinistryType.CAFE to ("Café" to "Equipa do café.")
        )

        val result = mutableMapOf<MinistryType, Ministry>()
        for (type in MinistryType.values()) {
            val (name, description) = labels[type] ?: (type.name to type.name)
            val existing = ministryRepository.findByType(type).firstOrNull { it.name == name }
            if (existing != null) {
                result[type] = existing
                continue
            }
            val saved = ministryRepository.save(
                Ministry(
                    name = name,
                    description = description,
                    type = type,
                    isJoinable = false,
                    requiresApproval = true
                )
            )
            logger.info("Default ministry created: ${saved.name} (${saved.type})")
            result[type] = saved
        }
        return result
    }

    private fun backfillGroupsToMinistries(byType: Map<MinistryType, Ministry>) {
        val groups = groupRepository.findAll().filter { it.ministryId == null }
        if (groups.isEmpty()) return
        var updated = 0
        groups.forEach { g ->
            val parent = byType[g.ministry] ?: return@forEach
            groupRepository.save(g.copy(ministryId = parent.id))
            updated++
        }
        if (updated > 0) logger.info("Backfilled ministryId on $updated groups")
    }

    private fun backfillGroupLeaders(byType: Map<MinistryType, Ministry>) {
        val groups = groupRepository.findAll().filter { it.leaderUserId == null && it.leaderName.isNotBlank() }
        if (groups.isEmpty()) return

        val users = userRepository.findAll()
        val byFullName = users.groupBy { it.fullName.lowercase() }
        var matched = 0
        groups.forEach { group ->
            val match = byFullName[group.leaderName.trim().lowercase()]?.singleOrNull() ?: return@forEach
            val parent = group.ministryId?.let { ministryRepository.findById(it).orElse(null) }
                ?: byType[group.ministry] ?: return@forEach
            groupRepository.save(group.copy(leaderUserId = match.id, ministryId = parent.id))

            val existing = membershipRepository.findByUserIdAndMinistryId(match.id!!, parent.id!!)
            if (existing == null) {
                membershipRepository.save(
                    MinistryMembership(
                        userId = match.id,
                        ministryId = parent.id,
                        role = MinistryRole.LEADER,
                        status = MembershipStatus.ACTIVE,
                        addedBy = "seeder"
                    )
                )
                if (match.id !in parent.leaderUserIds) {
                    ministryRepository.save(parent.copy(leaderUserIds = parent.leaderUserIds + match.id))
                }
            }
            matched++
        }
        if (matched > 0) logger.info("Backfilled $matched group leaders into ministry memberships")
    }
}
