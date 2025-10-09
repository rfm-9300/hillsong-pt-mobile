package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRect
import platform.UIKit.UIView
import rfm.hillsongptapp.feature.kids.util.IOSQRCodeScanner

/**
 * iOS implementation of camera preview with QR code scanner
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreviewWithScanner(
    onQRCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    var scanner: IOSQRCodeScanner? by remember { mutableStateOf(null) }
    
    DisposableEffect(Unit) {
        onDispose {
            scanner?.stopScanning()
        }
    }
    
    UIKitView(
        factory = {
            UIView().apply {
                // Create scanner with this view
                val qrScanner = IOSQRCodeScanner(this)
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
