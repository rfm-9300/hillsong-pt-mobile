package rfm.hillsongptapp.feature.kids.ui.staff

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import rfm.hillsongptapp.feature.qrcode.QrCodeValidator
import rfm.hillsongptapp.feature.qrcode.QrScannerScreen
import rfm.hillsongptapp.feature.qrcode.QrValidationResult

@Composable
fun QRCodeScannerScreen(
    onNavigateBack: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    QrScannerScreen(
        title = "Scan QR Code",
        instructions = "The QR code will be scanned automatically",
        onNavigateBack = onNavigateBack,
        onQrScanned = onQRCodeScanned,
        modifier = modifier,
        validator = QrCodeValidator { qrCode ->
            if (qrCode.length == 64 && qrCode.all { it.isLetterOrDigit() }) {
                QrValidationResult.Valid
            } else {
                QrValidationResult.Invalid("Invalid QR code. Please scan a valid check-in QR code.")
            }
        }
    )
}
