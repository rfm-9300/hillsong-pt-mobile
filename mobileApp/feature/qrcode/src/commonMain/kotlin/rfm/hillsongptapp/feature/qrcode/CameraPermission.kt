package rfm.hillsongptapp.feature.qrcode

interface CameraPermissionHandler {
    fun isPermissionGranted(): Boolean

    fun requestPermission(onResult: (Boolean) -> Unit)

    fun shouldShowRationale(): Boolean

    fun openAppSettings()
}

enum class PermissionState {
    GRANTED,
    DENIED,
    DENIED_PERMANENTLY,
    NOT_REQUESTED
}

expect fun createCameraPermissionHandler(context: Any): CameraPermissionHandler
