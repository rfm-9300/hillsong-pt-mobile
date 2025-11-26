package rfm.hillsongptapp.feature.kids.util

import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.Foundation.NSError
import platform.QuartzCore.CALayer
import platform.UIKit.UIView
import platform.darwin.NSObject
import kotlinx.cinterop.*

/**
 * iOS implementation of QR code scanner using AVFoundation
 */
class IOSQRCodeScanner(
    private val previewView: UIView
) : QRCodeScanner {
    
    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var isCurrentlyScanning = false
    private var onQRCodeDetectedCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
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
        
        setupCaptureSession()
    }
    
    override fun stopScanning() {
        isCurrentlyScanning = false
        captureSession?.stopRunning()
        previewLayer?.removeFromSuperlayer()
        captureSession = null
        previewLayer = null
        onQRCodeDetectedCallback = null
        onErrorCallback = null
    }
    
    override fun isScanning(): Boolean = isCurrentlyScanning
    
    @OptIn(ExperimentalForeignApi::class)
    private fun setupCaptureSession() {
        val session = AVCaptureSession()
        captureSession = session
        
        // Get the default video capture device
        val videoCaptureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        
        if (videoCaptureDevice == null) {
            onErrorCallback?.invoke("No camera available")
            isCurrentlyScanning = false
            return
        }
        
        // Create input from the capture device
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
        
        // Create metadata output for QR code detection
        val metadataOutput = AVCaptureMetadataOutput()
        
        if (session.canAddOutput(metadataOutput)) {
            session.addOutput(metadataOutput)
            
            // Set delegate for metadata output
            val delegate = QRCodeMetadataDelegate(
                onQRCodeDetected = { qrCode ->
                    onQRCodeDetectedCallback?.invoke(qrCode)
                    stopScanning()
                }
            )
            metadataOutput.setMetadataObjectsDelegate(delegate, null)
            metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        } else {
            onErrorCallback?.invoke("Failed to add metadata output to session")
            isCurrentlyScanning = false
            return
        }
        
        // Create preview layer
        val preview = AVCaptureVideoPreviewLayer(session = session)
        preview.frame = previewView.bounds
        preview.videoGravity = AVLayerVideoGravityResizeAspectFill
        previewView.layer.addSublayer(preview)
        previewLayer = preview
        
        // Start the session
        session.startRunning()
    }
    
    private class QRCodeMetadataDelegate(
        private val onQRCodeDetected: (String) -> Unit
    ) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
        
        override fun captureOutput(
            output: AVCaptureOutput,
            didOutputMetadataObjects: List<*>,
            fromConnection: AVCaptureConnection
        ) {
            for (metadata in didOutputMetadataObjects) {
                if (metadata is AVMetadataMachineReadableCodeObject) {
                    if (metadata.type == AVMetadataObjectTypeQRCode) {
                        metadata.stringValue?.let { qrCode ->
                            onQRCodeDetected(qrCode)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Factory function to create iOS QR code scanner
 */
actual fun createQRCodeScanner(context: Any): QRCodeScanner {
    require(context is UIView) { "Context must be UIView on iOS" }
    return IOSQRCodeScanner(context)
}
