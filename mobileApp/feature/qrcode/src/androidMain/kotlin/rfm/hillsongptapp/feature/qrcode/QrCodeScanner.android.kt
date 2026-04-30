package rfm.hillsongptapp.feature.qrcode

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

class AndroidQrCodeScanner(
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) : QrCodeScanner {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isCurrentlyScanning = false
    private var onQrCodeDetectedCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    private val barcodeScanner = BarcodeScanning.getClient()

    override fun startScanning(
        onQrCodeDetected: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isCurrentlyScanning) return

        isCurrentlyScanning = true
        onQrCodeDetectedCallback = onQrCodeDetected
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
        onQrCodeDetectedCallback = null
        onErrorCallback = null
    }

    override fun isScanning(): Boolean = isCurrentlyScanning

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, QrCodeAnalyzer())
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to bind camera: ${e.message}")
            isCurrentlyScanning = false
        }
    }

    private inner class QrCodeAnalyzer : ImageAnalysis.Analyzer {
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
                                    onQrCodeDetectedCallback?.invoke(qrCode)
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

actual fun createQrCodeScanner(context: Any): QrCodeScanner {
    require(context is Pair<*, *>) { "Context must be Pair<LifecycleOwner, PreviewView> on Android" }
    val lifecycleOwner = context.first as LifecycleOwner
    val previewView = context.second as PreviewView
    return AndroidQrCodeScanner(lifecycleOwner, previewView)
}
