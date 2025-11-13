package rfm.hillsongptapp.feature.videoplayer.ui

import android.graphics.Color
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import rfm.hillsongptapp.logging.LoggerHelper

/**
 * WebView-based YouTube Player with proper origin parameter support
 * This fixes Error Code 15 by including the origin parameter in the YouTube iframe API
 */
@Composable
fun WebViewYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = false
) {
    LoggerHelper.logDebug("WebViewYouTubePlayer: Loading videoId=$videoId", "WebViewYouTubePlayer")

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false

                    // Allow loading external content (YouTube iframe API)
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // Set a proper User-Agent to avoid restrictions
                    userAgentString = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                setBackgroundColor(Color.BLACK)

                // Enable WebView debugging in debug builds
                WebView.setWebContentsDebuggingEnabled(true)

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            LoggerHelper.logDebug(
                                "JS Console [${it.messageLevel()}]: ${it.message()} (${it.sourceId()}:${it.lineNumber()})",
                                "WebViewYouTubePlayer"
                            )
                        }
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        LoggerHelper.logDebug("✅ WebView page loaded: $url", "WebViewYouTubePlayer")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        LoggerHelper.logDebug(
                            "❌ WebView Error: $errorCode - $description at $failingUrl",
                            "WebViewYouTubePlayer"
                        )
                    }
                }

                val html = createYouTubePlayerHTML(videoId, autoplay)
                LoggerHelper.logDebug("Loading HTML for video: $videoId", "WebViewYouTubePlayer")

                // Use base URL to allow loading external scripts
                loadDataWithBaseURL(
                    "https://www.youtube.com",
                    html,
                    "text/html",
                    "utf-8",
                    null
                )
            }
        },
        update = { webView ->
            LoggerHelper.logDebug("🔄 WebView updated for videoId=$videoId", "WebViewYouTubePlayer")
            val html = createYouTubePlayerHTML(videoId, autoplay)
            webView.loadDataWithBaseURL(
                "https://www.youtube.com",
                html,
                "text/html",
                "utf-8",
                null
            )
        }
    )
}

/**
 * Creates YouTube Player HTML using direct iframe embed
 * This is simpler and more reliable than the iframe API
 */
private fun createYouTubePlayerHTML(
    videoId: String,
    autoplay: Boolean
): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
            border: 0;
        }
        html, body {
            height: 100%;
            width: 100%;
            background-color: #000000;
            overflow: hidden;
            position: fixed;
        }
        iframe {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border: none;
        }
    </style>
</head>
<body>
    <iframe
        src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=${if (autoplay) 1 else 0}&controls=1&rel=0&playsinline=1&fs=1&modestbranding=1&enablejsapi=1"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
        allowfullscreen>
    </iframe>
</body>
</html>
    """.trimIndent()
}
