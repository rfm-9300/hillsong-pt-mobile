package rfm.hillsongptapp.feature.kids.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.*
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.core.network.ktor.responses.CheckInRequestResponse

/**
 * Screen that displays a QR code for check-in verification by staff Shows child name, service name,
 * expiration countdown, and status updates Connects to WebSocket for real-time status updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeDisplayScreen(
        checkInRequest: CheckInRequestResponse,
        onNavigateBack: () -> Unit,
        onGenerateNewCode: (childId: Long, serviceId: Long) -> Unit,
        modifier: Modifier = Modifier,
        viewModel: CheckInRequestViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track expiration countdown
    var secondsRemaining by remember { mutableStateOf(checkInRequest.expiresInSeconds) }
    var isExpired by remember { mutableStateOf(checkInRequest.isExpired) }

    // Connect to WebSocket when screen is displayed
    LaunchedEffect(Unit) { viewModel.connectWebSocket() }

    // Disconnect from WebSocket when screen is disposed
    DisposableEffect(Unit) { onDispose { viewModel.disconnectWebSocket() } }

    // Handle system back button
    BackHandler {
        onNavigateBack()
    }

    // Update countdown every second
    LaunchedEffect(checkInRequest.id) {
        while (secondsRemaining > 0 && !isExpired) {
            delay(1000)
            secondsRemaining--
            if (secondsRemaining <= 0) {
                isExpired = true
            }
        }
    }

    // Get the current request from state (may have updated status via WebSocket)
    val currentRequest = uiState.currentRequest ?: checkInRequest
    val status = currentRequest.status

    // Show status message if available
    val statusMessage = uiState.statusMessage

    // Show snackbar for status messages
    if (statusMessage != null) {
        LaunchedEffect(statusMessage) {
            // You could show a snackbar here if you have a SnackbarHost
            // For now, we'll just clear it after a delay
            delay(3000)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Check-In QR Code") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Child and Service Information
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                            text = currentRequest.child.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                            text = currentRequest.service.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                            text =
                                    "${currentRequest.service.dayOfWeek} • ${currentRequest.service.startTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = 0.7f
                                    )
                    )
                }
            }

            // Status Message Banner
            if (statusMessage != null) {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                ) {
                    Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Status Display
            when (status) {
                "PENDING" -> {
                    if (isExpired) {
                        ExpiredStatusCard(
                                onGenerateNew = {
                                    onGenerateNewCode(
                                            currentRequest.child.id,
                                            currentRequest.service.id
                                    )
                                }
                        )
                    } else {
                        PendingStatusCard(secondsRemaining = secondsRemaining)

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Display
                        QRCodeDisplay(token = currentRequest.token)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Instructions
                        Text(
                                text = "Show this QR code to staff for check-in verification",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        // Real-time status indicator
                        Text(
                                text = "🔄 Waiting for staff verification...",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                "APPROVED" -> {
                    ApprovedStatusCard(checkInRequest = currentRequest)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Success actions
                    Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Done") }
                }
                "REJECTED" -> {
                    RejectedStatusCard(reason = "Request was rejected by staff")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rejection actions
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                            Text("Go Back")
                        }
                        Button(
                                onClick = {
                                    onGenerateNewCode(
                                            currentRequest.child.id,
                                            currentRequest.service.id
                                    )
                                },
                                modifier = Modifier.weight(1f)
                        ) { Text("Try Again") }
                    }
                }
                "CANCELLED" -> {
                    CancelledStatusCard()

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Go Back")
                    }
                }
                else -> {
                    ExpiredStatusCard(
                            onGenerateNew = {
                                onGenerateNewCode(
                                        currentRequest.child.id,
                                        currentRequest.service.id
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun QRCodeDisplay(token: String, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth().aspectRatio(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            val qrCodePainter =
                    rememberQrCodePainter(token) {
                        shapes {
                            ball = QrBallShape.circle()
                            darkPixel = QrPixelShape.roundCorners()
                            frame = QrFrameShape.roundCorners(.25f)
                        }
                        colors {
                            dark = QrBrush.solid(Color.Black)
                            frame = QrBrush.solid(Color.Black)
                        }
                    }

            Icon(
                    painter = qrCodePainter,
                    contentDescription = "QR Code for check-in",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Unspecified
            )
        }
    }
}

@Composable
private fun PendingStatusCard(secondsRemaining: Long, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "⏳ Pending Verification",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            val minutes = secondsRemaining / 60
            val seconds = secondsRemaining % 60
            Text(
                    text = "Expires in: ${minutes}m ${seconds}s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            LinearProgressIndicator(
                    progress = { (secondsRemaining.toFloat() / (15 * 60)) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun ApprovedStatusCard(
        checkInRequest: CheckInRequestResponse,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f))
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "✓ Checked In Successfully",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
            )
            Text(
                    text = "Your child has been checked in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun RejectedStatusCard(reason: String, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                    )
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "✗ Request Rejected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CancelledStatusCard(modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "Request Cancelled",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = "This check-in request has been cancelled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpiredStatusCard(onGenerateNew: () -> Unit, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier.fillMaxWidth(),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                    )
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                    text = "⏱ Code Expired",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                    text = "This QR code has expired. Generate a new one to check in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
            )

            Button(
                    onClick = onGenerateNew,
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                            )
            ) {
                Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate New Code")
            }
        }
    }
}
