package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * Wrapper composable that creates a check-in request and displays the QR code
 * This handles the async request creation before showing the QR code screen
 */
@Composable
fun QRCodeDisplayWrapper(
    childId: Long,
    serviceId: Long,
    onNavigateBack: () -> Unit,
    onGenerateNewCode: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckInRequestViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LoggerHelper.logDebug("Composable rendered with childId=$childId, serviceId=$serviceId", "QRCodeDisplayWrapper")
    LoggerHelper.logDebug("UI State: isLoading=${uiState.isLoading}, hasRequest=${uiState.currentRequest != null}, error=${uiState.error}", "QRCodeDisplayWrapper")
    
    // Create check-in request when screen is first displayed
    LaunchedEffect(childId, serviceId) {
        LoggerHelper.logDebug("LaunchedEffect triggered - creating check-in request", "QRCodeDisplayWrapper")
        viewModel.createCheckInRequest(childId, serviceId)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.currentRequest == null -> {
                // Show loading while creating request
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating QR Code...")
                }
            }
            
            uiState.error != null -> {
                // Show error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
            
            uiState.currentRequest != null -> {
                // Show QR code display screen
                QRCodeDisplayScreen(
                    checkInRequest = uiState.currentRequest!!,
                    onNavigateBack = onNavigateBack,
                    onGenerateNewCode = onGenerateNewCode,
                    viewModel = viewModel
                )
            }
        }
    }
}
