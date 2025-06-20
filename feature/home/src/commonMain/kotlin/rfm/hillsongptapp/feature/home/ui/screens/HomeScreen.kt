package rfm.hillsongptapp.feature.home.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun homeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    // simple composable function for the home screen
    Text(
        text = "Welcome to the Home Screen!",
    )

    // button for login
    Button(onClick = { viewModel.loginUser() }) {
        Text(text = "login")
    }

}
