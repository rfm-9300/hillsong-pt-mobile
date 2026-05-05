package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryType

interface MinistryRepositoryCustom {
    fun searchMinistries(
        type: MinistryType?,
        city: String?,
        query: String?,
        joinableOnly: Boolean,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<Ministry>
}
