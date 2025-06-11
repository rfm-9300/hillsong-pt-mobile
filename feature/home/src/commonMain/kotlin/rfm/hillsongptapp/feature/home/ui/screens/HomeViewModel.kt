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

    fun createUser() {
        viewModelScope.launch {
            val user = User(
                firstName = "Rodrigo",
                lastName = "Fernandes",
                email = "rodrigo",
                password = "123123",
                phone = "333"
            )
            userRepository.insertUser(user)
            println("User created: $user")
        }

    }

}