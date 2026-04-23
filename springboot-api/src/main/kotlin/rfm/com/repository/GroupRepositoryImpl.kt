package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import rfm.com.entity.Group
import rfm.com.entity.Ministry

class GroupRepositoryImpl(
    private val mongoTemplate: MongoTemplate
) : GroupRepositoryCustom {

    override fun searchActiveGroups(
        ministry: Ministry?,
        city: String?,
        query: String?,
        pageable: Pageable
    ): Page<Group> {
        val criteria = buildCriteria(
            ministry = ministry,
            city = city,
            query = query,
            includeInactive = false
        )
        return execute(criteria, pageable)
    }

    override fun searchGroupsForAdmin(
        ministry: Ministry?,
        city: String?,
        query: String?,
        includeInactive: Boolean,
        pageable: Pageable
    ): Page<Group> {
        val criteria = buildCriteria(
            ministry = ministry,
            city = city,
            query = query,
            includeInactive = includeInactive
        )
        return execute(criteria, pageable)
    }

    private fun execute(criteria: Criteria, pageable: Pageable): Page<Group> {
        val hasCriteria = criteria.criteriaObject.isNotEmpty()
        val query = if (hasCriteria) Query(criteria) else Query()
        query.with(pageable)
        val content = mongoTemplate.find(query, Group::class.java)
        val countQuery = if (hasCriteria) Query(criteria) else Query()
        val total = mongoTemplate.count(countQuery, Group::class.java)
        return PageImpl(content, pageable, total)
    }

    private fun buildCriteria(
        ministry: Ministry?,
        city: String?,
        query: String?,
        includeInactive: Boolean
    ): Criteria {
        val filters = mutableListOf<Criteria>()

        if (!includeInactive) {
            filters += Criteria.where("isActive").`is`(true)
        }

        ministry?.let {
            filters += Criteria.where("ministry").`is`(it)
        }

        city?.takeIf(String::isNotBlank)?.let {
            filters += Criteria.where("location.city").regex("^${Regex.escape(it)}$", "i")
        }

        query?.trim()?.takeIf(String::isNotBlank)?.let {
            filters += Criteria().orOperator(
                Criteria.where("name").regex(Regex.escape(it), "i"),
                Criteria.where("description").regex(Regex.escape(it), "i"),
                Criteria.where("leaderName").regex(Regex.escape(it), "i"),
                Criteria.where("location.addressLine").regex(Regex.escape(it), "i"),
                Criteria.where("location.city").regex(Regex.escape(it), "i"),
                Criteria.where("location.region").regex(Regex.escape(it), "i"),
                Criteria.where("tags").regex(Regex.escape(it), "i")
            )
        }

        return when (filters.size) {
            0 -> Criteria()
            1 -> filters.first()
            else -> Criteria().andOperator(*filters.toTypedArray())
        }
    }
}
