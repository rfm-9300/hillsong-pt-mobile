package rfm.hillsongptapp.util.platform

/**
 * Platform-specific URL opener
 * Opens URLs in the default browser or YouTube app
 */
expect object UrlOpener {
    fun openUrl(url: String)
}
