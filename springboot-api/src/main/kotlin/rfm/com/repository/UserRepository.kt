package rfm.com.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import rfm.com.entity.User
import java.time.LocalDateTime

@Repository
interface UserRepository : MongoRepository<User, String> {
    
    fun findByEmail(email: String): User?
    
    fun findByAuthId(authId: String): User?
    
    fun existsByEmail(email: String): Boolean
    
    fun findByIsAdminTrue(): List<User>

    @Query("{'joinedAt': {'\$gte': ?0}}")
    fun findUsersCreatedAfter(fromDate: LocalDateTime): List<User>

    override fun findAll(pageable: Pageable): Page<User>
}