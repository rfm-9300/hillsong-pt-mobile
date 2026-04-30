package rfm.hillsongptapp.feature.qrcode

/**
 * QR code scanner interface for multiplatform support.
 */
interface QrCodeScanner {
    fun startScanning(
        onQrCodeDetected: (String) -> Unit,
        onError: (String) -> Unit
    )

    fun stopScanning()

    fun isScanning(): Boolean
}

sealed class QrValidationResult {
    data object Valid : QrValidationResult()
    data class Invalid(val message: String) : QrValidationResult()
}

fun interface QrCodeValidator {
    fun validate(value: String): QrValidationResult

    companion object {
        val AnyNonBlank = QrCodeValidator { value ->
            if (value.isBlank()) {
                QrValidationResult.Invalid("QR code is empty.")
            } else {
                QrValidationResult.Valid
            }
        }
    }
}

expect fun createQrCodeScanner(context: Any): QrCodeScanner

