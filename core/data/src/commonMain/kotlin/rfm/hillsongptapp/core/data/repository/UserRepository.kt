package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.ktor.ApiService
import rfm.hillsongptapp.core.data.repository.ktor.requests.FacebookAuthRequest
import rfm.hillsongptapp.core.data.repository.ktor.requests.GoogleAuthRequest
import rfm.hillsongptapp.core.data.repository.ktor.requests.LoginRequest
import rfm.hillsongptapp.core.data.repository.ktor.requests.SignUpRequest
import rfm.hillsongptapp.core.data.repository.ktor.responses.LoginResponse
import rfm.hillsongptapp.core.data.repository.ktor.responses.SignUpResponse

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

    suspend fun googleLogin(idToken: String): LoginResponse {
        val apiRequest = GoogleAuthRequest(
            idToken = idToken
        )
        return api.googleLogin(apiRequest)
    }

    suspend fun facebookLogin(accessToken: String): LoginResponse {
        val apiRequest = FacebookAuthRequest(
            accessToken = accessToken
        )
        return api.facebookLogin(apiRequest)
    }

    suspend fun signUp(email: String, password: String, confirmPassword: String, firstName: String, lastName: String): SignUpResponse {
        val apiRequest = SignUpRequest(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            firstName = firstName,
            lastName = lastName
        )
        return api.signUp(apiRequest)
    }
}