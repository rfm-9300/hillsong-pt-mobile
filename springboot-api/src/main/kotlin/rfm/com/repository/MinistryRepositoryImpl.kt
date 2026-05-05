package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryType

class MinistryRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : MinistryRepositoryCustom {

    override fun searchMinistries(
        type: MinistryType?,
        city: String?,
        query: String?,
        joinableOnly: Boolean,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<Ministry> {
        val filters = mutableListOf<Criteria>()

        if (!includeInactive) filters += Criteria.where("isActive").`is`(true)
        if (joinableOnly) filters += Criteria.where("isJoinable").`is`(true)
        type?.let { filters += Criteria.where("type").`is`(it) }
        city?.takeIf(String::isNotBlank)?.let {
            filters += Criteria.where("location.city").regex("^${Regex.escape(it)}$", "i")
        }
        query?.trim()?.takeIf(String::isNotBlank)?.let {
            filters += Criteria().orOperator(
                Criteria.where("name").regex(Regex.escape(it), "i"),
                Criteria.where("description").regex(Regex.escape(it), "i"),
                Criteria.where("tags").regex(Regex.escape(it), "i")
            )
        }

        val criteria = when (filters.size) {
            0 -> Criteria()
            1 -> filters.first()
            else -> Criteria().andOperator(*filters.toTypedArray())
        }

        val hasCriteria = criteria.criteriaObject.isNotEmpty()
        val q = if (hasCriteria) Query(criteria) else Query()
        q.with(pageable)
        val content = mongoTemplate.find(q, Ministry::class.java)
        val countQuery = if (hasCriteria) Query(criteria) else Query()
        val total = mongoTemplate.count(countQuery, Ministry::class.java)
        return PageImpl(content, pageable, total)
    }
}
