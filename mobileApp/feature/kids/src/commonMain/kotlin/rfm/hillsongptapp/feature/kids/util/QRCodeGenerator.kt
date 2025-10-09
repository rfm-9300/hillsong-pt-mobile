package rfm.hillsongptapp.feature.kids.util

import androidx.compose.ui.graphics.ImageBitmap
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

/**
 * Utility class for generating QR codes from check-in request tokens
 * Uses the qrose library for QR code generation
 */
object QRCodeGenerator {
    
    /**
     * Generate a QR code from a token string
     * 
     * @param token The check-in request token to encode
     * @return Result containing ImageBitmap on success or error message on failure
     */
    fun generateQRCode(token: String): Result<String> {
        return try {
            if (token.isBlank()) {
                Result.failure(IllegalArgumentException("Token cannot be blank"))
            } else {
                // Return the token as-is for use with rememberQrCodePainter in Compose
                // The actual QR code rendering will be done in the UI layer
                Result.success(token)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to generate QR code: ${e.message}", e))
        }
    }
    
    /**
     * Validate that a token is in the correct format for QR code generation
     * 
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     */
    fun isValidToken(token: String): Boolean {
        // Token should be 64 characters alphanumeric as per design
        return token.isNotBlank() && 
               token.length == 64 && 
               token.all { it.isLetterOrDigit() }
    }
}
