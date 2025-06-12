package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.ktor.ApiService
import rfm.hillsongptapp.core.data.repository.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.data.repository.ktor.responses.LoginResponse

class UserRepository(
    private val api: ApiService,
    private val userDao: UserDao
) {
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val apiRequest = LoginRequest(
            email = email,
            password = password
        )
        return api.login(apiRequest)
    }
}