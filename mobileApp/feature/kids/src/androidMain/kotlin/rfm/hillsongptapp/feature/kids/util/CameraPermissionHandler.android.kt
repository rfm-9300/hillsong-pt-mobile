package rfm.hillsongptapp.feature.kids.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Android implementation of camera permission handler
 */
class AndroidCameraPermissionHandler(
    private val activity: ComponentActivity
) : CameraPermissionHandler {
    
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var onResultCallback: ((Boolean) -> Unit)? = null
    
    init {
        // Register permission launcher
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResultCallback?.invoke(isGranted)
            onResultCallback = null
        }
    }
    
    override fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun requestPermission(onResult: (Boolean) -> Unit) {
        onResultCallback = onResult
        permissionLauncher?.launch(Manifest.permission.CAMERA)
    }
    
    override fun shouldShowRationale(): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }
    
    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
}

/**
 * Factory function to create camera permission handler
 */
actual fun createCameraPermissionHandler(context: Any): CameraPermissionHandler {
    require(context is ComponentActivity) { "Context must be ComponentActivity on Android" }
    return AndroidCameraPermissionHandler(context)
}
