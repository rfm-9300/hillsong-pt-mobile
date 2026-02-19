package rfm.com.auth.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rfm.com.auth.model.Role
import java.util.Optional

@Repository
interface RoleRepository : MongoRepository<Role, String> {
    fun findByName(name: String): Optional<Role>
}
