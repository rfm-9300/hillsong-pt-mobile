package rfm.hillsongptapp.feature.kids.util

/**
 * QR code scanner interface for multiplatform support
 */
interface QRCodeScanner {
    /**
     * Start scanning for QR codes
     * @param onQRCodeDetected Callback when QR code is detected with the decoded text
     * @param onError Callback when an error occurs
     */
    fun startScanning(
        onQRCodeDetected: (String) -> Unit,
        onError: (String) -> Unit
    )
    
    /**
     * Stop scanning
     */
    fun stopScanning()
    
    /**
     * Check if scanner is currently active
     */
    fun isScanning(): Boolean
}

/**
 * QR code scan result
 */
sealed class QRCodeScanResult {
    data class Success(val data: String) : QRCodeScanResult()
    data class Error(val message: String) : QRCodeScanResult()
}

/**
 * Factory function to create platform-specific QR code scanner
 */
expect fun createQRCodeScanner(context: Any): QRCodeScanner
