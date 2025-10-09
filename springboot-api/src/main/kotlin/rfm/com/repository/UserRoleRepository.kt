package rfm.com.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rfm.com.entity.UserRole
import rfm.com.entity.UserRoleId

@Repository
interface UserRoleRepository : JpaRepository<UserRole, UserRoleId> {
    /**
     * Find all roles for a user
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.userId = :userId")
    fun findByUserId(@Param("userId") userId: Long): List<UserRole>
    
    /**
     * Find all users with a specific role
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.roleId = :roleId")
    fun findByRoleId(@Param("roleId") roleId: Long): List<UserRole>
    
    /**
     * Check if user has a specific role
     */
    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    fun existsByUserIdAndRoleId(@Param("userId") userId: Long, @Param("roleId") roleId: Long): Boolean
    
    /**
     * Delete a user's role
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId AND ur.roleId = :roleId")
    fun deleteByUserIdAndRoleId(@Param("userId") userId: Long, @Param("roleId") roleId: Long)
}
