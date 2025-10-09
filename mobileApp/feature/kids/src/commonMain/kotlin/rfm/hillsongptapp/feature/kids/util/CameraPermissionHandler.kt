package rfm.hillsongptapp.feature.kids.util

/**
 * Camera permission handler interface for multiplatform support
 */
interface CameraPermissionHandler {
    /**
     * Check if camera permission is granted
     */
    fun isPermissionGranted(): Boolean
    
    /**
     * Request camera permission
     * @param onResult Callback with permission result (true if granted, false if denied)
     */
    fun requestPermission(onResult: (Boolean) -> Unit)
    
    /**
     * Check if we should show permission rationale
     */
    fun shouldShowRationale(): Boolean
    
    /**
     * Open app settings to allow user to grant permission manually
     */
    fun openAppSettings()
}

/**
 * Permission state for UI
 */
enum class PermissionState {
    GRANTED,
    DENIED,
    DENIED_PERMANENTLY,
    NOT_REQUESTED
}

/**
 * Factory function to create platform-specific camera permission handler
 */
expect fun createCameraPermissionHandler(context: Any): CameraPermissionHandler
