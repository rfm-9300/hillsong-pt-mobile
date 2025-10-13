package rfm.hillsongptapp.feature.kids.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.data.model.Child
import rfm.hillsongptapp.core.data.model.KidsService
import rfm.hillsongptapp.feature.kids.ui.model.CheckOutResult
import hillsongptapp.feature.kids.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Check-Out screen for checking out children from services
 */
@Composable
fun CheckOutScreen(
    childId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckOutViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(childId) {
        viewModel.loadChild(childId)
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.child != null -> {
                CheckOutContent(
                    uiState = uiState,
                    childId = childId,
                    onStartCheckOutProcess = { viewModel.startCheckOutProcess() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                EmptyContent()
            }
        }
    }
}

@Composable
private fun CheckOutContent(
    uiState: CheckOutUiState,
    childId: String,
    onStartCheckOutProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = uiState.child?.name ?: stringResource(Res.string.unknown_child),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = stringResource(Res.string.current_status, uiState.child?.status?.getDisplayName() ?: stringResource(Res.string.unknown_status)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                uiState.currentService?.let { service ->
                    Text(
                        text = stringResource(Res.string.service_name, service.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // Check-Out Button
        Button(
            onClick = onStartCheckOutProcess,
            enabled = uiState.canCheckOut && !uiState.isCheckingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isCheckingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (uiState.isCheckingOut) stringResource(Res.string.checking_out) else stringResource(Res.string.start_checkout_process))
        }
        
        if (!uiState.canCheckOut) {
            Text(
                text = uiState.checkOutError ?: stringResource(Res.string.checkout_not_available),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.child_not_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}