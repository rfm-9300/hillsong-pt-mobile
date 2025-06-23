package rfm.hillsongptapp.feature.home.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.UserRepository
import rfm.hillsongptapp.core.data.repository.database.User

class HomeViewModel(
    private val userRepository: UserRepository
): ViewModel() {
    init {
        println("AQUIII")
    }

    fun loginUser(email: String = "rodrigomartins@msn.com", password:String = "feller123") {
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, password)
                if (response.success) {
                    println("Login successful: ${response.data?.token}")
                } else {
                    println("Login failed: ${response.message}")
                }
            } catch (e: Exception) {
                println("Error during login: ${e.message}")
            }
        }
    }

}