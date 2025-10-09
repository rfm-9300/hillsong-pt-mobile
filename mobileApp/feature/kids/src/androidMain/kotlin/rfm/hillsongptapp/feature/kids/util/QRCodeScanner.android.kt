package rfm.hillsongptapp.feature.kids.util

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Android implementation of QR code scanner using ML Kit and CameraX
 */
class AndroidQRCodeScanner(
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) : QRCodeScanner {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isCurrentlyScanning = false
    private var onQRCodeDetectedCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    private val barcodeScanner = BarcodeScanning.getClient()
    
    override fun startScanning(
        onQRCodeDetected: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isCurrentlyScanning) {
            return
        }
        
        isCurrentlyScanning = true
        onQRCodeDetectedCallback = onQRCodeDetected
        onErrorCallback = onError
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                onError("Failed to start camera: ${e.message}")
                isCurrentlyScanning = false
            }
        }, ContextCompat.getMainExecutor(previewView.context))
    }
    
    override fun stopScanning() {
        isCurrentlyScanning = false
        cameraProvider?.unbindAll()
        onQRCodeDetectedCallback = null
        onErrorCallback = null
    }
    
    override fun isScanning(): Boolean = isCurrentlyScanning
    
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        // Preview use case
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        // Image analysis use case for QR code detection
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, QRCodeAnalyzer())
            }
        
        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()
            
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to bind camera: ${e.message}")
            isCurrentlyScanning = false
        }
    }
    
    private inner class QRCodeAnalyzer : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null && isCurrentlyScanning) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                barcode.rawValue?.let { qrCode ->
                                    onQRCodeDetectedCallback?.invoke(qrCode)
                                    // Stop scanning after first successful detection
                                    stopScanning()
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        onErrorCallback?.invoke("QR code detection failed: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }
    
    fun cleanup() {
        stopScanning()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }
}

/**
 * Factory function to create Android QR code scanner
 */
actual fun createQRCodeScanner(context: Any): QRCodeScanner {
    require(context is Pair<*, *>) { "Context must be Pair<LifecycleOwner, PreviewView> on Android" }
    val lifecycleOwner = context.first as LifecycleOwner
    val previewView = context.second as PreviewView
    return AndroidQRCodeScanner(lifecycleOwner, previewView)
}
