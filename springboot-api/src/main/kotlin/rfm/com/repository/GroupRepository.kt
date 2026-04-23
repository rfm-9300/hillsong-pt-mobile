package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.Group
import rfm.com.entity.Ministry

/**
 * Repository for connection groups.
 *
 * The primary listing query is assembled at runtime in [GroupService] via
 * MongoTemplate because it needs to mix optional filters (ministry, city,
 * geo radius, text search) against a single collection. The simple
 * finder methods below cover direct lookups and the geo "near me"
 * shortcut used for map views.
 */
@Repository
interface GroupRepository : MongoRepository<Group, String>, GroupRepositoryCustom {

    fun findByIsActiveTrue(pageable: Pageable): Page<Group>

    fun findByIsActiveTrueAndMinistry(ministry: Ministry, pageable: Pageable): Page<Group>

    fun findByIsActiveTrueAndLocationCity(city: String, pageable: Pageable): Page<Group>

    fun findByIsActiveTrueAndMinistryAndLocationCity(
        ministry: Ministry,
        city: String,
        pageable: Pageable
    ): Page<Group>

    fun findByIdAndIsActiveTrue(id: String): Group?

    /**
     * Returns active groups whose coordinates fall within [maxDistanceMeters]
     * of the given [longitude],[latitude], ordered by distance ascending.
     * Uses MongoDB's $nearSphere operator backed by the 2dsphere index on
     * `location.coordinates`.
     */
    @Query(
        """
        {
          'isActive': true,
          'location.coordinates': {
            ${'$'}nearSphere: {
              ${'$'}geometry: { type: 'Point', coordinates: [?0, ?1] },
              ${'$'}maxDistance: ?2
            }
          }
        }
        """
    )
    fun findActiveNear(longitude: Double, latitude: Double, maxDistanceMeters: Double): List<Group>

    @Query(
        """
        {
          'isActive': true,
          'ministry': ?0,
          'location.coordinates': {
            ${'$'}nearSphere: {
              ${'$'}geometry: { type: 'Point', coordinates: [?1, ?2] },
              ${'$'}maxDistance: ?3
            }
          }
        }
        """
    )
    fun findActiveNearByMinistry(
        ministry: Ministry,
        longitude: Double,
        latitude: Double,
        maxDistanceMeters: Double
    ): List<Group>
}
