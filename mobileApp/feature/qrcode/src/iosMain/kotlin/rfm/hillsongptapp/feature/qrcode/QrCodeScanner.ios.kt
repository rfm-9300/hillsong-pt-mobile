package rfm.hillsongptapp.feature.qrcode

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.Foundation.NSObject
import platform.UIKit.UIView

class IOSQrCodeScanner(
    private val previewView: UIView
) : QrCodeScanner {

    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var metadataDelegate: QrCodeMetadataDelegate? = null
    private var isCurrentlyScanning = false
    private var onQrCodeDetectedCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    override fun startScanning(
        onQrCodeDetected: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isCurrentlyScanning) return

        isCurrentlyScanning = true
        onQrCodeDetectedCallback = onQrCodeDetected
        onErrorCallback = onError

        setupCaptureSession()
    }

    override fun stopScanning() {
        isCurrentlyScanning = false
        captureSession?.stopRunning()
        previewLayer?.removeFromSuperlayer()
        captureSession = null
        previewLayer = null
        metadataDelegate = null
        onQrCodeDetectedCallback = null
        onErrorCallback = null
    }

    override fun isScanning(): Boolean = isCurrentlyScanning

    @OptIn(ExperimentalForeignApi::class)
    private fun setupCaptureSession() {
        val session = AVCaptureSession()
        captureSession = session

        val videoCaptureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (videoCaptureDevice == null) {
            onErrorCallback?.invoke("No camera available")
            isCurrentlyScanning = false
            return
        }

        val videoInput = try {
            AVCaptureDeviceInput.deviceInputWithDevice(videoCaptureDevice, null) as AVCaptureDeviceInput
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to create camera input: ${e.message}")
            isCurrentlyScanning = false
            return
        }

        if (session.canAddInput(videoInput)) {
            session.addInput(videoInput)
        } else {
            onErrorCallback?.invoke("Failed to add camera input to session")
            isCurrentlyScanning = false
            return
        }

        val metadataOutput = AVCaptureMetadataOutput()
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
            val delegate = QrCodeMetadataDelegate(
                onQrCodeDetected = { qrCode ->
                    onQrCodeDetectedCallback?.invoke(qrCode)
                    stopScanning()
                }
            )
            metadataDelegate = delegate
            metadataOutput.setMetadataObjectsDelegate(delegate, null)
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        } else {
            onErrorCallback?.invoke("Failed to add metadata output to session")
            isCurrentlyScanning = false
            return
        }

        val preview = AVCaptureVideoPreviewLayer(session = session)
        preview.frame = previewView.bounds
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewView.layer.addSublayer(preview)
        previewLayer = preview

        session.startRunning()
    }

    private class QrCodeMetadataDelegate(
        private val onQrCodeDetected: (String) -> Unit
    ) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

        override fun captureOutput(
            output: AVCaptureOutput,
            didOutputMetadataObjects: List<*>,
            fromConnection: AVCaptureConnection
        ) {
            for (metadata in didOutputMetadataObjects) {
                if (metadata is AVMetadataMachineReadableCodeObject && metadata.type == AVMetadataObjectTypeQRCode) {
                    metadata.stringValue?.let { qrCode ->
                        onQrCodeDetected(qrCode)
                    }
                }
            }
        }
    }
}

actual fun createQrCodeScanner(context: Any): QrCodeScanner {
    require(context is UIView) { "Context must be UIView on iOS" }
    return IOSQrCodeScanner(context)
}
