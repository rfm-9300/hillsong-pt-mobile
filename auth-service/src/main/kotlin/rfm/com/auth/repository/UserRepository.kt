package rfm.com.auth.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import rfm.com.auth.model.User
import java.util.Optional

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findByVerificationToken(token: String): Optional<User>
    fun findByResetToken(token: String): Optional<User>
    fun findByGoogleId(googleId: String): Optional<User>
    fun findByFacebookId(facebookId: String): Optional<User>
}
