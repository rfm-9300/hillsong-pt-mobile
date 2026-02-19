package rfm.com.repository

import org.springframework.data.mongodb.repository.MongoRepository
import rfm.com.entity.Role
import java.util.*

interface RoleRepository : MongoRepository<Role, String> {
    fun findByName(name: String): Optional<Role>
}
