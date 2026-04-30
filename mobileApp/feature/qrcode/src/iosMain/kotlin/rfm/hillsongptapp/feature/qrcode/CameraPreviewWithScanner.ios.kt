package rfm.hillsongptapp.feature.qrcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreviewWithScanner(
    onQrCodeDetected: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier
) {
    var scanner: IOSQrCodeScanner? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        onDispose {
            scanner?.stopScanning()
        }
    }

    UIKitView(
        factory = {
            UIView().apply {
                val qrScanner = IOSQrCodeScanner(this)
                scanner = qrScanner
                qrScanner.startScanning(
                    onQrCodeDetected = onQrCodeDetected,
                    onError = onError
                )
            }
        },
        modifier = modifier
    )
}
