package rfm.hillsongptapp.feature.qrcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    title: String,
    instructions: String,
    onNavigateBack: () -> Unit,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    validator: QrCodeValidator = QrCodeValidator.AnyNonBlank,
) {
    var permissionState by remember { mutableStateOf(PermissionState.NOT_REQUESTED) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                    CameraPreviewWithScanner(
                        onQrCodeDetected = { qrCode ->
                            when (val result = validator.validate(qrCode)) {
                                QrValidationResult.Valid -> onQrScanned(qrCode)
                                is QrValidationResult.Invalid -> error = result.message
                            }
                        },
                        onError = { error = it }
                    )

                    ScannerOverlay(instructions = instructions)
                }

                PermissionState.DENIED,
                PermissionState.NOT_REQUESTED -> {
                    PermissionRequestContent(
                        instructions = instructions,
                        onRequestPermission = {
                            permissionState = PermissionState.GRANTED
                        }
                    )
                }

                PermissionState.DENIED_PERMANENTLY -> {
                    PermissionDeniedPermanentlyContent(onOpenSettings = {})
                }
            }

            if (error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { error = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(error.orEmpty())
                }
            }
        }
    }
}

@Composable
expect fun CameraPreviewWithScanner(
    onQrCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
private fun ScannerOverlay(
    instructions: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.Transparent)
            ) {
                ScannerCorners()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Position QR code within frame",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BoxScope.ScannerCorners() {
    val cornerSize = 40.dp
    val cornerThickness = 4.dp
    val cornerColor = MaterialTheme.colorScheme.primary

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

@Composable
private fun PermissionRequestContent(
    instructions: String,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
            text = instructions,
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
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
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
            text = "Camera permission has been denied. Enable it in your device settings to scan QR codes.",
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
