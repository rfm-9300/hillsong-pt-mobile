package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rfm.com.entity.Ministry
import rfm.com.entity.MinistryType

@Repository
interface MinistryRepository : MongoRepository<Ministry, String>, MinistryRepositoryCustom {

    fun findByIsActiveTrue(pageable: Pageable): Page<Ministry>

    fun findByIsActiveTrueAndType(type: MinistryType, pageable: Pageable): Page<Ministry>

    fun findByIdAndIsActiveTrue(id: String): Ministry?

    fun findByType(type: MinistryType): List<Ministry>

    fun existsByTypeAndName(type: MinistryType, name: String): Boolean
}
