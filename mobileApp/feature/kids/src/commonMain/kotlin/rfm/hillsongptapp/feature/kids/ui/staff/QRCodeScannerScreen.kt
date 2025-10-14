package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rfm.hillsongptapp.feature.kids.util.PermissionState

/**
 * QR Code Scanner Screen for staff to scan parent check-in requests
 * 
 * Features:
 * - Camera preview with QR code detection overlay
 * - Permission handling with rationale
 * - Real-time QR code detection
 * - Navigation to verification screen on successful scan
 * - Error handling for invalid QR codes
 * 
 * Requirements: 3.1, 3.2, 3.4, 3.5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    onNavigateBack: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StaffCheckInViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var permissionState by remember { mutableStateOf(PermissionState.NOT_REQUESTED) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (permissionState) {
                PermissionState.GRANTED -> {
                    // Show camera preview with QR scanner
                    CameraPreviewWithScanner(
                        onQRCodeDetected = { qrCode ->
                            // Validate and extract token
                            if (isValidQRCode(qrCode)) {
                                onQRCodeScanned(qrCode)
                            } else {
                                viewModel.setError("Invalid QR code. Please scan a valid check-in QR code.")
                            }
                        },
                        onError = { error ->
                            viewModel.setError(error)
                        }
                    )
                    
                    // Overlay with scanning frame and instructions
                    ScannerOverlay()
                }
                
                PermissionState.DENIED,
                PermissionState.NOT_REQUESTED -> {
                    // Show permission request UI
                    PermissionRequestContent(
                        onRequestPermission = {
                            // This will be handled by platform-specific code
                            permissionState = PermissionState.GRANTED
                        },
                        showRationale = showPermissionRationale
                    )
                }
                
                PermissionState.DENIED_PERMANENTLY -> {
                    // Show settings redirect UI
                    PermissionDeniedPermanentlyContent(
                        onOpenSettings = {
                            // This will be handled by platform-specific code
                        }
                    )
                }
            }
            
            // Error message display
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(uiState.error ?: "")
                }
            }
        }
    }
}

/**
 * Camera preview with QR code scanner
 * Platform-specific implementation via expect/actual
 */
@Composable
expect fun CameraPreviewWithScanner(
    onQRCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
)

/**
 * Scanner overlay with frame and instructions
 */
@Composable
private fun ScannerOverlay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        
        // Scanning frame in center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanning frame
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.Transparent)
            ) {
                // Corner indicators
                ScannerCorners()
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Instructions
            Text(
                text = "Position QR code within frame",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "The QR code will be scanned automatically",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Scanner corner indicators
 */
@Composable
private fun BoxScope.ScannerCorners() {
    val cornerSize = 40.dp
    val cornerThickness = 4.dp
    val cornerColor = MaterialTheme.colorScheme.primary
    
    // Top-left corner
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .size(cornerSize, cornerThickness)
            .background(cornerColor)
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .size(cornerThickness, cornerSize)
            .background(cornerColor)
    )
    
    // Top-right corner
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(cornerSize, cornerThickness)
            .background(cornerColor)
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(cornerThickness, cornerSize)
            .background(cornerColor)
    )
    
    // Bottom-left corner
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .size(cornerSize, cornerThickness)
            .background(cornerColor)
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .size(cornerThickness, cornerSize)
            .background(cornerColor)
    )
    
    // Bottom-right corner
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(cornerSize, cornerThickness)
            .background(cornerColor)
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(cornerThickness, cornerSize)
            .background(cornerColor)
    )
}

/**
 * Permission request content
 */
@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit,
    showRationale: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (showRationale) {
                "Camera access is required to scan QR codes for check-in verification. " +
                "This ensures children are physically present and properly verified."
            } else {
                "To scan QR codes for child check-in, we need access to your camera."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Camera Permission")
        }
    }
}

/**
 * Permission denied permanently content
 */
@Composable
private fun PermissionDeniedPermanentlyContent(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Permission Denied",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera permission has been permanently denied. " +
                "Please enable it in your device settings to scan QR codes.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Settings")
        }
    }
}

/**
 * Validate QR code format
 * QR codes should contain the check-in request token
 */
private fun isValidQRCode(qrCode: String): Boolean {
    // Token should be 64 characters alphanumeric
    return qrCode.length == 64 && qrCode.all { it.isLetterOrDigit() }
}
