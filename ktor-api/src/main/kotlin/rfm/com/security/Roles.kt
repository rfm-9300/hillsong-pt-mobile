package rfm.com.security

import rfm.com.data.db.user.UserRepository
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.getKoin

object Roles {
    val userRepository = getKoin().get<UserRepository>()
    enum class Role {
        ADMIN,
        USER
    }
    fun returnRole(userId: Int): Role {
        // launch coroutine to get user role
        var role = Role.USER
        runBlocking {
            val user = userRepository.getUserProfile(userId)
            if (user != null) {
                role = if (user.isAdmin) Role.ADMIN else Role.USER
            }
        }
        return role
    }
}