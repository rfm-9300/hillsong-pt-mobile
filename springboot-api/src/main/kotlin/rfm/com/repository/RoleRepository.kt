package rfm.com.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rfm.com.entity.Role

@Repository
interface RoleRepository : JpaRepository<Role, Long> {
    /**
     * Find role by name
     */
    fun findByName(name: String): Role?
    
    /**
     * Check if role exists by name
     */
    fun existsByName(name: String): Boolean
}
