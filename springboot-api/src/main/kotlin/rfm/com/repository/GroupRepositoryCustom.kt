package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import rfm.com.entity.Group
import rfm.com.entity.MinistryType

interface GroupRepositoryCustom {
    fun searchActiveGroups(
        ministry: MinistryType?,
        city: String?,
        query: String?,
        pageable: Pageable
    ): Page<Group>

    fun searchGroupsForAdmin(
        ministry: MinistryType?,
        city: String?,
        query: String?,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<Group>
}
