package rfm.hillsongptapp.core.data.repository

import rfm.hillsongptapp.core.data.repository.database.User
import rfm.hillsongptapp.core.data.repository.database.UserDao
import rfm.hillsongptapp.core.data.repository.database.UserProfile
import rfm.hillsongptapp.core.data.repository.database.UserProfileDao
import rfm.hillsongptapp.core.network.ktor.ApiService
import rfm.hillsongptapp.core.network.ktor.requests.*
import rfm.hillsongptapp.core.network.ktor.responses.*

class UserRepository(
    private val api: ApiService,
    private val userDao: UserDao,
    private val userProfileDao: UserProfileDao
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


    // UserProfile management
    suspend fun insertUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun getUserProfileByUserId(userId: Long): UserProfile? {
        return userProfileDao.getUserProfileByUserId(userId)
    }

    suspend fun deleteUserProfile(profile: UserProfile) {
        userProfileDao.deleteUserProfile(profile)
    }
}