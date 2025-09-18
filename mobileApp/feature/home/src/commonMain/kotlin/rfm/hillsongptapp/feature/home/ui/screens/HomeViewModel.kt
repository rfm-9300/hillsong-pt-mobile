package rfm.hillsongptapp.feature.home.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rfm.hillsongptapp.core.data.repository.AuthRepository

class HomeViewModel(
    private val authRepository: AuthRepository
): ViewModel() {
    init {
        println("AQUIII")
    }

    fun loginUser(email: String = "rodrigomartins@msn.com", password:String = "feller123") {
        viewModelScope.launch {

        }
    }

}