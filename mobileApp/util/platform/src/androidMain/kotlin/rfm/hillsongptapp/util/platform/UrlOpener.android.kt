package rfm.hillsongptapp.util.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

actual object UrlOpener {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    actual fun openUrl(url: String) {
        context?.let { ctx ->
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
