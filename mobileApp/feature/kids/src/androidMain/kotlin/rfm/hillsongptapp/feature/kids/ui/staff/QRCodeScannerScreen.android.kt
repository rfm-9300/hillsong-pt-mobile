package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import rfm.hillsongptapp.feature.kids.util.AndroidQRCodeScanner
import rfm.hillsongptapp.feature.kids.util.QRCodeScanner

/**
 * Android implementation of camera preview with QR code scanner
 */
@Composable
actual fun CameraPreviewWithScanner(
    onQRCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scanner: AndroidQRCodeScanner? by remember { mutableStateOf(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            scanner?.cleanup()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                
                // Create scanner with this preview view
                val qrScanner = AndroidQRCodeScanner(lifecycleOwner, this)
                scanner = qrScanner
                
                // Start scanning
                qrScanner.startScanning(
                    onQRCodeDetected = onQRCodeDetected,
                    onError = onError
                )
            }
        },
        modifier = modifier
    )
}
