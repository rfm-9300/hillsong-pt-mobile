package rfm.hillsongptapp.util.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object UrlOpener {
    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }
}
