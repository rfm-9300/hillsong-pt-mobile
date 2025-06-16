package rfm.hillsongptapp.core.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateToHome() {
    navigate(HomeGraph) {
        // Clear the back stack to prevent returning to the login screen
        popUpTo(LoginGraph) {
            inclusive = true
        }
    }
}